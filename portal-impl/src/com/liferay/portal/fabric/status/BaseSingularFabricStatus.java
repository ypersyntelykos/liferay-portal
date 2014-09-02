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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;


import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author Shuyang Zhou
 */
public abstract class BaseSingularFabricStatus<T extends PlatformManagedObject>
	implements FabricStatus {

	public BaseSingularFabricStatus(T platformManagedObject) {
		this.platformManagedObject = platformManagedObject;

		objectName = platformManagedObject.getObjectName();

		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

		MBeanInfo mBeanInfo = null;

		try {
			mBeanInfo = mBeanServer.getMBeanInfo(objectName);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn("Unable to get MBeanInfo for " + objectName);
			}

			return;
		}

		for (MBeanAttributeInfo mBeanAttributeInfo :
				mBeanInfo.getAttributes()) {

			if (!mBeanAttributeInfo.isReadable()) {
				continue;
			}

			try {
				attributes.put(
					mBeanAttributeInfo.getName(),
					mBeanServer.getAttribute(
						objectName, mBeanAttributeInfo.getName()));
			}
			catch (Exception e) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to get attribute " +
							mBeanAttributeInfo.getName() + " from " +
								objectName, e);
				}
			}
		}
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public Map<String, Object> getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}

	private static final long serialVersionUID = 1L;

	private static Log _log = LogFactoryUtil.getLog(BaseSingularFabricStatus.class);

	protected final Map<String, Object> attributes =
		new HashMap<String, Object>();

	protected final T platformManagedObject;
	protected final ObjectName objectName;

}