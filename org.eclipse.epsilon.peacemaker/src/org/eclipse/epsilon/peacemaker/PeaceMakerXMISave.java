package org.eclipse.epsilon.peacemaker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
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

		Map<?, ?> defaultSaveOptions = getDefaultSaveOptions();

		ByteArrayOutputStream leftStream = new ByteArrayOutputStream();
		pmResource.getLeftResource().save(leftStream, defaultSaveOptions);

		ByteArrayOutputStream rightStream = new ByteArrayOutputStream();
		pmResource.getRightResource().save(rightStream, defaultSaveOptions);

		StreamUtils.merge(leftStream.toString(), rightStream.toString(), outputStream);
	}

	protected Map<?, ?> getDefaultSaveOptions() {
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_LINE_WIDTH, 1);
		return options;
	}
}
