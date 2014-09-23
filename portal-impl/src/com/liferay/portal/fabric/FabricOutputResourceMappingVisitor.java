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

import com.liferay.portal.fabric.netty.repository.Repository;

import java.io.File;

import java.lang.reflect.Field;

/**
 * @author Shuyang Zhou
 */
public class FabricOutputResourceMappingVisitor
	extends FabricResouceMappingVisitor {

	public FabricOutputResourceMappingVisitor(File remoteRepositoryFolder) {
		super(OutputResource.class);

		_remoteRepositoryFolder = remoteRepositoryFolder;
	}

	@Override
	protected File doMap(Field field, File file) {
		return new File(
			_remoteRepositoryFolder,
			Repository.getRepositoryFile(file.getName()));
	}

	private final File _remoteRepositoryFolder;

}