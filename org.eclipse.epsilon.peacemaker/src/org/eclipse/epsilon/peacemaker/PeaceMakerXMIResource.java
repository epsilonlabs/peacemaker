package org.eclipse.epsilon.peacemaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
import org.eclipse.epsilon.peacemaker.conflicts.ObjectRedefinition;
import org.eclipse.epsilon.peacemaker.conflicts.ReferenceRedefinition;
import org.eclipse.epsilon.peacemaker.conflicts.UnconflictedObject;
import org.eclipse.epsilon.peacemaker.conflicts.UpdateDelete;

public class PeaceMakerXMIResource extends XMIResourceImpl {

	protected ConflictVersionResource leftResource;
	protected ConflictVersionResource baseResource;
	protected ConflictVersionResource rightResource;

	protected List<Conflict> conflicts = new ArrayList<>();
	protected List<ConflictSection> conflictSections = new ArrayList<>();

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
		leftResource = loadVersionResource("leftVersion", versionHelper);
		leftResource.setVersionName(versionName);
	}

	public void loadBase(ConflictVersionHelper versionHelper, String versionName) throws IOException {
		baseResource = loadVersionResource("baseVersion", versionHelper);
		baseResource.setVersionName(versionName);
	}

	public void loadRight(ConflictVersionHelper versionHelper, String versionName) throws IOException {
		rightResource = loadVersionResource("rightVersion", versionHelper);
		rightResource.setVersionName(versionName);
	}

	protected ConflictVersionResource loadVersionResource(String extension,
			ConflictVersionHelper versionHelper) throws IOException {

		URI versionURI = uri.appendFileExtension(extension);
		getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				versionURI.fileExtension(), new ConflictVersionResourceFactory());

		ConflictVersionResource resource =
				(ConflictVersionResource) getResourceSet().createResource(versionURI);
		resource.setVersionHelper(versionHelper);
		resource.load(versionHelper.getVersionContents(), Collections.EMPTY_MAP);

		return resource;
	}

	public void identifyConflicts(List<ConflictSection> conflictSections) {
		this.conflictSections.addAll(conflictSections);

		for (ConflictSection cs : conflictSections) {
			identifyConflicts(cs);
		}
	}

	protected void identifyConflicts(ConflictSection conflictSection) {
		for (String id : new ArrayList<>(conflictSection.getLeftIds())) {

			EObject leftObj = getLeftEObject(id);
			
			if (conflictSection.rightContains(id)) {
				// TODO: include here a more fine-grained analysis that can
				//       detect concrete changes (e.g. AttributeRedefinitions)
				addConflict(new ObjectRedefinition(id, this));
				conflictSection.removeLeft(id);
				conflictSection.removeRight(id);
			}
			else if (conflictSection.baseContains(id)) {
				// object updated in left version, and deleted in the right one
				addConflict(new UpdateDelete(id, this, true));
				conflictSection.removeLeft(id);
			}
			else {
				// check containing ref again
				EStructuralFeature feature = leftObj.eContainingFeature();

				if (feature != null && isContainmentNotManyReference(feature)) {
					EReference ref = (EReference) feature;
					
					String rightParentId = getLeftId(leftObj.eContainer());
					EObject rightParent = getRightEObject(rightParentId);
					if (rightParent == null) {
						throw new RuntimeException("complicated reference case, study deeper");
					}

					EObject rightObj = (EObject) rightParent.eGet(ref);

					if (rightObj != null) {
						ReferenceRedefinition redef = new ReferenceRedefinition(rightParentId, this, ref);
						addConflict(redef);
						conflictSection.removeLeft(id);
						conflictSection.removeRight(getRightId(rightObj));
					}
					else {
						throw new RuntimeException("changed reference in left, deleted in right?");
					}
				}
			}
		}

		// UpdateDelete conflicts can appear the other way (update in right version, delete in left)
		// loop over right ids to detect those cases
		for (String id : new ArrayList<>(conflictSection.getRightIds())) {
			if (conflictSection.baseContains(id)) {
				// object updated in right version, and deleted in the left one
				addConflict(new UpdateDelete(id, this, false));
				conflictSection.removeRight(id);
			}
		}
		
		// Any element remaining on the conflict section has not been identified
		//   as part of a conflict. Indicate them as "free" elements to keep or remove
		for (String id : new ArrayList<>(conflictSection.getLeftIds())) {
			addConflict(new UnconflictedObject(id, this, true));
		}
		for (String id : new ArrayList<>(conflictSection.getRightIds())) {
			addConflict(new UnconflictedObject(id, this, false));
		}
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
		return baseResource;
	}

	public ConflictVersionResource getRightResource() {
		return rightResource;
	}

	public String getLeftId(EObject obj) {
		return leftResource.getID(obj);
	}

	public String getRightId(EObject obj) {
		return rightResource.getID(obj);
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

	public List<ConflictSection> getConflictSections() {
		return conflictSections;
	}
}
