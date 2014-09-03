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

import com.liferay.portal.fabric.netty.agent.NettyStubFabricAgent;
import com.liferay.portal.kernel.concurrent.AsyncBroker;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.io.Serializable;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shuyang Zhou
 */
public class NettyChannelAttributes {

	public static NettyStubFabricAgent getNettyStubFabricAgent(
		Channel channel) {

		Attribute<NettyStubFabricAgent> attribute = channel.attr(
			_nettyStubFabricAgentKey);

		return attribute.get();
	}

	public static AsyncBroker<Long, Serializable> getRPCAsyncBroker(
		Channel channel) {

		Attribute<AsyncBroker<Long, Serializable>> attribute = channel.attr(
			_rpcAsyncBrokerKey);

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

	public static AtomicLong getRPCIdGenerator(Channel channel) {
		Attribute<AtomicLong> attribute = channel.attr(_rpcIdGeneratorKey);

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

	public static void setNettyStubFabricAgent(
		Channel channel, NettyStubFabricAgent nettyStubFabricAgent) {

		Attribute<NettyStubFabricAgent> attribute = channel.attr(
			_nettyStubFabricAgentKey);

		attribute.set(nettyStubFabricAgent);
	}

	private static final AttributeKey<NettyStubFabricAgent>
		_nettyStubFabricAgentKey = AttributeKey.valueOf(
			NettyStubFabricAgent.class.getName());
	private static final AttributeKey<AsyncBroker<Long, Serializable>>
		_rpcAsyncBrokerKey = AttributeKey.valueOf(
			"RPC-" + AsyncBroker.class.getName());
	private static final AttributeKey<AtomicLong>
		_rpcIdGeneratorKey = AttributeKey.valueOf(
			"RPC-" + AtomicLong.class.getName());

}