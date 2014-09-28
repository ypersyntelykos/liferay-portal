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
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shuyang Zhou
 */
public class NettyChannelAttributes {

	public static <T> void attach(
		Channel channel, NoticeableFuture<T> noticeableFuture) {

		AttributeKey<NoticeableFuture<T>> attributeKey = AttributeKey.valueOf(
			"Attachment-" + nextId(channel));

		final Attribute<NoticeableFuture<T>> attribute = channel.attr(
			attributeKey);

		attribute.set(noticeableFuture);

		noticeableFuture.addFutureListener(
			new FutureListener<T>() {

				@Override
				public void complete(Future<T> future) {
					attribute.remove();
				}

			});
	}

	public static NettyFabricAgentStub getNettyFabricAgentStub(
		Channel channel) {

		Attribute<NettyFabricAgentStub> attribute = channel.attr(
			_nettyFabricAgentStubKey);

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

	public static void setNettyFabricAgentStub(
		Channel channel, NettyFabricAgentStub nettyFabricAgentStub) {

		Attribute<NettyFabricAgentStub> attribute = channel.attr(
			_nettyFabricAgentStubKey);

		attribute.set(nettyFabricAgentStub);
	}

	private static final AttributeKey<AtomicLong> _idGeneratorKey =
		AttributeKey.valueOf(
			NettyChannelAttributes.class.getName() + "-IdGenerator");
	private static final AttributeKey<NettyFabricAgentStub>
		_nettyFabricAgentStubKey = AttributeKey.valueOf(
			NettyFabricAgentStub.class.getName());

}