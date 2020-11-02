package org.eclipse.epsilon.peacemaker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.peacemaker.conflicts.AttributeRedefinitions;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
import org.eclipse.epsilon.peacemaker.conflicts.ObjectRedefinition;
import org.eclipse.epsilon.peacemaker.conflicts.ReferenceRedefinition;

public class PeaceMakerXMIResource extends XMIResourceImpl {

	protected Resource leftResource;
	protected Resource rightResource;

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
	protected XMLHelper createXMLHelper() {
		return new PeaceMakerXMIHelper(this);
	}

	public void loadLeft(InputStream inputStream) throws IOException {
		URI leftUri = uri.appendFileExtension("leftversion");
		getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				leftUri.fileExtension(), new XMIResourceFactoryImpl());

		leftResource = getResourceSet().createResource(leftUri);
		leftResource.load(inputStream, Collections.EMPTY_MAP);
	}

	public void loadRight(InputStream inputStream) throws IOException {
		URI rightUri = uri.appendFileExtension("rightversion");
		getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				rightUri.fileExtension(), new XMIResourceFactoryImpl());

		rightResource = getResourceSet().createResource(rightUri);
		rightResource.load(inputStream, Collections.EMPTY_MAP);
	}

	public Resource getLeftResource() {
		return leftResource;
	}

	public Resource getRightResource() {
		return rightResource;
	}

	public List<Conflict> getConflicts() {
		return conflicts;
	}

	public void addConflict(ObjectRedefinition conflict) {
		conflicts.add(conflict);
		conflict.setLeftObject(leftResource.getEObject(conflict.getEObjectId()));
		conflict.setRightObject(rightResource.getEObject(conflict.getEObjectId()));
	}

	public void addConflict(ReferenceRedefinition conflict) {
		conflicts.add(conflict);
		conflict.setLeftValue((EObject) leftResource.getEObject(conflict.getEObjectId())
				.eGet(conflict.getReference()));
		conflict.setRightValue((EObject) rightResource.getEObject(conflict.getEObjectId())
				.eGet(conflict.getReference()));
	}

	public void addConflict(AttributeRedefinitions attrRedef) {
		conflicts.add(attrRedef);
	}

	public void addConflictSection(ConflictSection ct) {
		conflictSections.add(ct);
	}

	public List<ConflictSection> getConflictSections() {
		return conflictSections;
	}
}
