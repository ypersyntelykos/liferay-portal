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

import com.liferay.portal.kernel.concurrent.AsyncBroker;
import com.liferay.portal.kernel.concurrent.NoticeableFuture;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Shuyang Zhou
 */
public class LocalProcessChannel<T extends Serializable>
	implements ProcessChannel<T> {

	public LocalProcessChannel(
		NoticeableFuture<T> noticeableFuture,
		ObjectOutputStream objectOutputStream,
		AsyncBroker<Long, Serializable> asyncBroker) {

		_noticeableFuture = noticeableFuture;
		_objectOutputStream = objectOutputStream;
		_asyncBroker = asyncBroker;
	}

	@Override
	public NoticeableFuture<T> getProcessNoticeableFuture() {
		return _noticeableFuture;
	}

	@Override
	public <V extends Serializable> NoticeableFuture<V> write(
			ProcessCallable<V> processCallable)
		throws ProcessException {

		NoticeableFuture<Serializable> noticeableFuture = _asyncBroker.post(
			_idGenerator.getAndIncrement());

		try {
			_objectOutputStream.writeObject(processCallable);
		}
		catch (IOException ioe) {
			throw new ProcessException(ioe);
		}

		return (NoticeableFuture<V>)noticeableFuture;
	}

	private final AsyncBroker<Long, Serializable> _asyncBroker;
	private final AtomicLong _idGenerator = new AtomicLong();
	private final NoticeableFuture<T> _noticeableFuture;
	private final ObjectOutputStream _objectOutputStream;

}