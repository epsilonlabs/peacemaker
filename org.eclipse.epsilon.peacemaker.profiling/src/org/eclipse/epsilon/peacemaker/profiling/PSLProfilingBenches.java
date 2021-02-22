package org.eclipse.epsilon.peacemaker.profiling;


public class PSLProfilingBenches {

	public static class PSLDoubleUpdateTasks extends PSLPerformanceProfiling {

		public PSLDoubleUpdateTasks(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = PSLConflictModelsGenerator.DOUBLEUPDATE_TAKS_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/doubleupdateTasksResults.csv";
		}
	}

	public static class PSLUpdateDeleteTasks extends PSLPerformanceProfiling {

		public PSLUpdateDeleteTasks(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = PSLConflictModelsGenerator.UPDATEDELETE_TAKS_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/updatedeleteTasksResults.csv";
		}
	}

	public static class PSLUpdateDeleteTasksExtraChanges extends PSLPerformanceProfiling {

		public PSLUpdateDeleteTasksExtraChanges(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = PSLConflictModelsGenerator.UPDATEDELETE_TAKS_EXTRA_CHANGES_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/updatedeleteTasksExtraChangesResults.csv";
		}
	}
}
