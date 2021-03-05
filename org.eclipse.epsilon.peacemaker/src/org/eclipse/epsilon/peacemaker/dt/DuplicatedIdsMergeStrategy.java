package org.eclipse.epsilon.peacemaker.dt;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.StrategyRecursive;
import org.eclipse.jgit.merge.ThreeWayMerger;

/**
 * Marks model resources as conflicting if they hold duplicated ids
 *
 * @author alfonsodelavega
 */
public class DuplicatedIdsMergeStrategy extends StrategyRecursive {

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(Repository db) {
		return new DuplicatedIdsMerger(db, false);
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(Repository db, boolean inCore) {
		return new DuplicatedIdsMerger(db, inCore);
	}

	/** {@inheritDoc} */
	@Override
	public ThreeWayMerger newMerger(ObjectInserter inserter, Config config) {
		return new DuplicatedIdsMerger(inserter, config);
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return "detectDuplicateIds";
	}
}
