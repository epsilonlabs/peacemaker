package org.eclipse.epsilon.peacemaker.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ResourceMerger {

	public static void main(String[] args) throws IOException {
		
		String folder = "models/08-newLines-severalAttributes";
		String leftFile = new String(Files.readAllBytes(Paths.get(folder + "/left.model")));
		String rightFile = new String(Files.readAllBytes(Paths.get(folder + "/right.model")));

		System.out.println(merge(leftFile, rightFile));
	}

	private static String merge(String leftFile, String rightFile) {
		String[] leftLines = leftFile.split("\\R");
		String[] rightLines = rightFile.split("\\R");

		int leftIndex = 0, rightIndex = 0;
		StringBuilder result = new StringBuilder();
		StringBuilder rightBuffer = null;
		boolean inConflictSection = false;

		while (leftIndex < leftLines.length && rightIndex < rightLines.length) {
			if (inConflictSection) {
				if (leftLines[leftIndex].equals(rightLines[rightIndex])) {
					inConflictSection = false;

					append(result, "=======");
					result.append(rightBuffer.toString()); // to avoid double newLine
					append(result, ">>>>>>>");

					rightBuffer = null;
				}
				else {
					append(result, leftLines[leftIndex]);
					leftIndex++;

					append(rightBuffer, rightLines[rightIndex]);
					rightIndex++;
				}
			}
			else {
				if (leftLines[leftIndex].equals(rightLines[rightIndex])) {
					append(result, leftLines[leftIndex]);
					leftIndex++;
					rightIndex++;
				}
				else {
					inConflictSection = true;
					append(result, "<<<<<<<");
					rightBuffer = new StringBuilder();
				}
			}
		}
		return result.toString();
	}

	private static void append(StringBuilder s, String line) {
		s.append(line).append(System.lineSeparator());
	}

}
