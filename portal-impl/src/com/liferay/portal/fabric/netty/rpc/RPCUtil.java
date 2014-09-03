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

import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.process.ProcessCallable;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.io.Serializable;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shuyang Zhou
 */
public class RPCUtil {

	public static <T extends Serializable> NoticeableFuture<T> execute(
		Channel channel, ProcessCallable<T> processCallable) {

		AsyncBroker<Long, T> asyncBroker =
			(AsyncBroker<Long, T>)getRPCAsyncBroker(channel);

		AtomicLong idGenerator = getRPCIdGenerator(channel);

		long id = idGenerator.getAndIncrement();

		NoticeableFuture<T> noticeableFuture = asyncBroker.post(id);

		channel.writeAndFlush(new RPCRequest<T>(id, processCallable));

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

	protected static AtomicLong getRPCIdGenerator(Channel channel) {
		Attribute<AtomicLong> attribute = channel.attr(_idGeneratorKey);

		AtomicLong idGenerator = attribute.get();

		if (idGenerator == null) {
			AtomicLong newIdGenerator = new AtomicLong();

			idGenerator = attribute.setIfAbsent(newIdGenerator);

			if (idGenerator == null) {
				idGenerator = newIdGenerator;
			}
		}

		return idGenerator;
	}

	private static final AttributeKey<AsyncBroker<Long, Serializable>>
		_asyncBrokerKey = AttributeKey.valueOf(
			RPCUtil.class.getName() + "-AsyncBroker");
	private static final AttributeKey<AtomicLong>
		_idGeneratorKey = AttributeKey.valueOf(
			RPCUtil.class.getName() + "-IdGenerator");

}