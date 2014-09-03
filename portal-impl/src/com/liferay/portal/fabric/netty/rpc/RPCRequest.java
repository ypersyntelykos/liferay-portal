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

import com.liferay.portal.kernel.process.ProcessCallable;

import io.netty.channel.Channel;

import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class RPCRequest<T extends Serializable> extends RPCSerializable {

	public RPCRequest(long id, ProcessCallable<T> processCallable) {
		super(id);

		_processCallable = processCallable;
	}

	@Override
	public void execute(Channel channel) {
		ChannelThreadLocal.setChannel(channel);

		try {
			channel.writeAndFlush(
				new RPCResponse<T>(id, _processCallable.call(), null));
		}
		catch (Throwable t) {
			channel.writeAndFlush(new RPCResponse<T>(id, null, t));
		}
		finally {
			ChannelThreadLocal.removeChannel();
		}
	}

	private static final long serialVersionUID = 1L;

	private final ProcessCallable<T> _processCallable;

}