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

package com.liferay.portal.kernel.process;

import com.liferay.portal.kernel.concurrent.BaseFutureListener;
import com.liferay.portal.kernel.concurrent.DefaultNoticeableFuture;
import com.liferay.portal.kernel.concurrent.FutureListener;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.util.ObjectValuePair;

import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import java.io.IOException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * @author Shuyang Zhou
 */
public class ProcessUtil {

	public static final CollectorOutputProcessor COLLECTOR_OUTPUT_PROCESSOR =
		new CollectorOutputProcessor();

	public static final ConsumerOutputProcessor CONSUMER_OUTPUT_PROCESSOR =
		new ConsumerOutputProcessor();

	public static final EchoOutputProcessor ECHO_OUTPUT_PROCESSOR =
		new EchoOutputProcessor();

	public static final LoggingOutputProcessor LOGGING_OUTPUT_PROCESSOR =
		new LoggingOutputProcessor();

	public static <O, E> NoticeableFuture<ObjectValuePair<O, E>> execute(
			OutputProcessor<O, E> outputProcessor, List<String> arguments)
		throws ProcessException {

		if (outputProcessor == null) {
			throw new NullPointerException("Output processor is null");
		}

		if (arguments == null) {
			throw new NullPointerException("Arguments is null");
		}

		if (arguments.isEmpty()) {
			throw new IllegalArgumentException("Arguments is empty");
		}

		StringBundler sb = new StringBundler(arguments.size() * 2 - 1);

		for (String argument : arguments) {
			sb.append(argument);
			sb.append(StringPool.SPACE);
		}

		sb.setIndex(sb.index() - 1);

		String commandLine = sb.toString();

		ProcessBuilder processBuilder = new ProcessBuilder(arguments);

		try {
			Process process = processBuilder.start();

			DefaultNoticeableFuture<O> stdOutNoticeableFuture =
				new DefaultNoticeableFuture<>(
					new ProcessStdOutCallable<O>(outputProcessor, process));

			_runTask(
				stdOutNoticeableFuture,
				"Piper for [".concat(commandLine).concat("] stdout"));

			DefaultNoticeableFuture<E> stdErrNoticeableFuture =
				new DefaultNoticeableFuture<>(
					new ProcessStdErrCallable<E>(outputProcessor, process));

			_runTask(
				stdErrNoticeableFuture,
				"Piper for [".concat(commandLine).concat("] stderr"));

			NoticeableFuture<ObjectValuePair<O, E>> noticeableFuture =
				_wrapNoticeableFuture(
					stdOutNoticeableFuture, stdErrNoticeableFuture, process);

			noticeableFuture.addFutureListener(
				new FutureListener<ObjectValuePair<O, E>>() {

					@Override
					public void complete(
						Future<ObjectValuePair<O, E>> future) {

						_noticeableFutures.remove(noticeableFuture);
					}

				});

			_noticeableFutures.add(noticeableFuture);

			return noticeableFuture;
		}
		catch (IOException ioe) {
			throw new ProcessException(ioe);
		}
	}

	private static void _runTask(
		DefaultNoticeableFuture<?> stdOutNoticeableFuture, String threadName) {

		Thread thread = new Thread(stdOutNoticeableFuture, threadName);

		thread.setDaemon(true);

		thread.start();
	}

	public static <O, E> NoticeableFuture<ObjectValuePair<O, E>> execute(
			OutputProcessor<O, E> outputProcessor, String... arguments)
		throws ProcessException {

		return execute(outputProcessor, Arrays.asList(arguments));
	}

	/**
	 * @deprecated As of 7.0.0, replaced by {@link
	 *             #getUnfinishedNoticeableFutures()}
	 */
	@Deprecated
	public void destroy() {
	}

	public static List<NoticeableFuture<?>>	getNoticeableFutures() {
		return Collections.unmodifiableList(_noticeableFutures);
	}

	private static <O, E> NoticeableFuture<ObjectValuePair<O, E>>
		_wrapNoticeableFuture(
			final NoticeableFuture<O> stdOutNoticeableFuture,
			final NoticeableFuture<E> stdErrNoticeableFuture,
			final Process process) {

		final DefaultNoticeableFuture<ObjectValuePair<O, E>>
			defaultNoticeableFuture = new DefaultNoticeableFuture<>();

		defaultNoticeableFuture.addFutureListener(
			new FutureListener<ObjectValuePair<O, E>>() {

				@Override
				public void complete(Future<ObjectValuePair<O, E>> future) {
					if (!future.isCancelled()) {
						return;
					}

					stdOutNoticeableFuture.cancel(true);

					stdErrNoticeableFuture.cancel(true);

					process.destroy();
				}

			});

		final AtomicMarkableReference<O> stdOutReference =
			new AtomicMarkableReference<>(null, false);

		final AtomicMarkableReference<E> stdErrReference =
			new AtomicMarkableReference<>(null, false);

		stdOutNoticeableFuture.addFutureListener(
			new BaseFutureListener<O>() {

				@Override
				public void completeWithCancel(Future<O> future) {
					defaultNoticeableFuture.cancel(true);
				}

				@Override
				public void completeWithException(
					Future<O> future, Throwable throwable) {

					defaultNoticeableFuture.setException(throwable);
				}

				@Override
				public void completeWithResult(Future<O> future, O stdOut) {
					stdOutReference.set(stdOut, true);

					boolean[] markHolder = new boolean[1];

					E stdErr = stdErrReference.get(markHolder);

					if (markHolder[0]) {
						defaultNoticeableFuture.set(
							new ObjectValuePair<O, E>(stdOut, stdErr));
					}
				}

			});

		stdErrNoticeableFuture.addFutureListener(
			new BaseFutureListener<E>() {

				@Override
				public void completeWithCancel(Future<E> future) {
					defaultNoticeableFuture.cancel(true);
				}

				@Override
				public void completeWithException(
					Future<E> future, Throwable throwable) {

					defaultNoticeableFuture.setException(throwable);
				}

				@Override
				public void completeWithResult(Future<E> future, E stdErr) {
					stdErrReference.set(stdErr, true);

					boolean[] markHolder = new boolean[1];

					O stdOut = stdOutReference.get(markHolder);

					if (markHolder[0]) {
						defaultNoticeableFuture.set(
							new ObjectValuePair<O, E>(stdOut, stdErr));
					}
				}

			});

		return defaultNoticeableFuture;
	}

	private static final List<NoticeableFuture<?>> _noticeableFutures =
		new CopyOnWriteArrayList<>();

	private static class ProcessStdErrCallable<T> implements Callable<T> {

		public ProcessStdErrCallable(
			OutputProcessor<?, T> outputProcessor, Process process) {

			_outputProcessor = outputProcessor;
			_process = process;
		}

		@Override
		public T call() throws Exception {
			return _outputProcessor.processStdErr(_process.getErrorStream());
		}

		private final OutputProcessor<?, T> _outputProcessor;
		private final Process _process;

	}

	private static class ProcessStdOutCallable<T> implements Callable<T> {

		public ProcessStdOutCallable(
			OutputProcessor<T, ?> outputProcessor, Process process) {

			_outputProcessor = outputProcessor;
			_process = process;
		}

		@Override
		public T call() throws Exception {
			try {
				return _outputProcessor.processStdOut(
					_process.getInputStream());
			}
			finally {
				try {
					int exitCode = _process.waitFor();

					if (exitCode != 0) {
						throw new TerminationProcessException(exitCode);
					}
				}
				catch (InterruptedException ie) {
					_process.destroy();

					throw new ProcessException(
						"Forcibly killed subprocess on interruption", ie);
				}
			}
		}

		private final OutputProcessor<T, ?> _outputProcessor;
		private final Process _process;

	}

}