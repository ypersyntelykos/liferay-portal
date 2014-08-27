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
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.EventExecutor;

import java.io.IOException;

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;

import java.util.concurrent.Callable;

/**
 * @author Shuyang Zhou
 */
public class FileUploadChannelHandler extends ChannelInboundHandlerAdapter {

	public FileUploadChannelHandler(
			AsyncBroker<String, FileResponse> asyncBroker,
			FileResponse fileResponse, EventExecutor eventExecutor)
		throws IOException {

		if (asyncBroker == null) {
			throw new NullPointerException("Async broker is null");
		}

		if (fileResponse == null) {
			throw new NullPointerException("File response is null");
		}

		if (eventExecutor == null) {
			throw new NullPointerException("Event executor is null");
		}

		if (fileResponse.getSize() < 1) {
			throw new IllegalArgumentException(
				"File response has no content for uploading");
		}

		this.asyncBroker = asyncBroker;
		this.fileResponse = fileResponse;
		this.eventExecutor = eventExecutor;

		tempPath = Files.createTempFile(
			FileUploadChannelHandler.class.getName() + "-", null);

		fileChannel = FileChannel.open(
			tempPath, StandardOpenOption.WRITE,
			StandardOpenOption.TRUNCATE_EXISTING);

		fileResponse.setLocalFile(tempPath);
	}

	@Override
	public void channelRead(
			ChannelHandlerContext channelHandlerContext, Object object)
		throws Exception {

		ByteBuf byteBuf = (ByteBuf)object;

		if (!receive(byteBuf)) {
			return;
		}

		fileChannel.close();

		if (eventExecutor.inEventLoop() || !fileResponse.isFolder()) {
			finish(fileResponse.getPath(), fileResponse);
		}
		else {
			eventExecutor.submit(new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					try {
						finish(fileResponse.getPath(), fileResponse);
					}
					catch (IOException ioe) {
						exceptionCaught(null, ioe);
					}

					return null;
				}

			});
		}

		if (byteBuf.isReadable()) {
			channelHandlerContext.fireChannelRead(object);
		}

		ChannelPipeline channelPipeline = channelHandlerContext.pipeline();

		channelPipeline.remove(this);
	}

	@Override
	public void exceptionCaught(
			ChannelHandlerContext channelHandlerContext, Throwable throwable)
		throws IOException {

		_log.error("File upload failure", throwable);

		if (channelHandlerContext != null) {
			ChannelPipeline channelPipeline = channelHandlerContext.pipeline();

			channelPipeline.remove(this);
		}

		asyncBroker.takeWithException(fileResponse.getPath(), throwable);

		fileChannel.close();

		Files.delete(tempPath);
	}

	protected void finish(String key, FileResponse fileResponse)
		throws IOException {

		if (fileResponse.isFolder()) {
			fileResponse.setLocalFile(FileHelperUtil.unzip(tempPath));

			Files.delete(tempPath);
		}

		Files.setLastModifiedTime(
			fileResponse.getLocalFile(),
			FileTime.fromMillis(fileResponse.getLastModified()));

		asyncBroker.takeWithResult(key, fileResponse);
	}

	protected boolean receive(ByteBuf byteBuf) throws IOException {
		while (true) {
			long readSize = fileResponse.getSize() - fileChannel.position();

			int readableBytes = byteBuf.readableBytes();

			if (readSize > readableBytes) {
				readSize = readableBytes;
			}

			if (byteBuf.readBytes(fileChannel, (int)readSize) == readSize) {
				break;
			}
		}

		if (!byteBuf.isReadable()) {
			byteBuf.release();
		}

		if (fileChannel.position() < fileResponse.getSize()) {
			return false;
		}

		return true;
	}

	protected final AsyncBroker<String, FileResponse> asyncBroker;
	protected final EventExecutor eventExecutor;
	protected final FileChannel fileChannel;
	protected final FileResponse fileResponse;
	protected final Path tempPath;

	private static Log _log = LogFactoryUtil.getLog(
		FileUploadChannelHandler.class);

}