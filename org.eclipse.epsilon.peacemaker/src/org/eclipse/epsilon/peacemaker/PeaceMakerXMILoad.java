package org.eclipse.epsilon.peacemaker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMILoadImpl;
import org.xml.sax.helpers.DefaultHandler;

public class PeaceMakerXMILoad extends XMILoadImpl {

	public static final String LEFT_TAG = "left:-";
	public static final String SEPARATOR_TAG = "sep:-";
	public static final String RIGHT_TAG = "right:-";

	public PeaceMakerXMILoad(XMLHelper helper) {
		super(helper);
	}

	@Override
	protected DefaultHandler makeDefaultHandler() {
		return new PeaceMakerXMIHandler(resource, helper, options);
	}

	@Override
	public void load(XMLResource resource, InputStream inputStream, Map<?, ?> options) throws IOException {

		// convert the conflicts syntax to well-formed XML
		InputStream correctedStream = preprocessConflicts(inputStream);

		super.load(resource, correctedStream, options);
	}

	protected InputStream preprocessConflicts(InputStream inputStream) throws IOException {
		String streamContents = stream2string(inputStream);

		streamContents = streamContents.replaceAll("<<<<<<<\\s+(.*)",
				"<" + LEFT_TAG + " name=\"$1\"/>");
		streamContents = streamContents.replaceAll("=======",
				"<" + SEPARATOR_TAG + "/>");
		streamContents = streamContents.replaceAll(">>>>>>>\\s+(.*)",
				"<" + RIGHT_TAG + " name=\"$1\"/>");

		return new ByteArrayInputStream(streamContents.getBytes());
	}

	protected String stream2string(InputStream inputStream) throws IOException {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		return result.toString("UTF-8");
	}

}
