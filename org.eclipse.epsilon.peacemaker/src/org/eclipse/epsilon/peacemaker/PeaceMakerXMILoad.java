package org.eclipse.epsilon.peacemaker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMILoadImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.util.StreamUtils;

public class PeaceMakerXMILoad extends XMILoadImpl {

	public static final boolean debug = false;

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
				"model", new PeaceMakerXMIResourceFactory());

		String[] cases = {
				"01-newInLeft",
				"02-newInBoth-noConflict",
				"03-attribute",
				"04-nonContained1boundedRef",
				"05-contained1boundedRef",
				"06-newLines-newInBoth-noConflict",
				"07-newLines-attributes",
				"08-newLines-severalAttributes",
				"10-newLines-contained1boundedRef",
				"11-updateDelete",
				"12-deleteUpdate",
				"13-severalConflictSectionsSameObject",
				"14-externalCrossReferencesIsMany",
				"15-crossReferencesToConflictedObject" };

		// uncomment for specific cases
		//		cases = new String[1];
		//		cases[0] = "03-attribute";

		for (String inputCase : cases) {
			System.out.println("############################################");
			System.out.println(inputCase);
			System.out.println("############################################");

			Resource resource = resourceSet.createResource(
					URI.createFileURI(new File("modelconflicts/" + inputCase + ".model").getAbsolutePath()));
			resource.load(null);
			System.out.println();
		}
		System.out.println("Done");
	}

	public PeaceMakerXMILoad(XMLHelper helper) {
		super(helper);
	}

	@Override
	public void load(XMLResource resource, InputStream inputStream,
			Map<?, ?> options) throws IOException {

		ConflictsPreprocessor preprocessor = new ConflictsPreprocessor(inputStream);
		preprocessor.run();

		PeaceMakerXMIResource pmResource = (PeaceMakerXMIResource) resource;

		if (!preprocessor.hasConflicts()) {
			// load it as a standard XMI Resource
			// We need to use the helper contents because the inputStream is used and non-resetable
			super.load(resource, preprocessor.getLeftVersionHelper().getVersionContents(), options);
		}
		else {
			load(pmResource, preprocessor);
		}
	}

	public void load(PeaceMakerXMIResource pmResource, ConflictsPreprocessor preprocessor) throws IOException {
		if (debug) {
			System.out.println();
			System.out.println("<<< Model with Conflicts >>>");
			System.out.println(preprocessor.getOriginalContents());
			System.out.println();
			System.out.println("<<< left version: \n" +
					StreamUtils.stream2string(preprocessor.getLeftVersionHelper().getVersionContents()));
			System.out.println();

			if (preprocessor.hasBaseVersion()) {
				System.out.println("||| baseversion: \n" +
						StreamUtils.stream2string(preprocessor.getBaseVersionHelper().getVersionContents()));
				System.out.println();
			}

			System.out.println(">>> right version: \n" +
					StreamUtils.stream2string(preprocessor.getRightVersionHelper().getVersionContents()));
			System.out.println();
		}

		pmResource.loadLeft(preprocessor.getLeftVersionHelper(), preprocessor.getLeftVersionName());
		pmResource.loadRight(preprocessor.getRightVersionHelper(), preprocessor.getRightVersionName());

		if (preprocessor.hasBaseVersion()) {
			pmResource.loadBase(preprocessor.getBaseVersionHelper(), preprocessor.getBaseVersionName());
		}

		pmResource.identifyConflicts(preprocessor.getConflictSections());

		if (debug) {
			for (Conflict c : pmResource.getConflicts()) {
				System.out.println(">>>>>>>> CONFLICT <<<<<<<<<<<<<");
				System.out.println(c);
				System.out.println(">>>>>>>>>>>>>>>><<<<<<<<<<<<<<<");
			}
		}
	}
}
