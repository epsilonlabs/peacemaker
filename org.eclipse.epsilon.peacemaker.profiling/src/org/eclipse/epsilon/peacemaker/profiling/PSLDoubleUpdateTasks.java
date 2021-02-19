package org.eclipse.epsilon.peacemaker.profiling;

public class PSLDoubleUpdateTasks extends PSLPerformanceProfiling {

	public PSLDoubleUpdateTasks(int repetitions, int warmupReps) {
		super(repetitions, warmupReps);

		taskPath = PSLConflictModelsGenerator.DOUBLEUPDATE_TAKS_PATH;
	}

	@Override
	public String getOutputFile() {
		return "results/doubleupdateTasksResults.csv";
	}
}
