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

package com.liferay.portal.fabric.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Shuyang Zhou
 */
public class FabricAgentRegistry {

	public FabricAgentRegistry(FabricAgent defaultFabricAgent) {
		if (defaultFabricAgent == null) {
			throw new NullPointerException("Default fabric agent is null");
		}

		_defaultFabricAgent = defaultFabricAgent;
	}

	public FabricAgent getDefaultFabricAgent() {
		return _defaultFabricAgent;
	}

	public List<FabricAgent> getFabricAgents() {
		return new ArrayList<FabricAgent>(_fabricAgents);
	}

	public boolean registerFabricAgent(FabricAgent fabricAgent) {
		return _fabricAgents.addIfAbsent(fabricAgent);
	}

	public boolean unregisterFabricAgent(FabricAgent fabricAgent) {
		return _fabricAgents.remove(fabricAgent);
	}

	private final FabricAgent _defaultFabricAgent;
	private final CopyOnWriteArrayList<FabricAgent> _fabricAgents =
		new CopyOnWriteArrayList<FabricAgent>();

}