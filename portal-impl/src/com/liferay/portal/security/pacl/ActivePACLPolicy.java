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

		_portalBeanPropertyGetterWhiteList = parsePortalBeanPropertyWhiteList(
			beanPropertyGetterSet);

		Set<String> beanPropertySetterSet = getPropertySet(
			"security-manager-set-bean-property");

		_portalBeanPropertySetterWhiteList = parsePortalBeanPropertyWhiteList(
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

	protected void initSQLChecker() {
		_sqlChecker = new SQLChecker();

		initChecker(_sqlChecker);
	}

	protected Map<String, Set<String>> parsePortalBeanPropertyWhiteList(
		Set<String> beanPropertySet) {

		Map<String, Set<String>> beanPropertyWhiteList =
			new HashMap<String, Set<String>>();

		for (String beanProperty : beanPropertySet) {
			String className = beanProperty;

			int index = beanProperty.indexOf(CharPool.POUND);

			if (index == -1) {
				Set<String> methodNameSet = beanPropertyWhiteList.get(
					className);

				if (methodNameSet == null) {
					beanPropertyWhiteList.put(
						className, Collections.<String>emptySet());
				}
			}
			else {
				String methodName = className.substring(index + 1);
				className = className.substring(0, index);

				Set<String> methodNameSet = beanPropertyWhiteList.get(
					className);

				if ((methodNameSet == null) ||
					(methodNameSet == Collections.<String>emptySet())) {

					methodNameSet = new HashSet<String>();

					beanPropertyWhiteList.put(className, methodNameSet);
				}

				methodNameSet.add(methodName);
			}
		}

		return beanPropertyWhiteList;
	}

	private static Log _log = LogFactoryUtil.getLog(ActivePACLPolicy.class);

	private JNDIChecker _jndiChecker;
	private Map<String, Set<String>> _portalBeanPropertyGetterWhiteList;
	private Map<String, Set<String>> _portalBeanPropertySetterWhiteList;
	private PortalServiceChecker _portalServiceChecker;
	private SQLChecker _sqlChecker;

}