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

import com.liferay.portal.fabric.netty.client.NettyFabricClient;
import com.liferay.portal.fabric.netty.fileserver.FileHelperUtil;
import com.liferay.portal.fabric.netty.server.TestServer;
import com.liferay.portal.kernel.process.ProcessExecutor;
import com.liferay.portal.kernel.process.ProcessExecutorUtil;
import com.liferay.portal.kernel.process.local.LocalProcessExecutor;
import com.liferay.portal.kernel.util.ClassLoaderPool;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;

import io.netty.util.ResourceLeakDetector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Shuyang Zhou
 */
public class TestClient {

	public static void main(String[] args) throws Exception {
		ClassLoaderPool.register("", TestClient.class.getClassLoader());

		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

		PortalClassLoaderUtil.setClassLoader(TestServer.class.getClassLoader());

		ProcessExecutorUtil processExecutorUtil = new ProcessExecutorUtil();

		ProcessExecutor processExecutor = new LocalProcessExecutor();

		processExecutorUtil.setProcessExecutor(processExecutor);

		Logger logger = Logger.getLogger(FileHelperUtil.class.getName());

		logger.setLevel(Level.FINEST);

		for (Handler handler : Logger.getLogger("").getHandlers()) {
			handler.setLevel(Level.FINEST);
		}

		//--------------------------------------------------------------------//

		Path repositoryFolder = Paths.get(
			System.getProperty("user.home") + "/test-repo");

		Files.createDirectories(repositoryFolder);

		NettyFabricClient nettyFabricClient = new NettyFabricClient(
			repositoryFolder, processExecutor);

		FabricClientUtil fabricClientUtil = new FabricClientUtil();

		fabricClientUtil.setFabricClient(nettyFabricClient);

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