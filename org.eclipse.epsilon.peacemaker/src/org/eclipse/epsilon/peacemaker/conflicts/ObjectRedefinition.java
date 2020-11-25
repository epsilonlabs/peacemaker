package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
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
	 */
	public ObjectRedefinition(String objectId, PeaceMakerXMIResource pmResource) {
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
			return;
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
				List<EObject> list = (List<EObject>) toObjectParent.eGet(reference);

				int index = list.indexOf(toObject);
				list.remove(index);
				CopyUtils.safeIndexAdd(list, index, copy);
			}
		}
		else {
			// root element
			List<EObject> contents = toObject.eResource().getContents();
			int index = contents.indexOf(toObject);
			contents.remove(index);
			CopyUtils.safeIndexAdd(contents, index, copy);
		}
		CopyUtils.finishCopy(fromObject, copy);
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
		return "Object Redefinition";
	}

	@Override
	public String getDescription() {
		return String.format(
				"A %s object with the same id (%s) has been redefined in the left and right versions",
				leftObject.eClass().getName(), eObjectId);
	}
}
