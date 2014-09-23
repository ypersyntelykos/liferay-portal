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

package com.liferay.portal.fabric;

import com.liferay.portal.kernel.process.ProcessCallable;
import com.liferay.portal.kernel.util.ObjectGraphUtil.AnnotatedFieldMappingVisitor;

import java.io.File;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shuyang Zhou
 */
public abstract class FabricResouceMappingVisitor
	extends AnnotatedFieldMappingVisitor {

	public FabricResouceMappingVisitor(
		Class<? extends Annotation> annotationClass) {

		super(
			Collections.<Class<?>>singleton(ProcessCallable.class),
			Collections.<Class<? extends Annotation>>singleton(annotationClass),
			Collections.<Class<?>>singleton(File.class));
	}

	public Map<File, File> getResourceMap() {
		return _resourceMap;
	}

	protected abstract File doMap(Field field, File file);

	@Override
	protected Object doMap(Field field, Object value) {
		File file = (File)value;

		File mappedFile = doMap(field, file);

		_resourceMap.put(file, mappedFile);

		return mappedFile;
	}

	private final Map<File, File> _resourceMap = new HashMap<File, File>();

}