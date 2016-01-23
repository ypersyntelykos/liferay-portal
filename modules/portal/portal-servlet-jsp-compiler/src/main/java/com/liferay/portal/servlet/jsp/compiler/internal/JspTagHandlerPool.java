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

package com.liferay.portal.servlet.jsp.compiler.internal;

import com.liferay.portal.kernel.util.GetterUtil;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletConfig;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.Tag;

import org.apache.jasper.Constants;
import org.apache.jasper.runtime.TagHandlerPool;

/**
 * @author Shuyang Zhou
 * @author Preston Crary
 * @see com.liferay.support.tomcat.jasper.runtime.TagHandlerPool
 */
public class JspTagHandlerPool extends TagHandlerPool {

	@Override
	public <T extends JspTag> JspTag get(Class<T> jspTagClass)
		throws JspException {

		int index = _counter.decrementAndGet();

		if (index >= 0) {
			return _jspTags[index];
		}

		_counter.incrementAndGet();

		try {
			return jspTagClass.newInstance();
		}
		catch (Exception e) {
			throw new JspException(e);
		}
	}

	@Override
	public void release() {
		for (int i = 0; i < _counter.get(); i++) {
			JspTag jspTag = _jspTags[i];

			if (jspTag instanceof Tag) {
				Tag tag = (Tag)jspTag;

				tag.release();
			}
		}

		_jspTags = null;
	}

	@Override
	public void reuse(JspTag jspTag) {
		int index = _counter.getAndIncrement();

		if (index < _jspTags.length) {
			_jspTags[index] = jspTag;
		}
		else {
			_counter.decrementAndGet();

			if (jspTag instanceof Tag) {
				Tag tag = (Tag)jspTag;

				tag.release();
			}
		}
	}

	@Override
	protected void init(ServletConfig config) {
		int maxSize = GetterUtil.getInteger(
			getOption(config, OPTION_MAXSIZE, null), Constants.MAX_POOL_SIZE);

		_jspTags = new JspTag[maxSize];
	}

	private final AtomicInteger _counter = new AtomicInteger();
	private JspTag[] _jspTags;

}