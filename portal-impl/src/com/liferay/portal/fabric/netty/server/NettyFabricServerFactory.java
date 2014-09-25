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

package com.liferay.portal.fabric.netty.server;

import com.liferay.portal.fabric.agent.FabricAgentRegistry;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricServerFactory {

	public static NettyFabricServer createNettyFabricServer(
			FabricAgentRegistry fabricAgentRegistry)
		throws IOException {

		// TODO load from property

		Path repositoryParentFolder = Paths.get(
			System.getProperty("user.home") + "/test-repo/parent");

		Files.createDirectories(repositoryParentFolder);

		return new NettyFabricServer(
			fabricAgentRegistry, repositoryParentFolder);
	}

}