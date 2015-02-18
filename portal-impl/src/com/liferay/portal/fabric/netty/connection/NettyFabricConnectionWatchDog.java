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

import com.liferay.portal.fabric.connection.FabricConnection;
import com.liferay.portal.fabric.connection.FabricConnectionWatchDog;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentConfig;
import com.liferay.portal.fabric.netty.client.NettyFabricClientConfig;
import com.liferay.portal.fabric.netty.handlers.NettyChannelAttributes;
import com.liferay.portal.fabric.repository.Repository;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;

import java.net.SocketAddress;

import java.nio.file.Path;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricConnectionWatchDog implements FabricConnectionWatchDog {

	public NettyFabricConnectionWatchDog(
		Bootstrap bootstrap, SocketAddress socketAddress,
		NettyFabricClientConfig nettyFabricClientConfig,
		Repository<Channel> repository,
		ConcurrentMap<SocketAddress, NettyFabricConnectionWatchDog>
			registrationMap,
		AtomicInteger activeConnectionCounter) {

		this.bootstrap = bootstrap;
		this.socketAddress = socketAddress;
		this.nettyFabricClientConfig = nettyFabricClientConfig;
		this.repository = repository;
		this.registrationMap = registrationMap;
		this.activeConnectionCounter = activeConnectionCounter;

		reconnectCounter.set(nettyFabricClientConfig.getReconnectCount());

		start();
	}

	@Override
	public FabricConnection getFabricConnection() {
		return nettyFabricConnection;
	}

	@Override
	public SocketAddress getSocketAddress() {
		return socketAddress;
	}

	@Override
	public synchronized NettyFabricConnection stopWatching() {
		registrationMap.remove(socketAddress, this);

		reconnectCounter.set(0);

		return nettyFabricConnection;
	}

	protected final synchronized void start() {
		ChannelFuture channelFuture = bootstrap.connect(socketAddress);

		channelFuture.addListener(new PostConnectChannelFutureListener());

		Channel channel = channelFuture.channel();

		NettyChannelAttributes.setFabricConnectionWatchDog(channel, this);

		ChannelFuture closeChannelFuture = channel.closeFuture();

		closeChannelFuture.addListener(
			new PostDisconnectReconnectChannelFutureListener());
	}

	protected final AtomicInteger activeConnectionCounter;
	protected final Bootstrap bootstrap;
	protected final NettyFabricClientConfig nettyFabricClientConfig;
	protected volatile NettyFabricConnection nettyFabricConnection;
	protected final AtomicInteger reconnectCounter = new AtomicInteger();
	protected final ConcurrentMap<SocketAddress, NettyFabricConnectionWatchDog>
		registrationMap;
	protected final Repository<Channel> repository;
	protected final SocketAddress socketAddress;

	protected class PostConnectChannelFutureListener
		implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture channelFuture) {
			Channel channel = channelFuture.channel();

			if (channelFuture.isSuccess()) {
				if (_log.isInfoEnabled()) {
					_log.info("Connected to " + socketAddress);
				}

				Path repositoryPath = repository.getRepositoryPath();

				ChannelFuture registerChannelFuture = channel.writeAndFlush(
					new NettyFabricAgentConfig(repositoryPath.toFile()));

				registerChannelFuture.addListener(
					new PostRegisterChannelFutureListener());

				return;
			}

			if (channelFuture.isCancelled()) {
				_log.error("Cancelled connecting to " + socketAddress);
			}
			else {
				_log.error(
					"Unable to connect to " + socketAddress,
					channelFuture.cause());
			}
		}

	}

	protected class PostDisconnectReconnectChannelFutureListener
		implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture channelFuture) {
			if (reconnectCounter.getAndDecrement() <= 0) {
				return;
			}

			EventLoopGroup eventLoopGroup = bootstrap.group();

			eventLoopGroup.schedule(
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
							" ms later");
			}
		}

	}

	protected class PostDisconnectUnregisterChannelFutureListener
		implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture channelFuture) {
			if (_log.isInfoEnabled()) {
				_log.info("Disconnected from " + socketAddress);
			}

			nettyFabricConnection = null;

			if (activeConnectionCounter.decrementAndGet() <= 0) {
				repository.dispose(false);

				if (_log.isInfoEnabled()) {
					_log.info(
						"All connections are disconnected, disposed " +
							"repository " + repository.getRepositoryPath());
				}
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

				nettyFabricConnection = new NettyFabricConnection(
					channel, nettyFabricClientConfig.getExecutionTimeout());

				activeConnectionCounter.incrementAndGet();

				ChannelFuture closeChannelFuture = channel.closeFuture();

				closeChannelFuture.addListener(
					new PostDisconnectUnregisterChannelFutureListener());

				return;
			}

			if (channelFuture.isCancelled()) {
				_log.error(
					"Cancelled registering Netty fabric agent on " + channel);
			}
			else {
				_log.error(
					"Unable to register Netty fabric agent on " + channel,
					channelFuture.cause());
			}
		}

	}

	private static final Log _log = LogFactoryUtil.getLog(
		NettyFabricConnectionWatchDog.class);

}