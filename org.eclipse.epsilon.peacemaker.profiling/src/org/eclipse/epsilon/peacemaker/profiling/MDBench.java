package org.eclipse.epsilon.peacemaker.profiling;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public abstract class MDBench {

	protected int repetitions;
	protected int warmupReps;
	protected List<List<String>> results = new ArrayList<>();

	public MDBench(int repetitions, int warmupReps) {
		this.repetitions = repetitions;
		this.warmupReps = warmupReps;
	}

	protected ResourceSet getResourceSet() throws Exception {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());
		return resourceSet;
	}

	public void run() throws Exception {
		results.add(getResultsHeader());
		for (int warmupRep = 0; warmupRep < warmupReps; warmupRep++) {
			runIteration(false);
		}
		for (int rep = 0; rep < repetitions; rep++) {
			runIteration(true);
		}
	}

	protected void runIteration(boolean recordResults) throws Exception {
		for (List<Object> parameters : getExperiments()) {
			List<String> result = new ArrayList<>();
			for (Object parameter : parameters) {
				result.add(parameter.toString());
			}
			doRunIteration(parameters, result);
			if (recordResults) {
				results.add(result);
			}
		}
		File resultsFile = new File(getOutputFile());
		File parent = resultsFile.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new IllegalStateException("Couldn't create dir: " + parent);
		}
		PrintWriter writer = new PrintWriter(new FileWriter(resultsFile));
		for (List<String> result : results) {
			writer.println(String.join(",", result));
		}
		writer.close();
	}

	public abstract List<String> getResultsHeader();

	public abstract List<List<Object>> getExperiments();

	public abstract String getOutputFile();

	public abstract void doRunIteration(List<Object> parameters, List<String> result) throws Exception;
}
