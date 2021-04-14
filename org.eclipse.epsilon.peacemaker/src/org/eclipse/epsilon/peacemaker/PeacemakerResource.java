package org.eclipse.epsilon.peacemaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
import org.eclipse.epsilon.peacemaker.conflicts.ContainerUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.ContainingFeatureUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.DoubleUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.DuplicatedId;
import org.eclipse.epsilon.peacemaker.conflicts.KeepDelete;
import org.eclipse.epsilon.peacemaker.conflicts.SingleContainmentReferenceUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.UnconflictedObject;
import org.eclipse.epsilon.peacemaker.conflicts.UpdateDelete;
import org.eclipse.epsilon.peacemaker.util.TagBasedEqualityHelper;
import org.eclipse.epsilon.peacemaker.util.ids.IdUtils;

public class PeacemakerResource extends XMIResourceImpl {

	private static final String LEFT_VERSION_EXTENSION = "pmLeftVersion";
	private static final String BASE_VERSION_EXTENSION = "pmBaseVersion";
	private static final String RIGHT_VERSION_EXTENSION = "pmRightVersion";

	// for those resources where no conflict sections are found
	protected XMIResource singleLoadResource;

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
	protected Map<String, List<EObject>> leftDuplicatedIds = new HashMap<>();
	protected Map<String, List<EObject>> baseDuplicatedIds = new HashMap<>();
	protected Map<String, List<EObject>> rightDuplicatedIds = new HashMap<>();

	protected boolean parallelLoad = false;

	protected TagBasedEqualityHelper equalityHelper = new TagBasedEqualityHelper();

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

	public void loadSingle(ConflictsPreprocessor preprocessor, Map<?, ?> options) throws IOException {
		setLoadOptions(loadOptions);

		// load it as a standard XMI Resource (using specific factories if registered)
		Resource.Factory specificFactory = getSpecificFactory();

		singleLoadResource = (XMIResource) specificFactory.createResource(getURI());
		((XMIResourceImpl) singleLoadResource).basicSetResourceSet(getResourceSet(), null);

		Map<Object, Object> loadOptions = new HashMap<Object, Object>(this.loadOptions);
		loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new PeacemakerXMLParserPoolImpl(this, null));
		singleLoadResource.load(preprocessor.getLeftVersionHelper().getVersionContents(), loadOptions);

		// no conflict sections does not always imply that there are no conflicts
		identifyConflicts(preprocessor);
	}

	protected void identifyConflicts(ConflictsPreprocessor preprocessor) {
		for (ConflictSection cs : preprocessor.getConflictSections()) {
			identifyConflicts(cs);
		}
		// some conflicts can happen outside conflict sections
		identifyDuplicatedIdConflicts();
	}

	protected void identifyDuplicatedIdConflicts() {
		if (isSingleLoad()) {
			// only enough information to flag it as duplicated element
			//   could be a container change, or new additions
			for (Entry<String, List<EObject>> entry : getDuplicatedIds().entrySet()) {
				conflicts.add(new DuplicatedId(entry.getKey(), this));
			}
		}
		else {
			// if the base version contains the resource, treat the conflict as
			//   a container update (gives more precise information)
			for (Entry<String, List<EObject>> entry : leftDuplicatedIds.entrySet()) {
				if (hasBaseResource() && IdUtils.containsObjectWithId(baseResource, entry.getKey())) {
					conflicts.add(new ContainerUpdate(entry.getKey(), this));
				}
				else {
					conflicts.add(new DuplicatedId(entry.getKey(), this));
				}
			}
			for (Entry<String, List<EObject>> entry : rightDuplicatedIds.entrySet()) {
				// prevent adding the same conflict twice
				if (!leftDuplicatedIds.containsKey(entry.getKey())) {
					if (hasBaseResource()
							&& IdUtils.containsObjectWithId(baseResource, entry.getKey())) {

						conflicts.add(new ContainerUpdate(entry.getKey(), this));
					}
					else {
						conflicts.add(new DuplicatedId(entry.getKey(), this));
					}
				}
			}
		}
	}

	protected void identifyConflicts(ConflictSection conflictSection) {
		for (String id : conflictSection.getLeftIds()) {

			EObject leftObj = getLeftEObject(id);

			if (conflictSection.rightContains(id)) {
				EObject rightObj = getRightEObject(id);

				Conflict conflict = new DoubleUpdate(id, this);

				EStructuralFeature leftFeature = leftObj.eContainingFeature();
				EStructuralFeature rightFeature = rightObj.eContainingFeature();
				if (leftFeature != null && rightFeature != null && leftFeature != rightFeature) {
					conflict = new ContainingFeatureUpdate(id, this,
							leftObj.eContainingFeature(), rightObj.eContainingFeature());
				}
				else {
					if (equalityHelper.equals(leftObj, rightObj)) {
						conflict = null; // false positive
					}
				}

				if (conflict != null) {
					addConflict(conflict);
				}
				conflictSection.removeRight(id);
			}
			else if (checkSingleContainmentReference(id, leftObj, conflictSection)) {
				// special double update case: single containment reference
				// that contains objects with distinct ids in left and right
			}
			else if (hasBaseResource() && conflictSection.baseContains(id)) {
				Conflict conflict = getDeleteConflict(id, leftObj,
						leftResource, leftDuplicatedIds,
						rightResource, rightDuplicatedIds);

				if (conflict != null) {
					addConflict(conflict);
				}
			}
			else {
				addConflict(new UnconflictedObject(id, this, true));
			}
		}

		// delete conflicts can appear the other way (delete in left, update in right)
		for (String id : conflictSection.getRightIds()) {
			if (hasBaseResource() && conflictSection.baseContains(id)) {
				Conflict conflict = getDeleteConflict(id, getRightEObject(id),
						rightResource, rightDuplicatedIds,
						leftResource, leftDuplicatedIds);

				if (conflict != null) {
					addConflict(conflict);
				}
			}
			else {
				addConflict(new UnconflictedObject(id, this, false));
			}
		}
	}

	/**
	 * Gets the delete conflict that is taking place. Allows to work in both directions
	 * (e.g. UpdateDelete or DeleteUpdate conflicts)
	 */
	protected Conflict getDeleteConflict(String id, EObject object,
			XMIResource resource, Map<String, List<EObject>> duplicatedIds,
			XMIResource deleteVersionResource, Map<String, List<EObject>> deleteVersionDuplicatedIds) {

		Conflict conflict = null;

		if (duplicatedIds.containsKey(id)) {
			// this is not a delete conflict, is a duplicated id one (identified later)

			// here we know:
			//   1. the id is in our conflict section segment (left or right)
			//   2. that id is not appearing in the other segment of the cs
			//   3. that id is also duplicated in our version resource

			// because of 2, I do not think it is possible to have the
			//   id duplicated in the delete version
			if (deleteVersionDuplicatedIds.containsKey(id)) {
				throw new RuntimeException("Duplicated id in the deleted side of an update delete");
			}

			EObject deleteVersionObject = deleteVersionResource.getEObject(id);
			if (deleteVersionObject != null) {
				// root objects not considered, assumed tree structure 
				EObject deleteVersionObjectContainer = deleteVersionObject.eContainer();
				if (deleteVersionObjectContainer != null) {

					String deleteVersionObjectContainerId =
							IdUtils.getAvailableId(deleteVersionResource, deleteVersionObjectContainer);

					// delete from our version the duplicated object
					//   whose container matches the container from the delete version
					deleteObjectWithMatchingContainer(deleteVersionObjectContainerId,
							resource, duplicatedIds.get(id));

					// and from the base resource too, if duplicated
					if (baseDuplicatedIds.containsKey(id)) {
						deleteObjectWithMatchingContainer(deleteVersionObjectContainerId,
								baseResource, baseDuplicatedIds.get(id));
					}
				}
			}
		}
		else {
			boolean deleteInRightVersion = deleteVersionResource == rightResource;
			// if object kept unmodified
			if (equalityHelper.equals(object, getBaseEObject(id))) {
				conflict = new KeepDelete(id, this, deleteInRightVersion);
			}
			else {
				conflict = new UpdateDelete(id, this, deleteInRightVersion);
			}
		}
		
		return conflict;
	}

	protected void deleteObjectWithMatchingContainer(String containerId, XMIResource resource,
			List<EObject> dupObjects) {

		Iterator<EObject> dupObjectsIterator = dupObjects.iterator();
		while (dupObjectsIterator.hasNext()) {
			EObject dupObject = dupObjectsIterator.next();
			EObject dupObjectContainer = dupObject.eContainer();
			if (dupObjectContainer != null && containerId.equals(
					IdUtils.getAvailableId(resource, dupObjectContainer))) {

				// TODO: check dangling references
				dupObjectsIterator.remove();
				EcoreUtil.remove(dupObject);
			}
		}
		// it could happen that we have removed the element pointed by the idToEObject map
		// reset it to one of the remaining duplicated objects
		if (!dupObjects.isEmpty()) {
			EObject dupObject = dupObjects.get(0);
			String dupObjectId = resource.getID(dupObject);
			if (dupObjectId != null) {
				resource.setID(dupObject, dupObjectId);
			}
		}
	}

	protected boolean checkSingleContainmentReference(String leftId, EObject leftObj, ConflictSection conflictSection) {
		EStructuralFeature feature = leftObj.eContainingFeature();

		if (feature != null && isSignleContainmentReference(feature)) {
			EReference ref = (EReference) feature;

			String parentId = getLeftId(leftObj.eContainer());
			EObject rightParent = getRightEObject(parentId);
			if (rightParent == null) {
				throw new RuntimeException("complicated reference case, study deeper");
			}

			EObject rightObj = (EObject) rightParent.eGet(ref);
			if (rightObj != null && conflictSection.rightContains(getRightId(rightObj))) {
				addConflict(new SingleContainmentReferenceUpdate(parentId, this, ref));
				conflictSection.removeRight(getRightId(rightObj));
				return true;
			}
		}
		return false;
	}

	protected boolean isSignleContainmentReference(EStructuralFeature feature) {
		return feature instanceof EReference &&
				((EReference) feature).isContainment() &&
				!((EReference) feature).isMany();
	}

	public void addConflict(Conflict conflict) {
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
		return !conflicts.isEmpty();
	}

	public boolean isSingleLoad() {
		return singleLoadResource != null;
	}

	public XMIResource getSingleLoadResource() {
		return singleLoadResource;
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
		return hasLeftDuplicatedIds() || hasRightDuplicatedIds();
	}

	protected boolean hasLeftDuplicatedIds() {
		return !leftDuplicatedIds.isEmpty();
	}

	protected boolean hasRightDuplicatedIds() {
		return !rightDuplicatedIds.isEmpty();
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

	public Map<String, List<EObject>> getBaseDuplicatedIds() {
		return baseDuplicatedIds;
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
