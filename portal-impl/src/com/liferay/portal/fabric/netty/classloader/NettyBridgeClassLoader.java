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

package com.liferay.portal.fabric.netty.classloader;

import com.liferay.portal.fabric.netty.classloader.handlers.ClassLoadingResponseChannelHandler;
import com.liferay.portal.kernel.util.ReflectionUtil;
import com.liferay.portal.kernel.util.SystemProperties;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;

import java.io.File;

import java.lang.reflect.Method;

import java.net.SocketAddress;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Shuyang Zhou
 */
public class NettyBridgeClassLoader extends URLClassLoader {

	public NettyBridgeClassLoader(
		long fabricWorkerId, ClassLoader classLoader, Channel channel) {

		super(new URL[0], classLoader);

		_fabricWorkerId = fabricWorkerId;
		_channel = channel;

		SocketAddress socketAddress = channel.localAddress();

		_libDir = new File(
			new File(
				SystemProperties.get(SystemProperties.TMP_DIR),
				socketAddress.toString()),
			String.valueOf(fabricWorkerId));
	}

	@Override
	public void addURL(URL url) {
		super.addURL(url);
	}

	public Class<?> defineClass(String name, byte[] data) throws Exception {
		return (Class<?>)_DEFINE_CLASS.invoke(this, name, data, 0, data.length);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			ClassLoadingResponseChannelHandler
				classLoadingResponseChannelHandler =
					new ClassLoadingResponseChannelHandler();

			ChannelPipeline channelPipeline = _channel.pipeline();

			channelPipeline.addLast(classLoadingResponseChannelHandler);

			_channel.writeAndFlush(
				new ClassLoadingRequest(_fabricWorkerId, name));

			ClassLoadingResponse classLoadingResponse = null;

			try {
				classLoadingResponse =
					classLoadingResponseChannelHandler.getClassLoadingResponse(
						ClassLoadingResponse.FILE_TOKEN_TTL);
			}
			finally {
				channelPipeline.remove(classLoadingResponseChannelHandler);
			}

			return classLoadingResponse.loadClass(this, _channel, _libDir);
		}
		catch (Exception e) {
			throw new ClassNotFoundException(
				"Unable to load class from remote " + name, e);
		}
	}

	private static final Method _DEFINE_CLASS;

	static {
		try {
			_DEFINE_CLASS = ReflectionUtil.getDeclaredMethod(
				ClassLoader.class, "defineClass", String.class, byte[].class,
				int.class, int.class);
		}
		catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private final Channel _channel;
	private final long _fabricWorkerId;
	private final File _libDir;

}