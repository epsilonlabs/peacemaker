package org.eclipse.epsilon.peacemaker.profiling;

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
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.epsilon.profiling.Stopwatch;

public class BoxesProfilingBenches {

	public static class BoxesDoubleUpdate extends BoxesPerformanceProfiling {

		public BoxesDoubleUpdate(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = BoxesConflictModelsGenerator.DOUBLEUPDATE_BOXES_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/doubleupdateBoxesResults.csv";
		}
	}

	public static class BoxesUpdateDelete extends BoxesPerformanceProfiling {

		public BoxesUpdateDelete(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = BoxesConflictModelsGenerator.UPDATEDELETE_BOXES_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/updatedeleteBoxesResults.csv";
		}

		@Override
		protected long runEMFCompare(List<Object> parameters) throws Exception {

			// this benchmark needs a special engine to avoid checking for
			// ordering changes, because there is an OutOfMemory bug in EMFCompare
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432497

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
	}

	public static class BoxesUpdateDeleteExtraChanges extends BoxesUpdateDelete {

		public BoxesUpdateDeleteExtraChanges(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = BoxesConflictModelsGenerator.UPDATEDELETE_BOXES_EXTRA_CHANGES_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/updatedeleteBoxesExtraChangesResults.csv";
		}
	}
}
