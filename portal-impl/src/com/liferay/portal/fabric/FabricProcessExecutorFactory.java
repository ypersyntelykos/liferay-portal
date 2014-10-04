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

package com.liferay.portal.fabric;

import com.liferay.portal.fabric.agent.FabricAgentRegistry;
import com.liferay.portal.fabric.agent.selectors.FabricAgentSelector;

/**
 * @author Shuyang Zhou
 */
public class FabricProcessExecutorFactory {

	public static FabricProcessExecutor createFabricProcessExecutor(
			FabricAgentRegistry fabricAgentRegistry,
			String fabricAgentSelectorClassName)
		throws Exception {

		Thread currentThread = Thread.currentThread();

		ClassLoader classLoader = currentThread.getContextClassLoader();

		Class<? extends FabricAgentSelector> fabricAgentSelectorClass =
			(Class<? extends FabricAgentSelector>)classLoader.loadClass(
				fabricAgentSelectorClassName);

		return new FabricProcessExecutor(
			fabricAgentRegistry, fabricAgentSelectorClass.newInstance());
	}

}