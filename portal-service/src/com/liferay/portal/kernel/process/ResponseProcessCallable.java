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

import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class ResponseProcessCallable<T extends Serializable>
	implements ProcessCallable<T> {

	public ResponseProcessCallable(long id, T result) {
		_id = id;
		_result = result;
	}

	@Override
	public T call() throws ProcessException {

		// TODO figure out how to set the result back to LocalProcessExecutor's
		// matching ProcessChannel's NoticeableFuture

		// Maybe try to creata a package level ThreadLocal, populated inside
		// SubprocessReactor?

		throw new UnsupportedOperationException("Not supported yet.");
	}

	private static final long serialVersionUID = 1L;

	private final long _id;
	private final T _result;
	private final long _syntheticId =
		ProcessLauncher.ProcessContext.getSyntheticId();

}