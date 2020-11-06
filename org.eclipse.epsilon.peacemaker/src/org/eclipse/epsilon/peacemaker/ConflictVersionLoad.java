package org.eclipse.epsilon.peacemaker;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.impl.XMILoadImpl;
import org.xml.sax.helpers.DefaultHandler;

public class ConflictVersionLoad extends XMILoadImpl {

	public ConflictVersionLoad(XMLHelper helper) {
		super(helper);
	}

	@Override
	protected DefaultHandler makeDefaultHandler() {
		return new ConflictVersionHandler(resource, helper, options);
	}

}
