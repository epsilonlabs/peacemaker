package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;


public class MultiValuedAttributeDuplicates extends Conflict {

	protected EAttribute mvAttr;

	public MultiValuedAttributeDuplicates(String eObjectId, PeacemakerResource pmResource,
			EAttribute mvAttr) {

		super(eObjectId, pmResource);
		this.mvAttr = mvAttr;
	}

	@Override
	public String getTitle() {
		return "Multi-Valued Attribute Duplicates";
	}

	@Override
	public String getDescription() {
		return String.format(
				"Duplicated values have been found in the %s multi-valued attribute of a %s object with id %s",
				mvAttr.getName(), mvAttr.getEContainingClass().getName(), eObjectId);
	}
}
