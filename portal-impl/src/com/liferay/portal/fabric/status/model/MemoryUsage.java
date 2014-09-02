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
public class MemoryUsage implements Serializable {

	public MemoryUsage(java.lang.management.MemoryUsage memoryUsage) {
		_committed = memoryUsage.getCommitted();
		_init = memoryUsage.getInit();
		_max = memoryUsage.getMax();
		_used = memoryUsage.getUsed();
		_toString = memoryUsage.toString();
	}

	public long getCommitted() {
		return _committed;
	}

	public long getInit() {
		return _init;
	}

	public long getMax() {
		return _max;
	}

	public long getUsed() {
		return _used;
	}

	@Override
	public String toString() {
		return _toString;
	}

	private static final long serialVersionUID = 1L;

	private final long _committed;
	private final long _init;
	private final long _max;
	private final String _toString;
	private final long _used;

}