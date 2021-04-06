package org.eclipse.epsilon.peacemaker.conflicts;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;

public class DuplicatedId extends Conflict {

	public static final int FIRST = 0;
	public static final int SECOND = 1;

	public DuplicatedId(String objectId, PeacemakerResource pmResource) {
		super(objectId, pmResource);
	}

	@Override
	public String getTitle() {
		return "Duplicated Id";
	}

	@Override
	public String getDescription() {
		return String.format(
				"The same id %s has been found in two different elements",
				eObjectId);
	}

	@Override
	public boolean supports(ResolveAction action) {
		switch (action) {
		case KEEP_FIRST:
		case KEEP_SECOND:
			return true;
		default:
			return super.supports(action);
		}
	}

	@Override
	public void resolve(ResolveAction action) {

		int removePosition = -1;

		switch (action) {
		case KEEP_FIRST:
			removePosition = SECOND;
			break;
		case KEEP_SECOND:
			removePosition = FIRST;
			break;
		default:
			super.resolve(action);
			return;
		}

		if (pmResource.isSingleLoad()) {
			remove(pmResource.getDuplicatedIds(), removePosition);
		}
		else {
			remove(pmResource.getLeftDuplicatedIds(), removePosition);
			remove(pmResource.getRightDuplicatedIds(), removePosition);
			remove(pmResource.getBaseDuplicatedIds(), removePosition);
		}
	}

	protected void remove(Map<String, List<EObject>> duplicatedIds, int removePosition) {
		List<EObject> dupObjects = duplicatedIds.get(eObjectId);
		if (dupObjects != null) {
			EcoreUtil.remove(dupObjects.get(removePosition));
		}
	}

	@Override
	protected ConflictObjectStatus getStatus(ResolveAction action) {
		switch (action) {
		case KEEP_FIRST:
			return ConflictObjectStatus.ACCEPTED;
		case KEEP_SECOND:
			return ConflictObjectStatus.ACCEPTED;
		default:
			return super.getStatus(action);
		}
	}
}
