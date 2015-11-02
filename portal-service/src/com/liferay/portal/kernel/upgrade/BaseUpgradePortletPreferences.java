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

package com.liferay.portal.kernel.upgrade;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PortletKeys;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

/**
 * @author Jorge Ferrer
 * @author Brian Wing Shun Chan
 */
public abstract class BaseUpgradePortletPreferences extends UpgradeProcess {

	protected void deletePortletPreferences(
			Connection con, long portletPreferencesId)
		throws Exception {

		runSQL(
			con,
			"delete from PortletPreferences where portletPreferencesId = " +
				portletPreferencesId);
	}

	@Override
	protected void doUpgrade() throws Exception {
		updatePortletPreferences();
	}

	protected long getCompanyId(Connection con, String sql, long primaryKey)
		throws Exception {

		long companyId = 0;

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement(sql);

			ps.setLong(1, primaryKey);

			rs = ps.executeQuery();

			while (rs.next()) {
				companyId = rs.getLong("companyId");
			}
		}
		finally {
			DataAccess.cleanUp(null, ps, rs);
		}

		return companyId;
	}

	protected Object[] getGroup(Connection con, long groupId) throws Exception {
		Object[] group = null;

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement(
				"select companyId from Group_ where groupId = ?");

			ps.setLong(1, groupId);

			rs = ps.executeQuery();

			while (rs.next()) {
				long companyId = rs.getLong("companyId");

				group = new Object[] {groupId, companyId};
			}
		}
		finally {
			DataAccess.cleanUp(null, ps, rs);
		}

		return group;
	}

	protected Object[] getLayout(Connection con, long plid) throws Exception {
		Object[] layout = null;

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement(
				"select groupId, companyId, privateLayout, layoutId from " +
					"Layout where plid = ?");

			ps.setLong(1, plid);

			rs = ps.executeQuery();

			while (rs.next()) {
				long groupId = rs.getLong("groupId");
				long companyId = rs.getLong("companyId");
				boolean privateLayout = rs.getBoolean("privateLayout");
				long layoutId = rs.getLong("layoutId");

				layout = new Object[] {
					groupId, companyId, privateLayout, layoutId
				};
			}
		}
		finally {
			DataAccess.cleanUp(null, ps, rs);
		}

		return layout;
	}

	protected String getLayoutUuid(Connection con, long plid, long layoutId)
		throws Exception {

		Object[] layout = getLayout(con, plid);

		if (layout == null) {
			return null;
		}

		String uuid = null;

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement(
				"select uuid_ from Layout where groupId = ? and " +
					"privateLayout = ? and layoutId = ?");

			long groupId = (Long)layout[0];
			boolean privateLayout = (Boolean)layout[2];

			ps.setLong(1, groupId);
			ps.setBoolean(2, privateLayout);
			ps.setLong(3, layoutId);

			rs = ps.executeQuery();

			if (rs.next()) {
				uuid = rs.getString("uuid_");
			}
		}
		finally {
			DataAccess.cleanUp(null, ps, rs);
		}

		return uuid;
	}

	protected String[] getPortletIds() {
		return new String[0];
	}

	protected String getUpdatePortletPreferencesWhereClause() {
		String[] portletIds = getPortletIds();

		if (portletIds.length == 0) {
			throw new IllegalArgumentException(
				"Subclasses must override getPortletIds or " +
					"getUpdatePortletPreferencesWhereClause");
		}

		StringBundler sb = new StringBundler(portletIds.length * 5 - 1);

		for (int i = 0; i < portletIds.length; i++) {
			String portletId = portletIds[i];

			sb.append("portletId ");

			if (portletId.contains(StringPool.PERCENT)) {
				sb.append(" like '");
				sb.append(portletId);
				sb.append("'");
			}
			else {
				sb.append(" = '");
				sb.append(portletId);
				sb.append("'");
			}

			if ((i + 1) < portletIds.length) {
				sb.append(" or ");
			}
		}

		return sb.toString();
	}

	protected void updatePortletPreferences() throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try (Connection con = DataAccess.getUpgradeOptimizedConnection()) {
			StringBundler sb = new StringBundler(4);

			sb.append("select portletPreferencesId, ownerId, ownerType, ");
			sb.append("plid, portletId, preferences from PortletPreferences");

			String whereClause = getUpdatePortletPreferencesWhereClause();

			if (Validator.isNotNull(whereClause)) {
				sb.append(" where ");
				sb.append(whereClause);
			}

			String sql = sb.toString();

			ps = con.prepareStatement(sql);

			rs = ps.executeQuery();

			while (rs.next()) {
				long portletPreferencesId = rs.getLong("portletPreferencesId");
				long ownerId = rs.getLong("ownerId");
				int ownerType = rs.getInt("ownerType");
				long plid = rs.getLong("plid");
				String portletId = rs.getString("portletId");
				String preferences = GetterUtil.getString(
					rs.getString("preferences"));

				long companyId = 0;

				if (ownerType == PortletKeys.PREFS_OWNER_TYPE_ARCHIVED) {
					companyId = getCompanyId(
						con,
						"select companyId from PortletItem where " +
							"portletItemId = ?",
						ownerId);
				}
				else if (ownerType == PortletKeys.PREFS_OWNER_TYPE_COMPANY) {
					companyId = ownerId;
				}
				else if (ownerType == PortletKeys.PREFS_OWNER_TYPE_GROUP) {
					Object[] group = getGroup(con, ownerId);

					if (group != null) {
						companyId = (Long)group[1];
					}
				}
				else if (ownerType == PortletKeys.PREFS_OWNER_TYPE_LAYOUT) {
					Object[] layout = getLayout(con, plid);

					if (layout != null) {
						companyId = (Long)layout[1];
					}
				}
				else if (ownerType ==
							PortletKeys.PREFS_OWNER_TYPE_ORGANIZATION) {

					companyId = getCompanyId(
						con,
						"select companyId from Organization_ where " +
							"organizationId = ?",
						ownerId);
				}
				else if (ownerType == PortletKeys.PREFS_OWNER_TYPE_USER) {
					companyId = getCompanyId(
						con, "select companyId from User_ where userId = ?",
						ownerId);
				}
				else {
					throw new UnsupportedOperationException(
						"Unsupported owner type " + ownerType);
				}

				if (companyId > 0) {
					String newPreferences = upgradePreferences(
						con, companyId, ownerId, ownerType, plid, portletId,
						preferences);

					if (!preferences.equals(newPreferences)) {
						updatePortletPreferences(
							con, portletPreferencesId, newPreferences);
					}
				}
				else {
					deletePortletPreferences(con, portletPreferencesId);
				}
			}
		}
		finally {
			DataAccess.cleanUp(null, ps, rs);
		}
	}

	protected void updatePortletPreferences(
			Connection con, long portletPreferencesId, String preferences)
		throws Exception {

		PreparedStatement ps = null;

		try {
			ps = con.prepareStatement(
				"update PortletPreferences set preferences = ? where " +
					"portletPreferencesId = " + portletPreferencesId);

			ps.setString(1, preferences);

			ps.executeUpdate();
		}
		finally {
			DataAccess.cleanUp(ps);
		}
	}

	protected void upgradeMultiValuePreference(
			PortletPreferences portletPreferences, String key)
		throws ReadOnlyException {

		String value = portletPreferences.getValue(key, StringPool.BLANK);

		if (Validator.isNotNull(value)) {
			portletPreferences.setValues(key, StringUtil.split(value));
		}
	}

	protected String upgradePreferences(
			Connection con, long companyId, long ownerId, int ownerType,
			long plid, String portletId, String xml)
		throws Exception {

		return upgradePreferences(
			companyId, ownerId, ownerType, plid, portletId, xml);
	}

	/**
	 * @deprecated As of 7.0.0, replaced by {@link
	 *             #updatePortletPreferences(Connection, long, String)}
	 */
	@Deprecated
	protected abstract String upgradePreferences(
			long companyId, long ownerId, int ownerType, long plid,
			String portletId, String xml)
		throws Exception;

}