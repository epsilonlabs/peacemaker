package org.eclipse.epsilon.peacemaker.profiling;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.peacemaker.util.CopyUtils;

import boxes.Box;
import boxes.Boxes;
import boxes.BoxesFactory;
import boxes.BoxesPackage;

public class BoxesConflictModelsGenerator {

	public static void main(String[] args) throws Exception {
		BoxesConflictModelsGenerator generator = new BoxesConflictModelsGenerator();

		int[][] taskExperiments = getExperiments();
		for (int i = 0; i < taskExperiments.length; i++) {
			int numElems = taskExperiments[i][0];
			int numConflicts = taskExperiments[i][1];

			generator.createDoubleUpdateConflictModels(numElems, numConflicts);

			generator.createUpdateDeleteConflictModels(numElems, numConflicts);
		}
		System.out.println("Done");
	}

	public static final String LEFT = "left";
	public static final String ANCESTOR = "ancestor";
	public static final String RIGHT = "right";
	public static final String CONFLICTED = "conflicted";

	public static final EClass BOX_TYPE = (EClass) BoxesPackage.eINSTANCE.getEClassifier("Box20");

	@FunctionalInterface
	public interface ModelsPath {
		public String getPath(int numElems, int numConflicts, String suffix);
	}

	public static ModelsPath DOUBLEUPDATE_BOXES_PATH =
			(numTasks, numConflicts, suffix) -> String.format("models/boxes-doubleupdate/%delems-%dconflicts_%s.model",
					numTasks, numConflicts, suffix);

	public static ModelsPath UPDATEDELETE_BOXES_PATH =
			(numTasks, numConflicts, suffix) -> String.format("models/boxes-updatedelete/%delems-%dconflicts_%s.model",
					numTasks, numConflicts, suffix);

	public static ModelsPath UPDATEDELETE_BOXES_EXTRA_CHANGES_PATH = (numElems, numConflicts, suffix)
			-> String.format("models/boxes-updatedelete-extraChanges/%delems-%dconflicts_%s.model",
					numElems, numConflicts, suffix);

	/** number of elems and conflicts per experiment */
	public static int[][] EXPERIMENTS = null;

	public static int[][] getExperiments() {
		if (EXPERIMENTS != null) {
			return EXPERIMENTS;
		}
		int[] numElems = { 1000, 2000, 5000, 10000, 15000, 30000, 50000, 100000, 150000, 200000 };
		int[] numConflicts = { 10 };
		int numExperiments = numConflicts.length * numElems.length;

		EXPERIMENTS = new int[numExperiments][2];

		for (int elems = 0; elems < numElems.length; elems++) {
			for (int conflicts = 0; conflicts < numConflicts.length; conflicts++) {
				int position = elems * numConflicts.length + conflicts;
				EXPERIMENTS[position][0] = numElems[elems];
				EXPERIMENTS[position][1] = numConflicts[conflicts];
			}
		}
		return EXPERIMENTS;
	}


	protected BoxesFactory boxesFactory = BoxesPackage.eINSTANCE.getBoxesFactory();

	public BoxesConflictModelsGenerator() throws Exception {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());
	}

	/**
	 * Creates conflict models where tasks with conflicts have their efforts modified
	 */
	public void createDoubleUpdateConflictModels(
			int numElems, int numConflicts) throws Exception {

		String ancestorPath = DOUBLEUPDATE_BOXES_PATH.getPath(numElems, numConflicts, ANCESTOR);
		String leftPath = DOUBLEUPDATE_BOXES_PATH.getPath(numElems, numConflicts, LEFT);
		String rightPath = DOUBLEUPDATE_BOXES_PATH.getPath(numElems, numConflicts, RIGHT);
		String conflictedPath = DOUBLEUPDATE_BOXES_PATH.getPath(numElems, numConflicts, CONFLICTED);

		ResourceSet resourceSet = new ResourceSetImpl();
		XMIResource ancestorResource = (XMIResource) resourceSet.createResource(URI.createFileURI(ancestorPath));

		Boxes ancestorRoot = boxesFactory.createBoxes();
		ancestorResource.getContents().add(ancestorRoot);
		ancestorResource.setID(ancestorRoot, "boxesId");

		Random rand = new Random();
		rand.setSeed(127);

		populateAncestor(ancestorResource, ancestorRoot, BOX_TYPE, numElems, rand);

		XMIResource leftResource = (XMIResource) resourceSet.createResource(URI.createFileURI(leftPath));
		CopyUtils.copyContents(ancestorResource, leftResource);

		XMIResource rightResource = (XMIResource) resourceSet.createResource(URI.createFileURI(rightPath));
		CopyUtils.copyContents(ancestorResource, rightResource);

		Set<Integer> conflicted = new HashSet<>();
		while (conflicted.size() < numConflicts) {
			conflicted.add(rand.nextInt(numElems));
		}

		List<Box> leftElems = ((Boxes) leftResource.getContents().get(0)).getBoxes();
		List<Box> rightElems = ((Boxes) rightResource.getContents().get(0)).getBoxes();

		EStructuralFeature feature = BOX_TYPE.getEStructuralFeature("thing1");
		for (int index : conflicted) {
			leftElems.get(index).eSet(feature, leftElems.get(index).eGet(feature) + LEFT);
			rightElems.get(index).eSet(feature, rightElems.get(index).eGet(feature) + RIGHT);
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

	/**
	 * Creates conflict models where tasks with conflicts have their efforts modified
	 */
	public void createUpdateDeleteConflictModels(
			int numElems, int numConflicts) throws Exception {

		String ancestorPath = UPDATEDELETE_BOXES_PATH.getPath(numElems, numConflicts, ANCESTOR);
		String leftPath = UPDATEDELETE_BOXES_PATH.getPath(numElems, numConflicts, LEFT);
		String rightPath = UPDATEDELETE_BOXES_PATH.getPath(numElems, numConflicts, RIGHT);
		String conflictedPath = UPDATEDELETE_BOXES_PATH.getPath(numElems, numConflicts, CONFLICTED);

		ResourceSet resourceSet = new ResourceSetImpl();
		XMIResource ancestorResource = (XMIResource) resourceSet.createResource(URI.createFileURI(ancestorPath));

		Boxes ancestorRoot = boxesFactory.createBoxes();
		ancestorResource.getContents().add(ancestorRoot);
		ancestorResource.setID(ancestorRoot, "boxesId");

		Random rand = new Random();
		rand.setSeed(127);
		
		populateAncestor(ancestorResource, ancestorRoot, BOX_TYPE, numElems, rand);

		XMIResource leftResource = (XMIResource) resourceSet.createResource(URI.createFileURI(leftPath));
		CopyUtils.copyContents(ancestorResource, leftResource);

		XMIResource rightResource = (XMIResource) resourceSet.createResource(URI.createFileURI(rightPath));
		CopyUtils.copyContents(ancestorResource, rightResource);

		Set<Integer> conflicted = new HashSet<>();
		while (conflicted.size() < numConflicts) {
			conflicted.add(rand.nextInt(numElems));
		}

		List<Box> leftElems = ((Boxes) leftResource.getContents().get(0)).getBoxes();

		EStructuralFeature feature = BOX_TYPE.getEStructuralFeature("thing1");
		for (int index : conflicted) {
			leftElems.get(index).eSet(feature, leftElems.get(index).eGet(feature) + LEFT);
		}

		List<Integer> orderedConflictedElems = new ArrayList<>(conflicted);
		Collections.sort(orderedConflictedElems, Collections.reverseOrder());

		Boxes rightRoot = (Boxes) rightResource.getContents().get(0);
		for (int index : orderedConflictedElems) {
			rightRoot.getBoxes().remove(index);
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

	public void populateAncestor(XMIResource ancestorResource, Boxes ancestorRoot,
			EClass boxType, int numElems, Random rand) {

		for (int t = 0; t < numElems; t++) {
			Box box = (Box) boxesFactory.create(boxType);

			int thingNumber = 1;
			for (EAttribute attr : boxType.getEAttributes()) {
				box.eSet(attr, "value" + thingNumber);
				thingNumber++;
			}

			ancestorRoot.getBoxes().add(box);
			ancestorResource.setID(box, "boxID" + t);
		}
	}
}