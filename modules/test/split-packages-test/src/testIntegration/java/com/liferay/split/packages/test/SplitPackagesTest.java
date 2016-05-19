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

package com.liferay.split.packages.test;

import aQute.bnd.header.OSGiHeader;
import aQute.bnd.header.Parameters;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.util.HashUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.module.framework.ModuleFrameworkUtilAdapter;

import java.io.IOException;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * @author Tom Wang
 */
@RunWith(Arquillian.class)
public class SplitPackagesTest {

	@Test
	public void testSplitPackage() throws IOException {
		Bundle frameworkBundle =
			(Bundle)ModuleFrameworkUtilAdapter.getFramework();

		BundleContext frameworkBundleContext =
			frameworkBundle.getBundleContext();

		Bundle[] frameworkBundles = frameworkBundleContext.getBundles();

		Map<Bundle, Map<String, ExportPackage>> bundlesMap = new HashMap<>();

		Map<String, Packages> suitableImportsMap = _createSuitableImportsMap();

		for (Bundle bundle : frameworkBundles) {
			Map<String, ExportPackage> bundleExportPackages =
				_getBundleExportPackages(bundle);

			if (bundleExportPackages == null) {
				continue;
			}

			for (Map.Entry<Bundle, Map<String, ExportPackage>> entry :
					bundlesMap.entrySet()) {

				Map<String, ExportPackage> mapBundlePackages = new HashMap<>(
					entry.getValue());

				Set<String> keySet = mapBundlePackages.keySet();

				keySet.retainAll(bundleExportPackages.keySet());

				if (!mapBundlePackages.isEmpty()) {
					_processDuplicatedPackages(
						entry.getKey(), mapBundlePackages.values(),
						bundleExportPackages, suitableImportsMap,
						bundle.getSymbolicName());
				}
			}

			bundlesMap.put(bundle, bundleExportPackages);
		}
	}

	private boolean _checkSuitableImportContains(
		Packages suitableImportPackage, ExportPackage exportPackage,
		String currentSymbolicName) {

		String declaredVersion = suitableImportPackage.getVersion();
		String declaredBundles = suitableImportPackage.getBundles();

		if (declaredVersion.equals(exportPackage._version) &&
			declaredBundles.contains(currentSymbolicName)) {

			return true;
		}

		return false;
	}

	private Map<String, Packages> _createSuitableImportsMap()
		throws IOException {

		Class clazz = this.getClass();

		ClassLoader classLoader = clazz.getClassLoader();

		Map<String, Packages> suitableImports = new HashMap<>();

		for (String packages :
				StringUtil.split(
					IOUtils.toString(
						classLoader.getResourceAsStream("SuitableImports"),
						StringPool.UTF8),'\n')) {

			String[] packageSplit = StringUtil.split(packages, ';');

			suitableImports.put(
				packageSplit[0],
				new Packages(
					packageSplit[0], packageSplit[1], packageSplit[2]));
		}

		return suitableImports;
	}

	private Map<String, ExportPackage> _getBundleExportPackages(Bundle bundle) {
		Dictionary<String, String> headers = bundle.getHeaders();

		String exportPackage = headers.get(Constants.EXPORT_PACKAGE);

		if (exportPackage == null) {
			return null;
		}

		Parameters parameters = OSGiHeader.parseHeader(exportPackage);

		Map<String, ? extends Map<String, String>> exportPackages =
			parameters.asMapMap();

		Map<String, ExportPackage> bundleExportPackages = new HashMap<>();

		for (Map.Entry<String, ? extends Map<String, String>> entry :
				exportPackages.entrySet()) {

			String packageName = entry.getKey();

			Map<String, String> attributes = entry.getValue();

			String version = attributes.get(Constants.VERSION_ATTRIBUTE);

			if (version == null) {
				version = "0.0";
			}

			bundleExportPackages.put(
				packageName, new ExportPackage(packageName, version));
		}

		return bundleExportPackages;
	}

	private void _processDuplicatedPackages(
		Bundle mapBundle, Collection<ExportPackage> duplicatedExportPackages,
		Map<String, ExportPackage> currentBundleExportPackages,
		Map<String, Packages> suitableImportsMap, String currentSymbolicName) {

		String symbolicName = mapBundle.getSymbolicName();

		for (ExportPackage duplicatedExportPackage : duplicatedExportPackages) {
			String duplicatedPackageName = duplicatedExportPackage._name;

			ExportPackage exportPackage = currentBundleExportPackages.get(
				duplicatedPackageName);

			if (Objects.equals(
					exportPackage._version, duplicatedExportPackage._version)) {

				Assert.assertTrue(
					"Detected split packages in " + currentSymbolicName +
						" and " + symbolicName + ": " + duplicatedPackageName,
					suitableImportsMap.containsKey(duplicatedPackageName));

				Assert.assertTrue(
					"Detected split packages in " + currentSymbolicName +
						" and " + symbolicName + ": " +
							duplicatedExportPackages,
					_checkSuitableImportContains(
						suitableImportsMap.get(duplicatedPackageName),
						exportPackage, currentSymbolicName));
			}
		}
	}

	private class ExportPackage {

		@Override
		public boolean equals(Object obj) {
			ExportPackage exportPackage = (ExportPackage)obj;

			if (Objects.equals(_name, exportPackage._name) &&
				Objects.equals(_version, exportPackage._version)) {

				return true;
			}

			return false;
		}

		@Override
		public int hashCode() {
			int hashCode = HashUtil.hash(0, _name);

			return HashUtil.hash(hashCode, _version);
		}

		private ExportPackage(String name, String version) {
			_name = name;
			_version = version;
		}

		private final String _name;
		private final String _version;

	}

	private class Packages {

		public Packages(String name, String version, String bundles) {
			_name = name;
			_version = version;
			_bundles = bundles;
		}

		public String getBundles() {
			return _bundles;
		}

		public String getName() {
			return _name;
		}

		public String getVersion() {
			return _version;
		}

		private final String _bundles;
		private final String _name;
		private final String _version;

	}

}