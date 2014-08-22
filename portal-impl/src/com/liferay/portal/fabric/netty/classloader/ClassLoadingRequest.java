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

package com.liferay.portal.fabric.netty.classloader;

import com.liferay.portal.kernel.util.CharPool;

import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class ClassLoadingRequest implements Serializable {

	public ClassLoadingRequest(long fabricWorkerId, String className) {
		_fabrciWorkerId = fabricWorkerId;
		_className = className;
	}

	public String getClassName() {
		return _className;
	}

	public long getFabrciWorkerId() {
		return _fabrciWorkerId;
	}

	public String getResourceName() {
		return _className.replace(CharPool.PERIOD, CharPool.SLASH);
	}

	private static final long serialVersionUID = 1L;

	private final String _className;
	private final long _fabrciWorkerId;

}