package org.eclipse.epsilon.peacemaker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMILoadImpl;
import org.eclipse.epsilon.peacemaker.conflicts.ConflictSection;
import org.eclipse.epsilon.peacemaker.util.StreamUtils;
import org.xml.sax.helpers.DefaultHandler;

public class PeaceMakerXMILoad extends XMILoadImpl {

	public static final boolean debug = true;

	//TODO: remove
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
	public void load(XMLResource resource, InputStream inputStream,
			Map<?, ?> options) throws IOException {

		ConflictsPreprocessor preprocessor = new ConflictsPreprocessor(inputStream);
		preprocessor.run();

		if (debug) {
			System.out.println();
			System.out.println("<<< Model with Conflicts >>>");
			System.out.println(preprocessor.getOriginalContents());
			System.out.println();
			System.out.println("<<< left version: \n" +
					StreamUtils.stream2string(preprocessor.getLeftVersionHelper().getVersionContents()));
			System.out.println();
			System.out.println(">>> right version: \n" +
					StreamUtils.stream2string(preprocessor.getRightVersionHelper().getVersionContents()));
			System.out.println();
		}

		PeaceMakerXMIResource pmResource = (PeaceMakerXMIResource) resource;
		pmResource.loadLeft(preprocessor.getLeftVersionHelper());
		pmResource.loadRight(preprocessor.getRightVersionHelper());

		pmResource.setConflictSections(preprocessor.getConflictSections());

		if (debug) {
			for (ConflictSection cs : pmResource.getConflictSections()) {
				System.out.println("@@@@@@@@@@ Conflict Section @@@@@@@@@@@@@");
				System.out.println(cs);
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@");
			}
		}
		// pmResource."identify the conflicts"
	}
}
