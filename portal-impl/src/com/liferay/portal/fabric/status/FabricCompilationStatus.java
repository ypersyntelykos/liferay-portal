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

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

/**
 * @author Shuyang Zhou
 */
public class FabricCompilationStatus
	extends BaseSingularFabricStatus<CompilationMXBean> {


	public FabricCompilationStatus() {
		super(ManagementFactory.getCompilationMXBean());

		_compilationTimeMonitoringSupported =
			platformManagedObject.isCompilationTimeMonitoringSupported();
		_name = platformManagedObject.getName();
		_totalCompilationTime = platformManagedObject.getTotalCompilationTime();
	}

	public boolean isCompilationTimeMonitoringSupported() {
		return _compilationTimeMonitoringSupported;
	}

	public String getName() {
		return _name;
	}

	public long getTotalCompilationTime() {
		return _totalCompilationTime;
	}

	private static final long serialVersionUID = 1L;

	private final boolean _compilationTimeMonitoringSupported;
	private final String _name;
	private final long _totalCompilationTime;
	
}
