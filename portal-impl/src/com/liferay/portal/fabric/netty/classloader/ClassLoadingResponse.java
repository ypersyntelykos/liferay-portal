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

package com.liferay.portal.fabric.netty.classloader;

import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Time;

import io.netty.channel.Channel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.net.URL;

/**
 * @author Shuyang Zhou
 */
public class ClassLoadingResponse implements Serializable {

	// TODO move this to portal properties

	public static final long FILE_TOKEN_TTL = 10 * Time.MINUTE;

	public ClassLoadingResponse(String className, URL url) throws IOException {
		_className = className;

		if (url == null) {
			_classData = null;

			return;
		}

		String path = url.getPath();

		if (path.startsWith("file:")) {
			int pos = path.indexOf(CharPool.EXCLAMATION);

			if (pos != -1) {
				String filePath = path.substring("file:".length(), pos);

				// TODO clean up

				_classData = null;

				return;
			}
		}

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		StreamUtil.transfer(url.openStream(), unsyncByteArrayOutputStream);

		_classData = unsyncByteArrayOutputStream.toByteArray();
	}

	public Class<?> loadClass(
			NettyBridgeClassLoader nettyBridgeClassLoader, Channel channel,
			File libDir)
		throws Exception {

		if (_classData != null) {
			return nettyBridgeClassLoader.defineClass(_className, _classData);
		}

		throw new ClassNotFoundException(
			"Unable to find class with name " + _className + " from remote");
	}

	private String getFileName(String orginalFileName) {
		int pos = orginalFileName.lastIndexOf(CharPool.PERIOD);

		if (pos == -1) {
			return orginalFileName.concat(StringPool.DASH).concat(
				String.valueOf(System.currentTimeMillis()));
		}

		StringBundler sb = new StringBundler(4);

		sb.append(orginalFileName.substring(0, pos));
		sb.append(StringPool.DASH);
		sb.append(System.currentTimeMillis());
		sb.append(orginalFileName.substring(pos + 1));

		return sb.toString();
	}

	private static final long serialVersionUID = 1L;

	private final byte[] _classData;
	private final String _className;

}