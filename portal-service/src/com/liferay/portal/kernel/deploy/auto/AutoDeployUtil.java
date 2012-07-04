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

package com.liferay.portal.kernel.deploy.auto;

import com.liferay.portal.kernel.security.annotation.AccessControl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivica Cardic
 * @author Brian Wing Shun Chan
 */
@AccessControl
public class AutoDeployUtil {

	public static AutoDeployDir getDir(String name) {
		return _autoDeployDirs.get(name);
	}

	public static void registerDir(AutoDeployDir autoDeployDir) {
		_autoDeployDirs.put(autoDeployDir.getName(), autoDeployDir);

		autoDeployDir.start();
	}

	public static void unregisterDir(String name) {
		AutoDeployDir autoDeployDir = _autoDeployDirs.remove(name);

		if (autoDeployDir != null) {
			autoDeployDir.stop();
		}
	}

	private static Map<String, AutoDeployDir> _autoDeployDirs =
		new HashMap<String, AutoDeployDir>();;

}