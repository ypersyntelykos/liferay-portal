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

import java.lang.management.BufferPoolMXBean;

/**
 * @author Shuyang Zhou
 */
public class BufferPoolMXBeanModel extends MXBeanModel {

	public BufferPoolMXBeanModel(BufferPoolMXBean bufferPoolMXBean) {
		super(bufferPoolMXBean.getObjectName());

		_count = bufferPoolMXBean.getCount();
		_memoryUsed = bufferPoolMXBean.getMemoryUsed();
		_name = bufferPoolMXBean.getName();
		_totalCapacity = bufferPoolMXBean.getTotalCapacity();
	}

	public long getCount() {
		return _count;
	}

	public long getMemoryUsed() {
		return _memoryUsed;
	}

	public String getName() {
		return _name;
	}

	public long getTotalCapacity() {
		return _totalCapacity;
	}

	private static final long serialVersionUID = 1L;

	private final long _count;
	private final long _memoryUsed;
	private final String _name;
	private final long _totalCapacity;

}