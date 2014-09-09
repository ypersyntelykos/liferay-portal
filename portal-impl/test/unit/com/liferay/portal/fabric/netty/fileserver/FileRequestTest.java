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

package com.liferay.portal.fabric.netty.fileserver;

import com.liferay.portal.kernel.test.CodeCoverageAssertor;
import com.liferay.portal.kernel.util.HashUtil;
import com.liferay.portal.kernel.util.StringBundler;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Shuyang Zhou
 */
public class FileRequestTest {

	@ClassRule
	public static CodeCoverageAssertor codeCoverageAssertor =
		new CodeCoverageAssertor();

	@Before
	public void setUp() {
		_fileRequest = new FileRequest(
			_path, _lastModifiedTime, _deleteAfterFetch);
	}

	@Test
	public void testConstructor() {
		try {
			new FileRequest(null, System.currentTimeMillis(), true);

			Assert.fail();
		}
		catch (NullPointerException npe) {
		}

		Path absolutePath = _fileRequest.getPath();

		Assert.assertTrue(absolutePath.isAbsolute());
		Assert.assertEquals(
			_lastModifiedTime, _fileRequest.getLastModifiedTime());
		Assert.assertEquals(
			_deleteAfterFetch, _fileRequest.isDeleteAfterFetch());
	}

	@Test
	public void testEquals() {
		Assert.assertTrue(_fileRequest.equals(_fileRequest));
		Assert.assertFalse(_fileRequest.equals(new Object()));
		Assert.assertFalse(
			_fileRequest.equals(
				new FileRequest(
					Paths.get("unknown"), _lastModifiedTime,
					_deleteAfterFetch)));
		Assert.assertFalse(
			_fileRequest.equals(
				new FileRequest(
					_path, _lastModifiedTime + 1, _deleteAfterFetch)));
		Assert.assertFalse(
			_fileRequest.equals(
				new FileRequest(_path, _lastModifiedTime, !_deleteAfterFetch)));
		Assert.assertTrue(
			_fileRequest.equals(
				new FileRequest(_path, _lastModifiedTime, _deleteAfterFetch)));
	}

	@Test
	public void testHashCode() {
		int hash = HashUtil.hash(0, _path.toAbsolutePath());

		hash = HashUtil.hash(hash, _lastModifiedTime);

		Assert.assertEquals(
			HashUtil.hash(hash, _deleteAfterFetch), _fileRequest.hashCode());
	}

	@Test
	public void testToString() {
		StringBundler sb = new StringBundler(7);

		sb.append("{path=");
		sb.append(_path.toAbsolutePath());
		sb.append(", lastModifiedTime=");
		sb.append(_lastModifiedTime);
		sb.append(", deleteAfterFetch=");
		sb.append(_deleteAfterFetch);
		sb.append("}");

		Assert.assertEquals(sb.toString(), _fileRequest.toString());
	}

	private final boolean _deleteAfterFetch = true;
	private FileRequest _fileRequest;
	private final long _lastModifiedTime = System.currentTimeMillis();
	private final Path _path = Paths.get("testPath");

}