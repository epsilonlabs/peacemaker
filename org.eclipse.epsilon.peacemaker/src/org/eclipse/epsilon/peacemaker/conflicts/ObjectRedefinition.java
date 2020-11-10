package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;
import org.eclipse.epsilon.peacemaker.util.PrettyPrint;

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
		s.append("Left: ").append(PrettyPrint.featuresMap(leftObject)).append("\n");
		s.append("Right: ").append(PrettyPrint.featuresMap(rightObject));

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

	@Override
	public void resolve(ResolveAction action) {
		EObject copy = null;
		EObject fromObject = null;
		EObject toObject = null;

		switch (action) {
		case KEEP_LEFT:
			fromObject = leftObject;
			toObject = rightObject;
			break;
		case KEEP_RIGHT:
			fromObject = rightObject;
			toObject = leftObject;
			break;
		default:
			super.resolve(action);
		}

		copy = EcoreUtil.copy(fromObject);

		EReference reference = (EReference) fromObject.eContainingFeature();
		if (reference != null) {
			EObject toObjectParent = toObject.eContainer();
			if (!reference.isMany()) {
				toObjectParent.eSet(reference, copy);
			}
			else {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) toObjectParent.eGet(reference);

				int index = list.indexOf(toObject);
				list.remove(index);
				list.add(index, copy);
			}
		}
		else {
			// root element
			List<EObject> contents = toObject.eResource().getContents();
			int index = contents.indexOf(toObject);
			contents.remove(index);
			contents.add(index, copy);
		}
		CopyUtils.copyIds(fromObject, copy);
	}
}
