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

import com.liferay.portal.fabric.status.FabricStatusOperationUtil;
import com.liferay.portal.kernel.process.ProcessChannel;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.Arrays;

/**
 * @author Shuyang Zhou
 */
public class MemoryPoolMXBeanModel extends MXBeanModel {

	public MemoryPoolMXBeanModel(
		MemoryPoolMXBean memoryPoolMXBean) {

		super(memoryPoolMXBean.getObjectName());

		_collectionUsage = new MemoryUsage(
			memoryPoolMXBean.getCollectionUsage());
		_collectionUsageThreshold =
			memoryPoolMXBean.getCollectionUsageThreshold();
		_collectionUsageThresholdCount =
			memoryPoolMXBean.getCollectionUsageThresholdCount();
		_memoryManagerNames = memoryPoolMXBean.getMemoryManagerNames();
		_name = memoryPoolMXBean.getName();
		_peakUsage = new MemoryUsage(memoryPoolMXBean.getPeakUsage());
		_memoryType = memoryPoolMXBean.getType();
		_usage = new MemoryUsage(memoryPoolMXBean.getUsage());
		_usageThreshold = memoryPoolMXBean.getUsageThreshold();
		_usageThresholdCount = memoryPoolMXBean.getUsageThresholdCount();
		_collectionUsageThresholdExceeded =
			memoryPoolMXBean.isCollectionUsageThresholdExceeded();
		_collectionUsageThresholdSupported =
			memoryPoolMXBean.isCollectionUsageThresholdSupported();
		_usageThresholdExceeded = memoryPoolMXBean.isUsageThresholdExceeded();
		_usageThresholdSupported = memoryPoolMXBean.isUsageThresholdSupported();
		_valid = memoryPoolMXBean.isValid();
	}

	public MemoryUsage getCollectionUsage() {
		return _collectionUsage;
	}

	public long getCollectionUsageThreshold() {
		return _collectionUsageThreshold;
	}

	public long getCollectionUsageThresholdCount() {
		return _collectionUsageThresholdCount;
	}

	public String[] getMemoryManagerNames() {
		return Arrays.copyOf(_memoryManagerNames, _memoryManagerNames.length);
	}

	public String getName() {
		return _name;
	}

	public MemoryUsage getPeakUsage() {
		return _peakUsage;
	}

	public MemoryType getMemoryType() {
		return _memoryType;
	}

	public MemoryUsage getUsage() {
		return _usage;
	}

	public long getUsageThreshold() {
		return _usageThreshold;
	}

	public long getUsageThresholdCount() {
		return _usageThresholdCount;
	}

	public boolean isCollectionUsageThresholdExceeded() {
		return _collectionUsageThresholdExceeded;
	}

	public boolean isCollectionUsageThresholdSupported() {
		return _collectionUsageThresholdSupported;
	}

	public boolean isUsageThresholdExceeded() {
		return _usageThresholdExceeded;
	}

	public boolean isUsageThresholdSupported() {
		return _usageThresholdSupported;
	}

	public boolean isValid() {
		return _valid;
	}

	public void resetPeakUsage(ProcessChannel<?> processChannel) {
		FabricStatusOperationUtil.syncInvoke(
			processChannel, objectName,
			new MethodHandler(_RESET_PEAK_USAGE_METHOD_KEY));
	}

	public void setCollectionUsageThreshold(
		ProcessChannel<?> processChannel, long threshold) {

		FabricStatusOperationUtil.syncInvoke(
			processChannel, objectName,
			new MethodHandler(
				_SET_COLLECTION_USAGE_THRESHOLD_METHOD_KEY, threshold));
	}

	public void setUsageThreshold(
		ProcessChannel<?> processChannel, long threshold) {

		FabricStatusOperationUtil.syncInvoke(
			processChannel, objectName,
			new MethodHandler(_SET_USAGE_THRESHOLD_METHOD_KEY, threshold));
	}

	private static final long serialVersionUID = 1L;

	private static final MethodKey _RESET_PEAK_USAGE_METHOD_KEY =
		new MethodKey(MemoryPoolMXBean.class, "resetPeakUsage");

	private static final MethodKey _SET_COLLECTION_USAGE_THRESHOLD_METHOD_KEY =
		new MethodKey(
			MemoryPoolMXBean.class, "setCollectionUsageThreshold", long.class);

	private static final MethodKey _SET_USAGE_THRESHOLD_METHOD_KEY =
		new MethodKey(MemoryPoolMXBean.class, "setUsageThreshold", long.class);

	private final MemoryUsage _collectionUsage;
	private final long _collectionUsageThreshold;
	private final long _collectionUsageThresholdCount;
	private final String[] _memoryManagerNames;
	private final String _name;
	private final MemoryUsage _peakUsage;
	private final MemoryType _memoryType;
	private final MemoryUsage _usage;
	private final long _usageThreshold;
	private final long _usageThresholdCount;
	private final boolean _collectionUsageThresholdExceeded;
	private final boolean _collectionUsageThresholdSupported;
	private final boolean _usageThresholdExceeded;
	private final boolean _usageThresholdSupported;
	private final boolean _valid;
}
