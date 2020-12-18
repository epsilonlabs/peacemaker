package org.eclipse.epsilon.peacemaker.profiling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.URI;

public class PSLSimpleTasks extends PSLPerformanceProfiling {

	public PSLSimpleTasks(int repetitions, int warmupReps) {
		super(repetitions, warmupReps);
	}

	@Override
	public List<String> getResultsHeader() {
		return Arrays.asList("numElements", "numConflicts",
				"Peacemaker", "EMFCompare", "EMFDiffMerge");
	}

	@Override
	public List<List<Object>> getExperiments() {
		List<List<Object>> experiments = new ArrayList<>();
		for (int i = 0; i < PSLConflictModelsGenerator.SIMPLE_TASKS_EXPERIMENTS.length; i++) {
			experiments.add(Arrays.asList(
					PSLConflictModelsGenerator.SIMPLE_TASKS_EXPERIMENTS[i][0],
					PSLConflictModelsGenerator.SIMPLE_TASKS_EXPERIMENTS[i][1]));
		}
		return experiments;
	}

	@Override
	public String getOutputFile() {
		return "results/simpleTasksResults.csv";
	}

	@Override
	protected URI getLeftURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);

		return URI.createFileURI(PSLConflictModelsGenerator.SIMPLE_TASKS_PATH.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.LEFT));
	}

	@Override
	protected URI getAncestorURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);
		return URI.createFileURI(PSLConflictModelsGenerator.SIMPLE_TASKS_PATH.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.ANCESTOR));
	}

	@Override
	protected URI getRightURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);
		return URI.createFileURI(PSLConflictModelsGenerator.SIMPLE_TASKS_PATH.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.RIGHT));
	}

	@Override
	protected URI getConflictedURI(List<Object> parameters) {
		int numTasks = (int) parameters.get(0);
		int numConflicts = (int) parameters.get(1);
		return URI.createFileURI(PSLConflictModelsGenerator.SIMPLE_TASKS_PATH.getPath(
				numTasks, numConflicts, PSLConflictModelsGenerator.CONFLICTED));
	}
}
