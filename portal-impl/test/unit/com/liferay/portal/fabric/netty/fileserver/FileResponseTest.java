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
public class FileResponseTest {

	@ClassRule
	public static CodeCoverageAssertor codeCoverageAssertor =
		new CodeCoverageAssertor();

	@Before
	public void setUp() {
		_fileResponse = new FileResponse(
			_path, _size, _lastModifiedTime, _folder);
	}

	@Test
	public void testConstructor() {
		try {
			new FileResponse(
				null, System.currentTimeMillis(), System.currentTimeMillis(),
				false);

			Assert.fail();
		}
		catch (NullPointerException npe) {
		}

		Path absolutePath = _fileResponse.getPath();

		Assert.assertTrue(absolutePath.isAbsolute());
		Assert.assertEquals(_size, _fileResponse.getSize());
		Assert.assertEquals(
			_lastModifiedTime, _fileResponse.getLastModifiedTime());
		Assert.assertEquals(_folder, _fileResponse.isFolder());
		Assert.assertNull(_fileResponse.getLocalFile());
		Assert.assertFalse(_fileResponse.isFileNotFound());
		Assert.assertFalse(_fileResponse.isFileNotModified());

		_fileResponse.setLocalFile(_path);

		Assert.assertSame(_path, _fileResponse.getLocalFile());

		FileResponse fileResponse = new FileResponse(
			_path, FileResponse.FILE_NOT_FOUND, _lastModifiedTime, _folder);

		Assert.assertTrue(fileResponse.isFileNotFound());
		Assert.assertFalse(fileResponse.isFileNotModified());

		fileResponse = new FileResponse(
			_path, FileResponse.FILE_NOT_MODIFIED, _lastModifiedTime, _folder);

		Assert.assertFalse(fileResponse.isFileNotFound());
		Assert.assertTrue(fileResponse.isFileNotModified());
	}

	@Test
	public void testEquals() {
		Assert.assertTrue(_fileResponse.equals(_fileResponse));
		Assert.assertFalse(_fileResponse.equals(new Object()));
		Assert.assertFalse(
			_fileResponse.equals(
				new FileResponse(
					Paths.get("unknown"), _size, _lastModifiedTime, _folder)));
		Assert.assertFalse(
			_fileResponse.equals(
				new FileResponse(
					_path, _size + 1, _lastModifiedTime, _folder)));
		Assert.assertFalse(
			_fileResponse.equals(
				new FileResponse(
					_path, _size, _lastModifiedTime + 1, _folder)));
		Assert.assertFalse(
			_fileResponse.equals(
				new FileResponse(_path, _size, _lastModifiedTime, !_folder)));
		Assert.assertTrue(
			_fileResponse.equals(
				new FileResponse(_path, _size, _lastModifiedTime, _folder)));
	}

	@Test
	public void testHashCode() {
		int hash = HashUtil.hash(0, _path.toAbsolutePath());

		hash = HashUtil.hash(hash, _size);
		hash = HashUtil.hash(hash, _lastModifiedTime);

		Assert.assertEquals(
			HashUtil.hash(hash, _folder), _fileResponse.hashCode());
	}

	@Test
	public void testToString() {
		StringBundler sb = new StringBundler(9);

		sb.append("{path=");
		sb.append(_path.toAbsolutePath());
		sb.append(", isFolder=");
		sb.append(_folder);
		sb.append(", size=");
		sb.append(_size);
		sb.append(", lastModifiedTime=");
		sb.append(_lastModifiedTime);
		sb.append(", localFile=null}");

		Assert.assertEquals(sb.toString(), _fileResponse.toString());

		Path localFilePath = Paths.get("localFile");

		_fileResponse.setLocalFile(localFilePath);

		sb.setStringAt(", localFile=" + localFilePath.toString() + "}", 8);

		Assert.assertEquals(sb.toString(), _fileResponse.toString());

		FileResponse fileResponse = new FileResponse(
			_path, FileResponse.FILE_NOT_FOUND, _lastModifiedTime, _folder);

		sb = new StringBundler(8);

		sb.append("{path=");
		sb.append(_path.toAbsolutePath());
		sb.append(", isFolder=");
		sb.append(_folder);
		sb.append(", status=File Not Found");
		sb.append(", lastModifiedTime=");
		sb.append(_lastModifiedTime);
		sb.append(", localFile=null}");

		Assert.assertEquals(sb.toString(), fileResponse.toString());

		fileResponse = new FileResponse(
			_path, FileResponse.FILE_NOT_MODIFIED, _lastModifiedTime, _folder);

		sb = new StringBundler(8);

		sb.append("{path=");
		sb.append(_path.toAbsolutePath());
		sb.append(", isFolder=");
		sb.append(_folder);
		sb.append(", status=File Not Modified");
		sb.append(", lastModifiedTime=");
		sb.append(_lastModifiedTime);
		sb.append(", localFile=null}");

		Assert.assertEquals(sb.toString(), fileResponse.toString());
	}

	private FileResponse _fileResponse;
	private final boolean _folder = false;
	private final long _lastModifiedTime = System.currentTimeMillis();
	private final Path _path = Paths.get("testPath");
	private final long _size = System.currentTimeMillis();

}