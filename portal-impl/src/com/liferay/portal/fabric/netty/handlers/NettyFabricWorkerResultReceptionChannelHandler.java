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

import com.liferay.portal.fabric.netty.agent.NettyStubFabricAgent;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerResult;
import com.liferay.portal.fabric.netty.worker.NettyStubFabricWorker;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricWorkerResultReceptionChannelHandler
	extends SimpleChannelInboundHandler<NettyFabricWorkerResult> {

	@Override
	protected void channelRead0(
			ChannelHandlerContext channelHandlerContext,
			NettyFabricWorkerResult nettyFabricWorkerResult)
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

		NettyStubFabricWorker<Serializable> nettyStubFabricWorker =
			(NettyStubFabricWorker<Serializable>)
				nettyStubFabricAgent.getNettyStubFabricWorker(
					nettyFabricWorkerResult.getId());

		if (nettyStubFabricWorker == null) {
			_log.error(
				"Unable to locate fabric worker on fabric agent " +
					nettyStubFabricAgent + ", with fabric worker id " +
						nettyFabricWorkerResult.getId());

			return;
		}

		Exception exception = nettyFabricWorkerResult.getException();

		if (exception != null) {
			nettyStubFabricWorker.setException(exception);
		}
		else {
			nettyStubFabricWorker.setResult(
				nettyFabricWorkerResult.getResult());
		}
	}

	private static Log _log = LogFactoryUtil.getLog(
		NettyFabricWorkerResultReceptionChannelHandler.class);

}