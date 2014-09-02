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

/**
 * @author Shuyang Zhou
 */
public class MonitorInfo extends LockInfo {

	public MonitorInfo(java.lang.management.MonitorInfo monitorInfo) {
		super(monitorInfo);

		_lockedStackDepth = monitorInfo.getLockedStackDepth();
		_lockedStackFrame = monitorInfo.getLockedStackFrame();
	}

	public int getLockedStackDepth() {
		return _lockedStackDepth;
	}

	public StackTraceElement getLockedStackFrame() {
		return _lockedStackFrame;
	}

	private static final long serialVersionUID = 1L;

	private final int _lockedStackDepth;
	private final StackTraceElement _lockedStackFrame;

}
