/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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

package com.liferay.portal.security.permission;

import com.liferay.portal.NoSuchResourceActionException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.security.annotation.AccessControl;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.PasswordPolicy;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portlet.expando.model.ExpandoColumn;

import java.io.InputStream;

import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.PageContext;

/**
 * @author Brian Wing Shun Chan
 * @author Daeyoung Song
 */
@AccessControl
public class ResourceActionsUtil {

	/**
	 * @deprecated {@link #getActionNamePrefix}
	 */
	public static final String ACTION_NAME_PREFIX = "action.";

	/**
	 * @deprecated {@link #getModelResourceNamePrefix}
	 */
	public static final String MODEL_RESOURCE_NAME_PREFIX = "model.resource.";

	/**
	 * @deprecated {@link #getOrganizationModelResources}
	 */
	public static final String[] ORGANIZATION_MODEL_RESOURCES = {
		Organization.class.getName(), PasswordPolicy.class.getName(),
		User.class.getName()
	};

	/**
	 * @deprecated {@link #getPortalModelResources}
	 */
	public static final String[] PORTAL_MODEL_RESOURCES = {
		ExpandoColumn.class.getName(), Organization.class.getName(),
		PasswordPolicy.class.getName(), Role.class.getName(),
		User.class.getName(), UserGroup.class.getName()
	};

	public static void checkAction(String name, String actionId)
		throws NoSuchResourceActionException {

		getResourceActions().checkAction(name, actionId);
	}

	public static String getAction(Locale locale, String action) {
		return getResourceActions().getAction(locale, action);
	}

	public static String getAction(PageContext pageContext, String action) {
		return getResourceActions().getAction(pageContext, action);
	}

	public static String getActionNamePrefix() {
		return getResourceActions().getActionNamePrefix();
	}

	public static List<String> getActionsNames(
		PageContext pageContext, List<String> actions) {

		return getResourceActions().getActionsNames(pageContext, actions);
	}

	public static List<String> getActionsNames(
		PageContext pageContext, String name, long actionIds) {

		return getResourceActions().getActionsNames(
			pageContext, name, actionIds);
	}

	public static List<String> getModelNames() {
		return getResourceActions().getModelNames();
	}

	public static List<String> getModelPortletResources(String name) {
		return getResourceActions().getModelPortletResources(name);
	}

	public static String getModelResource(Locale locale, String name) {
		return getResourceActions().getModelResource(locale, name);
	}

	public static String getModelResource(
		PageContext pageContext, String name) {

		return getResourceActions().getModelResource(pageContext, name);
	}

	public static List<String> getModelResourceActions(String name) {
		return getResourceActions().getModelResourceActions(name);
	}

	public static List<String> getModelResourceGroupDefaultActions(
		String name) {

		return getResourceActions().getModelResourceGroupDefaultActions(name);
	}

	public static List<String> getModelResourceGuestDefaultActions(
		String name) {

		return getResourceActions().getModelResourceGuestDefaultActions(name);
	}

	public static List<String> getModelResourceGuestUnsupportedActions(
		String name) {

		return getResourceActions().getModelResourceGuestUnsupportedActions(
			name);
	}

	public static String getModelResourceNamePrefix() {
		return getResourceActions().getModelResourceNamePrefix();
	}

	public static List<String> getModelResourceOwnerDefaultActions(
		String name) {

		return getResourceActions().getModelResourceOwnerDefaultActions(name);
	}

	public static String[] getOrganizationModelResources() {
		return getResourceActions().getOrganizationModelResources();
	}

	public static String[] getPortalModelResources() {
		return getResourceActions().getPortalModelResources();
	}

	public static String getPortletBaseResource(String portletName) {
		return getResourceActions().getPortletBaseResource(portletName);
	}

	public static List<String> getPortletModelResources(String portletName) {
		return getResourceActions().getPortletModelResources(portletName);
	}

	public static List<String> getPortletNames() {
		return getResourceActions().getPortletNames();
	}

	public static List<String> getPortletResourceActions(Portlet portlet) {
		return getResourceActions().getPortletResourceActions(portlet);
	}

	public static List<String> getPortletResourceActions(String name) {
		return getResourceActions().getPortletResourceActions(name);
	}

	public static List<String> getPortletResourceGroupDefaultActions(
		String name) {

		return getResourceActions().getPortletResourceGroupDefaultActions(name);
	}

	public static List<String> getPortletResourceGuestDefaultActions(
		String name) {

		return getResourceActions().getPortletResourceGuestDefaultActions(name);
	}

	public static List<String> getPortletResourceGuestUnsupportedActions(
		String name) {

		return getResourceActions().getPortletResourceGuestUnsupportedActions(
			name);
	}

	public static List<String> getPortletResourceLayoutManagerActions(
		String name) {

		return getResourceActions().getPortletResourceLayoutManagerActions(
			name);
	}

	public static ResourceActions getResourceActions() {
		return _resourceActions;
	}

	public static List<String> getResourceActions(String name) {
		return getResourceActions().getResourceActions(name);
	}

	public static List<String> getResourceActions(
		String portletResource, String modelResource) {

		return getResourceActions().getResourceActions(
			portletResource, modelResource);
	}

	public static List<String> getResourceGroupDefaultActions(String name) {
		return getResourceActions().getResourceGroupDefaultActions(name);
	}

	public static List<String> getResourceGuestUnsupportedActions(
		String portletResource, String modelResource) {

		return getResourceActions().getResourceGuestUnsupportedActions(
			portletResource, modelResource);
	}

	/**
	 * @deprecated {@link #getRoles(long, Group, String, int[])}
	 */
	public static List<Role> getRoles(
			long companyId, Group group, String modelResource)
		throws SystemException {

		return getResourceActions().getRoles(companyId, group, modelResource);
	}

	public static List<Role> getRoles(
			long companyId, Group group, String modelResource, int[] roleTypes)
		throws SystemException {

		return getResourceActions().getRoles(
			companyId, group, modelResource, roleTypes);
	}

	public static boolean hasModelResourceActions(String name) {
		return getResourceActions().hasModelResourceActions(name);
	}

	/**
	 * @deprecated
	 */
	public static void init() {
	}

	public static boolean isOrganizationModelResource(String modelResource) {
		return getResourceActions().isOrganizationModelResource(modelResource);
	}

	public static boolean isPortalModelResource(String modelResource) {
		return getResourceActions().isPortalModelResource(modelResource);
	}

	public static void read(
			String servletContextName, ClassLoader classLoader, String source)
		throws Exception {

		getResourceActions().read(servletContextName, classLoader, source);
	}

	public static void read(String servletContextName, InputStream inputStream)
		throws Exception {

		getResourceActions().read(servletContextName, inputStream);
	}

	public void setResourceActions(ResourceActions resourceActions) {
		_resourceActions = resourceActions;
	}

	private static ResourceActions _resourceActions;

}