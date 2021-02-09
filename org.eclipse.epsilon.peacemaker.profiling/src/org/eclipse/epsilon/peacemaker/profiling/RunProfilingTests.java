package org.eclipse.epsilon.peacemaker.profiling;

public class RunProfilingTests {

	public static void main(String[] args) throws Exception {
		PSLPerformanceProfiling bench = new PSLUpdateDeleteTasks(5, 2);
		bench.run();
		System.out.println("Results written to " + bench.getOutputFile());
		
//		bench = new PSLDoubleUpdateTasks(5, 0);
//		bench.run();
//		System.out.println("Results written to " + bench.getOutputFile());
	}
}
