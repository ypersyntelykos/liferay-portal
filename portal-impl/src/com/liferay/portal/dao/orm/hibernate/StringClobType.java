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

package com.liferay.portal.dao.orm.hibernate;

import com.liferay.portal.kernel.memory.SoftReferenceThreadLocal;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.io.Reader;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Shuyang Zhou
 */
@SuppressWarnings("deprecation")
public class StringClobType extends org.hibernate.type.StringClobType {

	@Override
	public boolean equals(Object x, Object y) {
		if (Validator.equals(x, y)) {
			return true;
		}
		else if (((x == null) || x.equals(StringPool.BLANK)) &&
				 ((y == null) || y.equals(StringPool.BLANK))) {

			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
		throws SQLException {

		Reader reader = rs.getCharacterStream(names[0]);

		if (reader == null) {
			return null;
		}

		BufferBag bufferBag = _bufferBagThreadLocal.get();

		if (bufferBag == null) {
			bufferBag = new BufferBag(1024);

			_bufferBagThreadLocal.set(bufferBag);
		}

		char[] buffer = bufferBag._buffer;
		StringBuilder sb = bufferBag._sb;

		sb.setLength(0);

		int index = -1;

		try {
			while ((index = reader.read(buffer)) != -1) {
				sb.append(buffer, 0, index);
			}
		}
		catch (IOException ioe) {
			throw new SQLException(ioe);
		}

		return sb.toString();
	}

	private static final ThreadLocal<BufferBag> _bufferBagThreadLocal =
		new SoftReferenceThreadLocal<>();

	private static class BufferBag {

		public BufferBag(int size) {
			_buffer = new char[size];
			_sb = new StringBuilder(size);
		}

		private final char[] _buffer;
		private final StringBuilder _sb;

	}

}