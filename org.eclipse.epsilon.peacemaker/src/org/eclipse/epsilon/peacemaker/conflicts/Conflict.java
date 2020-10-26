package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;

public abstract class Conflict {

	protected EObject eObject;
	protected String eObjectId;

	public EObject geteObject() {
		return eObject;
	}

	public void seteObject(EObject eObject) {
		this.eObject = eObject;
	}

	public String geteObjectId() {
		return eObjectId;
	}

	public void seteObjectId(String eObjectId) {
		this.eObjectId = eObjectId;
	}

	public Conflict(EObject eObject, String eObjectId) {
		this.eObject = eObject;
		this.eObjectId = eObjectId;
	}

}
