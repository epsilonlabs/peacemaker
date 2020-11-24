package org.eclipse.epsilon.peacemaker.dt;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict.ConflictObjectStatus;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ResultLabelProvider extends VersionLabelProvider {

	public ResultLabelProvider(ILabelProvider labelProvider, XMIResource resource,
			Map<String, ConflictObjectStatus> conflictObject2status) {
		
		super(labelProvider, resource, conflictObject2status);
	}

	@Override
	public Color getBackground(Object element) {
		String elemId = resource.getID((EObject) element);
		if (elemId != null && conflictObject2status.containsKey(elemId)) {
			switch (conflictObject2status.get(elemId)) {
			// it does not matter which side accepts it, in the end it's resolved
			case ACCEPTED:
			case DISCARDED:
				return Display.getCurrent().getSystemColor(SWT.COLOR_GREEN);
			case UNRESOLVED:
				return Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
			}
		}
		return colorProvider == null ? null : colorProvider.getBackground(element);
	}
}
