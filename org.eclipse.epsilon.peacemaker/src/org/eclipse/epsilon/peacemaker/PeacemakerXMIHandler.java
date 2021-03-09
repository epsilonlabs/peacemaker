package org.eclipse.epsilon.peacemaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXXMIHandler;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.LineType;
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
	protected Map<String, List<EObject>> duplicatedIds = new HashMap<>();

	public PeacemakerXMIHandler(XMLResource xmlResource, XMLHelper helper, Map<?, ?> options,
			PeacemakerResource pmResource, ConflictVersionHelper versionHelper) {

		super(xmlResource, helper, options);

		this.pmResource = pmResource;
		currentLine = 1; //TODO: start in line 2 to avoid xml tag? is it relevant?
		if (versionHelper != null) {
			this.versionHelper = versionHelper;
			checkConflicts = true;
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

				if (checkConflicts && objId != null) {
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

		if (!duplicatedIds.isEmpty()) {
			if (versionHelper != null) {
				if (versionHelper.getVersionType() == LineType.LEFT) {
					pmResource.setLeftDuplicatedIds(duplicatedIds);
				}
				else {
					pmResource.setRightDuplicatedIds(duplicatedIds);
				}
			}
			else {
				pmResource.setDuplicatedIds(duplicatedIds);
			}
		}
	}
}
