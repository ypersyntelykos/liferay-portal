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

package com.liferay.portal.kernel.deploy.sandbox;

import com.liferay.portal.kernel.security.annotation.AccessControl;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Igor Spasic
 * @author Brian Wing Shun Chan
 */
@AccessControl
public class SandboxDeployUtil {

	public static SandboxDeployDir getDir(String name) {
		return _sandboxDeployDirs.get(name);
	}

	public static void registerDir(SandboxDeployDir sandboxDeployDir) {
		_sandboxDeployDirs.put(sandboxDeployDir.getName(), sandboxDeployDir);

		sandboxDeployDir.start();
	}

	public static void unregisterDir(String name) {
		SandboxDeployDir sandboxDeployDir = _sandboxDeployDirs.remove(name);

		if (sandboxDeployDir != null) {
			sandboxDeployDir.stop();
		}
	}

	private static Map<String, SandboxDeployDir> _sandboxDeployDirs =
		new HashMap<String, SandboxDeployDir>();

}