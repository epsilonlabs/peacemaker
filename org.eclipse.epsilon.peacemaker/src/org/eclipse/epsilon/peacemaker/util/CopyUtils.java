package org.eclipse.epsilon.peacemaker.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.ExternalCrossReferencer;
import org.eclipse.emf.ecore.xmi.XMIResource;

public class CopyUtils {

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
		XMIResource objResource = getResource(obj);
		XMIResource copyResource = getResource(copy);

		Map<EObject, Collection<Setting>> externalReferences = ExternalCrossReferencer.find(copy);

		for (EObject externalObj : externalReferences.keySet()) {
			if (getResource(externalObj) == objResource) {
				EObject externalObjCopy = copyResource.getEObject(objResource.getID(externalObj));
				if (externalObjCopy != null) {
					for (Setting setting : externalReferences.get(externalObj)) {
						setting.set(externalObjCopy);
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

	protected static XMIResource getResource(EObject obj) {
		return (XMIResource) EcoreUtil.getRootContainer(obj).eResource();
	}


}
