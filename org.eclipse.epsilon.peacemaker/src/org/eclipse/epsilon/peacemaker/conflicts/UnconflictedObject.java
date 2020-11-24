package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;

public class UnconflictedObject extends Conflict {

	protected boolean inLeftSegment;

	/** The resource containing the unconflicted object */
	protected XMIResource objectResource;
	/** The other resource (not containing the object) */
	protected XMIResource otherResource;

	public UnconflictedObject(String eObjectId, PeaceMakerXMIResource pmResource,
			boolean inLeftSegment) {
		super(eObjectId, pmResource);
		this.inLeftSegment = inLeftSegment;

		if (inLeftSegment) {
			objectResource = pmResource.getLeftResource();
			otherResource = pmResource.getRightResource();
		}
		else {
			objectResource = pmResource.getRightResource();
			otherResource = pmResource.getLeftResource();
		}
	}

	public String toString() {
		return getDescription();
	}

	@Override
	public boolean supports(ResolveAction action) {
		switch (action) {
		case KEEP:
		case REMOVE:
			return true;
		default:
			return super.supports(action);
		}
	}

	@Override
	public void resolve(ResolveAction action) {

		switch (action) {
		case KEEP: {
			// copy it to the other resource
			EObject obj = objectResource.getEObject(eObjectId);
			EObject copy = EcoreUtil.copy(obj);
			EReference ref = (EReference) obj.eContainingFeature();
			if (ref == null) {
				// root object: add new one to resource contents
				CopyUtils.safeIndexAdd(otherResource.getContents(),
						objectResource.getContents().indexOf(obj), copy);
			}
			else {
				String parentId = objectResource.getID(obj.eContainer());
				EObject otherParent = otherResource.getEObject(parentId);
				if (otherParent == null) {
					throw new IllegalStateException(
							"Trying to keep an object while the parent in the other resource does not exist");
				}

				if (ref.isMany()) {
					@SuppressWarnings("unchecked")
					List<EObject> parentRefValues = (List<EObject>) obj.eContainer().eGet(ref);
					@SuppressWarnings("unchecked")
					List<EObject> otherParentRefValues = (List<EObject>) otherParent.eGet(ref);

					CopyUtils.safeIndexAdd(otherParentRefValues, parentRefValues.indexOf(obj), copy);
				}
				else {
					otherParent.eSet(ref, copy);
				}
			}
			CopyUtils.copyIds(obj, copy);
			break;
		}
		case REMOVE: {
			EcoreUtil.remove(objectResource.getEObject(eObjectId));
			break;
		}
		default:
			super.resolve(action);
		}
	}

	@Override
	protected ConflictObjectStatus getStatus(ResolveAction action) {
		switch (action) {
		case KEEP:
			return ConflictObjectStatus.ACCEPTED;
		case REMOVE:
			return ConflictObjectStatus.DISCARDED;
		default:
			return super.getStatus(action);
		}
	}

	@Override
	public String getTitle() {
		return "Unconflicted object";
	}

	@Override
	public String getDescription() {

		return String.format(
				"Unconflicted %s object with id %s found in the %s segment of a conflict section",
				objectResource.getEObject(eObjectId).eClass().getName(),
				eObjectId,
				inLeftSegment ? "left" : "right");
	}
}
