package org.eclipse.epsilon.peacemaker.dt;

import org.eclipse.epsilon.common.dt.AbstractEpsilonUIPlugin;

public class PeaceMakerPlugin extends AbstractEpsilonUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.epsilon.peacemaker";

	public static PeaceMakerPlugin getDefault() {
		return (PeaceMakerPlugin) plugins.get(PeaceMakerPlugin.class);
	}
}
