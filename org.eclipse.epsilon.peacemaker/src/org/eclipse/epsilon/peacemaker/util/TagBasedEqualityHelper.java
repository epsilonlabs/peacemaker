package org.eclipse.epsilon.peacemaker.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.epsilon.peacemaker.util.ids.IdUtils;

/**
 * Equality helper that compares the EObject features that are serialised into
 * the starting XML tag, i.e., EAttributes and non-containment EReferences
 */
public class TagBasedEqualityHelper extends EqualityHelper {

	private static final long serialVersionUID = 2L;

	public class ThreeWayComparison {

		protected EObject leftObject;
		protected EObject baseObject;
		protected EObject rightObject;

		protected Set<EStructuralFeature> leftUpdates = new HashSet<>();
		protected Set<EStructuralFeature> rightUpdates = new HashSet<>();

		protected boolean canBeMerged = true;

		public ThreeWayComparison(EObject leftObject, EObject baseObject, EObject rightObject) {
			this.leftObject = leftObject;
			this.baseObject = baseObject;
			this.rightObject = rightObject;

			compare();
		}

		protected void compare() {
			EClass eClass = leftObject.eClass();

			// Check attributes and non-containment references
			for (int i = 0, size = eClass.getFeatureCount(); i < size; ++i) {
				EStructuralFeature feature = eClass.getEStructuralFeature(i);
				// Ignore derived features and containment references
				if (!feature.isDerived() &&
						!(feature instanceof EReference && ((EReference) feature).isContainment())) {

					canBeMerged = canBeMerged(feature);
					if (!canBeMerged) {
						return;
					}
				}
			}
		}

		protected boolean canBeMerged(EStructuralFeature feature) {

			// A feature can only be merged if
			// - there are no conflicting changes in left and right
			// - in the case of references, if the unchanged version can point to 
			//   the same elements as the changed one

			boolean leftEqualsRight = haveEqualFeature(leftObject, rightObject, feature);
			
			if (!leftEqualsRight) {
				boolean leftEqualsBase = haveEqualFeature(leftObject, baseObject, feature);
				boolean rightEqualsBase = haveEqualFeature(rightObject, baseObject, feature);

				// if only updated on the left version
				if (!leftEqualsBase && rightEqualsBase) {
					leftUpdates.add(feature);
					if (feature instanceof EReference &&
							!canReferenceSameObjects(leftObject, rightObject, (EReference) feature)) {
						return false;
					}
				}
				// else if only updated on the right
				else if (leftEqualsBase && !rightEqualsBase) {
					rightUpdates.add(feature);
					if (feature instanceof EReference &&
							!canReferenceSameObjects(rightObject, leftObject, (EReference) feature)) {
						return false;
					}
				}
				// else: both update the base version to a different value
				// this is a conflicting change
				else {
					return false;
				}
			}
			return true;
		}

		public boolean canBeMerged() {
			return canBeMerged;
		}

		/**
		 * Checks whether the objects pointed through a reference from originalObject
		 * can be also referenced from newObject (which is in a different resource)
		 */
		@SuppressWarnings("unchecked")
		public boolean canReferenceSameObjects(EObject originalObject, EObject newObject, EReference reference) {
			XMLResource originalResource = (XMLResource) originalObject.eResource();
			XMLResource newResource = (XMLResource) newObject.eResource();
			
			List<EObject> originalRefObjects = null;
			if (reference.isMany()) {
				originalRefObjects = (List<EObject>) originalObject.eGet(reference);
			}
			else {
				originalRefObjects = new ArrayList<>(1);
				originalRefObjects.add((EObject) originalObject.eGet(reference));
			}

			for (EObject originalRefObject : originalRefObjects) {
				String originalRefObjectId = IdUtils.getAvailableId(originalResource, originalRefObject);
				
				EObject newRefObject = newResource.getEObject(originalRefObjectId);
				if (newRefObject == null) {
					// originalRefObject (from originalResource)
					//   is referenced by originalObject, but that same object
					//   (i.e. with the same id) does not exist in newResource
					return false;
				}
			}
			return true;
		}

		public void merge() {
			if (!canBeMerged) {
				throw new IllegalStateException("Only non-conflicting changes can be automatically merged");
			}
			// TODO: copy left updates to the right, and right updates to the left
		}
	}

	public ThreeWayComparison compare(EObject leftObject, EObject baseObject, EObject rightObject) {
		return new ThreeWayComparison(leftObject, baseObject, rightObject);
	}

	public boolean equals(EObject eObject1, EObject eObject2) {

		// TODO: decide if the comparisons here should be further limited to the ones
		// that make sense in the context of peacemaker

		// If the first object is null, the second object must be null.
		if (eObject1 == null) {
			return eObject2 == null;
		}

		// We know the first object isn't null, so if the second one is, it can't be equal.
		if (eObject2 == null) {
			return false;
		}

		// If eObject1 and eObject2 are the same instance...
		if (eObject1 == eObject2) {
			return true;
		}

		// If eObject1 is a proxy...
		if (eObject1.eIsProxy()) {
			// Then the other object must be a proxy with the same URI.
			if (((InternalEObject) eObject1).eProxyURI().equals(((InternalEObject) eObject2).eProxyURI())) {
				return true;
			}
			else {
				return false;
			}
		}
		// If eObject1 isn't a proxy but eObject2 is, they can't be equal.
		else if (eObject2.eIsProxy()) {
			return false;
		}

		// If they don't have the same class, they can't be equal.
		EClass eClass = eObject1.eClass();
		if (eClass != eObject2.eClass()) {
			return false;
		}

		// Check attributes and non-containment references
		for (int i = 0, size = eClass.getFeatureCount(); i < size; ++i) {
			EStructuralFeature feature = eClass.getEStructuralFeature(i);
			// Ignore derived features and containment references
			if (!feature.isDerived() &&
					!(feature instanceof EReference && ((EReference) feature).isContainment())) {

				if (!haveEqualFeature(eObject1, eObject2, feature)) {
					return false;
				}
			}
		}

		// There's no reason they aren't equal, so they are.
		return true;
	}

	/**
	 * Returns whether the two objects have equal values for the
	 * reference (does not check referenced objects)
	 */
	@SuppressWarnings("unchecked")
	protected boolean haveEqualReference(EObject eObject1, EObject eObject2, EReference reference) {
		Object value1 = eObject1.eGet(reference);
		Object value2 = eObject2.eGet(reference);

		if (reference.isMany()) {
			List<EObject> values1 = (List<EObject>) value1;
			List<EObject> values2 = (List<EObject>) value2;

			if (values1.size() != values2.size()) {
				return false;
			}
			for (int i = 0; i < values1.size(); i++) {
				if (values1.get(i) != values2.get(i)) {
					return false;
				}
			}
		}
		else {
			return value1 == value2;
		}

		return true;
	}
}
