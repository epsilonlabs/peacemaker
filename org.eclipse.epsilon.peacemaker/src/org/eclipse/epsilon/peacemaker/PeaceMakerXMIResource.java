package org.eclipse.epsilon.peacemaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.eclipse.epsilon.peacemaker.conflicts.DoubleUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.ReferenceDoubleUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.UnconflictedObject;
import org.eclipse.epsilon.peacemaker.conflicts.UpdateDelete;
import org.eclipse.epsilon.peacemaker.util.IdUtils;

public class PeaceMakerXMIResource extends XMIResourceImpl {

	private static final String LEFT_VERSION_EXTENSION = "pmLeftVersion";
	private static final String BASE_VERSION_EXTENSION = "pmBaseVersion";
	private static final String RIGHT_VERSION_EXTENSION = "pmRightVersion";

	// placeholder for those resources where no conflicts are found
	protected Resource unconflictedResource;

	protected Map<?, ?> versionLoadOptions = Collections.EMPTY_MAP;

	protected XMIResource leftResource;
	protected XMIResource rightResource;

	protected XMIResource baseResource;
	protected ConflictVersionHelper baseVersionHelper;

	protected String baseVersionName = "";
	protected String leftVersionName = "";
	protected String rightVersionName = "";

	protected List<Conflict> conflicts = new ArrayList<>();

	public PeaceMakerXMIResource(URI uri) {
		super(uri);
	}

	@Override
	protected XMLLoad createXMLLoad() {
		return new PeaceMakerXMILoad(createXMLHelper());
	}

	@Override
	protected XMLSave createXMLSave() {
		return new PeaceMakerXMISave(createXMLHelper());
	}

	public void loadLeft(ConflictVersionHelper versionHelper) throws IOException {
		leftResource = loadVersionResource(LEFT_VERSION_EXTENSION, versionHelper);
	}

	/**
	 * Checks if there is ancestor version information, also loads it if not loaded yet
	 */
	protected boolean hasBaseVersion() {
		if (baseVersionHelper != null) {
			if (baseResource == null) {
				// demand-load of the resource
				doLoadBase();
			}
			return true;
		}
		return false;
	}

	public void loadBase(ConflictVersionHelper versionHelper) {
		// this version is loaded on demand (i.e. if needed to identify conflicts)
		baseVersionHelper = versionHelper;
	}

	protected void doLoadBase() {
		try {
			baseResource = loadVersionResource(BASE_VERSION_EXTENSION, baseVersionHelper);
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void loadRight(ConflictVersionHelper versionHelper) throws IOException {
		rightResource = loadVersionResource(RIGHT_VERSION_EXTENSION, versionHelper);
	}

	protected XMIResource loadVersionResource(String extension,
			ConflictVersionHelper versionHelper) throws IOException {

		URI versionURI = uri.appendFileExtension(extension);

		// load it as a standard XMI Resource (using specific factories if registered)
		Resource.Factory.Registry factoryRegistry = getResourceSet().getResourceFactoryRegistry();
		if (!(factoryRegistry.getExtensionToFactoryMap().get("*") instanceof PeaceMakerXMIResourceFactory)) {
			throw new RuntimeException("A peacemaker factory should be locally registered");
		}
		Object peacemakerFactory = factoryRegistry.getExtensionToFactoryMap().remove("*");
		Resource.Factory specificFactory = factoryRegistry.getFactory(getURI());
		factoryRegistry.getExtensionToFactoryMap().put("*", peacemakerFactory);

		XMIResourceImpl specificResource = (XMIResourceImpl) specificFactory.createResource(versionURI);
		specificResource.basicSetResourceSet(getResourceSet(), null);

		// the pool allows decorating the xml handler to get element lines
		Map<Object, Object> loadOptions = new HashMap<Object, Object>(versionLoadOptions);
		loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new PeacemakerXMLParserPoolImpl(this, versionHelper));
		specificResource.load(versionHelper.getVersionContents(), loadOptions);

		return specificResource;
	}

	public void identifyConflicts(List<ConflictSection> conflictSections) {
		for (ConflictSection cs : conflictSections) {
			identifyConflicts(cs);
		}
	}

	protected void identifyConflicts(ConflictSection conflictSection) {
		for (String id : conflictSection.getLeftIds()) {

			EObject leftObj = getLeftEObject(id);
			
			if (conflictSection.rightContains(id)) {
				addConflict(new DoubleUpdate(id, this));
				conflictSection.removeRight(id);
			}
			else if (checkContainmentReference(id, leftObj, conflictSection)) {
				// special double update case: single containment reference
				// that contains objects with distinct ids in left and right
			}
			else if (hasBaseVersion() && conflictSection.baseContains(id)) {
				// object updated in left version, and deleted in the right one
				addConflict(new UpdateDelete(id, this, true));
			}
			else {
				// not identified as part of a conflict: "free" element to keep or remove
				addConflict(new UnconflictedObject(id, this, true));
			}
		}

		// UpdateDelete conflicts can appear the other way (update in right version, delete in left)
		// loop over right ids to detect those cases
		for (String id : conflictSection.getRightIds()) {
			if (hasBaseVersion() && conflictSection.baseContains(id)) {
				// object updated in right version, and deleted in the left one
				addConflict(new UpdateDelete(id, this, false));
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
		if (hasBaseVersion()) {
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

	public EObject getRightEObject(String id) {
		return rightResource.getEObject(id);
	}

	public List<Conflict> getConflicts() {
		return conflicts;
	}

	public boolean hasConflicts() {
		return leftResource != null && rightResource != null && !conflicts.isEmpty();
	}

	public void setUnconflictedResource(Resource resource) {
		this.unconflictedResource = resource;
	}

	public Resource getUnconflictedResource() {
		return unconflictedResource;
	}

	public void setVersionLoadOptions(Map<?, ?> options) {
		if (options != null) {
			this.versionLoadOptions = options;
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

	public void setBaseVersionName(String baseVersionName) {
		this.baseVersionName = baseVersionName;
	}

	public void setLeftVersionName(String leftVersionName) {
		this.leftVersionName = leftVersionName;
	}

	public void setRightVersionName(String rightVersionName) {
		this.rightVersionName = rightVersionName;
	}
}
