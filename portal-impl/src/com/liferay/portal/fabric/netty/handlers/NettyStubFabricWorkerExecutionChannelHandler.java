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

import com.liferay.portal.fabric.netty.agent.NettyFabricAgent;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerResult;
import com.liferay.portal.fabric.netty.worker.NettyStubFabricWorker;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.FutureListener;

import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.Serializable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Shuyang Zhou
 */
public class NettyStubFabricWorkerExecutionChannelHandler
	extends SimpleChannelInboundHandler<NettyStubFabricWorker<Serializable>> {

	public NettyStubFabricWorkerExecutionChannelHandler(
		NettyFabricAgent nettyFabricAgent) {

		_nettyFabricAgent = nettyFabricAgent;
	}

	@Override
	protected void channelRead0(
			ChannelHandlerContext channelHandlerContext,
			NettyStubFabricWorker<Serializable> nettyStubFabricWorker)
		throws Exception {

		// TODO create the classloading processcallable, and assign the worker
		// id into it.

		FabricWorker<Serializable> fabricWorker =
			_nettyFabricAgent.execute(
				nettyStubFabricWorker.getProcessConfig(),
				nettyStubFabricWorker.getProcessCallable());

		final long id = nettyStubFabricWorker.getId();

		final Channel channel = channelHandlerContext.channel();

		// TODO need to handle the case that future has been cancelled

		NoticeableFuture<Serializable> noticeableFuture =
			fabricWorker.getProcessNoticeableFuture();

		noticeableFuture.addFutureListener(new FutureListener<Serializable>() {

			@Override
			public void complete(Future<Serializable> future) {
				NettyFabricWorkerResult nettyFabricWorkerResult = null;

				try {
					nettyFabricWorkerResult = new NettyFabricWorkerResult(
						id, future.get());
				}
				catch (ExecutionException ee) {
					nettyFabricWorkerResult = new NettyFabricWorkerResult(
						id, ee.getCause());
				}
				catch (InterruptedException ie) {
					nettyFabricWorkerResult = new NettyFabricWorkerResult(
						id, ie);
				}

				channel.writeAndFlush(nettyFabricWorkerResult);
			}

		});
	}

	private final NettyFabricAgent _nettyFabricAgent;

}