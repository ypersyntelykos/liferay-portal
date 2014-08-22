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
import com.liferay.portal.fabric.netty.agent.NettyFabricAgent;
import com.liferay.portal.fabric.netty.codec.serialization.AnnotatedObjectDecoder;
import com.liferay.portal.fabric.netty.codec.serialization.AnnotatedObjectEncoder;
import com.liferay.portal.fabric.netty.codec.serialization.FabricRemoteEncoder;
import com.liferay.portal.fabric.netty.fileserver.FileResponse;
import com.liferay.portal.fabric.netty.fileserver.handlers.FileResponseChannelHandler;
import com.liferay.portal.fabric.netty.handlers.NettyStubFabricWorkerExecutionChannelHandler;
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
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

/**
 * @author Shuyang Zhou
 */
public class NettyFabricClient implements FabricClient {

	public static final int CLIENT_UNZIP_GROUP_THREAD_COUNT = 1;

	// TODO move to portal.properties

	public static final int CLIENT_WORKER_GROUP_THREAD_COUNT = 1;

	public static final String HOST = "localhost";

	public static final int PORT = 8923;

	public NettyFabricClient(
		NettyFabricAgent nettyFabricAgent,
		AsyncBroker<String, FileResponse> asyncBroker) {

		_nettyFabricAgent = nettyFabricAgent;
		_asyncBroker = asyncBroker;
	}

	@Override
	public synchronized void connect() throws InterruptedException {
		if (_channel != null) {
			throw new IllegalStateException("Netty fabric client has started");
		}

		_eventLoopGroup = new NioEventLoopGroup(
			CLIENT_WORKER_GROUP_THREAD_COUNT,
			new NamedThreadFactory(
				_THREAD_POOL_NAME, Thread.MAX_PRIORITY, null));

		_unzipEventExecutorGroup = new DefaultEventExecutorGroup(
			CLIENT_UNZIP_GROUP_THREAD_COUNT,
			new NamedThreadFactory(
				"Netty Fabric Client/Unzip Event Executor Group",
				Thread.NORM_PRIORITY, null));

		Bootstrap bootstrap = new Bootstrap();

		bootstrap.group(_eventLoopGroup);
		bootstrap.channel(NioSocketChannel.class);

		// TODO client side options

//		serverBootstrap.option(ChannelOption.SO_BACKLOG, -1);

		bootstrap.handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel socketChannel) {
				ChannelPipeline channelPipeline = socketChannel.pipeline();

				channelPipeline.addLast(new LoggingHandler(LogLevel.INFO));
				channelPipeline.addLast(
					AnnotatedObjectEncoder.NAME,
					AnnotatedObjectEncoder.INSTANCE);
				channelPipeline.addLast(
					FabricRemoteEncoder.NAME, FabricRemoteEncoder.INSTANCE);
				channelPipeline.addLast(
					AnnotatedObjectDecoder.NAME, new AnnotatedObjectDecoder());
				channelPipeline.addLast(
					new FileResponseChannelHandler(
						_asyncBroker, _unzipEventExecutorGroup));
				channelPipeline.addLast(
					new NioEventLoopGroup(1),
					new NettyStubFabricWorkerExecutionChannelHandler(
						_nettyFabricAgent));
			}
		});

		ChannelFuture channelFuture = bootstrap.connect(HOST, PORT);

		_channel = channelFuture.channel();

		channelFuture.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture channelFuture)
				throws InterruptedException {

				_nettyFabricAgent.initialize(_channel, _asyncBroker);

				if (channelFuture.isSuccess()) {
					channelFuture = _channel.writeAndFlush(_nettyFabricAgent);

					channelFuture.addListener(new ChannelFutureListener() {

						@Override
						public void operationComplete(
								ChannelFuture channelFuture)
							throws InterruptedException {

							if (channelFuture.isSuccess()) {
								if (_log.isInfoEnabled()) {
									_log.info(
										"Registered fabric agent " +
											_nettyFabricAgent);
								}
							}
							else {
								_log.error(
									"Unable to register fabric agent " +
										_nettyFabricAgent);

								disconnect();
							}
						}

					});
				}
				else {
					_log.error("Unable to connect to " + HOST + ":" + PORT);

					disconnect();
				}
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
			_unzipEventExecutorGroup.shutdownGracefully();

			_channel = null;
			_eventLoopGroup = null;
			_unzipEventExecutorGroup = null;
		}
	}

	private static final String _THREAD_POOL_NAME =
		"Netty Fabric Client/NIO Event Loop Group";

	private static Log _log = LogFactoryUtil.getLog(NettyFabricClient.class);

	private AsyncBroker<String, FileResponse> _asyncBroker;
	private Channel _channel;
	private EventLoopGroup _eventLoopGroup;
	private final NettyFabricAgent _nettyFabricAgent;
	private EventExecutorGroup _unzipEventExecutorGroup;

}