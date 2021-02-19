package org.eclipse.epsilon.peacemaker.profiling;


public class PSLUpdateDeleteTasksExtraChanges extends PSLPerformanceProfiling {

	public PSLUpdateDeleteTasksExtraChanges(int repetitions, int warmupReps) {
		super(repetitions, warmupReps);

		taskPath = PSLConflictModelsGenerator.UPDATEDELETE_TAKS_EXTRA_CHANGES_PATH;
	}

	@Override
	public String getOutputFile() {
		return "results/updatedeleteTasksResultsExtraChanges.csv";
	}

}
