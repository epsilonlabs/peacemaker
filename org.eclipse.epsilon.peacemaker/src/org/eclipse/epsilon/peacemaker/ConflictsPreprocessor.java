package org.eclipse.epsilon.peacemaker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
import org.eclipse.epsilon.peacemaker.util.StreamUtils;

public class ConflictsPreprocessor {

	public static final String LEFT_REGEX = "<<<<<<<\\s?(.*)";
	public static final String BASE_REGEX = "\\|\\|\\|\\|\\|\\|\\|\\s?(.*)";
	public static final String SEPARATOR_REGEX = "=======";
	public static final String RIGHT_REGEX = ">>>>>>>\\s?(.*)";

	enum LineType {
		SEPARATOR, LEFT, BASE, RIGHT, COMMON
	}

	enum FileSegment {
		COMMON, LEFT_CONFLICT, BASE_CONFLICT, RIGHT_CONFLICT
	}


	protected InputStream inputStream;
	// if diff3 base (ancestor) style of conflicts has been detected
	protected boolean hasBaseVersion = false;

	// names of the conflicting versions
	protected String leftVersionName;
	protected String baseVersionName;
	protected String rightVersionName;

	// NOTE: All these line attributes assume 0 is the first line,
	//   so any parameter coming from the handler locator (which starts at 1)
	//   must be substracted 1 before use (the helper uses "originalLine(line)"
	//   for both substracting and translating from the version contents)

	protected String[] lines;
	protected LineType[] lineTypes;
	protected List<Integer> leftOriginalLinesIndex = new ArrayList<>();
	protected List<Integer> baseOriginalLinesIndex = new ArrayList<>();
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

			switch (currentSegment) {
			case COMMON:
				// if a conflict section starts
				if (line.matches(LEFT_REGEX)) {
					lineTypes[index] = LineType.SEPARATOR;
					currentSegment = FileSegment.LEFT_CONFLICT;

					currentSection = new ConflictSection();

					if (leftVersionName == null) {
						leftVersionName = getVersionName(line, LEFT_REGEX);
					}
				}
				// a new common line
				else {
					lineTypes[index] = LineType.COMMON;
					leftOriginalLinesIndex.add(index);
					baseOriginalLinesIndex.add(index);
					rightOriginalLinesIndex.add(index);
				}
				break;
			case LEFT_CONFLICT:
				// if the base segment starts (optional)
				if (line.matches(BASE_REGEX)) {
					hasBaseVersion = true;
					lineTypes[index] = LineType.SEPARATOR;
					currentSegment = FileSegment.BASE_CONFLICT;

					if (baseVersionName == null) {
						baseVersionName = getVersionName(line, BASE_REGEX);
					}
				}
				// if the right segment starts
				else if (line.matches(SEPARATOR_REGEX)) {
					lineTypes[index] = LineType.SEPARATOR;
					currentSegment = FileSegment.RIGHT_CONFLICT;
				}
				// a new line of the left segment
				else {
					lineTypes[index] = LineType.LEFT;
					leftOriginalLinesIndex.add(index);
					line2conflictSection.put(index, currentSection);
				}
				break;
			case BASE_CONFLICT:
				// if the right segment starts
				if (line.matches(SEPARATOR_REGEX)) {
					lineTypes[index] = LineType.SEPARATOR;
					currentSegment = FileSegment.RIGHT_CONFLICT;
				}
				// a new line of the base segment
				else {
					lineTypes[index] = LineType.BASE;
					baseOriginalLinesIndex.add(index);
					line2conflictSection.put(index, currentSection);
				}
				break;
			case RIGHT_CONFLICT:
				// if the right segment ends (and thus the conflict section)
				if (line.matches(RIGHT_REGEX)) {
					lineTypes[index] = LineType.SEPARATOR;
					currentSegment = FileSegment.COMMON;

					currentSection = null;

					if (rightVersionName == null) {
						rightVersionName = getVersionName(line, RIGHT_REGEX);
					}
				}
				// a new line of the right segment
				else {
					lineTypes[index] = LineType.RIGHT;
					rightOriginalLinesIndex.add(index);
					line2conflictSection.put(index, currentSection);
				}
			}
		}
	}

	public boolean hasConflicts() {
		return !line2conflictSection.isEmpty();
	}

	protected String getVersionName(String line, String regex) {
		// we already know the line matches the regex
		Matcher matcher = Pattern.compile(regex).matcher(line);
		matcher.matches();
		return matcher.group(1);
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
			if (!(versionType == LineType.LEFT ||
					versionType == LineType.BASE ||
					versionType == LineType.RIGHT)) {
				throw new IllegalArgumentException("Only left, base, or right versions can be created");
			}
			this.versionType = versionType;
			this.originalLinesIndex = originalLineNumber;
		}

		public InputStream getVersionContents() {
			return new ByteArrayInputStream(originalLinesIndex.parallelStream()
					.map(lineIndex -> lines[lineIndex])
					.collect(Collectors.joining(System.lineSeparator()))
					.getBytes());
		}

		protected int originalLine(int line) {
			return originalLinesIndex.get(line - 1);
		}

		public void addToConflictSections(int start, int end, String objId) {
			// there could be several conflict sections in the same element
			// for the only case this can happen (ObjectRedefinition), we only
			// need to add the element to the first conflict section
			for (int line = originalLine(start); line <= originalLine(end); line++) {
				if (lineTypes[line] == versionType) {
					ConflictSection cs = line2conflictSection.get(line);
					if (versionType == LineType.LEFT) {
						cs.addLeft(objId);
					}
					else if (versionType == LineType.BASE) {
						cs.addBase(objId);
					}
					else {
						cs.addRight(objId);
					}
					break; // we only need the first one
				}
			}
		}

		public boolean inConflictSection(int start, int end) {
			for (int line = originalLine(start); line <= originalLine(end); line++) {
				if (lineTypes[line] == versionType) {
					return true;
				}
			}
			return false;
		}
	}

	public ConflictVersionHelper getLeftVersionHelper() {
		return new ConflictVersionHelper(LineType.LEFT, leftOriginalLinesIndex);
	}

	public ConflictVersionHelper getBaseVersionHelper() {
		return new ConflictVersionHelper(LineType.BASE, baseOriginalLinesIndex);
	}

	public ConflictVersionHelper getRightVersionHelper() {
		return new ConflictVersionHelper(LineType.RIGHT, rightOriginalLinesIndex);
	}

	public String getLeftVersionName() {
		return leftVersionName;
	}

	public String getBaseVersionName() {
		return baseVersionName;
	}

	public String getRightVersionName() {
		return rightVersionName;
	}

	public boolean hasBaseVersion() {
		return hasBaseVersion;
	}
}
