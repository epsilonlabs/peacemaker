package org.eclipse.epsilon.peacemaker;

import java.util.Map;

import org.eclipse.emf.ecore.xmi.XMLDefaultHandler;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
import org.eclipse.epsilon.peacemaker.ConflictsPreprocessor.ConflictVersionHelper;

public class PeacemakerXMLParserPoolImpl extends XMLParserPoolImpl {

	protected PeaceMakerXMIResource pmResource;
	protected ConflictVersionHelper versionHelper;

	public PeacemakerXMLParserPoolImpl(PeaceMakerXMIResource pmResource, ConflictVersionHelper versionHelper) {
		this.pmResource = pmResource;
		this.versionHelper = versionHelper;
	}

	@Override
	public synchronized XMLDefaultHandler getDefaultHandler(XMLResource resource, XMLLoad xmlLoad, XMLHelper helper, Map<?, ?> options) {
		return new PeacemakerHandlerDecorator(super.getDefaultHandler(resource, xmlLoad, helper, options), pmResource, versionHelper);
	}
}
