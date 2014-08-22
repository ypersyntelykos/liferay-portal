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

package com.liferay.portal.fabric.netty.classloader.handlers;

import com.liferay.portal.fabric.netty.agent.NettyStubFabricAgent;
import com.liferay.portal.fabric.netty.classloader.ClassLoadingRequest;
import com.liferay.portal.fabric.netty.classloader.ClassLoadingResponse;
import com.liferay.portal.fabric.netty.handlers.NettyChannelAttributes;
import com.liferay.portal.fabric.netty.worker.NettyStubFabricWorker;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Shuyang Zhou
 */
public class ClassLoadingRequestChannelHandler
	extends SimpleChannelInboundHandler<ClassLoadingRequest> {

	@Override
	protected void channelRead0(
			ChannelHandlerContext channelHandlerContext,
			ClassLoadingRequest classLoadingRequest)
		throws Exception {

		NettyStubFabricAgent nettyStubFabricAgent =
			NettyChannelAttributes.getNettyStubFabricAgent(
				channelHandlerContext.channel());

		if (nettyStubFabricAgent == null) {
			_log.error(
				"Unable to locate fabric agent on channel " +
					channelHandlerContext.channel());

			return;
		}

		NettyStubFabricWorker<?> nettyStubFabricWorker =
			nettyStubFabricAgent.getNettyStubFabricWorker(
				classLoadingRequest.getFabrciWorkerId());

		if (nettyStubFabricWorker == null) {
			_log.error(
				"Unable to locate fabric worker on fabric agent " +
					nettyStubFabricAgent + ", with fabric worker id " +
						classLoadingRequest.getFabrciWorkerId());

			return;
		}

		ClassLoadingResponse classLoadingResponse = new ClassLoadingResponse(
			classLoadingRequest.getClassName(),
			nettyStubFabricWorker.getResource(
				classLoadingRequest.getResourceName()));

		channelHandlerContext.writeAndFlush(classLoadingResponse);
	}

	private static Log _log = LogFactoryUtil.getLog(
		ClassLoadingRequestChannelHandler.class);

}