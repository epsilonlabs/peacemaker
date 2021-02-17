package org.eclipse.epsilon.peacemaker.tests;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.peacemaker.DuplicatedIdsException;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResourceFactory;
import org.eclipse.epsilon.peacemaker.conflicts.ContainingFeatureUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.DoubleUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.KeepDelete;
import org.eclipse.epsilon.peacemaker.conflicts.ReferenceDoubleUpdate;
import org.eclipse.epsilon.peacemaker.conflicts.UnconflictedObject;
import org.eclipse.epsilon.peacemaker.conflicts.UpdateDelete;
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
				"*", new PeaceMakerXMIResourceFactory());
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

		testCase("b3_containment_reference", new Class<?>[] { ReferenceDoubleUpdate.class });

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

		testCase("f", new Class<?>[] { ContainingFeatureUpdate.class });
	}

	@Test
	public void g_ContainerChange() throws IOException {

		assertThrows(DuplicatedIdsException.class, () -> {
			testCase("g", NO_CONFLICTS);
		});
	}

	@Test
	public void ij_KeepDeleteAndFalsePositive() throws IOException {

		testCase("i", new Class<?>[] { KeepDelete.class, KeepDelete.class, KeepDelete.class });

		testCase("j", new Class<?>[] { KeepDelete.class });
	}

	@Test
	public void k_Additions() throws IOException {

		// k1, k2 and k3 are merged by git, causing no conflicts (but duplicated ids)
		testCase("k4", new Class<?>[] { DoubleUpdate.class, KeepDelete.class });
	}

	protected void testCase(String conflictCase, Class<?>[] conflictTypes) throws IOException {
		PeaceMakerXMIResource pmResource = loadConflictResource(conflictCase);

		if (pmResource.hasDuplicatedIds()) {
			throw pmResource.getDuplicatedIdException();
		}

		assertTrue(pmResource.getConflicts().size() == conflictTypes.length);

		for (int c = 0; c < conflictTypes.length; c++) {
			assertTrue(pmResource.getConflicts().get(c).getClass().equals(conflictTypes[c]));
		}
	}

	public static void displayCase(String inputCase) {
		System.out.println("############################################");
		System.out.println(inputCase);
		System.out.println("############################################");
	}

	public static PeaceMakerXMIResource loadConflictResource(String resourceName) throws IOException {
		PeaceMakerXMIResource resource = (PeaceMakerXMIResource) resourceSet.createResource(
				URI.createFileURI(new File(String.format(CONFLICTS_LOCATION, resourceName)).getAbsolutePath()));
		resource.load(null);
		return resource;
	}
}