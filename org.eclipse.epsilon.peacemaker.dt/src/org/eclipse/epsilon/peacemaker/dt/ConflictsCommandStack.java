package org.eclipse.epsilon.peacemaker.dt;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict.ResolveAction;

public class ConflictsCommandStack extends BasicCommandStack {

	/**
	 * Undoes conflict commands over the same conflict and action
	 */
	public void undo(Conflict conflict, ResolveAction action) {
		for (Command c : commandList) {
			ConflictResolveCommand command = (ConflictResolveCommand) c;
			if (command.getConflict() == conflict && command.getAction() == action) {
				command.undo();
			}
		}
	}

	@Override
	public void undo() {
		if (canUndo()) {
			Command command = commandList.get(top--);
			try {
				command.undo();
				mostRecentCommand = command;
			}
			catch (RuntimeException exception) {
				handleError(exception);

				mostRecentCommand = null;
				flush();
			}

			// Modification of super method:
			// if the command currently pointed by top can execute (it might
			//   happen if moving between the conflict options), redo it
			if (top >= 0) {
				Command previousCommand = commandList.get(top);
				if (previousCommand.canExecute()) {
					previousCommand.redo(); // not execute, this command has been undone
				}
			}

			notifyListeners();
		}
	}
}
