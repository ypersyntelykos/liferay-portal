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

package com.liferay.portal.fabric.netty.client;

import com.liferay.portal.fabric.client.FabricClient;
import com.liferay.portal.fabric.connection.FabricConnectionWatchDog;
import com.liferay.portal.fabric.netty.codec.serialization.AnnotatedObjectDecoder;
import com.liferay.portal.fabric.netty.codec.serialization.AnnotatedObjectEncoder;
import com.liferay.portal.fabric.netty.connection.NettyFabricConnectionWatchDog;
import com.liferay.portal.fabric.netty.fileserver.handlers.FileRequestChannelHandler;
import com.liferay.portal.fabric.netty.repository.NettyRepository;
import com.liferay.portal.fabric.netty.rpc.handlers.NettyRPCChannelHandler;
import com.liferay.portal.fabric.repository.Repository;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ProcessExecutor;
import com.liferay.portal.kernel.util.NamedThreadFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.IOException;

import java.net.InetSocketAddress;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricClient implements FabricClient {

	public NettyFabricClient(
			ProcessExecutor processExecutor,
			NettyFabricClientConfig nettyFabricClientConfig,
			Runnable shutdownCallback)
		throws IOException {

		this.processExecutor = processExecutor;
		this.nettyFabricClientConfig = nettyFabricClientConfig;
		this.shutdownCallback = shutdownCallback;

		EventLoopGroup eventLoopGroup = new NioEventLoopGroup(
			nettyFabricClientConfig.getEventLoopGroupThreadCount(),
			new NamedThreadFactory(
				"Netty Fabric Client/NIO Event Loop Group",
				Thread.NORM_PRIORITY, null));

		executionEventExecutorGroup = new DefaultEventExecutorGroup(
			nettyFabricClientConfig.getExecutionGroupThreadCount(),
			new NamedThreadFactory(
				"Netty Fabric Client/Execution Event Executor Group",
				Thread.NORM_PRIORITY, null));

		fileServerEventExecutorGroup = new DefaultEventExecutorGroup(
			nettyFabricClientConfig.getFileServerGroupThreadCount(),
			new NamedThreadFactory(
				"Netty Fabric Client/File Server Event Executor Group",
				Thread.NORM_PRIORITY, null));

		rpcEventExecutorGroup = new DefaultEventExecutorGroup(
			nettyFabricClientConfig.getRPCGroupThreadCount(),
			new NamedThreadFactory(
				"Netty Fabric Client/RPC Event Executor Group",
				Thread.NORM_PRIORITY, null));

		bootstrap = new Bootstrap();

		bootstrap.channel(NioSocketChannel.class);
		bootstrap.group(eventLoopGroup);
		bootstrap.handler(new NettyFabricClientChannelInitializer());

		Path repositoryPath = nettyFabricClientConfig.getRepositoryPath();

		Files.createDirectories(repositoryPath);

		repository = new NettyRepository(
			repositoryPath,
			nettyFabricClientConfig.getRepositoryGetFileTimeout());
	}

	@Override
	public FabricConnectionWatchDog connect(
		InetSocketAddress inetSocketAddress) {

		NettyFabricConnectionWatchDog nettyFabricConnectionWatchDog =
			new NettyFabricConnectionWatchDog(
				bootstrap, inetSocketAddress, nettyFabricClientConfig,
				repository, executionEventExecutorGroup,
				fileServerEventExecutorGroup, processExecutor,
				nettyFabricConnectionWatchDogs, activeConnectionCounter);

		NettyFabricConnectionWatchDog previousNettyFabricConnectionWatchDog =
			nettyFabricConnectionWatchDogs.putIfAbsent(
				inetSocketAddress, nettyFabricConnectionWatchDog);

		if (previousNettyFabricConnectionWatchDog != null) {
			nettyFabricConnectionWatchDog.stop();

			nettyFabricConnectionWatchDog =
				previousNettyFabricConnectionWatchDog;
		}

		return nettyFabricConnectionWatchDog;
	}

	@Override
	public Map<InetSocketAddress, FabricConnectionWatchDog>
		getFabricConnectionWatchDogs() {

		return new HashMap<InetSocketAddress, FabricConnectionWatchDog>(
			nettyFabricConnectionWatchDogs);
	}

	@Override
	public void shutdown() {
		try {
			for (Map.Entry<InetSocketAddress, NettyFabricConnectionWatchDog>
				entry : nettyFabricConnectionWatchDogs.entrySet()) {

				InetSocketAddress inetSocketAddress = entry.getKey();

				NettyFabricConnectionWatchDog nettyFabricConnectionWatchDog =
					entry.getValue();

				ChannelFuture channelFuture =
					nettyFabricConnectionWatchDog.stop();

				try {
					channelFuture.sync();
				}
				catch (Exception e) {
					_log.error(
						"Unable to stop Netty fabric connection watch dog on " +
							"address " + inetSocketAddress, e);
				}
			}

			repository.dispose(true);
		}
		finally {
			EventLoopGroup eventLoopGroup = bootstrap.group();

			eventLoopGroup.shutdownGracefully();

			executionEventExecutorGroup.shutdownGracefully();

			fileServerEventExecutorGroup.shutdownGracefully();

			rpcEventExecutorGroup.shutdownGracefully();

			shutdownCallback.run();
		}
	}

	protected final AtomicInteger activeConnectionCounter = new AtomicInteger();
	protected final Bootstrap bootstrap;
	protected final EventExecutorGroup executionEventExecutorGroup;
	protected final EventExecutorGroup fileServerEventExecutorGroup;
	protected final NettyFabricClientConfig nettyFabricClientConfig;
	protected final ConcurrentMap
		<InetSocketAddress, NettyFabricConnectionWatchDog>
			nettyFabricConnectionWatchDogs =
				new ConcurrentHashMap
					<InetSocketAddress, NettyFabricConnectionWatchDog>();
	protected final ProcessExecutor processExecutor;
	protected final Repository<Channel> repository;
	protected final EventExecutorGroup rpcEventExecutorGroup;
	protected final Runnable shutdownCallback;

	protected class NettyFabricClientChannelInitializer
		extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel socketChannel) {
			ChannelPipeline channelPipeline = socketChannel.pipeline();

			channelPipeline.addLast(
				AnnotatedObjectEncoder.NAME, AnnotatedObjectEncoder.INSTANCE);
			channelPipeline.addLast(
				AnnotatedObjectDecoder.NAME, new AnnotatedObjectDecoder());
			channelPipeline.addLast(
				fileServerEventExecutorGroup, FileRequestChannelHandler.NAME,
				new FileRequestChannelHandler(
					nettyFabricClientConfig.
						getFileServerFolderCompressionLevel()));
			channelPipeline.addLast(
				rpcEventExecutorGroup, NettyRPCChannelHandler.NAME,
				NettyRPCChannelHandler.INSTANCE);
		}

	}

	private static final Log _log = LogFactoryUtil.getLog(
		NettyFabricClient.class);

}