package org.eclipse.epsilon.peacemaker.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;

public class IdUtils {

	public static String getAvailableId(XMIResource resource, EObject obj) {
		String id = resource.getID(obj);
		return id != null ? id : EcoreUtil.getID(obj);
	}

	public static boolean hasXMIId(XMIResource resource, EObject obj) {
		return resource.getID(obj) != null;
	}
}
