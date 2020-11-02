package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.ArrayList;
import java.util.List;

public class ConflictSection {

	protected List<String> leftObjects = new ArrayList<>();
	protected List<String> rightObjects = new ArrayList<>();

	public void addLeft(String objId) {
		leftObjects.add(objId);
	}

	public void addRight(String objId) {
		rightObjects.add(objId);
	}

	public boolean containsLeft(String objId) {
		return leftObjects.contains(objId);
	}

	public void removeLeft(String objId) {
		leftObjects.remove(objId);
	}

	public void removeRight(String objId) {
		rightObjects.remove(objId);
	}

	public boolean isEmpty() {
		return leftObjects.isEmpty() && rightObjects.isEmpty();
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("New, unconflicted elements (ids) in left\n");
		s.append("\t").append(String.join("\n\t", leftObjects)).append("\n\n");
		s.append("New, unconflicted elements (ids) in right\n");
		s.append("\t").append(String.join("\n\t", rightObjects));

		return s.toString();
	}

}
