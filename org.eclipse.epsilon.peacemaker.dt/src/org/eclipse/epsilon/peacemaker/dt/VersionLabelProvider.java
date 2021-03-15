package org.eclipse.epsilon.peacemaker.dt;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict.ConflictObjectStatus;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Overrides a label provider based on the status of the detected conflicts
 */
public class VersionLabelProvider extends ColumnLabelProvider {

	protected ILabelProvider labelProvider;
	protected IColorProvider colorProvider;
	protected XMIResource resource;
	protected Map<String, ConflictObjectStatus> conflictObject2status;

	public VersionLabelProvider(ILabelProvider labelProvider, XMIResource resource,
			Map<String, ConflictObjectStatus> conflictObject2status) {

		this.labelProvider = labelProvider;
		if (labelProvider instanceof IColorProvider) {
			colorProvider = (IColorProvider) labelProvider;
		}
		this.resource = resource;
		this.conflictObject2status = conflictObject2status;
	}

	@Override
	public Image getImage(Object element) {
		return labelProvider.getImage(element);
	}

	@Override
	public String getText(Object element) {
		return labelProvider.getText(element);
	}

	@Override
	public Color getBackground(Object element) {
		String elemId = resource.getID((EObject) element);
		if (elemId != null && conflictObject2status.containsKey(elemId)) {
			switch (conflictObject2status.get(elemId)) {
			case ACCEPTED:
				return Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
			case DISCARDED:
				return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			case UNRESOLVED:
				return Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
			}
		}
		return colorProvider == null ? null : colorProvider.getBackground(element);
	}
}
