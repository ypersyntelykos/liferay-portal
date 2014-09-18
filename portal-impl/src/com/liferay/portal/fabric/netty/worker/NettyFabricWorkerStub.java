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

package com.liferay.portal.fabric.netty.worker;

import com.liferay.portal.fabric.netty.rpc.RPCUtil;
import com.liferay.portal.fabric.repository.Repository;
import com.liferay.portal.fabric.status.FabricStatus;
import com.liferay.portal.fabric.status.JMXProxyUtil;
import com.liferay.portal.fabric.status.RemoteFabricStatus;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.DefaultNoticeableFuture;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.process.ProcessCallable;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.Serializable;

import java.nio.file.Path;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricWorkerStub<T extends Serializable>
	implements FabricWorker<T> {

	public NettyFabricWorkerStub(
		Channel channel, Repository repository,
		Map<Path, Path> outputResourceMap) {

		if (channel == null) {
			throw new NullPointerException("Channel is null");
		}

		if (repository == null) {
			throw new NullPointerException("Repository is null");
		}

		if (outputResourceMap == null) {
			throw new NullPointerException("Output resource map is null");
		}

		_channel = channel;
		_repository = repository;
		_outputResourceMap = outputResourceMap;

		final ChannelFuture channelFuture = _channel.closeFuture();

		final ChannelFutureListener channelCloseListener =
			new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture) {
					_defaultNoticeableFuture.cancel(true);
				}

			};

		channelFuture.addListener(channelCloseListener);

		_defaultNoticeableFuture.addFutureListener(
			new FutureListener<T>() {

				@Override
				public void complete(Future<T> future) {
					channelFuture.removeListener(channelCloseListener);
				}

			});
	}

	@Override
	public FabricStatus getFabricStatus() {
		return new RemoteFabricStatus(
			JMXProxyUtil.toProcessCallableExecutor(_channel));
	}

	@Override
	public NoticeableFuture<T> getProcessNoticeableFuture() {
		return _defaultNoticeableFuture;
	}

	public void setException(Throwable t) {
		_defaultNoticeableFuture.setException(t);
	}

	public void setResult(final T result) {
		NoticeableFuture<Map<Path, Path>> noticeableFuture =
			_repository.getFiles(_outputResourceMap, true);

		noticeableFuture.addFutureListener(
			new FutureListener<Map<Path, Path>>() {

				@Override
				public void complete(Future<Map<Path, Path>> future) {
					if (future.isCancelled()) {
						_defaultNoticeableFuture.cancel(true);

						return;
					}

					try {
						future.get();

						_defaultNoticeableFuture.set(result);
					}
					catch (Throwable t) {
						if (t instanceof ExecutionException) {
							t = t.getCause();
						}

						_defaultNoticeableFuture.setException(t);
					}
				}

			});
	}

	@Override
	public <V extends Serializable> NoticeableFuture<V> write(
		ProcessCallable<V> processCallable) {

		return RPCUtil.execute(_channel, processCallable);
	}

	private final Channel _channel;
	private final DefaultNoticeableFuture<T> _defaultNoticeableFuture =
		new DefaultNoticeableFuture<T>();
	private final Map<Path, Path> _outputResourceMap;
	private final Repository _repository;

}