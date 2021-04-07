package org.eclipse.epsilon.peacemaker.tests;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;
import org.eclipse.epsilon.peacemaker.PeacemakerResourceFactory;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict.ResolveAction;
import org.eclipse.epsilon.peacemaker.conflicts.ContainerUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.ContainingFeatureUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.DoubleUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.DuplicatedId;
import org.eclipse.epsilon.peacemaker.conflicts.KeepDelete;
import org.eclipse.epsilon.peacemaker.conflicts.SingleContainmentReferenceUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.UnconflictedObject;
import org.eclipse.epsilon.peacemaker.conflicts.UpdateDelete;
import org.eclipse.epsilon.peacemaker.dt.ConflictResolveCommand;
import org.junit.BeforeClass;
import org.junit.Test;

public class EMFCompareTests {

	private static final String CONFLICTS_LOCATION = "models/emfcompare-generated/%s_gitmerge.nodes";
	protected static final Class<?>[] NO_CONFLICTS = new Class<?>[0];

	protected static ResourceSet resourceSet;

	@BeforeClass
	public static void init() throws IOException {

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());

		resourceSet = new ResourceSetImpl();

		ResourceSet ecoreResourceSet = new ResourceSetImpl();

		String[] ecoreFiles = { "metamodels/nodes.ecore" };
		for (String ecoreFile : ecoreFiles) {
			Resource ecoreResource = ecoreResourceSet.createResource(
					URI.createFileURI(new File(ecoreFile).getAbsolutePath()));
			ecoreResource.load(null);

			for (EObject o : ecoreResource.getContents()) {
				EPackage ePackage = (EPackage) o;
				EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
			}
		}

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new PeacemakerResourceFactory());
	}

	@Test
	public void a_UpdateDeleteSingleValuedFeature() throws IOException {

		Class<?>[] conflictTypes = new Class<?>[] { UpdateDelete.class };

		testCase("a1_attribute", conflictTypes);
		testCase("a1_reference", conflictTypes);

		testCase("a2_attribute", conflictTypes);
		testCase("a2_reference", conflictTypes);

		testCase("a3_attribute", conflictTypes);
		testCase("a3_reference", conflictTypes);
	}

	@Test
	public void b_DoubleUpdateSingleValuedFeature() throws IOException {

		Class<?>[] conflictTypes = new Class<?>[] { DoubleUpdate.class };

		testCase("b1_attribute", conflictTypes);
		testCase("b1_reference", conflictTypes);

		testCase("b2_attribute", conflictTypes);
		testCase("b2_reference", conflictTypes);

		testCase("b3_attribute", conflictTypes);
		testCase("b3_reference", conflictTypes);

		testCase("b3_containment_reference", 
				new Class<?>[] { SingleContainmentReferenceUpdate.class });

		testCase("b4_attribute", conflictTypes);
		testCase("b4_reference", conflictTypes);

		testCase("b5_attribute", NO_CONFLICTS);
		testCase("b5_reference", NO_CONFLICTS);

		testCase("b6_attribute", NO_CONFLICTS);
		testCase("b6_reference", NO_CONFLICTS);
	}

	@Test
	public void c_UpdateDeleteMultiValuedFeature() throws IOException {

		Class<?>[] conflictTypes = new Class<?>[] { UpdateDelete.class };

		testCase("c1_attribute", conflictTypes);
		testCase("c1_reference", conflictTypes);

		testCase("c2_attribute", conflictTypes);
		testCase("c2_reference", conflictTypes);

		testCase("c3_attribute", conflictTypes);
		testCase("c3_reference", conflictTypes);

		testCase("c4_attribute", conflictTypes);
		testCase("c4_reference", conflictTypes);

		testCase("c5_attribute", conflictTypes);
		testCase("c5_reference", conflictTypes);
	}

	@Test
	public void d_ChangeMultiValuedFeature() throws IOException {

		Class<?>[] conflictTypes = new Class<?>[] { DoubleUpdate.class };

		// d1_attribute, d2_attribute and d3_attribute are wrongly processed by git

		testCase("d1_reference", conflictTypes);

		testCase("d2_reference", conflictTypes);

		testCase("d3_reference", conflictTypes);

		testCase("d4_attribute", NO_CONFLICTS);
		testCase("d4_reference", NO_CONFLICTS);

		testCase("d5_attribute", NO_CONFLICTS);
		testCase("d5_reference", NO_CONFLICTS);

		testCase("d6_attribute", NO_CONFLICTS);
		testCase("d6_reference", NO_CONFLICTS);
	}

	@Test
	public void e_DeleteReferencedNotifier() throws IOException {

		testCase("e1", new Class<?>[] { KeepDelete.class, DoubleUpdate.class });

		testCase("e2", new Class<?>[] { KeepDelete.class, DoubleUpdate.class });

		testCase("e3",
				new Class<?>[] { KeepDelete.class, UnconflictedObject.class });
		testCase("e3_avoidemptyroot",
				new Class<?>[] { KeepDelete.class, UnconflictedObject.class });
	}

	@Test
	public void f_ContainingFeatureChange() throws IOException {

		PeacemakerResource pmResource =
				testCase("f", new Class<?>[] { ContainingFeatureUpdate.class });

		String objId = "_4l8U4JRCEeGUu8zWDEISZA";

		EObject leftObj = pmResource.getLeftEObject(objId);
		EObject rightObj = pmResource.getRightEObject(objId);

		EStructuralFeature leftFeature = leftObj.eContainingFeature();
		EStructuralFeature rightFeature = rightObj.eContainingFeature();

		assertTrue(leftFeature != rightFeature);

		Conflict conflict = pmResource.getConflicts().get(0);
		ConflictResolveCommand command = new ConflictResolveCommand(
				Arrays.asList(pmResource.getLeftResource(), pmResource.getRightResource()),
				conflict, ResolveAction.KEEP_LEFT, null);

		command.execute();

		assertTrue(leftObj.eContainingFeature() == rightObj.eContainingFeature()
				&& leftObj.eContainingFeature() == leftFeature);

		command.undo();

		assertTrue(leftObj.eContainingFeature() != rightObj.eContainingFeature()
				&& leftObj.eContainingFeature() == leftFeature
				&& rightObj.eContainingFeature() == rightFeature);

		command = new ConflictResolveCommand(
				Arrays.asList(pmResource.getLeftResource(), pmResource.getRightResource()),
				conflict, ResolveAction.KEEP_RIGHT, null);

		command.execute();

		assertTrue(leftObj.eContainingFeature() == rightObj.eContainingFeature()
				&& rightObj.eContainingFeature() == rightFeature);

		command.undo();

		assertTrue(leftObj.eContainingFeature() != rightObj.eContainingFeature()
				&& leftObj.eContainingFeature() == leftFeature
				&& rightObj.eContainingFeature() == rightFeature);
	}

	@Test
	public void g_ContainerChange() throws IOException {

		testCase("g", new Class<?>[] { ContainerUpdate.class });
		testCase("g_interleaveddummy", new Class<?>[] { DuplicatedId.class });
		testCase("g_interleaveddummy_singleline", new Class<?>[] { ContainerUpdate.class });
	}

	@Test
	public void ij_KeepDeleteAndFalsePositive() throws IOException {

		testCase("i", new Class<?>[] { KeepDelete.class, KeepDelete.class, KeepDelete.class });

		testCase("j", new Class<?>[] { KeepDelete.class });
	}

	@Test
	public void k_Additions() throws IOException {

		testCase("k1", new Class<?>[] { DuplicatedId.class, DuplicatedId.class });
		testCase("k2", new Class<?>[] { DuplicatedId.class, DuplicatedId.class, DuplicatedId.class });
		testCase("k3", new Class<?>[] { DuplicatedId.class, DuplicatedId.class, DuplicatedId.class });
		testCase("k4", new Class<?>[] { DoubleUpdate.class, KeepDelete.class });
	}

	protected PeacemakerResource testCase(String conflictCase, Class<?>[] conflictTypes) throws IOException {
		PeacemakerResource pmResource = loadConflictResource(conflictCase);

		assertTrue(pmResource.getConflicts().size() == conflictTypes.length);

		for (int c = 0; c < conflictTypes.length; c++) {
			assertTrue(pmResource.getConflicts().get(c).getClass().equals(conflictTypes[c]));
		}

		return pmResource;
	}

	@Test
	public void testStreamUtilsDifferentVersionEnding() throws IOException {
		String inputCase = "a1_attribute";

		PeacemakerResource resource = loadConflictResource(inputCase);

		// saving a1_attribute "as is" involves merging versions with different
		//   endings (because the root element of the right version has no
		//   contained objects)

		ByteArrayOutputStream beforeStream = new ByteArrayOutputStream();
		resource.save(beforeStream, Collections.EMPTY_MAP);

		System.out.println(beforeStream.toString());

		assertTrue(Arrays.equals(beforeStream.toByteArray(),
				Files.readAllBytes(Paths.get(String.format(CONFLICTS_LOCATION, inputCase)))));
	}

	public static void displayCase(String inputCase) {
		System.out.println("############################################");
		System.out.println(inputCase);
		System.out.println("############################################");
	}

	public static PeacemakerResource loadConflictResource(String resourceName) throws IOException {
		PeacemakerResource resource = (PeacemakerResource) resourceSet.createResource(
				URI.createFileURI(new File(String.format(CONFLICTS_LOCATION, resourceName)).getAbsolutePath()));
		resource.load(null);
		return resource;
	}
}