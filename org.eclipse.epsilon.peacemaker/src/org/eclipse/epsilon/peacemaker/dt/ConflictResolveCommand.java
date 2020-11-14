package org.eclipse.epsilon.peacemaker.dt;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict.ResolveAction;

public class ConflictResolveCommand extends ChangeCommand {

	protected Conflict conflict;
	protected ResolveAction action;
	protected ResolveAction previousAction;

	protected boolean isExecuted = false;

	public ConflictResolveCommand(Collection<Notifier> notifiers, Conflict conflict,
			ResolveAction action, ResolveAction previousAction) {
		super(notifiers);
		this.conflict = conflict;
		this.action = action;
		this.previousAction = previousAction;
	}

	/**
	 * Executes a command. Only valid if the command has not been executed before
	 */
	@Override
	protected void doExecute() {
		if (isExecuted) {
			throw new IllegalStateException("execute() of an already executed command");
		}
		getConflict().resolve(getAction());
		isExecuted = true;
	}

	/**
	 * Re-executes the command. This must only happen after a command has been
	 * executed first, and then undone
	 */
	@Override
	public void redo() {
		if (isExecuted) {
			throw new IllegalStateException("redo() of an already executed command");
		}
		super.redo();
		isExecuted = true;
	}

	/**
	 * Reverts the command. Only has an effect if the command has been executed before
	 */
	@Override
	public void undo() {
		if (isExecuted) {
			super.undo();
			isExecuted = false;
		}
	}

	/**
	 * Indicates if the command can be executed. Conflict commands are "binary",
	 * in the sense that they can only be executed once
	 */
	@Override
	public boolean canExecute() {
		return !isExecuted;
	}

	public Conflict getConflict() {
		return conflict;
	}

	public ResolveAction getAction() {
		return action;
	}

	public ResolveAction getPreviousAction() {
		return previousAction;
	}
}
