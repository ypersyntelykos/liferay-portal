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

package com.liferay.portal.upgrade.util;

import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.DBAssertionUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Preston Crary
 */
public class UpgradeResourceBlockTest extends BaseUpgradeResourceBlock {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_regularRole = RoleTestUtil.addRole(RoleConstants.TYPE_REGULAR);

		_siteRole = RoleTestUtil.addRole(RoleConstants.TYPE_SITE);

		connection = DataAccess.getUpgradeOptimizedConnection();

		runSQL(
			"create table UpgradeResourceBlockTest(id_ LONG not null primary " +
				"key, userId LONG, resourceBlockId LONG)");

		long resourceBlockId = -1;

		StringBundler sb = new StringBundler(11);

		sb.append("insert into UpgradeResourceBlockTest(id_, userId, ");
		sb.append("resourceBlockId) values (");
		sb.append(_RESOURCE_PRIMARY_KEY);
		sb.append(", ");
		sb.append(_USER_ID);
		sb.append(", ");
		sb.append(resourceBlockId);
		sb.append(")");

		runSQL(sb.toString());

		_insertResourceTypePermission(
			-2, 0, _regularRole.getRoleId(), _COMPANY_SCOPE_ACTION_IDS);

		_insertResourceTypePermission(
			-3, _GROUP_ID, _siteRole.getRoleId(), _GROUP_SCOPE_ACTION_IDS);

		_insertResourceTypePermission(
			-4, 0, _siteRole.getRoleId(), _GROUP_TEMPLATE_SCOPE_ACTION_IDS);

		sb.setIndex(0);

		sb.append("insert into ResourceBlock(mvccVersion, resourceBlockId, ");
		sb.append("companyId, groupId, name, permissionsHash, referenceCount");
		sb.append(") values (1, ");
		sb.append(resourceBlockId);
		sb.append(", ");
		sb.append(_COMPANY_ID);
		sb.append(", ");
		sb.append(_GROUP_ID);
		sb.append(", '");
		sb.append(UpgradeResourceBlockTest.class.getName());
		sb.append("', 'hash', 1)");

		runSQL(sb.toString());

		sb.setIndex(0);

		sb.append("insert into ResourceBlockPermission(mvccVersion, ");
		sb.append("resourceBlockPermissionId, companyId, resourceBlockId, ");
		sb.append("roleId, actionIds) values (1, -5, ");
		sb.append(_COMPANY_ID);
		sb.append(", ");
		sb.append(resourceBlockId);
		sb.append(", ");
		sb.append(_regularRole.getRoleId());
		sb.append(", ");
		sb.append(_INDIVIDUAL_SCOPE_ACTION_IDS);
		sb.append(")");

		runSQL(sb.toString());
	}

	@After
	public void tearDown() throws Exception {
		runSQL(
			"delete from ResourcePermission where name = '" +
				UpgradeResourceBlockTest.class.getName() + "'");

		runSQL(TableClass.TABLE_SQL_DROP);

		connection.close();
	}

	@Test
	public void testUpgradeWithoutUserId() throws Exception {
		_testUpgrade(false);
	}

	@Test
	public void testUpgradeWithUserId() throws Exception {
		_testUpgrade(true);
	}

	public static class TableClass {

		public static final Object[][] TABLE_COLUMNS = {
			{"id_", Types.BIGINT}, {"userId", Types.BIGINT}
		};

		public static final String TABLE_NAME = "UpgradeResourceBlockTest";

		public static final String[] TABLE_SQL_ADD_INDEXES = {};

		public static final String TABLE_SQL_CREATE =
			"create table UpgradeResourceBlockTest(id_ LONG not null primary " +
				"key, userId LONG, resourceBlockId LONG)";

		public static final String TABLE_SQL_DROP =
			"drop table UpgradeResourceBlockTest";

	}

	@Override
	protected String getClassName() {
		return UpgradeResourceBlockTest.class.getName();
	}

	@Override
	protected String getPrimaryKeyName() {
		return "id_";
	}

	@Override
	protected Class<?> getTableClass() {
		return TableClass.class;
	}

	@Override
	protected boolean hasUserId() {
		return _hasUserId;
	}

	private void _assertResourcePermission(
			ResultSet rs, int scope, long primKeyId, long roleId, long ownerId,
			long actionIds, boolean viewActionId)
		throws Exception {

		Assert.assertTrue(rs.next());

		Assert.assertEquals(_COMPANY_ID, rs.getLong("companyId"));
		Assert.assertEquals(
			UpgradeResourceBlockTest.class.getName(), rs.getString("name"));
		Assert.assertEquals(scope, rs.getLong("scope"));
		Assert.assertEquals(String.valueOf(primKeyId), rs.getString("primKey"));
		Assert.assertEquals(primKeyId, rs.getLong("primKeyId"));
		Assert.assertEquals(roleId, rs.getLong("roleId"));
		Assert.assertEquals(ownerId, rs.getLong("ownerId"));
		Assert.assertEquals(actionIds, rs.getLong("actionIds"));
		Assert.assertEquals(viewActionId, rs.getBoolean("viewActionId"));
	}

	private void _assertRowsRemoved(String tableName, String primaryKeyName)
		throws Exception {

		try (PreparedStatement ps = connection.prepareStatement(
				"select * from " + tableName + " where " + primaryKeyName +
					" < 0");
			ResultSet rs = ps.executeQuery()) {

			Assert.assertFalse(rs.next());
		}
	}

	private void _insertResourceTypePermission(
			long resourceTypePermissionId, long groupId, long roleId,
			long actionIds)
		throws Exception {

		StringBundler sb = new StringBundler(15);

		sb.append("insert into ResourceTypePermission(mvccVersion, ");
		sb.append("resourceTypePermissionId, companyId, groupId, name, ");
		sb.append("roleId, actionIds) values (1, ");
		sb.append(resourceTypePermissionId);
		sb.append(", ");
		sb.append(_COMPANY_ID);
		sb.append(", ");
		sb.append(groupId);
		sb.append(", '");
		sb.append(UpgradeResourceBlockTest.class.getName());
		sb.append("', ");
		sb.append(roleId);
		sb.append(", ");
		sb.append(actionIds);
		sb.append(")");

		runSQL(sb.toString());
	}

	private void _testUpgrade(boolean hasUserId) throws Exception {
		_hasUserId = hasUserId;

		DBAssertionUtil.assertColumns(
			TableClass.TABLE_NAME, "id_", "userId", "resourceBlockId");

		doUpgrade();

		try (PreparedStatement ps = connection.prepareStatement(
				"select * from ResourcePermission where name = '" +
					UpgradeResourceBlockTest.class.getName() +
						"' order by scope");
			ResultSet rs = ps.executeQuery()) {

			_assertResourcePermission(
				rs, ResourceConstants.SCOPE_COMPANY, _COMPANY_ID,
				_regularRole.getRoleId(), 0, _COMPANY_SCOPE_ACTION_IDS, true);

			_assertResourcePermission(
				rs, ResourceConstants.SCOPE_GROUP, _GROUP_ID,
				_siteRole.getRoleId(), 0, _GROUP_SCOPE_ACTION_IDS, false);

			_assertResourcePermission(
				rs, ResourceConstants.SCOPE_GROUP_TEMPLATE, 0,
				_siteRole.getRoleId(), 0, _GROUP_TEMPLATE_SCOPE_ACTION_IDS,
				true);

			long userId = 0;

			if (_hasUserId) {
				userId = _USER_ID;
			}

			_assertResourcePermission(
				rs, ResourceConstants.SCOPE_INDIVIDUAL, _RESOURCE_PRIMARY_KEY,
				_regularRole.getRoleId(), userId, _INDIVIDUAL_SCOPE_ACTION_IDS,
				false);

			Assert.assertFalse(rs.next());
		}

		DBAssertionUtil.assertColumns(TableClass.TABLE_NAME, "id_", "userId");

		_assertRowsRemoved("ResourceBlock", "resourceBlockId");
		_assertRowsRemoved(
			"ResourceBlockPermission", "resourceBlockPermissionId");
		_assertRowsRemoved(
			"ResourceTypePermission", "resourceTypePermissionId");
	}

	private static final long _COMPANY_ID = -6;

	private static final long _COMPANY_SCOPE_ACTION_IDS = 3;

	private static final long _GROUP_ID = -7;

	private static final long _GROUP_SCOPE_ACTION_IDS = 6;

	private static final long _GROUP_TEMPLATE_SCOPE_ACTION_IDS = 9;

	private static final long _INDIVIDUAL_SCOPE_ACTION_IDS = 12;

	private static final long _RESOURCE_PRIMARY_KEY = -8;

	private static final long _USER_ID = -9;

	private boolean _hasUserId;

	@DeleteAfterTestRun
	private Role _regularRole;

	@DeleteAfterTestRun
	private Role _siteRole;

}