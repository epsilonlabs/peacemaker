package org.eclipse.epsilon.peacemaker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.epsilon.peacemaker.PeaceMakerXMIHandler.FileSegment;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
import org.eclipse.epsilon.peacemaker.util.StreamUtils;

public class ConflictsPreprocessor {

	public static final String LEFT_REGEX = "<<<<<<<\\s*(.*)";
	public static final String SEPARATOR_REGEX = "=======";
	public static final String RIGHT_REGEX = ">>>>>>>\\s*+(.*)";

	enum LineType {
		SEPARATOR, LEFT, RIGHT, COMMON
	}

	protected InputStream inputStream;

	// NOTE: All these line attributes assume 0 is the first line,
	//   so any parameter coming from the handler locator (which starts at 1)
	//   must be substracted 1 before use (the helper uses "originalLine(line)"
	//   for both substracting and translating from the version contents)

	protected String[] lines;
	protected LineType[] lineTypes;
	protected List<Integer> leftOriginalLinesIndex = new ArrayList<>();
	protected List<Integer> rightOriginalLinesIndex = new ArrayList<>();
	protected Map<Integer, ConflictSection> line2conflictSection = new LinkedHashMap<>();

	public ConflictsPreprocessor(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void run() throws IOException {
		lines = StreamUtils.stream2string(inputStream).split("\\R");
		lineTypes = new LineType[lines.length];

		FileSegment currentSegment = FileSegment.COMMON;
		ConflictSection currentSection = null;

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
					line2conflictSection.put(index, currentSection);
					break;
				case RIGHT_CONFLICT:
					lineTypes[index] = LineType.RIGHT;
					rightOriginalLinesIndex.add(index);
					line2conflictSection.put(index, currentSection);
				}
			}
		}
	}

	public String getOriginalContents() {
		return String.join(System.lineSeparator(), lines);
	}

	public List<ConflictSection> getConflictSections() {
		return new ArrayList<>(new LinkedHashSet<>(line2conflictSection.values()));
	}

	public class ConflictVersionHelper {

		protected LineType versionType;
		protected List<Integer> originalLinesIndex; // careful: handler locator lines start in 1

		public ConflictVersionHelper(LineType versionType, List<Integer> originalLineNumber) {
			if (!(versionType == LineType.LEFT | versionType == LineType.RIGHT)) {
				throw new IllegalArgumentException("Only left or right versions can be created");
			}
			this.versionType = versionType;
			this.originalLinesIndex = originalLineNumber;
		}

		public InputStream getVersionContents() {

			return new ByteArrayInputStream(originalLinesIndex.stream()
					.map(lineIndex -> lines[lineIndex])
					.collect(Collectors.joining(System.lineSeparator()))
					.getBytes());
		}

		protected int originalLine(int line) {
			return originalLinesIndex.get(line - 1);
		}

		public void addToConflictSections(int start, int end, String objId) {
			// there could be several conflict sections in the same element
			Set<ConflictSection> conflictSections = new HashSet<>();
			for (int line = originalLine(start); line <= originalLine(end); line++) {
				if (lineTypes[line] == versionType) {
					conflictSections.add(line2conflictSection.get(line));
				}
			}
			for (ConflictSection cs : conflictSections) {
				if (versionType == LineType.LEFT) {
					cs.addLeft(objId);
				}
				else {
					cs.addRight(objId);
				}
			}
		}
	}

	public ConflictVersionHelper getLeftVersionHelper() {
		return new ConflictVersionHelper(LineType.LEFT, leftOriginalLinesIndex);
	}

	public ConflictVersionHelper getRightVersionHelper() {
		return new ConflictVersionHelper(LineType.RIGHT, rightOriginalLinesIndex);
	}
}
