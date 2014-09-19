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

package com.liferay.portal.fabric.netty.agent;

import com.liferay.portal.fabric.FabricRemote;
import com.liferay.portal.fabric.agent.FabricAgent;
import com.liferay.portal.fabric.netty.fileserver.FileResponse;
import com.liferay.portal.fabric.netty.repository.Repository;
import com.liferay.portal.fabric.status.FabricStatus;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessConfig;
import com.liferay.portal.kernel.process.ProcessConfig.Builder;
import com.liferay.portal.kernel.process.ProcessException;
import com.liferay.portal.kernel.util.StringUtil;

import io.netty.channel.Channel;

import java.io.File;
import java.io.Serializable;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricAgentSkeleton
	implements FabricAgent, FabricRemote<NettyFabricAgentStub> {

	public NettyFabricAgentSkeleton(
		FabricAgent fabricAgent, Path repositoryFolderPath) {

		_fabricAgent = fabricAgent;
		_repositoryFolder = repositoryFolderPath;
	}

	@Override
	public <T extends Serializable> FabricWorker<T> execute(
			ProcessConfig processConfig, ProcessCallable<T> processCallable)
		throws ProcessException {

		Builder builder = new Builder();

		builder.setArguments(processConfig.getArguments());
		builder.setBootstrapClassPath(
			convertClassPath(processConfig.getBootstrapClassPath()));
		builder.setJavaExecutable(processConfig.getJavaExecutable());
		builder.setRuntimeClassPath(
			convertClassPath(processConfig.getRuntimeClassPath()));

		return _fabricAgent.execute(builder.build(), processCallable);
	}

	@Override
	public FabricStatus getFabricStatus() {
		return _fabricAgent.getFabricStatus();
	}

	@Override
	public Collection<? extends FabricWorker<?>> getFabricWorkers() {
		return _fabricAgent.getFabricWorkers();
	}

	public void initialize(
		Channel channel, AsyncBroker<String, FileResponse> asyncBroker) {

		_channel = channel;
		_repository = new Repository(_repositoryFolder, channel, asyncBroker);
	}

	@Override
	public NettyFabricAgentStub toStub() {
		return new NettyFabricAgentStub();
	}

	protected String convertClassPath(String classPath) {
		List<String> localPaths = new ArrayList<String>();

		for (String path :
				StringUtil.split(classPath, File.pathSeparatorChar)) {

			Path filePath = _repository.getFile(path);

			if (filePath == null) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to map remote path : " + path +
							", removed it from class path");
				}

				continue;
			}

			filePath = filePath.toAbsolutePath();

			localPaths.add(filePath.toString());

			if (_log.isDebugEnabled()) {
				_log.debug(
					"Class path file mapped, remote path : " + path +
						", local path : " + filePath);
			}
		}

		String localClassPath = StringUtil.merge(
			localPaths, File.pathSeparator);

		if (_log.isInfoEnabled()) {
			_log.info(
				"Class path mapped, remote path : " + classPath +
					", local path : " + localClassPath);
		}

		return localClassPath;
	}

	private static Log _log = LogFactoryUtil.getLog(
		NettyFabricAgentSkeleton.class);

	// TODO maybe not need the volatile ?

	private volatile Channel _channel;

	private final FabricAgent _fabricAgent;
	private volatile Repository _repository;
	private final Path _repositoryFolder;

}