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

package com.liferay.portal.fabric.netty.repository;

import com.liferay.portal.fabric.netty.fileserver.FileHelperUtil;
import com.liferay.portal.fabric.netty.fileserver.FileRequest;
import com.liferay.portal.fabric.netty.fileserver.FileResponse;
import com.liferay.portal.fabric.netty.fileserver.handlers.FileResponseChannelHandler;
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shuyang Zhou
 */
public class Repository {

	public static String getRepositoryFile(String remoteFilePath) {
		File remoteFile = new File(remoteFilePath);

		String name = remoteFile.getName();

		int index = name.lastIndexOf(CharPool.PERIOD);

		if (index == -1) {
			StringBundler sb = new StringBundler(5);

			sb.append(name);
			sb.append(StringPool.DASH);
			sb.append(System.currentTimeMillis());
			sb.append(StringPool.DASH);
			sb.append(_idGenerator.getAndIncrement());

			return sb.toString();
		}

		StringBundler sb = new StringBundler(6);

		sb.append(name.substring(0, index));
		sb.append(StringPool.DASH);
		sb.append(System.currentTimeMillis());
		sb.append(StringPool.DASH);
		sb.append(_idGenerator.getAndIncrement());
		sb.append(name.substring(index));

		return sb.toString();
	}

	public Repository(
		Path repositoryFolder, Channel channel,
		EventExecutorGroup eventExecutorGroup, long fileSyncTimeout) {

		if (!Files.isDirectory(repositoryFolder)) {
			throw new IllegalArgumentException(
				repositoryFolder + " is not a directory");
		}

		_repositoryFolder = repositoryFolder;
		_channel = channel;
		_fileSyncTimeout = fileSyncTimeout;

		ChannelPipeline channelPipeline = _channel.pipeline();

		channelPipeline.addLast(
			new FileResponseChannelHandler(_asyncBroker, eventExecutorGroup));
	}

	public synchronized Path getFile(
		final String remoteFilePathString, boolean deleteAfterFetch) {

		if (_log.isDebugEnabled()) {
			_log.debug("Resolving remote file " + remoteFilePathString);
		}

		Path localFilePath = _fileMap.get(remoteFilePathString);

		final Future<FileResponse> future = _asyncBroker.post(
			remoteFilePathString);

		ChannelFuture channelFuture = _channel.writeAndFlush(
			new FileRequest(
				Paths.get(remoteFilePathString),
				_getLastModifiedTime(localFilePath), deleteAfterFetch));

		channelFuture.addListener(
			new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture) {
					if (channelFuture.isSuccess()) {
						return;
					}

					future.cancel(true);

					_log.error(
						"Cancelled file resolving for " + remoteFilePathString +
							", due to ",
						channelFuture.cause());
				}

			});

		try {
			FileResponse fileResponse = future.get(
				_fileSyncTimeout, TimeUnit.MILLISECONDS);

			if (fileResponse.isFileNotFound()) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to find remote file " + remoteFilePathString);
				}

				return null;
			}

			if (fileResponse.isFileNotModified()) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"Remote file " + remoteFilePathString +
							" is not modified, keep using local file " +
								localFilePath);
				}

				return localFilePath;
			}

			localFilePath = _repositoryFolder.resolve(
				getRepositoryFile(remoteFilePathString));

			FileHelperUtil.move(fileResponse.getLocalFile(), localFilePath);

			_fileMap.put(remoteFilePathString, localFilePath);

			return localFilePath;
		}
		catch (Exception e) {
			_log.error(
				"Unable to fetch remote file " + remoteFilePathString +
					" from " + _channel.remoteAddress(),
				e);
		}

		return null;
	}

	private long _getLastModifiedTime(Path path) {
		if (path == null) {
			return Long.MIN_VALUE;
		}

		try {
			FileTime fileTime = Files.getLastModifiedTime(path);

			return fileTime.toMillis();
		}
		catch (IOException ioe) {
			return Long.MIN_VALUE;
		}
	}

	private static Log _log = LogFactoryUtil.getLog(Repository.class);

	private static final AtomicLong _idGenerator = new AtomicLong();

	private final AsyncBroker<String, FileResponse> _asyncBroker =
		new AsyncBroker<String, FileResponse>();
	private final Channel _channel;
	private final Map<String, Path> _fileMap = new HashMap<String, Path>();
	private final long _fileSyncTimeout;
	private final Path _repositoryFolder;

}