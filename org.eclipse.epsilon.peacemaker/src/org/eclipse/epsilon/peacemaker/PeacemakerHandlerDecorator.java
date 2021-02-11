package org.eclipse.epsilon.peacemaker;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLDefaultHandler;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class PeacemakerHandlerDecorator extends DefaultHandler implements XMLDefaultHandler {

	public static final String ID_ATTRIB = XMIResource.XMI_NS + ":" + XMIResource.XMI_ID;
	public static final String TYPE_ATTRIB = "xsi:type";
	public static final String XMI_TYPE_ATTRIB = "xmi:type";

	protected int currentLine;
	protected Stack<EClass> types = new Stack<>();

	protected PeaceMakerXMIResource pmResource;
	protected XMIHelperImpl xmiHelper;
	protected XMLDefaultHandler target;
	protected ConflictVersionHelper versionHelper;

	protected Locator locator;

	public PeacemakerHandlerDecorator(XMLDefaultHandler target, PeaceMakerXMIResource pmResource, ConflictVersionHelper versionHelper) {
		this.pmResource = pmResource;
		xmiHelper = new XMIHelperImpl(pmResource);

		this.target = target;
		this.versionHelper = versionHelper;
		currentLine = 1;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		target.startElement(uri, localName, qName, atts);

		// start and end here mark the first and last lines of the started element's
		//   initial tag (not the whole element)
		int start = currentLine;
		int end = locator.getLineNumber();

		// this is as far as the handler's decorator gets us, so now we need to
		//   keep track of the current EClass type of the element being parsed,
		//   so that we can check whether there is an Ecore identifier that we
		//   may use (i.e. when there are no xmi:ids in the model)

		EClass elementType = null;
		if (types.isEmpty()) {
			// top object, localName is the EClass name

			String prefix = "";
			int index = qName.indexOf(':', 0);
			if (index != -1) {
				prefix = qName.substring(0, index);
			}

			// if a package is accessible through extended metadata, this is not
			//   a valid enough solution
			String packageUri = xmiHelper.getURI(prefix);
			EPackage epackage = pmResource.getResourceSet().getPackageRegistry().getEPackage(packageUri);
			elementType = (EClass) epackage.getEClassifier(localName);
		}
		else {
			elementType = getType(types.peek(), localName, atts);
		}

		if (elementType == null) {
			throw new RuntimeException("" + start + ": element type not found");
		}
		types.push(elementType);

		if (versionHelper.inConflictSection(start, end)) {
			String objectId = getObjectId(elementType, atts);
			if (objectId != null) {
				versionHelper.addToConflictSections(start, end, objectId);
			}
			else {
				throw new RuntimeException("" + start + ": Model object in conflict section does not have a valid Id");
			}

		}

		currentLine = end + 1;
	}

	protected String getObjectId(EClass type, Attributes atts) {
		// find xmi id
		String objectId = atts.getValue(ID_ATTRIB);

		if (objectId == null) {
			// find Ecore attribute id (if applicable)
			// TODO: cache eclasses and id attributes?
			EAttribute idAttribute = type.getEIDAttribute();
			if (idAttribute != null) {
				objectId = atts.getValue(idAttribute.getName());
			}
		}

		return objectId;
	}

	protected EClass getType(EClass parentType, String refName, Attributes atts) {

		EClass type = null;

		String typeName = atts.getValue(TYPE_ATTRIB);

		if (typeName == null) {
			typeName = atts.getValue(XMI_TYPE_ATTRIB);
		}

		if (typeName != null) {

			String prefix = null;
			int index = typeName.indexOf(':', 0);
			if (index != -1) {
				prefix = typeName.substring(0, index);
				typeName = typeName.substring(index + 1);
			}
			
			if (prefix != null) {
				String packageUri = xmiHelper.getURI(prefix);
				EPackage epackage =
						pmResource.getResourceSet().getPackageRegistry().getEPackage(packageUri);
				type = (EClass) epackage.getEClassifier(typeName);
			}
			else {
				type = (EClass) parentType.getEPackage().getEClassifier(typeName);
			}
		}
		else {
			EReference ref = (EReference) parentType.getEStructuralFeature(refName);
			if (ref != null) {
				type = ref.getEReferenceType();
			}
		}

		return type;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		target.endElement(uri, localName, qName);

		types.pop();

		currentLine = locator.getLineNumber() + 1;
	}

	public XMLDefaultHandler getTarget() {
		return target;
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
		target.setDocumentLocator(locator);
	}

	@Override
	public void startDocument() throws SAXException {
		target.startDocument();
		xmiHelper.pushContext();
	}

	@Override
	public void endDocument() throws SAXException {
		target.endDocument();
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		target.startPrefixMapping(prefix, uri);

		xmiHelper.addPrefix(prefix, uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		target.endPrefixMapping(prefix);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		target.characters(ch, start, length);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		target.ignorableWhitespace(ch, start, length);
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		this.target.processingInstruction(target, data);
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		target.skippedEntity(name);

	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		return target.resolveEntity(publicId, systemId);
	}

	@Override
	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
		target.notationDecl(name, publicId, systemId);
	}

	@Override
	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
		target.unparsedEntityDecl(name, publicId, systemId, notationName);
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		target.warning(exception);
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		target.error(exception);
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		target.fatalError(exception);
	}

	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {
		target.startDTD(name, publicId, systemId);
	}

	@Override
	public void endDTD() throws SAXException {
		target.endDTD();
	}

	@Override
	public void startEntity(String name) throws SAXException {
		target.startEntity(name);
	}

	@Override
	public void endEntity(String name) throws SAXException {
		target.endEntity(name);
	}

	@Override
	public void startCDATA() throws SAXException {
		target.startCDATA();
	}

	@Override
	public void endCDATA() throws SAXException {
		target.endCDATA();
	}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		target.comment(ch, start, length);
	}

	@Override
	public void reset() {
		target.reset();
	}

	@Override
	public void prepare(XMLResource resource, XMLHelper helper, Map<?, ?> options) {
		target.prepare(resource, helper, options);
	}
}
