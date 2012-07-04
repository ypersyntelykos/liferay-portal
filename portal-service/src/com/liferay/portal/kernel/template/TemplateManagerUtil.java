/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.kernel.template;

import com.liferay.portal.kernel.security.annotation.AccessControl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tina Tian
 */
@AccessControl
public class TemplateManagerUtil {

	public static void destroy() {
		for (TemplateManager templateManager : _templateManagers.values()) {
			templateManager.destroy();
		}

		_templateManagers.clear();
	}

	public static void destroy(ClassLoader classLoader) {
		for (TemplateManager templateManager : _templateManagers.values()) {
			templateManager.destroy(classLoader);
		}
	}

	public static Template getTemplate(
			String templateManagerName, TemplateResource templateResource,
			TemplateContextType templateContextType)
		throws TemplateException {

		TemplateManager templateManager = _getTemplateManager(
			templateManagerName);

		return templateManager.getTemplate(
			templateResource, templateContextType);
	}

	public static Template getTemplate(
			String templateManagerName, TemplateResource templateResource,
			TemplateResource errorTemplateResource,
			TemplateContextType templateContextType)
		throws TemplateException {

		TemplateManager templateManager = _getTemplateManager(
			templateManagerName);

		return templateManager.getTemplate(
			templateResource, errorTemplateResource, templateContextType);
	}

	public static TemplateManager getTemplateManager(
		String templateManagerName) {

		return _templateManagers.get(templateManagerName);
	}

	public static Set<String> getTemplateManagerNames(
		String templateManagerName) {

		return _templateManagers.keySet();
	}

	public static Map<String, TemplateManager> getTemplateManagers() {
		return Collections.unmodifiableMap(_templateManagers);
	}

	public static boolean hasTemplateManager(String templateManagerName) {
		return _templateManagers.containsKey(templateManagerName);
	}

	public static void init() throws TemplateException {
		for (TemplateManager templateManager : _templateManagers.values()) {
			templateManager.init();
		}
	}

	public static void registerTemplateManager(TemplateManager templateManager)
		throws TemplateException {

		templateManager.init();

		_templateManagers.put(templateManager.getName(), templateManager);
	}

	public static void unregisterTemplateManager(String templateManagerName) {
		TemplateManager templateManager = _templateManagers.remove(
			templateManagerName);

		if (templateManager != null) {
			templateManager.destroy();
		}
	}

	public void setTemplateManagers(List<TemplateManager> templateManagers) {
		for (TemplateManager templateManager : templateManagers) {
			_templateManagers.put(templateManager.getName(), templateManager);
		}
	}

	private static TemplateManager _getTemplateManager(
			String templateManagerName)
		throws TemplateException {

		TemplateManager templateManager = _templateManagers.get(
			templateManagerName);

		if (templateManager == null) {
			throw new TemplateException(
				"Unsupported template manager " + templateManagerName);
		}

		return templateManager;
	}

	private static Map<String, TemplateManager> _templateManagers =
		new ConcurrentHashMap<String, TemplateManager>();

}