package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;
import org.eclipse.epsilon.peacemaker.util.PrettyPrint;

public class UpdateDelete extends Conflict {

	protected boolean updateInLeft;

	/** where the object has been updated */
	protected XMIResource resourceWithUpdate;
	/** where the object has been deleted */
	protected XMIResource resourceWithDelete;

	public UpdateDelete(String eObjectId, PeaceMakerXMIResource pmResource,
			boolean updateInLeft) {
		super(eObjectId, pmResource);
		this.updateInLeft = updateInLeft;

		if (updateInLeft) {
			resourceWithUpdate = pmResource.getLeftResource();
			resourceWithDelete = pmResource.getRightResource();
		}
		else {
			resourceWithUpdate = pmResource.getRightResource();
			resourceWithDelete = pmResource.getLeftResource();
		}
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append(getTitle()).append("\n").append(getDescription()).append("\n");
		s.append("Updated version: ")
				.append(PrettyPrint.featuresMap(resourceWithUpdate.getEObject(eObjectId))).append("\n");
		s.append("Base version: ")
				.append(PrettyPrint.featuresMap(pmResource.getBaseResource().getEObject(eObjectId))).append("\n");

		return s.toString();
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
			EObject obj = resourceWithUpdate.getEObject(eObjectId);
			EObject copy = EcoreUtil.copy(obj);
			EReference ref = (EReference) obj.eContainingFeature();
			if (ref == null) {
				// root object: add new one to resource contents
				CopyUtils.safeIndexAdd(resourceWithDelete.getContents(),
						resourceWithUpdate.getContents().indexOf(obj), copy);
			}
			else {
				String parentId = resourceWithUpdate.getID(obj.eContainer());
				EObject otherParent = resourceWithDelete.getEObject(parentId);
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
			EcoreUtil.remove(resourceWithUpdate.getEObject(eObjectId));
			break;
		}
		default:
			super.resolve(action);
		}
	}

	@Override
	public String getTitle() {
		return "Update-Delete";
	}

	@Override
	public String getDescription() {
		return String.format(
				"%s object with id %s updated in the %s version while deleted in the other one",
				resourceWithUpdate.getEObject(eObjectId).eClass().getName(),
				eObjectId,
				updateInLeft ? "left" : "right");
	}
}
