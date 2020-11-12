package org.eclipse.epsilon.peacemaker;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;

public class ConflictVersionResource extends XMIResourceImpl {

	protected ConflictVersionHelper versionHelper;

	public ConflictVersionResource() {
		super();
	}

	public ConflictVersionResource(URI uri) {
		super(uri);
	}

	@Override
	protected XMLLoad createXMLLoad() {
		return new ConflictVersionLoad(createXMLHelper());
	}

	@Override
	protected boolean useUUIDs() {
		return true;
	}

	public void setVersionHelper(ConflictVersionHelper versionHelper) {
		this.versionHelper = versionHelper;
	}

	public ConflictVersionHelper getVersionHelper() {
		return versionHelper;
	}
}
