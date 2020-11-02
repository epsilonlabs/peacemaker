package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

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
		s.append("Left: ").append(leftValue).append("\n");
		s.append("Right: ").append(rightValue);

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
}
