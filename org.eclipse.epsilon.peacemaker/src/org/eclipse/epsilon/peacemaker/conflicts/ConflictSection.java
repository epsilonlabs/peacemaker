package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

public class ConflictSection {

	protected List<Conflict> conflicts = new ArrayList<>();
	protected List<EObject> leftObjects = new ArrayList<>();
	protected List<EObject> rightObjects = new ArrayList<>();

	public void addConflict(Conflict c) {
		getConflicts().add(c);
	}

	public List<Conflict> getConflicts() {
		return conflicts;
	}

	public void addLeft(EObject obj) {
		getLeftObjects().add(obj);
	}

	public void addRight(EObject obj) {
		rightObjects.add(obj);
	}

	public List<EObject> getLeftObjects() {
		return leftObjects;
	}
}
