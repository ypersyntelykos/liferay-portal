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

package com.liferay.portal.osgi.debug.spring.extender.internal;

import static java.lang.Thread.sleep;

import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.Time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.dm.ComponentDeclaration;
import org.apache.felix.dm.ComponentDependencyDeclaration;
import org.apache.felix.dm.DependencyManager;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Matthew Tambara
 */
@Component(
	configurationPid = "com.liferay.portal.osgi.debug.spring.extender.internal.UnavailableComponentScannerConfiguration",
	configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true
)
public class UnavailableComponentScanner {

	@Activate
	protected void activate(
		BundleContext bundleContext, Map<String, Object> properties) {

		UnavailableComponentScannerConfiguration
			unavailableComponentScannerConfiguration =
				ConfigurableUtil.createConfigurable(
					UnavailableComponentScannerConfiguration.class, properties);

		long scanningInterval =
			unavailableComponentScannerConfiguration.
				unavailableComponentScanningInterval();

		if (scanningInterval > 0) {
			_unavailableComponentScanningThread =
				new UnavailableComponentScanningThread(
					scanningInterval * Time.SECOND);

			_unavailableComponentScanningThread.start();
		}
	}

	@Deactivate
	protected void deactivate(BundleContext bundleContext)
		throws InterruptedException {

		if (_unavailableComponentScanningThread != null) {
			_unavailableComponentScanningThread.interrupt();

			_unavailableComponentScanningThread.join();
		}
	}

	private static void _scanUnavailableComponents() {
		StringBundler sb = new StringBundler();

		for (DependencyManager dependencyManager :
				(List<DependencyManager>)
					DependencyManager.getDependencyManagers()) {

			BundleContext bundleContext = dependencyManager.getBundleContext();

			Bundle bundle = bundleContext.getBundle();

			Map<ComponentDeclaration, List<ComponentDependencyDeclaration>>
				unavailableComponentDeclarations = new HashMap<>();

			for (ComponentDeclaration componentDeclaration :
					(List<ComponentDeclaration>)
						dependencyManager.getComponents()) {

				if (componentDeclaration.getState() !=
						ComponentDeclaration.STATE_UNREGISTERED) {

					continue;
				}

				List<ComponentDependencyDeclaration>
					componentDependencyDeclarations =
						unavailableComponentDeclarations.computeIfAbsent(
							componentDeclaration, key -> new ArrayList<>());

				for (ComponentDependencyDeclaration
						componentDependencyDeclaration :
							componentDeclaration.getComponentDependencies()) {

					if (componentDependencyDeclaration.getState() ==
							ComponentDependencyDeclaration.
								STATE_UNAVAILABLE_REQUIRED) {

						componentDependencyDeclarations.add(
							componentDependencyDeclaration);
					}
				}
			}

			if (!unavailableComponentDeclarations.isEmpty()) {
				sb.append("Found unavailable component in bundle ");
				sb.append(bundle);
				sb.append(".\n");

				for (Map.Entry<
						ComponentDeclaration,
						List<ComponentDependencyDeclaration>> entry :
							unavailableComponentDeclarations.entrySet()) {

					sb.append("\tComponent ");
					sb.append(entry.getKey());
					sb.append(" is unavailable due to missing required ");
					sb.append("dependencies: ");

					List<ComponentDependencyDeclaration>
						componentDependencyDeclarations = entry.getValue();

					for (int i = 0; i < componentDependencyDeclarations.size();
						i++) {

						if (i != 0) {
							sb.append(", ");
						}

						ComponentDependencyDeclaration
							componentDependencyDeclaration =
								componentDependencyDeclarations.get(i);

						sb.append(componentDependencyDeclaration);
					}

					sb.append(".");
				}
			}
		}

		if (sb.index() == 0) {
			if (_log.isInfoEnabled()) {
				_log.info(
					"All Spring extender dependency manager components are " +
						"registered");
			}
		}
		else {
			if (_log.isWarnEnabled()) {
				_log.warn(sb.toString());
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UnavailableComponentScanner.class);

	private Thread _unavailableComponentScanningThread;

	private static class UnavailableComponentScanningThread extends Thread {

		@Override
		public void run() {
			try {
				while (true) {
					sleep(_scanningInterval);

					_scanUnavailableComponents();
				}
			}
			catch (InterruptedException ie) {
				if (_log.isInfoEnabled()) {
					_log.info("Stopped scanning for unavailable components");
				}
			}
		}

		private UnavailableComponentScanningThread(long scanningInterval) {
			_scanningInterval = scanningInterval;

			setDaemon(true);
			setName("Spring Extender Unavailable Component Scanner");
		}

		private final long _scanningInterval;

	}

}