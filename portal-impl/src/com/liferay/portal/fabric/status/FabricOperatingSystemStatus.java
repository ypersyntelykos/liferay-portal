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
import java.lang.management.OperatingSystemMXBean;

/**
 * @author Shuyang Zhou
 */
public class FabricOperatingSystemStatus
	extends BaseSingularFabricStatus<OperatingSystemMXBean> {

	public FabricOperatingSystemStatus() {
		super(ManagementFactory.getOperatingSystemMXBean());

		_osArchitecture = platformManagedObject.getArch();
		_osName = platformManagedObject.getName();
		_osVersion = platformManagedObject.getVersion();
		_availableProcessors = platformManagedObject.getAvailableProcessors();
		_systemLoadAverage = platformManagedObject.getSystemLoadAverage();
	}

	public int getAvailableProcessors() {
		return _availableProcessors;
	}

	public Long getExtCommittedVirtualMemorySize() {
		return (Long)attributes.get("CommittedVirtualMemorySize");
	}

	public Long getExtFreePhysicalMemorySize() {
		return (Long)attributes.get("FreePhysicalMemorySize");
	}

	public Long getExtFreeSwapSpaceSize() {
		return (Long)attributes.get("FreeSwapSpaceSize");
	}

	public Long getExtMaxFileDescriptorCount() {
		return (Long)attributes.get("MaxFileDescriptorCount");
	}

	public Long getExtOpenFileDescriptorCount() {
		return (Long)attributes.get("OpenFileDescriptorCount");
	}

	public Double getExtProcessCpuLoad() {
		return (Double)attributes.get("ProcessCpuLoad");
	}

	public Long getExtProcessCpuTime() {
		return (Long)attributes.get("ProcessCpuTime");
	}

	public Double getExtSystemCpuLoad() {
		return (Double)attributes.get("SystemCpuLoad");
	}

	public Long getExtTotalPhysicalMemorySize() {
		return (Long)attributes.get("TotalPhysicalMemorySize");
	}

	public Long getExtTotalSwapSpaceSize() {
		return (Long)attributes.get("TotalSwapSpaceSize");
	}

	public String getOsArchitecture() {
		return _osArchitecture;
	}

	public String getOsName() {
		return _osName;
	}

	public String getOsVersion() {
		return _osVersion;
	}

	public double getSystemLoadAverage() {
		return _systemLoadAverage;
	}

	private static final long serialVersionUID = 1L;

	private final int _availableProcessors;
	private final String _osArchitecture;
	private final String _osName;
	private final String _osVersion;
	private final double _systemLoadAverage;

}