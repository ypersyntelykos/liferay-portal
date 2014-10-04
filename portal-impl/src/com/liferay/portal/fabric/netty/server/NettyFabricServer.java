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

package com.liferay.portal.fabric.netty.server;

import com.liferay.portal.fabric.agent.FabricAgentRegistry;
import com.liferay.portal.fabric.netty.codec.serialization.AnnotatedObjectDecoder;
import com.liferay.portal.fabric.netty.codec.serialization.AnnotatedObjectEncoder;
import com.liferay.portal.fabric.netty.fileserver.CompressionLevel;
import com.liferay.portal.fabric.netty.fileserver.handlers.FileRequestChannelHandler;
import com.liferay.portal.fabric.netty.handlers.NettyFabricAgentExecutionChannelHandler;
import com.liferay.portal.fabric.netty.rpc.handlers.NettyRPCChannelHandler;
import com.liferay.portal.fabric.server.FabricServer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.NamedThreadFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.file.Path;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricServer implements FabricServer {

	public static final int BOSS_GROUP_THREAD_COUNT = 1;

	public static final int FILE_SERVER_GROUP_THREAD_COUNT = 1;

	public static final int FILESERVER_FOLDER_COMPRESSION_LEVEL = 1;

	public static final long GET_FILE_TIMEOUT = 10 * 60 * 1000;

	// TODO move to portal.properties

	public static final String HOST = "localhost";

	public static final int PORT = 8923;

	public static final int RPC_GROUP_THREAD_COUNT = 1;

	public static final int WORKER_GROUP_THREAD_COUNT = 1;

	public NettyFabricServer(
		FabricAgentRegistry fabricAgentRegistry, Path repositoryParentPath) {

		_fabricAgentRegistry = fabricAgentRegistry;
		_repositoryParentPath = repositoryParentPath;
	}

	@Override
	public synchronized void start() throws InterruptedException {
		if (_serverChannel != null) {
			throw new IllegalStateException(
				"Netty fabric server has already started");
		}

		_bossGroup = new NioEventLoopGroup(
			BOSS_GROUP_THREAD_COUNT,
			new NamedThreadFactory(
				"Netty Fabric Server/Boss Event Loop Group",
				Thread.NORM_PRIORITY, null));
		_workerGroup = new NioEventLoopGroup(
			WORKER_GROUP_THREAD_COUNT,
			new NamedThreadFactory(
				"Netty Fabric Server/Worker Event Loop Group",
				Thread.NORM_PRIORITY, null));
		_rpcEventExecutorGroup = new DefaultEventExecutorGroup(
			RPC_GROUP_THREAD_COUNT,
			new NamedThreadFactory(
				"Netty Fabric Server/RPC Event Executor Group",
				Thread.NORM_PRIORITY, null));
		_fileServerEventExecutorGroup = new DefaultEventExecutorGroup(
			FILE_SERVER_GROUP_THREAD_COUNT,
			new NamedThreadFactory(
				"Netty Fabric Server/File Server Event Executor Group",
				Thread.NORM_PRIORITY, null));

		ServerBootstrap serverBootstrap = new ServerBootstrap();

		serverBootstrap.group(_bossGroup, _workerGroup);
		serverBootstrap.channel(NioServerSocketChannel.class);

		// TODO server side options

//		serverBootstrap.option(ChannelOption.SO_BACKLOG, -1);

		final ChannelHandler loggingHandler = new LoggingHandler(LogLevel.INFO);

		serverBootstrap.handler(loggingHandler);
		serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel socketChannel) {
					ChannelPipeline channelPipeline = socketChannel.pipeline();

					channelPipeline.addLast(loggingHandler);
					channelPipeline.addLast(
						AnnotatedObjectEncoder.NAME,
						AnnotatedObjectEncoder.INSTANCE);
					channelPipeline.addLast(
						AnnotatedObjectDecoder.NAME,
						new AnnotatedObjectDecoder());
					channelPipeline.addLast(
						_rpcEventExecutorGroup, NettyRPCChannelHandler.NAME,
						NettyRPCChannelHandler.INSTANCE);
					channelPipeline.addLast(
						_fileServerEventExecutorGroup,
						FileRequestChannelHandler.NAME,
						new FileRequestChannelHandler(
							CompressionLevel.getCompressionLevel(
								FILESERVER_FOLDER_COMPRESSION_LEVEL)));
					channelPipeline.addLast(
						new NettyFabricAgentExecutionChannelHandler(
							_fabricAgentRegistry, _repositoryParentPath,
							_fileServerEventExecutorGroup, GET_FILE_TIMEOUT));
				}

			});

		ChannelFuture channelFuture = serverBootstrap.bind(HOST, PORT);

		_serverChannel = channelFuture.channel();

		channelFuture.addListener(
			new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture)
					throws InterruptedException {

					if (channelFuture.isSuccess()) {
						return;
					}

					if (channelFuture.isCancelled()) {
						_log.error("Cancelled netty fabric server starting");
					}
					else {
						_log.error(
							"Unable to start netty fabric server",
							channelFuture.cause());
					}

					stop();
				}

			});

		channelFuture.sync();
	}

	@Override
	public synchronized void stop() throws InterruptedException {
		if (_serverChannel == null) {
			throw new IllegalStateException(
				"Netty fabric server has not started");
		}

		try {
			ChannelFuture channelFuture = _serverChannel.close();

			channelFuture.sync();
		}
		finally {
			_bossGroup.shutdownGracefully();
			_workerGroup.shutdownGracefully();
			_rpcEventExecutorGroup.shutdownGracefully();
			_fileServerEventExecutorGroup.shutdownGracefully();

			_serverChannel = null;
			_bossGroup = null;
			_workerGroup = null;
			_rpcEventExecutorGroup = null;
			_fileServerEventExecutorGroup = null;
		}
	}

	private static Log _log = LogFactoryUtil.getLog(NettyFabricServer.class);

	private EventLoopGroup _bossGroup;
	private final FabricAgentRegistry _fabricAgentRegistry;
	private EventExecutorGroup _fileServerEventExecutorGroup;
	private final Path _repositoryParentPath;
	private EventExecutorGroup _rpcEventExecutorGroup;
	private Channel _serverChannel;
	private EventLoopGroup _workerGroup;

}