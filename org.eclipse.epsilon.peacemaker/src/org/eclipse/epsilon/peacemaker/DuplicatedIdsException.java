package org.eclipse.epsilon.peacemaker;


public class DuplicatedIdsException extends RuntimeException {
	private static final long serialVersionUID = 2L;

	protected String id;
	protected int startLine;
	protected int endLine;

	public DuplicatedIdsException(String id, int startLine, int endLine) {
		super();
		this.id = id;
		this.startLine = startLine;
		this.endLine = endLine;
	}

	public String getId() {
		return id;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getEndLine() {
		return endLine;
	}
}
