package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.Collections;
import java.util.List;

public abstract class Conflict {

	public enum ResolveAction {

		KEEP, REMOVE, KEEP_LEFT, KEEP_RIGHT;

		@Override
		public String toString() {
			switch (this) {
			case KEEP:
				return "Keep";
			case REMOVE:
				return "Remove";
			case KEEP_LEFT:
				return "Keep left";
			case KEEP_RIGHT:
				return "Keep right";
			default:
				throw new IllegalArgumentException();
			}
		}
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

	public List<ResolveAction> getSupportedActions() {
		return Collections.EMPTY_LIST;
	}

	public void resolve(ResolveAction action) {
		throw new UnsupportedOperationException(
				"Unsupported resolve action for this conflict: " + action);
	}
}
