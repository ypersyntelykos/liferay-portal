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

package com.liferay.portal.fabric.netty.rpc;

import com.liferay.portal.fabric.netty.handlers.NettyChannelAttributes;
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ProcessCallable;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class RPCUtil {

	public static <T extends Serializable> NoticeableFuture<T> execute(
		Channel channel, ProcessCallable<T> processCallable) {

		final AsyncBroker<Long, T> asyncBroker =
			(AsyncBroker<Long, T>)getRPCAsyncBroker(channel);

		final long id = NettyChannelAttributes.nextId(channel);

		NoticeableFuture<T> noticeableFuture = asyncBroker.post(id);

		ChannelFuture channelFuture = channel.writeAndFlush(
			new RPCRequest<T>(id, processCallable));

		channelFuture.addListener(
			new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture)
					throws Exception {

					if (channelFuture.isSuccess()) {
						return;
					}

					if (!asyncBroker.takeWithException(
							id, channelFuture.cause())) {

						_log.error(
							"No match key : " + id +
								" for rpc response exception ",
							channelFuture.cause());
					}
				}

			});

		return noticeableFuture;
	}

	protected static AsyncBroker<Long, Serializable> getRPCAsyncBroker(
		Channel channel) {

		Attribute<AsyncBroker<Long, Serializable>> attribute = channel.attr(
			_asyncBrokerKey);

		AsyncBroker<Long, Serializable> asyncBroker = attribute.get();

		if (asyncBroker == null) {
			AsyncBroker<Long, Serializable> newAsyncBroker =
				new AsyncBroker<Long, Serializable>();

			asyncBroker = attribute.setIfAbsent(newAsyncBroker);

			if (asyncBroker == null) {
				asyncBroker = newAsyncBroker;
			}
		}

		return asyncBroker;
	}

	private static Log _log = LogFactoryUtil.getLog(RPCUtil.class);

	private static final AttributeKey<AsyncBroker<Long, Serializable>>
		_asyncBrokerKey = AttributeKey.valueOf(
			RPCUtil.class.getName() + "-AsyncBroker");

}