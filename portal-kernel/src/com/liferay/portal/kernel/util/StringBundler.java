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

package com.liferay.portal.kernel.util;

import com.liferay.portal.kernel.memory.SoftReferenceThreadLocal;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

/**
 * <p>
 * See https://issues.liferay.com/browse/LPS-6072.
 * </p>
 *
 * @author Shuyang Zhou
 * @author Brian Wing Shun Chan
 */
public class StringBundler implements Serializable {

	public StringBundler() {
		_charSequences = new CharSequence[_DEFAULT_ARRAY_CAPACITY];
	}

	public StringBundler(int initialCapacity) {
		if (initialCapacity <= 0) {
			initialCapacity = _DEFAULT_ARRAY_CAPACITY;
		}

		_charSequences = new CharSequence[initialCapacity];
	}

	public StringBundler(String s) {
		_charSequences = new CharSequence[_DEFAULT_ARRAY_CAPACITY];

		_charSequences[0] = s;

		_index = 1;
	}

	public StringBundler(String[] stringArray) {
		this(stringArray, 0);
	}

	public StringBundler(String[] stringArray, int extraSpace) {
		_charSequences = new String[stringArray.length + extraSpace];

		for (String s : stringArray) {
			if ((s != null) && (s.length() > 0)) {
				_charSequences[_index++] = s;
			}
		}
	}

	public StringBundler append(boolean b) {
		if (b) {
			return append(StringPool.TRUE);
		}
		else {
			return append(StringPool.FALSE);
		}
	}

	public StringBundler append(char c) {
		return append(String.valueOf(c));
	}

	public StringBundler append(char[] chars) {
		if (chars == null) {
			return append("null");
		}
		else {
			return append(new String(chars));
		}
	}

	public StringBundler append(CharSequence charSequence) {
		if (charSequence == null) {
			charSequence = StringPool.NULL;
		}

		if (charSequence.length() == 0) {
			return this;
		}

		if (_index >= _charSequences.length) {
			expandCapacity(_charSequences.length * 2);
		}

		_charSequences[_index++] = charSequence;

		return this;
	}

	public StringBundler append(double d) {
		return append(Double.toString(d));
	}

	public StringBundler append(float f) {
		return append(Float.toString(f));
	}

	public StringBundler append(int i) {
		return append(Integer.toString(i));
	}

	public StringBundler append(long l) {
		return append(Long.toString(l));
	}

	public StringBundler append(Object obj) {
		return append(String.valueOf(obj));
	}

	public StringBundler append(String s) {
		if (s == null) {
			s = StringPool.NULL;
		}

		if (s.length() == 0) {
			return this;
		}

		if (_index >= _charSequences.length) {
			expandCapacity(_charSequences.length * 2);
		}

		_charSequences[_index++] = s;

		return this;
	}

	public StringBundler append(String[] stringArray) {
		if (ArrayUtil.isEmpty(stringArray)) {
			return this;
		}

		if ((_charSequences.length - _index) < stringArray.length) {
			expandCapacity((_charSequences.length + stringArray.length) * 2);
		}

		for (String s : stringArray) {
			if ((s != null) && (s.length() > 0)) {
				_charSequences[_index++] = s;
			}
		}

		return this;
	}

	public StringBundler append(StringBundler sb) {
		if ((sb == null) || (sb._index == 0)) {
			return this;
		}

		if ((_charSequences.length - _index) < sb._index) {
			expandCapacity((_charSequences.length + sb._index) * 2);
		}

		System.arraycopy(
			sb._charSequences, 0, _charSequences, _index, sb._index);

		_index += sb._index;

		return this;
	}

	public int capacity() {
		return _charSequences.length;
	}

	public CharSequence charSequenceAt(int index) {
		if ((index < 0) || (index >= _index)) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		return _charSequences[index];
	}

	public CharSequence[] getCharSequences() {
		return _charSequences;
	}

	public String[] getStrings() {
		String[] strings = new String[_index];

		for (int i = 0; i < _index; i++) {
			strings[i] = _charSequences[i].toString();
		}

		return strings;
	}

	public int index() {
		return _index;
	}

	public int length() {
		int length = 0;

		for (int i = 0; i < _index; i++) {
			length += _charSequences[i].length();
		}

		return length;
	}

	public void setCharSequenceAt(CharSequence charSequence, int index) {
		if ((index < 0) || (index >= _index)) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		_charSequences[index] = charSequence;
	}

	public void setIndex(int newIndex) {
		if (newIndex < 0) {
			throw new ArrayIndexOutOfBoundsException(newIndex);
		}

		if (newIndex > _charSequences.length) {
			CharSequence[] newCharSequences = new CharSequence[newIndex];

			System.arraycopy(_charSequences, 0, newCharSequences, 0, _index);

			_charSequences = newCharSequences;
		}

		if (_index < newIndex) {
			for (int i = _index; i < newIndex; i++) {
				_charSequences[i] = EmptyCharSequenece.INSTANCE;
			}
		}

		if (_index > newIndex) {
			for (int i = newIndex; i < _index; i++) {
				_charSequences[i] = null;
			}
		}

		_index = newIndex;
	}

	public void setStringAt(String s, int index) {
		setCharSequenceAt(s, index);
	}

	public String stringAt(int index) {
		CharSequence charSequence = charSequenceAt(index);

		return charSequence.toString();
	}

	@Override
	public String toString() {
		if (_index == 0) {
			return StringPool.BLANK;
		}

		if (_index == 1) {
			return _charSequences[0].toString();
		}

		if (_index == 2) {
			String s1 = _charSequences[0].toString();
			String s2 = _charSequences[1].toString();

			return s1.concat(s2);
		}

		if (_index == 3) {
			String s1 = _charSequences[0].toString();
			String s2 = _charSequences[1].toString();
			String s3 = _charSequences[3].toString();

			return s1.concat(s2).concat(s3);
		}

		int length = 0;

		for (int i = 0; i < _index; i++) {
			length += _charSequences[i].length();
		}

		StringBuilder sb = null;

		if (length > _THREAD_LOCAL_BUFFER_LIMIT) {
			sb = _stringBuilderThreadLocal.get();

			if (sb == null) {
				sb = new StringBuilder(length);

				_stringBuilderThreadLocal.set(sb);
			}
			else if (sb.capacity() < length) {
				sb.setLength(length);
			}

			sb.setLength(0);
		}
		else {
			sb = new StringBuilder(length);
		}

		for (int i = 0; i < _index; i++) {
			CharSequence charSequence = _charSequences[i];

			if (charSequence instanceof EnhancedCharSequence) {
				EnhancedCharSequence enhancedCharSequence =
					(EnhancedCharSequence)charSequence;

				enhancedCharSequence.appendTo(sb);
			}
			else {
				sb.append(_charSequences[i]);
			}
		}

		return sb.toString();
	}

	public void writeTo(Writer writer) throws IOException {
		for (int i = 0; i < _index; i++) {
			writer.write(_charSequences[i].toString());
		}
	}

	public static class ArrayCharSequence implements EnhancedCharSequence {

		public ArrayCharSequence(char[] chars, int start, int end) {
			_chars = chars;
			_start = start;
			_end = end;
		}

		@Override
		public void appendTo(StringBuilder sb) {
			sb.append(_chars, _start, _end);
		}

		@Override
		public char charAt(int index) {
			if ((index < 0) || (index >= (_end - _start))) {
				throw new StringIndexOutOfBoundsException(index);
			}

			return _chars[index];
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof ArrayCharSequence)) {
				return false;
			}

			ArrayCharSequence arrayCharSequence = (ArrayCharSequence)object;

			int length = length();

			if (length != arrayCharSequence.length()) {
				return false;
			}

			for (int i = 0; i < length; i++) {
				if (_chars[_start + i] !=
						arrayCharSequence.
							_chars[arrayCharSequence._start + i]) {

					return false;
				}
			}

			return true;
		}

		@Override
		public int hashCode() {
			int hash = _hash;

			if ((hash == 0) && (_end > _start)) {
				for (int i = _start; i < _end; i++) {
					hash = 31 * hash + _chars[i];
				}

				_hash = hash;
			}

			return hash;
		}

		@Override
		public int length() {
			return _end - _start;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			if (start < 0) {
				throw new StringIndexOutOfBoundsException(start);
			}

			int length = length();

			if (end > length) {
				throw new StringIndexOutOfBoundsException(end);
			}

			int subLength = end - start;

			if (subLength < 0) {
				throw new StringIndexOutOfBoundsException(subLength);
			}

			if ((start == 0) && (end == length)) {
				return this;
			}

			return new ArrayCharSequence(_chars, _start + start, _start + end);
		}

		@Override
		public String toString() {
			return new String(_chars, _start, _end - _start);
		}

		private final char[] _chars;
		private final int _end;
		private int _hash;
		private final int _start;

	}

	public static class EmptyCharSequenece implements EnhancedCharSequence {

		public static final EmptyCharSequenece INSTANCE =
			new EmptyCharSequenece();

		@Override
		public void appendTo(StringBuilder sb) {
		}

		@Override
		public char charAt(int index) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public int length() {
			return 0;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			throw new ArrayIndexOutOfBoundsException(start);
		}

		@Override
		public String toString() {
			return StringPool.BLANK;
		}

		private EmptyCharSequenece() {
		}

	}

	public static class SingleCharSequence implements EnhancedCharSequence {

		public SingleCharSequence(char c) {
			_c = c;
		}

		@Override
		public void appendTo(StringBuilder sb) {
			sb.append(_c);
		}

		@Override
		public char charAt(int index) {
			if (index != 0) {
				throw new ArrayIndexOutOfBoundsException(index);
			}

			return _c;
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof SingleCharSequence)) {
				return false;
			}

			SingleCharSequence singleCharSequence = (SingleCharSequence)object;

			if (_c == singleCharSequence._c) {
				return true;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return _c;
		}

		@Override
		public int length() {
			return 1;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			if (start != 0) {
				throw new ArrayIndexOutOfBoundsException(start);
			}

			if ((end < 0) || (end > 1)) {
				throw new ArrayIndexOutOfBoundsException(end);
			}

			if (end == 0) {
				return EmptyCharSequenece.INSTANCE;
			}

			return this;
		}

		@Override
		public String toString() {
			return new String(new char[] {_c});
		}

		private final char _c;

	}

	public static class StringPart implements EnhancedCharSequence {

		public StringPart(String s, int start) {
			this(s, start, s.length());
		}

		public StringPart(String s, int start, int end) {
			_s = s;
			_start = start;
			_end = end;
		}

		@Override
		public void appendTo(StringBuilder sb) {
			sb.append(_s, _start, _end);
		}

		@Override
		public char charAt(int index) {
			if ((index < 0) || (index >= (_end - _start))) {
				throw new StringIndexOutOfBoundsException(index);
			}

			return _s.charAt(_start + index);
		}

		@Override
		public boolean equals(Object object) {
			if (this == object) {
				return true;
			}

			if (!(object instanceof StringPart)) {
				return false;
			}

			StringPart stringPart = (StringPart)object;

			return _s.regionMatches(
				_start, stringPart._s, stringPart._start, stringPart.length());
		}

		@Override
		public int hashCode() {
			int hash = _hash;

			if ((hash == 0) && (_end > _start)) {
				for (int i = _start; i < _end; i++) {
					hash = 31 * hash + _s.charAt(i);
				}

				_hash = hash;
			}

			return hash;
		}

		@Override
		public int length() {
			return _end - _start;
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			if (start < 0) {
				throw new StringIndexOutOfBoundsException(start);
			}

			int length = length();

			if (end > length) {
				throw new StringIndexOutOfBoundsException(end);
			}

			int subLength = end - start;

			if (subLength < 0) {
				throw new StringIndexOutOfBoundsException(subLength);
			}

			if ((start == 0) && (end == length)) {
				return this;
			}

			return new StringPart(_s, _start + start, _start + end);
		}

		@Override
		public String toString() {
			return _s.substring(_start, _end);
		}

		private final int _end;
		private int _hash;
		private final String _s;
		private final int _start;

	}

	public interface EnhancedCharSequence extends CharSequence {

		public void appendTo(StringBuilder sb);

	}

	protected void expandCapacity(int newCapacity) {
		String[] newArray = new String[newCapacity];

		System.arraycopy(_charSequences, 0, newArray, 0, _index);

		_charSequences = newArray;
	}

	private static final int _DEFAULT_ARRAY_CAPACITY = 16;

	private static final int _THREAD_LOCAL_BUFFER_LIMIT;

	private static final ThreadLocal<StringBuilder> _stringBuilderThreadLocal;
	private static final long serialVersionUID = 1L;

	static {
		int threadLocalBufferLimit = GetterUtil.getInteger(
			System.getProperty(
				StringBundler.class.getName() + ".threadlocal.buffer.limit"),
			Integer.MAX_VALUE);

		if ((threadLocalBufferLimit > 0) &&
			(threadLocalBufferLimit < Integer.MAX_VALUE)) {

			_THREAD_LOCAL_BUFFER_LIMIT = threadLocalBufferLimit;

			_stringBuilderThreadLocal = new SoftReferenceThreadLocal<>();
		}
		else {
			_THREAD_LOCAL_BUFFER_LIMIT = Integer.MAX_VALUE;

			_stringBuilderThreadLocal = null;
		}
	}

	private CharSequence[] _charSequences;
	private int _index;

}