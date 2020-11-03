package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class AttributeRedefinitions extends Conflict {

	protected List<String> leftRedefinitions = new ArrayList<>();
	protected List<String> rightRedefinitions = new ArrayList<>();

	protected EObject leftObject;
	protected EObject rightObject;

	protected String leftId;
	protected String rightId;

	public AttributeRedefinitions(String eObjectId) {
		super(eObjectId);
	}

	public void addLeft(String attributeName) {
		leftRedefinitions.add(attributeName);
	}

	public void addRight(String attributeName) {
		rightRedefinitions.add(attributeName);
	}

	public EObject getLeftObject() {
		return leftObject;
	}

	public void setLeftObject(EObject leftObject) {
		this.leftObject = leftObject;
	}

	public EObject getRightObject() {
		return rightObject;
	}

	public void setRightObject(EObject rightObject) {
		this.rightObject = rightObject;
	}

	public String getLeftId() {
		return leftId;
	}

	public void setLeftId(String leftId) {
		this.leftId = leftId;
	}

	public String getRightId() {
		return rightId;
	}

	public void setRightId(String rightId) {
		this.rightId = rightId;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();

		s.append("Attribute redefinitions detected").append("\n");
		s.append("Object id: ").append(eObjectId).append("\n");
		s.append("left redefinitions:").append("\n");
		s.append(leftRedefinitions.stream()
				.map(attr -> "\t" + (attr.equals("xmi:id") ? "xmi:id : " + leftId : prettyPrint(leftObject, attr)))
				.collect(Collectors.joining("\n"))).append("\n");
		s.append("right redefinitions:").append("\n");
		s.append(rightRedefinitions.stream()
				.map(attr -> "\t" + (attr.equals("xmi:id") ? "xmi:id : " + rightId : prettyPrint(rightObject, attr)))
				.collect(Collectors.joining("\n")));
		return s.toString();
	}

	public String prettyPrint(EObject obj, String attr) {
		EStructuralFeature feature = obj.eClass().getEStructuralFeature(attr);
		if (feature == null) {
			return attr;
		}
		else {
			return attr + ": " + obj.eGet(feature);
		}
	}

	@Override
	public void resolve(ResolveAction action) {
		EClass eclass = leftObject.eClass();

		Set<String> redefinitions = new HashSet<>(leftRedefinitions);
		redefinitions.addAll(rightRedefinitions);

		switch (action) {
		case KEEP_LEFT:
			for (String attribute : redefinitions) {
				EStructuralFeature feature = eclass.getEStructuralFeature(attribute);
				rightObject.eSet(feature, leftObject.eGet(feature));
			}
			break;
		case KEEP_RIGHT:
			for (String attribute : redefinitions) {
				EStructuralFeature feature = eclass.getEStructuralFeature(attribute);
				leftObject.eSet(feature, rightObject.eGet(feature));
			}
			break;
		default:
			super.resolve(action);
		}
	}
}
