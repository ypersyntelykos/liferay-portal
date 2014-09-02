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

package com.liferay.portal.fabric.status;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

/**
 * @author Shuyang Zhou
 */
public class FabricClassLoadingStatus extends BaseFabricStatus {

	public FabricClassLoadingStatus() {
		super(ManagementFactory.getClassLoadingMXBean());

		ClassLoadingMXBean classLoadingMXBean =
			ManagementFactory.getClassLoadingMXBean();

		_loadedClassCount = classLoadingMXBean.getLoadedClassCount();
		_totalLoadedClassCount = classLoadingMXBean.getTotalLoadedClassCount();
		_unloadedClassCount = classLoadingMXBean.getUnloadedClassCount();
	}

	public int getLoadedClassCount() {
		return _loadedClassCount;
	}

	public long getTotalLoadedClassCount() {
		return _totalLoadedClassCount;
	}

	public long getUnloadedClassCount() {
		return _unloadedClassCount;
	}

	private final int _loadedClassCount;
	private final long _totalLoadedClassCount;
	private final long _unloadedClassCount;

}
