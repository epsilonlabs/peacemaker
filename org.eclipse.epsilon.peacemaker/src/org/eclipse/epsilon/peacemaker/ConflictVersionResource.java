package org.eclipse.epsilon.peacemaker;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;

public class ConflictVersionResource extends XMIResourceImpl {

	protected ConflictVersionHelper versionHelper;
	private String versionName;

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

	@Override
	public void setID(EObject eObject, String id) {
		Object oldID = id != null ? getEObjectToIDMap().put(eObject, id) : getEObjectToIDMap().remove(eObject);

		// the extra comparisons prevent the removal of a map from a conflicting object
		if (oldID != null &&
				getIDToEObjectMap().containsKey(oldID) &&
				getIDToEObjectMap().get(oldID) == eObject) {

			getIDToEObjectMap().remove(oldID);
		}

		if (id != null) {
			getIDToEObjectMap().put(id, eObject);
		}
	}

	public void setVersionHelper(ConflictVersionHelper versionHelper) {
		this.versionHelper = versionHelper;
	}

	public ConflictVersionHelper getVersionHelper() {
		return versionHelper;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
}
