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

package com.liferay.portal.fabric.netty.fileserver.handlers;

import com.liferay.portal.fabric.netty.fileserver.FileResponse;
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.test.CodeCoverageAssertor;
import com.liferay.portal.kernel.util.Time;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.embedded.EmbeddedChannel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Shuyang Zhou
 */
public class FileResponseChannelHandlerTest {

	@ClassRule
	public static CodeCoverageAssertor codeCoverageAssertor =
		new CodeCoverageAssertor();

	@Before
	public void setUp() {
		EmbeddedChannel embeddedChannel = new EmbeddedChannel(
			new ChannelInitializer<Channel>() {

				@Override
				protected void initChannel(Channel channel) {
				}

			});

		_fileResponseChannelHandler = new FileResponseChannelHandler(
			_asyncBroker, embeddedChannel.eventLoop());

		_channelPipeline = embeddedChannel.pipeline();

		_channelPipeline.addFirst(_fileResponseChannelHandler);

		_channelHandlerContext = _channelPipeline.firstContext();
	}

	@Test
	public void testFile() throws Exception {
		byte[] data = FileServerTestUtil.createRandomDate(1024);

		long lastModified = FileServerTestUtil.getFileSystemTime(
			System.currentTimeMillis() - Time.DAY);

		FileResponse fileResponse = new FileResponse(
			_file, data.length, lastModified, false);

		_fileResponseChannelHandler.channelRead(
			_channelHandlerContext, fileResponse,
			FileServerTestUtil.wrapFirstHalf(data));

		ChannelHandler channelHandler = _channelPipeline.first();

		Assert.assertTrue(channelHandler instanceof FileUploadChannelHandler);

		FileUploadChannelHandler fileUploadChannelHandler =
			(FileUploadChannelHandler)channelHandler;

		_channelPipeline.fireChannelRead(
			FileServerTestUtil.wrapSecondHalf(data));

		channelHandler = _channelPipeline.first();

		Assert.assertFalse(channelHandler instanceof FileUploadChannelHandler);
		Assert.assertSame(fileResponse, fileUploadChannelHandler.fileResponse);

		Path localFile = fileResponse.getLocalFile();

		Assert.assertNotNull(localFile);

		FileTime fileTime = Files.getLastModifiedTime(localFile);

		Assert.assertEquals(lastModified, fileTime.toMillis());
		Assert.assertArrayEquals(data, Files.readAllBytes(localFile));

		Files.delete(localFile);
	}

	@Test
	public void testFileNotFound() throws Exception {
		Future<FileResponse> future = _asyncBroker.post(_key);

		FileResponse fileResponse = new FileResponse(
			_file, FileResponse.FILE_NOT_FOUND, -1, false);

		_fileResponseChannelHandler.channelRead(
			_channelHandlerContext, fileResponse, null);

		Assert.assertSame(fileResponse, future.get());
	}

	@Test
	public void testFileNotModified() throws Exception {
		Future<FileResponse> future = _asyncBroker.post(_key);

		FileResponse fileResponse = new FileResponse(
			_file, FileResponse.FILE_NOT_MODIFIED, -1, false);

		_fileResponseChannelHandler.channelRead(
			_channelHandlerContext, fileResponse, null);

		Assert.assertSame(fileResponse, future.get());
	}

	private static final Path _file = Paths.get("testFile");
	private static final String _key = _file.toAbsolutePath().toString();

	private AsyncBroker<String, FileResponse> _asyncBroker =
		new AsyncBroker<String, FileResponse>();
	private ChannelHandlerContext _channelHandlerContext;
	private ChannelPipeline _channelPipeline;
	private FileResponseChannelHandler _fileResponseChannelHandler;

}