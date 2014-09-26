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

package com.liferay.portal.fabric.netty.handlers;

import com.liferay.portal.fabric.FabricInputResourceMappingVisitor;
import com.liferay.portal.fabric.agent.FabricAgent;
import com.liferay.portal.fabric.local.agent.LocalFabricAgent;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentStub;
import com.liferay.portal.fabric.netty.fileserver.FileHelperUtil;
import com.liferay.portal.fabric.netty.repository.Repository;
import com.liferay.portal.fabric.netty.rpc.ChannelThreadLocal;
import com.liferay.portal.fabric.netty.rpc.RPCUtil;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerConfig;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerStub;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessConfig;
import com.liferay.portal.kernel.process.ProcessConfig.Builder;
import com.liferay.portal.kernel.process.ProcessException;
import com.liferay.portal.kernel.process.ProcessExecutor;
import com.liferay.portal.kernel.util.ObjectGraphUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.Serializable;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricWorkerExecutionChannelHandler
	extends SimpleChannelInboundHandler
		<NettyFabricWorkerConfig<Serializable>> {

	public NettyFabricWorkerExecutionChannelHandler(
		Repository repository, ProcessExecutor processExecutor) {

		_repository = repository;
		_fabricAgent = new LocalFabricAgent(processExecutor);
	}

	@Override
	protected void channelRead0(
			final ChannelHandlerContext channelHandlerContext,
			final NettyFabricWorkerConfig<Serializable> nettyFabricWorkerConfig)
		throws Exception {

		ProcessCallable<Serializable> processCallable =
			nettyFabricWorkerConfig.getProcessCallable();

		FabricInputResourceMappingVisitor
			fabricInputResourceMappingVisitor =
				new FabricInputResourceMappingVisitor(_repository);

		final Map<File, File> inputResourceMap =
			fabricInputResourceMappingVisitor.getResourceMap();

		ObjectGraphUtil.walkObjectGraph(
			processCallable, fabricInputResourceMappingVisitor);

		FabricWorker<Serializable> fabricWorker = _fabricAgent.execute(
			convertProcessConfig(nettyFabricWorkerConfig.getProcessConfig()),
			processCallable);

		NoticeableFuture<Serializable> noticeableFuture =
			fabricWorker.getProcessNoticeableFuture();

		noticeableFuture.addFutureListener(
			new FutureListener<Serializable>() {

				@Override
				public void complete(Future<Serializable> future) {
					for (File file : inputResourceMap.values()) {
						FileHelperUtil.delete(true, file.toPath());
					}

					Serializable result = null;

					Throwable throwable = null;

					try {
						result = future.get();
					}
					catch (ExecutionException ee) {
						throwable = ee.getCause();
					}
					catch (InterruptedException ie) {
						throwable = ie;
					}

					final ResultProcessCallable resultProcessCallable =
						new ResultProcessCallable(
							nettyFabricWorkerConfig.getId(), result, throwable);

					NoticeableFuture<Serializable> noticeableFuture =
						RPCUtil.execute(
							channelHandlerContext.channel(),
							resultProcessCallable);

					noticeableFuture.addFutureListener(
						new FutureListener<Serializable>() {

							@Override
							public void complete(Future<Serializable> future) {
								try {
									future.get();
								}
								catch (Throwable t) {
									if (t instanceof ExecutionException) {
										t = t.getCause();
									}

									_log.error(
										"Unable to send back fabric worker " +
											"result " + resultProcessCallable,
										t);
								}
							}

						});
				}

			});
	}

	protected String convertClassPath(String classPath) {
		List<String> localPathStrings = new ArrayList<String>();

		for (String pathString :
				StringUtil.split(classPath, File.pathSeparatorChar)) {

			Path filePath = _repository.getFile(pathString, false);

			if (filePath == null) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Unable to map remote path : " + pathString +
							", removed it from class path");
				}

				continue;
			}

			filePath = filePath.toAbsolutePath();

			localPathStrings.add(filePath.toString());

			if (_log.isDebugEnabled()) {
				_log.debug(
					"Class path file mapped, remote path : " + pathString +
						", local path : " + filePath);
			}
		}

		String localClassPath = StringUtil.merge(
			localPathStrings, File.pathSeparator);

		if (_log.isInfoEnabled()) {
			_log.info(
				"Class path mapped, remote path : " + classPath +
					", local path : " + localClassPath);
		}

		return localClassPath;
	}

	protected ProcessConfig convertProcessConfig(ProcessConfig processConfig) {
		Builder builder = new Builder();

		builder.setArguments(processConfig.getArguments());
		builder.setBootstrapClassPath(
			convertClassPath(processConfig.getBootstrapClassPath()));
		builder.setJavaExecutable(processConfig.getJavaExecutable());
		builder.setRuntimeClassPath(
			convertClassPath(processConfig.getRuntimeClassPath()));

		return builder.build();
	}

	private static Log _log = LogFactoryUtil.getLog(
		NettyFabricWorkerExecutionChannelHandler.class);

	private final FabricAgent _fabricAgent;
	private final Repository _repository;

	private static class ResultProcessCallable
		implements ProcessCallable<Serializable> {

		@Override
		public Serializable call() throws ProcessException {
			Channel channel = ChannelThreadLocal.getChannel();

			NettyFabricAgentStub nettyStubFabricAgent =
				NettyChannelAttributes.getNettyFabricAgentStub(channel);

			if (nettyStubFabricAgent == null) {
				throw new ProcessException(
					"Unable to locate fabric agent on channel " + channel);
			}

			NettyFabricWorkerStub<Serializable> nettyStubFabricWorker =
				(NettyFabricWorkerStub<Serializable>)
					nettyStubFabricAgent.takeNettyStubFabricWorker(_id);

			if (nettyStubFabricWorker == null) {
				throw new ProcessException(
					"Unable to locate fabric worker on channel " + channel +
						", with fabric worker id " + _id);
			}

			if (_throwable != null) {
				nettyStubFabricWorker.setException(_throwable);
			}
			else {
				nettyStubFabricWorker.setResult(_result);
			}

			return null;
		}

		@Override
		public String toString() {
			StringBundler sb = new StringBundler(7);

			sb.append("Fabric Worker Result : {id = ");
			sb.append(_id);
			sb.append(", result = ");
			sb.append(_result);
			sb.append(", throwable = ");
			sb.append(_throwable);
			sb.append("}");

			return sb.toString();
		}

		private ResultProcessCallable(
			long id, Serializable result, Throwable throwable) {

			_id = id;
			_result = result;
			_throwable = throwable;
		}

		private static final long serialVersionUID = 1L;

		private final long _id;
		private final Serializable _result;
		private final Throwable _throwable;

	}

}