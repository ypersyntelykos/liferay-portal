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

package com.liferay.portal.fabric.local.worker;

import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;

import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.process.ProcessChannel;
import com.liferay.portal.kernel.process.ProcessException;
import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class LocalFabricWorker<T extends Serializable>
	implements FabricWorker<T> {

	public LocalFabricWorker(ProcessChannel<T> processChannel) {
		_processChannel = processChannel;
	}

	@Override
	public NoticeableFuture<T> getProcessNoticeableFuture() {
		return _processChannel.getProcessNoticeableFuture();
	}

	@Override
	public <V extends Serializable> NoticeableFuture<V> write(
			ProcessCallable<V> processCallable)
		throws ProcessException {

		return _processChannel.write(processCallable);
	}

	private ProcessChannel<T> _processChannel;

}