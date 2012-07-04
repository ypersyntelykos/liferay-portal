/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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

package com.liferay.portal.security.pacl.checker;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.pacl.permission.PortalRuntimePermission;

import java.security.Permission;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Brian Wing Shun Chan
 */
public class PortalRuntimeChecker extends BaseChecker {

	public void afterPropertiesSet() {
		initExpandoBridgeClassNames();
		initSearchEngineIds();
		initThreadPoolExecutorNames();
	}

	public void checkPermission(Permission permission) {
		PortalRuntimePermission portalRuntimePermission =
			(PortalRuntimePermission)permission;

		String name = portalRuntimePermission.getName();
		Object subject = portalRuntimePermission.getSubject();

		if (name.equals(PORTAL_RUNTIME_PERMISSION_EXPANDO_BRIDGE)) {
			String className = (String)subject;

			if (!_expandoBridgeClassNames.contains(className)) {
				throwSecurityException(
					_log, "Attempted to get Expando bridge on " + className);
			}
		}
		else if (name.equals(PORTAL_RUNTIME_PERMISSION_SEARCH_ENGINE)) {
			String searchEngineId = (String)subject;

			if (!_searchEngineIds.contains(searchEngineId)) {
				throwSecurityException(
					_log, "Attempted to get search engine " + searchEngineId);
			}
		}
		else if (name.equals(PORTAL_RUNTIME_PERMISSION_THREAD_POOL_EXECUTOR)) {
			String threadPoolExecutorName = (String)subject;

			if (!_threadPoolExecutorNames.contains(threadPoolExecutorName)) {
				throwSecurityException(
					_log,
					"Attempted to modify thread pool executor " +
						threadPoolExecutorName);
			}
		}
	}

	protected void initExpandoBridgeClassNames() {
		_expandoBridgeClassNames = getPropertySet(
			"security-manager-expando-bridge");

		if (_log.isDebugEnabled()) {
			Set<String> classNames = new TreeSet<String>(
				_expandoBridgeClassNames);

			for (String className : classNames) {
				_log.debug("Allowing Expando bridge on class " + className);
			}
		}
	}

	protected void initSearchEngineIds() {
		_searchEngineIds = getPropertySet("security-manager-search-engine-ids");

		if (_log.isDebugEnabled()) {
			Set<String> searchEngineIds = new TreeSet<String>(_searchEngineIds);

			for (String searchEngineId : searchEngineIds) {
				_log.debug("Allowing search engine " + searchEngineId);
			}
		}
	}

	protected void initThreadPoolExecutorNames() {
		_threadPoolExecutorNames = getPropertySet(
			"security-manager-thread-pool-executor-names");

		if (_log.isDebugEnabled()) {
			Set<String> threadPoolExecutorNames = new TreeSet<String>(
				_threadPoolExecutorNames);

			for (String threadPoolExecutorName : threadPoolExecutorNames) {
				_log.debug(
					"Allowing thread pool executor " + threadPoolExecutorName);
			}
		}
	}

	private static Log _log = LogFactoryUtil.getLog(PortalRuntimeChecker.class);

	private Set<String> _expandoBridgeClassNames;
	private Set<String> _searchEngineIds;
	private Set<String> _threadPoolExecutorNames;

}