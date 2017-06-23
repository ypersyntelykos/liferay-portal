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

import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.GroupConstants;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.LayoutSet;
import com.liferay.portal.kernel.service.GroupLocalServiceUtil;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.VirtualHostLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.CompanyTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Michael Bowerman
 */
public class PortalImplLayoutFriendlyURLTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_company = CompanyTestUtil.addCompany();

		_companyVirtualHostname = _company.getVirtualHostname();

		_group = GroupLocalServiceUtil.fetchGroup(
			_company.getCompanyId(), GroupConstants.GUEST);

		_layout = LayoutLocalServiceUtil.fetchDefaultLayout(
			_group.getGroupId(), false);
	}

	@Test
	public void testCompanyDefaultSiteVirtualHost() throws Exception {
		testLayoutFriendlyURL(
			_companyVirtualHostname, _layout.getFriendlyURL());
	}

	@Test
	public void testCompanyDefaultSiteVirtualHostWithLayoutSetVirtualHost()
		throws Exception {

		assignNewPublicLayoutSetVirtualHost();

		String expectedURL =
			PropsValues.LAYOUT_FRIENDLY_URL_PUBLIC_SERVLET_MAPPING +
				_group.getFriendlyURL() + _layout.getFriendlyURL();

		testLayoutFriendlyURL(_companyVirtualHostname, expectedURL);
	}

	@Test
	public void testLayoutSetVirtualHost() throws Exception {
		assignNewPublicLayoutSetVirtualHost();

		testLayoutFriendlyURL(
			_publicLayoutSetVirtualHostname, _layout.getFriendlyURL());
	}

	@Test
	public void testLocalhost() throws Exception {
		String expectedURL =
			PropsValues.LAYOUT_FRIENDLY_URL_PUBLIC_SERVLET_MAPPING +
				_group.getFriendlyURL() + _layout.getFriendlyURL();

		testLayoutFriendlyURL("localhost", expectedURL);
	}

	@Test
	public void testLocalhostWithLayoutSetVirtualHost() throws Exception {
		assignNewPublicLayoutSetVirtualHost();

		String expectedURL =
			PropsValues.LAYOUT_FRIENDLY_URL_PUBLIC_SERVLET_MAPPING +
				_group.getFriendlyURL() + _layout.getFriendlyURL();

		testLayoutFriendlyURL("localhost", expectedURL);
	}

	protected void assignNewPublicLayoutSetVirtualHost() {
		LayoutSet layoutSet = _group.getPublicLayoutSet();

		_publicLayoutSetVirtualHostname =
			RandomTestUtil.randomString() + "." +
				RandomTestUtil.randomString(3);

		VirtualHostLocalServiceUtil.updateVirtualHost(
			_company.getCompanyId(), layoutSet.getLayoutSetId(),
			_publicLayoutSetVirtualHostname);
	}

	protected void testLayoutFriendlyURL(
			String virtualHostname, String expectedURL)
		throws Exception {

		_company.setVirtualHostname(virtualHostname);

		ThemeDisplay themeDisplay = new ThemeDisplay();

		themeDisplay.setCompany(_company);
		themeDisplay.setI18nLanguageId(StringPool.BLANK);
		themeDisplay.setLayout(_layout);
		themeDisplay.setLayoutSet(_layout.getLayoutSet());
		themeDisplay.setPortalDomain(virtualHostname);
		themeDisplay.setSecure(false);
		themeDisplay.setServerName(virtualHostname);
		themeDisplay.setServerPort(8080);
		themeDisplay.setSiteGroupId(_group.getGroupId());
		themeDisplay.setUser(TestPropsValues.getUser());
		themeDisplay.setWidget(false);

		Assert.assertEquals(
			expectedURL, PortalUtil.getLayoutFriendlyURL(themeDisplay));
	}

	@DeleteAfterTestRun
	private Company _company;

	private String _companyVirtualHostname;
	private Group _group;
	private Layout _layout;
	private String _publicLayoutSetVirtualHostname;

}