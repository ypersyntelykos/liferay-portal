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

package com.liferay.portal.fabric.client;

import com.liferay.portal.fabric.connection.FabricConnectionWatchDog;
import com.liferay.portal.kernel.security.pacl.permission.PortalRuntimePermission;

import java.net.InetSocketAddress;

import java.util.Map;

/**
 * @author Shuyang Zhou
 */
public class FabricClientUtil {

	public static FabricConnectionWatchDog connect(
		InetSocketAddress inetSocketAddress) {

		return getFabricClient().connect(inetSocketAddress);
	}

	public static FabricClient getFabricClient() {
		PortalRuntimePermission.checkGetBeanProperty(FabricClientUtil.class);

		return _fabricClient;
	}

	public static Map<InetSocketAddress, FabricConnectionWatchDog>
		getFabricConnectionWatchDogs() {

		return getFabricClient().getFabricConnectionWatchDogs();
	}

	public static void shutdown() {
		getFabricClient().shutdown();
	}

	public void setFabricClient(FabricClient fabricClient) {
		PortalRuntimePermission.checkSetBeanProperty(getClass());

		_fabricClient = fabricClient;
	}

	private static FabricClient _fabricClient;

}