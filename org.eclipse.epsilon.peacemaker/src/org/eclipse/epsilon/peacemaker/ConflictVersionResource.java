package org.eclipse.epsilon.peacemaker;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
	protected boolean assignIDsWhileLoading() {
		return false;
	}

	public boolean hasXMIID(EObject obj) {
		return super.getID(obj) != null;
	}

	/**
	 * Returns the XMI id of an EObject, or the value of an EAttribute marked as
	 * id if the first is not present
	 * 
	 * @throws RuntimeException When no ids could be found
	 */
	public String getAvailableId(EObject eObject) {
		String id = super.getID(eObject);
		if (id == null) {
			id = EcoreUtil.getID(eObject);
		}
		if (id == null) {
			throw new RuntimeException("Peacemaker requires all objects to have an id");
		}
		return id;
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

	/**
	 * Get the original id of previously detached objects
	 * 
	 * @returns The id, or null if the object was never attached to the resource
	 */
	public Object getDetachedId(EObject externalObj) {
		return DETACHED_EOBJECT_TO_ID_MAP.get(externalObj);
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
