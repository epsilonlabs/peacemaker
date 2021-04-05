package org.eclipse.epsilon.peacemaker.util.ids;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.epsilon.peacemaker.PeacemakerResource;
import org.eclipse.epsilon.peacemaker.PeacemakerResourceFactory;

public class IdUtils {

	public static String getAvailableId(XMLResource resource, EObject obj) {
		String id = resource.getID(obj);
		return id != null ? id : EcoreUtil.getID(obj);
	}

	public static boolean hasXMIId(XMLResource resource, EObject obj) {
		return resource.getID(obj) != null;
	}

	public static boolean containsObjectWithId(XMIResource resource, String id) {
		return resource.getEObject(id) != null;
	}

	/**
	 * Loads a resource and checks for duplicated ids
	 * 
	 * @param resource The resource to load
	 * @param contents Resource contents to perform the load
	 * @return True if the resource has at least one duplicated id
	 */
	public static boolean hasDuplicatedIds(URI resourceURI, InputStream contents) {
		return !findDuplicatedIds(resourceURI, contents, true).isEmpty();
	}

	/**
	 * Loads a resource and finds all duplicated ids
	 * 
	 * @param resource  The resource to load
	 * @param contents  Resource contents to perform the load
	 * @param findFirst If true, loading stops after finding a duplicated id
	 */
	public static Set<String> findDuplicatedIds(URI resourceURI, InputStream contents) {
		return findDuplicatedIds(resourceURI, contents, false);
	}

	/**
	 * Loads a resource and finds duplicated ids
	 * 
	 * @param resource      The resource to load
	 * @param contents      Resource contents to perform the load
	 * @param failOnDuplicatedids If true, loading stops after finding a duplicated id
	 */
	private static Set<String> findDuplicatedIds(URI resourceURI,
			InputStream contents, boolean failOnDuplicatedids) {

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				"*", new PeacemakerResourceFactory());
		PeacemakerResource pmResource =
				(PeacemakerResource) resourceSet.createResource(resourceURI);
		pmResource.setFailOnDuplicatedIds(failOnDuplicatedids);

		try {
			pmResource.load(contents, null);
		}
		catch (DuplicatedIdsException ex) {
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		// atm assume that input resources have no conflict sections
		return pmResource.getDuplicatedIds().keySet();
	}
}
