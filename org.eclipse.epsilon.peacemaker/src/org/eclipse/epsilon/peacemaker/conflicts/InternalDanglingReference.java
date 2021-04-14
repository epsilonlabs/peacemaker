package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;

public class InternalDanglingReference extends Conflict {

	protected EObject referencingObject;
	protected EReference reference;
	protected String referenceValue;

	public InternalDanglingReference(String referencingObjectId, EObject referencingObject,
			PeacemakerResource pmResource, EReference reference, String referenceValue) {

		super(referencingObjectId, pmResource);
		this.referencingObject = referencingObject;
		this.reference = reference;
		this.referenceValue = referenceValue;
	}

	@Override
	public String getTitle() {
		return "Internal Dangling Reference";
	}

	@Override
	public String getDescription() {
		return String.format(
				"An internal dangling reference with value %s"
						+ " from the %s feature of a %s object has been found",
				referenceValue, reference.getName(), referencingObject.eClass().getName());
	}

	@Override
	public boolean supports(ResolveAction action) {
		return false;
	}

}
