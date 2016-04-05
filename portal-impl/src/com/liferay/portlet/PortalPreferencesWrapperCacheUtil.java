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

package com.liferay.portlet;

import com.liferay.portal.kernel.cache.MultiVMPoolUtil;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.cache.PortalCacheHelperUtil;
import com.liferay.portal.kernel.util.HashUtil;

import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class PortalPreferencesWrapperCacheUtil {

	public static final String CACHE_NAME =
		PortalPreferencesWrapperCacheUtil.class.getName();

	public static PortalPreferencesWrapper get(long ownerId, int ownerType) {
		return _portalPreferencesWrapperPortalCache.get(
			new CacheKey(ownerId, ownerType));
	}

	public static void put(
		long ownerId, int ownerType,
		PortalPreferencesWrapper portalPreferencesWrapper) {

		PortalCacheHelperUtil.putWithoutReplicator(
			_portalPreferencesWrapperPortalCache,
			new CacheKey(ownerId, ownerType), portalPreferencesWrapper);
	}

	public static void remove(long ownerId, int ownerType) {
		_portalPreferencesWrapperPortalCache.remove(
			new CacheKey(ownerId, ownerType));
	}

	private static final PortalCache<CacheKey, PortalPreferencesWrapper>
		_portalPreferencesWrapperPortalCache = MultiVMPoolUtil.getPortalCache(
			CACHE_NAME);

	private static class CacheKey implements Serializable {

		public CacheKey(long ownerId, int ownerType) {
			_ownerId = ownerId;
			_ownerType = ownerType;
		}

		@Override
		public boolean equals(Object obj) {
			CacheKey cacheKey = (CacheKey)obj;

			if ((cacheKey._ownerId == _ownerId) &&
				(cacheKey._ownerType == _ownerType)) {

				return true;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return HashUtil.hash(_ownerType, _ownerId);
		}

		private long _ownerId;
		private int _ownerType;

	}

}