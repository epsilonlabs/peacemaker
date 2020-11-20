package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.ArrayList;
import java.util.List;

public class ConflictSection {

	protected List<String> leftObjects = new ArrayList<>();
	protected List<String> rightObjects = new ArrayList<>();
	protected List<String> baseObjects = new ArrayList<>();

	public void addLeft(String objId) {
		leftObjects.add(objId);
	}

	public void addBase(String objId) {
		baseObjects.add(objId);
	}

	public void addRight(String objId) {
		rightObjects.add(objId);
	}

	public boolean leftContains(String objId) {
		return leftObjects.contains(objId);
	}

	public boolean baseContains(String objId) {
		return baseObjects.contains(objId);
	}

	public boolean rightContains(String objId) {
		return rightObjects.contains(objId);
	}

	public void removeLeft(String objId) {
		leftObjects.remove(objId);
	}

	public void removeRight(String objId) {
		rightObjects.remove(objId);
	}

	public List<String> getLeftIds() {
		return leftObjects;
	}

	public List<String> getRightIds() {
		return rightObjects;
	}

	public boolean isEmpty() {
		return leftObjects.isEmpty() && rightObjects.isEmpty();
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("New (or modified) left objects without match\n");
		s.append("\t").append(String.join("\n\t", leftObjects)).append("\n\n");
		s.append("New (or modified) right objects without match\n");
		s.append("\t").append(String.join("\n\t", rightObjects));

		return s.toString();
	}
}
