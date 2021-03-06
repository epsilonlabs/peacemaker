package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;
import org.eclipse.epsilon.peacemaker.util.PrettyPrint;

/**
 * A conflict indicating that an object with the same id has been modified in
 * the left and right segments of a conflict section
 *
 * @author alfonsodelavega
 *
 */
public class DoubleUpdate extends Conflict {

	protected EObject leftObject;
	protected EObject rightObject;

	/**
	 * Create a double update conflict
	 */
	public DoubleUpdate(String objectId, PeacemakerResource pmResource) {
		super(objectId, pmResource);
		leftObject = pmResource.getLeftEObject(eObjectId);
		rightObject = pmResource.getRightEObject(eObjectId);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append(getTitle()).append("\n").append(getDescription()).append("\n");
		s.append("Left: ").append(PrettyPrint.featuresMap(leftObject)).append("\n");
		s.append("Right: ").append(PrettyPrint.featuresMap(rightObject));

		return s.toString();
	}

	public boolean supports(ResolveAction action) {
		switch (action) {
		case KEEP_LEFT:
		case KEEP_RIGHT:
			return true;
		default:
			return super.supports(action);
		}
	}

	@Override
	public void resolve(ResolveAction action) {

		switch (action) {
		case KEEP_LEFT:
			CopyUtils.replace(leftObject, rightObject);
			break;
		case KEEP_RIGHT:
			CopyUtils.replace(rightObject, leftObject);
			break;
		default:
			super.resolve(action);
		}
	}

	@Override
	public ConflictObjectStatus getLeftStatus(ResolveAction action) {
		switch (action) {
		case KEEP_LEFT:
			return ConflictObjectStatus.ACCEPTED;
		case KEEP_RIGHT:
			return ConflictObjectStatus.DISCARDED;
		default:
			return super.getLeftStatus(action);
		}
	}

	@Override
	public ConflictObjectStatus getRightStatus(ResolveAction action) {
		switch (action) {
		case KEEP_LEFT:
			return ConflictObjectStatus.DISCARDED;
		case KEEP_RIGHT:
			return ConflictObjectStatus.ACCEPTED;
		default:
			return super.getRightStatus(action);
		}
	}

	@Override
	public String getTitle() {
		return "Double Update";
	}

	@Override
	public String getDescription() {
		return String.format(
				"A %s object with the same id (%s) has been updated in the left and right versions",
				leftObject.eClass().getName(), eObjectId);
	}
}
