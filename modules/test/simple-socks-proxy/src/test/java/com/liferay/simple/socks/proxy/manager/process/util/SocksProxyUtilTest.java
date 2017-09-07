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

package com.liferay.simple.socks.proxy.manager.process.util;

import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.test.rule.CodeCoverageAssertor;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Tom Wang
 */
public class SocksProxyUtilTest {

	@ClassRule
	public static final CodeCoverageAssertor codeCoverageAssertor =
		CodeCoverageAssertor.INSTANCE;

	@Test
	public void testContructor() {
		new SocksProxyUtil();
	}

	@Test
	public void testRead() throws IOException {
		byte[] bytes = {Constants.SOCKS5_VERSION};

		InputStream inputStream = new UnsyncByteArrayInputStream(bytes);

		byte value = SocksProxyUtil.read(inputStream);

		Assert.assertEquals(Constants.SOCKS5_VERSION, value);

		try {
			SocksProxyUtil.read(inputStream);

			Assert.fail();
		}
		catch (EOFException eofe) {
		}
	}

	@Test
	public void testReadFully() throws IOException {
		byte[] bytes =
			{Constants.SOCKS5_VERSION, Constants.CMD_CONNECT, Constants.RSV};

		InputStream inputStream = new UnsyncByteArrayInputStream(bytes) {

			@Override
			public int read(byte[] bytes, int offset, int length) {
				return super.read(bytes, offset, 1);
			}

		};

		byte[] values = new byte[3];

		SocksProxyUtil.readFully(inputStream, values);

		Assert.assertEquals(Constants.SOCKS5_VERSION, values[0]);
		Assert.assertEquals(Constants.CMD_CONNECT, values[1]);
		Assert.assertEquals(Constants.RSV, values[2]);

		try {
			SocksProxyUtil.readFully(inputStream, values);

			Assert.fail();
		}
		catch (EOFException eofe) {
		}
	}

}