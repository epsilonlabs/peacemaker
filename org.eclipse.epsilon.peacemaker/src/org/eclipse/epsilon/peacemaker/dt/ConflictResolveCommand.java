package org.eclipse.epsilon.peacemaker.dt;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict.ResolveAction;

public class ConflictResolveCommand extends ChangeCommand {

	protected Conflict conflict;
	protected ResolveAction action;

	public ConflictResolveCommand(Collection<Notifier> notifiers, Conflict conflict, ResolveAction action) {
		super(notifiers);
		this.conflict = conflict;
		this.action = action;
	}

	@Override
	protected void doExecute() {
		conflict.resolve(action);
	}
}
