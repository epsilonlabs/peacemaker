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

		if (!pmResource.hasConflicts()) {
			super.save(pmResource, outputStream, options);
		}
		else {
			doSave(pmResource, outputStream, options);
		}
	}

	public void doSave(PeaceMakerXMIResource pmResource, OutputStream outputStream,
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
