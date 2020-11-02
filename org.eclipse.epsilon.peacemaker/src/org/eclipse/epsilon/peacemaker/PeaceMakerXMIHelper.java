package org.eclipse.epsilon.peacemaker;

import java.util.StringTokenizer;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl;
import org.eclipse.emf.ecore.xml.type.SimpleAnyType;
import org.eclipse.epsilon.peacemaker.conflicts.ReferenceRedefinition;

public class PeaceMakerXMIHelper extends XMIHelperImpl {

	protected PeaceMakerXMIHandler handler;

	public PeaceMakerXMIHelper() {
		super();
	}

	public PeaceMakerXMIHelper(XMLResource resource) {
		super(resource);
	}

	public void setHandler(PeaceMakerXMIHandler handler) {
		this.handler = handler;
	}

	@Override
	public void setValue(EObject object, EStructuralFeature feature, Object value, int position) {
		// PeaceMaker: the only changes are in the "default" block at the end
		if (extendedMetaData != null) {
			EStructuralFeature targetFeature = extendedMetaData.getAffiliation(object.eClass(), feature);
			if (targetFeature != null && targetFeature != feature) {
				EStructuralFeature group = extendedMetaData.getGroup(targetFeature);
				if (group != null) {
					targetFeature = group;
				}
				if (targetFeature.getEType() == EcorePackage.Literals.EFEATURE_MAP_ENTRY) {
					FeatureMap featureMap = (FeatureMap) object.eGet(targetFeature);
					EClassifier eClassifier = feature.getEType();
					if (eClassifier instanceof EDataType) {
						EDataType eDataType = (EDataType) eClassifier;
						EFactory eFactory = eDataType.getEPackage().getEFactoryInstance();
						value = createFromString(eFactory, eDataType, (String) value);
					}
					featureMap.add(feature, value);
					return;
				}
				else {
					// If we are substituting an EAttribute for an EReference...
					//
					EClassifier eType = feature.getEType();
					if (eType instanceof EDataType && targetFeature instanceof EReference) {
						// Create an simple any type wrapper for the attribute value and use that with the EReference.
						//
						SimpleAnyType simpleAnyType = (SimpleAnyType) EcoreUtil.create(anySimpleType);
						simpleAnyType.setInstanceType((EDataType) eType);
						simpleAnyType.setRawValue((String) value);
						value = simpleAnyType;
					}
					feature = targetFeature;
				}
			}
		}

		int kind = getFeatureKind(feature);
		switch (kind) {
		case DATATYPE_SINGLE:
		case DATATYPE_IS_MANY: {
			EClassifier eClassifier = feature.getEType();
			EDataType eDataType = (EDataType) eClassifier;
			EFactory eFactory = eDataType.getEPackage().getEFactoryInstance();

			if (kind == DATATYPE_IS_MANY) {
				@SuppressWarnings("unchecked")
				InternalEList<Object> list = (InternalEList<Object>) object.eGet(feature);
				if (position == -2) {
					for (StringTokenizer stringTokenizer = new StringTokenizer((String) value, " "); stringTokenizer.hasMoreTokens();) {
						String token = stringTokenizer.nextToken();
						list.addUnique(createFromString(eFactory, eDataType, token));
					}

					// Make sure that the list will appear to be set to be empty.
					//
					if (list.isEmpty()) {
						list.clear();
					}
				}
				else if (value == null) {
					list.addUnique(null);
				}
				else {
					list.addUnique(createFromString(eFactory, eDataType, (String) value));
				}
			}
			else if (value == null) {
				object.eSet(feature, null);
			}
			else {
				object.eSet(feature, createFromString(eFactory, eDataType, (String) value));
			}
			break;
		}
		case IS_MANY_ADD:
		case IS_MANY_MOVE: {
			@SuppressWarnings("unchecked")
			InternalEList<Object> list = (InternalEList<Object>) object.eGet(feature);

			if (position == -1) {
				if (object == value) {
					list.add(value);
				}
				else {
					list.addUnique(value);
				}
			}
			else if (position == -2) {
				list.clear();
			}
			else if (checkForDuplicates || object == value) {
				int index = list.basicIndexOf(value);
				if (index == -1) {
					list.addUnique(position, value);
				}
				else {
					list.move(position, index);
				}
			}
			else if (kind == IS_MANY_ADD) {
				list.addUnique(position, value);
			}
			else {
				list.move(position, value);
			}
			break;
		}
		default: {
			// at this point, feature is an EReference with upperBound == 1
			Object previousValue = object.eGet(feature);
			if (previousValue != null) {
				EReference reference = (EReference) feature;
				if (reference.isContainment()) {
					// conflict: overriding a single-value containment reference
					
					//TODO: here, the reference could have contained elements,
					//  but these have not been parsed yet. Think about this
					handler.addConflict(new ReferenceRedefinition(
							resource.getID(object), reference));
				}
				else {
					throw new RuntimeException("this case is unthought of right now");
				}
			}
			object.eSet(feature, value);
			break;
		}
		}
	}
}
