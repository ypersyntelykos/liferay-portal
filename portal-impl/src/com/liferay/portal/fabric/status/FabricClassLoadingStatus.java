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

import com.liferay.portal.kernel.process.ProcessChannel;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

/**
 * @author Shuyang Zhou
 */
public class FabricClassLoadingStatus
	extends BaseSingularFabricStatus<ClassLoadingMXBean> {

	public FabricClassLoadingStatus() {
		super(ManagementFactory.getClassLoadingMXBean());

		_loadedClassCount = platformManagedObject.getLoadedClassCount();
		_totalLoadedClassCount =
			platformManagedObject.getTotalLoadedClassCount();
		_unloadedClassCount = platformManagedObject.getUnloadedClassCount();
		_verbose = platformManagedObject.isVerbose();
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

	public boolean isVerbose() {
		return _verbose;
	}

	public void setVerbose(ProcessChannel<?> processChannel, boolean verbose) {
		FabricStatusOperationUtil.invoke(
			processChannel, objectName,
			new MethodHandler(_SET_VERBOSE_METHOD_KEY, verbose));
	}

	private static final MethodKey _SET_VERBOSE_METHOD_KEY = new MethodKey(
		ClassLoadingMXBean.class, "setVerbose", boolean.class);

	private static final long serialVersionUID = 1L;

	private final int _loadedClassCount;
	private final long _totalLoadedClassCount;
	private final long _unloadedClassCount;
	private final boolean _verbose;

}