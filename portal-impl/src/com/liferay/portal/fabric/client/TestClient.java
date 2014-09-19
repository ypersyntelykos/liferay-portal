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

package com.liferay.portal.fabric.client;

import com.liferay.portal.fabric.local.agent.LocalFabricAgent;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentSkeleton;
import com.liferay.portal.fabric.netty.client.NettyFabricClient;
import com.liferay.portal.fabric.netty.fileserver.FileHelperUtil;
import com.liferay.portal.fabric.netty.fileserver.FileResponse;
import com.liferay.portal.fabric.netty.server.TestServer;
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.process.ProcessExecutorUtil;
import com.liferay.portal.kernel.process.local.LocalProcessExecutor;
import com.liferay.portal.kernel.util.ClassLoaderPool;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;

import io.netty.util.ResourceLeakDetector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Shuyang Zhou
 */
public class TestClient {

	public static void main(String[] args) throws Exception {
		ClassLoaderPool.register("", TestClient.class.getClassLoader());

		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

		PortalClassLoaderUtil.setClassLoader(TestServer.class.getClassLoader());

		ProcessExecutorUtil processExecutorUtil = new ProcessExecutorUtil();

		processExecutorUtil.setProcessExecutor(new LocalProcessExecutor());

		//--------------------------------------------------------------------//

		Path repositoryFolder = Paths.get("~/test-repo");

		Files.createDirectories(repositoryFolder);

		AsyncBroker<String, FileResponse> asyncBroker =
			new AsyncBroker<String, FileResponse>();

		NettyFabricClient nettyFabricClient = new NettyFabricClient(
			new NettyFabricAgentSkeleton(
				new LocalFabricAgent(), repositoryFolder),
			asyncBroker);

		FabricClientUtil fabricClientUtil = new FabricClientUtil();

		fabricClientUtil.setFabricServer(nettyFabricClient);

		//--------------------------------------------------------------------//

		try {
			FabricClientUtil.connect();

//			Thread.sleep(20 * 1000);
			Thread.sleep(Long.MAX_VALUE);

			FabricClientUtil.disconnect();
		}
		finally {
			FileHelperUtil.delete(repositoryFolder);
		}
	}

}