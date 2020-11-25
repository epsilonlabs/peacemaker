package org.eclipse.epsilon.peacemaker.conflicts;

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
			CopyUtils.copyToResource(objectResource.getEObject(eObjectId),
					objectResource, otherResource);
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
