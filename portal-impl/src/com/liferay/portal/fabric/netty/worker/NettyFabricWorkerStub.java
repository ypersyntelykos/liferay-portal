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
import com.liferay.portal.fabric.status.FabricStatus;
import com.liferay.portal.fabric.status.JMXProxyUtil;
import com.liferay.portal.fabric.status.RemoteFabricStatus;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.DefaultNoticeableFuture;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessConfig;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.Serializable;

import java.util.concurrent.Future;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricWorkerStub<T extends Serializable>
	implements FabricWorker<T>, Serializable {

	public NettyFabricWorkerStub(
		long id, Channel channel, ProcessConfig processConfig,
		ProcessCallable<T> processCallable) {

		_id = id;
		_channel = channel;
		_processConfig = processConfig;
		_processCallable = processCallable;

		ChannelFuture channelFuture = _channel.closeFuture();

		channelFuture.addListener(_channelCloseListener);

		_defaultNoticeableFuture.addFutureListener(
			new FutureListener<T>() {

				@Override
				public void complete(Future<T> future) {
					ChannelFuture channelFuture = _channel.closeFuture();

					channelFuture.removeListener(_channelCloseListener);
				}

			});
	}

	@Override
	public FabricStatus getFabricStatus() {
		return new RemoteFabricStatus(
			JMXProxyUtil.toProcessCallableExecutor(_channel));
	}

	public long getId() {
		return _id;
	}

	public ProcessCallable<T> getProcessCallable() {
		return _processCallable;
	}

	public ProcessConfig getProcessConfig() {
		return _processConfig;
	}

	@Override
	public NoticeableFuture<T> getProcessNoticeableFuture() {
		return _defaultNoticeableFuture;
	}

	public void setException(Throwable t) {
		_defaultNoticeableFuture.setException(t);
	}

	public void setResult(T t) {
		_defaultNoticeableFuture.set(t);
	}

	@Override
	public <V extends Serializable> NoticeableFuture<V> write(
		ProcessCallable<V> processCallable) {

		return RPCUtil.execute(_channel, processCallable);
	}

	private final transient Channel _channel;

	private final transient ChannelFutureListener _channelCloseListener =
		new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture channelFuture) {
				if (channelFuture.isDone() &&
					!_defaultNoticeableFuture.isDone()) {

					_defaultNoticeableFuture.cancel(true);
				}
			}

		};

	private final transient DefaultNoticeableFuture<T>
		_defaultNoticeableFuture = new DefaultNoticeableFuture<T>();
	private final long _id;
	private final ProcessCallable<T> _processCallable;
	private final ProcessConfig _processConfig;

}