package org.eclipse.epsilon.peacemaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;

public class PeaceMakerXMIResource extends XMIResourceImpl {

	protected ConflictVersionResource leftResource;
	protected ConflictVersionResource rightResource;

	protected List<Conflict> conflicts = new ArrayList<>();
	protected List<ConflictSection> conflictSections;

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

	@Override
	protected XMLHelper createXMLHelper() {
		return new PeaceMakerXMIHelper(this);
	}

	public void loadLeft(ConflictVersionHelper versionHelper) throws IOException {
		leftResource = loadVersionResource("leftVersion", versionHelper);
	}

	public void loadRight(ConflictVersionHelper versionHelper) throws IOException {
		rightResource = loadVersionResource("rightVersion", versionHelper);
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

	public XMIResource getLeftResource() {
		return leftResource;
	}

	public XMIResource getRightResource() {
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

	public void addConflict(Conflict conflict) {
		conflicts.add(conflict);
	}

	public List<ConflictSection> getConflictSections() {
		return conflictSections;
	}

	public void setConflictSections(List<ConflictSection> conflictSections) {
		this.conflictSections = conflictSections;
	}
}
