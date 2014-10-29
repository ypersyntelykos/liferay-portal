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

import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.BaseFutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.configuration.Filter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ClassPathUtil;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessException;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.OSDetector;
import com.liferay.portal.kernel.util.ProgressStatusConstants;
import com.liferay.portal.kernel.util.ProgressTracker;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.xuggler.Xuggler;
import com.liferay.portal.util.FileImpl;
import com.liferay.portal.util.HttpImpl;
import com.liferay.portal.util.PropsUtil;
import com.liferay.portal.xuggler.XugglerImpl;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Shuyang Zhou
 */
public class XugglerInstallationFabricAgentListener
	implements FabricAgentListener {

	@Override
	public void registered(FabricAgent fabricAgent) {
		try {
			long startTime = System.currentTimeMillis();

			FabricWorker<Serializable> fabricWorker = fabricAgent.execute(
				ClassPathUtil.getPortalProcessConfig(),
				_xugglerInstallProcessCallable);

			NoticeableFuture<Serializable> noticeableFuture =
				fabricWorker.getProcessNoticeableFuture();

			noticeableFuture.addFutureListener(
				new FinishFutureListener(startTime));
		}
		catch (ProcessException pe) {
			_log.error(
				"Unable to start xuggler installation fabric worker", pe);
		}
	}

	@Override
	public void unregistered(FabricAgent fabricAgent) {
	}

	protected class FinishFutureListener
		extends BaseFutureListener<Serializable> {

		public FinishFutureListener(long startTime) {
			_startTime = startTime;
		}

		@Override
		public void completeWithCancel(Future<Serializable> future) {
			_log.error("Xuggler installation got cancelled");
		}

		@Override
		public void completeWithException(
			Future<Serializable> future, Throwable throwable) {

			_log.error("Xuggler installation failed", throwable);
		}

		@Override
		public void completeWithResult(
			Future<Serializable> future, Serializable result) {

			if (_log.isInfoEnabled()) {
				_log.info(
					"Xuggler installation finished successfully in " +
						(System.currentTimeMillis() - _startTime) + "ms");
			}
		}

		private final long _startTime;

	}

	private static final Log _log = LogFactoryUtil.getLog(
		XugglerInstallationFabricAgentListener.class);

	private static final ProcessCallable<Serializable>
		_xugglerInstallProcessCallable = new ProcessCallable<Serializable>() {

			@Override
			public Serializable call() throws ProcessException {
				Xuggler xuggler = new XugglerImpl();

				if (xuggler.isNativeLibraryInstalled()) {
					if (_log.isInfoEnabled()) {
						_log.info("Xuggler is already installed");
					}

					return null;
				}

				String xugglerJarOption = null;

				String bitmode = OSDetector.getBitmode();

				if (Validator.isNotNull(bitmode) &&
					(bitmode.equals("32") || bitmode.equals("64"))) {

					if (OSDetector.isApple()) {
						xugglerJarOption = bitmode + "-mac";
					}
					else if (OSDetector.isLinux()) {
						xugglerJarOption = bitmode + "-linux";
					}
					else if (OSDetector.isWindows()) {
						xugglerJarOption = bitmode + "-win";
					}
				}

				String xugglerJarFile = null;

				if (xugglerJarOption != null) {
					xugglerJarFile = _xugglerJarMap.get(xugglerJarOption);
				}

				if (xugglerJarFile == null) {
					_log.error(
						"Xuggler auto installation is not supported on " +
							"current system :" + System.getProperty("os.name") +
								"/" + System.getProperty("os.arch"));

					return null;
				}

				FileUtil fileUtil = new FileUtil();

				fileUtil.setFile(new FileImpl());

				HttpUtil httpUtil = new HttpUtil();

				httpUtil.setHttp(new HttpImpl());

				try {
					ProgressTracker progressTracker = new ProgressTracker(
						"XugglerInstaller");

					progressTracker.addProgress(
						ProgressStatusConstants.DOWNLOADING, 15,
						"downloading-xuggler");
					progressTracker.addProgress(
						ProgressStatusConstants.COPYING, 70,
						"copying-xuggler-files");

					xuggler.installNativeLibraries(
						xugglerJarFile, progressTracker);
				}
				catch (Exception e) {
					throw new ProcessException(e);
				}

				if (_log.isInfoEnabled()) {
					_log.info("Xuggler installed successfully");
				}

				return null;
			}

			private static final long serialVersionUID = 1L;

			private final Map<String, String> _xugglerJarMap =
				new HashMap<String, String>();

			{
				for (String xugglerJarOption :
						PropsUtil.getArray(PropsKeys.XUGGLER_JAR_OPTIONS)) {

					_xugglerJarMap.put(
						xugglerJarOption,
						PropsUtil.get(
							PropsKeys.XUGGLER_JAR_FILE,
							new Filter(xugglerJarOption)));
				}
			}

		};

}