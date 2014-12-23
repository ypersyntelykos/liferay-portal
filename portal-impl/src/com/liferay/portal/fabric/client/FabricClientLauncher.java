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

import com.liferay.portal.fabric.netty.client.NettyFabricClient;
import com.liferay.portal.fabric.netty.client.NettyFabricClientConfig;
import com.liferay.portal.fabric.netty.client.NettyFabricClientShutdownCallback;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.local.LocalProcessExecutor;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;

import java.io.IOException;
import java.io.InputStream;

import java.net.InetSocketAddress;
import java.net.URL;

import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * @author Shuyang Zhou
 */
public class FabricClientLauncher {

	public static final String CUSTOM_CONFIG_KEY = "config";

	public static final String DEFAULT_CONFIG_FILE = "config.properties";

	public static final String EXT_CONFIG_FILE = "config-ext.properties";

	public static void main(String[] args) throws Exception {
		UUID uuid = UUID.randomUUID();

		Properties properties = loadProperties();

		NettyFabricClientConfig nettyFabricClientConfig =
			new NettyFabricClientConfig(uuid.toString(), properties);

		final CountDownLatch countDownLatch = new CountDownLatch(1);

		NettyFabricClient nettyFabricClient = new NettyFabricClient(
			new LocalProcessExecutor(), nettyFabricClientConfig,
			new NettyFabricClientShutdownCallback() {

				@Override
				public void shutdown() {
					countDownLatch.countDown();
				}

			});

		FabricClientUtil fabricClientUtil = new FabricClientUtil();

		fabricClientUtil.setFabricClient(nettyFabricClient);

		String portalFabricServerHost = GetterUtil.getString(
			properties.getProperty(PropsKeys.PORTAL_FABRIC_SERVER_HOST),
			"localhost");

		int portalFabricServerPort = GetterUtil.getInteger(
			properties.getProperty(PropsKeys.PORTAL_FABRIC_SERVER_PORT), 8923);

		FabricClientUtil.connect(
			new InetSocketAddress(
				portalFabricServerHost, portalFabricServerPort));

		countDownLatch.await();
	}

	protected static Properties loadProperties() throws IOException {
		Properties properties = loadProperties(DEFAULT_CONFIG_FILE, null);

		properties = loadProperties(EXT_CONFIG_FILE, properties);

		String customConfig = System.getProperty(CUSTOM_CONFIG_KEY);

		if (customConfig != null) {
			properties = loadProperties(customConfig, properties);
		}

		return properties;
	}

	protected static Properties loadProperties(
			String resourceName, Properties defaultProperties)
		throws IOException {

		Thread currentThread = Thread.currentThread();

		ClassLoader contextClassLoader = currentThread.getContextClassLoader();

		Enumeration<URL> enumeration = contextClassLoader.getResources(
			resourceName);

		Properties properties = new Properties(defaultProperties);

		while (enumeration.hasMoreElements()) {
			URL url = enumeration.nextElement();

			try {
				try (InputStream is = url.openStream()) {
					properties.load(is);
				}

				if (_log.isInfoEnabled()) {
					_log.info("Loading " + url);
				}
			}
			catch (IOException ioe) {
				if (_log.isDebugEnabled()) {
					_log.debug("Unable to load " + url);
				}
			}
		}

		return properties;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		FabricClientLauncher.class);

}