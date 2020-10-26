package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

/**
 * Conflict caused by redefining a single (i.e. !isMany()) containment reference
 * @author alfonsodelavega
 */
public class ReferenceRedefinition extends Conflict {

	protected EReference reference;
	protected Object leftValue;

	/**
	 * Create a reference redefinition conflict
	 * 
	 * @param parent    The parent that contains the reference feature
	 * @param parentId
	 * @param reference
	 * @param leftValue The value that is being redefined
	 */
	public ReferenceRedefinition(EObject parent, String parentId, EReference reference, Object leftValue) {
		super(parent, parentId);
		this.reference = reference;
		this.leftValue = leftValue;
	}

	public EReference getReference() {
		return reference;
	}

	public Object getLeftValue() {
		return leftValue;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		
		s.append("A single, containment reference redefinition was found\n");
		s.append("Parent: ").append(eObject.eClass().getName()).append(" ").append(eObjectId).append("\n");
		s.append("Left: ").append(leftValue).append("\n");
		s.append("Right: ").append(eObject.eGet(reference));

		return s.toString();
	}
}
