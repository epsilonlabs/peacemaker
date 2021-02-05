package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.util.IdUtils;

/**
 * A special case of a double update conflict where the changes happen in a
 * single (i.e. !isMany()) containment reference
 * 
 * @author alfonsodelavega
 */
public class ReferenceDoubleUpdate extends DoubleUpdate {

	protected EReference reference;

	/*
	 * Each object version contained in the reference might or might not have
	 * the same id, so we need to store them separately
	 * 
	 * The inherited objectId marks the parent object owning the reference value
	 */
	protected String leftObjectId;
	protected String rightObjectId;

	/**
	 * Create a reference double update conflict
	 *
	 * @param parentId  The parent that contains the reference feature
	 * @param reference
	 */
	public ReferenceDoubleUpdate(String parentId, PeaceMakerXMIResource pmResource,
			EReference reference) {
		
		super(parentId, pmResource);
		this.reference = reference;

		leftObject = (EObject) pmResource.getLeftEObject(eObjectId)
				.eGet(reference);
		rightObject = (EObject) pmResource.getRightEObject(eObjectId)
				.eGet(reference);

		leftObjectId = pmResource.getLeftId(leftObject);
		rightObjectId = pmResource.getRightId(rightObject);
	}

	@Override
	public EObject getLeftVersionObject() {
		return leftObject;
	}

	@Override
	public EObject getRightVersionObject() {
		return rightObject;
	}

	public String getLeftVersionId() {
		return leftObjectId;
	}

	public String getRightVersionId() {
		return rightObjectId;
	}

	public void resetXMIIds() {
		if (IdUtils.hasXMIId(pmResource.getLeftResource(), leftObject)) {
			pmResource.getLeftResource().setID(leftObject, leftObjectId);
			pmResource.getRightResource().setID(rightObject, rightObjectId);
		}
	}

	@Override
	public String getTitle() {
		return "Reference Update";
	}

	@Override
	public String getDescription() {
		String referenceEClass = ((EClass)reference.eContainer()).getName();
		return String.format(
				"The single-bounded and containment reference \"%s\" from type %s" +
				" was updated on the left and right versions of a %s object with id %s",
				reference.getName(), referenceEClass, referenceEClass, eObjectId);
	}
}
