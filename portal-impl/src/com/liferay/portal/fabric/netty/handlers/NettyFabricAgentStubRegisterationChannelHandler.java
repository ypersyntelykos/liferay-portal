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
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentStub;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricAgentStubRegisterationChannelHandler
	extends SimpleChannelInboundHandler<NettyFabricAgentStub> {

	public NettyFabricAgentStubRegisterationChannelHandler(
		FabricAgentRegistry fabricAgentRegistry) {

		_fabricAgentRegistry = fabricAgentRegistry;
	}

	@Override
	public void channelInactive(ChannelHandlerContext channelHandlerContext) {
		NettyFabricAgentStub nettyFabricAgentStub =
			NettyChannelAttributes.getNettyStubFabricAgent(
				channelHandlerContext.channel());

		if (nettyFabricAgentStub == null) {
			_log.error(
				"Unable to locate fabric agent on channel " +
					channelHandlerContext.channel());

			return;
		}

		if (_fabricAgentRegistry.unregisterFabricAgent(nettyFabricAgentStub)) {
			if (_log.isInfoEnabled()) {
				_log.info("Unregistered fabric agent " + nettyFabricAgentStub);
			}
		}
		else if (_log.isWarnEnabled()) {
			_log.warn(
				"Unable to unregister fabric agent " + nettyFabricAgentStub);
		}
	}

	@Override
	protected void channelRead0(
		ChannelHandlerContext channelHandlerContext,
		final NettyFabricAgentStub nettyStubFabricAgent) {

		Channel channel = channelHandlerContext.channel();

		nettyStubFabricAgent.setChannel(channel);

		if (_fabricAgentRegistry.registerFabricAgent(nettyStubFabricAgent)) {
			if (_log.isInfoEnabled()) {
				_log.info("Registered fabric agent " + nettyStubFabricAgent);
			}

			NettyChannelAttributes.setNettyStubFabricAgent(
				channel, nettyStubFabricAgent);

			return;
		}

		ChannelFuture channelFuture = channelHandlerContext.close();

		channelFuture.addListener(
			new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture) {
					if (channelFuture.isSuccess()) {
						if (_log.isWarnEnabled()) {
							_log.warn(
								"Rejected duplicated fabric agent " +
									nettyStubFabricAgent);
						}
					}
					else {
						_log.error(
							"Unable to reject duplicated fabric agent " +
								nettyStubFabricAgent,
							channelFuture.cause());
					}
				}

			});
	}

	private static Log _log = LogFactoryUtil.getLog(
		NettyFabricAgentStubRegisterationChannelHandler.class);

	private final FabricAgentRegistry _fabricAgentRegistry;

}