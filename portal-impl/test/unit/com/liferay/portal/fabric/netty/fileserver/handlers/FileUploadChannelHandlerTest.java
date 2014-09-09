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

import com.liferay.portal.fabric.netty.fileserver.FileHelperUtil;
import com.liferay.portal.fabric.netty.fileserver.FileResponse;
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.test.CaptureHandler;
import com.liferay.portal.kernel.test.CodeCoverageAssertor;
import com.liferay.portal.kernel.test.JDKLoggerTestUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.test.AdviseWith;
import com.liferay.portal.test.runners.AspectJMockingNewClassLoaderJUnitTestRunner;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Shuyang Zhou
 */
@RunWith(AspectJMockingNewClassLoaderJUnitTestRunner.class)
public class FileUploadChannelHandlerTest {

	@ClassRule
	public static CodeCoverageAssertor codeCoverageAssertor =
		new CodeCoverageAssertor();

	@After
	public void tearDown() throws IOException {
		FileServerTestUtil.cleanUp();
	}

	@Test
	public void testConstructor() throws IOException {
		FileResponse fileResponse = new FileResponse(
			Paths.get("testFile"), 1, -1, false);

		FileUploadChannelHandler fileUploadChannelHandler =
			new FileUploadChannelHandler(
				_asyncBroker, fileResponse, _embeddedChannel.eventLoop());

		Assert.assertSame(_asyncBroker, fileUploadChannelHandler.asyncBroker);
		Assert.assertSame(fileResponse, fileUploadChannelHandler.fileResponse);
		Assert.assertSame(
			_embeddedChannel.eventLoop(),
			fileUploadChannelHandler.eventExecutor);

		Path file = fileResponse.getLocalFile();

		FileServerTestUtil.registerForCleanUp(file);

		Assert.assertTrue(Files.isRegularFile(file));

		try (FileChannel fileChannel = fileUploadChannelHandler.fileChannel) {
			Assert.assertTrue(fileChannel.isOpen());
		}
	}

	@Test
	public void testConstructorWithParameterValidation() throws IOException {
		try {
			new FileUploadChannelHandler(null, null, null);

			Assert.fail();
		}
		catch (NullPointerException npe) {
			Assert.assertEquals("Async broker is null", npe.getMessage());
		}

		try {
			new FileUploadChannelHandler(_asyncBroker, null, null);

			Assert.fail();
		}
		catch (NullPointerException npe) {
			Assert.assertEquals("File response is null", npe.getMessage());
		}

		try {
			new FileUploadChannelHandler(
				_asyncBroker,
				new FileResponse(
					Paths.get("testFile"), FileResponse.FILE_NOT_FOUND, -1,
					false),
				null);

			Assert.fail();
		}
		catch (NullPointerException npe) {
			Assert.assertEquals("Event executor is null", npe.getMessage());
		}

		try {
			new FileUploadChannelHandler(
				_asyncBroker,
				new FileResponse(
					Paths.get("testFile"), FileResponse.FILE_NOT_FOUND, -1,
					false),
				_embeddedChannel.eventLoop());

			Assert.fail();
		}
		catch (IllegalArgumentException iae) {
			Assert.assertEquals(
				"File response has no content for uploading", iae.getMessage());
		}
	}

	@Test
	public void testFileUpload() throws Exception {
		_doTestFileUpload(true, false);
		_doTestFileUpload(false, false);
		_doTestFileUpload(true, true);
		_doTestFileUpload(false, true);
	}

	@AdviseWith(adviceClasses = FileHelperUtilAdvice.class)
	@Test
	public void testFolderUpload() throws Exception {
		_doTestFolderUpload(true, false);
		_doTestFolderUpload(false, false);
		_doTestFolderUpload(true, true);
		_doTestFolderUpload(false, true);
	}

	@Test
	public void testReceive() throws Exception {
		byte[] data = FileServerTestUtil.createRandomData(20);

		FileResponse fileResponse = new FileResponse(
			Paths.get("testFile"), data.length, -1, false);

		FileUploadChannelHandler fileUploadChannelHandler =
			new FileUploadChannelHandler(
				_asyncBroker, fileResponse, _embeddedChannel.eventLoop());

		FileServerTestUtil.registerForCleanUp(fileResponse.getLocalFile());

		final UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		ReflectionTestUtil.setFieldValue(
			fileUploadChannelHandler, "fileChannel",
			new FileChannelWrapper(fileUploadChannelHandler.fileChannel) {

				@Override
				public int write(ByteBuffer byteBuffer) {
					unsyncByteArrayOutputStream.write(byteBuffer.get());

					return 1;
				}

				@Override
				public long position() {
					return unsyncByteArrayOutputStream.size();
				}

			});

		ByteBuf byteBuf = FileServerTestUtil.wrapFirstHalf(data);

		Assert.assertEquals(1, byteBuf.refCnt());
		Assert.assertFalse(fileUploadChannelHandler.receive(byteBuf));
		Assert.assertEquals(0, byteBuf.refCnt());

		byteBuf = Unpooled.buffer();

		byteBuf.writeBytes(FileServerTestUtil.wrapSecondHalf(data));
		byteBuf.writeBytes(data);

		Assert.assertEquals(1, byteBuf.refCnt());
		Assert.assertTrue(fileUploadChannelHandler.receive(byteBuf));
		Assert.assertEquals(1, byteBuf.refCnt());
		Assert.assertArrayEquals(
			data, unsyncByteArrayOutputStream.toByteArray());
		Assert.assertEquals(Unpooled.wrappedBuffer(data), byteBuf);
	}

	@Aspect
	public static class FileHelperUtilAdvice {

		@Around(
			"execution(public static java.nio.file.Path " +
				"com.liferay.portal.fabric.netty.fileserver.FileHelperUtil." +
					"unzip(java.nio.file.Path))")
		public Object unzip(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {

			if (_throwException) {
				_throwException = false;

				throw new IOException("Forced Exception");
			}

			return proceedingJoinPoint.proceed();
		}

		private static boolean _throwException;

	}

	private void _doTestFileUpload(boolean inEventLoop, boolean fail)
		throws Exception {

		byte[] data = FileServerTestUtil.createRandomData(1024);

		long lastModified = FileServerTestUtil.getFileSystemTime(
			System.currentTimeMillis() - Time.DAY);

		Path file = _doTestUpload(inEventLoop, data, lastModified, false, fail);

		if (!fail) {
			Assert.assertArrayEquals(data, Files.readAllBytes(file));
		}
	}

	private void _doTestFolderUpload(boolean inEventLoop, boolean fail)
		throws Exception {

		Path testFolder = FileServerTestUtil.createFolderWithFiles(
			Paths.get("testFolder"));

		long lastModified = FileServerTestUtil.getFileSystemTime(
			System.currentTimeMillis() - Time.DAY);

		Files.setLastModifiedTime(
			testFolder, FileTime.fromMillis(lastModified));

		Path zipFile = FileHelperUtil.zip(testFolder);

		try {
			Path folder = _doTestUpload(
				inEventLoop, Files.readAllBytes(zipFile), lastModified, true,
				fail);

			if (!fail) {
				FileServerTestUtil.assertFileEquals(testFolder, folder);
			}
		}
		finally {
			FileHelperUtil.delete(zipFile);
		}
	}

	private Path _doTestUpload(
			boolean inEventloop, byte[] data, long lastModified, boolean folder,
			boolean fail)
		throws Exception {

		FileResponse fileResponse = new FileResponse(
			Paths.get("testFile"), data.length, lastModified, folder);

		EventLoop eventLoop = _embeddedChannel.eventLoop();

		if (!inEventloop) {
			NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(1);

			eventLoop = nioEventLoopGroup.next();
		}

		FileUploadChannelHandler fileUploadChannelHandler =
			new FileUploadChannelHandler(
				new AsyncBroker<String, FileResponse>(), fileResponse,
				eventLoop);

		if (folder) {
			FileServerTestUtil.registerForCleanUp(fileResponse.getLocalFile());
		}

		ChannelPipeline channelPipeline = _embeddedChannel.pipeline();

		channelPipeline.addFirst(fileUploadChannelHandler);

		CaptureHandler captureHandler = null;

		if (fail) {
			captureHandler = JDKLoggerTestUtil.configureJDKLogger(
				FileUploadChannelHandler.class.getName(), Level.SEVERE);

			if (folder) {
				FileHelperUtilAdvice._throwException = true;
			}
			else {
				FileChannel fileChannel = fileUploadChannelHandler.fileChannel;

				fileChannel.close();
			}
		}

		try {
			ByteBuf byteBuf = Unpooled.buffer();

			byteBuf.writeBytes(FileServerTestUtil.wrapSecondHalf(data));

			if (inEventloop) {
				byteBuf.writeBytes(data);

				_embeddedChannel.writeInbound(
					FileServerTestUtil.wrapFirstHalf(data), byteBuf);
			}
			else {
				fileUploadChannelHandler.channelRead(
					channelPipeline.firstContext(),
					FileServerTestUtil.wrapFirstHalf(data));
				fileUploadChannelHandler.channelRead(
					channelPipeline.firstContext(), byteBuf);
			}

			if (!fail && inEventloop) {
				Queue<Object> queue = _embeddedChannel.inboundMessages();

				Assert.assertEquals(1, queue.size());
				Assert.assertEquals(Unpooled.wrappedBuffer(data), queue.poll());
			}
		}
		catch (Exception e) {
			fileUploadChannelHandler.exceptionCaught(
				channelPipeline.firstContext(), e);
		}

		if (folder) {
			if (inEventloop) {
				_embeddedChannel.runPendingTasks();
			}
			else {
				Future<?> future = eventLoop.shutdownGracefully();

				future.get();
			}
		}

		if (fail) {
			List<LogRecord> logRecords = captureHandler.getLogRecords();

			LogRecord logRecord = logRecords.get(0);

			Assert.assertEquals("File upload failure", logRecord.getMessage());

			Throwable throwable = logRecord.getThrown();

			if (folder) {
				Assert.assertEquals("Forced Exception", throwable.getMessage());
			}
			else {
				Assert.assertTrue(throwable instanceof ClosedChannelException);
			}
		}

		if (!inEventloop) {
			eventLoop.shutdownGracefully();
		}

		Assert.assertNotSame(fileUploadChannelHandler, channelPipeline.first());
		Assert.assertSame(channelPipeline.first(), channelPipeline.last());

		Path file = fileResponse.getLocalFile();

		FileServerTestUtil.registerForCleanUp(file);

		if (!fail) {
			FileTime fileTime = Files.getLastModifiedTime(file);

			Assert.assertEquals(lastModified, fileTime.toMillis());
		}

		return file;
	}

	private final AsyncBroker<String, FileResponse> _asyncBroker =
		new AsyncBroker<String, FileResponse>();

	private final EmbeddedChannel _embeddedChannel = new EmbeddedChannel(
		new ChannelInitializer<Channel>() {

		@Override
		protected void initChannel(Channel channel) {
		}

	});

}