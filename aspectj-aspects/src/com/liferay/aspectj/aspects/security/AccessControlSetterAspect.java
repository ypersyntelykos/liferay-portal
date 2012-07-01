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
public class AccessControlSetterAspect extends BaseAccessControlAspect {

	@Override
	public boolean acceptClass(
		Set<String> getterMethodNameSet, Set<String> setterMethodNameSet,
		AccessControl accessControl) {

		if (!accessControl.checkGetter()) {
			return false;
		}

		return isFullAccess(setterMethodNameSet);
	}

	@Before("set(* (@com.liferay.portal.kernel.security.annotation.AccessControl *).*)")
	public void doAccessControl(
		JoinPoint.EnclosingStaticPart enclosingJoinPointStaticPart) {

		if (paclPolicy == null) {
			return;
		}

		Map<String, Set<String>> portalBeanPropertySetterWhiteList =
			paclPolicy.getPortalBeanPropertySetterWhiteList();

		Signature signature = enclosingJoinPointStaticPart.getSignature();

		String className = signature.getDeclaringTypeName();

		Set<String> setterMethodNameSet = portalBeanPropertySetterWhiteList.get(
			className);

		if (setterMethodNameSet == null) {
			throw new SecurityException("Undeclared access to " + signature);
		}

		String methodName = signature.getName();

		if (!setterMethodNameSet.contains(methodName)) {
			throw new SecurityException("Undeclared access to " + signature);
		}
	}

}