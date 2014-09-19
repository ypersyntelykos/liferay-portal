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

import com.liferay.portal.kernel.util.HashUtil;
import com.liferay.portal.kernel.util.StringBundler;

import java.io.IOException;
import java.io.Serializable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

/**
 * @author Shuyang Zhou
 */
public class MetaInfo implements Comparable<MetaInfo>, Serializable {

	public MetaInfo(String remoteFilePath, Path localFile) {
		if (remoteFilePath == null) {
			throw new NullPointerException("Remote file path is null");
		}

		if (localFile == null) {
			throw new NullPointerException("Local file is null");
		}

		_remoteFilePath = remoteFilePath;
		_localFile = localFile.toAbsolutePath();
	}

	@Override
	public int compareTo(MetaInfo metaInfo) {
		int result = _remoteFilePath.compareTo(metaInfo._remoteFilePath);

		if (result != 0) {
			return result;
		}

		return _localFile.compareTo(metaInfo._localFile);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof MetaInfo)) {
			return false;
		}

		MetaInfo metaInfo = (MetaInfo)obj;

		// No need to use Validator, the constructor and final keyword ensure
		// _remoteFilePath/_localFile can not be null

		if (_remoteFilePath.equals(metaInfo._remoteFilePath) &&
			_localFile.equals(metaInfo._localFile)) {

			return true;
		}

		return false;
	}

	public long getLastModifiedTime() {
		try {
			FileTime fileTime = Files.getLastModifiedTime(_localFile);

			return fileTime.toMillis();
		}
		catch (IOException ioe) {
			return Long.MIN_VALUE;
		}
	}

	public Path getLocalFile() {
		return _localFile;
	}

	public String getRemoteFilePath() {
		return _remoteFilePath;
	}

	@Override
	public int hashCode() {
		int hashCode = HashUtil.hash(0, _remoteFilePath);

		return HashUtil.hash(hashCode, _localFile);
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(5);

		sb.append("{remoteFilePath=");
		sb.append(_remoteFilePath);
		sb.append(", localFilePath=");
		sb.append(_localFile);
		sb.append("}");

		return sb.toString();
	}

	private static final long serialVersionUID = 1L;

	private final Path _localFile;
	private final String _remoteFilePath;

}