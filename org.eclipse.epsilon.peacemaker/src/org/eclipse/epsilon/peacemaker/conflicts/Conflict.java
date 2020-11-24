package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;

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

	/**
	 * The status of an object that is involved in a conflict. Depends on the
	 * applied resolve action
	 */
	public enum ConflictObjectStatus {
		ACCEPTED, DISCARDED, UNRESOLVED
	}

	protected PeaceMakerXMIResource pmResource;
	protected String eObjectId;

	public Conflict(String eObjectId, PeaceMakerXMIResource pmResource) {
		this.eObjectId = eObjectId;
		this.pmResource = pmResource;
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

	public ConflictObjectStatus getLeftStatus(ResolveAction action) {
		return getStatus(action);
	}

	public ConflictObjectStatus getRightStatus(ResolveAction action) {
		return getStatus(action);
	}

	protected ConflictObjectStatus getStatus(ResolveAction action) {
		switch (action) {
		case NO_ACTION:
			return ConflictObjectStatus.UNRESOLVED;
		default:
			throw new UnsupportedOperationException(
					"Unsupported resolve action for this conflict: " + action);
		}
	}

	/**
	 * Returns the object from the left version that intervenes in the conflict
	 */
	public EObject getLeftVersionObject() {
		return pmResource.getLeftEObject(eObjectId);
	}

	/**
	 * Returns the object from the right version that intervenes in the conflict
	 */
	public EObject getRightVersionObject() {
		return pmResource.getRightEObject(eObjectId);
	}

	public String getLeftVersionId() {
		return eObjectId;
	}

	public String getRightVersionId() {
		return eObjectId;
	}

	public String getTitle() {
		return "Conflict";
	}

	public String getDescription() {
		return "A conflict has been detected around an object with id " + eObjectId;
	}

	public String getEObjectId() {
		return eObjectId;
	}
}