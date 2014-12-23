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
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.DefaultNoticeableFuture;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.concurrent.ReadOnlyNoticeableFutureWrapper;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.TerminationProcessException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.io.Serializable;

import java.net.InetSocketAddress;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricConnection implements FabricConnection {

	public NettyFabricConnection(
		Channel channel, FabricAgent fabricAgent, long executionTimeout) {

		_channel = channel;
		_fabricAgent = fabricAgent;
		_executionTimeout = executionTimeout;

		_defaultNoticeableFuture = new DefaultNoticeableFuture<Void>();
		_disconnectNoticeableFuture = new ReadOnlyNoticeableFutureWrapper<Void>(
			_defaultNoticeableFuture);

		ChannelFuture channelFuture = _channel.closeFuture();

		channelFuture.addListener(new PostCloseChannelFutureListener());
	}

	@Override
	public NoticeableFuture<Void> disconnect() {
		_channel.close();

		return _disconnectNoticeableFuture;
	}

	@Override
	public NoticeableFuture<Void> disconnectNoticeableFuture() {
		return _disconnectNoticeableFuture;
	}

	@Override
	public InetSocketAddress getLocalInetSocketAddress() {
		return (InetSocketAddress)_channel.localAddress();
	}

	@Override
	public InetSocketAddress getRemoteInetSocketAddress() {
		return (InetSocketAddress)_channel.remoteAddress();
	}

	@Override
	public String toString() {
		return _channel.toString();
	}

	protected void terminateFabricWorkers() {
		for (FabricWorker<?> fabricWorker : _fabricAgent.getFabricWorkers()) {
			fabricWorker.write(_runtimeExitProcessCallable);

			NoticeableFuture<?> noticeableFuture =
				fabricWorker.getProcessNoticeableFuture();

			try {
				try {
					noticeableFuture.get(
						_executionTimeout, TimeUnit.MILLISECONDS);
				}
				catch (TimeoutException te) {
					fabricWorker.write(_runtimeHaltProcessCallable);

					noticeableFuture.get(
						_executionTimeout, TimeUnit.MILLISECONDS);
				}
			}
			catch (Throwable t) {
				if (t instanceof ExecutionException) {
					Throwable cause = t.getCause();

					if (cause instanceof TerminationProcessException) {
						TerminationProcessException tpe =
							(TerminationProcessException)cause;

						if (_log.isWarnEnabled()) {
							_log.warn(
								"Forcibly terminate fabric worker with exit " +
									"code " + tpe.getExitCode());
						}

						continue;
					}
				}

				_log.error("Unable to terminate fabric worker", t);
			}
		}
	}

	protected class PostCloseChannelFutureListener
		implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture channelFuture) {
			try {
				terminateFabricWorkers();

				if (_log.isInfoEnabled()) {
					_log.info(
						"Disconnect Netty fabric connection on " + _channel);
				}
			}
			finally {
				if (channelFuture.isCancelled()) {
					_defaultNoticeableFuture.cancel(true);
				}
				else {
					Throwable throwable = channelFuture.cause();

					if (throwable != null) {
						_defaultNoticeableFuture.setException(throwable);
					}
					else {
						_defaultNoticeableFuture.set(channelFuture.getNow());
					}
				}
			}
		}

	}

	private static final int _FABRIC_AGENT_SHUTDOWN_CODE = 211;

	private static final Log _log = LogFactoryUtil.getLog(
		NettyFabricConnection.class);

	private static final ProcessCallable<Serializable>
		_runtimeExitProcessCallable = new ProcessCallable<Serializable>() {

			@Override
			public Serializable call() {
				Runtime runtime = Runtime.getRuntime();

				runtime.exit(_FABRIC_AGENT_SHUTDOWN_CODE);

				return null;
			}

			private static final long serialVersionUID = 1L;

		};

	private static final ProcessCallable<Serializable>
		_runtimeHaltProcessCallable = new ProcessCallable<Serializable>() {

			@Override
			public Serializable call() {
				Runtime runtime = Runtime.getRuntime();

				runtime.halt(_FABRIC_AGENT_SHUTDOWN_CODE);

				return null;
			}

			private static final long serialVersionUID = 1L;

		};

	private final Channel _channel;
	private final DefaultNoticeableFuture<Void> _defaultNoticeableFuture;
	private final NoticeableFuture<Void> _disconnectNoticeableFuture;
	private final long _executionTimeout;
	private final FabricAgent _fabricAgent;

}