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
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;

import io.netty.channel.Channel;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * TODO, need a deep review and optimization
 *
 * @author Shuyang Zhou
 */
public class Repository {

	// TODO make this configurable

	public static final long FILE_SYNC_TIMEOUT = Long.MAX_VALUE;

	public Repository(
		Path repositoryFolder, Channel channel,
		AsyncBroker<String, FileResponse> asyncBroker) {

		if (!Files.isDirectory(repositoryFolder)) {
			throw new IllegalArgumentException(
				repositoryFolder + " is not a directory");
		}

		_repositoryFolder = repositoryFolder;
		_channel = channel;
		_asyncBroker = asyncBroker;
	}

	public synchronized Path getFile(String remoteFilePath) {
		long lastModifiedTime = Long.MIN_VALUE;

		System.out.println("Requsting : " + remoteFilePath);

		MetaInfo metaInfo = _metaInfoMap.get(remoteFilePath);

		if (metaInfo != null) {
			lastModifiedTime = metaInfo.getLastModifiedTime();
		}

		Future<FileResponse> future = _asyncBroker.post(remoteFilePath);

		_channel.writeAndFlush(
			new FileRequest(Paths.get(remoteFilePath), lastModifiedTime));

		try {
			FileResponse fileResponse = future.get(
				FILE_SYNC_TIMEOUT, TimeUnit.MILLISECONDS);

			if ((fileResponse == null) || fileResponse.isFileNotFound()) {
				return null;
			}

			if (fileResponse.isFileNotModified()) {
				return metaInfo.getLocalFile();
			}

			Path repositoryFile = _repositoryFolder.resolve(
				_getRepositoryFile(remoteFilePath));

			FileHelperUtil.move(fileResponse.getLocalFile(), repositoryFile);

			_metaInfoMap.put(
				remoteFilePath, new MetaInfo(remoteFilePath, repositoryFile));

			return repositoryFile;
		}
		catch (Exception e) {
			_log.error(
				"Unable to fetch file " + remoteFilePath + " from " +
					_channel.remoteAddress(), e);
		}

		return null;
	}

	private String _getRepositoryFile(String remoteFilePath) {
		File remoteFile = new File(remoteFilePath);

		String name = remoteFile.getName();

		int index = name.indexOf(CharPool.PERIOD);

		StringBundler sb = null;

		if (index == -1) {
			sb = new StringBundler(5);

			sb.append(name);
			sb.append(StringPool.DASH);
			sb.append(System.currentTimeMillis());
			sb.append(StringPool.DASH);
			sb.append(_idGenerator.getAndIncrement());
		}
		else {
			sb = new StringBundler(6);

			sb.append(name.substring(0, index));
			sb.append(StringPool.DASH);
			sb.append(System.currentTimeMillis());
			sb.append(StringPool.DASH);
			sb.append(_idGenerator.getAndIncrement());
			sb.append(name.substring(index));
		}

		return sb.toString();
	}

	private static final AtomicLong _idGenerator = new AtomicLong();

	private static Log _log = LogFactoryUtil.getLog(Repository.class);

	private final AsyncBroker<String, FileResponse> _asyncBroker;
	private final Channel _channel;
	private final Map<String, MetaInfo> _metaInfoMap =
		new HashMap<String, MetaInfo>();
	private final Path _repositoryFolder;

}