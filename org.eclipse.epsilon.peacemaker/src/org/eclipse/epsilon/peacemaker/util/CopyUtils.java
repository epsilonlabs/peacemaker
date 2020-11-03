package org.eclipse.epsilon.peacemaker.util;

import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
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

}
