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

import java.lang.management.MemoryManagerMXBean;

import java.util.Arrays;

/**
 * @author Shuyang Zhou
 */
public class MemoryManagerMXBeanModel extends MXBeanModel {

	public MemoryManagerMXBeanModel(MemoryManagerMXBean memoryManagerMXBean) {
		super(memoryManagerMXBean.getObjectName());

		_memoryPoolNames = memoryManagerMXBean.getMemoryPoolNames();
		_name = memoryManagerMXBean.getName();
		_valid = memoryManagerMXBean.isValid();
	}

	public String[] getMemoryPoolNames() {
		return Arrays.copyOf(_memoryPoolNames, _memoryPoolNames.length);
	}

	public String getName() {
		return _name;
	}

	public boolean isValid() {
		return _valid;
	}

	private static final long serialVersionUID = 1L;

	private final String[] _memoryPoolNames;
	private final String _name;
	private final boolean _valid;

}