package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;

/**
 * A conflict indicating that an object with the same id has been started in
 * each segments of a conflict section
 * 
 * @author alfonsodelavega
 *
 */
public class ObjectRedefinition extends Conflict {

	protected EObject rightObject;

	/**
	 * Create a object redefinition conflict
	 * 
	 * @param leftObject  The definition in the left conflict segment
	 * @param leftObjectId
	 * @param rightObject
	 */
	public ObjectRedefinition(EObject leftObject, String leftObjectId, EObject rightObject) {
		super(leftObject, leftObjectId);
		this.rightObject = rightObject;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("An object with the same id has been defined in left and right\n");
		s.append("Type: ").append(eObject.eClass().getName())
				.append(", Id: ").append(eObjectId).append("\n");
		s.append("Left: ").append(eObject).append("\n");
		s.append("Right: ").append(rightObject);

		return s.toString();
	}
}
