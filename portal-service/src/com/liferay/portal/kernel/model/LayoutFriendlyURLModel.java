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

package com.liferay.portal.kernel.model;

import aQute.bnd.annotation.ProviderType;

import com.liferay.expando.kernel.model.ExpandoBridge;

import com.liferay.portal.kernel.bean.AutoEscape;
import com.liferay.portal.kernel.service.ServiceContext;

import java.io.Serializable;

import java.util.Date;

/**
 * The base model interface for the LayoutFriendlyURL service. Represents a row in the &quot;LayoutFriendlyURL&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This interface and its corresponding implementation {@link com.liferay.portal.model.impl.LayoutFriendlyURLModelImpl} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link com.liferay.portal.model.impl.LayoutFriendlyURLImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see LayoutFriendlyURL
 * @see com.liferay.portal.model.impl.LayoutFriendlyURLImpl
 * @see com.liferay.portal.model.impl.LayoutFriendlyURLModelImpl
 * @generated
 */
@ProviderType
public interface LayoutFriendlyURLModel extends BaseModel<LayoutFriendlyURL>,
	MVCCModel, ShardedModel, StagedGroupedModel {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. All methods that expect a layout friendly u r l model instance should use the {@link LayoutFriendlyURL} interface instead.
	 */

	/**
	 * Returns the primary key of this layout friendly u r l.
	 *
	 * @return the primary key of this layout friendly u r l
	 */
	public long getPrimaryKey();

	/**
	 * Sets the primary key of this layout friendly u r l.
	 *
	 * @param primaryKey the primary key of this layout friendly u r l
	 */
	public void setPrimaryKey(long primaryKey);

	/**
	 * Returns the mvcc version of this layout friendly u r l.
	 *
	 * @return the mvcc version of this layout friendly u r l
	 */
	@Override
	public long getMvccVersion();

	/**
	 * Sets the mvcc version of this layout friendly u r l.
	 *
	 * @param mvccVersion the mvcc version of this layout friendly u r l
	 */
	@Override
	public void setMvccVersion(long mvccVersion);

	/**
	 * Returns the uuid of this layout friendly u r l.
	 *
	 * @return the uuid of this layout friendly u r l
	 */
	@AutoEscape
	@Override
	public String getUuid();

	/**
	 * Sets the uuid of this layout friendly u r l.
	 *
	 * @param uuid the uuid of this layout friendly u r l
	 */
	@Override
	public void setUuid(String uuid);

	/**
	 * Returns the layout friendly u r l ID of this layout friendly u r l.
	 *
	 * @return the layout friendly u r l ID of this layout friendly u r l
	 */
	public long getLayoutFriendlyURLId();

	/**
	 * Sets the layout friendly u r l ID of this layout friendly u r l.
	 *
	 * @param layoutFriendlyURLId the layout friendly u r l ID of this layout friendly u r l
	 */
	public void setLayoutFriendlyURLId(long layoutFriendlyURLId);

	/**
	 * Returns the group ID of this layout friendly u r l.
	 *
	 * @return the group ID of this layout friendly u r l
	 */
	@Override
	public long getGroupId();

	/**
	 * Sets the group ID of this layout friendly u r l.
	 *
	 * @param groupId the group ID of this layout friendly u r l
	 */
	@Override
	public void setGroupId(long groupId);

	/**
	 * Returns the company ID of this layout friendly u r l.
	 *
	 * @return the company ID of this layout friendly u r l
	 */
	@Override
	public long getCompanyId();

	/**
	 * Sets the company ID of this layout friendly u r l.
	 *
	 * @param companyId the company ID of this layout friendly u r l
	 */
	@Override
	public void setCompanyId(long companyId);

	/**
	 * Returns the user ID of this layout friendly u r l.
	 *
	 * @return the user ID of this layout friendly u r l
	 */
	@Override
	public long getUserId();

	/**
	 * Sets the user ID of this layout friendly u r l.
	 *
	 * @param userId the user ID of this layout friendly u r l
	 */
	@Override
	public void setUserId(long userId);

	/**
	 * Returns the user uuid of this layout friendly u r l.
	 *
	 * @return the user uuid of this layout friendly u r l
	 */
	@Override
	public String getUserUuid();

	/**
	 * Sets the user uuid of this layout friendly u r l.
	 *
	 * @param userUuid the user uuid of this layout friendly u r l
	 */
	@Override
	public void setUserUuid(String userUuid);

	/**
	 * Returns the user name of this layout friendly u r l.
	 *
	 * @return the user name of this layout friendly u r l
	 */
	@AutoEscape
	@Override
	public String getUserName();

	/**
	 * Sets the user name of this layout friendly u r l.
	 *
	 * @param userName the user name of this layout friendly u r l
	 */
	@Override
	public void setUserName(String userName);

	/**
	 * Returns the create date of this layout friendly u r l.
	 *
	 * @return the create date of this layout friendly u r l
	 */
	@Override
	public Date getCreateDate();

	/**
	 * Sets the create date of this layout friendly u r l.
	 *
	 * @param createDate the create date of this layout friendly u r l
	 */
	@Override
	public void setCreateDate(Date createDate);

	/**
	 * Returns the modified date of this layout friendly u r l.
	 *
	 * @return the modified date of this layout friendly u r l
	 */
	@Override
	public Date getModifiedDate();

	/**
	 * Sets the modified date of this layout friendly u r l.
	 *
	 * @param modifiedDate the modified date of this layout friendly u r l
	 */
	@Override
	public void setModifiedDate(Date modifiedDate);

	/**
	 * Returns the plid of this layout friendly u r l.
	 *
	 * @return the plid of this layout friendly u r l
	 */
	public long getPlid();

	/**
	 * Sets the plid of this layout friendly u r l.
	 *
	 * @param plid the plid of this layout friendly u r l
	 */
	public void setPlid(long plid);

	/**
	 * Returns the private layout of this layout friendly u r l.
	 *
	 * @return the private layout of this layout friendly u r l
	 */
	public boolean getPrivateLayout();

	/**
	 * Returns <code>true</code> if this layout friendly u r l is private layout.
	 *
	 * @return <code>true</code> if this layout friendly u r l is private layout; <code>false</code> otherwise
	 */
	public boolean isPrivateLayout();

	/**
	 * Sets whether this layout friendly u r l is private layout.
	 *
	 * @param privateLayout the private layout of this layout friendly u r l
	 */
	public void setPrivateLayout(boolean privateLayout);

	/**
	 * Returns the friendly u r l of this layout friendly u r l.
	 *
	 * @return the friendly u r l of this layout friendly u r l
	 */
	@AutoEscape
	public String getFriendlyURL();

	/**
	 * Sets the friendly u r l of this layout friendly u r l.
	 *
	 * @param friendlyURL the friendly u r l of this layout friendly u r l
	 */
	public void setFriendlyURL(String friendlyURL);

	/**
	 * Returns the language ID of this layout friendly u r l.
	 *
	 * @return the language ID of this layout friendly u r l
	 */
	@AutoEscape
	public String getLanguageId();

	/**
	 * Sets the language ID of this layout friendly u r l.
	 *
	 * @param languageId the language ID of this layout friendly u r l
	 */
	public void setLanguageId(String languageId);

	/**
	 * Returns the last publish date of this layout friendly u r l.
	 *
	 * @return the last publish date of this layout friendly u r l
	 */
	@Override
	public Date getLastPublishDate();

	/**
	 * Sets the last publish date of this layout friendly u r l.
	 *
	 * @param lastPublishDate the last publish date of this layout friendly u r l
	 */
	@Override
	public void setLastPublishDate(Date lastPublishDate);

	@Override
	public boolean isNew();

	@Override
	public void setNew(boolean n);

	@Override
	public boolean isCachedModel();

	@Override
	public void setCachedModel(boolean cachedModel);

	@Override
	public boolean isEscapedModel();

	@Override
	public Serializable getPrimaryKeyObj();

	@Override
	public void setPrimaryKeyObj(Serializable primaryKeyObj);

	@Override
	public ExpandoBridge getExpandoBridge();

	@Override
	public void setExpandoBridgeAttributes(BaseModel<?> baseModel);

	@Override
	public void setExpandoBridgeAttributes(ExpandoBridge expandoBridge);

	@Override
	public void setExpandoBridgeAttributes(ServiceContext serviceContext);

	@Override
	public Object clone();

	@Override
	public int compareTo(
		com.liferay.portal.kernel.model.LayoutFriendlyURL layoutFriendlyURL);

	@Override
	public int hashCode();

	@Override
	public CacheModel<com.liferay.portal.kernel.model.LayoutFriendlyURL> toCacheModel();

	@Override
	public com.liferay.portal.kernel.model.LayoutFriendlyURL toEscapedModel();

	@Override
	public com.liferay.portal.kernel.model.LayoutFriendlyURL toUnescapedModel();

	@Override
	public String toString();

	@Override
	public String toXmlString();
}