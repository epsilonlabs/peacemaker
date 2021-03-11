package org.eclipse.epsilon.peacemaker.profiling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	public static abstract class PSLUpdateDeleteTasksPercentBenches extends PSLPerformanceProfiling {

		public PSLUpdateDeleteTasksPercentBenches(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);
		}

		@Override
		public List<List<Object>> getExperiments() {
			List<List<Object>> experiments = new ArrayList<>();
			for (int i = 0; i < PSLConflictModelsGenerator.NUM_TASKS.length; i++) {
				experiments.add(Arrays.asList(
						PSLConflictModelsGenerator.NUM_TASKS[i],
						10)); // dummy number of conflicts that in reality is not used
			}
			return experiments;
		}

	}

	public static class PSLUpdateDeleteTasks10PercentChanges extends PSLUpdateDeleteTasksPercentBenches {

		public PSLUpdateDeleteTasks10PercentChanges(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = PSLConflictModelsGenerator.UPDATEDELETE_TAKS_10PERC_EXTRA_CHANGES_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/updatedeleteTasks10percentChanges.csv";
		}
	}

	public static class PSLUpdateDeleteTasks50PercentChanges extends PSLUpdateDeleteTasksPercentBenches {

		public PSLUpdateDeleteTasks50PercentChanges(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = PSLConflictModelsGenerator.UPDATEDELETE_TAKS_50PERC_EXTRA_CHANGES_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/updatedeleteTasks50percentChanges.csv";
		}
	}

	public static class PSLUpdateDeleteTasks100PercentChanges extends PSLUpdateDeleteTasksPercentBenches {

		public PSLUpdateDeleteTasks100PercentChanges(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = PSLConflictModelsGenerator.UPDATEDELETE_TAKS_100PERC_EXTRA_CHANGES_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/updatedeleteTasks100percentChanges.csv";
		}
	}

	public static class PSLUpdateDeleteTasks10PercentConflicts extends PSLUpdateDeleteTasksPercentBenches {

		public PSLUpdateDeleteTasks10PercentConflicts(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = PSLConflictModelsGenerator.UPDATEDELETE_TAKS_10PERC_CONFLICTS_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/updatedeleteTasks10percentConflicts.csv";
		}
	}

	public static class PSLUpdateDeleteTasks50PercentConflicts extends PSLUpdateDeleteTasksPercentBenches {

		public PSLUpdateDeleteTasks50PercentConflicts(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = PSLConflictModelsGenerator.UPDATEDELETE_TAKS_50PERC_CONFLICTS_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/updatedeleteTasks50percentConflicts.csv";
		}
	}

	public static class PSLUpdateDeleteTasks100PercentConflicts extends PSLUpdateDeleteTasksPercentBenches {

		public PSLUpdateDeleteTasks100PercentConflicts(int repetitions, int warmupReps) {
			super(repetitions, warmupReps);

			taskPath = PSLConflictModelsGenerator.UPDATEDELETE_TAKS_100PERC_CONFLICTS_PATH;
		}

		@Override
		public String getOutputFile() {
			return "results/updatedeleteTasks100percentConflicts.csv";
		}
	}
}
