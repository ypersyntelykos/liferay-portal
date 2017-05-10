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

package com.liferay.portlet.asset.util;

import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;

import java.text.Collator;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.portlet.PortletURL;

/**
 * @author Preston Crary
 */
public class AssetPortletAddURL implements Comparable<AssetPortletAddURL> {

	public AssetPortletAddURL(
		String portletId, String name, ResourceBundle resourceBundle,
		Locale locale, PortletURL addPortletURL) {

		_portletId = portletId;
		_name = name;
		_locale = locale;
		_addPortletURL = addPortletURL;

		_modelResource = _getModelResource(resourceBundle);

		_collator = Collator.getInstance(locale);
	}

	@Override
	public int compareTo(AssetPortletAddURL assetPortletAddURL) {
		return _collator.compare(
			_modelResource, assetPortletAddURL._modelResource);
	}

	public PortletURL getAddPortletURL() {
		return _addPortletURL;
	}

	public String getModelResource() {
		return _modelResource;
	}

	public String getName() {
		return _name;
	}

	public String getPortletId() {
		return _portletId;
	}

	/**
	 * @see AssetRendererFactory#getTypeName(Locale)
	 */
	private String _getModelResource(ResourceBundle resourceBundle) {
		String modelResourceNamePrefix =
			ResourceActionsUtil.getModelResourceNamePrefix();

		String key = modelResourceNamePrefix.concat(_name);

		String value = LanguageUtil.get(_locale, key, null);

		if ((value == null) && (resourceBundle != null)) {
			value = ResourceBundleUtil.getString(resourceBundle, key);
		}

		if (value == null) {
			value = _name;
		}

		return value;
	}

	private final PortletURL _addPortletURL;
	private final Collator _collator;
	private final Locale _locale;
	private final String _modelResource;
	private final String _name;
	private final String _portletId;

}