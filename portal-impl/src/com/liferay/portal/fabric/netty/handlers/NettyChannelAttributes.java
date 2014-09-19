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

import com.liferay.portal.fabric.netty.agent.NettyFabricAgentStub;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * @author Shuyang Zhou
 */
public class NettyChannelAttributes {

	public static NettyFabricAgentStub getNettyStubFabricAgent(
		Channel channel) {

		Attribute<NettyFabricAgentStub> attribute = channel.attr(
			_nettyStubFabricAgentKey);

		return attribute.get();
	}

	public static void setNettyStubFabricAgent(
		Channel channel, NettyFabricAgentStub nettyStubFabricAgent) {

		Attribute<NettyFabricAgentStub> attribute = channel.attr(
			_nettyStubFabricAgentKey);

		attribute.set(nettyStubFabricAgent);
	}

	private static final AttributeKey<NettyFabricAgentStub>
		_nettyStubFabricAgentKey = AttributeKey.valueOf(
			NettyFabricAgentStub.class.getName());

}