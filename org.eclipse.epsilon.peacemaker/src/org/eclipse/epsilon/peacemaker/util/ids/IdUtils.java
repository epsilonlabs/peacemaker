package org.eclipse.epsilon.peacemaker.util.ids;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class IdUtils {

	public static String getAvailableId(XMLResource resource, EObject obj) {
		String id = resource.getID(obj);
		return id != null ? id : EcoreUtil.getID(obj);
	}

	public static boolean hasXMIId(XMLResource resource, EObject obj) {
		return resource.getID(obj) != null;
	}

	/**
	 * Loads a resource and checks for duplicated ids
	 * 
	 * @param resource The resource to load
	 * @param contents Resource contents to perform the load
	 * @return True if the resource has at least one duplicated id
	 */
	public static boolean hasDuplicatedIds(XMIResource resource, InputStream contents) {
		return !findDuplicatedIds(resource, contents, true).isEmpty();
	}

	/**
	 * Loads a resource and finds all duplicated ids
	 * 
	 * @param resource  The resource to load
	 * @param contents  Resource contents to perform the load
	 * @param findFirst If true, loading stops after finding a duplicated id
	 */
	public static Set<String> findDuplicatedIds(XMIResource resource, InputStream contents) {
		return findDuplicatedIds(resource, contents, false);
	}

	/**
	 * Loads a resource and finds duplicated ids
	 * 
	 * @param resource      The resource to load
	 * @param contents      Resource contents to perform the load
	 * @param stopWithFirst If true, loading stops after finding a duplicated id
	 */
	private static Set<String> findDuplicatedIds(XMIResource resource,
			InputStream contents, boolean stopWithFirst) {

		Map<String, List<EObject>> duplicatedIds = new HashMap<>();

		// the pool allows decorating the xml handler to get element lines
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
		loadOptions.put(XMLResource.OPTION_USE_PARSER_POOL,
				new FindDuplicatedIdsParserPoolImpl(duplicatedIds, stopWithFirst));

		try {
			resource.load(contents, loadOptions);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (DuplicatedIdsException dupIds) {
			// launched when stopWithFirst == true and a dup is found, nothing to do
		}

		return duplicatedIds.keySet();
	}
}
