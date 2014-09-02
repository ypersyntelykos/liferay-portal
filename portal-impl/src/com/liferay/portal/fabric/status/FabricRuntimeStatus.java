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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Shuyang Zhou
 */
public class FabricRuntimeStatus extends BaseFabricStatus {

	public FabricRuntimeStatus() {
		super(ManagementFactory.getRuntimeMXBean());

		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

		_bootClassPath = runtimeMXBean.getBootClassPath();
		_bootClassPathSupported = runtimeMXBean.isBootClassPathSupported();
		_classPath = runtimeMXBean.getClassPath();
		_inputArguments = runtimeMXBean.getInputArguments();
		_libraryPath = runtimeMXBean.getLibraryPath();
		_managementSpecVersion = runtimeMXBean.getManagementSpecVersion();
		_name = runtimeMXBean.getName();
		_specName = runtimeMXBean.getSpecName();
		_specVendor = runtimeMXBean.getSpecVendor();
		_specVersion = runtimeMXBean.getSpecVersion();
		_startTime = runtimeMXBean.getStartTime();
		_systemProperties = runtimeMXBean.getSystemProperties();
		_uptime = runtimeMXBean.getUptime();
		_vmName = runtimeMXBean.getVmName();
		_vmVendor = runtimeMXBean.getVmVendor();
		_vmVersion = runtimeMXBean.getVmVersion();
	}

	public String getBootClassPath() {
		return _bootClassPath;
	}

	public String getClassPath() {
		return _classPath;
	}

	public List<String> getInputArguments() {
		return Collections.unmodifiableList(_inputArguments);
	}

	public String getLibraryPath() {
		return _libraryPath;
	}

	public String getManagementSpecVersion() {
		return _managementSpecVersion;
	}

	public String getName() {
		return _name;
	}

	public String getSpecName() {
		return _specName;
	}

	public String getSpecVendor() {
		return _specVendor;
	}

	public String getSpecVersion() {
		return _specVersion;
	}

	public long getStartTime() {
		return _startTime;
	}

	public Map<String, String> getSystemProperties() {
		return Collections.unmodifiableMap(_systemProperties);
	}

	public long getUptime() {
		return _uptime;
	}

	public String getVmName() {
		return _vmName;
	}

	public String getVmVendor() {
		return _vmVendor;
	}

	public String getVmVersion() {
		return _vmVersion;
	}

	public boolean isBootClassPathSupported() {
		return _bootClassPathSupported;
	}

	private static final long serialVersionUID = 1L;

	private final String _bootClassPath;
	private final boolean _bootClassPathSupported;
	private final String _classPath;
	private final List<String> _inputArguments;
	private final String _libraryPath;
	private final String _managementSpecVersion;
	private final String _name;
	private final String _specName;
	private final String _specVendor;
	private final String _specVersion;
	private final long _startTime;
	private final Map<String, String> _systemProperties;
	private final long _uptime;
	private final String _vmName;
	private final String _vmVendor;
	private final String _vmVersion;

}