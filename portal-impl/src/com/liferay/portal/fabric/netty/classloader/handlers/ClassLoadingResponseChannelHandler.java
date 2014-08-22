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

package com.liferay.portal.fabric.netty.classloader.handlers;

import com.liferay.portal.fabric.netty.classloader.ClassLoadingResponse;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Shuyang Zhou
 */
public class ClassLoadingResponseChannelHandler
	extends SimpleChannelInboundHandler<ClassLoadingResponse> {

	public ClassLoadingResponse getClassLoadingResponse(long timeout)
		throws InterruptedException, TimeoutException {

		if (_countDownLatch.await(timeout, TimeUnit.MILLISECONDS)) {
			throw new TimeoutException(
				"Timeout on receiving ClassLoadingResponse");
		}

		return _classLoadingResponse;
	}

	@Override
	protected void channelRead0(
			ChannelHandlerContext channelHandlerContext,
			ClassLoadingResponse classLoadingResponse)
		throws Exception {

		_classLoadingResponse = classLoadingResponse;

		_countDownLatch.countDown();
	}

	private ClassLoadingResponse _classLoadingResponse;
	private final CountDownLatch _countDownLatch = new CountDownLatch(1);

}