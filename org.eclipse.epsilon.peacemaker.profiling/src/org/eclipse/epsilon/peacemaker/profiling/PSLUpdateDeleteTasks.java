package org.eclipse.epsilon.peacemaker.profiling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.URI;

public class PSLUpdateDeleteTasks extends PSLPerformanceProfiling {

	public PSLUpdateDeleteTasks(int repetitions, int warmupReps) {
		super(repetitions, warmupReps);
	}

	@Override
	public List<String> getResultsHeader() {
		return Arrays.asList("numElements", "numConflicts",
				"Peacemaker", "EMFCompare", "EMFDiffMerge", "XMILoad");
	}

	@Override
	public List<List<Object>> getExperiments() {
		List<List<Object>> experiments = new ArrayList<>();
		int[][] taskExperiments = PSLConflictModelsGenerator.getTaskExperiments();
		for (int i = 0; i < taskExperiments.length; i++) {
			experiments.add(Arrays.asList(
					taskExperiments[i][0],
					taskExperiments[i][1]));
		}
		return experiments;
	}

	@Override
	public String getOutputFile() {
		return "results/updatedeleteTasksResults.csv";
	}

	@Override
	protected URI getLeftURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);

		return URI.createFileURI(PSLConflictModelsGenerator.UPDATEDELETE_TAKS_PATH.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.LEFT));
	}

	@Override
	protected URI getAncestorURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);
		return URI.createFileURI(PSLConflictModelsGenerator.UPDATEDELETE_TAKS_PATH.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.ANCESTOR));
	}

	@Override
	protected URI getRightURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);
		return URI.createFileURI(PSLConflictModelsGenerator.UPDATEDELETE_TAKS_PATH.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.RIGHT));
	}

	@Override
	protected URI getConflictedURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);
		return URI.createFileURI(PSLConflictModelsGenerator.UPDATEDELETE_TAKS_PATH.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.CONFLICTED));
	}
}
