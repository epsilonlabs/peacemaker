package org.eclipse.epsilon.peacemaker.dt;

import static java.time.Instant.EPOCH;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.internal.content.ContentTypeHandler;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.epsilon.peacemaker.util.ids.IdUtils;
import org.eclipse.jgit.attributes.Attributes;
import org.eclipse.jgit.dircache.DirCacheBuildIterator;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeResult;
import org.eclipse.jgit.merge.RecursiveMerger;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.WorkingTreeIterator;

@SuppressWarnings("restriction")
public class DuplicatedIdsMerger extends RecursiveMerger {

	protected List<String> pathsToWrongModels = new ArrayList<String>();

	protected Map<String, CanonicalTreeParser> baseMap = new HashMap<>();
	protected Map<String, CanonicalTreeParser> oursMap = new HashMap<>();
	protected Map<String, CanonicalTreeParser> theirsMap = new HashMap<>();

	public DuplicatedIdsMerger(ObjectInserter inserter, Config config) {
		super(inserter, config);
	}

	public DuplicatedIdsMerger(Repository db, boolean inCore) {
		super(db, inCore);
	}


	@Override
	protected boolean processEntry(CanonicalTreeParser base, CanonicalTreeParser ours, CanonicalTreeParser theirs, DirCacheBuildIterator index,
			WorkingTreeIterator work, boolean ignoreConflicts, Attributes attributes)
			throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException {

		boolean noMergeProblems = super.processEntry(base, ours, theirs, index, work, ignoreConflicts, attributes);

		if (!noMergeProblems) {
			return false;
		}

		final int modeO = tw.getRawMode(T_OURS);
		final int modeT = tw.getRawMode(T_THEIRS);
		final int modeB = tw.getRawMode(T_BASE);
		if (modeO == 0 && modeT == 0 && modeB == 0) {
			// File is either untracked or new, staged but uncommitted
			return true;
		}
		
		// if it is a folder
		if (tw.isSubtree()) {
			return true;
		}

		// if it has been merged with conflicts
		MergeResult<?> result = mergeResults.get(tw.getPathString());
		if (result != null && result.containsConflicts()) {
			return true;
		}

		// else: if it is a model, do the check duplicate ids thing

		// TODO: determine the best way to detect if a file is an xmi model
		// - Use a list of filename extensions: good performance, requires setting preferences
		// - Check if the file is XMI with the contentype descriptor: not as good performance, but maybe it's ok

		String fullPath = nonNullRepo().getWorkTree().getAbsolutePath() + '/' + tw.getPathString();
		File file = new Path(fullPath).toFile();

		InputStream fileContents = new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
		int type = getXMIDescriber().describe(fileContents, null);

		// if it is not a model
		if (type != IContentDescriber.VALID) {
			return true;
		}

		// if the check detects duplicates, store the information required
		//   for marking the model as in conflict later (after super.mergeTrees())
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(URI.createFileURI(fullPath));
		if (resource instanceof XMIResource &&
				IdUtils.hasDuplicatedIds((XMIResource) resource, fileContents)) {

			String modelPath = tw.getPathString();
			pathsToWrongModels.add(modelPath);
			baseMap.put(modelPath, base);
			oursMap.put(modelPath, ours);
			theirsMap.put(modelPath, theirs);
		}

		return true;
	}

	protected IContentDescriber getXMIDescriber() {
		return ((ContentTypeHandler) Platform.getContentTypeManager().getContentType("org.eclipse.emf.ecore.xmi"))
				.getTarget().getDescriber();
	}

	@Override
	protected boolean mergeTrees(AbstractTreeIterator baseTree, RevTree headTree, RevTree mergeTree, boolean ignoreConflicts) throws IOException {

		boolean noConflicts = super.mergeTrees(baseTree, headTree, mergeTree, ignoreConflicts);

		// we check the wrong models set in case there several models and
		//   conflicts were detected in some, but undetected in others
		if (!noConflicts && pathsToWrongModels.isEmpty()) {
			return false;
		}
		// if the merge ended cleanly and no models with duplicated ids were found
		else if (pathsToWrongModels.isEmpty()) {
			return true;
		}

		// update the index to mark as conflicting models with duplicated ids
		dircache = nonNullRepo().lockDirCache();
		builder = dircache.builder();

		for (int index = 0; index < dircache.getEntryCount(); ++index) {
			DirCacheEntry entry = dircache.getEntry(index);
			String modelPath = entry.getPathString();
			if (pathsToWrongModels.contains(modelPath)) {
				// create a conflict (see if this works)
				add(entry.getRawPath(), baseMap.get(modelPath), DirCacheEntry.STAGE_1, EPOCH, 0);
				add(entry.getRawPath(), oursMap.get(modelPath), DirCacheEntry.STAGE_2, EPOCH, 0);
				add(entry.getRawPath(), theirsMap.get(modelPath), DirCacheEntry.STAGE_3, EPOCH, 0);

				unmergedPaths.add(modelPath);
				mergeResults.put(modelPath, new MergeResult<>(Collections.emptyList()));
			}
			else {
				builder.add(entry); // leave it be
			}
		}
		builder.commit();

		return false; // because we have found duplicated ids in a model
	}

	protected DirCacheEntry add(byte[] path, CanonicalTreeParser p, int stage,
			Instant lastMod, long len) {
		if (p != null && !p.getEntryFileMode().equals(FileMode.TREE)) {
			DirCacheEntry e = new DirCacheEntry(path, stage);
			e.setFileMode(p.getEntryFileMode());
			e.setObjectId(p.getEntryObjectId());
			e.setLastModified(lastMod);
			e.setLength(len);
			builder.add(e);
			return e;
		}
		return null;
	}

	protected static boolean nonTree(int mode) {
		return mode != 0 && !FileMode.TREE.equals(mode);
	}
}
