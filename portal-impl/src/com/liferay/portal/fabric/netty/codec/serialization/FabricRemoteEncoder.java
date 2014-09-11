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

package com.liferay.portal.fabric.netty.codec.serialization;

import com.liferay.portal.fabric.FabricRemote;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.Serializable;

import java.util.List;

/**
 * @author Shuyang Zhou
 */
@ChannelHandler.Sharable
public class FabricRemoteEncoder
	extends MessageToMessageEncoder<FabricRemote<? extends Serializable>> {

	public static final FabricRemoteEncoder INSTANCE =
		new FabricRemoteEncoder();

	public static final String NAME = FabricRemoteEncoder.class.getName();

	@Override
	protected void encode(
		ChannelHandlerContext channelHandlerContext,
		FabricRemote<? extends Serializable> fabricRemote, List<Object> list) {

		list.add(fabricRemote.toStub());
	}

	private FabricRemoteEncoder() {
	}

}