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

import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.DefaultNoticeableFuture;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessConfig;
import com.liferay.portal.kernel.util.AggregateClassLoader;
import com.liferay.portal.util.ClassLoaderUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.Serializable;

import java.net.URL;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Shuyang Zhou
 */
public class NettyStubFabricWorker<T extends Serializable>
	implements FabricWorker<T>, Serializable {

	public NettyStubFabricWorker(
		long id, Channel channel, ProcessConfig processConfig,
		ProcessCallable<T> processCallable) {

		_id = id;
		_channel = channel;
		_processConfig = processConfig;
		_processCallable = processCallable;

		ChannelFuture channelFuture = _channel.closeFuture();

		channelFuture.addListener(_channelCloseListener);

		Class<?> clazz = processCallable.getClass();

		_resolvingClassLoader = AggregateClassLoader.getAggregateClassLoader(
			clazz.getClassLoader(),
			new ClassLoader[] {ClassLoaderUtil.getContextClassLoader()});

		_defaultNoticeableFuture.addFutureListener(new FutureListener<T>() {

			@Override
			public void complete(Future<T> future) {
				ChannelFuture channelFuture = _channel.closeFuture();

				channelFuture.removeListener(_channelCloseListener);
			}

		});
	}

	@Override
	public boolean addFutureListener(FutureListener<T> futureListener) {
		return _defaultNoticeableFuture.addFutureListener(futureListener);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return _defaultNoticeableFuture.cancel(mayInterruptIfRunning);
	}

	@Override
	public T get() throws ExecutionException, InterruptedException {
		return _defaultNoticeableFuture.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit)
		throws ExecutionException, InterruptedException, TimeoutException {

		return _defaultNoticeableFuture.get(timeout, unit);
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

	public URL getResource(String resourceName) {
		return _resolvingClassLoader.getResource(resourceName);
	}

	@Override
	public boolean isCancelled() {
		return _defaultNoticeableFuture.isCancelled();
	}

	@Override
	public boolean isDone() {
		return _defaultNoticeableFuture.isDone();
	}

	@Override
	public boolean removeFutureListener(FutureListener<T> futureListener) {
		return _defaultNoticeableFuture.removeFutureListener(futureListener);
	}

	public void setException(Throwable t) {
		_defaultNoticeableFuture.setException(t);
	}

	public void setResult(T t) {
		_defaultNoticeableFuture.set(t);
	}

	// TODO, all sorts of process status query support

	private final transient Channel _channel;
	private final transient ChannelFutureListener _channelCloseListener =
		new ChannelFutureListener() {

		@Override
		public void operationComplete(ChannelFuture channelFuture) {
			if (channelFuture.isDone() && !_defaultNoticeableFuture.isDone()) {
				_defaultNoticeableFuture.cancel(true);
			}
		}
	};

	private final transient DefaultNoticeableFuture<T>
		_defaultNoticeableFuture = new DefaultNoticeableFuture<T>();
	private final long _id;
	private final ProcessCallable<T> _processCallable;
	private final ProcessConfig _processConfig;
	private final transient ClassLoader _resolvingClassLoader;

}