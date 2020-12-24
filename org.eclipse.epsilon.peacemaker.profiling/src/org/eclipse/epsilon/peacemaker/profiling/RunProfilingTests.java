package org.eclipse.epsilon.peacemaker.profiling;

public class RunProfilingTests {

	public static void main(String[] args) throws Exception {
		PSLPerformanceProfiling bench = new PSLSimpleTasks(5, 2);
		bench.run();
		System.out.println("Results written to " + bench.getOutputFile());
	}
}
