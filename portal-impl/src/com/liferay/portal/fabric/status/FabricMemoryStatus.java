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

import com.liferay.portal.fabric.status.model.MemoryUsage;
import com.liferay.portal.kernel.process.ProcessChannel;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.MethodKey;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * @author Shuyang Zhou
 */
public class FabricMemoryStatus extends BaseSingularFabricStatus<MemoryMXBean> {

	public FabricMemoryStatus() {
		super(ManagementFactory.getMemoryMXBean());

		_heapMemoryUsage = new MemoryUsage(
			platformManagedObject.getHeapMemoryUsage());
		_nonHeapMemoryUsage = new MemoryUsage(
			platformManagedObject.getNonHeapMemoryUsage());
		_objectPendingFinalizationCount =
			platformManagedObject.getObjectPendingFinalizationCount();
		_verbose = platformManagedObject.isVerbose();
	}

	public MemoryUsage getHeapMemoryUsage() {
		return _heapMemoryUsage;
	}

	public MemoryUsage getNonHeapMemoryUsage() {
		return _nonHeapMemoryUsage;
	}

	public int getObjectPendingFinalizationCount() {
		return _objectPendingFinalizationCount;
	}

	public boolean isVerbose() {
		return _verbose;
	}

	public void setVerbose(ProcessChannel<?> processChannel, boolean verbose) {
		FabricStatusOperationUtil.syncInvoke(
			processChannel, objectName,
			new MethodHandler(_SET_VERBOSE_METHOD_KEY, verbose));
	}

	public void gc(ProcessChannel<?> processChannel) {
		FabricStatusOperationUtil.syncInvoke(
			processChannel, objectName, new MethodHandler(_GC_METHOD_KEY));
	}

	private static final long serialVersionUID = 1L;

	private static final MethodKey _SET_VERBOSE_METHOD_KEY =
		new MethodKey(MemoryMXBean.class, "setVerbose", boolean.class);

	private static final MethodKey _GC_METHOD_KEY =
		new MethodKey(MemoryMXBean.class, "gc");

	private final MemoryUsage _heapMemoryUsage;
	private final MemoryUsage _nonHeapMemoryUsage;
	private final int _objectPendingFinalizationCount;
	private final boolean _verbose;

}
