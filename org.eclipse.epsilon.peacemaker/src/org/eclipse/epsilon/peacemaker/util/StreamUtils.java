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
		String leftFile = new String(Files.readAllBytes(Paths.get(folder + "/left.model")));
		String rightFile = new String(Files.readAllBytes(Paths.get(folder + "/right.model")));

		merge(leftFile, rightFile, System.out);
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

	public static void merge(String left, String right, OutputStream outputStream) {

		String[] leftLines = left.split("\\R");
		String[] rightLines = right.split("\\R");

		PrintWriter writer = new PrintWriter(outputStream);

		int leftIndex = 0, rightIndex = 0;
		
		while (leftIndex < leftLines.length && rightIndex < rightLines.length) {
			if (leftLines[leftIndex].equals(rightLines[rightIndex])) {
				writer.println(leftLines[leftIndex]);
				leftIndex++;
				rightIndex++;
			}
			else {
				String nextCommonLine = findNextCommonline(leftLines, leftIndex, rightLines, rightIndex);
				writer.println("<<<<<<<");
				while (!leftLines[leftIndex].equals(nextCommonLine)) {
					writer.println(leftLines[leftIndex]);
					leftIndex++;
				}
				writer.println("=======");
				while (!rightLines[rightIndex].equals(nextCommonLine)) {
					writer.println(rightLines[rightIndex]);
					rightIndex++;
				}
				writer.println(">>>>>>>");
			}
		}
		// one of the sides has ended, only one of the following loops may be entered
		while (leftIndex < leftLines.length) {
			writer.println(leftLines[leftIndex]);
			leftIndex++;
		}
		while (rightIndex < rightLines.length) {
			writer.println(rightLines[rightIndex]);
			rightIndex++;
		}
		writer.flush();
	}

	private static String findNextCommonline(String[] leftLines, int leftIndex, String[] rightLines, int rightIndex) {
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
		throw new RuntimeException("There must be a final common line in all cases");
	}
}
