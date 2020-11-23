package org.eclipse.epsilon.peacemaker.util;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;

public class CopyUtils {

	public static void copyIds(EObject obj, EObject copy) {
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
}
