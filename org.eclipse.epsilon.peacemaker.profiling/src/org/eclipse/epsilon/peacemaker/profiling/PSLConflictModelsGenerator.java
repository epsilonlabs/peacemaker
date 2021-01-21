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

import psl.Effort;
import psl.Person;
import psl.Project;
import psl.PslFactory;
import psl.PslPackage;
import psl.Task;

public class PSLConflictModelsGenerator {

	public static void main(String[] args) throws Exception {
		PSLConflictModelsGenerator generator = new PSLConflictModelsGenerator();

		int[][] simpleTaskExperiments = getSimpleTasksExperiments();
		for (int i = 0; i < simpleTaskExperiments.length; i++) {
			generator.createSimpleTasksConflictModels(
					simpleTaskExperiments[i][0], simpleTaskExperiments[i][1]);
		}
		System.out.println("Done");
	}

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
	public static int[][] SIMPLE_TASKS_EXPERIMENTS = null;

	public static int[][] getSimpleTasksExperiments() {
		if (SIMPLE_TASKS_EXPERIMENTS != null) {
			return SIMPLE_TASKS_EXPERIMENTS;
		}
		int[] numTasks = { 1000, 2000, 5000, 10000, 15000, 30000, 50000, 100000, 150000, 200000 };
		int[] numConflicts = { 10 };
		int numExperiments = numConflicts.length * numTasks.length;

		SIMPLE_TASKS_EXPERIMENTS = new int[numExperiments][2];

		for (int tasks = 0; tasks < numTasks.length; tasks++) {
			for (int conflicts = 0; conflicts < numConflicts.length; conflicts++) {
				int position = tasks * numConflicts.length + conflicts;
				SIMPLE_TASKS_EXPERIMENTS[position][0] = numTasks[tasks];
				SIMPLE_TASKS_EXPERIMENTS[position][1] = numConflicts[conflicts];
			}
		}
		return SIMPLE_TASKS_EXPERIMENTS;
	}


	protected ResourceSet resourceSet;
	protected PslFactory pslFactory;

	public PSLConflictModelsGenerator() throws Exception {
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());
		pslFactory = PslPackage.eINSTANCE.getPslFactory();
	}

	public void createSimpleTasksConflictModels(int numTasks, int numConflicts) throws Exception {
		String ancestorPath = SIMPLE_TASKS_PATH.getPath(numTasks, numConflicts, ANCESTOR);
		String leftPath = SIMPLE_TASKS_PATH.getPath(numTasks, numConflicts, LEFT);
		String rightPath = SIMPLE_TASKS_PATH.getPath(numTasks, numConflicts, RIGHT);
		String conflictedPath = SIMPLE_TASKS_PATH.getPath(numTasks, numConflicts, CONFLICTED);
		
		XMIResource ancestorResource = (XMIResource) resourceSet.createResource(URI.createFileURI(ancestorPath));

		Project ancestorRoot = pslFactory.createProject();
		ancestorResource.getContents().add(ancestorRoot);
		ancestorResource.setID(ancestorRoot, "projectID");
		
		int numPeople = 5;
		for (int p = 0; p < numPeople; p++) {
			Person person = pslFactory.createPerson();
			person.setName("Person" + p);
			ancestorRoot.getPeople().add(person);
			ancestorResource.setID(person, "PersonID" + p);
		}

		Random rand = new Random();
		rand.setSeed(127);

		for (int t = 0; t < numTasks; t++) {
			Task task = pslFactory.createTask();
			task.setTitle("task" + t);
			task.setDuration(10);
			task.setStart(20);

			// each task made by two people
			Effort effort = pslFactory.createEffort();
			effort.setPercentage(50);
			effort.setPerson(ancestorRoot.getPeople().get(rand.nextInt(numPeople)));
			task.getEffort().add(effort);
			ancestorResource.setID(effort, task.getTitle() + "effort1");

			effort = pslFactory.createEffort();
			effort.setPercentage(50);
			effort.setPerson(ancestorRoot.getPeople().get(rand.nextInt(numPeople)));
			task.getEffort().add(effort);
			ancestorResource.setID(effort, task.getTitle() + "effort2");

			ancestorRoot.getTasks().add(task);
			ancestorResource.setID(task, "taskID" + t);
		}

		XMIResource leftResource = (XMIResource) resourceSet.createResource(URI.createFileURI(leftPath));
		CopyUtils.copyContents(ancestorResource, leftResource);

		XMIResource rightResource = (XMIResource) resourceSet.createResource(URI.createFileURI(rightPath));
		CopyUtils.copyContents(ancestorResource, rightResource);


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