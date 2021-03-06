package org.eclipse.epsilon.peacemaker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMISaveImpl;
import org.eclipse.epsilon.peacemaker.util.StreamUtils;


public class PeacemakerXMISave extends XMISaveImpl {

	public PeacemakerXMISave(XMLHelper helper) {
		super(helper);
	}

	@Override
	public void save(XMLResource resource, OutputStream outputStream, Map<?, ?> options) throws IOException {
		PeacemakerResource pmResource = (PeacemakerResource) resource;

		if (pmResource.isSingleLoad()) {
			pmResource.getSingleLoadResource().save(outputStream, options);
		}
		else {
			save(pmResource, outputStream, options);
		}
	}

	public void save(PeacemakerResource pmResource, OutputStream outputStream,
			Map<?, ?> options) throws IOException {

		ByteArrayOutputStream leftStream = new ByteArrayOutputStream();
		pmResource.getLeftResource().save(leftStream, options);

		ByteArrayOutputStream rightStream = new ByteArrayOutputStream();
		pmResource.getRightResource().save(rightStream, options);

		if (pmResource.getBaseResource() != null) {
			ByteArrayOutputStream baseStream = new ByteArrayOutputStream();
			pmResource.getBaseResource().save(baseStream, options);

			StreamUtils.merge(
					leftStream.toString(), baseStream.toString(), rightStream.toString(),
					outputStream,
					pmResource.getLeftVersionName(),
					pmResource.getBaseVersionName(),
					pmResource.getRightVersionName());
		}
		else {
			StreamUtils.merge(leftStream.toString(), rightStream.toString(), outputStream,
					pmResource.getLeftVersionName(),
					pmResource.getRightVersionName());
		}
	}
}
