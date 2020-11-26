package org.eclipse.epsilon.peacemaker.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.ExternalCrossReferencer;
import org.eclipse.emf.ecore.xmi.XMIResource;

public class CopyUtils {

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
		finishCopy(obj, copy);
	}

	/**
	 * Replaces an object with a copy of another one
	 */
	public static void copyAndReplace(EObject replacingObj, EObject replacedObj) {
		EObject copy = EcoreUtil.copy(replacingObj);

		EReference reference = (EReference) replacingObj.eContainingFeature();
		if (reference != null) {
			EObject toObjectParent = replacedObj.eContainer();
			if (!reference.isMany()) {
				toObjectParent.eSet(reference, copy);
			}
			else {
				@SuppressWarnings("unchecked")
				List<EObject> list = (List<EObject>) toObjectParent.eGet(reference);

				int index = list.indexOf(replacedObj);
				list.remove(index);
				safeIndexAdd(list, index, copy);
			}
		}
		else {
			// root element
			List<EObject> contents = replacedObj.eResource().getContents();
			int index = contents.indexOf(replacedObj);
			contents.remove(index);
			safeIndexAdd(contents, index, copy);
		}
		finishCopy(replacingObj, copy);
	}

	public static void finishCopy(EObject fromObject, EObject toObject) {
		copyIds(fromObject, toObject);
		fixCrossReferences(fromObject, toObject);
	}

	protected static void copyIds(EObject obj, EObject copy) {
		XMIResource objResource = (XMIResource)obj.eResource();
		XMIResource copyResource = (XMIResource)copy.eResource();
		
		copyResource.setID(copy, objResource.getID(obj));

		Iterator<EObject> objContents = obj.eAllContents();
		Iterator<EObject> copyContents = copy.eAllContents();

		while (objContents.hasNext()) {
			copyResource.setID(copyContents.next(), objResource.getID(objContents.next()));
		}
	}

	/**
	 * Change cross-references to the previous resource with references to
	 * objects in the current resource of the copy
	 */
	protected static void fixCrossReferences(EObject obj, EObject copy) {
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
			finishCopy(obj, copy);
		}
	}
}
