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

import java.lang.management.ManagementFactory;
import java.lang.management.PlatformLoggingMXBean;

import java.util.Collections;
import java.util.List;

/**
 * @author Shuyang Zhou
 */
public class FabricPlatformLoggingStatus
	extends BaseSingularFabricStatus<PlatformLoggingMXBean> {

	public FabricPlatformLoggingStatus() {
		super(ManagementFactory.getPlatformMXBean(PlatformLoggingMXBean.class));

		_loggerNames = platformManagedObject.getLoggerNames();
	}

	public String getLoggerLevel(
		ProcessChannel<?> processChannel, String loggerName) {

		return FabricStatusOperationUtil.syncInvoke(
			processChannel, objectName,
			new MethodHandler(_GET_LOGGER_LEVEL_METHOD_KEY, loggerName));
	}

	public List<String> getLoggerNames() {
		return Collections.unmodifiableList(_loggerNames);
	}

	public String getParentLoggerName(
		ProcessChannel<?> processChannel, String loggerName) {

		return FabricStatusOperationUtil.syncInvoke(
			processChannel, objectName,
			new MethodHandler(_GET_PARENT_LOGGER_NAME_METHOD_KEY, loggerName));
	}

	public void setLoggerLevel(
		ProcessChannel<?> processChannel, String loggerName, String levelName) {

		FabricStatusOperationUtil.invoke(
			processChannel, objectName,
			new MethodHandler(
				_SET_LOGGER_LEVEL_METHOD_KEY, loggerName, levelName));
	}

	private static final MethodKey _GET_LOGGER_LEVEL_METHOD_KEY =
		new MethodKey(
			PlatformLoggingMXBean.class, "getLoggerLevel", String.class);

	private static final MethodKey _GET_PARENT_LOGGER_NAME_METHOD_KEY =
		new MethodKey(
			PlatformLoggingMXBean.class, "getParentLoggerName", String.class);

	private static final MethodKey _SET_LOGGER_LEVEL_METHOD_KEY =
		new MethodKey(
			PlatformLoggingMXBean.class, "setLoggerLevel", String.class,
			String.class);

	private static final long serialVersionUID = 1L;

	private final List<String> _loggerNames;

}