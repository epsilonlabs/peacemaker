package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;

/**
 * A conflict indicating that an object with the same id has been modified in
 * each segment of a conflict section
 *
 * @author alfonsodelavega
 *
 */
public class ObjectRedefinition extends Conflict {

	protected EObject leftObject;
	protected EObject rightObject;

	/**
	 * Create a object redefinition conflict
	 *
	 * @param objectId
	 */
	public ObjectRedefinition(String objectId) {
		super(objectId);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("An object with the same id has been redefined in left and right\n");
		s.append("Id: ").append(eObjectId).append("\n");
		s.append("Left: ").append(leftObject).append("\n");
		s.append("Right: ").append(rightObject);

		return s.toString();
	}

	public EObject getLeftObject() {
		return leftObject;
	}

	public void setLeftObject(EObject leftObject) {
		this.leftObject = leftObject;
	}

	public EObject getRightObject() {
		return rightObject;
	}

	public void setRightObject(EObject rightObject) {
		this.rightObject = rightObject;
	}
}
