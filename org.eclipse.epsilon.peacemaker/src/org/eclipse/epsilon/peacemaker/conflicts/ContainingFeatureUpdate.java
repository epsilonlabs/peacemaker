package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;

public class ContainingFeatureUpdate extends DoubleUpdate {

	protected EStructuralFeature leftFeature;
	protected EStructuralFeature rightFeature;

	public ContainingFeatureUpdate(String objectId, PeacemakerResource pmResource,
			EStructuralFeature leftFeature, EStructuralFeature rightFeature) {

		super(objectId, pmResource);
		this.leftFeature = leftFeature;
		this.rightFeature = rightFeature;
	}

	@Override
	public String getTitle() {
		return "Containing Feature Update";
	}

	@Override
	public String getDescription() {
		return String.format(
				"The containment feature of the %s object with id (%s) has been changed"
						+ " to %s in the left and to %s in the right",
				leftObject.eClass().getName(), eObjectId,
				leftFeature.getName(), rightFeature.getName());
	}

	@Override
	public void resolve(ResolveAction action) {
		switch (action) {
		case KEEP_LEFT:
			CopyUtils.swapContainingFeature(rightObject, leftFeature,
					CopyUtils.getContainingFeatureIndex(leftObject));
			CopyUtils.replace(leftObject, rightObject);
			break;
		case KEEP_RIGHT:
			CopyUtils.swapContainingFeature(leftObject, rightFeature,
					CopyUtils.getContainingFeatureIndex(rightObject));
			CopyUtils.replace(rightObject, leftObject);
			break;
		default:
			super.resolve(action);
		}
	}

}
