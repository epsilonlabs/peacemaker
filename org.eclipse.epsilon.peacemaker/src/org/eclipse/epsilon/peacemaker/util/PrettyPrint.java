package org.eclipse.epsilon.peacemaker.util;

import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMIResource;

public class PrettyPrint {

	public static String featuresMap(EObject obj) {
		return featuresMap(obj, "");
	}

	private static String featuresMap(EObject obj, String prefix) {
		StringBuilder s = new StringBuilder();

		EClass eclass = obj.eClass();
		String objId = ((XMIResource) obj.eResource()).getID(obj);

		s.append(eclass.getName()).append(" ").append(objId).append(" {\n\t" + prefix);
		s.append(eclass.getEAllStructuralFeatures().stream()
				.filter(feat -> obj.eIsSet(feat))
				.map(feat -> feat.getName() +
						(feat instanceof EAttribute ?
								": " + obj.eGet(feat) :
								"->" + featuresMap((EObject) obj.eGet(feat), prefix + "\t")))
				.collect(Collectors.joining(",\n\t" + prefix)));
		s.append("}");
		return s.toString();
	}
}
