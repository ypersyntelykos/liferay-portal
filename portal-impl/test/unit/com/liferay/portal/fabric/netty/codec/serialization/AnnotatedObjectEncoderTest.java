
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

import com.liferay.portal.kernel.io.AnnotatedObjectInputStream;
import com.liferay.portal.kernel.test.CodeCoverageAssertor;
import com.liferay.portal.kernel.test.ReflectionTestUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;

import org.testng.Assert;

/**
 * @author Shuyang Zhou
 */
public class AnnotatedObjectEncoderTest {

	@ClassRule
	public static CodeCoverageAssertor codeCoverageAssertor =
		new CodeCoverageAssertor();

	@Test
	public void testEncode() throws Exception {
		doTestEncode(true);
		doTestEncode(false);
	}

	@Test
	public void testName() {
		Assert.assertEquals(
			AnnotatedObjectEncoder.class.getName(),
			AnnotatedObjectEncoder.NAME);
	}

	protected void doTestEncode(boolean bridge) throws Exception {
		AnnotatedObjectEncoder annotatedObjectEncoder =
			AnnotatedObjectEncoder.INSTANCE;

		Date date = new Date();

		ByteBuf byteBuf = Unpooled.buffer();

		if (bridge) {
			ReflectionTestUtil.invokeBridge(
				annotatedObjectEncoder, "encode",
				new Class<?>[] {
					ChannelHandlerContext.class, Object.class, ByteBuf.class},
				null, date, byteBuf);
		}
		else {
			annotatedObjectEncoder.encode(null, date, byteBuf);
		}

		Assert.assertEquals(byteBuf.readInt(), byteBuf.readableBytes());

		AnnotatedObjectInputStream annotatedObjectInputStream =
			new AnnotatedObjectInputStream(new ByteBufInputStream(byteBuf));

		Assert.assertEquals(date, annotatedObjectInputStream.readObject());
		Assert.assertFalse(byteBuf.isReadable());
	}

}