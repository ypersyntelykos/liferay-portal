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

import com.liferay.portal.kernel.io.BigEndianCodec;
import com.liferay.portal.kernel.util.ReflectionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Shuyang Zhou
 */
public class FileHelperUtil {

	public static void delete(final boolean quite, Path... paths) {
		try {
			for (Path path : paths) {
				Files.walkFileTree(
					path, new SimpleFileVisitor<Path>() {

						@Override
						public FileVisitResult postVisitDirectory(
								Path dir, IOException ioe)
							throws IOException {

							if ((ioe != null) && !quite) {
								throw ioe;
							}

							Files.delete(dir);

							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFileFailed(
								Path file, IOException ioe)
							throws IOException {

							if (quite || (ioe instanceof NoSuchFileException)) {
								return FileVisitResult.CONTINUE;
							}

							throw ioe;
						}

						@Override
						public FileVisitResult visitFile(
								Path file,
								BasicFileAttributes basicFileAttributes)
							throws IOException {

							Files.delete(file);

							return FileVisitResult.CONTINUE;
						}

					});
			}
		}
		catch (IOException ioe) {
			if (!quite) {
				ReflectionUtil.throwException(ioe);
			}
		}
	}

	public static void delete(Path... paths) {
		delete(false, paths);
	}

	public static void move(
			final Path fromPath, final Path toPath, boolean tryAtomicMove)
		throws IOException {

		final AtomicBoolean atomicMove = new AtomicBoolean(tryAtomicMove);
		final AtomicBoolean touched = new AtomicBoolean();
		final Map<Path, FileTime> fileTimes = new HashMap<Path, FileTime>();

		try {
			Files.walkFileTree(
				fromPath,
				new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult postVisitDirectory(
							Path dir, IOException ioe)
						throws IOException {

						Files.setLastModifiedTime(
							toPath.resolve(fromPath.relativize(dir)),
							fileTimes.remove(dir));

						if (atomicMove.get()) {
							Files.delete(dir);
						}

						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(
							Path file, BasicFileAttributes basicFileAttributes)
						throws IOException {

						Path toFile = toPath.resolve(fromPath.relativize(file));

						if (atomicMove.get()) {
							try {
								Files.move(
									file, toFile,
									StandardCopyOption.ATOMIC_MOVE,
									StandardCopyOption.REPLACE_EXISTING);

								touched.set(true);

								return FileVisitResult.CONTINUE;
							}
							catch (AtomicMoveNotSupportedException amnse) {
								atomicMove.set(false);
							}
						}

						Files.copy(
							file, toFile, StandardCopyOption.COPY_ATTRIBUTES,
							StandardCopyOption.REPLACE_EXISTING);

						return FileVisitResult.CONTINUE;
					}

				@Override
				public FileVisitResult preVisitDirectory(
						Path dir, BasicFileAttributes basicFileAttributes)
					throws IOException {

					Files.copy(
						dir, toPath.resolve(fromPath.relativize(dir)),
						StandardCopyOption.COPY_ATTRIBUTES,
						StandardCopyOption.REPLACE_EXISTING);

					fileTimes.put(dir, Files.getLastModifiedTime(dir));

					return FileVisitResult.CONTINUE;
				}

			});
		}
		catch (IOException ioe) {
			delete(true, toPath);

			if (touched.get()) {
				throw new IOException(
					"Source path " + fromPath + " has been left in " +
						"inconsistent state.", ioe);
			}

			throw ioe;
		}

		if (!atomicMove.get()) {
			delete(true, fromPath);
		}
	}

	public static void move(Path fromPath, final Path toPath)
		throws IOException {

		move(fromPath, toPath, true);
	}

	public static void unzip(InputStream inputStream, Path destPath)
		throws IOException {

		try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
			ZipEntry zipEntry = null;

			while ((zipEntry = zipInputStream.getNextEntry()) != null) {
				if (zipEntry.isDirectory()) {
					continue;
				}

				Path entryPath = destPath.resolve(zipEntry.getName());

				Files.createDirectories(entryPath.getParent());

				long size = Files.copy(zipInputStream, entryPath);

				Files.setLastModifiedTime(
					entryPath,
					FileTime.fromMillis(
						BigEndianCodec.getLong(zipEntry.getExtra(), 0)));

				long length = BigEndianCodec.getLong(zipEntry.getExtra(), 8);

				if (size != length) {
					throw new IOException(
						"Corrupted zip stream for entry " + zipEntry.getName() +
							", expected size " + size + ", actual size " +
								length);
				}
			}
		}
	}

	public static Path unzip(Path sourcePath) throws IOException {
		Path destPath = Files.createTempDirectory(null);

		try (InputStream inputStream = Files.newInputStream(sourcePath)) {
			unzip(inputStream, destPath);
		}
		catch (IOException ioe) {
			delete(destPath);

			throw ioe;
		}

		return destPath;
	}

	public static void zip(final Path sourcePath, OutputStream outputStream)
		throws IOException {

		final byte[] buffer = new byte[16];

		try (ZipOutputStream zipOutputStream =
				new ZipOutputStream(outputStream)) {

			Files.walkFileTree(
				sourcePath, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(
							Path file, BasicFileAttributes basicFileAttributes)
						throws IOException {

						Path relativePath = sourcePath.relativize(file);

						ZipEntry zipEntry = new ZipEntry(
							relativePath.toString());

						FileTime fileTime =
							basicFileAttributes.lastModifiedTime();

						BigEndianCodec.putLong(buffer, 0, fileTime.toMillis());
						BigEndianCodec.putLong(
							buffer, 8, basicFileAttributes.size());

						zipEntry.setExtra(buffer);

						zipOutputStream.putNextEntry(zipEntry);

						Files.copy(file, zipOutputStream);

						zipOutputStream.closeEntry();

						return FileVisitResult.CONTINUE;
					}

				});
		}
	}

	public static Path zip(Path sourccePath) throws IOException {
		Path zipPath = Files.createTempFile(null, null);

		try (OutputStream outputStream = Files.newOutputStream(zipPath)) {
			zip(sourccePath, outputStream);
		}
		catch (IOException ioe) {
			Files.delete(zipPath);

			throw ioe;
		}

		return zipPath;
	}

}