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

import com.liferay.portal.fabric.FabricResourceMappingVisitor;
import com.liferay.portal.fabric.InputResource;
import com.liferay.portal.fabric.agent.FabricAgent;
import com.liferay.portal.fabric.local.agent.LocalFabricAgent;
import com.liferay.portal.fabric.netty.agent.NettyFabricAgentStub;
import com.liferay.portal.fabric.netty.fileserver.FileHelperUtil;
import com.liferay.portal.fabric.netty.rpc.ChannelThreadLocal;
import com.liferay.portal.fabric.netty.rpc.RPCUtil;
import com.liferay.portal.fabric.netty.util.NettyUtil;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerConfig;
import com.liferay.portal.fabric.netty.worker.NettyFabricWorkerStub;
import com.liferay.portal.fabric.repository.Repository;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.concurrent.NoticeableFutureConverter;
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
import io.netty.util.concurrent.EventExecutor;

import java.io.File;
import java.io.Serializable;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricWorkerExecutionChannelHandler
	extends SimpleChannelInboundHandler<NettyFabricWorkerConfig<Serializable>> {

	// TODO move to properties

	public static final long FABRIC_WORKER_EXECUTION_TIME_OUT = 10000;

	public NettyFabricWorkerExecutionChannelHandler(
		Repository repository, ProcessExecutor processExecutor) {

		_repository = repository;
		_fabricAgent = new LocalFabricAgent(processExecutor);
	}

	protected static void sendResult(
		Channel channel, long fabricWorkerId, Serializable result,
		Throwable t) {

		if (t instanceof ExecutionException) {
			t = t.getCause();
		}

		final ResultProcessCallable resultProcessCallable =
			new ResultProcessCallable(fabricWorkerId, result, t);

		NoticeableFuture<Serializable> noticeableFuture = RPCUtil.execute(
			channel, resultProcessCallable);

		NettyUtil.scheduleCancellation(
			channel, noticeableFuture, FABRIC_WORKER_EXECUTION_TIME_OUT);

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
							"Unable to send back fabric worker result " +
								resultProcessCallable,
							t);
					}
				}

			});
	}

	@Override
	protected void channelRead0(
			final ChannelHandlerContext channelHandlerContext,
			final NettyFabricWorkerConfig<Serializable> nettyFabricWorkerConfig)
		throws Exception {

		NoticeableFuture<LoadedResources> noticeableFuture = loadResources(
			nettyFabricWorkerConfig);

		noticeableFuture.addFutureListener(
			new SendResultFutureListener<LoadedResources>(
				channelHandlerContext.channel(),
				nettyFabricWorkerConfig.getId()) {

				@Override
				protected Serializable doComplete(
					LoadedResources loadedResources) {

					EventExecutor eventExecutor =
						channelHandlerContext.executor();

					eventExecutor.submit(
						new ExecutionRunnable(
							channelHandlerContext.channel(),
							nettyFabricWorkerConfig, loadedResources));

					return _NO_RESULT;
				}

			});
	}

	protected NoticeableFuture<LoadedResources> loadResources(
		NettyFabricWorkerConfig<Serializable> nettyFabricWorkerConfig) {

		Map<Path, Path> mergedResources = new HashMap<Path, Path>();

		ProcessConfig processConfig =
			nettyFabricWorkerConfig.getProcessConfig();

		final Map<Path, Path> bootstrapResources =
			new LinkedHashMap<Path, Path>();

		for (String pathString :
				StringUtil.split(
					processConfig.getBootstrapClassPath(),
					File.pathSeparatorChar)) {

			bootstrapResources.put(Paths.get(pathString), null);
		}

		mergedResources.putAll(bootstrapResources);

		final Map<Path, Path> runtimeResources =
			new LinkedHashMap<Path, Path>();

		for (String pathString :
				StringUtil.split(
					processConfig.getRuntimeClassPath(),
					File.pathSeparatorChar)) {

			runtimeResources.put(Paths.get(pathString), null);
		}

		mergedResources.putAll(runtimeResources);

		FabricResourceMappingVisitor fabricResourceMappingVisitor =
			new FabricResourceMappingVisitor(
				InputResource.class, _repository.getRepositoryPath());

		ObjectGraphUtil.walkObjectGraph(
			nettyFabricWorkerConfig.getProcessCallable(),
			fabricResourceMappingVisitor);

		final Map<Path, Path> inputResources =
			fabricResourceMappingVisitor.getResourceMap();

		mergedResources.putAll(inputResources);

		return new NoticeableFutureConverter<LoadedResources, Map<Path, Path>>(
			_repository.getFiles(mergedResources, false)) {

				@Override
				protected LoadedResources convert(
					Map<Path, Path> mergedResources) {

					Map<Path, Path> loadedInputResources =
						new HashMap<Path, Path>();

					for (Path path : inputResources.keySet()) {
						loadedInputResources.put(
							path, mergedResources.get(path));
					}

					List<Path> loadedBootstrapResources = new ArrayList<Path>();

					for (Path path : bootstrapResources.keySet()) {
						loadedBootstrapResources.add(mergedResources.get(path));
					}

					List<Path> loadedRuntimeResources = new ArrayList<Path>();

					for (Path path : runtimeResources.keySet()) {
						loadedRuntimeResources.add(mergedResources.get(path));
					}

					return new LoadedResources(
						inputResources,
						StringUtil.merge(
							loadedBootstrapResources, File.pathSeparator),
						StringUtil.merge(
							loadedRuntimeResources, File.pathSeparator));
				}

			};
	}

	protected static class LoadedResources {

		public LoadedResources(
			Map<Path, Path> inputResources, String bootstrapClassPath,
			String runtimeClassPath) {

			_inputResources = inputResources;
			_bootstrapClassPath = bootstrapClassPath;
			_runtimeClassPath = runtimeClassPath;
		}

		public Map<Path, Path> getInputResources() {
			return _inputResources;
		}

		public ProcessConfig toProcessConfig(ProcessConfig processConfig) {
			Builder builder = new Builder();

			builder.setArguments(processConfig.getArguments());
			builder.setBootstrapClassPath(_bootstrapClassPath);
			builder.setJavaExecutable(processConfig.getJavaExecutable());
			builder.setRuntimeClassPath(_runtimeClassPath);

			return builder.build();
		}

		private final String _bootstrapClassPath;
		private final Map<Path, Path> _inputResources;
		private final String _runtimeClassPath;
	}

	protected static class ResultProcessCallable
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

			sb.append("{id=");
			sb.append(_id);
			sb.append(", result=");
			sb.append(_result);
			sb.append(", throwable=");
			sb.append(_throwable);
			sb.append("}");

			return sb.toString();
		}

		protected ResultProcessCallable(
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

	protected static abstract class SendResultFutureListener<T>
		implements FutureListener<T> {

		public SendResultFutureListener(Channel channel, long fabricWorkerId) {
			_channel = channel;
			_fabricWorkerId = fabricWorkerId;
		}

		@Override
		public void complete(Future<T> future) {
			try {
				Serializable result = doComplete(future.get());

				if (result == _NO_RESULT) {
					return;
				}

				sendResult(_channel, _fabricWorkerId, result, null);
			}
			catch (Throwable t) {
				sendResult(_channel, _fabricWorkerId, null, t);
			}
		}

		protected abstract Serializable doComplete(T result) throws Throwable;

		private final Channel _channel;
		private final long _fabricWorkerId;

	}

	protected class ExecutionRunnable implements Runnable {

		public ExecutionRunnable(
			Channel channel,
			NettyFabricWorkerConfig<Serializable> nettyFabricWorkerConfig,
			LoadedResources loadedResources) {

			_channel = channel;
			_nettyFabricWorkerConfig = nettyFabricWorkerConfig;
			_loadedResources = loadedResources;
		}

		@Override
		public void run() {
			try {
				doRun(_loadedResources);
			}
			catch (Throwable t) {
				sendResult(_channel, _nettyFabricWorkerConfig.getId(), null, t);
			}
		}

		protected void doRun(final LoadedResources loadedResources)
			throws Throwable {

			FabricWorker<Serializable> fabricWorker = _fabricAgent.execute(
				loadedResources.toProcessConfig(
					_nettyFabricWorkerConfig.getProcessConfig()),
				_nettyFabricWorkerConfig.getProcessCallable());

			NoticeableFuture<Serializable> processNoticeableFuture =
				fabricWorker.getProcessNoticeableFuture();

			processNoticeableFuture.addFutureListener(
				new SendResultFutureListener<Serializable>(
					_channel, _nettyFabricWorkerConfig.getId()) {

					@Override
					protected Serializable doComplete(Serializable result) {
						Map<Path, Path> inputResources =
							loadedResources.getInputResources();

						for (Path path : inputResources.values()) {
							FileHelperUtil.delete(true, path);
						}

						return result;
					}

				});
		}

		private final Channel _channel;
		private final LoadedResources _loadedResources;
		private NettyFabricWorkerConfig<Serializable> _nettyFabricWorkerConfig;

	}

	private static final Serializable _NO_RESULT = new Serializable() {

		private static final long serialVersionUID = 1L;

	};

	private static Log _log = LogFactoryUtil.getLog(
		NettyFabricWorkerExecutionChannelHandler.class);

	private final FabricAgent _fabricAgent;
	private final Repository _repository;

}