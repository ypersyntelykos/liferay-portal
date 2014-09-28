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
import com.liferay.portal.fabric.netty.handlers.NettyChannelAttributes;
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.concurrent.DefaultNoticeableFuture;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.concurrent.NoticeableFutureConverter;
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

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shuyang Zhou
 */
public class Repository {

	public static Path getRepositoryFilePath(
		Path repository, Path remoteFilePath) {

		Path fileNamePath = remoteFilePath.getFileName();

		String name = fileNamePath.toString();

		int index = name.lastIndexOf(CharPool.PERIOD);

		if (index == -1) {
			StringBundler sb = new StringBundler(5);

			sb.append(name);
			sb.append(StringPool.DASH);
			sb.append(System.currentTimeMillis());
			sb.append(StringPool.DASH);
			sb.append(_idGenerator.getAndIncrement());

			return repository.resolve(sb.toString());
		}

		StringBundler sb = new StringBundler(6);

		sb.append(name.substring(0, index));
		sb.append(StringPool.DASH);
		sb.append(System.currentTimeMillis());
		sb.append(StringPool.DASH);
		sb.append(_idGenerator.getAndIncrement());
		sb.append(name.substring(index));

		return repository.resolve(sb.toString());
	}

	public Repository(
		Path repositoryPath, Channel channel,
		EventExecutorGroup eventExecutorGroup) {

		if (!Files.isDirectory(repositoryPath)) {
			throw new IllegalArgumentException(
				repositoryPath + " is not a directory");
		}

		_repositoryPath = repositoryPath;
		_channel = channel;

		ChannelPipeline channelPipeline = _channel.pipeline();

		channelPipeline.addLast(
			new FileResponseChannelHandler(_asyncBroker, eventExecutorGroup));
	}

	public NoticeableFuture<Path> getFile(
		final Path remoteFilePath, final Path localFilePath,
		boolean deleteAfterFetch) {

		if (localFilePath == null) {
			return getFile(
				remoteFilePath,
				getRepositoryFilePath(_repositoryPath, remoteFilePath),
				deleteAfterFetch, true);
		}

		return getFile(remoteFilePath, localFilePath, deleteAfterFetch, false);
	}

	public NoticeableFuture<Map<Path, Path>> getFiles(
		final Map<Path, Path> pathMap, boolean deleteAfterFetch) {

		final DefaultNoticeableFuture<Map<Path, Path>> defaultNoticeableFuture =
			new DefaultNoticeableFuture<Map<Path, Path>>();

		if (pathMap.isEmpty()) {
			defaultNoticeableFuture.set(pathMap);

			return defaultNoticeableFuture;
		}

		final Map<Path, Path> resultPathMap =
			new ConcurrentHashMap<Path, Path>();

		final AtomicInteger counter = new AtomicInteger(pathMap.size());

		for (Map.Entry<Path, Path> entry : pathMap.entrySet()) {
			final Path remoteFilePath = entry.getKey();

			NoticeableFuture<Path> noticeableFuture = getFile(
				remoteFilePath, entry.getValue(), deleteAfterFetch);

			noticeableFuture.addFutureListener(
				new FutureListener<Path>() {

					@Override
					public void complete(Future<Path> future) {
						if (future.isCancelled()) {
							defaultNoticeableFuture.cancel(true);

							return;
						}

						try {
							Path localFilePath = future.get();

							if (localFilePath != null) {
								resultPathMap.put(
									remoteFilePath, localFilePath);
							}

							if (counter.decrementAndGet() <= 0) {
								defaultNoticeableFuture.set(resultPathMap);
							}
						}
						catch (Throwable t) {
							if (t instanceof ExecutionException) {
								t = t.getCause();
							}

							defaultNoticeableFuture.setException(t);
						}
					}

				});
		}

		return defaultNoticeableFuture;
	}

	public Path getRepositoryPath() {
		return _repositoryPath;
	}

	protected NoticeableFuture<Path> getFile(
		final Path remoteFilePath, final Path localFilePath,
		boolean deleteAfterFetch, final boolean populateCache) {

		if (_log.isDebugEnabled()) {
			_log.debug("Fetching remote file " + remoteFilePath);
		}

		final Path cachedLocalFilePath = _pathMap.get(remoteFilePath);

		NoticeableFuture<FileResponse> noticeableFuture = _asyncBroker.post(
			remoteFilePath);

		NettyChannelAttributes.attach(_channel, noticeableFuture);

		ChannelFuture channelFuture = _channel.writeAndFlush(
			new FileRequest(
				remoteFilePath, getLastModifiedTime(cachedLocalFilePath),
				deleteAfterFetch));

		channelFuture.addListener(
			new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture channelFuture) {
					if (channelFuture.isSuccess()) {
						return;
					}

					_asyncBroker.takeWithException(
						remoteFilePath,
						new IOException(
							"Unable to fetch remote file " +
								remoteFilePath,
							channelFuture.cause()));
				}

			});

		return new NoticeableFutureConverter<Path, FileResponse>(
			noticeableFuture) {

				@Override
				protected Path convert(FileResponse fileResponse)
					throws IOException {

					if (fileResponse.isFileNotFound()) {
						if (_log.isWarnEnabled()) {
							_log.warn(
								"Remote file " + remoteFilePath +
									" is not found");
						}

						return null;
					}

					if (fileResponse.isFileNotModified()) {
						if (_log.isDebugEnabled()) {
							_log.debug(
								"Remote file " + remoteFilePath +
									" is not modified, use cached local file " +
									cachedLocalFilePath);
						}

						return cachedLocalFilePath;
					}

					FileHelperUtil.move(
						fileResponse.getLocalFile(), localFilePath);

					if (populateCache) {
						_pathMap.put(remoteFilePath, localFilePath);
					}

					if (_log.isDebugEnabled()) {
						_log.debug(
							"Fetched remote file " + remoteFilePath + " to " +
								localFilePath);
					}

					return localFilePath;
				}

			};
	}

	protected long getLastModifiedTime(Path path) {
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

	private final AsyncBroker<Path, FileResponse> _asyncBroker =
		new AsyncBroker<Path, FileResponse>();
	private final Channel _channel;
	private final Map<Path, Path> _pathMap =
		new ConcurrentHashMap<Path, Path>();
	private final Path _repositoryPath;

}