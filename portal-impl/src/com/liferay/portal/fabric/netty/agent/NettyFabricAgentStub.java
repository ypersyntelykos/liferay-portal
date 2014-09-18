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

package com.liferay.portal.fabric.netty.agent;

import com.liferay.portal.fabric.agent.FabricAgent;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerStub;
import com.liferay.portal.fabric.status.FabricStatus;
import com.liferay.portal.fabric.status.JMXProxyUtil;
import com.liferay.portal.fabric.status.RemoteFabricStatus;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessConfig;
import com.liferay.portal.kernel.process.ProcessException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.Serializable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricAgentStub implements FabricAgent, Serializable {

	@Override
	public <T extends Serializable> FabricWorker<T> execute(
			ProcessConfig processConfig, ProcessCallable<T> processCallable)
		throws ProcessException {

		long id = _idGenerator.getAndIncrement();

		final NettyFabricWorkerStub<T> nettyStubFabricWorker =
			new NettyFabricWorkerStub<T>(
				id, _channel, processConfig, processCallable);

		_nettyStubFabricWorkers.put(id, nettyStubFabricWorker);

		ChannelFuture channelFuture = _channel.writeAndFlush(
			nettyStubFabricWorker);

		channelFuture.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture channelFuture) {
				if (channelFuture.isCancelled()) {
					Future<T> future =
						nettyStubFabricWorker.getProcessNoticeableFuture();

					future.cancel(true);
				}
				else if (!channelFuture.isSuccess()) {
					nettyStubFabricWorker.setException(channelFuture.cause());
				}
			}

		});

		return nettyStubFabricWorker;
	}

	@Override
	public FabricStatus getFabricStatus() {
		return new RemoteFabricStatus(
			JMXProxyUtil.toProcessCallableExecutor(_channel));
	}

	@Override
	public Collection<? extends FabricWorker<?>> getFabricWorkers() {
		return Collections.unmodifiableCollection(
			_nettyStubFabricWorkers.values());
	}

	public NettyFabricWorkerStub<?> getNettyStubFabricWorker(long id) {
		return _nettyStubFabricWorkers.get(id);
	}

	public void setChannel(Channel channel) {
		_channel = channel;
	}

	private static final long serialVersionUID = 1L;

	private volatile Channel _channel;
	private final AtomicLong _idGenerator = new AtomicLong();
	private final Map<Long, NettyFabricWorkerStub<?>> _nettyStubFabricWorkers =
		new ConcurrentHashMap<Long, NettyFabricWorkerStub<?>>();

}