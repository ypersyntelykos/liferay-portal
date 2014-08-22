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

package com.liferay.portal.fabric.local.agent;

import com.liferay.portal.fabric.FabricException;
import com.liferay.portal.fabric.agent.FabricAgent;
import com.liferay.portal.fabric.local.worker.LocalFabricWorker;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessConfig;
import com.liferay.portal.kernel.process.ProcessException;
import com.liferay.portal.kernel.process.ProcessExecutorUtil;

import java.io.Serializable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Shuyang Zhou
 */
public class LocalFabricAgent implements FabricAgent {

	@Override
	public <T extends Serializable> LocalFabricWorker<T> execute(
			ProcessConfig processConfig, ProcessCallable<T> processCallable)
		throws FabricException {

		try {
			NoticeableFuture<T> future = ProcessExecutorUtil.execute(
				processConfig, processCallable);

			LocalFabricWorker<T> localFabricWorker = new LocalFabricWorker<T>(
				future);

			_fabricWorkerQueue.add(localFabricWorker);

			return localFabricWorker;
		}
		catch (ProcessException pe) {
			throw new FabricException(pe);
		}
	}

	private final Queue<LocalFabricWorker<?>> _fabricWorkerQueue =
		new ConcurrentLinkedQueue<LocalFabricWorker<?>>();

}