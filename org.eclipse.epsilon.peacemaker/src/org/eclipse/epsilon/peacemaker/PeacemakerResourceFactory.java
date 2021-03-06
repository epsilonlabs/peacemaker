package org.eclipse.epsilon.peacemaker;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class PeacemakerResourceFactory extends XMIResourceFactoryImpl {

	@Override
	public Resource createResource(URI uri) {
		return new PeacemakerResource(uri);
	}
}
