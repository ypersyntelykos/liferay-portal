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

import com.liferay.portal.fabric.netty.agent.NettyFabricAgentSkeleton;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentStub;
import com.liferay.portal.fabric.netty.rpc.ChannelThreadLocal;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerStub;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.Serializable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricWorkerStubExecutionChannelHandler
	extends SimpleChannelInboundHandler<NettyFabricWorkerStub<Serializable>> {

	public NettyFabricWorkerStubExecutionChannelHandler(
		NettyFabricAgentSkeleton nettyFabricAgentSkeleton) {

		_nettyFabricAgentSkeleton = nettyFabricAgentSkeleton;
	}

	@Override
	protected void channelRead0(
			ChannelHandlerContext channelHandlerContext,
			final NettyFabricWorkerStub<Serializable> nettyStubFabricWorker)
		throws Exception {

		FabricWorker<Serializable> fabricWorker =
			_nettyFabricAgentSkeleton.execute(
				nettyStubFabricWorker.getProcessConfig(),
				nettyStubFabricWorker.getProcessCallable());

		final long id = nettyStubFabricWorker.getId();

		NoticeableFuture<Serializable> noticeableFuture =
			fabricWorker.getProcessNoticeableFuture();

		noticeableFuture.addFutureListener(
			new FutureListener<Serializable>() {

				@Override
				public void complete(Future<Serializable> future) {
					FabricWorkerResultProcessCallable
						fabricWorkerResultProcessCallable = null;

					try {
						fabricWorkerResultProcessCallable =
							new FabricWorkerResultProcessCallable(
								id, future.get(), null);
					}
					catch (ExecutionException ee) {
						fabricWorkerResultProcessCallable =
							new FabricWorkerResultProcessCallable(
								id, null, ee.getCause());
					}
					catch (InterruptedException ie) {
						fabricWorkerResultProcessCallable =
							new FabricWorkerResultProcessCallable(id, null, ie);
					}

					// TODO check result?

					nettyStubFabricWorker.write(
						fabricWorkerResultProcessCallable);
				}

			});
	}

	private final NettyFabricAgentSkeleton _nettyFabricAgentSkeleton;

	private static class FabricWorkerResultProcessCallable
		implements ProcessCallable<Serializable> {

		public FabricWorkerResultProcessCallable(
			long id, Serializable result, Throwable throwable) {

			_id = id;
			_result = result;
			_throwable = throwable;
		}

		@Override
		public Serializable call() throws ProcessException {
			Channel channel = ChannelThreadLocal.getChannel();

			NettyFabricAgentStub nettyStubFabricAgent =
				NettyChannelAttributes.getNettyStubFabricAgent(channel);

			if (nettyStubFabricAgent == null) {
				throw new ProcessException(
					"Unable to locate fabric agent on channel " + channel);
			}

			NettyFabricWorkerStub<Serializable> nettyStubFabricWorker =
				(NettyFabricWorkerStub<Serializable>)
					nettyStubFabricAgent.getNettyStubFabricWorker(_id);

			if (nettyStubFabricWorker == null) {
				throw new ProcessException(
					"Unable to locate fabric worker on fabric agent " +
						nettyStubFabricAgent + ", with fabric worker id " +
							_id);
			}

			if (_throwable != null) {
				nettyStubFabricWorker.setException(_throwable);
			}
			else {
				nettyStubFabricWorker.setResult(_result);
			}

			return null;
		}

		private static final long serialVersionUID = 1L;

		private final long _id;
		private final Serializable _result;
		private final Throwable _throwable;
	}

}