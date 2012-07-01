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
import com.liferay.portal.security.pacl.aspect.PACLAspect;

import java.util.Set;

/**
 * @author Shuyang Zhou
 */
public abstract class BaseAccessControlAspect extends PACLAspect {

	public AcceptStatus acceptClass(PACLPolicy paclPolicy, Class<?> clazz) {
		AccessControl accessControl = clazz.getAnnotation(AccessControl.class);

		if (accessControl == null) {
			return AcceptStatus.FULL_ACCESS;
		}

		return acceptClass(paclPolicy, clazz, accessControl);
	}

	public abstract AcceptStatus acceptClass(
		PACLPolicy paclPolicy, Class<?> clazz, AccessControl accessControl);

	public AcceptStatus toAcceptStatus(Set<String> methodNameSet) {
		if (methodNameSet == null) {
			return AcceptStatus.REJECT_ACCESS;
		}
		else if (methodNameSet.isEmpty()) {
			return AcceptStatus.FULL_ACCESS;
		}
		else {
			return AcceptStatus.PARTIAL_ACCESS;
		}
	}

}