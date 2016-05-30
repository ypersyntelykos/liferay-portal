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

package com.liferay.portal.lpkg.deployer.internal;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayOutputStream;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.lpkg.deployer.LPKGVerifyException;
import com.liferay.portal.target.platform.indexer.IndexValidator;
import com.liferay.portal.target.platform.indexer.IndexValidatorFactory;
import com.liferay.portal.target.platform.indexer.Indexer;
import com.liferay.portal.target.platform.indexer.IndexerFactory;
import com.liferay.portal.util.PropsValues;

import java.io.File;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Shuyang Zhou
 */
@Component(immediate = true, service = LPKGIndexValidator.class)
public class LPKGIndexValidator {

	public void validate(List<File> lpkgFiles) throws Exception {
		long start = System.currentTimeMillis();

		List<URI> uris = _indexLPKGFiles(lpkgFiles);

		IndexValidator indexValidator = _indexValidatorFactory.create(
			_getTargetPlatformIndexURIs());

		try {
			List<String> messages = indexValidator.validate(uris);

			if (!messages.isEmpty()) {
				StringBundler sb = new StringBundler((messages.size() * 3) + 1);

				sb.append("LPKG validation failed with {");

				for (String message : messages) {
					sb.append("[");
					sb.append(message);
					sb.append("], ");
				}

				sb.setIndex(sb.index() - 1);

				sb.append("]}");

				throw new LPKGVerifyException(sb.toString());
			}
		}
		finally {
			_cleanUp(uris);

			if (_log.isInfoEnabled()) {
				long duration = System.currentTimeMillis() - start;

				_log.info(
					String.format(
						"LPKG validation time %02d:%02ds",
						MILLISECONDS.toMinutes(duration),
						MILLISECONDS.toSeconds(duration % Time.MINUTE)));
			}
		}
	}

	private void _cleanUp(List<URI> uris) throws MalformedURLException {
		for (URI uri : uris) {
			_bytesURLProtocolSupport.removeData(uri.toURL());
		}
	}

	private List<URI> _getTargetPlatformIndexURIs() throws IOException {
		List<URI> uris = new ArrayList<>();

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
				Paths.get(
					PropsValues.MODULE_FRAMEWORK_BASE_DIR,
					Indexer.DIR_NAME_TARGET_PLATFORM),
				"*.xml")) {

			Iterator<Path> iterator = directoryStream.iterator();

			while (iterator.hasNext()) {
				Path path = iterator.next();

				uris.add(path.toUri());
			}
		}

		return uris;
	}

	private List<URI> _indexLPKGFiles(List<File> lpkgFiles) throws Exception {
		List<URI> uris = new ArrayList<>();

		UnsyncByteArrayOutputStream unsyncByteArrayOutputStream =
			new UnsyncByteArrayOutputStream();

		try {
			for (File lpkgFile : lpkgFiles) {
				Indexer indexer = _indexerFactory.createLPKGIndexer(lpkgFile);

				indexer.index(unsyncByteArrayOutputStream);

				String name = lpkgFile.getName();

				URL url = _bytesURLProtocolSupport.putData(
					name.substring(0, name.length() - 5),
					unsyncByteArrayOutputStream.toByteArray());

				unsyncByteArrayOutputStream.reset();

				uris.add(url.toURI());
			}
		}
		catch (Exception e) {
			_cleanUp(uris);

			throw e;
		}

		return uris;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		LPKGIndexValidator.class);

	@Reference
	private BytesURLProtocolSupport _bytesURLProtocolSupport;

	@Reference
	private IndexerFactory _indexerFactory;

	@Reference
	private IndexValidatorFactory _indexValidatorFactory;

}