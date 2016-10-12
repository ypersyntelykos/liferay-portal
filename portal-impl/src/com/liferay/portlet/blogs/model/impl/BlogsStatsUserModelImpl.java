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

package com.liferay.portlet.blogs.model.impl;

import aQute.bnd.annotation.ProviderType;

import com.liferay.blogs.kernel.model.BlogsStatsUser;
import com.liferay.blogs.kernel.model.BlogsStatsUserModel;

import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.expando.kernel.util.ExpandoBridgeFactoryUtil;

import com.liferay.portal.kernel.bean.AutoEscapeBeanHandler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.impl.BaseModelImpl;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;

import java.io.Serializable;

import java.sql.Types;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The base model implementation for the BlogsStatsUser service. Represents a row in the &quot;BlogsStatsUser&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This implementation and its corresponding interface {@link BlogsStatsUserModel} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link BlogsStatsUserImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see BlogsStatsUserImpl
 * @see BlogsStatsUser
 * @see BlogsStatsUserModel
 * @deprecated
 * @generated
 */
@Deprecated
@ProviderType
public class BlogsStatsUserModelImpl extends BaseModelImpl<BlogsStatsUser>
	implements BlogsStatsUserModel {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. All methods that expect a blogs stats user model instance should use the {@link BlogsStatsUser} interface instead.
	 */
	public static final String TABLE_NAME = "BlogsStatsUser";
	public static final Object[][] TABLE_COLUMNS = {
			{ "statsUserId", Types.BIGINT },
			{ "groupId", Types.BIGINT },
			{ "companyId", Types.BIGINT },
			{ "userId", Types.BIGINT },
			{ "entryCount", Types.INTEGER },
			{ "lastPostDate", Types.TIMESTAMP },
			{ "ratingsTotalEntries", Types.INTEGER },
			{ "ratingsTotalScore", Types.DOUBLE },
			{ "ratingsAverageScore", Types.DOUBLE }
		};
	public static final Map<String, Integer> TABLE_COLUMNS_MAP = new HashMap<String, Integer>();

	static {
		TABLE_COLUMNS_MAP.put("statsUserId", Types.BIGINT);
		TABLE_COLUMNS_MAP.put("groupId", Types.BIGINT);
		TABLE_COLUMNS_MAP.put("companyId", Types.BIGINT);
		TABLE_COLUMNS_MAP.put("userId", Types.BIGINT);
		TABLE_COLUMNS_MAP.put("entryCount", Types.INTEGER);
		TABLE_COLUMNS_MAP.put("lastPostDate", Types.TIMESTAMP);
		TABLE_COLUMNS_MAP.put("ratingsTotalEntries", Types.INTEGER);
		TABLE_COLUMNS_MAP.put("ratingsTotalScore", Types.DOUBLE);
		TABLE_COLUMNS_MAP.put("ratingsAverageScore", Types.DOUBLE);
	}

	public static final String TABLE_SQL_CREATE = "create table BlogsStatsUser (statsUserId LONG not null primary key,groupId LONG,companyId LONG,userId LONG,entryCount INTEGER,lastPostDate DATE null,ratingsTotalEntries INTEGER,ratingsTotalScore DOUBLE,ratingsAverageScore DOUBLE)";
	public static final String TABLE_SQL_DROP = "drop table BlogsStatsUser";
	public static final String ORDER_BY_JPQL = " ORDER BY blogsStatsUser.entryCount DESC";
	public static final String ORDER_BY_SQL = " ORDER BY BlogsStatsUser.entryCount DESC";
	public static final String DATA_SOURCE = "liferayDataSource";
	public static final String SESSION_FACTORY = "liferaySessionFactory";
	public static final String TX_MANAGER = "liferayTransactionManager";
	public static final boolean ENTITY_CACHE_ENABLED = GetterUtil.getBoolean(com.liferay.portal.util.PropsUtil.get(
				"value.object.entity.cache.enabled.com.liferay.blogs.kernel.model.BlogsStatsUser"),
			true);
	public static final boolean FINDER_CACHE_ENABLED = GetterUtil.getBoolean(com.liferay.portal.util.PropsUtil.get(
				"value.object.finder.cache.enabled.com.liferay.blogs.kernel.model.BlogsStatsUser"),
			true);
	public static final boolean COLUMN_BITMASK_ENABLED = GetterUtil.getBoolean(com.liferay.portal.util.PropsUtil.get(
				"value.object.column.bitmask.enabled.com.liferay.blogs.kernel.model.BlogsStatsUser"),
			true);
	public static final long COMPANYID_COLUMN_BITMASK = 1L;
	public static final long ENTRYCOUNT_COLUMN_BITMASK = 2L;
	public static final long GROUPID_COLUMN_BITMASK = 4L;
	public static final long LASTPOSTDATE_COLUMN_BITMASK = 8L;
	public static final long USERID_COLUMN_BITMASK = 16L;
	public static final long LOCK_EXPIRATION_TIME = GetterUtil.getLong(com.liferay.portal.util.PropsUtil.get(
				"lock.expiration.time.com.liferay.blogs.kernel.model.BlogsStatsUser"));

	public BlogsStatsUserModelImpl() {
	}

	@Override
	public long getPrimaryKey() {
		return _statsUserId;
	}

	@Override
	public void setPrimaryKey(long primaryKey) {
		setStatsUserId(primaryKey);
	}

	@Override
	public Serializable getPrimaryKeyObj() {
		return _statsUserId;
	}

	@Override
	public void setPrimaryKeyObj(Serializable primaryKeyObj) {
		setPrimaryKey(((Long)primaryKeyObj).longValue());
	}

	@Override
	public Class<?> getModelClass() {
		return BlogsStatsUser.class;
	}

	@Override
	public String getModelClassName() {
		return BlogsStatsUser.class.getName();
	}

	@Override
	public Map<String, Object> getModelAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();

		attributes.put("statsUserId", getStatsUserId());
		attributes.put("groupId", getGroupId());
		attributes.put("companyId", getCompanyId());
		attributes.put("userId", getUserId());
		attributes.put("entryCount", getEntryCount());
		attributes.put("lastPostDate", getLastPostDate());
		attributes.put("ratingsTotalEntries", getRatingsTotalEntries());
		attributes.put("ratingsTotalScore", getRatingsTotalScore());
		attributes.put("ratingsAverageScore", getRatingsAverageScore());

		attributes.put("entityCacheEnabled", isEntityCacheEnabled());
		attributes.put("finderCacheEnabled", isFinderCacheEnabled());

		return attributes;
	}

	@Override
	public void setModelAttributes(Map<String, Object> attributes) {
		Long statsUserId = (Long)attributes.get("statsUserId");

		if (statsUserId != null) {
			setStatsUserId(statsUserId);
		}

		Long groupId = (Long)attributes.get("groupId");

		if (groupId != null) {
			setGroupId(groupId);
		}

		Long companyId = (Long)attributes.get("companyId");

		if (companyId != null) {
			setCompanyId(companyId);
		}

		Long userId = (Long)attributes.get("userId");

		if (userId != null) {
			setUserId(userId);
		}

		Integer entryCount = (Integer)attributes.get("entryCount");

		if (entryCount != null) {
			setEntryCount(entryCount);
		}

		Date lastPostDate = (Date)attributes.get("lastPostDate");

		if (lastPostDate != null) {
			setLastPostDate(lastPostDate);
		}

		Integer ratingsTotalEntries = (Integer)attributes.get(
				"ratingsTotalEntries");

		if (ratingsTotalEntries != null) {
			setRatingsTotalEntries(ratingsTotalEntries);
		}

		Double ratingsTotalScore = (Double)attributes.get("ratingsTotalScore");

		if (ratingsTotalScore != null) {
			setRatingsTotalScore(ratingsTotalScore);
		}

		Double ratingsAverageScore = (Double)attributes.get(
				"ratingsAverageScore");

		if (ratingsAverageScore != null) {
			setRatingsAverageScore(ratingsAverageScore);
		}
	}

	@Override
	public long getStatsUserId() {
		return _statsUserId;
	}

	@Override
	public void setStatsUserId(long statsUserId) {
		_statsUserId = statsUserId;
	}

	@Override
	public String getStatsUserUuid() {
		try {
			User user = UserLocalServiceUtil.getUserById(getStatsUserId());

			return user.getUuid();
		}
		catch (PortalException pe) {
			return StringPool.BLANK;
		}
	}

	@Override
	public void setStatsUserUuid(String statsUserUuid) {
	}

	@Override
	public long getGroupId() {
		return _groupId;
	}

	@Override
	public void setGroupId(long groupId) {
		_columnBitmask |= GROUPID_COLUMN_BITMASK;

		if (!_setOriginalGroupId) {
			_setOriginalGroupId = true;

			_originalGroupId = _groupId;
		}

		_groupId = groupId;
	}

	public long getOriginalGroupId() {
		return _originalGroupId;
	}

	@Override
	public long getCompanyId() {
		return _companyId;
	}

	@Override
	public void setCompanyId(long companyId) {
		_columnBitmask |= COMPANYID_COLUMN_BITMASK;

		if (!_setOriginalCompanyId) {
			_setOriginalCompanyId = true;

			_originalCompanyId = _companyId;
		}

		_companyId = companyId;
	}

	public long getOriginalCompanyId() {
		return _originalCompanyId;
	}

	@Override
	public long getUserId() {
		return _userId;
	}

	@Override
	public void setUserId(long userId) {
		_columnBitmask |= USERID_COLUMN_BITMASK;

		if (!_setOriginalUserId) {
			_setOriginalUserId = true;

			_originalUserId = _userId;
		}

		_userId = userId;
	}

	@Override
	public String getUserUuid() {
		try {
			User user = UserLocalServiceUtil.getUserById(getUserId());

			return user.getUuid();
		}
		catch (PortalException pe) {
			return StringPool.BLANK;
		}
	}

	@Override
	public void setUserUuid(String userUuid) {
	}

	public long getOriginalUserId() {
		return _originalUserId;
	}

	@Override
	public int getEntryCount() {
		return _entryCount;
	}

	@Override
	public void setEntryCount(int entryCount) {
		_columnBitmask = -1L;

		if (!_setOriginalEntryCount) {
			_setOriginalEntryCount = true;

			_originalEntryCount = _entryCount;
		}

		_entryCount = entryCount;
	}

	public int getOriginalEntryCount() {
		return _originalEntryCount;
	}

	@Override
	public Date getLastPostDate() {
		return _lastPostDate;
	}

	@Override
	public void setLastPostDate(Date lastPostDate) {
		_columnBitmask |= LASTPOSTDATE_COLUMN_BITMASK;

		if (_originalLastPostDate == null) {
			_originalLastPostDate = _lastPostDate;
		}

		_lastPostDate = lastPostDate;
	}

	public Date getOriginalLastPostDate() {
		return _originalLastPostDate;
	}

	@Override
	public int getRatingsTotalEntries() {
		return _ratingsTotalEntries;
	}

	@Override
	public void setRatingsTotalEntries(int ratingsTotalEntries) {
		_ratingsTotalEntries = ratingsTotalEntries;
	}

	@Override
	public double getRatingsTotalScore() {
		return _ratingsTotalScore;
	}

	@Override
	public void setRatingsTotalScore(double ratingsTotalScore) {
		_ratingsTotalScore = ratingsTotalScore;
	}

	@Override
	public double getRatingsAverageScore() {
		return _ratingsAverageScore;
	}

	@Override
	public void setRatingsAverageScore(double ratingsAverageScore) {
		_ratingsAverageScore = ratingsAverageScore;
	}

	public long getColumnBitmask() {
		return _columnBitmask;
	}

	@Override
	public ExpandoBridge getExpandoBridge() {
		return ExpandoBridgeFactoryUtil.getExpandoBridge(getCompanyId(),
			BlogsStatsUser.class.getName(), getPrimaryKey());
	}

	@Override
	public void setExpandoBridgeAttributes(ServiceContext serviceContext) {
		ExpandoBridge expandoBridge = getExpandoBridge();

		expandoBridge.setAttributes(serviceContext);
	}

	@Override
	public BlogsStatsUser toEscapedModel() {
		if (_escapedModel == null) {
			_escapedModel = (BlogsStatsUser)ProxyUtil.newProxyInstance(_classLoader,
					_escapedModelInterfaces, new AutoEscapeBeanHandler(this));
		}

		return _escapedModel;
	}

	@Override
	public Object clone() {
		BlogsStatsUserImpl blogsStatsUserImpl = new BlogsStatsUserImpl();

		blogsStatsUserImpl.setStatsUserId(getStatsUserId());
		blogsStatsUserImpl.setGroupId(getGroupId());
		blogsStatsUserImpl.setCompanyId(getCompanyId());
		blogsStatsUserImpl.setUserId(getUserId());
		blogsStatsUserImpl.setEntryCount(getEntryCount());
		blogsStatsUserImpl.setLastPostDate(getLastPostDate());
		blogsStatsUserImpl.setRatingsTotalEntries(getRatingsTotalEntries());
		blogsStatsUserImpl.setRatingsTotalScore(getRatingsTotalScore());
		blogsStatsUserImpl.setRatingsAverageScore(getRatingsAverageScore());

		blogsStatsUserImpl.resetOriginalValues();

		return blogsStatsUserImpl;
	}

	@Override
	public int compareTo(BlogsStatsUser blogsStatsUser) {
		int value = 0;

		if (getEntryCount() < blogsStatsUser.getEntryCount()) {
			value = -1;
		}
		else if (getEntryCount() > blogsStatsUser.getEntryCount()) {
			value = 1;
		}
		else {
			value = 0;
		}

		value = value * -1;

		if (value != 0) {
			return value;
		}

		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof BlogsStatsUser)) {
			return false;
		}

		BlogsStatsUser blogsStatsUser = (BlogsStatsUser)obj;

		long primaryKey = blogsStatsUser.getPrimaryKey();

		if (getPrimaryKey() == primaryKey) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int)getPrimaryKey();
	}

	@Override
	public boolean isEntityCacheEnabled() {
		return ENTITY_CACHE_ENABLED;
	}

	@Override
	public boolean isFinderCacheEnabled() {
		return FINDER_CACHE_ENABLED;
	}

	@Override
	public void resetOriginalValues() {
		BlogsStatsUserModelImpl blogsStatsUserModelImpl = this;

		blogsStatsUserModelImpl._originalGroupId = blogsStatsUserModelImpl._groupId;

		blogsStatsUserModelImpl._setOriginalGroupId = false;

		blogsStatsUserModelImpl._originalCompanyId = blogsStatsUserModelImpl._companyId;

		blogsStatsUserModelImpl._setOriginalCompanyId = false;

		blogsStatsUserModelImpl._originalUserId = blogsStatsUserModelImpl._userId;

		blogsStatsUserModelImpl._setOriginalUserId = false;

		blogsStatsUserModelImpl._originalEntryCount = blogsStatsUserModelImpl._entryCount;

		blogsStatsUserModelImpl._setOriginalEntryCount = false;

		blogsStatsUserModelImpl._originalLastPostDate = blogsStatsUserModelImpl._lastPostDate;

		blogsStatsUserModelImpl._columnBitmask = 0;
	}

	@Override
	public CacheModel<BlogsStatsUser> toCacheModel() {
		BlogsStatsUserCacheModel blogsStatsUserCacheModel = new BlogsStatsUserCacheModel();

		blogsStatsUserCacheModel.statsUserId = getStatsUserId();

		blogsStatsUserCacheModel.groupId = getGroupId();

		blogsStatsUserCacheModel.companyId = getCompanyId();

		blogsStatsUserCacheModel.userId = getUserId();

		blogsStatsUserCacheModel.entryCount = getEntryCount();

		Date lastPostDate = getLastPostDate();

		if (lastPostDate != null) {
			blogsStatsUserCacheModel.lastPostDate = lastPostDate.getTime();
		}
		else {
			blogsStatsUserCacheModel.lastPostDate = Long.MIN_VALUE;
		}

		blogsStatsUserCacheModel.ratingsTotalEntries = getRatingsTotalEntries();

		blogsStatsUserCacheModel.ratingsTotalScore = getRatingsTotalScore();

		blogsStatsUserCacheModel.ratingsAverageScore = getRatingsAverageScore();

		return blogsStatsUserCacheModel;
	}

	@Override
	public String toString() {
		StringBundler sb = new StringBundler(19);

		sb.append("{statsUserId=");
		sb.append(getStatsUserId());
		sb.append(", groupId=");
		sb.append(getGroupId());
		sb.append(", companyId=");
		sb.append(getCompanyId());
		sb.append(", userId=");
		sb.append(getUserId());
		sb.append(", entryCount=");
		sb.append(getEntryCount());
		sb.append(", lastPostDate=");
		sb.append(getLastPostDate());
		sb.append(", ratingsTotalEntries=");
		sb.append(getRatingsTotalEntries());
		sb.append(", ratingsTotalScore=");
		sb.append(getRatingsTotalScore());
		sb.append(", ratingsAverageScore=");
		sb.append(getRatingsAverageScore());
		sb.append("}");

		return sb.toString();
	}

	@Override
	public String toXmlString() {
		StringBundler sb = new StringBundler(31);

		sb.append("<model><model-name>");
		sb.append("com.liferay.blogs.kernel.model.BlogsStatsUser");
		sb.append("</model-name>");

		sb.append(
			"<column><column-name>statsUserId</column-name><column-value><![CDATA[");
		sb.append(getStatsUserId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>groupId</column-name><column-value><![CDATA[");
		sb.append(getGroupId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>companyId</column-name><column-value><![CDATA[");
		sb.append(getCompanyId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>userId</column-name><column-value><![CDATA[");
		sb.append(getUserId());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>entryCount</column-name><column-value><![CDATA[");
		sb.append(getEntryCount());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>lastPostDate</column-name><column-value><![CDATA[");
		sb.append(getLastPostDate());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>ratingsTotalEntries</column-name><column-value><![CDATA[");
		sb.append(getRatingsTotalEntries());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>ratingsTotalScore</column-name><column-value><![CDATA[");
		sb.append(getRatingsTotalScore());
		sb.append("]]></column-value></column>");
		sb.append(
			"<column><column-name>ratingsAverageScore</column-name><column-value><![CDATA[");
		sb.append(getRatingsAverageScore());
		sb.append("]]></column-value></column>");

		sb.append("</model>");

		return sb.toString();
	}

	private static final ClassLoader _classLoader = BlogsStatsUser.class.getClassLoader();
	private static final Class<?>[] _escapedModelInterfaces = new Class[] {
			BlogsStatsUser.class
		};
	private long _statsUserId;
	private long _groupId;
	private long _originalGroupId;
	private boolean _setOriginalGroupId;
	private long _companyId;
	private long _originalCompanyId;
	private boolean _setOriginalCompanyId;
	private long _userId;
	private long _originalUserId;
	private boolean _setOriginalUserId;
	private int _entryCount;
	private int _originalEntryCount;
	private boolean _setOriginalEntryCount;
	private Date _lastPostDate;
	private Date _originalLastPostDate;
	private int _ratingsTotalEntries;
	private double _ratingsTotalScore;
	private double _ratingsAverageScore;
	private long _columnBitmask;
	private BlogsStatsUser _escapedModel;
}