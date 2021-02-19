package org.eclipse.epsilon.peacemaker.profiling;

public class RunProfilingTests {

	public static void main(String[] args) throws Exception {
		PSLPerformanceProfiling bench;

		//		bench = new PSLDoubleUpdateTasks(5, 2);
		//		bench.run();
		//		System.out.println("Results written to " + bench.getOutputFile());
		//
		//		bench = new PSLUpdateDeleteTasks(5, 1);
		//		bench.run();
		//		System.out.println("Results written to " + bench.getOutputFile());

		bench = new PSLUpdateDeleteTasksExtraChanges(5, 2);
		bench.run();
		System.out.println("Results written to " + bench.getOutputFile());
	}
}
