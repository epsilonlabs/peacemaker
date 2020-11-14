package org.eclipse.epsilon.peacemaker.conflicts;

public abstract class Conflict {

	public enum ResolveAction {

		KEEP, REMOVE, KEEP_LEFT, KEEP_RIGHT, NO_ACTION;

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
			case NO_ACTION:
				return "No Action";
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

	public boolean supports(ResolveAction action) {
		return action == ResolveAction.NO_ACTION;
	}

	public void resolve(ResolveAction action) {
		switch (action) {
		case NO_ACTION:
			break;
		default:
			throw new UnsupportedOperationException(
					"Unsupported resolve action for this conflict: " + action);
		}
	}
}
