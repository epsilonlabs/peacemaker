package org.eclipse.epsilon.peacemaker.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.peacemaker.PeacemakerResourceFactory;

public class FormatModels {

	public static void main(String[] args) throws Exception {
		ResourceSet resourceSet = new ResourceSetImpl();

		ResourceSet ecoreResourceSet = new ResourceSetImpl();
		ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());
		Resource ecoreResource = ecoreResourceSet.createResource(
				URI.createFileURI(new File("models/comicshop.ecore").getAbsolutePath()));
		ecoreResource.load(null);
		for (EObject o : ecoreResource.getContents()) {
			EPackage ePackage = (EPackage) o;
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		}

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"model", new PeacemakerResourceFactory());

		String folder = "models/07-newLines-attributes/";
		String[] models = { "base.model", "left.model", "right.model" };

		for (String model : models) {
			breakAttributesInLines(resourceSet, folder + model);
		}

		System.out.println("Done");
	}

	public static void breakAttributesInLines(ResourceSet resourceSet, String modelFile) throws Exception {
		Resource resource = resourceSet.createResource(
				URI.createFileURI(new File(modelFile).getAbsolutePath()));
		resource.load(null);
		resource.save(getBreakAttributesSaveOptions());
	}

	public static Map<?, ?> getBreakAttributesSaveOptions() {
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_LINE_WIDTH, 1);
		return options;
	}
}
