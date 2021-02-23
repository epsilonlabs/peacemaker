package org.eclipse.epsilon.peacemaker.profiling;

import org.eclipse.epsilon.peacemaker.profiling.BoxesProfilingBenches.BoxesDoubleUpdate;
import org.eclipse.epsilon.peacemaker.profiling.BoxesProfilingBenches.BoxesUpdateDelete;
import org.eclipse.epsilon.peacemaker.profiling.BoxesProfilingBenches.BoxesUpdateDeleteExtraChanges;
import org.eclipse.epsilon.peacemaker.profiling.PSLProfilingBenches.PSLDoubleUpdateTasks;
import org.eclipse.epsilon.peacemaker.profiling.PSLProfilingBenches.PSLUpdateDeleteTasks;
import org.eclipse.epsilon.peacemaker.profiling.PSLProfilingBenches.PSLUpdateDeleteTasksExtraChanges;

public class RunProfilingTests {

	public static void main(String[] args) throws Exception {

		int numReps = 5, numWarms = 2, intermediateWarms = 1;

		new PSLDoubleUpdateTasks(numReps, numWarms).run();
		new PSLUpdateDeleteTasks(numReps, intermediateWarms).run();
		new PSLUpdateDeleteTasksExtraChanges(numReps, 2).run();

		new BoxesDoubleUpdate(numReps, intermediateWarms).run();
		new BoxesUpdateDelete(numReps, intermediateWarms).run();
		new BoxesUpdateDeleteExtraChanges(numReps, intermediateWarms).run();
	}
}
