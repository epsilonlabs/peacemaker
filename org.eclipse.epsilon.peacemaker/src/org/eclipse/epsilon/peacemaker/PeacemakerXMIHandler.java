package org.eclipse.epsilon.peacemaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.xmi.UnresolvedReferenceException;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXXMIHandler;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;
import org.eclipse.epsilon.peacemaker.conflicts.InternalDanglingReference;
import org.eclipse.epsilon.peacemaker.conflicts.MultiValuedAttributeDuplicates;
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

	protected Map<EObject, Set<EAttribute>> multiValuedAttributesToCheck = new HashMap<>();

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

		EObject peekObject = objects.peekEObject();
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
							IdUtils.getAvailableId(xmlResource, peekObject),
							peekObject);
				}
			}
		}
		else {
			Object feature = types.peek();
			if (isMultiValuedAttribute(feature)) {
				addMultiValuedAttributeToCheck(getHiddenPeekObject(), (EAttribute) feature);
			}
		}

		currentLine = end + 1;
	}

	protected void addMultiValuedAttributeToCheck(EObject peekObject, EAttribute mvAttr) {
		Set<EAttribute> mvAttributes = multiValuedAttributesToCheck.get(peekObject);
		if (mvAttributes == null) {
			mvAttributes = new HashSet<>();
			multiValuedAttributesToCheck.put(peekObject, mvAttributes);
		}
		mvAttributes.add(mvAttr);
	}

	protected EObject getHiddenPeekObject() {
		EObject peekObject = objects.peekEObject();
		if (peekObject == null && objects.size() > 1) {
			objects.popEObject(); // remove the "null" at the top
			peekObject = objects.peekEObject();
			objects.push(null); // put it back
		}
		return peekObject;
	}

	protected boolean isMultiValuedAttribute(Object feature) {
		return feature instanceof EAttribute &&
				helper.getFeatureKind((EAttribute) feature) == XMLHelper.DATATYPE_IS_MANY;
	}


	@Override
	public void endElement(String uri, String localName, String name) {

		EObject peekObject = objects.peekEObject();
		if (multiValuedAttributesToCheck.containsKey(peekObject)) {
			for (EAttribute mvAttr : multiValuedAttributesToCheck.get(peekObject)) {
				if (hasDuplicatedValues(peekObject, mvAttr)) {
					pmResource.addConflict(new MultiValuedAttributeDuplicates(
							IdUtils.getAvailableId(xmlResource, peekObject),
							pmResource, mvAttr));
				}
			}
			multiValuedAttributesToCheck.remove(peekObject);
		}

		currentLine = locator.getLineNumber() + 1;

		super.endElement(uri, localName, name);
	}

	protected boolean hasDuplicatedValues(EObject peekObject, EAttribute mvAttr) {
		@SuppressWarnings("unchecked")
		List<Object> values = (List<Object>) peekObject.eGet(mvAttr);

		Set<Object> uniqueValues = new HashSet<>();
		for (Object value : values) {
			if (!uniqueValues.add(value)) {
				return true;
			}
		}

		return false;
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
