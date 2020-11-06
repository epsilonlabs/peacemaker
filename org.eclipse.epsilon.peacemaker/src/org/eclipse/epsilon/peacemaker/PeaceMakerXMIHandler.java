package org.eclipse.epsilon.peacemaker;

import static org.eclipse.epsilon.peacemaker.PeaceMakerXMILoad.LEFT_TAG;
import static org.eclipse.epsilon.peacemaker.PeaceMakerXMILoad.RIGHT_TAG;

import java.io.File;
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
import org.eclipse.epsilon.peacemaker.conflicts.ReferenceRedefinition;
import org.xml.sax.Attributes;

public class PeaceMakerXMIHandler extends SAXXMIHandler {

	enum FileSegment {
		COMMON, LEFT_CONFLICT, RIGHT_CONFLICT
	}
	protected FileSegment currentSegment = FileSegment.COMMON;

	protected PeaceMakerXMIResource pmResource;

	protected ConflictSection currentSection;

	public PeaceMakerXMIHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
		super(xmiResource, helper, options);

		pmResource = (PeaceMakerXMIResource) xmiResource;

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
				"08-newLines-severalAttributes",
				"10-newLines-contained1boundedRef" };

		// uncomment for specific cases
		//		cases = new String[1];
		//		cases[0] = "10-newLines-contained1boundedRef";

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
	protected void handleObjectAttribs(EObject obj) {
		if (attribs != null) {
			InternalEObject internalEObject = (InternalEObject) obj;

			AttributeRedefinitions attrRedef = null;

			String objId = findEObjectId(attribs);
			// if objId == null:
			//   we either have an xmi:id attribute that is being redefined,
			//   or we have an EObject without an id (so we must terminate
			//   the process). We will know this at the end of the next loop

			for (int i = 0, size = attribs.getLength(); i < size; ++i) {
				String name = attribs.getQName(i);

				if (name.startsWith(LEFT_TAG)) {
					name = name.substring(LEFT_TAG.length());

					if (attrRedef == null) {
						attrRedef = new AttributeRedefinitions(objId);
					}
					attrRedef.addLeft(name);

					if (name.equals(ID_ATTRIB)) {
						attrRedef.setLeftId(attribs.getValue(i));
					}
				}
				else if (name.startsWith(RIGHT_TAG)) {
					name = name.substring(RIGHT_TAG.length());

					if (attrRedef == null) {
						attrRedef = new AttributeRedefinitions(objId);
					}
					attrRedef.addRight(name);

					if (name.equals(ID_ATTRIB)) {
						attrRedef.setRightId(attribs.getValue(i));
					}
				}

				if (name.equals(ID_ATTRIB)) {
					xmlResource.setID(internalEObject, attribs.getValue(i));

					switch (currentSegment) {
					case LEFT_CONFLICT:
						currentSection.addLeft(objId);
						break;
					case RIGHT_CONFLICT:
						// if we already registered an EObject with the same id
						if (currentSection.containsLeft(objId)) {
							addConflict(new ObjectRedefinition(objId));
							currentSection.removeLeft(objId);
						}
						else {
							currentSection.addRight(objId);
						}
						break;
					case COMMON:
					}
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

			if (objId == null) {
				if (attrRedef.getLeftId() == null || attrRedef.getLeftId() == null) {
					throw new RuntimeException("EObjects must have xmi:ids to use PeaceMaker");
				}
			}
			if (attrRedef != null) {
				addConflict(attrRedef);
			}
		}
	}

	private String findEObjectId(Attributes attribs) {
		for (int i = 0, size = attribs.getLength(); i < size; ++i) {
			if (attribs.getQName(i).matches(ID_ATTRIB)) {
				return attribs.getValue(i);
			}
		}
		return null;
	}

	public String toString(EObject obj) {
		return String.format("%s %s", obj.eClass().getName(), xmlResource.getID(obj));
	}

	public void addConflict(ObjectRedefinition objRedef) {
		pmResource.addConflict(objRedef);

		objRedef.setLeftObject(pmResource.getLeftResource().getEObject(objRedef.getEObjectId()));
		objRedef.setRightObject(pmResource.getRightResource().getEObject(objRedef.getEObjectId()));
	}

	public void addConflict(AttributeRedefinitions attrRedef) {
		pmResource.addConflict(attrRedef);

		// if the xmi:id attribute has been redefined
		if (attrRedef.getEObjectId() != null) {
			attrRedef.setLeftObject(pmResource.getLeftEObject(attrRedef.getEObjectId()));
			attrRedef.setRightObject(pmResource.getRightEObject(attrRedef.getEObjectId()));
		}
		else {
			attrRedef.setLeftObject(pmResource.getLeftEObject(attrRedef.getLeftId()));
			attrRedef.setRightObject(pmResource.getRightEObject(attrRedef.getRightId()));
		}
	}

	public void addConflict(ReferenceRedefinition redef) {
		pmResource.addConflict(redef);

		redef.setLeftValue((EObject) pmResource.getLeftEObject(redef.getEObjectId())
				.eGet(redef.getReference()));
		redef.setRightValue((EObject) pmResource.getRightEObject(redef.getEObjectId())
				.eGet(redef.getReference()));

		// remove both reference values from the conflict section
		String leftValueId = pmResource.getLeftId(redef.getLeftValue());
		currentSection.removeLeft(leftValueId);

		String rightValueId = pmResource.getRightId(redef.getRightValue());
		currentSection.removeRight(rightValueId);

		if (leftValueId.equals(rightValueId)) {
			// an ObjectRedefinition conflict would have been found before
			//   the actual conflict (i.e. this reference redefinition)
			for (Conflict c : pmResource.getConflicts()) {
				if (c instanceof ObjectRedefinition && c.getEObjectId().equals(leftValueId)) {
					pmResource.getConflicts().remove(c);
					break;
				}
			}
		}
	}

}
