package org.eclipse.epsilon.peacemaker.profiling;

import java.io.File;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.diffmerge.api.IComparison;
import org.eclipse.emf.diffmerge.api.diff.IDifference;
import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.diffdata.impl.EComparisonImpl;
import org.eclipse.emf.diffmerge.impl.policies.DefaultDiffPolicy;
import org.eclipse.emf.diffmerge.impl.policies.DefaultMatchPolicy;
import org.eclipse.emf.diffmerge.impl.policies.DefaultMergePolicy;
import org.eclipse.emf.diffmerge.impl.scopes.FragmentedModelScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResource;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIResourceFactory;

public class ProgrammaticLaunchesPlayground {

	public static void main(String[] args) throws Exception {
		ProgrammaticLaunchesPlayground playground = new ProgrammaticLaunchesPlayground();

		playground.testPeacemaker();
		playground.testEMFCompare();
		playground.testEMFDiffMerge();
	}

	public void testPeacemaker() throws Exception {
		ResourceSet resourceSet = getResourceSet();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"model", new PeaceMakerXMIResourceFactory());

		PeaceMakerXMIResource resource = (PeaceMakerXMIResource) resourceSet.createResource(
				URI.createFileURI(new File("models/psl/conflicted.model").getAbsolutePath()));
		resource.load(null);
	}

	public void testEMFCompare() throws Exception {
		URI leftURI = URI.createFileURI("models/psl/left.model");
		URI rightURI = URI.createFileURI("models/psl/right.model");
		URI ancestorURI = URI.createFileURI("models/psl/ancestor.model");

		ResourceSet resourceSet = getResourceSet();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());

		IComparisonScope scope = new DefaultComparisonScope(
				resourceSet.getResource(leftURI, true),
				resourceSet.getResource(rightURI, true),
				resourceSet.getResource(ancestorURI, true));
		Comparison comparison = EMFCompare.builder().build().compare(scope);

		List<org.eclipse.emf.compare.Conflict> conflicts = comparison.getConflicts();

		System.out.println(String.format("%d conflicts detected", conflicts.size()));
		for (org.eclipse.emf.compare.Conflict c : conflicts) {
			System.out.println(c);
		}

		System.out.println("@@@@@@@");
		System.out.println("@@@@@@@");
		System.out.println("@@@@@@@");
	}

	public void testEMFDiffMerge() throws Exception {
		URI leftURI = URI.createFileURI("models/psl/left.model");
		URI rightURI = URI.createFileURI("models/psl/right.model");
		URI ancestorURI = URI.createFileURI("models/psl/ancestor.model");

		ResourceSet resourceSet = getResourceSet();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());

		IEditableModelScope targetScope = new FragmentedModelScope(resourceSet.getResource(leftURI, true), false);
		IEditableModelScope referenceScope = new FragmentedModelScope(resourceSet.getResource(rightURI, true), false);
		IEditableModelScope ancestorScope = new FragmentedModelScope(resourceSet.getResource(ancestorURI, true), false);

		IComparison comparison = new EComparisonImpl(targetScope, referenceScope, ancestorScope);

		comparison.compute(new DefaultMatchPolicy(), new DefaultDiffPolicy(), new DefaultMergePolicy(), null);

		for (IDifference d : comparison.getRemainingDifferences()) {
			System.out.println(d);
		}
		System.out.println("@@@@@@@");
		System.out.println("@@@@@@@");
		System.out.println("@@@@@@@");
	}

	public ResourceSet getResourceSet() throws Exception {
		ResourceSet resourceSet = new ResourceSetImpl();

		ResourceSet ecoreResourceSet = new ResourceSetImpl();
		ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());
		Resource ecoreResource = ecoreResourceSet.createResource(
				URI.createFileURI(new File("metamodels/psl.ecore").getAbsolutePath()));
		ecoreResource.load(null);
		for (EObject o : ecoreResource.getContents()) {
			EPackage ePackage = (EPackage) o;
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		}

		return resourceSet;
	}
}
