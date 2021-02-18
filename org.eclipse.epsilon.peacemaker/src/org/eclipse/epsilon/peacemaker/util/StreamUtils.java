package org.eclipse.epsilon.peacemaker.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class StreamUtils {

	public static void main(String[] args) throws IOException {

		String folder = "models/09-newLines-asymetricConflicts";
		System.out.println(folder);
		String leftFile = new String(Files.readAllBytes(Paths.get(folder + "/left.model")));
		String rightFile = new String(Files.readAllBytes(Paths.get(folder + "/right.model")));

		merge(leftFile, rightFile, System.out, "HEAD", "branch1");

		System.out.println();

		folder = "models/11-updateDelete";
		System.out.println(folder);
		leftFile = new String(Files.readAllBytes(Paths.get(folder + "/left.model")));
		String baseFile = new String(Files.readAllBytes(Paths.get(folder + "/base.model")));
		rightFile = new String(Files.readAllBytes(Paths.get(folder + "/right.model")));

		merge(leftFile, baseFile, rightFile, System.out, "HEAD", "base", "branch1");
	}

	public static String stream2string(InputStream inputStream) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString("UTF-8");
	}

	public static void merge(String left, String right, OutputStream outputStream,
			String leftVersionName, String rightVersionName) {

		String[] leftLines = left.split("\\R");
		String[] rightLines = right.split("\\R");

		String leftSeparator = "<<<<<<<" + PrettyPrint.prefix(leftVersionName, " ");
		String rightSeparator = ">>>>>>>" + PrettyPrint.prefix(rightVersionName, " ");

		PrintWriter writer = new PrintWriter(outputStream);

		int leftIndex = 0, rightIndex = 0;
		
		while (leftIndex < leftLines.length && rightIndex < rightLines.length) {
			if (leftLines[leftIndex].equals(rightLines[rightIndex])) {
				writer.println(leftLines[leftIndex]);
				leftIndex++;
				rightIndex++;
			}
			else {
				writer.println(leftSeparator);

				String nextCommonLine = findNextCommonline(leftLines, leftIndex, rightLines, rightIndex);
				if (nextCommonLine == null) {
					// the end of the files does not match (i.e. empty root elem)
					while (leftIndex < leftLines.length) {
						writer.println(leftLines[leftIndex]);
						leftIndex++;
					}
					writer.println("=======");
					while (rightIndex < rightLines.length) {
						writer.println(rightLines[rightIndex]);
						rightIndex++;
					}
					writer.println(rightSeparator);
				}
				else {
					while (!leftLines[leftIndex].equals(nextCommonLine)) {
						writer.println(leftLines[leftIndex]);
						leftIndex++;
					}
					writer.println("=======");
					while (!rightLines[rightIndex].equals(nextCommonLine)) {
						writer.println(rightLines[rightIndex]);
						rightIndex++;
					}
					writer.println(rightSeparator);
				}
			}
		}
		writer.flush();
	}

	public static void merge(String left, String base, String right, OutputStream outputStream,
			String leftVersionName, String baseVersionName, String rightVersionName) {

		String[] leftLines = left.split("\\R");
		String[] baseLines = base.split("\\R");
		String[] rightLines = right.split("\\R");

		String leftSeparator = "<<<<<<<" + PrettyPrint.prefix(leftVersionName, " ");
		String baseSeparator = "|||||||" + PrettyPrint.prefix(baseVersionName, " ");
		String rightSeparator = ">>>>>>>" + PrettyPrint.prefix(rightVersionName, " ");

		PrintWriter writer = new PrintWriter(outputStream);

		int leftIndex = 0, baseIndex = 0, rightIndex = 0;

		while (leftIndex < leftLines.length && rightIndex < rightLines.length) {
			if (leftLines[leftIndex].equals(rightLines[rightIndex])) {
				writer.println(leftLines[leftIndex]);
				leftIndex++;
				baseIndex++;
				rightIndex++;
			}
			else {
				// similar to the merge method without a base version (above); here we
				//   need to find a line common to the three versions

				writer.println(leftSeparator);

				String nextCommonLine = findNextCommonline(
						leftLines, leftIndex,
						baseLines, baseIndex,
						rightLines, rightIndex);

				if (nextCommonLine == null) {
					// the end of the files does not match (i.e. empty root elem)
					while (leftIndex < leftLines.length) {
						writer.println(leftLines[leftIndex]);
						leftIndex++;
					}
					writer.println(baseSeparator);
					while (baseIndex < baseLines.length) {
						writer.println(baseLines[baseIndex]);
						baseIndex++;
					}
					writer.println("=======");
					while (rightIndex < rightLines.length) {
						writer.println(rightLines[rightIndex]);
						rightIndex++;
					}
					writer.println(rightSeparator);
				}
				else {
					while (!leftLines[leftIndex].equals(nextCommonLine)) {
						writer.println(leftLines[leftIndex]);
						leftIndex++;
					}
					writer.println(baseSeparator);
					while (!baseLines[baseIndex].equals(nextCommonLine)) {
						writer.println(baseLines[baseIndex]);
						baseIndex++;
					}
					writer.println("=======");
					while (!rightLines[rightIndex].equals(nextCommonLine)) {
						writer.println(rightLines[rightIndex]);
						rightIndex++;
					}
					writer.println(rightSeparator);
				}
			}
		}
		writer.flush();
	}

	private static String findNextCommonline(String[] leftLines, int leftIndex,
			String[] rightLines, int rightIndex) {

		Set<String> bucket = new HashSet<>();
		//TODO: improve performance by filling the bucket with the array that
		//  has less remaining lines
		while (leftIndex < leftLines.length) {
			bucket.add(leftLines[leftIndex]);
			leftIndex++;
		}
		while (rightIndex < rightLines.length) {
			if (bucket.contains(rightLines[rightIndex])) {
				return rightLines[rightIndex];
			}
			rightIndex++;
		}
		return null;
	}

	private static String findNextCommonline(String[] leftLines, int leftIndex,
			String[] baseLines, int baseIndex,
			String[] rightLines, int rightIndex) {

		Set<String> leftBucket = new HashSet<>();
		while (leftIndex < leftLines.length) {
			leftBucket.add(leftLines[leftIndex]);
			leftIndex++;
		}

		Set<String> rightBucket = new HashSet<>();
		while (rightIndex < rightLines.length) {
			rightBucket.add(rightLines[rightIndex]);
			rightIndex++;
		}

		// calculate the intersection of left and right
		leftBucket.retainAll(rightBucket);

		while (baseIndex < baseLines.length) {
			if (leftBucket.contains(baseLines[baseIndex])) {
				return baseLines[baseIndex];
			}
			baseIndex++;
		}
		return null;
	}

}
