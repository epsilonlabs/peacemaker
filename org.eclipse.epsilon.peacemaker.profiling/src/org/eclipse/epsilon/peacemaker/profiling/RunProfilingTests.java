package org.eclipse.epsilon.peacemaker.profiling;

import org.eclipse.epsilon.peacemaker.profiling.BoxesProfilingBenches.BoxesDoubleUpdate;
import org.eclipse.epsilon.peacemaker.profiling.BoxesProfilingBenches.BoxesUpdateDelete;
import org.eclipse.epsilon.peacemaker.profiling.PSLProfilingBenches.PSLDoubleUpdateTasks;
import org.eclipse.epsilon.peacemaker.profiling.PSLProfilingBenches.PSLUpdateDeleteTasks;
import org.eclipse.epsilon.peacemaker.profiling.PSLProfilingBenches.PSLUpdateDeleteTasksExtraChanges;

public class RunProfilingTests {

	public static void main(String[] args) throws Exception {
		MDBench bench;

		System.out.println(Runtime.getRuntime().maxMemory());

		bench = new PSLDoubleUpdateTasks(5, 2);
		bench.run();
		System.out.println("Results written to " + bench.getOutputFile());

		bench = new PSLUpdateDeleteTasks(5, 1);
		bench.run();
		System.out.println("Results written to " + bench.getOutputFile());

		bench = new PSLUpdateDeleteTasksExtraChanges(5, 2);
		bench.run();
		System.out.println("Results written to " + bench.getOutputFile());

		bench = new BoxesDoubleUpdate(5, 2);
		bench.run();
		System.out.println("Results written to " + bench.getOutputFile());

		bench = new BoxesUpdateDelete(5, 1);
		bench.run();
		System.out.println("Results written to " + bench.getOutputFile());
	}
}
