package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.HashSet;
import java.util.Set;

public class ConflictSection {

	protected Set<String> leftIds = new HashSet<>();
	protected Set<String> rightIds = new HashSet<>();
	protected Set<String> baseIds = new HashSet<>();

	public synchronized void addLeft(String objId) {
		leftIds.add(objId);
	}

	public synchronized void addBase(String objId) {
		baseIds.add(objId);
	}

	public synchronized void addRight(String objId) {
		rightIds.add(objId);
	}

	public boolean leftContains(String objId) {
		return leftIds.contains(objId);
	}

	public boolean baseContains(String objId) {
		return baseIds.contains(objId);
	}

	public boolean rightContains(String objId) {
		return rightIds.contains(objId);
	}

	public void removeLeft(String objId) {
		leftIds.remove(objId);
	}

	public void removeRight(String objId) {
		rightIds.remove(objId);
	}

	public Set<String> getLeftIds() {
		return leftIds;
	}

	public Set<String> getRightIds() {
		return rightIds;
	}

	public boolean isEmpty() {
		return leftIds.isEmpty() && rightIds.isEmpty();
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("New (or modified) left ids without match\n");
		s.append("\t").append(String.join("\n\t", leftIds)).append("\n\n");
		s.append("New (or modified) right ids without match\n");
		s.append("\t").append(String.join("\n\t", rightIds));

		return s.toString();
	}
}
