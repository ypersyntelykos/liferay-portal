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
import com.liferay.portal.fabric.connection.FabricConnectionWatchDog;
import com.liferay.portal.fabric.local.agent.LocalFabricAgent;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentConfig;
import com.liferay.portal.fabric.netty.client.NettyFabricClientConfig;
import com.liferay.portal.fabric.netty.fileserver.handlers.FileResponseChannelHandler;
import com.liferay.portal.fabric.netty.handlers.NettyFabricWorkerExecutionChannelHandler;
import com.liferay.portal.fabric.repository.Repository;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ProcessExecutor;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.util.concurrent.EventExecutorGroup;

import java.net.InetSocketAddress;

import java.nio.file.Path;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricConnectionWatchDog implements FabricConnectionWatchDog {

	public NettyFabricConnectionWatchDog(
		Bootstrap bootstrap, InetSocketAddress inetSocketAddress,
		NettyFabricClientConfig nettyFabricClientConfig,
		Repository<Channel> repository,
		EventExecutorGroup executionEventExecutorGroup,
		EventExecutorGroup fileServerEventExecutorGroup,
		ProcessExecutor processExecutor,
		ConcurrentMap<InetSocketAddress, NettyFabricConnectionWatchDog>
			registrationMap, AtomicInteger activeConnectionCounter) {

		this.bootstrap = bootstrap;
		this.inetSocketAddress = inetSocketAddress;
		this.nettyFabricClientConfig = nettyFabricClientConfig;
		this.repository = repository;
		this.executionEventExecutorGroup = executionEventExecutorGroup;
		this.fileServerEventExecutorGroup = fileServerEventExecutorGroup;
		this.processExecutor = processExecutor;
		this.registrationMap = registrationMap;
		this.activeConnectionCounter = activeConnectionCounter;

		reconnectCounter.set(nettyFabricClientConfig.getReconnectCount());

		start();
	}

	@Override
	public FabricConnection getFabricConnection() {
		return fabricConnection;
	}

	@Override
	public InetSocketAddress getInetSocketAddress() {
		return inetSocketAddress;
	}

	@Override
	public synchronized ChannelFuture stop() {
		registrationMap.remove(inetSocketAddress, this);

		reconnectCounter.set(0);

		return channel.close();
	}

	protected final synchronized void start() {
		ChannelFuture channelFuture = bootstrap.connect(inetSocketAddress);

		channelFuture.addListener(new PostConnectChannelFutureListener());

		channel = channelFuture.channel();

		ChannelFuture closeChannelFuture = channel.closeFuture();

		closeChannelFuture.addListener(
			new PostDisconnectChannelFutureListener());
	}

	protected final AtomicInteger activeConnectionCounter;
	protected final Bootstrap bootstrap;
	protected Channel channel;
	protected final EventExecutorGroup executionEventExecutorGroup;
	protected volatile FabricConnection fabricConnection;
	protected final EventExecutorGroup fileServerEventExecutorGroup;
	protected final InetSocketAddress inetSocketAddress;
	protected final NettyFabricClientConfig nettyFabricClientConfig;
	protected final ProcessExecutor processExecutor;
	protected final AtomicInteger reconnectCounter = new AtomicInteger();
	protected final ConcurrentMap
		<InetSocketAddress, NettyFabricConnectionWatchDog> registrationMap;
	protected final Repository<Channel> repository;

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
						repository.getAsyncBroker(),
						fileServerEventExecutorGroup));

				FabricAgent fabricAgent = new LocalFabricAgent(processExecutor);

				channelPipeline.addLast(
					executionEventExecutorGroup,
					new NettyFabricWorkerExecutionChannelHandler(
						repository, fabricAgent,
						nettyFabricClientConfig.getExecutionTimeout()));

				Path repositoryPath = repository.getRepositoryPath();

				ChannelFuture registerChannelFuture = channel.writeAndFlush(
					new NettyFabricAgentConfig(repositoryPath.toFile()));

				registerChannelFuture.addListener(
					new PostRegisterChannelFutureListener(fabricAgent));

				return;
			}

			if (channelFuture.isCancelled()) {
				_log.error(
					"Cancelled connecting to " + channel.remoteAddress());

				return;
			}

			_log.error(
				"Unable to connect to " + channel.remoteAddress(),
				channelFuture.cause());
		}

	}

	protected class PostDisconnectChannelFutureListener
		implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture channelFuture) {
			Channel channel = channelFuture.channel();

			if (_log.isInfoEnabled()) {
				_log.info("Disconnected from " + channel.remoteAddress());
			}

			if (activeConnectionCounter.decrementAndGet() <= 0) {
				repository.dispose(true);
			}

			if (reconnectCounter.getAndDecrement() <= 0) {
				return;
			}

			EventLoop eventLoop = channel.eventLoop();

			eventLoop.schedule(
				new Runnable() {

					@Override
					public void run() {
						start();
					}

				},
				nettyFabricClientConfig.getReconnectInterval(),
				TimeUnit.MILLISECONDS);

			if (_log.isInfoEnabled()) {
				_log.info(
					"Try to reconnect " +
						nettyFabricClientConfig.getReconnectInterval() +
							"ms later");
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

				fabricConnection = new NettyFabricConnection(
					channel, fabricAgent,
					nettyFabricClientConfig.getExecutionTimeout());

				activeConnectionCounter.incrementAndGet();

				return;
			}

			if (channelFuture.isCancelled()) {
				_log.error(
					"Cancelled registering Netty fabric agent on " + channel);

				return;
			}

			_log.error(
				"Unable to register Netty fabric agent on " + channel,
				channelFuture.cause());
		}

		protected PostRegisterChannelFutureListener(FabricAgent fabricAgent) {
			this.fabricAgent = fabricAgent;
		}

		protected final FabricAgent fabricAgent;

	}

	private static final Log _log = LogFactoryUtil.getLog(
		NettyFabricConnectionWatchDog.class);

}