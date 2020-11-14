package org.eclipse.epsilon.peacemaker.tests;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResourceFactory;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict.ResolveAction;
import org.eclipse.epsilon.peacemaker.conflicts.ReferenceRedefinition;
import org.eclipse.epsilon.peacemaker.dt.ConflictResolveCommand;
import org.eclipse.epsilon.peacemaker.util.FormatModels;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConflictResolveTests {

	private static String CONFLICTS_LOCATION = "modelconflicts/%s.model";

	protected static ResourceSet resourceSet;

	@BeforeClass
	public static void init() throws IOException {
		ResourceSet ecoreResourceSet = new ResourceSetImpl();
		ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());
		Resource ecoreResource = ecoreResourceSet.createResource(
				URI.createFileURI(new File("models/comicshop.ecore").getAbsolutePath()));
		ecoreResource.load(null);

		resourceSet = new ResourceSetImpl();
		for (EObject o : ecoreResource.getContents()) {
			EPackage ePackage = (EPackage) o;
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		}
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"model", new PeaceMakerXMIResourceFactory());
	}

	@Test
	public void contained1BoundedReference() throws IOException {
		String inputCase = "05-contained1boundedRef";
		displayCase(inputCase);
		
		// expected one reference conflict, let's resolve it keeping left
		PeaceMakerXMIResource resource = loadConflictResource(String.format(CONFLICTS_LOCATION, inputCase));

		assertTrue(resource.getConflicts().size() == 2);
		assertTrue(resource.getConflicts().get(0) instanceof ReferenceRedefinition);

		System.out.println("\nKeep left in both (FULL RESOLUTION)");
		resource.getConflicts().get(0).resolve(ResolveAction.KEEP_LEFT);
		resource.getConflicts().get(1).resolve(ResolveAction.KEEP_LEFT);

		resource.save(System.out, Collections.EMPTY_MAP);
		System.out.println("\n");

		// now resolve keeping right
		resource = loadConflictResource(String.format(CONFLICTS_LOCATION, inputCase));

		System.out.println("\nKeep right in both (FULL RESOLUTION)");
		resource.getConflicts().get(0).resolve(ResolveAction.KEEP_RIGHT);
		resource.getConflicts().get(1).resolve(ResolveAction.KEEP_RIGHT);

		resource.doSave(System.out, Collections.EMPTY_MAP);
		System.out.println("\n");
	}

	@Test
	public void attributeRedefinitionsPartialResolution() throws IOException {
		String inputCase = "07-newLines-attributes";
		displayCase(inputCase);

		// expected one reference conflict, let's resolve it keeping left
		PeaceMakerXMIResource resource = loadConflictResource(String.format(CONFLICTS_LOCATION, inputCase));

		assertTrue(resource.getConflicts().size() == 2);

		resource.getConflicts().get(0).resolve(ResolveAction.KEEP_LEFT);

		System.out.println("\nKeep left for first (PARTIAL RESOLUTION)");
		resource.save(System.out, FormatModels.getBreakAttributesSaveOptions());
		System.out.println("\n");
	}

	@Test
	public void attributeRedefinitionsFullResolution() throws IOException {
		String inputCase = "07-newLines-attributes";
		displayCase(inputCase);

		// expected one reference conflict, let's resolve it keeping left
		PeaceMakerXMIResource resource = loadConflictResource(String.format(CONFLICTS_LOCATION, inputCase));

		assertTrue(resource.getConflicts().size() == 2);

		resource.getConflicts().get(0).resolve(ResolveAction.KEEP_LEFT);
		resource.getConflicts().get(1).resolve(ResolveAction.KEEP_RIGHT);

		System.out.println("\nKeep left for first, right for second (FULL RESOLUTION)");
		resource.save(System.out, FormatModels.getBreakAttributesSaveOptions());
		System.out.println("\n");
	}

	@Test
	public void testResolveCommandDoUndo() throws IOException {
		String inputCase = "07-newLines-attributes";
		displayCase(inputCase + "-DoUndo");

		// expected one reference conflict, let's resolve it keeping left
		PeaceMakerXMIResource resource = loadConflictResource(String.format(CONFLICTS_LOCATION, inputCase));

		assertTrue(resource.getConflicts().size() == 2);

		Conflict firstConflict = resource.getConflicts().get(0);
		ConflictResolveCommand command = new ConflictResolveCommand(
				Arrays.asList(resource.getLeftResource(), resource.getRightResource()),
				firstConflict, ResolveAction.KEEP_LEFT, null);

		ByteArrayOutputStream beforeStream = new ByteArrayOutputStream();
		System.out.println("\nBefore executing command:");
		resource.save(beforeStream, FormatModels.getBreakAttributesSaveOptions());
		System.out.println(beforeStream.toString());

		command.execute();
		System.out.println("\nAfter executing command (keep left in first conflict):");
		resource.save(System.out, FormatModels.getBreakAttributesSaveOptions());

		command.undo();
		System.out.println("\nAfter undoing command:");
		ByteArrayOutputStream afterStream = new ByteArrayOutputStream();
		resource.save(afterStream, FormatModels.getBreakAttributesSaveOptions());
		System.out.println(afterStream.toString());
		System.out.println("\n");

		assertTrue(Arrays.equals(beforeStream.toByteArray(), afterStream.toByteArray()));
	}

	public static void displayCase(String inputCase) {
		System.out.println("############################################");
		System.out.println(inputCase);
		System.out.println("############################################");
	}

	public static PeaceMakerXMIResource loadConflictResource(String resourceName) throws IOException {
		PeaceMakerXMIResource resource = (PeaceMakerXMIResource) resourceSet.createResource(
				URI.createFileURI(new File(resourceName).getAbsolutePath()));
		resource.load(null);
		return resource;
	}
}
