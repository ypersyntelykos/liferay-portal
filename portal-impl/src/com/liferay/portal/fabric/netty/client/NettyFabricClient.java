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

import static com.liferay.portal.fabric.netty.server.NettyFabricServer.FILESERVER_FOLDER_COMPRESSION_LEVEL;
import static com.liferay.portal.fabric.netty.server.NettyFabricServer.RPC_GROUP_THREAD_COUNT;

import com.liferay.portal.fabric.client.FabricClient;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentConfig;
import com.liferay.portal.fabric.netty.codec.serialization.AnnotatedObjectDecoder;
import com.liferay.portal.fabric.netty.codec.serialization.AnnotatedObjectEncoder;
import com.liferay.portal.fabric.netty.fileserver.CompressionLevel;
import com.liferay.portal.fabric.netty.fileserver.handlers.FileRequestChannelHandler;
import com.liferay.portal.fabric.netty.handlers.NettyFabricWorkerExecutionChannelHandler;
import com.liferay.portal.fabric.netty.repository.NettyRepository;
import com.liferay.portal.fabric.netty.rpc.handlers.NettyRPCChannelHandler;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ProcessExecutor;
import com.liferay.portal.kernel.util.NamedThreadFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.file.Path;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricClient implements FabricClient {

	// TODO move to portal.properties

	public static final int FILE_SERVER_GROUP_THREAD_COUNT = 1;

	public static final int FILESERVER_FOLDER_COMPRESSION_LEVEL = 1;

	public static final long GET_FILE_TIMEOUT = 10 * 60 * 1000;

	public static final String HOST = "localhost";

	public static final int PORT = 8923;

	public static final int RPC_GROUP_THREAD_COUNT = 1;

	public static final int WORKER_GROUP_THREAD_COUNT = 1;

	public static final int WORKER_STARTER_THREAD_COUNT = 1;

	public NettyFabricClient(
		Path repositoryPath, ProcessExecutor processExecutor) {

		_repositoryPath = repositoryPath.toAbsolutePath();
		_processExecutor = processExecutor;
	}

	@Override
	public synchronized void connect() throws InterruptedException {
		if (_channel != null) {
			throw new IllegalStateException(
				"Netty fabric client has already started");
		}

		_eventLoopGroup = new NioEventLoopGroup(
			WORKER_GROUP_THREAD_COUNT,
			new NamedThreadFactory(
				"Netty Fabric Client/NIO Event Loop Group",
				Thread.NORM_PRIORITY, null));

		_rpcEventExecutorGroup = new DefaultEventExecutorGroup(
			RPC_GROUP_THREAD_COUNT,
			new NamedThreadFactory(
				"Netty Fabric Client/RPC Event Executor Group",
				Thread.NORM_PRIORITY, null));

		_fileServerEventExecutorGroup = new DefaultEventExecutorGroup(
			FILE_SERVER_GROUP_THREAD_COUNT,
			new NamedThreadFactory(
				"Netty Fabric Client/File Server Event Executor Group",
				Thread.NORM_PRIORITY, null));

		_workerStarterEventExecutorGroup = new DefaultEventExecutorGroup(
			WORKER_STARTER_THREAD_COUNT,
			new NamedThreadFactory(
				"Netty Fabric Client/Worker Starter Event Executor Group",
				Thread.NORM_PRIORITY, null));

		Bootstrap bootstrap = new Bootstrap();

		bootstrap.group(_eventLoopGroup);
		bootstrap.channel(NioSocketChannel.class);

		// TODO client side options

//		serverBootstrap.option(ChannelOption.SO_BACKLOG, -1);

		bootstrap.handler(
			new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel socketChannel) {
					ChannelPipeline channelPipeline = socketChannel.pipeline();

					channelPipeline.addLast(new LoggingHandler(LogLevel.INFO));
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
				}

			});

		ChannelFuture channelFuture = bootstrap.connect(HOST, PORT);

		_channel = channelFuture.channel();

		channelFuture.addListener(
			new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture)
					throws InterruptedException {

					if (!channelFuture.isSuccess()) {
						_log.error("Unable to connect to " + HOST + ":" + PORT);

						disconnect();

						return;
					}

					ChannelPipeline channelPipeline = _channel.pipeline();

					channelPipeline.addLast(
						_workerStarterEventExecutorGroup,
						new NettyFabricWorkerExecutionChannelHandler(
							new NettyRepository(
								_repositoryPath, _channel,
								_fileServerEventExecutorGroup,
								GET_FILE_TIMEOUT),
							_processExecutor));

					channelFuture = _channel.writeAndFlush(
						new NettyFabricAgentConfig(_repositoryPath.toFile()));

					channelFuture.addListener(
						new ChannelFutureListener() {

							@Override
							public void operationComplete(
									ChannelFuture channelFuture)
								throws InterruptedException {

								if (channelFuture.isSuccess()) {
									if (_log.isInfoEnabled()) {
										_log.info(
											"Registered fabric agent on " +
												_channel);
									}
								}
								else {
									_log.error(
										"Unable to register fabric agent on " +
											_channel);

									disconnect();
								}
							}

						});
				}

			});

		channelFuture.sync();
	}

	@Override
	public synchronized void disconnect() throws InterruptedException {
		if (_channel == null) {
			throw new IllegalStateException(
				"Netty fabric client has not started");
		}

		try {
			ChannelFuture channelFuture = _channel.close();

			channelFuture.sync();
		}
		finally {
			_eventLoopGroup.shutdownGracefully();
			_rpcEventExecutorGroup.shutdownGracefully();
			_fileServerEventExecutorGroup.shutdownGracefully();

			_channel = null;
			_eventLoopGroup = null;
			_rpcEventExecutorGroup = null;
			_fileServerEventExecutorGroup = null;
		}
	}

	private static Log _log = LogFactoryUtil.getLog(NettyFabricClient.class);

	private Channel _channel;
	private EventLoopGroup _eventLoopGroup;
	private EventExecutorGroup _fileServerEventExecutorGroup;
	private final ProcessExecutor _processExecutor;
	private final Path _repositoryPath;
	private EventExecutorGroup _rpcEventExecutorGroup;
	private EventExecutorGroup _workerStarterEventExecutorGroup;

}