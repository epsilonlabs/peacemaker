package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;

public class ContainingFeatureUpdate extends DoubleUpdate {

	protected EStructuralFeature leftFeature;
	protected EStructuralFeature rightFeature;

	public ContainingFeatureUpdate(String objectId, PeaceMakerXMIResource pmResource,
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

}
