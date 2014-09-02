/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.portal.fabric.status.model;

import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class LockInfo implements Serializable {

	public LockInfo(java.lang.management.LockInfo lockInfo) {
		_className = lockInfo.getClassName();
		_identityHashCode = lockInfo.getIdentityHashCode();
		_toString = lockInfo.toString();
	}

	public String getClassName() {
		return _className;
	}

	public int getIdentityHashCode() {
		return _identityHashCode;
	}

	@Override
	public String toString() {
		return _toString;
	}

	private static final long serialVersionUID = 1L;

	private final String _className;
	private final int _identityHashCode;
	private final String _toString;
}
