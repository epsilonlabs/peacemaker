package org.eclipse.epsilon.peacemaker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMILoadImpl;
import org.eclipse.epsilon.peacemaker.PeaceMakerXMIHandler.FileSegment;
import org.xml.sax.helpers.DefaultHandler;

public class PeaceMakerXMILoad extends XMILoadImpl {

	public static final String LEFT_TAG = "left:-";
	public static final String SEPARATOR_TAG = "sep:-";
	public static final String RIGHT_TAG = "right:-";

	private static final String LEFT_REGEX = "<<<<<<<\\s+(.*)";
	private static final String SEPARATOR_REGEX = "=======";
	private static final String RIGHT_REGEX = ">>>>>>>\\s+(.*)";
	private static final String ASSIGNMENT_REGEX = "\\s+([\\w:]+)=(.*)";

	public PeaceMakerXMILoad(XMLHelper helper) {
		super(helper);
	}

	@Override
	protected DefaultHandler makeDefaultHandler() {
		return new PeaceMakerXMIHandler(resource, helper, options);
	}

	@Override
	public void load(XMLResource resource, InputStream inputStream, Map<?, ?> options) throws IOException {

		// convert the conflicts syntax to well-formed XML
		InputStream correctedStream = preprocessConflicts(inputStream);

		super.load(resource, correctedStream, options);
	}

	protected InputStream preprocessConflicts(InputStream inputStream) throws IOException {
		String streamContents = stream2string(inputStream);

		String[] lines = streamContents.split("\\R");
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
					output[i] = line.replaceAll(SEPARATOR_TAG, "<" + SEPARATOR_TAG + "/>");
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

	protected String stream2string(InputStream inputStream) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString("UTF-8");
	}

}
