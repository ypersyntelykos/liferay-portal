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

package com.liferay.portal.fabric.netty.handlers;

import com.liferay.portal.fabric.agent.FabricAgentRegistry;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentConfig;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentStub;
import com.liferay.portal.fabric.netty.repository.NettyRepository;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.file.Path;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricAgentExecutionChannelHandler
	extends SimpleChannelInboundHandler<NettyFabricAgentConfig> {

	public NettyFabricAgentExecutionChannelHandler(
		FabricAgentRegistry fabricAgentRegistry, Path repositoryParentPath,
		EventExecutorGroup eventExecutorGroup, long getFileTimeout) {

		_fabricAgentRegistry = fabricAgentRegistry;
		_repositoryParentPath = repositoryParentPath;
		_eventExecutorGroup = eventExecutorGroup;
		_getFileTimeout = getFileTimeout;
	}

	@Override
	protected void channelRead0(
		ChannelHandlerContext channelHandlerContext,
		NettyFabricAgentConfig nettyFabricAgentConfig) {

		final Channel channel = channelHandlerContext.channel();

		final NettyFabricAgentStub nettyFabricAgentStub =
			new NettyFabricAgentStub(
				channel,
				new NettyRepository(
					_repositoryParentPath, channel, _eventExecutorGroup,
					_getFileTimeout),
				nettyFabricAgentConfig.getRepositoryPath());

		if (!_fabricAgentRegistry.registerFabricAgent(nettyFabricAgentStub)) {
			if (_log.isWarnEnabled()) {
				_log.warn("Rejected duplicated fabric agent on " + channel);
			}

			return;
		}

		if (_log.isInfoEnabled()) {
			_log.info("Registered fabric agent on " + channel);
		}

		NettyChannelAttributes.setNettyFabricAgentStub(
			channel, nettyFabricAgentStub);

		ChannelFuture channelFuture = channel.closeFuture();

		channelFuture.addListener(
			new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture) {
					if (_fabricAgentRegistry.unregisterFabricAgent(
							nettyFabricAgentStub)) {

						if (_log.isInfoEnabled()) {
							_log.info(
								"Unregistered fabric agent on " + channel);
						}
					}
					else if (_log.isWarnEnabled()) {
						_log.warn(
							"Unable to unregister fabric agent on " + channel);
					}
				}

			});
	}

	private static Log _log = LogFactoryUtil.getLog(
		NettyFabricAgentExecutionChannelHandler.class);

	private final EventExecutorGroup _eventExecutorGroup;
	private final FabricAgentRegistry _fabricAgentRegistry;
	private final long _getFileTimeout;
	private final Path _repositoryParentPath;

}