package org.eclipse.epsilon.peacemaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
import org.eclipse.epsilon.peacemaker.conflicts.DoubleUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.ReferenceDoubleUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.UnconflictedObject;
import org.eclipse.epsilon.peacemaker.conflicts.UpdateDelete;

public class PeaceMakerXMIResource extends XMIResourceImpl {

	private static final String LEFT_VERSION_EXTENSION = "pmLeftVersion";
	private static final String BASE_VERSION_EXTENSION = "pmBaseVersion";
	private static final String RIGHT_VERSION_EXTENSION = "pmRightVersion";

	// placeholder for those resources where no conflicts are found
	protected Resource unconflictedResource;

	protected ConflictVersionResource leftResource;
	protected ConflictVersionResource rightResource;

	protected ConflictVersionResource baseResource;
	protected ConflictVersionHelper baseVersionHelper;
	protected String baseVersionName;

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

	public void loadLeft(ConflictVersionHelper versionHelper, String versionName) throws IOException {
		leftResource = loadVersionResource(LEFT_VERSION_EXTENSION, versionHelper, versionName);
	}

	/**
	 * Checks if there is ancestor version information, also loads it if not loaded yet
	 */
	protected boolean hasBaseVersion() {
		if (baseVersionHelper != null) {
			if (baseResource == null) {
				// demand-load of the resource
				try {
					baseResource = loadVersionResource(BASE_VERSION_EXTENSION, baseVersionHelper, baseVersionName);
				}
				catch (IOException ex) {
					ex.printStackTrace();
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public void loadBase(ConflictVersionHelper versionHelper, String versionName) {
		// this version is loaded on demand (i.e. if needed to identify conflicts)
		baseVersionHelper = versionHelper;
		baseVersionName = versionName;
	}

	public void loadRight(ConflictVersionHelper versionHelper, String versionName) throws IOException {
		rightResource = loadVersionResource(RIGHT_VERSION_EXTENSION, versionHelper, versionName);
	}

	protected ConflictVersionResource loadVersionResource(String extension,
			ConflictVersionHelper versionHelper, String versionName) throws IOException {

		URI versionURI = uri.appendFileExtension(extension);
		getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				versionURI.fileExtension(), new ConflictVersionResourceFactory());

		ConflictVersionResource resource =
				(ConflictVersionResource) getResourceSet().createResource(versionURI);
		resource.setVersionHelper(versionHelper);
		resource.load(versionHelper.getVersionContents(), Collections.EMPTY_MAP);
		resource.setVersionName(versionName);

		return resource;
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

	public ConflictVersionResource getLeftResource() {
		return leftResource;
	}

	public ConflictVersionResource getBaseResource() {
		if (hasBaseVersion()) {
			return baseResource;
		}
		return null;
	}

	public ConflictVersionResource getRightResource() {
		return rightResource;
	}

	public String getLeftId(EObject obj) {
		return leftResource.getAvailableId(obj);
	}

	public String getRightId(EObject obj) {
		return rightResource.getAvailableId(obj);
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
}
