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
import com.liferay.portal.fabric.connection.FabricConnection;
import com.liferay.portal.fabric.connection.FabricConnector;
import com.liferay.portal.fabric.netty.codec.serialization.AnnotatedObjectDecoder;
import com.liferay.portal.fabric.netty.codec.serialization.AnnotatedObjectEncoder;
import com.liferay.portal.fabric.netty.connection.NettyFabricConnector;
import com.liferay.portal.fabric.netty.fileserver.handlers.FileRequestChannelHandler;
import com.liferay.portal.fabric.netty.repository.NettyRepository;
import com.liferay.portal.fabric.netty.rpc.handlers.NettyRPCChannelHandler;
import com.liferay.portal.fabric.repository.Repository;
import com.liferay.portal.kernel.concurrent.BaseFutureListener;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ProcessExecutor;
import com.liferay.portal.kernel.util.NamedThreadFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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
import java.util.concurrent.Future;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricClient implements FabricClient {

	public NettyFabricClient(
			ProcessExecutor processExecutor,
			NettyFabricClientConfig nettyFabricClientConfig,
			NettyFabricClientShutdownCallback nettyFabricClientShutdownCallback)
		throws IOException {

		_processExecutor = processExecutor;
		_nettyFabricClientConfig = nettyFabricClientConfig;
		_nettyFabricClientShutdownCallback = nettyFabricClientShutdownCallback;

		EventLoopGroup eventLoopGroup = new NioEventLoopGroup(
			_nettyFabricClientConfig.getEventLoopGroupThreadCount(),
			new NamedThreadFactory(
				"Netty Fabric Client/NIO Event Loop Group",
				Thread.NORM_PRIORITY, null));

		_executionEventExecutorGroup = new DefaultEventExecutorGroup(
			_nettyFabricClientConfig.getExecutionGroupThreadCount(),
			new NamedThreadFactory(
				"Netty Fabric Client/Execution Event Executor Group",
				Thread.NORM_PRIORITY, null));

		_fileServerEventExecutorGroup = new DefaultEventExecutorGroup(
			_nettyFabricClientConfig.getFileServerGroupThreadCount(),
			new NamedThreadFactory(
				"Netty Fabric Client/File Server Event Executor Group",
				Thread.NORM_PRIORITY, null));

		_rpcEventExecutorGroup = new DefaultEventExecutorGroup(
			_nettyFabricClientConfig.getRPCGroupThreadCount(),
			new NamedThreadFactory(
				"Netty Fabric Client/RPC Event Executor Group",
				Thread.NORM_PRIORITY, null));

		_bootstrap = new Bootstrap();

		_bootstrap.channel(NioSocketChannel.class);
		_bootstrap.group(eventLoopGroup);
		_bootstrap.handler(new NettyFabricClientChannelInitializer());

		Path repositoryPath = _nettyFabricClientConfig.getRepositoryPath();

		Files.createDirectories(repositoryPath);

		_repository = new NettyRepository(
			repositoryPath,
			_nettyFabricClientConfig.getRepositoryGetFileTimeout());
	}

	@Override
	public NoticeableFuture<FabricConnection> connect(
		InetSocketAddress inetSocketAddress) {

		FabricConnector fabricConnector = new NettyFabricConnector(
			_bootstrap, inetSocketAddress, _nettyFabricClientConfig,
			_repository, _executionEventExecutorGroup,
			_fileServerEventExecutorGroup, _processExecutor);

		NoticeableFuture<FabricConnection> noticeableFuture =
			fabricConnector.connect();

		noticeableFuture.addFutureListener(
			new FabricConnectionRegisterFutureListener(inetSocketAddress));

		return noticeableFuture;
	}

	@Override
	public Map<InetSocketAddress, FabricConnection> getFabricConnections() {
		return new HashMap<InetSocketAddress, FabricConnection>(
			_fabricConnections);
	}

	@Override
	public void shutdown() {
		try {
			for (FabricConnection fabricConnection :
					_fabricConnections.values()) {

				try {
					Future<Void> future = fabricConnection.disconnect();

					future.get();
				}
				catch (Exception e) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							"Error on disconnecting " + fabricConnection, e);
					}
				}
			}

			_repository.dispose(true);
		}
		finally {
			EventLoopGroup eventLoopGroup = _bootstrap.group();

			eventLoopGroup.shutdownGracefully();

			_executionEventExecutorGroup.shutdownGracefully();

			_fileServerEventExecutorGroup.shutdownGracefully();

			_rpcEventExecutorGroup.shutdownGracefully();

			_nettyFabricClientShutdownCallback.shutdown();
		}
	}

	protected class FabricConnectionRegisterFutureListener
		extends BaseFutureListener<FabricConnection> {

		@Override
		public void completeWithResult(
			Future<FabricConnection> future,
			final FabricConnection fabricConnection) {

			FabricConnection previousFabricConnection =
				_fabricConnections.putIfAbsent(
					_inetSocketAddress, fabricConnection);

			if (previousFabricConnection != null) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Disconnecting duplicated connection " +
							fabricConnection);
				}

				fabricConnection.disconnect();

				return;
			}

			NoticeableFuture<Void> noticeableFuture =
				fabricConnection.disconnectNoticeableFuture();

			noticeableFuture.addFutureListener(
				new FabricConnectionUnregisterFutureListener(
					_inetSocketAddress, fabricConnection));
		}

		protected FabricConnectionRegisterFutureListener(
			InetSocketAddress inetSocketAddress) {

			_inetSocketAddress = inetSocketAddress;
		}

		private final InetSocketAddress _inetSocketAddress;

	}

	protected class FabricConnectionUnregisterFutureListener
		implements FutureListener<Void> {

		@Override
		public void complete(Future<Void> future) {
			_fabricConnections.remove(_inetSocketAddress, _fabricConnection);

			if (_fabricConnections.isEmpty()) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"All connections have " +
							"disconnected, auto-purging " + "repository.");
				}

				_repository.dispose(false);
			}
		}

		protected FabricConnectionUnregisterFutureListener(
			InetSocketAddress inetSocketAddress,
			FabricConnection fabricConnection) {

			_inetSocketAddress = inetSocketAddress;
			_fabricConnection = fabricConnection;
		}

		private final FabricConnection _fabricConnection;
		private final InetSocketAddress _inetSocketAddress;

	}

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
				_fileServerEventExecutorGroup, FileRequestChannelHandler.NAME,
				new FileRequestChannelHandler(
					_nettyFabricClientConfig.
						getFileServerFolderCompressionLevel()));
			channelPipeline.addLast(
				_rpcEventExecutorGroup, NettyRPCChannelHandler.NAME,
				NettyRPCChannelHandler.INSTANCE);
		}

	}

	private static final Log _log = LogFactoryUtil.getLog(
		NettyFabricClient.class);

	private final Bootstrap _bootstrap;
	private final EventExecutorGroup _executionEventExecutorGroup;
	private final ConcurrentMap<InetSocketAddress, FabricConnection>
		_fabricConnections =
			new ConcurrentHashMap<InetSocketAddress, FabricConnection>();
	private final EventExecutorGroup _fileServerEventExecutorGroup;
	private final NettyFabricClientConfig _nettyFabricClientConfig;
	private final NettyFabricClientShutdownCallback
		_nettyFabricClientShutdownCallback;
	private final ProcessExecutor _processExecutor;
	private final Repository<Channel> _repository;
	private final EventExecutorGroup _rpcEventExecutorGroup;

}