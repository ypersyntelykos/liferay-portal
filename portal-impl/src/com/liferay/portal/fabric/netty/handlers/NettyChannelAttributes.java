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

package com.liferay.portal.fabric.netty.handlers;

import com.liferay.portal.fabric.agent.FabricAgentRegistry;
import com.liferay.portal.fabric.connection.FabricConnection;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentStub;
import com.liferay.portal.fabric.netty.client.NettyFabricClientConfig;
import com.liferay.portal.fabric.netty.rpc.RPCUtil;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.io.Serializable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shuyang Zhou
 */
public class NettyChannelAttributes {

	public static <T extends Serializable> AsyncBroker<Long, T> getAsyncBroker(
		Channel channel) {

		Attribute<AsyncBroker<Long, Serializable>> attribute = channel.attr(
			_asyncBrokerKey);

		AsyncBroker<Long, Serializable> asyncBroker = attribute.get();

		if (asyncBroker == null) {
			asyncBroker = new AsyncBroker<>();

			AsyncBroker<Long, Serializable> previousAsyncBroker =
				attribute.setIfAbsent(asyncBroker);

			if (previousAsyncBroker != null) {
				asyncBroker = previousAsyncBroker;
			}
		}

		return (AsyncBroker<Long, T>)asyncBroker;
	}

	public static FabricAgentRegistry getFabricAgentRegistry(Channel channel) {
		Attribute<FabricAgentRegistry> attribute = channel.attr(
			_fabricAgentRegistryKey);

		return attribute.get();
	}

	public static FabricConnection getFabricConnection(Channel channel) {
		Attribute<FabricConnection> attribute = channel.attr(
			_fabricConnectionKey);

		return attribute.get();
	}

	public static <T extends Serializable> FabricWorker<T> getFabricWorker(
		Channel channel, long id) {

		Map<Long, FabricWorker<?>> fabricWorkers = getFabricWorkers(channel);

		return (FabricWorker<T>)fabricWorkers.get(id);
	}

	public static Map<Long, FabricWorker<?>> getFabricWorkers(Channel channel) {
		Attribute<Map<Long, FabricWorker<?>>> attribute = channel.attr(
			_fabricWorkersKey);

		Map<Long, FabricWorker<?>> fabricWorkers = attribute.get();

		if (fabricWorkers == null) {
			return Collections.emptyMap();
		}

		return fabricWorkers;
	}

	public static NettyFabricAgentStub getNettyFabricAgentStub(
		Channel channel) {

		Attribute<NettyFabricAgentStub> attribute = channel.attr(
			_nettyFabricAgentStubKey);

		return attribute.get();
	}

	public static NettyFabricClientConfig getNettyFabricClientConfig(
		Channel channel) {

		Attribute<NettyFabricClientConfig> attribute = channel.attr(
			_nettyFabricClientConfigKey);

		return attribute.get();
	}

	public static long nextId(Channel channel) {
		Attribute<AtomicLong> attribute = channel.attr(_idGeneratorKey);

		AtomicLong attachmentIdGenerator = attribute.get();

		if (attachmentIdGenerator == null) {
			attachmentIdGenerator = new AtomicLong();

			AtomicLong previousAttachmentIdGenerator = attribute.setIfAbsent(
				attachmentIdGenerator);

			if (previousAttachmentIdGenerator != null) {
				attachmentIdGenerator = previousAttachmentIdGenerator;
			}
		}

		return attachmentIdGenerator.getAndIncrement();
	}

	public static <T extends Serializable> void putFabricWorker(
		Channel channel, final long id, FabricWorker<T> fabricWorker) {

		Attribute<Map<Long, FabricWorker<?>>> attribute = channel.attr(
			_fabricWorkersKey);

		Map<Long, FabricWorker<?>> fabricWorkers = attribute.get();

		if (fabricWorkers == null) {
			fabricWorkers = new ConcurrentHashMap<>();

			Map<Long, FabricWorker<?>> previousFabricWorkers =
				attribute.setIfAbsent(fabricWorkers);

			if (previousFabricWorkers != null) {
				fabricWorkers = previousFabricWorkers;
			}
		}

		fabricWorkers.put(id, fabricWorker);

		NoticeableFuture<T> noticeableFuture =
			fabricWorker.getProcessNoticeableFuture();

		final Map<Long, FabricWorker<?>> fabricWorkersRef = fabricWorkers;

		noticeableFuture.addFutureListener(
			new FutureListener<T>() {

				@Override
				public void complete(Future<T> future) {
					fabricWorkersRef.remove(id);
				}

			});
	}

	public static void setFabricAgentRegistry(
		Channel channel, FabricAgentRegistry fabricAgentRegistry) {

		Attribute<FabricAgentRegistry> attribute = channel.attr(
			_fabricAgentRegistryKey);

		attribute.set(fabricAgentRegistry);
	}

	public static void setFabricConnection(
		Channel channel, FabricConnection fabricConnection) {

		Attribute<FabricConnection> attribute = channel.attr(
			_fabricConnectionKey);

		attribute.set(fabricConnection);
	}

	public static void setNettyFabricAgentStub(
		Channel channel, NettyFabricAgentStub nettyFabricAgentStub) {

		Attribute<NettyFabricAgentStub> attribute = channel.attr(
			_nettyFabricAgentStubKey);

		attribute.set(nettyFabricAgentStub);
	}

	public static void setNettyFabricClientConfig(
		Channel channel, NettyFabricClientConfig fabricAgentRegistry) {

		Attribute<NettyFabricClientConfig> attribute = channel.attr(
			_nettyFabricClientConfigKey);

		attribute.set(fabricAgentRegistry);
	}

	private static final AttributeKey<AsyncBroker<Long, Serializable>>
		_asyncBrokerKey = AttributeKey.valueOf(
			RPCUtil.class.getName() + "-AsyncBroker");
	private static final AttributeKey<FabricAgentRegistry>
		_fabricAgentRegistryKey = AttributeKey.valueOf(
			FabricAgentRegistry.class.getName());
	private static final AttributeKey<FabricConnection> _fabricConnectionKey =
		AttributeKey.valueOf(FabricConnection.class.getName());
	private static final AttributeKey<Map<Long, FabricWorker<?>>>
		_fabricWorkersKey = AttributeKey.valueOf(
			NettyChannelAttributes.class.getName() + "-FabricWorkers");
	private static final AttributeKey<AtomicLong> _idGeneratorKey =
		AttributeKey.valueOf(RPCUtil.class.getName() + "-IdGenerator");
	private static final AttributeKey<NettyFabricAgentStub>
		_nettyFabricAgentStubKey = AttributeKey.valueOf(
			NettyFabricAgentStub.class.getName());
	private static final AttributeKey<NettyFabricClientConfig>
		_nettyFabricClientConfigKey = AttributeKey.valueOf(
			NettyFabricClientConfig.class.getName());

}