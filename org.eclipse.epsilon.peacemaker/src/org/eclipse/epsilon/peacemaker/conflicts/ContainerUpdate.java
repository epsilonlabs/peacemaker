package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;

public class ContainerUpdate extends DoubleUpdate {

	public ContainerUpdate(String objectId, PeacemakerResource pmResource) {
		super(objectId, pmResource);
	}

	@Override
	public void resolve(ResolveAction action) {

		switch (action) {
		case KEEP_LEFT:
			String leftContainerId = pmResource.getLeftId(leftObject.eContainer());
			EObject newRightContainer = pmResource.getRightEObject(leftContainerId);
			if (newRightContainer == null) {
				throw new RuntimeException("The destination container does not exist");
			}
			CopyUtils.swapContainer(rightObject, newRightContainer,
					leftObject.eContainingFeature(),
					CopyUtils.getContainingFeatureIndex(leftObject));
			CopyUtils.replace(leftObject, rightObject);
			break;
		case KEEP_RIGHT:
			String rightContainerId = pmResource.getRightId(rightObject.eContainer());
			EObject newLeftContainer = pmResource.getLeftEObject(rightContainerId);
			if (newLeftContainer == null) {
				throw new RuntimeException("The destination container does not exist");
			}
			CopyUtils.swapContainer(leftObject, newLeftContainer,
					rightObject.eContainingFeature(),
					CopyUtils.getContainingFeatureIndex(rightObject));
			CopyUtils.replace(rightObject, leftObject);
			break;
		default:
			super.resolve(action);
		}
	}

	@Override
	public String getTitle() {
		return "Container Update";
	}

	@Override
	public String getDescription() {
		return String.format(
				"The container of a %s object has been updated in the left and right versions",
				leftObject.eClass().getName());
	}

}
