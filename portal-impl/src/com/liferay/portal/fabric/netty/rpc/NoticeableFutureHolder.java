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

import com.liferay.portal.kernel.concurrent.NoticeableFuture;

import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Shuyang Zhou
 */
public class NoticeableFutureHolder<T extends Serializable>
	implements Serializable {

	public NoticeableFutureHolder(NoticeableFuture<T> noticeableFuture) {
		_noticeableFuture = noticeableFuture;
	}

	public NoticeableFuture<T> getNoticeableFuture() {
		return _noticeableFuture;
	}

	private void writeObject(ObjectOutputStream objectOutputStream) {
		throw new UnsupportedOperationException(
			NoticeableFutureHolder.class.getName() +
				" is a transient place holder, should never be serilaized");
	}

	private final transient NoticeableFuture<T> _noticeableFuture;

}