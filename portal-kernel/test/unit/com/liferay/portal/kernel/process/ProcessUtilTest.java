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

import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.test.CaptureHandler;
import com.liferay.portal.kernel.test.JDKLoggerTestUtil;
import com.liferay.portal.kernel.test.rule.CodeCoverageAssertor;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.ObjectValuePair;

import java.io.IOException;
import java.io.InputStream;


import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Shuyang Zhou
 */
public class ProcessUtilTest {

	@ClassRule
	public static final CodeCoverageAssertor codeCoverageAssertor =
		CodeCoverageAssertor.INSTANCE;

	@Test
	public void testDestroy() {

		// Useless test, just to keep whip happy with the coverage.

		ProcessUtil processUtil = new ProcessUtil();

		processUtil.destroy();
	}

	@Test
	public void testEcho() throws Exception {

		// Logging

		try (CaptureHandler captureHandler =
				JDKLoggerTestUtil.configureJDKLogger(
					LoggingOutputProcessor.class.getName(), Level.INFO)) {

			Future<ObjectValuePair<Void, Void>> loggingFuture =
				ProcessUtil.execute(
					ProcessUtil.LOGGING_OUTPUT_PROCESSOR,
					_buildArguments(Echo.class, "2"));

			loggingFuture.get();

			List<String> messageRecords = new ArrayList<>();

			for (LogRecord logRecord : captureHandler.getLogRecords()) {
				messageRecords.add(logRecord.getMessage());
			}

			Assert.assertTrue(
				messageRecords.contains(Echo.buildMessage(false, 0)));
			Assert.assertTrue(
				messageRecords.contains(Echo.buildMessage(false, 1)));
			Assert.assertTrue(
				messageRecords.contains(Echo.buildMessage(true, 0)));
			Assert.assertTrue(
				messageRecords.contains(Echo.buildMessage(true, 1)));
		}

		// Collector

		Future<ObjectValuePair<byte[], byte[]>> collectorFuture =
			ProcessUtil.execute(
				ProcessUtil.COLLECTOR_OUTPUT_PROCESSOR,
				_buildArguments(Echo.class, "2"));

		ObjectValuePair<byte[], byte[]> objectValuePair = collectorFuture.get();

		Assert.assertEquals(
			Echo.buildMessage(true, 0) + "\n" + Echo.buildMessage(true, 1) +
				"\n",
			new String(objectValuePair.getKey()));
		Assert.assertEquals(
			Echo.buildMessage(false, 0) + "\n" + Echo.buildMessage(false, 1) +
				"\n",
			new String(objectValuePair.getValue()));
	}

	@Test
	public void testErrorExit() throws Exception {
		Future<?> future = ProcessUtil.execute(
			ProcessUtil.CONSUMER_OUTPUT_PROCESSOR,
			_buildArguments(ErrorExit.class));

		try {
			future.get();

			Assert.fail();
		}
		catch (ExecutionException ee) {
			Throwable throwable = ee.getCause();

			Assert.assertSame(
				TerminationProcessException.class, throwable.getClass());
			Assert.assertEquals(
				"Subprocess terminated with exit code " + ErrorExit.EXIT_CODE,
				throwable.getMessage());

			TerminationProcessException terminationProcessException =
				(TerminationProcessException)throwable;

			Assert.assertEquals(
				ErrorExit.EXIT_CODE, terminationProcessException.getExitCode());
		}

		Assert.assertTrue(future.isDone());
	}

	@Test
	public void testErrorOutputProcessor() throws Exception {
		String[] arguments = _buildArguments(Echo.class, "1");

		Future<?> future = ProcessUtil.execute(
			new ErrorStderrOutputProcessor(), arguments);

		try {
			future.get();

			Assert.fail();
		}
		catch (ExecutionException ee) {
			Throwable throwable = ee.getCause();

			Assert.assertEquals(ProcessException.class, throwable.getClass());
			Assert.assertEquals(
				ErrorStderrOutputProcessor.class.getName(),
				throwable.getMessage());
		}

		Assert.assertTrue(future.isDone());

		future = ProcessUtil.execute(
			new ErrorStdoutOutputProcessor(), arguments);

		try {
			future.get();

			Assert.fail();
		}
		catch (ExecutionException ee) {
			Throwable throwable = ee.getCause();

			Assert.assertEquals(ProcessException.class, throwable.getClass());
			Assert.assertEquals(
				ErrorStdoutOutputProcessor.class.getName(),
				throwable.getMessage());
		}

		Assert.assertTrue(future.isDone());
	}

	@Test
	public void testFuture() throws Exception {

		// Time out on standard error processing

		String[] arguments = _buildArguments(Pause.class);

		Future<?> future = ProcessUtil.execute(
			ProcessUtil.CONSUMER_OUTPUT_PROCESSOR, arguments);

		Assert.assertFalse(future.isCancelled());
		Assert.assertFalse(future.isDone());

		try {
			future.get(1, TimeUnit.SECONDS);

			Assert.fail();
		}
		catch (TimeoutException te) {
		}

		List<NoticeableFuture<?>> noticeableFutures =
			ProcessUtil.getNoticeableFutures();

		Assert.assertTrue(ListUtil.isUnmodifiableList(noticeableFutures));
		Assert.assertEquals(1, noticeableFutures.size());
		Assert.assertSame(future, noticeableFutures.get(0));
		
		future.cancel(true);

		Assert.assertTrue(noticeableFutures.isEmpty());

		// Cancel twice to satisfy code coverage

		future.cancel(true);

		// Time out on standard out processing

		future = ProcessUtil.execute(
			new ConsumerOutputProcessor() {

				@Override
				public Void processStdErr(InputStream stdOutInputStream) {
					return null;
				}

			},
			arguments);

		Assert.assertFalse(future.isCancelled());
		Assert.assertFalse(future.isDone());

		try {
			future.get(1, TimeUnit.SECONDS);

			Assert.fail();
		}
		catch (TimeoutException te) {
		}

		future.cancel(true);

		// Success time out get

		future = ProcessUtil.execute(
			ProcessUtil.CONSUMER_OUTPUT_PROCESSOR,
			_buildArguments(Echo.class, "0"));

		future.get(1, TimeUnit.MINUTES);
	}

	@Test
	public void testWrongArguments() throws ProcessException {
		try {
			ProcessUtil.execute(null, (List<String>)null);

			Assert.fail();
		}
		catch (NullPointerException npe) {
			Assert.assertEquals("Output processor is null", npe.getMessage());
		}

		try {
			ProcessUtil.execute(
				ProcessUtil.CONSUMER_OUTPUT_PROCESSOR, (List<String>)null);

			Assert.fail();
		}
		catch (NullPointerException npe) {
			Assert.assertEquals("Arguments is null", npe.getMessage());
		}

		try {
			ProcessUtil.execute(
				ProcessUtil.CONSUMER_OUTPUT_PROCESSOR, Collections.emptyList());

			Assert.fail();
		}
		catch (IllegalArgumentException iae) {
			Assert.assertEquals("Arguments is empty", iae.getMessage());
		}

		try {
			ProcessUtil.execute(
				ProcessUtil.CONSUMER_OUTPUT_PROCESSOR, "commandNotExist");

			Assert.fail();
		}
		catch (ProcessException pe) {
			Throwable throwable = pe.getCause();

			Assert.assertEquals(IOException.class, throwable.getClass());
		}
	}

	private static String[] _buildArguments(
		Class<?> clazz, String... arguments) {

		List<String> argumentsList = new ArrayList<>();

		argumentsList.add("java");
		argumentsList.add("-cp");
		argumentsList.add(_CLASS_PATH);
		argumentsList.add(clazz.getName());
		argumentsList.addAll(Arrays.asList(arguments));

		return argumentsList.toArray(new String[argumentsList.size()]);
	}

	private static final String _CLASS_PATH;

	static {
		Class<?> clazz = Echo.class;

		ClassLoader classLoader = clazz.getClassLoader();

		String className = clazz.getName();

		String name = className.replace('.', '/') + ".class";

		URL url = classLoader.getResource(name);

		String path = url.getPath();

		int index = path.lastIndexOf(name);

		_CLASS_PATH = path.substring(0, index);
	}

	private static class Echo {

		public static String buildMessage(boolean stdOut, int number) {
			if (stdOut) {
				return "{stdOut}" + Echo.class.getName() + number;
			}

			return "{stdErr}" + Echo.class.getName() + number;
		}

		@SuppressWarnings("unused")
		public static void main(String[] arguments) {
			int times = Integer.parseInt(arguments[0]);

			for (int i = 0; i < times; i++) {
				System.err.println(buildMessage(false, i));
				System.out.println(buildMessage(true, i));
			}
		}

	}

	private static class ErrorExit {

		public static final int EXIT_CODE = 10;

		@SuppressWarnings("unused")
		public static void main(String[] arguments) {
			System.exit(EXIT_CODE);
		}

	}

	private static class ErrorStderrOutputProcessor
		implements OutputProcessor<Void, Void> {

		@Override
		public Void processStdErr(InputStream stdErrInputStream)
			throws ProcessException {

			throw new ProcessException(
				ErrorStderrOutputProcessor.class.getName());
		}

		@Override
		public Void processStdOut(InputStream stdOutInputStream) {
			return null;
		}

	}

	private static class ErrorStdoutOutputProcessor
		implements OutputProcessor<Void, Void> {

		@Override
		public Void processStdErr(InputStream stdErrInputStream) {
			return null;
		}

		@Override
		public Void processStdOut(InputStream stdOutInputStream)
			throws ProcessException {

			throw new ProcessException(
				ErrorStdoutOutputProcessor.class.getName());
		}

	}

	private static class Pause {

		@SuppressWarnings("unused")
		public static void main(String[] arguments) throws Exception {
			Thread.sleep(Long.MAX_VALUE);
		}

	}

}