package org.eclipse.epsilon.peacemaker.conflicts;

public abstract class Conflict {

	protected String eObjectId;

	public String getEObjectId() {
		return eObjectId;
	}

	public void setEObjectId(String eObjectId) {
		this.eObjectId = eObjectId;
	}

	public Conflict(String eObjectId) {
		this.eObjectId = eObjectId;
	}

}
