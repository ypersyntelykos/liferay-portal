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

package com.liferay.simple.socks.proxy;

import com.liferay.portal.kernel.process.local.LocalProcessExecutor;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Tom Wang
 */
@Component(
	configurationPid = "com.liferay.simple.socks.proxy.SocksProxyConfiguration"
)
public class SimpleSocksProxy {

	@Activate
	protected void activate(Map<String, Object> properties) throws Exception {
		String allowedHostnames = GetterUtil.getString(
			properties.get("allowedHostnames"), "");

		int executorServiceAwaitTimeout = GetterUtil.getInteger(
			properties.get("executorServiceAwaitTimeout"), 10);

		int serverSocketPort = GetterUtil.getInteger(
			properties.get("serverSocketPort"), 8888);

		List<String> allowedIPAddresses = new ArrayList<>();

		for (String allowedHostname : StringUtil.split(allowedHostnames)) {
			InetAddress inetAddress = InetAddress.getByName(allowedHostname);

			allowedIPAddresses.add(inetAddress.getHostAddress());
		}

		_socksProxyInitializer = new SocksProxyInitializer(
			_localProcessExecutor);

		_socksProxyInitializer.start(
			allowedIPAddresses, executorServiceAwaitTimeout, serverSocketPort);
	}

	@Deactivate
	protected void deactivate() throws Exception {
		_socksProxyInitializer.stop();
	}

	@Reference
	private LocalProcessExecutor _localProcessExecutor;

	private SocksProxyInitializer _socksProxyInitializer;

}