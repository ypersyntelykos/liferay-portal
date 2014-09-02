
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
public class ThreadInfo implements Serializable {

	public ThreadInfo(java.lang.management.ThreadInfo threadInfo) {
		_blockedCount = threadInfo.getBlockedCount();
		_blockedTime = threadInfo.getBlockedTime();
		_lockInfo = new LockInfo(threadInfo.getLockInfo());
		_lockName = threadInfo.getLockName();
		_lockOwnerId = threadInfo.getLockOwnerId();
		_lockOwnerName = threadInfo.getLockOwnerName();
		_stackTrace = threadInfo.getStackTrace();
		_threadId = threadInfo.getThreadId();
		_threadName = threadInfo.getThreadName();
		_threadState = threadInfo.getThreadState();
		_waitedCount = threadInfo.getWaitedCount();
		_waitedTime = threadInfo.getWaitedTime();
		_inNative = threadInfo.isInNative();
		_suspended = threadInfo.isSuspended();
		_toString = threadInfo.toString();
		

		java.lang.management.MonitorInfo[] monitorInfos =
			threadInfo.getLockedMonitors();

		_lockedMonitors = new MonitorInfo[monitorInfos.length];

		for (int i = 0; i < monitorInfos.length; i++) {
			_lockedMonitors[i] = new MonitorInfo(monitorInfos[i]);
		}

		java.lang.management.LockInfo[] lockInfos =
			threadInfo.getLockedSynchronizers();

		_lockedSynchronizers = new LockInfo[lockInfos.length];

		for (int i = 0; i < lockInfos.length; i++) {
			_lockedSynchronizers[i] = new LockInfo(lockInfos[i]);
		}
	}

	public String getThreadName() {
		return _threadName;
	}

	public long getThreadId() {
		return _threadId;
	}

	public long getBlockedTime() {
		return _blockedTime;
	}

	public long getBlockedCount() {
		return _blockedCount;
	}

	public long getWaitedTime() {
		return _waitedTime;
	}

	public long getWaitedCount() {
		return _waitedCount;
	}

	public LockInfo getLockInfo() {
		return _lockInfo;
	}

	public String getLockName() {
		return _lockName;
	}

	public long getLockOwnerId() {
		return _lockOwnerId;
	}

	public String getLockOwnerName() {
		return _lockOwnerName;
	}

	public boolean isInNative() {
		return _inNative;
	}

	public boolean isSuspended() {
		return _suspended;
	}

	public Thread.State getThreadState() {
		return _threadState;
	}

	public StackTraceElement[] getStackTrace() {
		return _stackTrace;
	}

	public MonitorInfo[] getLockedMonitors() {
		return _lockedMonitors;
	}

	public LockInfo[] getLockedSynchronizers() {
		return _lockedSynchronizers;
	}

	@Override
	public String toString() {
		return _toString;
	}

	private static final long serialVersionUID = 1L;

	private final String _threadName;
	private final long _threadId;
	private final long _blockedTime;
	private final long _blockedCount;
	private final long _waitedTime;
	private final long _waitedCount;
	private final LockInfo _lockInfo;
	private final String _lockName;
	private final long _lockOwnerId;
	private final String _lockOwnerName;
	private final boolean _inNative;
	private final boolean _suspended;
	private final Thread.State _threadState;
	private final StackTraceElement[] _stackTrace;
	private final MonitorInfo[] _lockedMonitors;
	private final LockInfo[] _lockedSynchronizers;
	private final String _toString;
}
