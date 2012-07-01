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
import com.liferay.portal.security.pacl.aspect.AcceptStatus;

import java.util.Map;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * @author Shuyang Zhou
 */
@Aspect("pertypewithin(*)")
public class AccessControlServiceAspect extends BaseAccessControlAspect {

	@Override
	public AcceptStatus acceptClass(
		PACLPolicy paclPolicy, Class<?> clazz, AccessControl accessControl) {

		if (!accessControl.persistence() && !accessControl.service()) {
			return AcceptStatus.FULL_ACCESS;
		}

		String servletContextName = accessControl.servletContextName();

		Map<String, Set<String>> serviceWhiteList =
			paclPolicy.getServiceWhiteList(servletContextName);

		String className = toInterfaceName(clazz.getName(), accessControl);

		Set<String> serviceMethodNameSet = serviceWhiteList.get(className);

		return toAcceptStatus(serviceMethodNameSet);
	}

	@Before("execution(public static * (@com.liferay.portal.kernel.security.annotation.AccessControl *).*(..)) && @within(accessControl)")
	public void doAccessControl(
		JoinPoint.StaticPart joinPointStaticPart, AccessControl accessControl) {

		if (paclPolicy == null) {
			return;
		}

		String servletContextName = accessControl.servletContextName();

		Map<String, Set<String>> serviceWhiteList =
			paclPolicy.getServiceWhiteList(servletContextName);

		Signature signature = joinPointStaticPart.getSignature();

		String className = toInterfaceName(
			signature.getDeclaringTypeName(), accessControl);

		Set<String> serviceMethodNameSet = serviceWhiteList.get(className);

		if (serviceMethodNameSet == null) {
			throw new SecurityException("Undeclared access to " + signature);
		}

		String methodName = signature.getName();

		if (!serviceMethodNameSet.contains(methodName)) {
			throw new SecurityException("Undeclared access to " + signature);
		}
	}

	protected String toInterfaceName(
		String className, AccessControl accessControl) {

		if (accessControl.persistence()) {
			if (!className.endsWith("Util")) {
				throw new SecurityException(
					className + " is not a valid persistence util class.");
			}

			className = className.substring(0, className.length() - 4);

			className = className.concat("Persistence");
		}
		else if (accessControl.service()) {
			if (!className.endsWith("ServiceUtil")) {
				throw new SecurityException(
					className + " is not a valid service util class.");
			}

			className = className.substring(0, className.length() - 4);
		}

		return className;
	}

}