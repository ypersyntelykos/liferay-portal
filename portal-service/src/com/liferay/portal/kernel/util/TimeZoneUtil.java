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

package com.liferay.portal.kernel.util;

import com.liferay.portal.kernel.security.annotation.AccessControl;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author Brian Wing Shun Chan
 */
@AccessControl
public class TimeZoneUtil {

	public static TimeZone getDefault() {
		TimeZone timeZone = TimeZoneThreadLocal.getDefaultTimeZone();

		if (timeZone != null) {
			return timeZone;
		}

		return _timeZone;
	}

	public static TimeZone getTimeZone(String timeZoneId) {
		TimeZone timeZone = _timeZones.get(timeZoneId);

		if (timeZone == null) {
			timeZone = TimeZone.getTimeZone(timeZoneId);

			_timeZones.put(timeZoneId, timeZone);
		}

		return timeZone;
	}

	public static void setDefault(String timeZoneId) {
		if (Validator.isNotNull(timeZoneId)) {
			_timeZone = TimeZone.getTimeZone(timeZoneId);
		}
	}

	private static TimeZone _timeZone;
	private static Map<String, TimeZone> _timeZones =
		new HashMap<String, TimeZone>();

	static {
		_timeZone = getTimeZone(StringPool.UTC);
	}

}