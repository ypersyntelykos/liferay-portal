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

package com.liferay.portal.kernel.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Shuyang Zhou
 */
public class NoticeableFutureWrapper<T> implements NoticeableFuture<T> {

	public NoticeableFutureWrapper(NoticeableFuture<T> noticeableFuture) {
		_noticeableFuture = noticeableFuture;
	}

	@Override
	public boolean addFutureListener(FutureListener<T> futureListener) {
		return _noticeableFuture.addFutureListener(futureListener);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return _noticeableFuture.cancel(mayInterruptIfRunning);
	}

	@Override
	public T get() throws ExecutionException, InterruptedException {
		return _noticeableFuture.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit)
		throws ExecutionException, InterruptedException, TimeoutException {

		return _noticeableFuture.get(timeout, unit);
	}

	@Override
	public boolean isCancelled() {
		return _noticeableFuture.isCancelled();
	}

	@Override
	public boolean isDone() {
		return _noticeableFuture.isDone();
	}

	@Override
	public boolean removeFutureListener(FutureListener<T> futureListener) {
		return _noticeableFuture.removeFutureListener(futureListener);
	}

	private final NoticeableFuture<T> _noticeableFuture;

}