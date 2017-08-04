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

package com.liferay.portal.lpkg.deployer.util;

import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.StringBundler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.file.Path;

import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.osgi.framework.Constants;

/**
 * @author Matthew Tambara
 */
public class LPKGTestUtil {

	public static InputStream createJAR(String symbolicName)
		throws IOException {

		try (UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
				new UnsyncByteArrayOutputStream()) {

			try (JarOutputStream jarOutputStream = new JarOutputStream(
					unsyncByteArrayOutputStream)) {

				Manifest manifest = new Manifest();

				Attributes attributes = manifest.getMainAttributes();

				attributes.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");
				attributes.putValue(
					Constants.BUNDLE_SYMBOLICNAME, symbolicName);
				attributes.putValue(Constants.BUNDLE_VERSION, "1.0.0");
				attributes.putValue("Manifest-Version", "2");

				jarOutputStream.putNextEntry(
					new ZipEntry(JarFile.MANIFEST_NAME));

				manifest.write(jarOutputStream);

				jarOutputStream.closeEntry();
			}

			return new UnsyncByteArrayInputStream(
				unsyncByteArrayOutputStream.unsafeGetByteArray(), 0,
				unsyncByteArrayOutputStream.size());
		}
	}

	public static void createLPKG(
			Path path, String symbolicName, boolean createWar)
		throws IOException {

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(
				new FileOutputStream(path.toFile()))) {

			zipOutputStream.putNextEntry(
				new ZipEntry("liferay-marketplace.properties"));

			StringBundler sb = new StringBundler(11);

			sb.append("bundles=");
			sb.append(symbolicName);
			sb.append("#1.0.0##\n");
			sb.append("category=Test\n");
			sb.append("context-names=\n");
			sb.append("description=Test\n");
			sb.append("icon-url=https://www.liferay.com/web/guest");
			sb.append("/marketplace/-/mp/asset/icon/71985553\n");
			sb.append("remote-app-id=Test\n");
			sb.append("title=Test\n");
			sb.append("version=1.0");

			String properties = sb.toString();

			zipOutputStream.write(properties.getBytes());

			zipOutputStream.closeEntry();

			zipOutputStream.putNextEntry(
				new ZipEntry(symbolicName.concat("-1.0.0.jar")));

			try (InputStream inputStream = createJAR(symbolicName);
				OutputStream outputStream = StreamUtil.uncloseable(
					zipOutputStream)) {

				StreamUtil.transfer(inputStream, outputStream);
			}

			zipOutputStream.closeEntry();

			if (createWar) {
				zipOutputStream.putNextEntry(
					new ZipEntry(symbolicName.concat("-war-1.0.0.war")));

				try (InputStream inputStream = createWAR(symbolicName);
					OutputStream outputStream = StreamUtil.uncloseable(
						zipOutputStream)) {

					StreamUtil.transfer(inputStream, outputStream);
				}
			}
		}
	}

	public static InputStream createWAR(String symbolicName)
		throws IOException {

		try (UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
				new UnsyncByteArrayOutputStream()) {

			try (JarOutputStream jarOutputStream = new JarOutputStream(
					unsyncByteArrayOutputStream)) {

				jarOutputStream.putNextEntry(
					new ZipEntry("WEB-INF/liferay-plugin-package.properties"));

				StringBundler sb = new StringBundler(12);

				sb.append("author=Liferay, Inc.\n");
				sb.append("change-log=\n");
				sb.append("licenses=LGPL\n");
				sb.append("liferay-versions=7.0.1+\n");
				sb.append("long-description=\n");
				sb.append("module-group-id=liferay\n");
				sb.append("module-incremental-version=1\n");
				sb.append("page-url=http://www.liferay.com\n");
				sb.append("module-version=1.0.20\n");
				sb.append("name=");
				sb.append(symbolicName);
				sb.append("-war");

				String properties = sb.toString();

				jarOutputStream.write(properties.getBytes());

				jarOutputStream.closeEntry();
			}

			return new UnsyncByteArrayInputStream(
				unsyncByteArrayOutputStream.unsafeGetByteArray(), 0,
				unsyncByteArrayOutputStream.size());
		}
	}

}