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

package com.liferay.portal.fabric.netty.worker;

import com.liferay.portal.fabric.netty.NettyTestUtil;
import com.liferay.portal.fabric.netty.rpc.handlers.NettyRPCChannelHandler;
import com.liferay.portal.fabric.repository.MockRepository;
import com.liferay.portal.fabric.status.FabricStatus;
import com.liferay.portal.fabric.status.RemoteFabricStatus;
import com.liferay.portal.kernel.concurrent.DefaultNoticeableFuture;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.process.local.ReturnProcessCallable;
import com.liferay.portal.kernel.test.CodeCoverageAssertor;
import com.liferay.portal.kernel.test.ReflectionTestUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;

import java.nio.file.Path;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.ClassRule;
import org.junit.Test;

import org.testng.Assert;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricWorkerStubTest {

	@ClassRule
	public static CodeCoverageAssertor codeCoverageAssertor =
		new CodeCoverageAssertor();

	@Test
	public void testConstructor() {
		try {
			new NettyFabricWorkerStub<String>(null, null, null);

			Assert.fail();
		}
		catch (NullPointerException npe) {
			Assert.assertEquals("Channel is null", npe.getMessage());
		}

		try {
			new NettyFabricWorkerStub<String>(
				NettyTestUtil.createEmptyEmbeddedChannel(), null, null);

			Assert.fail();
		}
		catch (NullPointerException npe) {
			Assert.assertEquals("Repository is null", npe.getMessage());
		}

		try {
			new NettyFabricWorkerStub<String>(
				NettyTestUtil.createEmptyEmbeddedChannel(),
				new MockRepository(), null);

			Assert.fail();
		}
		catch (NullPointerException npe) {
			Assert.assertEquals(
				"Output resource map is null", npe.getMessage());
		}

		Channel channel = NettyTestUtil.createEmptyEmbeddedChannel();

		ChannelFuture channelFuture = channel.closeFuture();

		Assert.assertFalse(channelFuture.isDone());

		NettyFabricWorkerStub<String> nettyFabricWorkerStub =
			new NettyFabricWorkerStub<String>(
				channel, new MockRepository(),
				Collections.<Path, Path>emptyMap());

		Assert.assertNotNull(
			ReflectionTestUtil.getFieldValue(channelFuture, "listeners"));

		NoticeableFuture<String> noticeableFuture =
			nettyFabricWorkerStub.getProcessNoticeableFuture();

		Assert.assertFalse(noticeableFuture.isDone());
		Assert.assertTrue(channelFuture.cancel(true));
		Assert.assertTrue(noticeableFuture.isCancelled());
		Assert.assertNull(
			ReflectionTestUtil.getFieldValue(channelFuture, "listeners"));

		channel = NettyTestUtil.createEmptyEmbeddedChannel();

		channelFuture = channel.closeFuture();

		Assert.assertFalse(channelFuture.isDone());

		nettyFabricWorkerStub = new NettyFabricWorkerStub<String>(
			channel, new MockRepository(), Collections.<Path, Path>emptyMap());

		Assert.assertNotNull(
			ReflectionTestUtil.getFieldValue(channelFuture, "listeners"));

		noticeableFuture = nettyFabricWorkerStub.getProcessNoticeableFuture();

		Assert.assertFalse(noticeableFuture.isDone());
		Assert.assertTrue(noticeableFuture.cancel(true));
		Assert.assertFalse(channelFuture.isDone());
		Assert.assertNull(
			ReflectionTestUtil.getFieldValue(channelFuture, "listeners"));
	}

	@Test
	public void testGetFabricStatus() {
		NettyFabricWorkerStub<String> nettyFabricWorkerStub =
			new NettyFabricWorkerStub<String>(
				NettyTestUtil.createEmptyEmbeddedChannel(),
				new MockRepository(), Collections.<Path, Path>emptyMap());

		FabricStatus fabricStatus = nettyFabricWorkerStub.getFabricStatus();

		Assert.assertSame(RemoteFabricStatus.class, fabricStatus.getClass());
	}

	@Test
	public void testSetException() throws InterruptedException {
		NettyFabricWorkerStub<String> nettyFabricWorkerStub =
			new NettyFabricWorkerStub<String>(
				NettyTestUtil.createEmptyEmbeddedChannel(),
				new MockRepository(), Collections.<Path, Path>emptyMap());

		Throwable throwable = new Throwable();

		nettyFabricWorkerStub.setException(throwable);

		NoticeableFuture<String> noticeableFuture =
			nettyFabricWorkerStub.getProcessNoticeableFuture();

		try {
			noticeableFuture.get();
		}
		catch (ExecutionException ee) {
			Assert.assertSame(throwable, ee.getCause());
		}
	}

	@Test
	public void testSetResult() throws Exception {
		final DefaultNoticeableFuture<Map<Path, Path>> defaultNoticeableFuture =
			new DefaultNoticeableFuture<Map<Path, Path>>();

		NettyFabricWorkerStub<String> nettyFabricWorkerStub =
			new NettyFabricWorkerStub<String>(
				NettyTestUtil.createEmptyEmbeddedChannel(),
				new MockRepository() {

					@Override
					public NoticeableFuture<Map<Path, Path>> getFiles(
						Map<Path, Path> pathMap, boolean deleteAfterFetch) {

						return defaultNoticeableFuture;
					}

				},
				Collections.<Path, Path>emptyMap());

		String result = "Test result";

		nettyFabricWorkerStub.setResult(result);

		NoticeableFuture<String> noticeableFuture =
			nettyFabricWorkerStub.getProcessNoticeableFuture();

		Assert.assertFalse(noticeableFuture.isDone());

		defaultNoticeableFuture.set(Collections.<Path, Path>emptyMap());

		Assert.assertEquals(result, noticeableFuture.get());
	}

	@Test
	public void testSetResultWithCancellation() {
		final DefaultNoticeableFuture<Map<Path, Path>> defaultNoticeableFuture =
			new DefaultNoticeableFuture<Map<Path, Path>>();

		NettyFabricWorkerStub<String> nettyFabricWorkerStub =
			new NettyFabricWorkerStub<String>(
				NettyTestUtil.createEmptyEmbeddedChannel(),
				new MockRepository() {

					@Override
					public NoticeableFuture<Map<Path, Path>> getFiles(
						Map<Path, Path> pathMap, boolean deleteAfterFetch) {

						return defaultNoticeableFuture;
					}

				},
				Collections.<Path, Path>emptyMap());

		nettyFabricWorkerStub.setResult("Test result");

		NoticeableFuture<String> noticeableFuture =
			nettyFabricWorkerStub.getProcessNoticeableFuture();

		Assert.assertFalse(noticeableFuture.isDone());
		Assert.assertTrue(defaultNoticeableFuture.cancel(true));
		Assert.assertTrue(noticeableFuture.isCancelled());
	}

	@Test
	public void testSetResultWithException() throws InterruptedException {
		final DefaultNoticeableFuture<Map<Path, Path>> defaultNoticeableFuture =
			new DefaultNoticeableFuture<Map<Path, Path>>();

		NettyFabricWorkerStub<String> nettyFabricWorkerStub =
			new NettyFabricWorkerStub<String>(
				NettyTestUtil.createEmptyEmbeddedChannel(),
				new MockRepository() {

					@Override
					public NoticeableFuture<Map<Path, Path>> getFiles(
						Map<Path, Path> pathMap, boolean deleteAfterFetch) {

						return defaultNoticeableFuture;
					}

				},
				Collections.<Path, Path>emptyMap());

		nettyFabricWorkerStub.setResult("Test result");

		NoticeableFuture<String> noticeableFuture =
			nettyFabricWorkerStub.getProcessNoticeableFuture();

		Assert.assertFalse(noticeableFuture.isDone());

		Throwable throwable = new Throwable();

		defaultNoticeableFuture.setException(throwable);

		try {
			defaultNoticeableFuture.get();
		}
		catch (ExecutionException ee) {
			Assert.assertSame(throwable, ee.getCause());
		}
	}

	@Test
	public void testWrite() throws Exception {
		EmbeddedChannel embeddedChannel = new EmbeddedChannel(
			NettyRPCChannelHandler.INSTANCE);

		NettyFabricWorkerStub<String> nettyFabricWorkerStub =
			new NettyFabricWorkerStub<String>(
				embeddedChannel, new MockRepository(),
				Collections.<Path, Path>emptyMap());

		String result = "Test result";

		NoticeableFuture<String> noticeableFuture = nettyFabricWorkerStub.write(
			new ReturnProcessCallable<String>(result));

		embeddedChannel.writeInbound(embeddedChannel.readOutbound());
		embeddedChannel.writeInbound(embeddedChannel.readOutbound());

		Assert.assertEquals(result, noticeableFuture.get());
	}

}