package org.eclipse.epsilon.peacemaker;

import static org.eclipse.epsilon.peacemaker.PeaceMakerXMILoad.LEFT_REGEX;
import static org.eclipse.epsilon.peacemaker.PeaceMakerXMILoad.RIGHT_REGEX;
import static org.eclipse.epsilon.peacemaker.PeaceMakerXMILoad.SEPARATOR_REGEX;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.epsilon.peacemaker.PeaceMakerXMIHandler.FileSegment;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
import org.eclipse.epsilon.peacemaker.util.StreamUtils;

public class ConflictsPreprocessor {

	enum LineType {
		SEPARATOR, LEFT, RIGHT, COMMON
	}

	protected InputStream inputStream;

	protected String[] lines;
	protected LineType[] lineTypes;
	protected List<Integer> leftOriginalLinesIndex = new ArrayList<>();
	protected List<Integer> rightOriginalLinesIndex = new ArrayList<>();

	protected List<ConflictSection> conflictSections = new ArrayList<>();

	public ConflictsPreprocessor(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void run() throws IOException {
		lines = StreamUtils.stream2string(inputStream).split("\\R");
		lineTypes = new LineType[lines.length];

		FileSegment currentSegment = FileSegment.COMMON;
		ConflictSection currentSection = null; //TODO: gather required conflict section information

		for (int index = 0; index < lines.length; index++) {
			String line = lines[index];

			if (line.matches(LEFT_REGEX)) {
				lineTypes[index] = LineType.SEPARATOR;
				currentSegment = FileSegment.LEFT_CONFLICT;

				currentSection = new ConflictSection();
			}
			else if (line.matches(SEPARATOR_REGEX)) {
				lineTypes[index] = LineType.SEPARATOR;
				currentSegment = FileSegment.RIGHT_CONFLICT;
			}
			else if (line.matches(RIGHT_REGEX)) {
				lineTypes[index] = LineType.SEPARATOR;
				currentSegment = FileSegment.COMMON;

				conflictSections.add(currentSection);
				currentSection = null;
			}
			else {
				switch (currentSegment) {
				case COMMON:
					lineTypes[index] = LineType.COMMON;
					leftOriginalLinesIndex.add(index);
					rightOriginalLinesIndex.add(index);
					break;
				case LEFT_CONFLICT:
					lineTypes[index] = LineType.LEFT;
					leftOriginalLinesIndex.add(index);
					break;
				case RIGHT_CONFLICT:
					lineTypes[index] = LineType.RIGHT;
					rightOriginalLinesIndex.add(index);
				}
			}
		}
	}

	public String getOriginalContents() {
		return String.join(System.lineSeparator(), lines);
	}

	public class ConflictVersionHelper {

		protected LineType versionType;
		protected List<Integer> originalLinesIndex;

		public ConflictVersionHelper(LineType versionType, List<Integer> originalLineNumber) {
			this.versionType = versionType;
			this.originalLinesIndex = originalLineNumber;
		}

		public InputStream getVersionContents() {

			return new ByteArrayInputStream(originalLinesIndex.stream()
					.map(lineIndex -> lines[lineIndex])
					.collect(Collectors.joining(System.lineSeparator()))
					.getBytes());
		}
	}

	public ConflictVersionHelper getLeftVersionHelper() {
		return new ConflictVersionHelper(LineType.LEFT, leftOriginalLinesIndex);
	}

	public ConflictVersionHelper getRightVersionHelper() {
		return new ConflictVersionHelper(LineType.RIGHT, rightOriginalLinesIndex);
	}
}
