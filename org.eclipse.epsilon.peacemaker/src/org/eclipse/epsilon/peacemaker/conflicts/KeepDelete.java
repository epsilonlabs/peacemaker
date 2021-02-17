package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;

public class KeepDelete extends UpdateDelete {

	public KeepDelete(String eObjectId, PeaceMakerXMIResource pmResource, boolean deleteInRight) {
		super(eObjectId, pmResource, deleteInRight);
	}

	@Override
	public String getTitle() {
		return "Keep-Delete";
	}

	@Override
	public String getDescription() {
		return String.format(
				"%s object with id %s kept (unchanged) in the %s version while deleted in the other one",
				resourceWithUpdate.getEObject(eObjectId).eClass().getName(),
				eObjectId,
				deleteInRight ? "left" : "right");
	}

}
