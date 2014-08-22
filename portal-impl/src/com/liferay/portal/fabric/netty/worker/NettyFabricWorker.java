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

package com.liferay.portal.fabric.netty.worker;

import com.liferay.portal.fabric.local.worker.LocalFabricWorker;
import com.liferay.portal.fabric.worker.FabricWorker;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;

import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class NettyFabricWorker<T extends Serializable>
	implements FabricWorker<T> {

	public NettyFabricWorker(LocalFabricWorker<T> localFabricWorker) {
		_localFabricWorker = localFabricWorker;
	}

	@Override
	public NoticeableFuture<T> getFuture() {
		return _localFabricWorker.getFuture();
	}

	private final LocalFabricWorker<T> _localFabricWorker;

}