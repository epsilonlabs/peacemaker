package org.eclipse.epsilon.peacemaker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLSave;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;

public class PeaceMakerXMIResource extends XMIResourceImpl {

	protected XMIResource leftResource;
	protected XMIResource rightResource;

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

	@Override
	protected XMLHelper createXMLHelper() {
		return new PeaceMakerXMIHelper(this);
	}

	public void loadLeft(InputStream inputStream) throws IOException {
		URI leftUri = uri.appendFileExtension("leftversion");
		getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				leftUri.fileExtension(), new XMIResourceFactoryImpl());

		leftResource = (XMIResource) getResourceSet().createResource(leftUri);
		leftResource.load(inputStream, Collections.EMPTY_MAP);
	}

	public void loadRight(InputStream inputStream) throws IOException {
		URI rightUri = uri.appendFileExtension("rightversion");
		getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				rightUri.fileExtension(), new XMIResourceFactoryImpl());

		rightResource = (XMIResource) getResourceSet().createResource(rightUri);
		rightResource.load(inputStream, Collections.EMPTY_MAP);
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

	public void addConflictSection(ConflictSection ct) {
		conflictSections.add(ct);
	}

	public List<ConflictSection> getConflictSections() {
		return conflictSections;
	}
}
