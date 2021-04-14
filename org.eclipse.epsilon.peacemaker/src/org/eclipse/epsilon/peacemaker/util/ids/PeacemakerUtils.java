package org.eclipse.epsilon.peacemaker.util.ids;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;
import org.eclipse.epsilon.peacemaker.PeacemakerResourceFactory;

public class PeacemakerUtils {

	public static boolean hasConflicts(URI resourceURI, InputStream contents) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new PeacemakerResourceFactory());
		PeacemakerResource pmResource =
				(PeacemakerResource) resourceSet.createResource(resourceURI);
		pmResource.setFailOnDuplicatedIds(true);

		try {
			pmResource.load(contents, null);
		}
		catch (DuplicatedIdsException ex) {
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		return pmResource.hasDuplicatedIds() || pmResource.hasConflicts();
	}
}
