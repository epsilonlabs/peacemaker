package org.eclipse.epsilon.peacemaker.profiling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.diff.DefaultDiffEngine;
import org.eclipse.emf.compare.diff.DiffBuilder;
import org.eclipse.emf.compare.diff.FeatureFilter;
import org.eclipse.emf.compare.diff.IDiffEngine;
import org.eclipse.emf.compare.diff.IDiffProcessor;
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
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;
import org.eclipse.epsilon.peacemaker.PeacemakerResourceFactory;
import org.eclipse.epsilon.peacemaker.profiling.BoxesConflictModelsGenerator.ModelsPath;
import org.eclipse.epsilon.profiling.Stopwatch;

import boxes.BoxesPackage;

public abstract class BoxesPerformanceProfiling extends MDBench {

	protected ModelsPath taskPath;

	public BoxesPerformanceProfiling(int repetitions, int warmupReps) {
		super(repetitions, warmupReps);
	}

	@Override
	public void doRunIteration(List<Object> parameters, List<String> result) throws Exception {
		result.add("" + runPeacemaker(parameters));
		result.add("" + runEMFCompare(parameters));
		result.add("" + runEMFDiffMerge(parameters));
		result.add("" + runXMILoad(parameters));
	}

	@Override
	public List<String> getResultsHeader() {
		return Arrays.asList("numElements", "numConflicts",
				"Peacemaker", "EMFCompare", "EMFDiffMerge", "XMILoad");
	}

	protected long runPeacemaker(List<Object> parameters) throws Exception {
		ResourceSet resourceSet = getResourceSet();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new PeacemakerResourceFactory());
		
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.resume();
		PeacemakerResource resource =
				(PeacemakerResource) resourceSet.getResource(getConflictedURI(parameters), true);
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

		IDiffProcessor diffProcessor = new DiffBuilder();
		IDiffEngine diffEngine = new DefaultDiffEngine(diffProcessor) {

			@Override
			protected FeatureFilter createFeatureFilter() {
				return new FeatureFilter() {

					@Override
					public boolean checkForOrderingChanges(EStructuralFeature feature) {
						return false;
					}
				};
			}
		};
		Comparison comparison = EMFCompare.builder().setDiffEngine(diffEngine).build().compare(scope);
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

	/**
	 * Measure the time it takes for one of the models to load (for reference)
	 */
	protected long runXMILoad(List<Object> parameters) throws Exception {

		URI leftURI = getLeftURI(parameters);

		ResourceSet resourceSet = getResourceSet();

		Stopwatch stopwatch = new Stopwatch();
		stopwatch.resume();

		resourceSet.getResource(leftURI, true);

		stopwatch.pause();
		return stopwatch.getElapsed();
	}

	@Override
	protected ResourceSet getResourceSet() throws Exception {

		ResourceSet resourceSet = super.getResourceSet();

		resourceSet.getPackageRegistry().put(BoxesPackage.eNS_URI, BoxesPackage.eINSTANCE);

		resourceSet.getLoadOptions().put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);

		return resourceSet;
	}

	@Override
	public List<List<Object>> getExperiments() {
		List<List<Object>> experiments = new ArrayList<>();
		int[][] simpleTaskExperiments = BoxesConflictModelsGenerator.getExperiments();
		for (int i = 0; i < simpleTaskExperiments.length; i++) {
			experiments.add(Arrays.asList(
					simpleTaskExperiments[i][0],
					simpleTaskExperiments[i][1]));
		}
		return experiments;
	}

	protected URI getLeftURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);

		return URI.createFileURI(taskPath.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.LEFT));
	}

	protected URI getAncestorURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);
		return URI.createFileURI(taskPath.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.ANCESTOR));
	}

	protected URI getRightURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);
		return URI.createFileURI(taskPath.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.RIGHT));
	}

	protected URI getConflictedURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);
		return URI.createFileURI(taskPath.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.CONFLICTED));
	}
}
