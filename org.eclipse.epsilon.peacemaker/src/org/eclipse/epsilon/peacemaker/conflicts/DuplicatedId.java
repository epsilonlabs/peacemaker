package org.eclipse.epsilon.peacemaker.conflicts;

import org.eclipse.epsilon.peacemaker.PeacemakerResource;

public class DuplicatedId extends Conflict {

	public DuplicatedId(String objectId, PeacemakerResource pmResource) {
		super(objectId, pmResource);
	}

}
