package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.ArrayList;
import java.util.List;

public class AttributeRedefinitions extends Conflict {

	protected List<String> leftRedefinitions = new ArrayList<>();
	protected List<String> rightRedefinitions = new ArrayList<>();

	public AttributeRedefinitions(String eObjectId) {
		super(eObjectId);
	}

	public void addLeft(String attributeName) {
		leftRedefinitions.add(attributeName);
	}

	public void addRight(String attributeName) {
		rightRedefinitions.add(attributeName);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("Attribute redefinitions detected").append("\n");
		s.append("Object id: ").append(eObjectId).append("\n");
		s.append("left redefinitions:").append("\n");
		s.append("\t").append(String.join("\n\t", leftRedefinitions)).append("\n");
		s.append("right redefinitions:").append("\n");
		s.append("\t").append(String.join("\n\t", rightRedefinitions));

		return s.toString();
	}
}
