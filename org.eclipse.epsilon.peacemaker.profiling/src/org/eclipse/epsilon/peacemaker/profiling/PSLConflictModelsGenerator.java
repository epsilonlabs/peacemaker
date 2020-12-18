package org.eclipse.epsilon.peacemaker.profiling;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;

import psl.Project;
import psl.PslFactory;
import psl.PslPackage;
import psl.Task;

public class PSLConflictModelsGenerator {

	public static final String LEFT = "left";
	public static final String ANCESTOR = "ancestor";
	public static final String RIGHT = "right";
	public static final String CONFLICTED = "conflicted";


	@FunctionalInterface
	public interface SimpleTasksPath {
		public String getPath(int numTasks, int numConflicts, String suffix);
	}

	public static SimpleTasksPath SIMPLE_TASKS_PATH = (numTasks, numConflicts, suffix) 
			-> String.format("models/psl-simpleTasks/%delems-%dconflicts_%s.model",
					numTasks, numConflicts, suffix);

	/** number of tasks and conflicts per experiment */
	public static final int[][] SIMPLE_TASKS_EXPERIMENTS = {
			{ 100, 1 },
			{ 1000, 1 },
			{ 10000, 1 }
	};


	public static void main(String[] args) throws Exception {
		PSLConflictModelsGenerator generator = new PSLConflictModelsGenerator();

		for (int i = 0; i < SIMPLE_TASKS_EXPERIMENTS.length; i++) {
			generator.createSimpleTasksConflictModels(
					SIMPLE_TASKS_EXPERIMENTS[i][0], SIMPLE_TASKS_EXPERIMENTS[i][1]);
		}
		System.out.println("Done");
	}


	protected ResourceSet resourceSet;
	protected PslFactory pslFactory;

	public PSLConflictModelsGenerator() throws Exception {
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());
		pslFactory = PslPackage.eINSTANCE.getPslFactory();
	}

	protected void createSimpleTasksConflictModels(int numTasks, int numConflicts) throws Exception {
		String ancestorPath = SIMPLE_TASKS_PATH.getPath(numTasks, numConflicts, ANCESTOR);
		String leftPath = SIMPLE_TASKS_PATH.getPath(numTasks, numConflicts, LEFT);
		String rightPath = SIMPLE_TASKS_PATH.getPath(numTasks, numConflicts, RIGHT);
		String conflictedPath = SIMPLE_TASKS_PATH.getPath(numTasks, numConflicts, CONFLICTED);
		
		XMIResource ancestorResource = (XMIResource) resourceSet.createResource(URI.createFileURI(ancestorPath));

		Project ancestorRoot = pslFactory.createProject();
		ancestorResource.getContents().add(ancestorRoot);
		
		for (int task = 0; task < numTasks; task++) {
			Task t = pslFactory.createTask();
			t.setTitle("task" + task);

			ancestorRoot.getTasks().add(t);
			ancestorResource.setID(t, "taskID" + task);
		}

		XMIResource leftResource = (XMIResource) resourceSet.createResource(URI.createFileURI(leftPath));
		CopyUtils.copyContents(ancestorResource, leftResource);

		XMIResource rightResource = (XMIResource) resourceSet.createResource(URI.createFileURI(rightPath));
		CopyUtils.copyContents(ancestorResource, rightResource);

		Random rand = new Random();
		rand.setSeed(127);
		Set<Integer> conflictedTasks = new HashSet<>();
		while (conflictedTasks.size() < numConflicts) {
			conflictedTasks.add(rand.nextInt(numTasks));
		}

		List<Task> leftTasks = ((Project) leftResource.getContents().get(0)).getTasks();
		List<Task> rightTasks = ((Project) rightResource.getContents().get(0)).getTasks();

		for (int index : conflictedTasks) {
			leftTasks.get(index).setTitle(leftTasks.get(index).getTitle() + LEFT);
			rightTasks.get(index).setTitle(rightTasks.get(index).getTitle() + RIGHT);
		}

		ancestorResource.save(Collections.EMPTY_MAP);
		leftResource.save(Collections.EMPTY_MAP);
		rightResource.save(Collections.EMPTY_MAP);

		// create conflict files with Peacemaker
		ProcessBuilder pb = new ProcessBuilder("diff3", "-m", leftPath, ancestorPath, rightPath);
		pb.directory(new File(System.getProperty("user.dir")));
		pb.redirectOutput(new File(conflictedPath));
		Process process = pb.start();
		process.waitFor();
	}
}