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

package com.liferay.osgi.felix.util;

/**
 * @author Shuyang Zhou
 */
public abstract class AbstractExtender
	extends org.apache.felix.utils.extender.AbstractExtender {

	public AbstractExtender() {
		setSynchronous(true);
	}

	@Override
	public final void setSynchronous(boolean synchronous) {
		super.setSynchronous(synchronous);
	}

}