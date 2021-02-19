package org.eclipse.epsilon.peacemaker.profiling;

public class PSLUpdateDeleteTasks extends PSLPerformanceProfiling {

	public PSLUpdateDeleteTasks(int repetitions, int warmupReps) {
		super(repetitions, warmupReps);

		taskPath = PSLConflictModelsGenerator.UPDATEDELETE_TAKS_PATH;
	}

	@Override
	public String getOutputFile() {
		return "results/updatedeleteTasksResults.csv";
	}
}
