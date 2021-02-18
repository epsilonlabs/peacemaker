package org.eclipse.epsilon.peacemaker;


public interface XMIResetIdsHandler {

	/**
	 * Reset XMI ids (if any) to their initial state
	 * (necesary when undoing object removals)
	 */
	public void resetXMIIds();
}
