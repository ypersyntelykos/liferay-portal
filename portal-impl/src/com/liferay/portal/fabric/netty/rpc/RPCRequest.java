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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.util.StringBundler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

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
			final RPCResponse<T> rpcResponse = new RPCResponse<T>(
				id, _processCallable.call(), null);

			ChannelFuture channelFuture = channel.writeAndFlush(rpcResponse);

			channelFuture.addListener(
				new ChannelFutureListener() {

					@Override
					public void operationComplete(ChannelFuture channelFuture) {
						if (channelFuture.isSuccess()) {
							return;
						}

						_log.error(
							"Unable to send back rpc response : " + rpcResponse,
							channelFuture.cause());
					}

				});
		}
		catch (Throwable t) {
			final RPCResponse<T> rpcResponse = new RPCResponse<T>(id, null, t);

			ChannelFuture channelFuture = channel.writeAndFlush(rpcResponse);

			channelFuture.addListener(
				new ChannelFutureListener() {

					@Override
					public void operationComplete(ChannelFuture channelFuture) {
						if (channelFuture.isSuccess()) {
							return;
						}

						_log.error(
							"Unable to send back rpc response : " + rpcResponse,
							channelFuture.cause());
					}

				});
		}
		finally {
			ChannelThreadLocal.removeChannel();
		}
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(5);

		sb.append("{id=");
		sb.append(id);
		sb.append(", processCallable=");
		sb.append(_processCallable);
		sb.append("}");

		return sb.toString();
	}

	private static Log _log = LogFactoryUtil.getLog(RPCRequest.class);

	private static final long serialVersionUID = 1L;

	private final ProcessCallable<T> _processCallable;

}