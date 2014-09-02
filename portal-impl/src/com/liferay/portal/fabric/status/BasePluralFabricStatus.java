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
import java.lang.management.PlatformManagedObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Shuyang Zhou
 */
public abstract class BasePluralFabricStatus<T extends PlatformManagedObject, V>
	implements FabricStatus {

	public BasePluralFabricStatus(Class<T> clazz) {
		List<T> platformManagedObjects = ManagementFactory.getPlatformMXBeans(
			clazz);

		_statusInfos = new ArrayList<V>(platformManagedObjects.size());

		for (T platformManagedObject : platformManagedObjects) {
			_statusInfos.add(convert(platformManagedObject));
		}
	}

	public List<V> getStatusInfos() {
		return Collections.unmodifiableList(_statusInfos);
	}

	protected abstract V convert(T platformManagedObject);

	private static final long serialVersionUID = 1L;

	protected final List<V> _statusInfos;

}