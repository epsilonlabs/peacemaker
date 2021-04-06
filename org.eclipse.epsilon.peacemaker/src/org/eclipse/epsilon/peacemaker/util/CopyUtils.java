package org.eclipse.epsilon.peacemaker.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.ecore.util.EcoreUtil.ExternalCrossReferencer;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.emf.ecore.xmi.XMIResource;

public class CopyUtils {

	/**
	 * Allows overriding the internal contents of an element with another.
	 * Ignores contained children objects
	 */
	public static class Replacer extends Copier {

		private static final long serialVersionUID = 1L;

		public void replaceContents(EObject replacingObj, EObject replacedObj) {

			EClass eClass = replacingObj.eClass();
			for (int i = 0, size = eClass.getFeatureCount(); i < size; ++i) {
				EStructuralFeature feature = eClass.getEStructuralFeature(i);
				if (feature.isChangeable() && !feature.isDerived()) {
					if (!replacingObj.eIsSet(feature)) {
						replacedObj.eUnset(feature);
					}
					else if (feature instanceof EAttribute) {
						copyAttribute((EAttribute) feature, replacingObj, replacedObj);
					}
					else {
						EReference eReference = (EReference) feature;
						if (!eReference.isContainment()) {
							copyReference(eReference, replacingObj, replacedObj);
						}
					}
				}
			}

			String replacingObjId = getXMIId(replacingObj);
			if (replacingObjId != null && !replacingObjId.equals(getXMIId(replacedObj))) {
				setXMIId(replacedObj, replacingObjId);
			}
		}

		@Override
		protected void copyReference(EReference eReference, EObject replacingObj, EObject replacedObj) {
			XMIResource replacingObjResource = (XMIResource) replacingObj.eResource();
			XMIResource replacedObjResource = (XMIResource) replacedObj.eResource();

			EStructuralFeature.Setting setting = getTarget(eReference, replacingObj, replacedObj);
			if (setting != null) {
				Object value = replacingObj.eGet(eReference, resolveProxies);
				if (eReference.isMany()) {
					@SuppressWarnings("unchecked")
					InternalEList<EObject> source = (InternalEList<EObject>) value;
					@SuppressWarnings("unchecked")
					InternalEList<EObject> target = (InternalEList<EObject>) setting;

					target.clear(); // first step to replace the reference pointers

					int index = 0;
					for (Iterator<EObject> k = resolveProxies ? source.iterator() : source.basicIterator(); k.hasNext();) {
						EObject referencedEObject = k.next();
						EObject nonExternalReferencedEObject = replacedObjResource.getEObject(
								replacingObjResource.getID((EObject) referencedEObject));

						if (nonExternalReferencedEObject == null) {
							// there is nothing in the resource of the replaced
							//   element to replace the current external reference.
							throw new IllegalStateException(
									"Nothing to reference internally to when copying the resource: " + referencedEObject);
						}
						else {
							target.addUnique(index, nonExternalReferencedEObject);
							++index;
						}
					}
				}
				else {
					if (value == null) {
						setting.set(null);
					}
					else {
						EObject nonExternalValue = replacedObjResource.getEObject(
								replacingObjResource.getID((EObject) value));
						if (nonExternalValue == null) {
							// there is nothing in the resource of the replaced
							//   element to replace the current external reference.
							throw new IllegalStateException(
									"Nothing to reference internally to when copying the resource: " + value);
						}
						else {
							setting.set(nonExternalValue);
						}
					}
				}
			}
		}
	}

	public static class DanglingCrossReferencer extends EcoreUtil.CrossReferencer {

		private static final long serialVersionUID = 1L;

		public DanglingCrossReferencer(ResourceSet resourceSet) {
			super(resourceSet);
		}

		public DanglingCrossReferencer(Resource resource) {
			super(resource);
		}

		public DanglingCrossReferencer(EObject obj) {
			super(obj);
		}

		@Override
		protected boolean crossReference(EObject eObject, EReference eReference, EObject crossReferencedEObject) {
			return crossReferencedEObject.eResource() == null && !crossReferencedEObject.eIsProxy() && !eReference.isTransient();
		}

		public Map<EObject, Collection<EStructuralFeature.Setting>> findDanglingCrossReferences() {
			crossReference();
			done();
			return this;
		}
	}

	public static void replace(EObject replacingObj, EObject replacedObj) {
		new Replacer().replaceContents(replacingObj, replacedObj);
	}

	public static void copyToResource(EObject obj, XMIResource objResource, XMIResource otherResource) {
		EObject copy = EcoreUtil.copy(obj);
		EReference ref = (EReference) obj.eContainingFeature();
		if (ref == null) {
			// root object: add new one to resource contents
			safeIndexAdd(otherResource.getContents(),
					objResource.getContents().indexOf(obj), copy);
		}
		else {
			String parentId = objResource.getID(obj.eContainer());
			EObject otherParent = otherResource.getEObject(parentId);
			if (otherParent == null) {
				throw new IllegalStateException(
						"Trying to keep an object while the parent in the other resource does not exist");
			}

			if (ref.isMany()) {
				@SuppressWarnings("unchecked")
				List<EObject> parentRefValues = (List<EObject>) obj.eContainer().eGet(ref);
				@SuppressWarnings("unchecked")
				List<EObject> otherParentRefValues = (List<EObject>) otherParent.eGet(ref);

				safeIndexAdd(otherParentRefValues, parentRefValues.indexOf(obj), copy);
			}
			else {
				otherParent.eSet(ref, copy);
			}
		}
		copyIds(obj, copy);
		fixExternalReferences(obj, copy);
	}

	protected static void copyIds(EObject obj, EObject copy) {
		XMIResource objResource = (XMIResource) obj.eResource();

		// here it is asumed that if an object of the model has an XMI id, then
		// all objects have one
		if (objResource.getID(obj) != null) {
			XMIResource copyResource = (XMIResource) copy.eResource();
			copyResource.setID(copy, objResource.getID(obj));

			Iterator<EObject> objContents = obj.eAllContents();
			Iterator<EObject> copyContents = copy.eAllContents();

			while (objContents.hasNext()) {
				copyResource.setID(copyContents.next(), objResource.getID(objContents.next()));
			}
		}
	}

	/**
	 * Change cross-references to the previous resource with references to
	 * objects in the current resource of the copy
	 */
	protected static void fixExternalReferences(EObject obj, EObject copy) {
		XMIResource objResource = (XMIResource) obj.eResource();
		XMIResource copyResource = (XMIResource) copy.eResource();

		Map<EObject, Collection<Setting>> externalReferences = ExternalCrossReferencer.find(copy);

		for (EObject externalObj : externalReferences.keySet()) {
			if (externalObj.eResource() == objResource) {
				EObject externalObjCopy = copyResource.getEObject(objResource.getID(externalObj));
				if (externalObjCopy != null) {
					for (Setting setting : externalReferences.get(externalObj)) {
						if (!setting.getEStructuralFeature().isMany()) {
							setting.set(externalObjCopy);
						}
						else {
							@SuppressWarnings("unchecked")
							List<EObject> list = (List<EObject>) setting.get(true);

							int index = list.indexOf(externalObj);
							list.remove(index);
							safeIndexAdd(list, index, externalObjCopy);
						}
					}
				}
				else {
					// there is no object to point to in the copy resource, what to do?
					throw new IllegalStateException(
							"Nothing to reference internally to when copying the resource: " + obj);
				}
			}
		}
	}

	public static String getXMIId(EObject obj) {
		if (obj.eResource() == null) {
			throw new IllegalStateException("Object has no resource");
		}
		return ((XMIResource) obj.eResource()).getID(obj);
	}

	public static void setXMIId(EObject obj, String id) {
		if (obj.eResource() == null) {
			throw new IllegalStateException("Object has no resource");
		}
		((XMIResource) obj.eResource()).setID(obj, id);
	}

	/**
	 * Adds object at the indicated index of the list if in range, or at the
	 * end if not
	 */
	public static void safeIndexAdd(List<EObject> list, int index, EObject obj) {
		if (index < 0 || index > list.size()) {
			// out of range: add at the end
			list.add(obj);
		}
		else {
			list.add(index, obj);
		}
	}

	public static void copyContents(XMIResource from, XMIResource to) {
		for (EObject obj : from.getContents()) {
			EObject copy = EcoreUtil.copy(obj);
			to.getContents().add(copy);
			copyIds(obj, copy);
		}
	}

	/**
	 * Swaps object to a different containing feature of the same container
	 */
	public static void swapContainingFeature(EObject obj,
			EStructuralFeature destinationFeature, int destinationIndex) {

		if (destinationFeature.isMany()) {
			@SuppressWarnings("unchecked")
			List<EObject> destinationFeatureValues =
					(List<EObject>) obj.eContainer().eGet(destinationFeature);
			safeIndexAdd(destinationFeatureValues, destinationIndex, obj);
		}
		else {
			obj.eContainer().eSet(destinationFeature, obj);
		}
	}

	@SuppressWarnings("unchecked")
	public static int getContainingFeatureIndex(EObject obj) {
		EStructuralFeature containingFeature = obj.eContainingFeature();
		if (containingFeature == null) {
			return -1;
		}
		if (containingFeature.isMany()) {
			return ((List<EObject>) obj.eContainer().eGet(containingFeature)).indexOf(obj);
		}
		else {
			return 0;
		}
	}

}
