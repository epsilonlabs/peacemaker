package org.eclipse.epsilon.peacemaker.profiling;

import java.io.File;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.diffdata.impl.EComparisonImpl;
import org.eclipse.emf.diffmerge.generic.api.IComparison;
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
import org.eclipse.epsilon.profiling.Stopwatch;

public abstract class PSLPerformanceProfiling extends MDBench {

	public PSLPerformanceProfiling(int repetitions, int warmupReps) {
		super(repetitions, warmupReps);
	}

	@Override
	public void doRunIteration(List<Object> parameters, List<String> result) throws Exception {
		result.add("" + runPeacemaker(parameters));
		result.add("" + runEMFCompare(parameters));
		result.add("" + runEMFDiffMerge(parameters));
	}

	protected long runPeacemaker(List<Object> parameters) throws Exception {
		ResourceSet resourceSet = getResourceSet();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"model", new PeaceMakerXMIResourceFactory());
		
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.resume();
		PeaceMakerXMIResource resource =
				(PeaceMakerXMIResource) resourceSet.getResource(getConflictedURI(parameters), true);
		resource.load(null);

		stopwatch.pause();
		return stopwatch.getElapsed();
	}

	protected long runEMFCompare(List<Object> parameters) throws Exception {

		URI leftURI = getLeftURI(parameters);
		URI rightURI = getRightURI(parameters);
		URI ancestorURI = getAncestorURI(parameters);

		ResourceSet resourceSet = getResourceSet();

		Stopwatch stopwatch = new Stopwatch();
		stopwatch.resume();
		IComparisonScope scope = new DefaultComparisonScope(
				resourceSet.getResource(leftURI, true),
				resourceSet.getResource(rightURI, true),
				resourceSet.getResource(ancestorURI, true));
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		comparison.getConflicts();

		stopwatch.pause();
		return stopwatch.getElapsed();
	}

	protected long runEMFDiffMerge(List<Object> parameters) throws Exception {

		URI leftURI = getLeftURI(parameters);
		URI rightURI = getRightURI(parameters);
		URI ancestorURI = getAncestorURI(parameters);

		ResourceSet resourceSet = getResourceSet();

		Stopwatch stopwatch = new Stopwatch();
		stopwatch.resume();

		IEditableModelScope targetScope = new FragmentedModelScope(resourceSet.getResource(leftURI, true), false);
		IEditableModelScope referenceScope = new FragmentedModelScope(resourceSet.getResource(rightURI, true), false);
		IEditableModelScope ancestorScope = new FragmentedModelScope(resourceSet.getResource(ancestorURI, true), false);

		IComparison<EObject> comparison = new EComparisonImpl(targetScope, referenceScope, ancestorScope);

		comparison.compute(new DefaultMatchPolicy(), new DefaultDiffPolicy(), new DefaultMergePolicy(), null);

		stopwatch.pause();
		return stopwatch.getElapsed();
	}

	@Override
	protected ResourceSet getResourceSet() throws Exception {
		ResourceSet resourceSet = super.getResourceSet();

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

	protected abstract URI getLeftURI(List<Object> parameters);

	protected abstract URI getAncestorURI(List<Object> parameters);

	protected abstract URI getRightURI(List<Object> parameters);

	protected abstract URI getConflictedURI(List<Object> parameters);
}
