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

package com.liferay.portal.fabric.netty.rpc;

import com.liferay.portal.fabric.netty.rpc.handlers.NettyRPCChannelHandler;
import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessException;
import com.liferay.portal.kernel.test.CodeCoverageAssertor;
import com.liferay.portal.test.AdviseWith;
import com.liferay.portal.test.runners.AspectJMockingNewClassLoaderJUnitTestRunner;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.Attribute;

import java.io.Serializable;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.testng.Assert;

/**
 * @author Shuyang Zhou
 */
@RunWith(AspectJMockingNewClassLoaderJUnitTestRunner.class)
public class RPCUtilTest {

	@ClassRule
	public static CodeCoverageAssertor codeCoverageAssertor =
		new CodeCoverageAssertor() {

			@Override
			public void appendAssertClasses(List<Class<?>> assertClasses) {
				assertClasses.add(RPCRequest.class);
				assertClasses.add(RPCResponse.class);
				assertClasses.add(RPCSerializable.class);
				assertClasses.add(NettyRPCChannelHandler.class);
			}

		};

	@AdviseWith(adviceClasses = AttributeAdvice.class)
	@Test
	public void testConcurrentGetRPCAsyncBroker() {
		AsyncBroker<Long, Serializable> asyncBroker =
			new AsyncBroker<Long, Serializable>();

		AttributeAdvice.setConcurrentValue(asyncBroker);

		Assert.assertSame(
			asyncBroker, RPCUtil.getRPCAsyncBroker(_embeddedChannel));

		// Get from cache

		Assert.assertSame(
			asyncBroker, RPCUtil.getRPCAsyncBroker(_embeddedChannel));
	}

	@AdviseWith(adviceClasses = AttributeAdvice.class)
	@Test
	public void testConcurrentGetRPCIdGenerator() {
		AtomicLong idGenerator = new AtomicLong();

		AttributeAdvice.setConcurrentValue(idGenerator);

		Assert.assertSame(
			idGenerator, RPCUtil.getRPCIdGenerator(_embeddedChannel));

		// Get from cache

		Assert.assertSame(
			idGenerator, RPCUtil.getRPCIdGenerator(_embeddedChannel));
	}

	@Test
	public void testConstructor() {
		new RPCUtil();
	}

	@Test
	public void testRPCWithException() throws Exception {
		ProcessException testException = new ProcessException("message");

		Future<Serializable> future = RPCUtil.execute(
			_embeddedChannel, new ExceptionProcessCallable(testException));

		_embeddedChannel.writeInbound(_embeddedChannel.readOutbound());
		_embeddedChannel.writeInbound(_embeddedChannel.readOutbound());

		try {
			future.get();

			Assert.fail();
		}
		catch (ExecutionException ee) {
			Throwable throwable = ee.getCause();

			Assert.assertSame(testException, throwable);
		}
	}

	@Test
	public void testRPCWithResult() throws Exception {
		String result = "result";

		Future<String> future = RPCUtil.execute(
			_embeddedChannel, new ResultProcessCallable(result));

		_embeddedChannel.writeInbound(_embeddedChannel.readOutbound());
		_embeddedChannel.writeInbound(_embeddedChannel.readOutbound());

		Assert.assertEquals(result, future.get());
	}

	@Aspect
	public static class AttributeAdvice {

		public static void setConcurrentValue(Object concurrentValue) {
			_concurrentValue = concurrentValue;
		}

		@Around(
			"execution(public Object io.netty.util.Attribute.setIfAbsent(" +
				"Object))")
		public Object setIfAbsent(ProceedingJoinPoint proceedingJoinPoint)
			throws Throwable {

			Attribute<Object> attribute =
				(Attribute)proceedingJoinPoint.getThis();

			attribute.set(_concurrentValue);

			return proceedingJoinPoint.proceed();
		}

		private static Object _concurrentValue;

	}

	private final EmbeddedChannel _embeddedChannel = new EmbeddedChannel(
		NettyRPCChannelHandler.INSTANCE);

	private static class ExceptionProcessCallable
		implements ProcessCallable<Serializable> {

		public ExceptionProcessCallable(ProcessException processException) {
			_processException = processException;
		}

		@Override
		public Serializable call() throws ProcessException {
			throw _processException;
		}

		private static final long serialVersionUID = 1L;

		private final ProcessException _processException;

	}

	private static class ResultProcessCallable
		implements ProcessCallable<String> {

		public ResultProcessCallable(String result) {
			_result = result;
		}

		@Override
		public String call() {
			return _result;
		}

		private static final long serialVersionUID = 1L;

		private final String _result;

	}

}