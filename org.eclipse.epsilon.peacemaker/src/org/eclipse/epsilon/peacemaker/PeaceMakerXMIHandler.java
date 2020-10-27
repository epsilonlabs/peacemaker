package org.eclipse.epsilon.peacemaker;

import static org.eclipse.epsilon.peacemaker.PeaceMakerXMILoad.LEFT_TAG;
import static org.eclipse.epsilon.peacemaker.PeaceMakerXMILoad.RIGHT_TAG;
import static org.eclipse.epsilon.peacemaker.PeaceMakerXMILoad.SEPARATOR_TAG;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXXMIHandler;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.peacemaker.conflicts.AttributeRedefinitions;
import org.eclipse.epsilon.peacemaker.conflicts.Conflict;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
import org.eclipse.epsilon.peacemaker.conflicts.ObjectRedefinition;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PeaceMakerXMIHandler extends SAXXMIHandler {

	enum FileSegment {
		COMMON, LEFT_CONFLICT, RIGHT_CONFLICT
	}
	protected FileSegment currentSegment = FileSegment.COMMON;

	protected List<ConflictSection> conflictSections = new ArrayList<>();
	protected ConflictSection currentSection;

	protected Map<EObject, AttributeRedefinitions> attributeRedefinitions = new HashMap<>();

	public PeaceMakerXMIHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
		super(xmiResource, helper, options);

		((PeaceMakerXMIHelper) helper).setHandler(this);
	}

	public static void main(String[] args) throws Exception {

		ResourceSet resourceSet = new ResourceSetImpl();

		ResourceSet ecoreResourceSet = new ResourceSetImpl();
		ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new XMIResourceFactoryImpl());
		Resource ecoreResource = ecoreResourceSet.createResource(
				URI.createFileURI(new File("models/comicshop.ecore").getAbsolutePath()));
		ecoreResource.load(null);
		for (EObject o : ecoreResource.getContents()) {
			EPackage ePackage = (EPackage) o;
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		}

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"model", new PeaceMakerXMIResourceFactory());

		String[] cases = {
				"01-newInLeft",
				"02-newInBoth-noConflict",
				"03-attribute",
				"04-nonContained1boundedRef",
				"05-contained1boundedRef",
				"06-newLines-newInBoth-noConflict",
				"07-newLines-attributes",
				"08-newLines-severalAttributes" };

		// uncomment for specific cases
		//		cases = new String[1];
		//		cases[0] = "07-newLines-attributes";

		for (String inputCase : cases) {
			System.out.println("############################################");
			System.out.println(inputCase);
			System.out.println("############################################");

			Resource resource = resourceSet.createResource(
					URI.createFileURI(new File("modelconflicts/" + inputCase + ".model").getAbsolutePath()));
			resource.load(null);
			System.out.println();
		}
		System.out.println("Done");
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {

		switch (name) {
		case LEFT_TAG:
			currentSegment = FileSegment.LEFT_CONFLICT;
			currentSection = new ConflictSection();
			conflictSections.add(currentSection);
			break;
		case SEPARATOR_TAG:
			currentSegment = FileSegment.RIGHT_CONFLICT;
			break;
		case RIGHT_TAG:
			currentSegment = FileSegment.COMMON;
			currentSection = null;
			break;
		default:
			super.startElement(uri, localName, name, attributes);

			// here the new element is in the stack
			EObject newObject = objects.peek();

			switch (currentSegment) {
			case LEFT_CONFLICT:
				currentSection.addLeft(newObject);
				break;
			case RIGHT_CONFLICT:
				currentSection.addRight(newObject);
				EObject leftObject = getLeftObject(newObject);
				if (leftObject != null) {
					// conflict: same element started in left and right
					currentSection.addConflict(new ObjectRedefinition(leftObject,
							xmlResource.getID(leftObject), newObject));
				}
				break;
			case COMMON:
			}
		}
	}

	protected EObject getLeftObject(EObject newObject) {
		String newObjectId = xmlResource.getID(newObject);

		for (EObject left : currentSection.getLeftObjects()) {
			if (newObjectId.equals(xmlResource.getID(left))) {
				return left;
			}
		}
		return null;
	}

	@Override
	public void endElement(String uri, String localName, String name) {
		// here the element is still in the stack
		switch (name) {
		case LEFT_TAG:
		case SEPARATOR_TAG:
		case RIGHT_TAG:
			break;
		default:
			super.endElement(uri, localName, name);
		}
	}

	@Override
	protected void handleObjectAttribs(EObject obj) {
		if (attribs != null) {
			InternalEObject internalEObject = (InternalEObject) obj;
			for (int i = 0, size = attribs.getLength(); i < size; ++i) {
				String name = attribs.getQName(i);

				if (name.startsWith(LEFT_TAG)) {
					name = name.substring(LEFT_TAG.length());
					AttributeRedefinitions attrRedef = getAttributeRedefinitions(obj);
					attrRedef.addLeft(name, attribs.getValue(i));
				}
				else if (name.startsWith(RIGHT_TAG)) {
					name = name.substring(RIGHT_TAG.length());
					AttributeRedefinitions attrRedef = getAttributeRedefinitions(obj);
					attrRedef.addRight(name, attribs.getValue(i));
				}

				if (name.equals(ID_ATTRIB)) {
					xmlResource.setID(internalEObject, attribs.getValue(i));
				}
				else if (name.equals(hrefAttribute) && (!recordUnknownFeature || types.peek() != UNKNOWN_FEATURE_TYPE || obj.eClass() != anyType)) {
					handleProxy(internalEObject, attribs.getValue(i));
				}
				else if (isNamespaceAware) {
					String namespace = attribs.getURI(i);
					if (!ExtendedMetaData.XSI_URI.equals(namespace) && !notFeatures.contains(name)) {
						setAttribValue(obj, name, attribs.getValue(i));
					}
				}
				else if (!name.startsWith(XMLResource.XML_NS) && !notFeatures.contains(name)) {
					setAttribValue(obj, name, attribs.getValue(i));
				}
			}
		}
	}

	@Override
	public void endDocument() {
		super.endDocument();

		for (ConflictSection s : conflictSections) {
			for (Conflict c : s.getConflicts()) {
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@");
				System.out.println(c);
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@");
			}
		}

		for (AttributeRedefinitions redefs : attributeRedefinitions.values()) {
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@");
			System.out.println(redefs);
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@");
		}
	}

	protected AttributeRedefinitions getAttributeRedefinitions(EObject obj) {
		AttributeRedefinitions result = attributeRedefinitions.get(obj);
		if (result == null) {
			result = new AttributeRedefinitions(obj, xmlResource.getID(obj));
			attributeRedefinitions.put(obj, result);
		}
		return result;
	}

	public String toString(EObject obj) {
		return String.format("%s %s", obj.eClass().getName(), xmlResource.getID(obj));
	}

	public void addConflict(Conflict conflict) {
		currentSection.addConflict(conflict);
	}

}
