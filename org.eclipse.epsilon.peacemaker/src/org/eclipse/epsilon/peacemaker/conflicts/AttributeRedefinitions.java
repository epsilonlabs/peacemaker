package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

public class AttributeRedefinitions extends Conflict {

	protected Map<String, AttributeRedefinition> redefinitions = new HashMap<>();

	public class AttributeRedefinition {

		protected String attributeName;
		protected String leftValue;
		protected String rightValue;

		public AttributeRedefinition(String attributeName) {
			this.attributeName = attributeName;
		}

		public String toString() {
			return new StringBuilder()
					.append("Redefinition of ").append(attributeName)
					.append(". left: ").append(leftValue)
					.append(", right: ").append(rightValue)
					.toString();
		}
	}

	public AttributeRedefinitions(EObject eObject, String eObjectId) {
		super(eObject, eObjectId);
	}

	public void addLeft(String attributeName, String value) {
		add(attributeName, value, true);

	}

	public void addRight(String attributeName, String value) {
		add(attributeName, value, false);
	}

	protected void add(String attributeName, String value, boolean addToLeft) {
		AttributeRedefinition redef = redefinitions.get(attributeName);
		if (redef == null) {
			redef = new AttributeRedefinition(attributeName);
			redefinitions.put(attributeName, redef);
		}
		if (addToLeft) {
			redef.leftValue = value;
		}
		else {
			redef.rightValue = value;
		}
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("Attribute redefinitions detected").append("\n");
		s.append("Type: ").append(eObject.eClass().getName()).append(", ");
		s.append("Id: ").append(eObjectId).append("\n");
		for (AttributeRedefinition redef : redefinitions.values()) {
			s.append("\t").append(redef).append("\n");
		}
		
		return s.toString();
	}

}
