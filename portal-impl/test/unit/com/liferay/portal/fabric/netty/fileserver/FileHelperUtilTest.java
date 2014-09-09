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

import com.liferay.portal.kernel.test.SwappableSecurityManager;
import com.liferay.portal.kernel.util.ReflectionUtil;

import java.io.File;
import java.io.IOException;

import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Shuyang Zhou
 */
public class FileHelperUtilTest {

	@Test
	public void testConstructor() {
		new FileHelperUtil();
	}

	@Test
	public void testDeleteNoSuchFile() throws IOException {
		Path noSuchFilePath = Paths.get("NoSuchFile");

		Files.deleteIfExists(noSuchFilePath);

		FileHelperUtil.delete(true, noSuchFilePath);
		FileHelperUtil.delete(noSuchFilePath);
	}

	@Test
	public void testDeleteRegularDirectoryWithRegularFile() throws IOException {
		Path regularDirectoryPath = Paths.get("RegularDirectory");

		Path regularFilePath = regularDirectoryPath.resolve("RegularFile");

		createFile(regularFilePath);

		FileHelperUtil.delete(true, regularDirectoryPath);

		Assert.assertTrue(Files.notExists(regularFilePath));
		Assert.assertTrue(Files.notExists(regularDirectoryPath));

		createFile(regularFilePath);

		FileHelperUtil.delete(regularDirectoryPath);

		Assert.assertTrue(Files.notExists(regularFilePath));
		Assert.assertTrue(Files.notExists(regularDirectoryPath));
	}

	@Test
	public void testDeleteRegularDirectoryWithUndeleteableFile()
		throws IOException {

		final IOException ioException = new IOException("Unable to delete");

		Path regularDirectoryPath = Paths.get("RegularDirectory");

		final Path undeleteableFilePath = regularDirectoryPath.resolve(
			"UndeleteableFile");

		createFile(undeleteableFilePath);

		try (SwappableSecurityManager swappableSecurityManager =
				new SwappableSecurityManager() {

					@Override
					public void checkDelete(String file) {
						if (file.equals(undeleteableFilePath.toString())) {
							ReflectionUtil.throwException(
								new DirectoryIteratorException(ioException));
						}
					}

				}) {

			swappableSecurityManager.install();

			FileHelperUtil.delete(true, regularDirectoryPath);
			FileHelperUtil.delete(regularDirectoryPath);

			Assert.fail();
		}
		catch (Exception e) {
			Assert.assertSame(ioException, e);
		}
		finally {
			Files.delete(undeleteableFilePath);
			Files.delete(regularDirectoryPath);
		}
	}

	@Test
	public void testDeleteRegularFile() throws IOException {
		Path regularFilePath = Paths.get("RegularFile");

		createFile(regularFilePath);

		FileHelperUtil.delete(true, regularFilePath);

		Assert.assertTrue(Files.notExists(regularFilePath));

		createFile(regularFilePath);

		FileHelperUtil.delete(regularFilePath);

		Assert.assertTrue(Files.notExists(regularFilePath));
	}

	@Test
	public void testDeleteUndeleteableDirectoryWithRegularFile()
		throws IOException {

		final Path undeleteableDirectoryPath = Paths.get(
			"UndeleteableDirectory");

		Path regularFilePath = undeleteableDirectoryPath.resolve("RegularFile");

		final Path newRegularFilePath = undeleteableDirectoryPath.resolve(
			"NewRegularFile");

		createFile(regularFilePath);

		try (SwappableSecurityManager swappableSecurityManager =
				new SwappableSecurityManager() {

					@Override
					public void checkDelete(String file) {
						if (!file.equals(
								undeleteableDirectoryPath.toString())) {

							return;
						}

						try {
							createFile(newRegularFilePath);
						}
						catch (IOException ioe) {
							ReflectionUtil.throwException(ioe);
						}
					}

				}) {

			swappableSecurityManager.install();

			FileHelperUtil.delete(true, undeleteableDirectoryPath);

			Files.delete(newRegularFilePath);
			createFile(regularFilePath);

			FileHelperUtil.delete(undeleteableDirectoryPath);

			Assert.fail();
		}
		catch (Exception e) {
			Assert.assertSame(DirectoryNotEmptyException.class, e.getClass());
		}
		finally {
			Files.delete(newRegularFilePath);
			Files.delete(undeleteableDirectoryPath);
		}
	}

	@Test
	public void testDeleteUndeleteableFile() throws IOException {
		final IOException ioException = new IOException("Unable to delete");

		final Path undeleteableFilePath = Paths.get("UndeleteableFile");

		createFile(undeleteableFilePath);

		try (SwappableSecurityManager swappableSecurityManager =
				new SwappableSecurityManager() {

					@Override
					public void checkDelete(String file) {
						if (file.equals(undeleteableFilePath.toString())) {
							ReflectionUtil.throwException(ioException);
						}
					}

				}) {

			swappableSecurityManager.install();

			FileHelperUtil.delete(true, undeleteableFilePath);
			FileHelperUtil.delete(undeleteableFilePath);

			Assert.fail();
		}
		catch (Exception e) {
			Assert.assertSame(ioException, e);
		}
		finally {
			Files.delete(undeleteableFilePath);
		}
	}

	@Test
	public void testDeleteUnreadableFile() throws IOException {
		final IOException ioException = new IOException("Unable to read");

		final Path unreadableFilePath = Paths.get("UnreadableFile");

		createFile(unreadableFilePath);

		try (SwappableSecurityManager swappableSecurityManager =
				new SwappableSecurityManager() {

					@Override
					public void checkRead(String file) {
						if (file.equals(unreadableFilePath.toString())) {
							ReflectionUtil.throwException(ioException);
						}
					}

				}) {

			swappableSecurityManager.install();

			FileHelperUtil.delete(true, unreadableFilePath);
			FileHelperUtil.delete(unreadableFilePath);

			Assert.fail();
		}
		catch (Exception e) {
			Assert.assertSame(ioException, e);
		}
		finally {
			Files.delete(unreadableFilePath);
		}
	}

	private void createFile(Path path) throws IOException {
		boolean deleteDirectory = false;

		Path parentPath = path.getParent();

		if ((parentPath != null) && Files.notExists(parentPath)) {
			Files.createDirectories(parentPath);

			deleteDirectory = true;
		}

		Files.createFile(path);

		File file = path.toFile();

		file.deleteOnExit();

		if (deleteDirectory) {
			file = parentPath.toFile();

			file.deleteOnExit();
		}
	}

}