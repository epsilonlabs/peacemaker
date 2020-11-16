package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
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
	public ReferenceRedefinition(String parentId, PeaceMakerXMIResource pmResource,
			EReference reference) {
		super(parentId, pmResource);
		this.reference = reference;

		leftValue = (EObject) pmResource.getLeftEObject(eObjectId)
				.eGet(reference);
		rightValue = (EObject) pmResource.getRightEObject(eObjectId)
				.eGet(reference);
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
}
