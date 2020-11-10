package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;
import org.eclipse.epsilon.peacemaker.util.PrettyPrint;

/**
 * Conflict caused by redefining a single (i.e. !isMany()) containment reference
 * @author alfonsodelavega
 */
public class ReferenceRedefinition extends Conflict {

	protected EReference reference;
	protected EObject leftValue;
	protected EObject rightValue;

	/**
	 * Create a reference redefinition conflict
	 *
	 * @param parentId  The parent that contains the reference feature
	 * @param reference
	 */
	public ReferenceRedefinition(String parentId, EReference reference) {
		super(parentId);
		this.reference = reference;
	}

	public EReference getReference() {
		return reference;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		
		s.append("A single, containment reference redefinition was found\n");
		s.append("Parent: ").append(eObjectId).append("\n");
		s.append("Reference name: ").append(reference.getName()).append("\n");
		s.append("Left(id): ").append(PrettyPrint.featuresMap(leftValue)).append("\n");
		s.append("Right(id): ").append(PrettyPrint.featuresMap(rightValue));

		return s.toString();
	}

	public EObject getLeftValue() {
		return leftValue;
	}

	public void setLeftValue(EObject leftValue) {
		this.leftValue = leftValue;
	}

	public EObject getRightValue() {
		return rightValue;
	}

	public void setRightValue(EObject rightValue) {
		this.rightValue = rightValue;
	}

	@Override
	public void resolve(ResolveAction action) {
		switch (action) {
		case KEEP_LEFT:
			EObject leftCopy = EcoreUtil.copy(leftValue);
			EObject rightParent = rightValue.eContainer();
			rightParent.eSet(reference, leftCopy);
			CopyUtils.copyIds(leftValue, leftCopy);
			break;
		case KEEP_RIGHT:
			EObject rightCopy = EcoreUtil.copy(rightValue);
			EObject leftParent = leftValue.eContainer();
			leftParent.eSet(reference, rightCopy);
			CopyUtils.copyIds(rightValue, rightCopy);
			break;
		default:
			super.resolve(action);
		}
	}
}
