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

import com.liferay.portal.kernel.process.log.ProcessOutputStream;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class RequestProcessCallable<T extends Serializable>
	implements ProcessCallable<T> {

	public RequestProcessCallable(long id, ProcessCallable<T> processCallable) {
		_id = id;
		_processCallable = processCallable;
	}

	@Override
	public T call() throws ProcessException {
		T t = _processCallable.call();

		ProcessOutputStream processOutputStream =
			ProcessLauncher.ProcessContext.getProcessOutputStream();

		try {
			processOutputStream.writeProcessCallable(
				new ResponseProcessCallable<T>(_id, t));
		}
		catch (IOException ioe) {
			throw new ProcessException(ioe);
		}

		return t;
	}

	private static final long serialVersionUID = 1L;

	private final long _id;
	private final ProcessCallable<T> _processCallable;

}