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

import java.util.Map;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * @author Shuyang Zhou
 */
@Aspect("pertypewithin(@com.liferay.portal.kernel.security.annotation.AccessControl *)")
public class AccessControlGetterAspect extends BaseAccessControlAspect {

	@Override
	public boolean acceptClass(
		Set<String> getterMethodNameSet, Set<String> setterMethodNameSet,
		AccessControl accessControl) {

		if (!accessControl.checkGetter()) {
			return false;
		}

		return isFullAccess(getterMethodNameSet);
	}

	@Before("execution(public static * (@com.liferay.portal.kernel.security.annotation.AccessControl *).*(..))")
	public void doAccessControl(JoinPoint.StaticPart joinPointStaticPart) {
		if (paclPolicy == null) {
			return;
		}

		Map<String, Set<String>> portalBeanPropertyGetterWhiteList =
			paclPolicy.getPortalBeanPropertyGetterWhiteList();

		Signature signature = joinPointStaticPart.getSignature();

		String className = signature.getDeclaringTypeName();

		Set<String> getterMethodNameSet = portalBeanPropertyGetterWhiteList.get(
			className);

		if (getterMethodNameSet == null) {
			throw new SecurityException("Undeclared access to " + signature);
		}

		String methodName = signature.getName();

		if (!getterMethodNameSet.contains(methodName)) {
			throw new SecurityException("Undeclared access to " + signature);
		}
	}

}