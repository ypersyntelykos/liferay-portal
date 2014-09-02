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

package com.liferay.portal.fabric.status;

import com.liferay.portal.kernel.concurrent.NoticeableFuture;
import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessChannel;
import com.liferay.portal.kernel.process.ProcessException;
import com.liferay.portal.kernel.util.MethodHandler;
import com.liferay.portal.kernel.util.ReflectionUtil;

import java.io.Serializable;

import java.lang.management.ManagementFactory;

import java.util.concurrent.Future;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * @author Shuyang Zhou
 */
public class FabricStatusOperationUtil {

	public static <T extends Serializable> NoticeableFuture<T> invoke(
		ProcessChannel<?> processChannel, ObjectName objectName,
		MethodHandler methodHandler) {

		try {
			return processChannel.write(
				new InvokeProcessCallable<T>(objectName, methodHandler));
		}
		catch (ProcessException pe) {
			ReflectionUtil.throwException(pe);
		}

		throw ReflectionUtil.SHOULD_NEVER_HAPPEN_ERROR;
	}

	public static <T extends Serializable> T syncInvoke(
		ProcessChannel<?> processChannel, ObjectName objectName,
		MethodHandler methodHandler) {

		try {
			Future<T> future = processChannel.write(
				new InvokeProcessCallable<T>(objectName, methodHandler));

			return future.get();
		}
		catch (Exception e) {
			ReflectionUtil.throwException(e);
		}

		throw ReflectionUtil.SHOULD_NEVER_HAPPEN_ERROR;
	}

	private static class InvokeProcessCallable<T extends Serializable>
		implements ProcessCallable<T> {

		public InvokeProcessCallable(
			ObjectName objectName, MethodHandler methodHandler) {

			_objectName = objectName;
			_methodHandler = methodHandler;
		}

		@Override
		public T call() throws ProcessException {
			MBeanServer mBeanServer =
				ManagementFactory.getPlatformMBeanServer();

			try {
				ObjectInstance objectInstance = mBeanServer.getObjectInstance(
					_objectName);

				return (T)_methodHandler.invoke(objectInstance);
			}
			catch (Exception e) {
				throw new ProcessException(e);
			}
		}

		private static final long serialVersionUID = 1L;

		private final MethodHandler _methodHandler;
		private final ObjectName _objectName;

	}

}