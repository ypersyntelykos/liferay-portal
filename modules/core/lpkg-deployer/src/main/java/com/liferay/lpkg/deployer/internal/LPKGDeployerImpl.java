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

package com.liferay.lpkg.deployer.internal;

import com.liferay.lpkg.deployer.LPKGDeployer;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

/**
 * @author Shuyang Zhou
 */
@Component(immediate = true, service = LPKGDeployer.class)
public class LPKGDeployerImpl implements LPKGDeployer {

	@Override
	public Bundle deploy(BundleContext bundleContext, File lpkgFile)
		throws IOException {

		try {
			return bundleContext.installBundle(
				lpkgFile.getCanonicalPath(), _lpkgToOSGiBundle(lpkgFile));
		}
		catch (BundleException be) {
			throw new IOException(be);
		}
	}

	private InputStream _lpkgToOSGiBundle(File lpkgFile) throws IOException {
		try (UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
				new UnsyncByteArrayOutputStream()) {

			try (ZipFile zipFile = new ZipFile(lpkgFile);
				JarOutputStream jarOutputStream = new JarOutputStream(
					unsyncByteArrayOutputStream)) {

				_writeManifest(zipFile, jarOutputStream);

				Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

				while (zipEntries.hasMoreElements()) {
					ZipEntry zipEntry = zipEntries.nextElement();

					jarOutputStream.putNextEntry(
						new ZipEntry(zipEntry.getName()));

					StreamUtil.transfer(
						zipFile.getInputStream(zipEntry), jarOutputStream,
						false);

					jarOutputStream.closeEntry();
				}
			}

			return new UnsyncByteArrayInputStream(
				unsyncByteArrayOutputStream.unsafeGetByteArray(), 0,
				unsyncByteArrayOutputStream.size());
		}
	}

	private void _writeManifest(
			ZipFile zipFile, JarOutputStream jarOutputStream)
		throws IOException {

		Manifest manifest = new Manifest();

		Attributes attributes = manifest.getMainAttributes();

		attributes.putValue("Manifest-Version", "2");
		attributes.putValue(Constants.BUNDLE_MANIFESTVERSION, "2");

		Properties properties = new Properties();

		properties.load(
			zipFile.getInputStream(
				zipFile.getEntry("liferay-marketplace.properties")));

		attributes.putValue(
			Constants.BUNDLE_SYMBOLICNAME, properties.getProperty("title"));
		attributes.putValue(
			Constants.BUNDLE_DESCRIPTION,
			properties.getProperty("description"));
		attributes.putValue(
			Constants.BUNDLE_VERSION, properties.getProperty("version"));

		jarOutputStream.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));

		manifest.write(jarOutputStream);

		jarOutputStream.closeEntry();
	}

}