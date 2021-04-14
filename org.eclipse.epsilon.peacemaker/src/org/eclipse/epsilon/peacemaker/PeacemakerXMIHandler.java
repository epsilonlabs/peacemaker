package org.eclipse.epsilon.peacemaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.xmi.UnresolvedReferenceException;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXXMIHandler;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;
import org.eclipse.epsilon.peacemaker.conflicts.InternalDanglingReference;
import org.eclipse.epsilon.peacemaker.util.ids.DuplicatedIdsException;
import org.eclipse.epsilon.peacemaker.util.ids.IdUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PeacemakerXMIHandler extends SAXXMIHandler {

	protected int currentLine;

	protected PeacemakerResource pmResource;

	protected ConflictVersionHelper versionHelper;
	protected boolean checkConflicts = false;

	protected Set<String> ids = new HashSet<>();
	protected Map<String, List<EObject>> idObjects = new HashMap<>();
	protected Map<String, List<EObject>> duplicatedIds;

	public PeacemakerXMIHandler(XMLResource xmlResource, XMLHelper helper, Map<?, ?> options,
			PeacemakerResource pmResource, ConflictVersionHelper versionHelper) {

		super(xmlResource, helper, options);

		currentLine = 1;

		this.pmResource = pmResource;

		if (versionHelper != null) {
			this.versionHelper = versionHelper;
			checkConflicts = true;

			switch (versionHelper.getVersionType()) {
			case LEFT:
				duplicatedIds = pmResource.getLeftDuplicatedIds();
				break;
			case BASE:
				duplicatedIds = pmResource.getBaseDuplicatedIds();
				break;
			case RIGHT:
				duplicatedIds = pmResource.getRightDuplicatedIds();
			default:
			}
		}
		else {
			duplicatedIds = pmResource.getDuplicatedIds();
		}
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {

		super.startElement(uri, localName, name, attributes);

		// start and end here mark the first and last lines of the started element's
		//   initial tag (not the whole element)
		int start = currentLine;
		int end = locator.getLineNumber();

		EObject peekObject = objects.peek();
		// if the object's stack head is not null (sometimes null is even pushed
		//   e.g. when parsing multi-valued attributes)
		if (peekObject != null) {
			String objId = IdUtils.getAvailableId(xmlResource, peekObject);

			if (objId != null) {
				if (ids.contains(objId)) {
					duplicatedIds.put(objId, idObjects.get(objId));
					idObjects.get(objId).add(peekObject);
					if (pmResource.failOnDuplicateIds()) {
						throw new DuplicatedIdsException(objId, start, end);
					}
				}
				else {
					ids.add(objId);
					List<EObject> list = new ArrayList<>();
					list.add(peekObject);
					idObjects.put(objId, list);
				}

				if (checkConflicts) {
					versionHelper.addToConflictSections(start, end,
							IdUtils.getAvailableId(xmlResource, objects.peek()));
				}
			}
		}

		currentLine = end + 1;
	}

	@Override
	public void endElement(String uri, String localName, String name) {
		super.endElement(uri, localName, name);

		currentLine = locator.getLineNumber() + 1;
	}

	@Override
	public void endDocument() {
		super.endDocument();

		Iterator<Diagnostic> errorsIterator = xmlResource.getErrors().iterator();
		while (errorsIterator.hasNext()) {
			Diagnostic error = errorsIterator.next();
			if (error instanceof UnresolvedReferenceException) {
				UnresolvedReferenceException unresolvedReference =
						(UnresolvedReferenceException) error;

				InternalDanglingReference danglingRef = new InternalDanglingReference (
						IdUtils.getAvailableId(xmlResource, unresolvedReference.getObject()),
						unresolvedReference.getObject(),
						pmResource,
						(EReference) unresolvedReference.getFeature(),
						unresolvedReference.getReference());
				pmResource.addConflict(danglingRef);

				errorsIterator.remove();
			}
		}
	}
}
