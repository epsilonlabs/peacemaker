package org.eclipse.epsilon.peacemaker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
import org.eclipse.epsilon.peacemaker.conflicts.ContainingFeatureUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.DoubleUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.KeepDelete;
import org.eclipse.epsilon.peacemaker.conflicts.ReferenceDoubleUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.UnconflictedObject;
import org.eclipse.epsilon.peacemaker.conflicts.UpdateDelete;
import org.eclipse.epsilon.peacemaker.util.TagBasedEqualityHelper;
import org.eclipse.epsilon.peacemaker.util.ids.IdUtils;

public class PeacemakerResource extends XMIResourceImpl {

	private static final String LEFT_VERSION_EXTENSION = "pmLeftVersion";
	private static final String BASE_VERSION_EXTENSION = "pmBaseVersion";
	private static final String RIGHT_VERSION_EXTENSION = "pmRightVersion";

	// placeholder for those resources where no conflicts are found
	protected XMIResource unconflictedResource;

	protected Map<?, ?> loadOptions = Collections.EMPTY_MAP;

	protected XMIResource leftResource;
	protected XMIResource rightResource;

	protected XMIResource baseResource;
	protected ConflictVersionHelper baseVersionHelper;

	protected String baseVersionName = "";
	protected String leftVersionName = "";
	protected String rightVersionName = "";

	protected List<Conflict> conflicts = new ArrayList<>();

	protected boolean failOnDuplicatedIds = false;
	protected Map<String, List<EObject>> leftDuplicatedIds;
	protected Map<String, List<EObject>> rightDuplicatedIds;

	protected boolean parallelLoad = false;

	public PeacemakerResource(URI uri) {
		super(uri);
	}

	@Override
	protected XMLLoad createXMLLoad() {
		return new PeacemakerXMILoad(createXMLHelper());
	}

	@Override
	protected XMLSave createXMLSave() {
		return new PeacemakerXMISave(createXMLHelper());
	}

	protected void loadLeft(ConflictsPreprocessor preprocessor, Resource.Factory resourceFactory) throws IOException {
		leftVersionName = preprocessor.getLeftVersionName();
		leftResource = loadVersionResource(resourceFactory, LEFT_VERSION_EXTENSION,
				preprocessor.getLeftVersionHelper());
	}

	/**
	 * Direct loading of the base version (for parallel loads only)
	 */
	protected void loadBase(ConflictsPreprocessor preprocessor, Resource.Factory resourceFactory) throws IOException {
		baseVersionName = preprocessor.getBaseVersionName();
		baseResource = loadVersionResource(resourceFactory, BASE_VERSION_EXTENSION,
				preprocessor.getBaseVersionHelper());
	}

	protected void loadRight(ConflictsPreprocessor preprocessor, Resource.Factory resourceFactory) throws IOException {
		rightVersionName = preprocessor.getRightVersionName();
		rightResource = loadVersionResource(resourceFactory, RIGHT_VERSION_EXTENSION,
				preprocessor.getRightVersionHelper());
	}

	/**
	 * Checks if there is ancestor version information, also loads it if not loaded yet
	 */
	public boolean hasBaseResource() {
		if (baseResource != null) {
			return true;
		}
		// if doing sequential load, demand-load the base version
		if (baseVersionHelper != null) {
			if (baseResource == null) {
				demandLoadBase();
			}
			return true;
		}
		return false;
	}

	/**
	 * For sequential loads, base loading is delayed until needed
	 */
	protected void prepareBase(ConflictsPreprocessor preprocessor) {
		// this version is loaded on demand (i.e. if needed to identify conflicts)
		baseVersionName = preprocessor.getBaseVersionName();
		baseVersionHelper = preprocessor.getBaseVersionHelper();
	}

	protected void demandLoadBase() {
		try {
			baseResource = loadVersionResource(getSpecificFactory(), BASE_VERSION_EXTENSION, baseVersionHelper);
		}
		catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected XMIResource loadVersionResource(Resource.Factory resourceFactory, String extension,
			ConflictVersionHelper versionHelper) throws IOException {

		URI versionURI = uri.appendFileExtension(extension);
		XMIResourceImpl specificResource = (XMIResourceImpl) resourceFactory.createResource(versionURI);
		specificResource.basicSetResourceSet(getResourceSet(), null);

		// the pool allows decorating the xml handler to get element lines
		Map<Object, Object> loadOptions = new HashMap<Object, Object>(this.loadOptions);
		loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new PeacemakerXMLParserPoolImpl(this, versionHelper));
		specificResource.load(versionHelper.getVersionContents(), loadOptions);

		return specificResource;
	}

	public void loadUnconflicted(InputStream contents, Map<?, ?> options) throws IOException {
		setLoadOptions(loadOptions);

		// load it as a standard XMI Resource (using specific factories if registered)
		Resource.Factory specificFactory = getSpecificFactory();

		unconflictedResource = (XMIResource) specificFactory.createResource(getURI());
		((XMIResourceImpl) unconflictedResource).basicSetResourceSet(getResourceSet(), null);

		Map<Object, Object> loadOptions = new HashMap<Object, Object>(this.loadOptions);
		loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new PeacemakerXMLParserPoolImpl(this, null));
		unconflictedResource.load(contents, loadOptions);
	}

	protected void identifyConflicts(ConflictsPreprocessor preprocessor) {
		for (ConflictSection cs : preprocessor.getConflictSections()) {
			identifyConflicts(cs);
		}
	}

	protected void identifyConflicts(ConflictSection conflictSection) {
		TagBasedEqualityHelper equalityHelper = new TagBasedEqualityHelper();
		for (String id : conflictSection.getLeftIds()) {

			EObject leftObj = getLeftEObject(id);

			if (conflictSection.rightContains(id)) {
				EObject rightObj = getRightEObject(id);

				Conflict conflict = new DoubleUpdate(id, this);

				if (leftObj.eContents().isEmpty() != rightObj.eContents().isEmpty()) {
					// a false positive might be happening because of the end of
					// the starting tag (i.e. ">" vs "/>")

					// TODO: determine if a line comparison here would be worth
					//   to save some comparison time. If not, simplify code

					if (equalityHelper.equals(leftObj, rightObj)) {
						// features are equal, so false positive
						conflict = null;
					}
				}
				else {
					EStructuralFeature leftFeature = leftObj.eContainingFeature();
					EStructuralFeature rightFeature = rightObj.eContainingFeature();

					if (leftFeature != null && rightFeature != null && leftFeature != rightFeature) {
						conflict = new ContainingFeatureUpdate(id, this,
								leftObj.eContainingFeature(), rightObj.eContainingFeature());
					}
					else {
						// check attributes and non-containment references
						if (equalityHelper.equals(leftObj, rightObj)) {
							// features are equal, so false positive
							conflict = null;
						}
					}
				}

				if (conflict != null) {
					addConflict(conflict);
				}
				conflictSection.removeRight(id);
			}
			else if (checkContainmentReference(id, leftObj, conflictSection)) {
				// special double update case: single containment reference
				// that contains objects with distinct ids in left and right
			}
			else if (hasBaseResource() && conflictSection.baseContains(id)) {
				// object kept(no changes)/updated in left version, and deleted in the right one
				if (equalityHelper.equals(leftObj, getBaseEObject(id))) {
					addConflict(new KeepDelete(id, this, true));
				}
				else {
					addConflict(new UpdateDelete(id, this, true));
				}
			}
			else {
				// not identified as part of a conflict: "free" element to keep or remove
				addConflict(new UnconflictedObject(id, this, true));
			}
		}

		// UpdateDelete conflicts can appear the other way (update in right version, delete in left)
		// loop over right ids to detect those cases
		for (String id : conflictSection.getRightIds()) {
			if (hasBaseResource() && conflictSection.baseContains(id)) {
				// object kept(no changes)/updated in right version, and deleted in the left one
				if (equalityHelper.equals(getRightEObject(id), getBaseEObject(id))) {
					addConflict(new KeepDelete(id, this, false));
				}
				else {
					addConflict(new UpdateDelete(id, this, false));
				}
			}
			else {
				// not identified as part of a conflict: "free" element to keep or remove
				addConflict(new UnconflictedObject(id, this, false));
			}
		}
	}

	protected boolean checkContainmentReference(String leftId, EObject leftObj, ConflictSection conflictSection) {
		EStructuralFeature feature = leftObj.eContainingFeature();

		if (feature != null && isContainmentNotManyReference(feature)) {
			EReference ref = (EReference) feature;

			String parentId = getLeftId(leftObj.eContainer());
			EObject rightParent = getRightEObject(parentId);
			if (rightParent == null) {
				throw new RuntimeException("complicated reference case, study deeper");
			}

			EObject rightObj = (EObject) rightParent.eGet(ref);
			if (rightObj != null && conflictSection.rightContains(getRightId(rightObj))) {
				addConflict(new ReferenceDoubleUpdate(parentId, this, ref));
				conflictSection.removeRight(getRightId(rightObj));
				return true;
			}
		}
		return false;
	}

	protected boolean isContainmentNotManyReference(EStructuralFeature feature) {
		return feature instanceof EReference &&
				((EReference) feature).isContainment() &&
				!((EReference) feature).isMany();
	}

	protected void addConflict(Conflict conflict) {
		conflicts.add(conflict);
	}

	public XMIResource getLeftResource() {
		return leftResource;
	}

	public XMIResource getBaseResource() {
		if (hasBaseResource()) {
			return baseResource;
		}
		return null;
	}

	public XMIResource getRightResource() {
		return rightResource;
	}

	public String getLeftId(EObject obj) {
		return IdUtils.getAvailableId(leftResource, obj);
	}

	public String getRightId(EObject obj) {
		return IdUtils.getAvailableId(rightResource, obj);
	}

	public EObject getLeftEObject(String id) {
		return leftResource.getEObject(id);
	}

	public EObject getBaseEObject(String id) {
		return baseResource.getEObject(id);
	}

	public EObject getRightEObject(String id) {
		return rightResource.getEObject(id);
	}

	public List<Conflict> getConflicts() {
		return conflicts;
	}

	public boolean hasConflicts() {
		return leftResource != null && rightResource != null && !conflicts.isEmpty();
	}

	public void setUnconflictedResource(XMIResource resource) {
		this.unconflictedResource = resource;
	}

	public XMIResource getUnconflictedResource() {
		return unconflictedResource;
	}

	public void setLoadOptions(Map<?, ?> options) {
		if (options != null) {
			this.loadOptions = options;
		}
	}

	public String getBaseVersionName() {
		return baseVersionName;
	}

	public String getLeftVersionName() {
		return leftVersionName;
	}

	public String getRightVersionName() {
		return rightVersionName;
	}

	protected Factory getSpecificFactory() {
		Resource.Factory.Registry factoryRegistry = getResourceSet().getResourceFactoryRegistry();
		if (!(factoryRegistry.getExtensionToFactoryMap().get("*") instanceof PeacemakerResourceFactory)) {
			throw new RuntimeException("A peacemaker factory should be locally registered");
		}
		Object peacemakerFactory = factoryRegistry.getExtensionToFactoryMap().remove("*");
		Resource.Factory specificFactory = factoryRegistry.getFactory(getURI());
		factoryRegistry.getExtensionToFactoryMap().put("*", peacemakerFactory);
		return specificFactory;
	}

	public void loadVersions(ConflictsPreprocessor preprocessor, Map<?, ?> options) throws IOException {
		if (parallelLoad) {
			parallelLoadVersions(preprocessor, options);
		}
		else {
			sequentialLoadVersions(preprocessor, options);
		}
	}

	protected void sequentialLoadVersions(ConflictsPreprocessor preprocessor, Map<?, ?> options) throws IOException {
		setLoadOptions(loadOptions);

		Resource.Factory resourceFactory = getSpecificFactory();

		loadLeft(preprocessor, resourceFactory);
		loadRight(preprocessor, resourceFactory);

		if (preprocessor.hasBaseVersion()) {
			prepareBase(preprocessor);
		}

		identifyConflicts(preprocessor);
	}

	protected void parallelLoadVersions(ConflictsPreprocessor preprocessor, Map<?, ?> loadOptions) {

		setLoadOptions(loadOptions);

		Resource.Factory resourceFactory = getSpecificFactory();

		// parallel load of the three model versions
		ExecutorService versionLoadExecutor;
		if (preprocessor.hasBaseVersion()) {
			versionLoadExecutor = Executors.newFixedThreadPool(3);
		}
		else {
			versionLoadExecutor = Executors.newFixedThreadPool(2);
		}
		
		List<Future<?>> loadingTasks = new ArrayList<>();

		loadingTasks.add(versionLoadExecutor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					loadLeft(preprocessor, resourceFactory);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}));
		
		loadingTasks.add(versionLoadExecutor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					loadRight(preprocessor, resourceFactory);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}));
		
		if (preprocessor.hasBaseVersion()) {
			loadingTasks.add(versionLoadExecutor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						loadBase(preprocessor, resourceFactory);
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}));
		}

		try {
			for (Future<?> task : loadingTasks) {
				task.get();
			}
		}
		catch (InterruptedException e1) {
			e1.printStackTrace(); // TODO: determine a better way to handle this
		}
		catch (ExecutionException executionEx) {
			if (executionEx.getCause() instanceof RuntimeException) {
				throw (RuntimeException) executionEx.getCause();
			}
		}
		versionLoadExecutor.shutdown(); // TODO: use shutdownNow?

		identifyConflicts(preprocessor);
	}

	public boolean failOnDuplicateIds() {
		return failOnDuplicatedIds;
	}

	public void setFailOnDuplicatedIds(boolean failOnDuplicatedIds) {
		this.failOnDuplicatedIds = failOnDuplicatedIds;
	}

	public boolean hasDuplicatedIds() {
		return leftDuplicatedIds != null || rightDuplicatedIds != null;
	}

	public void setLeftDuplicatedIds(Map<String, List<EObject>> duplicatedIds) {
		leftDuplicatedIds = duplicatedIds;

	}

	public void setRightDuplicatedIds(Map<String, List<EObject>> duplicatedIds) {
		rightDuplicatedIds = duplicatedIds;
	}

	public Map<String, List<EObject>> getLeftDuplicatedIds() {
		return leftDuplicatedIds;
	}

	public Map<String, List<EObject>> getRightDuplicatedIds() {
		return rightDuplicatedIds;
	}

	public Map<String, List<EObject>> getDuplicatedIds() {
		return getLeftDuplicatedIds();
	}

	public void setDuplicatedIds(Map<String, List<EObject>> duplicatedIds) {
		setLeftDuplicatedIds(duplicatedIds);
	}

	public void setParallelLoad(boolean parallelLoad) {
		this.parallelLoad = parallelLoad;
	}
}
