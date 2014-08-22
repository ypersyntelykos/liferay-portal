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

import com.liferay.portal.fabric.FabricProcessExecutor;
import com.liferay.portal.fabric.agent.FabricAgent;
import com.liferay.portal.fabric.agent.FabricAgentRegistry;
import com.liferay.portal.fabric.agent.FabricAgentSelector;
import com.liferay.portal.fabric.local.agent.LocalFabricAgent;
import com.liferay.portal.fabric.server.FabricServerUtil;
import com.liferay.portal.kernel.process.LocalProcessExecutor;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessConfig;
import com.liferay.portal.kernel.process.ProcessException;
import com.liferay.portal.kernel.process.ProcessExecutorUtil;
import com.liferay.portal.kernel.util.ClassLoaderPool;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;

import io.netty.util.ResourceLeakDetector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Shuyang Zhou
 */
public class TestServer {

	public static void main(String[] args) throws Exception {
		ClassLoaderPool.register("", TestServer.class.getClassLoader());

		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

		PortalClassLoaderUtil.setClassLoader(TestServer.class.getClassLoader());

		ProcessExecutorUtil processExecutorUtil = new ProcessExecutorUtil();

		processExecutorUtil.setProcessExecutor(new LocalProcessExecutor());

		//--------------------------------------------------------------------//

		FabricAgentRegistry fabricAgentRegistry = new FabricAgentRegistry(
			new LocalFabricAgent());

		FabricAgentSelector fabricAgentSelector = new FabricAgentSelector() {

			@Override
			public Collection<FabricAgent> select(
				Collection<FabricAgent> fabricAgents,
				ProcessCallable<?> processCallable) {

				List<FabricAgent> fabricAgentsList =
					new ArrayList<FabricAgent>();

				for (FabricAgent fabricAgent : fabricAgents) {
					if (!(fabricAgent instanceof LocalFabricAgent)) {
						fabricAgentsList.add(fabricAgent);
					}
				}

				if (fabricAgentsList.isEmpty()) {
					return fabricAgents;
				}

				return fabricAgentsList;
			}

		};

		FabricProcessExecutor fabricProcessExecutor = new FabricProcessExecutor(
			fabricAgentRegistry, fabricAgentSelector);

		NettyFabricServer nettyFabricServer = new NettyFabricServer(
			fabricAgentRegistry);

		FabricServerUtil fabricServerUtil = new FabricServerUtil();

		fabricServerUtil.setFabricServer(nettyFabricServer);

		//--------------------------------------------------------------------//

		FabricServerUtil.start();

		ProcessConfig processConfig = new ProcessConfig.Builder().build();
		ProcessCallable<String> processCallable =
			new HelloWorldProcessCallable();

		Future<String> future = fabricProcessExecutor.execute(
			processConfig, processCallable);

		System.out.println("Location 1 : " + future.get());

		Thread.sleep(10 * 1000);

		future = fabricProcessExecutor.execute(processConfig, processCallable);

		System.out.println("Location 2 : " + future.get());

		FabricServerUtil.stop();
	}

	private static final class HelloWorldProcessCallable
		implements ProcessCallable<String> {

		@Override
		public String call() throws ProcessException {
			System.out.println("Hello World!!!!!!");

			return "I said Hello world!!!!!!";
		}

		private static final long serialVersionUID = 1L;

	}

}