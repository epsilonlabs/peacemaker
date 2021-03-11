package org.eclipse.epsilon.peacemaker.profiling;

import org.eclipse.epsilon.peacemaker.profiling.BoxesProfilingBenches.*;
import org.eclipse.epsilon.peacemaker.profiling.PSLProfilingBenches.*;

public class RunProfilingTests {

	public static void main(String[] args) throws Exception {

		int numReps = 5, numWarms = 2, intermediateWarms = 1;

		// scenario 1
		new PSLUpdateDeleteTasks(numReps, numWarms).run();

		// scenario 2
		new PSLDoubleUpdateTasks(numReps, intermediateWarms).run();

		// scenario 3
		new PSLUpdateDeleteTasks10PercentChanges(numReps, intermediateWarms).run();
		new PSLUpdateDeleteTasks50PercentChanges(numReps, intermediateWarms).run();
		new PSLUpdateDeleteTasks100PercentChanges(numReps, intermediateWarms).run();

		new PSLUpdateDeleteTasks10PercentConflicts(numReps, intermediateWarms).run();
		new PSLUpdateDeleteTasks50PercentConflicts(numReps, intermediateWarms).run();
		new PSLUpdateDeleteTasks100PercentConflicts(numReps, intermediateWarms).run();

		// scenario 4
		new BoxesUpdateDeleteBox1(numReps, intermediateWarms).run();
		new BoxesUpdateDeleteBox10(numReps, intermediateWarms).run();
		new BoxesUpdateDeleteBox20(numReps, intermediateWarms).run();
	}
}
