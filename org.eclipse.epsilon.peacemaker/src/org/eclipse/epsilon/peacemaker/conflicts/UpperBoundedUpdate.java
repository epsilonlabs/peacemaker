package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;
import org.eclipse.epsilon.peacemaker.XMIResetIdsHandler;

/**
 * A special case of a double update conflict where the changes happen in an
 * upper bounded EStructural feature
 * 
 * @author alfonsodelavega
 */
public class UpperBoundedUpdate extends DoubleUpdate implements XMIResetIdsHandler {

	protected EStructuralFeature feature;

	/**
	 * Create a conflict around an upper bounded feature
	 *
	 * @param parentId The parent that contains the reference feature
	 * @param feature
	 */
	public UpperBoundedUpdate(String parentId, PeacemakerResource pmResource,
			EStructuralFeature feature) {
		
		super(parentId, pmResource);
		this.feature = feature;
	}

	@Override
	public void resolve(ResolveAction action) {

		// TODO: implement adequate resolve actions for mvattrs and containment references
		switch (action) {
		case KEEP_LEFT:
		case KEEP_RIGHT:
		case NO_ACTION:
			break;
		default:
			throw new UnsupportedOperationException(
					"Unsupported resolve action for this conflict: " + action);
		}
	}

	@Override
	public EObject getLeftVersionObject() {
		return leftObject;
	}

	@Override
	public EObject getRightVersionObject() {
		return rightObject;
	}

	public void resetXMIIds() {
		// TODO: adapt for undoing changes from the editor for containment references
	}

	@Override
	public String getTitle() {
		return "UpperBoundedUpdate";
	}

	@Override
	public String getDescription() {
		String containerClassName = ((EClass) feature.eContainer()).getName();
		return String.format(
				"The upper bounded feature \"%s\" from type %s"
						+ " was updated in left and right in a non-compatible way"
						+ " for an object with id %s ",
				feature.getName(), containerClassName, eObjectId);
	}
}
