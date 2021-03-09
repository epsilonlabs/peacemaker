package org.eclipse.epsilon.peacemaker.dt;

import org.eclipse.epsilon.common.dt.AbstractEpsilonUIPlugin;

public class PeacemakerPlugin extends AbstractEpsilonUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.epsilon.peacemaker";

	public static PeacemakerPlugin getDefault() {
		return (PeacemakerPlugin) plugins.get(PeacemakerPlugin.class);
	}
}
