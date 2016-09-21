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

package com.liferay.portal.portlet.bridge.soy.internal;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.template.Template;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.util.HtmlImpl;

import java.io.Writer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.osgi.framework.Bundle;

/**
 * @author Marcellus Tavares
 */
public class SoyPortletHelperTest {

	@Before
	public void setUp() {
		setUpHtmlUtil();
		setUpJSONFactoryUtil();
	}

	@Test
	public void testgetPortletJavaScriptWithBundleWithoutPackageFile()
		throws Exception {

		SoyPortletHelper soyPortletHelper = new SoyPortletHelper(_mockBundle);

		String portletJavaScript = soyPortletHelper.getPortletJavaScript(
			_mockTemplate, "View", StringUtil.randomString(),
			Collections.<String>emptySet());

		Assert.assertEquals(StringPool.BLANK, portletJavaScript);
	}

	@Test
	public void testgetPortletJavaScriptWithBundleWithPackageFile()
		throws Exception {

		Bundle bundle = (Bundle)ProxyUtil.newProxyInstance(
			Bundle.class.getClassLoader(), new Class<?>[] {Bundle.class},
			new InvocationHandler() {

				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
					throws NoSuchMethodException {

					if (method.equals(
							Bundle.class.getMethod("getEntry", String.class)) &&
						"package.json".equals(args[0])) {

						return SoyPortletHelperTest.class.getResource(
							"dependencies/package.json");
					}

					return null;
				}

			});

		SoyPortletHelper soyPortletHelper = new SoyPortletHelper(bundle);

		String portletNamespace = StringUtil.randomString();

		// Expected

		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		String portletComponentId = portletNamespace.concat("PortletComponent");

		jsonObject.put(TemplateConstants.NAMESPACE, portletNamespace);
		jsonObject.put("element", StringPool.POUND.concat(portletComponentId));
		jsonObject.put("id", portletComponentId);

		jsonObject.put("key 1", "value 1");

		String expectedPortletJavaScript =
			soyPortletHelper.getPortletJavaScript(
				jsonObject.toJSONString(), portletNamespace,
				"\"SampleModuleName/View.soy\"");

		// Actual

		Template template = new MockTemplate();

		template.put(TemplateConstants.NAMESPACE, portletNamespace);
		template.put("element", StringPool.POUND.concat(portletComponentId));
		template.put("id", portletComponentId);
		template.put("key 1", "value 1");

		String actualPortletJavaScript = soyPortletHelper.getPortletJavaScript(
			template, "View", portletNamespace, Collections.<String>emptySet());

		Assert.assertEquals(expectedPortletJavaScript, actualPortletJavaScript);
	}

	@Test
	public void testTemplateNamespace() throws Exception {
		String path = "View";

		SoyPortletHelper soyPortletHelper = new SoyPortletHelper(_mockBundle);

		Assert.assertEquals(
			path.concat(".render"),
			soyPortletHelper.getTemplateNamespace(path));
	}

	protected void setUpHtmlUtil() {
		HtmlUtil htmlUtil = new HtmlUtil();

		htmlUtil.setHtml(new HtmlImpl());
	}

	protected void setUpJSONFactoryUtil() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());
	}

	private final Bundle _mockBundle = (Bundle)ProxyUtil.newProxyInstance(
		Bundle.class.getClassLoader(), new Class<?>[] {Bundle.class},
		new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				return null;
			}

		});

	private final Template _mockTemplate = (Template)ProxyUtil.newProxyInstance(
		Template.class.getClassLoader(), new Class<?>[] {Template.class},
		new InvocationHandler() {

			@Override
			public Object invoke(Object proxy, Method method, Object[] args) {
				return null;
			}

		});

	private static class MockTemplate
		extends HashMap<String, Object> implements Template {

		@Override
		public void doProcessTemplate(Writer writer) {
		}

		@Override
		public Object get(String key) {
			return super.get(key);
		}

		@Override
		public String[] getKeys() {
			Set<String> keys = keySet();

			return keys.toArray(new String[keys.size()]);
		}

		@Override
		public void prepare(HttpServletRequest request) {
		}

		@Override
		public void processTemplate(Writer writer) {
		}

	}

}