package org.eclipse.epsilon.peacemaker.profiling;

import java.util.Arrays;
import java.util.List;

public class ProfilePeacemaker extends BoxesPerformanceProfiling {

	public static void main(String[] args) throws Exception {
		new ProfilePeacemaker(5, 0).run();
		System.out.println("Done");
	}

	public ProfilePeacemaker(int repetitions, int warmupReps) {
		super(repetitions, warmupReps);

		taskPath = BoxesConflictModelsGenerator.UPDATEDELETE_BOXES_PATH;
	}

	@Override
	public List<String> getResultsHeader() {
		return Arrays.asList("Peacemaker");
	}

	@Override
	public void doRunIteration(List<Object> parameters, List<String> result) throws Exception {
		System.out.println("************************************");
		result.add("" + runPeacemaker(parameters));
	}

	@Override
	public String getOutputFile() {
		return "results/peacemakerProfiling.csv";
	}

}
