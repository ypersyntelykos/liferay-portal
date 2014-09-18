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

import com.liferay.portal.fabric.FabricOutputResourceMappingVisitor;
import com.liferay.portal.fabric.agent.FabricAgent;
import com.liferay.portal.fabric.netty.fileserver.FileHelperUtil;
import com.liferay.portal.fabric.netty.repository.Repository;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerConfig;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerStub;
import com.liferay.portal.fabric.status.FabricStatus;
import com.liferay.portal.fabric.status.JMXProxyUtil;
import com.liferay.portal.fabric.status.RemoteFabricStatus;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.cluster.ClusterNode;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessConfig;
import com.liferay.portal.kernel.process.ProcessException;
import com.liferay.portal.kernel.util.ObjectGraphUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.File;
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
		Channel channel, Repository repository, File remoteRepositoryFolder) {

		_channel = channel;
		_repository = repository;
		_remoteRepositoryFolder = remoteRepositoryFolder;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ClusterNode)) {
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

		long id = _idGenerator.getAndIncrement();

		final NettyFabricWorkerStub<T> nettyFabricWorkerStub =
			new NettyFabricWorkerStub<T>(
				_channel, id, processConfig, processCallable);

		FabricOutputResourceMappingVisitor fabricOutputResourceMappingVisitor =
			new FabricOutputResourceMappingVisitor(_remoteRepositoryFolder);

		ObjectGraphUtil.walkObjectGraph(
			processCallable, fabricOutputResourceMappingVisitor);

		final Map<File, File> outputResourceMap =
			fabricOutputResourceMappingVisitor.getResourceMap();

		if (!outputResourceMap.isEmpty()) {
			NoticeableFuture<T> noticeableFuture =
				nettyFabricWorkerStub.getProcessNoticeableFuture();

			noticeableFuture.addFutureListener(
				new FutureListener<T>() {

					@Override
					public void complete(Future<T> future) {
						try {
							future.get();

							for (Map.Entry<File, File> entry :
									outputResourceMap.entrySet()) {

								File localFile = entry.getKey();
								File remoteFile = entry.getValue();

								Path repositoryFilePath = _repository.getFile(
									remoteFile.getAbsolutePath(), true);

								if (repositoryFilePath != null) {
									FileHelperUtil.move(
										repositoryFilePath, localFile.toPath());
								}
							}
						}
						catch (Exception e) {

							// TODO have to do exception handling here

						}
					}

				});
		}

		_nettyFabricWorkerStubs.put(id, nettyFabricWorkerStub);

		ChannelFuture channelFuture = _channel.writeAndFlush(
			new NettyFabricWorkerConfig<T>(id, processConfig, processCallable));

		channelFuture.addListener(
			new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture) {
					if (channelFuture.isCancelled()) {
						Future<T> future =
							nettyFabricWorkerStub.getProcessNoticeableFuture();

						future.cancel(true);
					}
					else if (!channelFuture.isSuccess()) {
						nettyFabricWorkerStub.setException(
							channelFuture.cause());
					}
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

	public NettyFabricWorkerStub<?> getNettyStubFabricWorker(long id) {
		return _nettyFabricWorkerStubs.get(id);
	}

	@Override
	public int hashCode() {
		return _channel.hashCode();
	}

	private final Channel _channel;
	private final AtomicLong _idGenerator = new AtomicLong();
	private final Map<Long, NettyFabricWorkerStub<?>>
		_nettyFabricWorkerStubs =
			new ConcurrentHashMap<Long, NettyFabricWorkerStub<?>>();
	private final File _remoteRepositoryFolder;
	private final Repository _repository;

}