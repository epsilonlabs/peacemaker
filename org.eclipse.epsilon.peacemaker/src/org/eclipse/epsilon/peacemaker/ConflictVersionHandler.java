package org.eclipse.epsilon.peacemaker;

import java.util.Map;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXXMIHandler;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ConflictVersionHandler extends SAXXMIHandler {

	protected int currentLine;
	protected ConflictVersionResource versionResource;
	protected ConflictVersionHelper versionHelper;

	public ConflictVersionHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
		super(xmiResource, helper, options);

		currentLine = 1; //TODO: start in line 2 to avoid xml tag? is it relevant?
		versionResource = (ConflictVersionResource) xmiResource;
		versionHelper = versionResource.getVersionHelper();
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, name, attributes);
		
		// start and end here mark the first and last lines of the started element's
		//   initial tag (not the whole element)
		int start = currentLine;
		int end = locator.getLineNumber();

		versionHelper.addToConflictSections(start, end, versionResource.getAvailableId(objects.peek()));

		currentLine = end + 1;
	}

	@Override
	public void endElement(String uri, String localName, String name) {
		super.endElement(uri, localName, name);

		currentLine = locator.getLineNumber() + 1;
	}
}
