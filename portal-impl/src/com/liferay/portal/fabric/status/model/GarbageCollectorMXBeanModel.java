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

import java.lang.management.GarbageCollectorMXBean;

/**
 * @author Shuyang Zhou
 */
public class GarbageCollectorMXBeanModel extends MemoryManagerMXBeanModel {

	public GarbageCollectorMXBeanModel(
		GarbageCollectorMXBean garbageCollectorMXBean) {

		super(garbageCollectorMXBean);

		_collectionCount = garbageCollectorMXBean.getCollectionCount();
		_collectionTime = garbageCollectorMXBean.getCollectionTime();
	}

	public long getCollectionCount() {
		return _collectionCount;
	}

	public long getCollectionTime() {
		return _collectionTime;
	}

	private static final long serialVersionUID = 1L;

	private final long _collectionCount;
	private final long _collectionTime;

}
