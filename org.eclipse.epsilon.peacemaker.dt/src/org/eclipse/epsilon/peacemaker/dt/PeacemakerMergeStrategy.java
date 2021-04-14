package org.eclipse.epsilon.peacemaker.dt;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.StrategyRecursive;
import org.eclipse.jgit.merge.ThreeWayMerger;

/**
 * Marks model resources as conflicting if Peacemaker found conflicts missed by Git
 *
 * @author alfonsodelavega
 */
public class PeacemakerMergeStrategy extends StrategyRecursive {

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(Repository db) {
		return new PeacemakerConflictsMerger(db, false);
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(Repository db, boolean inCore) {
		return new PeacemakerConflictsMerger(db, inCore);
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(ObjectInserter inserter, Config config) {
		return new PeacemakerConflictsMerger(inserter, config);
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "detectMissedConflicts";
	}
}
