package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;
import org.eclipse.epsilon.peacemaker.util.PrettyPrint;

/**
 * A special case of a double update conflict where the changes happen in a
 * single (i.e. !isMany()) containment reference
 * 
 * @author alfonsodelavega
 */
public class ReferenceDoubleUpdate extends Conflict {

	protected EReference reference;

	protected EObject leftValue;
	protected EObject rightValue;

	protected String leftValueId;
	protected String rightValueId;

	/**
	 * Create a reference double update conflict
	 *
	 * @param parentId  The parent that contains the reference feature
	 * @param reference
	 */
	public ReferenceDoubleUpdate(String parentId, PeaceMakerXMIResource pmResource,
			EReference reference) {
		
		super(parentId, pmResource);
		this.reference = reference;

		leftValue = (EObject) pmResource.getLeftEObject(eObjectId)
				.eGet(reference);
		rightValue = (EObject) pmResource.getRightEObject(eObjectId)
				.eGet(reference);

		leftValueId = pmResource.getLeftId(leftValue);
		rightValueId = pmResource.getRightId(rightValue);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		
		s.append(getTitle()).append("\n").append(getDescription()).append("\n");
		s.append("Left: ").append(PrettyPrint.featuresMap(leftValue)).append("\n");
		s.append("Right: ").append(PrettyPrint.featuresMap(rightValue));

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
			CopyUtils.replace(leftValue, rightValue);
			break;
		case KEEP_RIGHT:
			CopyUtils.replace(rightValue, leftValue);
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
	public EObject getLeftVersionObject() {
		// objectId is the parent in this case, get the reference value from it
		return (EObject) pmResource.getLeftEObject(eObjectId).eGet(reference);
	}

	@Override
	public EObject getRightVersionObject() {
		// objectId is the parent in this case, get the reference value from it
		return (EObject) pmResource.getRightEObject(eObjectId).eGet(reference);
	}

	public String getLeftVersionId() {
		return leftValueId;
	}

	public String getRightVersionId() {
		return rightValueId;
	}

	@Override
	public String getTitle() {
		return "Reference Update";
	}

	@Override
	public String getDescription() {
		String referenceEClass = ((EClass)reference.eContainer()).getName();
		return String.format(
				"The single-bounded and containment reference \"%s\" from type %s" +
				" was updated on the left and right versions of a %s object with id %s",
				reference.getName(), referenceEClass, referenceEClass, eObjectId);
	}
}
