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

import com.liferay.portal.fabric.FabricResourceMappingVisitor;
import com.liferay.portal.fabric.OutputResource;
import com.liferay.portal.fabric.agent.FabricAgent;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerConfig;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerStub;
import com.liferay.portal.fabric.repository.Repository;
import com.liferay.portal.fabric.status.FabricStatus;
import com.liferay.portal.fabric.status.JMXProxyUtil;
import com.liferay.portal.fabric.status.RemoteFabricStatus;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessConfig;
import com.liferay.portal.kernel.process.ProcessException;
import com.liferay.portal.kernel.util.ObjectGraphUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.Serializable;

import java.nio.file.Path;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricAgentStub implements FabricAgent {

	public NettyFabricAgentStub(
		Channel channel, Repository repository, Path remoteRepositoryPath) {

		if (channel == null) {
			throw new NullPointerException("Channel is null");
		}

		if (repository == null) {
			throw new NullPointerException("Repository is null");
		}

		if (remoteRepositoryPath == null) {
			throw new NullPointerException("Remote repository path is null");
		}

		_channel = channel;
		_repository = repository;
		_remoteRepositoryPath = remoteRepositoryPath;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof NettyFabricAgentStub)) {
			return false;
		}

		NettyFabricAgentStub nettyFabricAgentStub = (NettyFabricAgentStub)obj;

		if (_channel.equals(nettyFabricAgentStub._channel)) {
			return true;
		}

		return false;
	}

	@Override
	public <T extends Serializable> FabricWorker<T> execute(
			ProcessConfig processConfig, ProcessCallable<T> processCallable)
		throws ProcessException {

		final long id = _idGenerator.getAndIncrement();

		FabricResourceMappingVisitor fabricResourceMappingVisitor =
			new FabricResourceMappingVisitor(
				OutputResource.class, _remoteRepositoryPath);

		ObjectGraphUtil.walkObjectGraph(
			processCallable, fabricResourceMappingVisitor);

		NettyFabricWorkerStub<T> nettyFabricWorkerStub =
			new NettyFabricWorkerStub<T>(
				_channel, _repository,
				fabricResourceMappingVisitor.getResourceMap());

		_nettyFabricWorkerStubs.put(id, nettyFabricWorkerStub);

		ChannelFuture channelFuture = _channel.writeAndFlush(
			new NettyFabricWorkerConfig<T>(id, processConfig, processCallable));

		channelFuture.addListener(
			new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture) {
					if (channelFuture.isSuccess()) {
						return;
					}

					NettyFabricWorkerStub<?> nettyFabricWorkerStub =
						takeNettyStubFabricWorker(id);

					if (channelFuture.isCancelled()) {
						Future<?> future =
							nettyFabricWorkerStub.getProcessNoticeableFuture();

						future.cancel(true);

						return;
					}

					nettyFabricWorkerStub.setException(channelFuture.cause());
				}

			});

		return nettyFabricWorkerStub;
	}

	@Override
	public FabricStatus getFabricStatus() {
		return new RemoteFabricStatus(
			JMXProxyUtil.toProcessCallableExecutor(_channel));
	}

	@Override
	public Collection<? extends FabricWorker<?>> getFabricWorkers() {
		return Collections.unmodifiableCollection(
			_nettyFabricWorkerStubs.values());
	}

	@Override
	public int hashCode() {
		return _channel.hashCode();
	}

	public NettyFabricWorkerStub<?> takeNettyStubFabricWorker(long id) {
		return _nettyFabricWorkerStubs.remove(id);
	}

	private final Channel _channel;
	private final AtomicLong _idGenerator = new AtomicLong();
	private final Map<Long, NettyFabricWorkerStub<?>>
		_nettyFabricWorkerStubs =
			new ConcurrentHashMap<Long, NettyFabricWorkerStub<?>>();
	private final Path _remoteRepositoryPath;
	private final Repository _repository;

}