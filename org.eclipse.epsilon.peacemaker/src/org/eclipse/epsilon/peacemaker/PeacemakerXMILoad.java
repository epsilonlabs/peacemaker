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

public class PeacemakerXMILoad extends XMILoadImpl {

	public static final boolean debug = false;

	public static void main(String[] args) throws Exception {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());

		ResourceSet resourceSet = new ResourceSetImpl();

		ResourceSet ecoreResourceSet = new ResourceSetImpl();

		String[] ecoreFiles = { "models/comicshop.ecore", "models/comicshopIds.ecore" };
		for (String ecoreFile : ecoreFiles) {
			Resource ecoreResource = ecoreResourceSet.createResource(
					URI.createFileURI(new File(ecoreFile).getAbsolutePath()));
			ecoreResource.load(null);

			for (EObject o : ecoreResource.getContents()) {
				EPackage ePackage = (EPackage) o;
				resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
			}
		}

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new PeacemakerResourceFactory());

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
				"15-crossReferencesToConflictedObject",
				"16-externalResourceNotInConflicts",
				"17-attribute-EcoreIds" };

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

	public PeacemakerXMILoad(XMLHelper helper) {
		super(helper);
	}

	@Override
	public void load(XMLResource resource, InputStream inputStream,
			Map<?, ?> options) throws IOException {

		ConflictsPreprocessor preprocessor = new ConflictsPreprocessor(inputStream);
		preprocessor.run();

		PeacemakerResource pmResource = (PeacemakerResource) resource;

		if (!preprocessor.hasConflicts()) {
			// We need to use the helper contents because the inputStream is used and non-resetable
			pmResource.loadSingle(preprocessor, options);
		}
		else {
			load(pmResource, preprocessor, options);
		}
	}

	public void load(PeacemakerResource pmResource, ConflictsPreprocessor preprocessor,
			Map<?, ?> options) throws IOException {

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

		pmResource.loadVersions(preprocessor, options);

		if (debug) {
			for (Conflict c : pmResource.getConflicts()) {
				System.out.println(">>>>>>>> CONFLICT <<<<<<<<<<<<<");
				System.out.println(c);
				System.out.println(">>>>>>>>>>>>>>>><<<<<<<<<<<<<<<");
			}
		}
	}
}
