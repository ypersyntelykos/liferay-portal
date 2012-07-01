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

package com.liferay.portal.security.pacl;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.pacl.permission.PortalServicePermission;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.pacl.checker.Checker;
import com.liferay.portal.security.pacl.checker.JNDIChecker;
import com.liferay.portal.security.pacl.checker.PortalServiceChecker;
import com.liferay.portal.security.pacl.checker.SQLChecker;

import java.lang.reflect.Method;

import java.security.Permission;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Brian Wing Shun Chan
 * @author Shuyang Zhou
 */
public class ActivePACLPolicy extends BasePACLPolicy {

	public ActivePACLPolicy(
		String servletContextName, ClassLoader classLoader,
		Properties properties) {

		super(servletContextName, classLoader, properties);

		try {
			initJNDIChecker();
			initPortalBeanPropertyWhiteList();
			initPortalServiceChecker();
			initSQLChecker();
			initServices();
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void checkPermission(Permission permission) {
		Checker checker = getChecker(permission.getClass());

		checker.checkPermission(permission);
	}

	public Map<String, Set<String>> getPortalBeanPropertyGetterWhiteList() {
		return _portalBeanPropertyGetterWhiteList;
	}

	public Map<String, Set<String>> getPortalBeanPropertySetterWhiteList() {
		return _portalBeanPropertySetterWhiteList;
	}

	public Map<String, Set<String>> getServiceWhiteList(
		String servletContextName) {

		if (Validator.isNull(servletContextName)) {
			return _portalServiceWhiteList;
		}

		return _pluginServiceWhiteList.get(servletContextName);
	}

	public boolean hasJNDI(String name) {
		return _jndiChecker.hasJNDI(name);
	}

	public boolean hasPortalService(
		Object object, Method method, Object[] arguments) {

		return _portalServiceChecker.hasService(object, method, arguments);
	}

	public boolean hasSQL(String sql) {
		return _sqlChecker.hasSQL(sql);
	}

	public boolean isActive() {
		return true;
	}

	protected void initJNDIChecker() {
		_jndiChecker = new JNDIChecker();

		initChecker(_jndiChecker);
	}

	protected void initPortalBeanPropertyWhiteList() {
		Set<String> beanPropertyGetterSet = getPropertySet(
			"security-manager-get-bean-property");

		_portalBeanPropertyGetterWhiteList = parsePropertyWhiteList(
			beanPropertyGetterSet);

		Set<String> beanPropertySetterSet = getPropertySet(
			"security-manager-set-bean-property");

		_portalBeanPropertySetterWhiteList = parsePropertyWhiteList(
			beanPropertySetterSet);
	}

	protected void initPortalServiceChecker() {
		_portalServiceChecker = (PortalServiceChecker)getChecker(
			PortalServicePermission.class);

		if (_portalServiceChecker == null) {
			_portalServiceChecker = new PortalServiceChecker();

			initChecker(_portalServiceChecker);
		}
	}

	protected void initServices() {
		_pluginServiceWhiteList =
			new HashMap<String, Map<String, Set<String>>>();

		Properties properties = getProperties();

		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();

			if (!key.startsWith("security-manager-services[")) {
				continue;
			}

			int x = key.indexOf("[");
			int y = key.indexOf("]", x);

			String servicesServletContextName = key.substring(x + 1, y);

			Set<String> services = SetUtil.fromArray(StringUtil.split(value));

			Map<String, Set<String>> serviceWhiteList = parsePropertyWhiteList(
				services);

			if (servicesServletContextName.equals(
				_PORTAL_SERVLET_CONTEXT_NAME)) {

				_portalServiceWhiteList = serviceWhiteList;
			}
			else {
				_pluginServiceWhiteList.put(
					servicesServletContextName, serviceWhiteList);
			}
		}

		Set<String> portalServiceSet = getPropertySet(
			"security-manager-services[portal]");

	}

	protected void initSQLChecker() {
		_sqlChecker = new SQLChecker();

		initChecker(_sqlChecker);
	}

	protected Map<String, Set<String>> parsePropertyWhiteList(
		Set<String> propertySet) {

		Map<String, Set<String>> whiteList = new HashMap<String, Set<String>>();

		for (String property : propertySet) {
			String className = property;

			int index = property.indexOf(CharPool.POUND);

			if (index == -1) {
				Set<String> methodNameSet = whiteList.get(className);

				if (methodNameSet == null) {
					whiteList.put(className, Collections.<String>emptySet());
				}
			}
			else {
				String methodName = className.substring(index + 1);
				className = className.substring(0, index);

				Set<String> methodNameSet = whiteList.get(className);

				if ((methodNameSet == null) ||
					(methodNameSet == Collections.<String>emptySet())) {

					methodNameSet = new HashSet<String>();

					whiteList.put(className, methodNameSet);
				}

				methodNameSet.add(methodName);
			}
		}

		return whiteList;
	}

	private static final String _PORTAL_SERVLET_CONTEXT_NAME = "portal";

	private static Log _log = LogFactoryUtil.getLog(ActivePACLPolicy.class);

	private JNDIChecker _jndiChecker;
	private Map<String, Map<String, Set<String>>> _pluginServiceWhiteList;
	private Map<String, Set<String>> _portalBeanPropertyGetterWhiteList;
	private Map<String, Set<String>> _portalBeanPropertySetterWhiteList;
	private PortalServiceChecker _portalServiceChecker;
	private Map<String, Set<String>> _portalServiceWhiteList;
	private SQLChecker _sqlChecker;

}