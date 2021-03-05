package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.XMIResetIdsHandler;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;
import org.eclipse.epsilon.peacemaker.util.PrettyPrint;
import org.eclipse.epsilon.peacemaker.util.ids.IdUtils;

public class UpdateDelete extends Conflict implements XMIResetIdsHandler {

	protected boolean deleteInRight;
	protected boolean usesXMIIds = false;

	/** where the object has been updated */
	protected XMIResource resourceWithUpdate;
	/** where the object has been deleted */
	protected XMIResource resourceWithDelete;

	protected EObject eObject;

	public UpdateDelete(String eObjectId, PeaceMakerXMIResource pmResource,
			boolean deleteInRight) {
		super(eObjectId, pmResource);
		this.deleteInRight = deleteInRight;

		if (deleteInRight) {
			resourceWithUpdate = pmResource.getLeftResource();
			resourceWithDelete = pmResource.getRightResource();
		}
		else {
			resourceWithUpdate = pmResource.getRightResource();
			resourceWithDelete = pmResource.getLeftResource();
		}
		eObject = resourceWithUpdate.getEObject(eObjectId);
		usesXMIIds = IdUtils.hasXMIId(resourceWithUpdate, eObject);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append(getTitle()).append("\n").append(getDescription()).append("\n");
		s.append("Updated version: ")
				.append(PrettyPrint.featuresMap(eObject)).append("\n");
		s.append("Base version: ")
				.append(PrettyPrint.featuresMap(pmResource.getBaseEObject(eObjectId)));

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
			CopyUtils.copyToResource(eObject, resourceWithUpdate, resourceWithDelete);
			break;
		}
		case REMOVE: {
			EcoreUtil.remove(eObject);
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
		return "Update-Delete";
	}

	@Override
	public String getDescription() {
		return String.format(
				"%s object with id %s updated in the %s version while deleted in the other one",
				eObject.eClass().getName(),
				eObjectId,
				deleteInRight ? "left" : "right");
	}

	@Override
	public void resetXMIIds() {
		if (usesXMIIds) {
			resourceWithUpdate.setID(eObject, eObjectId);
		}
	}
}
