package org.eclipse.epsilon.peacemaker.util.ids;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
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

	@SuppressWarnings("unchecked")
	public static List<EObject> getContents(EObject obj, EReference ref) {
		if (ref.getUpperBound() == 1) {
			return Arrays.asList((EObject) obj.eGet(ref));
		}
		else {
			return (List<EObject>) obj.eGet(ref);
		}
	}
}
