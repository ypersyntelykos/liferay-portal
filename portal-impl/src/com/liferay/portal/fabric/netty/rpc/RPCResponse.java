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

import io.netty.channel.Channel;

import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class RPCResponse<T extends Serializable> extends RPCSerializable {

	public RPCResponse(long id, T result, Throwable throwable) {
		super(id);

		_result = result;
		_throwable = throwable;
	}

	@Override
	public void execute(Channel channel) {
		AsyncBroker<Long, Serializable> asyncBroker = RPCUtil.getRPCAsyncBroker(
			channel);

		if (_throwable != null) {
			asyncBroker.takeWithException(id, _throwable);
		}
		else {
			asyncBroker.takeWithResult(id, _result);
		}
	}

	private static final long serialVersionUID = 1L;

	private final T _result;
	private final Throwable _throwable;

}