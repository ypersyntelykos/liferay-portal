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

package com.liferay.portal.util;

import com.liferay.portal.kernel.test.CaptureHandler;
import com.liferay.portal.kernel.test.JDKLoggerTestUtil;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.PredicateFilter;
import com.liferay.portal.kernel.util.StringPool;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Miguel Pastor
 * @author Preston Crary
 */
public class HttpImplTest {

	@Test
	public void testAddBooleanParameter() {
		Assert.assertEquals(
			"http://foo?p1=true",
			_httpImpl.addParameter("http://foo", "p1", true));
	}

	@Test
	public void testAddDoubleParameter() {
		Assert.assertEquals(
			"http://foo?p1=1.0",
			_httpImpl.addParameter("http://foo", "p1", 1.0));
	}

	@Test
	public void testAddIntParameter() {
		Assert.assertEquals(
			"http://foo?p1=1",
			_httpImpl.addParameter("http://foo", "p1", 1));
	}

	@Test
	public void testAddLongParameter() {
		Assert.assertEquals(
			"http://foo?p1=1",
			_httpImpl.addParameter("http://foo", "p1", 1L));
	}

	@Test
	public void testAddParameterNull() {
		Assert.assertEquals(
			"http://foo?p=null",
			_httpImpl.addParameter("http://foo", "p", null));
	}

	@Test
	public void testAddParameterTrailingQuestion() {
		Assert.assertEquals(
			"http://foo?p1=1",
			_httpImpl.addParameter("http://foo?", "p1", "1"));
	}

	@Test
	public void testAddParameterWithExistingParameter() {
		Assert.assertEquals(
			"http://foo?p1=1&p2=2",
			_httpImpl.addParameter("http://foo?p1=1", "p2", "2"));
	}

	@Test
	public void testAddParameterWithExistingParameterAnchor() {
		Assert.assertEquals(
			"http://foo?p1=1&p2=2#anchor",
			_httpImpl.addParameter("http://foo?p1=1#anchor", "p2", "2"));
	}

	@Test
	public void testAddParameterWithExistingParameterAnchorTrailingAmpersand() {
		Assert.assertEquals(
			"http://foo?p1=1&p2=2#anchor",
			_httpImpl.addParameter("http://foo?p1=1&#anchor", "p2", "2"));
	}

	@Test
	public void testAddParameterWithExistingParameterTrailingAmpersand() {
		Assert.assertEquals(
			"http://foo?p1=1&p2=2",
			_httpImpl.addParameter("http://foo?p1=1&", "p2", "2"));
	}

	@Test
	public void testAddShortParameter() {
		Assert.assertEquals(
			"http://foo?p1=1",
			_httpImpl.addParameter("http://foo", "p1", (short)1));
	}

	@Test
	public void testAddStringParameter() {
		Assert.assertEquals(
			"http://foo?p1=1",
			_httpImpl.addParameter("http://foo", "p1", "1"));
	}

	@Test
	public void testDecodeMultipleCharacterEncodedPath() {
		Assert.assertEquals(
			"http://foo?p=$param",
			_httpImpl.decodePath("http://foo%3Fp%3D%24param"));
	}

	@Test
	public void testDecodeNoCharacterEncodedPath() {
		Assert.assertEquals("http://foo", _httpImpl.decodePath("http://foo"));
	}

	@Test
	public void testDecodeSingleCharacterEncodedPath() {
		Assert.assertEquals(
			"http://foo#anchor", _httpImpl.decodePath("http://foo%23anchor"));
	}

	@Test
	public void testDecodeURLWithInvalidURLEncoding() {
		testDecodeURLWithInvalidURLEncoding("%");
		testDecodeURLWithInvalidURLEncoding("%0");
		testDecodeURLWithInvalidURLEncoding("%00%");
		testDecodeURLWithInvalidURLEncoding("%00%0");
		testDecodeURLWithInvalidURLEncoding("http://localhost:8080/?id=%");
	}

	@Test
	public void testDecodeURLWithNotHexChars() throws Exception {
		testDecodeURLWithNotHexChars("%0" + (char)(CharPool.NUMBER_0 - 1));
		testDecodeURLWithNotHexChars("%0" + (char)(CharPool.NUMBER_9 + 1));
		testDecodeURLWithNotHexChars("%0" + (char)(CharPool.UPPER_CASE_A - 1));
		testDecodeURLWithNotHexChars("%0" + (char)(CharPool.UPPER_CASE_F + 1));
		testDecodeURLWithNotHexChars("%0" + (char)(CharPool.LOWER_CASE_A - 1));
		testDecodeURLWithNotHexChars("%0" + (char)(CharPool.LOWER_CASE_F + 1));
	}

	@Test
	public void testEncodeMultipleCharacterEncodedPath() {
		Assert.assertEquals(
			"http%3A//foo%3Fp%3D%24param",
			_httpImpl.encodePath("http://foo?p=$param"));
	}

	@Test
	public void testEncodeNoCharacterEncodedPath() {
		Assert.assertEquals("http%3A//foo", _httpImpl.encodePath("http://foo"));
	}

	@Test
	public void testEncodeSingleCharacterEncodedPath() {
		Assert.assertEquals(
			"http%3A//foo%23anchor", _httpImpl.encodePath("http://foo#anchor"));
	}

	@Test
	public void testGetParameterMapWithCorrectQuery() {
		Map<String, String[]> parameterMap = _httpImpl.getParameterMap(
			"a=1&b=2");

		Assert.assertNotNull(parameterMap);

		Assert.assertEquals("1", parameterMap.get("a")[0]);
		Assert.assertEquals("2", parameterMap.get("b")[0]);
	}

	@Test
	public void testGetParameterMapWithMultipleBadParameter() {
		Map<String, String[]> parameterMap = _httpImpl.getParameterMap(
			"null&a=1&null");

		Assert.assertNotNull(parameterMap);

		Assert.assertEquals("1", parameterMap.get("a")[0]);
	}

	@Test
	public void testGetParameterMapWithSingleBadParameter() {
		Map<String, String[]> parameterMap = _httpImpl.getParameterMap(
			"null&a=1");

		Assert.assertNotNull(parameterMap);

		Assert.assertEquals("1", parameterMap.get("a")[0]);
	}

	@Test
	public void testGetParameterOneWithThreeParametersAnchorEscaped() {
		Assert.assertEquals(
			"1",
			_httpImpl.getParameter(
				"http://foo?p1=1&amp;p2=2&amp;p3=3&amp;#anchor", "p1"));
	}

	@Test
	public void testGetParameterThreeWithThreeParameters() {
		Assert.assertEquals(
			"3",
			_httpImpl.getParameter("http://foo?p1=1&p2=2&p3=3&", "p3", false));
	}

	@Test
	public void testGetParameterThreeWithThreeParametersAnchor() {
		Assert.assertEquals(
			"3",
			_httpImpl.getParameter(
				"http://foo?p1=1&p2=2&p3=3&#anchor", "p3", false));
	}

	@Test
	public void testGetParameterThreeWithThreeParametersAnchorEscaped() {
		Assert.assertEquals(
			"3",
			_httpImpl.getParameter(
				"http://foo?p1=1&amp;p2=2&amp;p3=3&amp;#anchor", "p3"));
	}

	@Test
	public void testGetParameterTwoWithThreeParameters() {
		Assert.assertEquals(
			"2",
			_httpImpl.getParameter("http://foo?p1=1&p2=2&p3=3&", "p2", false));
	}

	@Test
	public void testGetParameterTwoWithThreeParametersAnchor() {
		Assert.assertEquals(
			"2",
			_httpImpl.getParameter(
				"http://foo?p1=1&p2=2&p3=3&#anchor", "p2", false));
	}

	@Test
	public void testGetParameterTwoWithThreeParametersAnchorEscaped() {
		Assert.assertEquals(
			"2",
			_httpImpl.getParameter(
				"http://foo?p1=1&amp;p2=2&amp;p3=3&amp;#anchor", "p2"));
	}

	@Test
	public void testGetParameterWithNoParameters() {
		Assert.assertEquals(
			StringPool.BLANK,
			_httpImpl.getParameter("http://foo?", "p1", false));
	}

	@Test
	public void testGetParameterWithNoParametersAnchor() {
		Assert.assertEquals(
			StringPool.BLANK,
			_httpImpl.getParameter("http://foo?#anchor", "p1", false));
	}

	@Test
	public void testGetParameterWithOneParameter() {
		Assert.assertEquals(
			"1", _httpImpl.getParameter("http://foo?p1=1&", "p1", false));
	}

	@Test
	public void testGetParameterWithOneParameterAnchor() {
		Assert.assertEquals(
			"1",
			_httpImpl.getParameter("http://foo?p1=1&#anchor", "p1", false));
	}

	@Test
	public void testGetParameterWithOneParameterAnchorEscaped() {
		Assert.assertEquals(
			"1", _httpImpl.getParameter("http://foo?p1=1&amp;#anchor", "p1"));
	}

	@Test
	public void testNormalizePath() {
		Assert.assertEquals("/", _httpImpl.normalizePath("/.."));
		Assert.assertEquals(
			"/api/axis", _httpImpl.normalizePath("/api/%61xis"));
		Assert.assertEquals(
			"/api/%2561xis", _httpImpl.normalizePath("/api/%2561xis"));
		Assert.assertEquals(
			"/api/ax%3Fs", _httpImpl.normalizePath("/api/ax%3fs"));
		Assert.assertEquals(
			"/api/%2F/axis",
			_httpImpl.normalizePath("/api/%2f/;x=aaa_%2f_y/axis"));
		Assert.assertEquals(
			"/api/axis", _httpImpl.normalizePath("/api/;x=aaa_%2f_y/axis"));
		Assert.assertEquals(
			"/api/axis", _httpImpl.normalizePath("/api/;x=aaa_%5b_y/axis"));
		Assert.assertEquals(
			"/api/axis",
			_httpImpl.normalizePath("/api/;x=aaa_LIFERAY_TEMP_SLASH_y/axis"));
		Assert.assertEquals(
			"/api/axis",
			_httpImpl.normalizePath("/api///////%2e/../;x=y/axis"));
		Assert.assertEquals(
			"/api/axis",
			_httpImpl.normalizePath("/////api///////%2e/a/../;x=y/axis"));
		Assert.assertEquals(
			"/api/axis",
			_httpImpl.normalizePath("/////api///////%2e/../;x=y/axis"));
		Assert.assertEquals(
			"/api/axis", _httpImpl.normalizePath("/api///////%2e/axis"));
		Assert.assertEquals(
			"/api/axis", _httpImpl.normalizePath("./api///////%2e/axis"));
		Assert.assertEquals(
			"/api/axis?foo=bar&bar=foo",
			_httpImpl.normalizePath("./api///////%2e/axis?foo=bar&bar=foo"));
	}

	@Test
	public void testProtocolizeMalformedURL() {
		Assert.assertEquals(
			"foo.com", _httpImpl.protocolize("foo.com", 8080, true));
	}

	@Test
	public void testProtocolizeNonsecure() {
		Assert.assertEquals(
			"http://foo.com:8080",
			_httpImpl.protocolize("https://foo.com", 8080, false));
	}

	@Test
	public void testProtocolizeSecure() {
		Assert.assertEquals(
			"https://foo.com:8443",
			_httpImpl.protocolize("http://foo.com", 8443, true));
	}

	@Test
	public void testProtocolizeWithoutPort() {
		Assert.assertEquals(
			"http://foo.com:8443/web/guest",
			_httpImpl.protocolize("https://foo.com:8443/web/guest", -1, false));
	}

	@Test
	public void testRemoveParameterOneWithThreeParameters() {
		Assert.assertEquals(
			"http://foo?p2=2&p3=3",
			_httpImpl.removeParameter("http://foo?p1=1&p2=2&p3=3&", "p1"));
	}

	@Test
	public void testRemoveParameterOneWithThreeParametersAnchor() {
		Assert.assertEquals(
			"http://foo?p2=2&p3=3#anchor",
			_httpImpl.removeParameter(
				"http://foo?p1=1&p2=2&p3=3&#anchor", "p1"));
	}

	@Test
	public void testRemoveParametersWithThreeParameters() {
		Assert.assertEquals(
			"http://foo",
			_httpImpl.removeParameters(
				"http://foo?p1=1&p2=2&p3=3&",
				new PredicateFilter<String>() {

					@Override
					public boolean filter(String s) {
						return false;
					}

				}));
	}

	@Test
	public void testRemoveParametersWithThreeParametersAnchor() {
		Assert.assertEquals(
			"http://foo#anchor",
			_httpImpl.removeParameters(
				"http://foo?p1=1&p2=2&p3=3&#anchor",
				new PredicateFilter<String>() {

					@Override
					public boolean filter(String s) {
						return false;
					}

				}));
	}

	@Test
	public void testRemoveParameterThreeWithThreeParameters() {
		Assert.assertEquals(
			"http://foo?p1=1&p2=2",
			_httpImpl.removeParameter("http://foo?p1=1&p2=2&p3=3&", "p3"));
	}

	@Test
	public void testRemoveParameterThreeWithThreeParametersAnchor() {
		Assert.assertEquals(
			"http://foo?p1=1&p2=2#anchor",
			_httpImpl.removeParameter(
				"http://foo?p1=1&p2=2&p3=3&#anchor", "p3"));
	}

	@Test
	public void testRemoveParameterTwoWithThreeParameters() {
		Assert.assertEquals(
			"http://foo?p1=1&p3=3",
			_httpImpl.removeParameter("http://foo?p1=1&p2=2&p3=3&", "p2"));
	}

	@Test
	public void testRemoveParameterTwoWithThreeParametersAnchor() {
		Assert.assertEquals(
			"http://foo?p1=1&p3=3#anchor",
			_httpImpl.removeParameter(
				"http://foo?p1=1&p2=2&p3=3&#anchor", "p2"));
	}

	@Test
	public void testRemoveParameterWithNoParameters() {
		Assert.assertEquals(
			"http://foo?", _httpImpl.removeParameter("http://foo?", "p1"));
	}

	@Test
	public void testRemoveParameterWithNoParametersAnchor() {
		Assert.assertEquals(
			"http://foo?#anchor",
			_httpImpl.removeParameter("http://foo?#anchor", "p1"));
	}

	@Test
	public void testRemoveParameterWithOneParameter() {
		Assert.assertEquals(
			"http://foo", _httpImpl.removeParameter("http://foo?p1=1&", "p1"));
	}

	@Test
	public void testRemoveParameterWithOneParameterAnchor() {
		Assert.assertEquals(
			"http://foo#anchor",
			_httpImpl.removeParameter("http://foo?p1=1&#anchor", "p1"));
	}

	@Test
	public void testRemovePathParameters() {
		Assert.assertEquals(
			"/TestServlet/one/two",
			_httpImpl.removePathParameters(
				"/TestServlet;jsessionid=ae01b0f2af/one;test=$one@two/two"));
		Assert.assertEquals(
			"/TestServlet/one/two",
			_httpImpl.removePathParameters(
				"/TestServlet;jsessionid=ae01b0f2af;test2=123,456" +
					"/one;test=$one@two/two"));
		Assert.assertEquals(
			"/TestServlet/one/two",
			_httpImpl.removePathParameters(
				"/TestServlet/one;test=$one@two/two;jsessionid=ae01b0f2af" +
					";test2=123,456"));
		Assert.assertEquals("/", _httpImpl.removePathParameters("/;?"));
	}

	protected void testDecodeURLWithInvalidURLEncoding(String url) {
		_testDecodeURL(url, "Invalid URL encoding " + url);
	}

	protected void testDecodeURLWithNotHexChars(String url) {
		_testDecodeURL(url, "is not a hex char");
	}

	private void _testDecodeURL(String url, String expectedMessage) {
		try (CaptureHandler captureHandler =
				JDKLoggerTestUtil.configureJDKLogger(
					HttpImpl.class.getName(), Level.SEVERE)) {

			String decodeURL = _httpImpl.decodeURL(url);

			Assert.assertEquals(StringPool.BLANK, decodeURL);

			List<LogRecord> logRecords = captureHandler.getLogRecords();

			Assert.assertEquals(1, logRecords.size());

			LogRecord logRecord = logRecords.get(0);

			String message = logRecord.getMessage();

			Assert.assertTrue(message.contains(expectedMessage));
		}
	}

	private final HttpImpl _httpImpl = new HttpImpl();

}