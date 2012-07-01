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

package com.liferay.aspectj.aspects.security;

import com.liferay.portal.kernel.security.annotation.AccessControl;
import com.liferay.portal.security.pacl.PACLPolicy;
import com.liferay.portal.security.pacl.aspect.PACLAspect;

import java.util.Map;
import java.util.Set;

/**
 * @author Shuyang Zhou
 */
public abstract class BaseAccessControlAspect extends PACLAspect {

	public boolean acceptClass(PACLPolicy paclPolicy, Class<?> clazz) {
		AccessControl accessControl = clazz.getAnnotation(AccessControl.class);

		if (accessControl == null) {
			return false;
		}

		String className = clazz.getName();

		Map<String, Set<String>> portalBeanPropertyGetterWhiteList =
			paclPolicy.getPortalBeanPropertyGetterWhiteList();

		Map<String, Set<String>> portalBeanPropertySetterWhiteList =
			paclPolicy.getPortalBeanPropertySetterWhiteList();

		Set<String> getterMethodNameSet = portalBeanPropertyGetterWhiteList.get(
			className);

		Set<String> setterMethodNameSet = portalBeanPropertySetterWhiteList.get(
			className);

		if (accessControl.checkGetter() && (getterMethodNameSet == null) &&
			accessControl.checkSetter() && (setterMethodNameSet == null)) {

			throw new SecurityException("Undeclared access to " + clazz);
		}

		return acceptClass(
			getterMethodNameSet, setterMethodNameSet, accessControl);
	}

	public abstract boolean acceptClass(
		Set<String> getterMethodNameSet, Set<String> setterMethodNameSet,
		AccessControl accessControl);

	public boolean isFullAccess(Set<String> methodNameSet) {
		if ((methodNameSet != null) && methodNameSet.isEmpty()) {
			return false;
		}
		else {
			return true;
		}
	}

}