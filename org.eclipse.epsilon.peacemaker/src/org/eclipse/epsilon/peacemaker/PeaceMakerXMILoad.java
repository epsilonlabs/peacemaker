package org.eclipse.epsilon.peacemaker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMILoadImpl;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIHandler.FileSegment;
import org.eclipse.epsilon.peacemaker.util.StreamUtils;
import org.xml.sax.helpers.DefaultHandler;

public class PeaceMakerXMILoad extends XMILoadImpl {

	public static final boolean debug = false;

	public static final String LEFT_TAG = "left:-";
	public static final String SEPARATOR_TAG = "sep:-";
	public static final String RIGHT_TAG = "right:-";

	private static final String LEFT_REGEX = "<<<<<<<\\s*(.*)";
	private static final String SEPARATOR_REGEX = "=======";
	private static final String RIGHT_REGEX = ">>>>>>>\\s*+(.*)";
	private static final String ASSIGNMENT_REGEX = "\\s*([\\w:]+)=(.*)";

	public PeaceMakerXMILoad(XMLHelper helper) {
		super(helper);
	}

	@Override
	protected DefaultHandler makeDefaultHandler() {
		return new PeaceMakerXMIHandler(resource, helper, options);
	}

	@Override
	public void load(XMLResource resource, InputStream inputStream,
			Map<?, ?> options) throws IOException {

		String streamContents = StreamUtils.stream2string(inputStream);

		PeaceMakerXMIResource pmResource = (PeaceMakerXMIResource) resource;
		pmResource.loadLeft(
				getVersionStream(streamContents, FileSegment.LEFT_CONFLICT));
		pmResource.loadRight(
				getVersionStream(streamContents, FileSegment.RIGHT_CONFLICT));

		// convert the conflicts syntax to well-formed XML
		InputStream correctedStream = getConflictsStream(streamContents);

		super.load(resource, correctedStream, options);
	}

	protected InputStream getVersionStream(String input,
			FileSegment versionSegment) throws IOException {

		String[] lines = input.split("\\R");
		String[] output = new String[lines.length];

		FileSegment currentSegment = FileSegment.COMMON;

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.matches(LEFT_REGEX)) {
				currentSegment = FileSegment.LEFT_CONFLICT;
			}
			else if (line.matches(SEPARATOR_REGEX)) {
				currentSegment = FileSegment.RIGHT_CONFLICT;
			}
			else if (line.matches(RIGHT_REGEX)) {
				currentSegment = FileSegment.COMMON;
			}
			else if (currentSegment == FileSegment.COMMON ||
					currentSegment == versionSegment) {
				output[i] = line;
			}
		}

		String result = Arrays.stream(output)
				.filter(line -> line != null)
				.collect(Collectors.joining(System.lineSeparator()));

		if (debug) {
			System.out.println();
			System.out.println("<<< Version model: " + versionSegment.name());
			System.out.println();
			System.out.println(result);
		}

		return new ByteArrayInputStream(result.getBytes());
	}

	protected InputStream getConflictsStream(String input) throws IOException {

		String[] lines = input.split("\\R");
		String[] output = new String[lines.length];

		boolean inElementAttributes = false;
		boolean inExternalConflictSection = false;
		boolean skipNextRightSeparator = false;

		FileSegment currentSegment = FileSegment.COMMON;

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line.startsWith("<?xml")) {
				output[i] = line;
			}
			else if (line.matches(LEFT_REGEX)) {
				currentSegment = FileSegment.LEFT_CONFLICT;

				if (!inElementAttributes) {
					output[i] = line.replaceAll(LEFT_REGEX, "<" + LEFT_TAG + " name=\"$1\"/>");
					inExternalConflictSection = true;
				}
			}
			else if (line.matches(SEPARATOR_REGEX)) {
				currentSegment = FileSegment.RIGHT_CONFLICT;

				if (!inElementAttributes) {
					output[i] = line.replaceAll(SEPARATOR_REGEX, "<" + SEPARATOR_TAG + "/>");
				}
			}
			else if (line.matches(RIGHT_REGEX)) {
				currentSegment = FileSegment.COMMON;
				inExternalConflictSection = false;

				if (skipNextRightSeparator) {
					skipNextRightSeparator = false;
					continue;
				}
				if (!inElementAttributes) {
					output[i] = line.replaceAll(RIGHT_REGEX, "<" + RIGHT_TAG + " name=\"$1\"/>");
				}
			}
			else if (line.contains("<") && !line.contains("</") && !line.contains("/>")) {
				if (!line.contains(">")) {
					inElementAttributes = true;
				}
				output[i] = line;
			}
			else if (line.contains("/>") || line.contains(">")) {
				if (!inExternalConflictSection &&
						inElementAttributes &&
						currentSegment != FileSegment.COMMON) {

					String lineEnding = line.contains("/>") ? "/>" : ">";
					output[i] = line.replaceAll(lineEnding, "");

					output[i] = processAssignment(output[i], currentSegment);

					if (currentSegment == FileSegment.RIGHT_CONFLICT) {
						output[i] += lineEnding;
						inElementAttributes = false;
						skipNextRightSeparator = true; //TODO: this does not solve when other nested elements appear
					}
				}
				else {
					output[i] = line;
					inElementAttributes = false;
				}
			}
			else if (!inExternalConflictSection && inElementAttributes) {
				// this is an assignment of an attribute
				output[i] = processAssignment(line, currentSegment);
			}
			else {
				output[i] = line;
			}
		}
		
		String result = Arrays.stream(output)
				.filter(line -> line != null)
				.collect(Collectors.joining(System.lineSeparator()));

		if (debug) {
			System.out.println();
			System.out.println("<<< Model with Conflicts >>>");
			System.out.println();
			System.out.println(result);
		}

		return new ByteArrayInputStream(result.getBytes());
	}

	protected String processAssignment(String line, FileSegment currentSegment) {
		switch (currentSegment) {
		case LEFT_CONFLICT:
			return line.replaceAll(ASSIGNMENT_REGEX, LEFT_TAG + "$1=$2");
		case RIGHT_CONFLICT:
			return line.replaceAll(ASSIGNMENT_REGEX, RIGHT_TAG + "$1=$2");
		default:
			return line;
		}
	}
}
