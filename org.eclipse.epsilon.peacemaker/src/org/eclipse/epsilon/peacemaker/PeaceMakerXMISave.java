package org.eclipse.epsilon.peacemaker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMISaveImpl;
import org.eclipse.epsilon.peacemaker.util.StreamUtils;


public class PeaceMakerXMISave extends XMISaveImpl {

	public PeaceMakerXMISave(XMLHelper helper) {
		super(helper);
	}

	@Override
	public void save(XMLResource resource, OutputStream outputStream, Map<?, ?> options) throws IOException {
		PeaceMakerXMIResource pmResource = (PeaceMakerXMIResource) resource;

		ByteArrayOutputStream leftStream = new ByteArrayOutputStream();
		pmResource.getLeftResource().save(leftStream, options);

		ByteArrayOutputStream rightStream = new ByteArrayOutputStream();
		pmResource.getRightResource().save(rightStream, options);

		StreamUtils.merge(leftStream.toString(), rightStream.toString(), outputStream,
				pmResource.getLeftResource().getVersionName(),
				pmResource.getRightResource().getVersionName());
	}
}
