package org.eclipse.epsilon.peacemaker.conflicts;

public abstract class Conflict {

	public enum ResolveAction {
		KEEP, REMOVE, KEEP_LEFT, KEEP_RIGHT
	}

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

	public void resolve(ResolveAction action) {
		throw new UnsupportedOperationException(
				"Unsupported resolve action for this conflict: " + action);
	}
}
