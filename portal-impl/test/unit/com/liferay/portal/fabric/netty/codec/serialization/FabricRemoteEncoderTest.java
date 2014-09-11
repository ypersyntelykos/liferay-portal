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

package com.liferay.portal.fabric.netty.codec.serialization;

import com.liferay.portal.fabric.FabricRemote;
import com.liferay.portal.kernel.test.CodeCoverageAssertor;
import com.liferay.portal.kernel.test.ReflectionTestUtil;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Shuyang Zhou
 */
public class FabricRemoteEncoderTest {

	@ClassRule
	public static CodeCoverageAssertor codeCoverageAssertor =
		new CodeCoverageAssertor();

	@Test
	public void testEncode() {
		FabricRemoteEncoder fabricRemoteEncoder = FabricRemoteEncoder.INSTANCE;

		final String stubString = "stubString";

		FabricRemote<String> fabricRemote = new FabricRemote<String>() {

			@Override
			public String toStub() {
				return stubString;
			}

		};

		List<Object> list = new ArrayList<Object>();

		fabricRemoteEncoder.encode(null, fabricRemote, list);

		Assert.assertEquals(1, list.size());
		Assert.assertEquals(stubString, list.get(0));

		list.clear();

		ReflectionTestUtil.invokeBridge(
			fabricRemoteEncoder, "encode",
			new Class<?>[] {
				ChannelHandlerContext.class, Object.class, List.class}, null,
			fabricRemote, list);

		Assert.assertEquals(1, list.size());
		Assert.assertEquals(stubString, list.get(0));
	}

}