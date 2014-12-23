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

package com.liferay.portal.fabric.netty.connection;

import com.liferay.portal.fabric.agent.FabricAgent;
import com.liferay.portal.fabric.connection.FabricConnection;
import com.liferay.portal.fabric.connection.FabricConnector;
import com.liferay.portal.fabric.local.agent.LocalFabricAgent;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentConfig;
import com.liferay.portal.fabric.netty.client.NettyFabricClientConfig;
import com.liferay.portal.fabric.netty.fileserver.handlers.FileResponseChannelHandler;
import com.liferay.portal.fabric.netty.handlers.NettyFabricWorkerExecutionChannelHandler;
import com.liferay.portal.fabric.repository.Repository;
import com.liferay.portal.kernel.concurrent.BaseFutureListener;
import com.liferay.portal.kernel.concurrent.DefaultNoticeableFuture;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ProcessExecutor;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;

import java.nio.file.Path;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricConnector implements FabricConnector {

	public NettyFabricConnector(
		Bootstrap bootstrap, InetSocketAddress inetSocketAddress,
		NettyFabricClientConfig nettyFabricClientConfig,
		Repository<Channel> repository,
		EventExecutorGroup executionEventExecutorGroup,
		EventExecutorGroup fileServerEventExecutorGroup,
		ProcessExecutor processExecutor) {

		_bootstrap = bootstrap;
		_inetSocketAddress = inetSocketAddress;
		_nettyFabricClientConfig = nettyFabricClientConfig;
		_repository = repository;
		_executionEventExecutorGroup = executionEventExecutorGroup;
		_fileServerEventExecutorGroup = fileServerEventExecutorGroup;
		_processExecutor = processExecutor;

		resetReconnectCounter();
	}

	@Override
	public NoticeableFuture<FabricConnection> connect() {
		DefaultNoticeableFuture<FabricConnection> defaultNoticeableFuture =
			new DefaultNoticeableFuture<FabricConnection>();

		ChannelFuture channelFuture = _bootstrap.connect(_inetSocketAddress);

		channelFuture.addListener(
			new PostConnectChannelFutureListener(defaultNoticeableFuture));

		defaultNoticeableFuture.addFutureListener(
			new ReconnectFutureListener());

		return defaultNoticeableFuture;
	}

	protected static void setErrorStatus(
		DefaultNoticeableFuture<FabricConnection> defaultNoticeableFuture,
		ChannelFuture channelFuture) {

		if (channelFuture.isCancelled()) {
			defaultNoticeableFuture.cancel(true);

			return;
		}

		Throwable throwable = channelFuture.cause();

		if (throwable != null) {
			defaultNoticeableFuture.setException(throwable);
		}
	}

	protected void reconnect() {
		if (_reconnectCounter.getAndDecrement() <= 0) {
			return;
		}

		EventLoopGroup eventLoopGroup = _bootstrap.group();

		eventLoopGroup.schedule(
			new Runnable() {

				@Override
				public void run() {
					connect();
				}

			},
			_nettyFabricClientConfig.getReconnectInterval(),
			TimeUnit.MILLISECONDS);

		if (_log.isInfoEnabled()) {
			_log.info(
				"Try to reconnect " +
					_nettyFabricClientConfig.getReconnectInterval() +
						"ms later");
		}
	}

	protected final void resetReconnectCounter() {
		int reconnectCount = _nettyFabricClientConfig.getReconnectCount();

		if (reconnectCount < 0) {
			reconnectCount = Integer.MAX_VALUE;
		}

		_reconnectCounter.set(reconnectCount);
	}

	protected class PostConnectChannelFutureListener
		implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture channelFuture) {
			Channel channel = channelFuture.channel();

			if (channelFuture.isSuccess()) {
				if (_log.isInfoEnabled()) {
					_log.info("Connected to " + channel.remoteAddress());
				}

				ChannelPipeline channelPipeline = channel.pipeline();

				channelPipeline.addLast(
					new FileResponseChannelHandler(
						_repository.getAsyncBroker(),
						_fileServerEventExecutorGroup));

				FabricAgent fabricAgent = new LocalFabricAgent(
					_processExecutor);

				channelPipeline.addLast(
					_executionEventExecutorGroup,
					new NettyFabricWorkerExecutionChannelHandler(
						_repository, fabricAgent,
						_nettyFabricClientConfig.getExecutionTimeout()));

				Path repositoryPath = _repository.getRepositoryPath();

				ChannelFuture registerChannelFuture = channel.writeAndFlush(
					new NettyFabricAgentConfig(repositoryPath.toFile()));

				registerChannelFuture.addListener(
					new PostRegisterChannelFutureListener(
						_defaultNoticeableFuture, fabricAgent));

				return;
			}

			if (channelFuture.isCancelled()) {
				_log.error(
					"Cancelled connecting to " + channel.remoteAddress());
			}
			else {
				_log.error(
					"Unable to connect to " + channel.remoteAddress(),
					channelFuture.cause());
			}

			setErrorStatus(_defaultNoticeableFuture, channelFuture);
		}

		protected PostConnectChannelFutureListener(
			DefaultNoticeableFuture<FabricConnection> defaultNoticeableFuture) {

			_defaultNoticeableFuture = defaultNoticeableFuture;
		}

		private final DefaultNoticeableFuture<FabricConnection>
			_defaultNoticeableFuture;

	}

	protected class PostDisconnectChannelFutureListener
		implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture channelFuture) {
			Channel channel = channelFuture.channel();

			if (_log.isInfoEnabled()) {
				_log.info("Disconnected from " + channel.remoteAddress());
			}
		}

	}

	protected class PostRegisterChannelFutureListener
		implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture channelFuture) {
			Channel channel = channelFuture.channel();

			if (channelFuture.isSuccess()) {
				if (_log.isInfoEnabled()) {
					_log.info("Registered Netty fabric agent on " + channel);
				}

				_defaultNoticeableFuture.set(
					new NettyFabricConnection(
						channel, _fabricAgent,
						_nettyFabricClientConfig.getExecutionTimeout()));

				channelFuture = channel.closeFuture();

				channelFuture.addListener(
					new PostDisconnectChannelFutureListener());

				return;
			}

			_log.error("Unable to register Netty fabric agent on " + channel);

			setErrorStatus(_defaultNoticeableFuture, channelFuture);
		}

		protected PostRegisterChannelFutureListener(
			DefaultNoticeableFuture<FabricConnection> defaultNoticeableFuture,
			FabricAgent fabricAgent) {

			_defaultNoticeableFuture = defaultNoticeableFuture;
			_fabricAgent = fabricAgent;
		}

		private final DefaultNoticeableFuture<FabricConnection>
			_defaultNoticeableFuture;
		private final FabricAgent _fabricAgent;

	}

	protected class ReconnectFutureListener
		extends BaseFutureListener<FabricConnection> {

		@Override
		public void completeWithCancel(Future<FabricConnection> future) {
			reconnect();
		}

		@Override
		public void completeWithException(
			Future<FabricConnection> future, Throwable throwable) {

			reconnect();
		}

		@Override
		public void completeWithResult(
			Future<FabricConnection> future,
			FabricConnection fabricConnection) {

			resetReconnectCounter();

			NoticeableFuture<Void> noticeableFuture =
				fabricConnection.disconnectNoticeableFuture();

			noticeableFuture.addFutureListener(
				new FutureListener<Void>() {

					@Override
					public void complete(Future<Void> future) {
						reconnect();
					}

				});
		}

	}

	private static final Log _log = LogFactoryUtil.getLog(
		NettyFabricConnector.class);

	private final Bootstrap _bootstrap;
	private final EventExecutorGroup _executionEventExecutorGroup;
	private final EventExecutorGroup _fileServerEventExecutorGroup;
	private final InetSocketAddress _inetSocketAddress;
	private final NettyFabricClientConfig _nettyFabricClientConfig;
	private final ProcessExecutor _processExecutor;
	private final AtomicInteger _reconnectCounter = new AtomicInteger();
	private final Repository<Channel> _repository;

}