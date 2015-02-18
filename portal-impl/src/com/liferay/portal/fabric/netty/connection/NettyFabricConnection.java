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
import com.liferay.portal.fabric.netty.handlers.NettyChannelAttributes;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.net.SocketAddress;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricConnection implements FabricConnection {

	public NettyFabricConnection(Channel channel, long executionTimeout) {
		this.channel = channel;
		this.executionTimeout = executionTimeout;

		ChannelFuture channelFuture = channel.closeFuture();

		channelFuture.addListener(new PostCloseChannelFutureListener());
	}

	@Override
	public ChannelFuture disconnect() {
		return channel.close();
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		return channel.localAddress();
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		return channel.remoteAddress();
	}

	@Override
	public String toString() {
		return channel.toString();
	}

	protected void terminateFabricWorkers() {
		Map<Long, FabricWorker<?>> fabricWorkers =
			NettyChannelAttributes.getFabricWorkers(channel);

		for (Entry<Long, FabricWorker<?>> entry : fabricWorkers.entrySet()) {
			FabricWorker<?> fabricWorker = entry.getValue();

			NoticeableFuture<?> noticeableFuture =
				fabricWorker.getProcessNoticeableFuture();

			try {
				noticeableFuture.get(executionTimeout, TimeUnit.MILLISECONDS);
			}
			catch (Throwable t) {
				noticeableFuture.cancel(true);

				_log.error(
					"Unable to terminate fabric worker " + entry.getKey(), t);
			}
		}
	}

	protected final Channel channel;
	protected final long executionTimeout;

	protected class PostCloseChannelFutureListener
		implements ChannelFutureListener {

		@Override
		public void operationComplete(ChannelFuture channelFuture) {
			terminateFabricWorkers();

			if (_log.isInfoEnabled()) {
				_log.info("Disconnect Netty fabric connection on " + channel);
			}
		}

	}

	private static final Log _log = LogFactoryUtil.getLog(
		NettyFabricConnection.class);

}