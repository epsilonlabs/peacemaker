package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

public class ConflictSection {

	protected Set<String> leftIds = new HashSet<>();
	protected Set<String> rightIds = new HashSet<>();
	protected Set<String> baseIds = new HashSet<>();

	protected Set<String> solvedLeftIds = new HashSet<>();

	protected Map<String, EObject> leftObjects = new HashMap<>();
	protected Map<String, EObject> rightObjects = new HashMap<>();
	protected Map<String, EObject> baseObjects = new HashMap<>();

	public synchronized void addLeft(String objId, EObject obj) {
		leftIds.add(objId);
		leftObjects.put(objId, obj);
	}

	public synchronized void addBase(String objId, EObject obj) {
		baseIds.add(objId);
		baseObjects.put(objId, obj);
	}

	public synchronized void addRight(String objId, EObject obj) {
		rightIds.add(objId);
		rightObjects.put(objId, obj);
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

	public EObject getLeft(String objId) {
		return leftObjects.get(objId);
	}

	public EObject getBase(String objId) {
		return baseObjects.get(objId);
	}

	public EObject getRight(String objId) {
		return rightObjects.get(objId);
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

	/**
	 * Mark a identifier from the left segment as solved, basically due to a previously
	 * identified conflict involving several elements of the conflict section
	 */
	public void markSolved(String leftId) {
		if (leftContains(leftId)) {
			solvedLeftIds.add(leftId);
		}
	}

	public boolean isSolved(String leftId) {
		return solvedLeftIds.contains(leftId);
	}
}
