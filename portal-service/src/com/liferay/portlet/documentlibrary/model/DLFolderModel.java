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

package com.liferay.portlet.documentlibrary.model;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.kernel.bean.AutoEscape;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.trash.TrashHandler;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.CacheModel;
import com.liferay.portal.model.ContainerModel;
import com.liferay.portal.model.ShardedModel;
import com.liferay.portal.model.StagedGroupedModel;
import com.liferay.portal.model.TrashedModel;
import com.liferay.portal.model.WorkflowedModel;
import com.liferay.portal.service.ServiceContext;

import com.liferay.portlet.expando.model.ExpandoBridge;

import com.liferay.trash.kernel.model.TrashEntry;

import java.io.Serializable;

import java.util.Date;

/**
 * The base model interface for the DLFolder service. Represents a row in the &quot;DLFolder&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This interface and its corresponding implementation {@link com.liferay.portlet.documentlibrary.model.impl.DLFolderModelImpl} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link com.liferay.portlet.documentlibrary.model.impl.DLFolderImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see DLFolder
 * @see com.liferay.portlet.documentlibrary.model.impl.DLFolderImpl
 * @see com.liferay.portlet.documentlibrary.model.impl.DLFolderModelImpl
 * @generated
 */
@ProviderType
public interface DLFolderModel extends BaseModel<DLFolder>, ContainerModel,
	ShardedModel, StagedGroupedModel, TrashedModel, WorkflowedModel {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. All methods that expect a document library folder model instance should use the {@link DLFolder} interface instead.
	 */

	/**
	 * Returns the primary key of this document library folder.
	 *
	 * @return the primary key of this document library folder
	 */
	public long getPrimaryKey();

	/**
	 * Sets the primary key of this document library folder.
	 *
	 * @param primaryKey the primary key of this document library folder
	 */
	public void setPrimaryKey(long primaryKey);

	/**
	 * Returns the uuid of this document library folder.
	 *
	 * @return the uuid of this document library folder
	 */
	@AutoEscape
	@Override
	public String getUuid();

	/**
	 * Sets the uuid of this document library folder.
	 *
	 * @param uuid the uuid of this document library folder
	 */
	@Override
	public void setUuid(String uuid);

	/**
	 * Returns the folder ID of this document library folder.
	 *
	 * @return the folder ID of this document library folder
	 */
	public long getFolderId();

	/**
	 * Sets the folder ID of this document library folder.
	 *
	 * @param folderId the folder ID of this document library folder
	 */
	public void setFolderId(long folderId);

	/**
	 * Returns the group ID of this document library folder.
	 *
	 * @return the group ID of this document library folder
	 */
	@Override
	public long getGroupId();

	/**
	 * Sets the group ID of this document library folder.
	 *
	 * @param groupId the group ID of this document library folder
	 */
	@Override
	public void setGroupId(long groupId);

	/**
	 * Returns the company ID of this document library folder.
	 *
	 * @return the company ID of this document library folder
	 */
	@Override
	public long getCompanyId();

	/**
	 * Sets the company ID of this document library folder.
	 *
	 * @param companyId the company ID of this document library folder
	 */
	@Override
	public void setCompanyId(long companyId);

	/**
	 * Returns the user ID of this document library folder.
	 *
	 * @return the user ID of this document library folder
	 */
	@Override
	public long getUserId();

	/**
	 * Sets the user ID of this document library folder.
	 *
	 * @param userId the user ID of this document library folder
	 */
	@Override
	public void setUserId(long userId);

	/**
	 * Returns the user uuid of this document library folder.
	 *
	 * @return the user uuid of this document library folder
	 */
	@Override
	public String getUserUuid();

	/**
	 * Sets the user uuid of this document library folder.
	 *
	 * @param userUuid the user uuid of this document library folder
	 */
	@Override
	public void setUserUuid(String userUuid);

	/**
	 * Returns the user name of this document library folder.
	 *
	 * @return the user name of this document library folder
	 */
	@AutoEscape
	@Override
	public String getUserName();

	/**
	 * Sets the user name of this document library folder.
	 *
	 * @param userName the user name of this document library folder
	 */
	@Override
	public void setUserName(String userName);

	/**
	 * Returns the create date of this document library folder.
	 *
	 * @return the create date of this document library folder
	 */
	@Override
	public Date getCreateDate();

	/**
	 * Sets the create date of this document library folder.
	 *
	 * @param createDate the create date of this document library folder
	 */
	@Override
	public void setCreateDate(Date createDate);

	/**
	 * Returns the modified date of this document library folder.
	 *
	 * @return the modified date of this document library folder
	 */
	@Override
	public Date getModifiedDate();

	/**
	 * Sets the modified date of this document library folder.
	 *
	 * @param modifiedDate the modified date of this document library folder
	 */
	@Override
	public void setModifiedDate(Date modifiedDate);

	/**
	 * Returns the repository ID of this document library folder.
	 *
	 * @return the repository ID of this document library folder
	 */
	public long getRepositoryId();

	/**
	 * Sets the repository ID of this document library folder.
	 *
	 * @param repositoryId the repository ID of this document library folder
	 */
	public void setRepositoryId(long repositoryId);

	/**
	 * Returns the mount point of this document library folder.
	 *
	 * @return the mount point of this document library folder
	 */
	public boolean getMountPoint();

	/**
	 * Returns <code>true</code> if this document library folder is mount point.
	 *
	 * @return <code>true</code> if this document library folder is mount point; <code>false</code> otherwise
	 */
	public boolean isMountPoint();

	/**
	 * Sets whether this document library folder is mount point.
	 *
	 * @param mountPoint the mount point of this document library folder
	 */
	public void setMountPoint(boolean mountPoint);

	/**
	 * Returns the parent folder ID of this document library folder.
	 *
	 * @return the parent folder ID of this document library folder
	 */
	public long getParentFolderId();

	/**
	 * Sets the parent folder ID of this document library folder.
	 *
	 * @param parentFolderId the parent folder ID of this document library folder
	 */
	public void setParentFolderId(long parentFolderId);

	/**
	 * Returns the tree path of this document library folder.
	 *
	 * @return the tree path of this document library folder
	 */
	@AutoEscape
	public String getTreePath();

	/**
	 * Sets the tree path of this document library folder.
	 *
	 * @param treePath the tree path of this document library folder
	 */
	public void setTreePath(String treePath);

	/**
	 * Returns the name of this document library folder.
	 *
	 * @return the name of this document library folder
	 */
	@AutoEscape
	public String getName();

	/**
	 * Sets the name of this document library folder.
	 *
	 * @param name the name of this document library folder
	 */
	public void setName(String name);

	/**
	 * Returns the description of this document library folder.
	 *
	 * @return the description of this document library folder
	 */
	@AutoEscape
	public String getDescription();

	/**
	 * Sets the description of this document library folder.
	 *
	 * @param description the description of this document library folder
	 */
	public void setDescription(String description);

	/**
	 * Returns the last post date of this document library folder.
	 *
	 * @return the last post date of this document library folder
	 */
	public Date getLastPostDate();

	/**
	 * Sets the last post date of this document library folder.
	 *
	 * @param lastPostDate the last post date of this document library folder
	 */
	public void setLastPostDate(Date lastPostDate);

	/**
	 * Returns the default file entry type ID of this document library folder.
	 *
	 * @return the default file entry type ID of this document library folder
	 */
	public long getDefaultFileEntryTypeId();

	/**
	 * Sets the default file entry type ID of this document library folder.
	 *
	 * @param defaultFileEntryTypeId the default file entry type ID of this document library folder
	 */
	public void setDefaultFileEntryTypeId(long defaultFileEntryTypeId);

	/**
	 * Returns the hidden of this document library folder.
	 *
	 * @return the hidden of this document library folder
	 */
	public boolean getHidden();

	/**
	 * Returns <code>true</code> if this document library folder is hidden.
	 *
	 * @return <code>true</code> if this document library folder is hidden; <code>false</code> otherwise
	 */
	public boolean isHidden();

	/**
	 * Sets whether this document library folder is hidden.
	 *
	 * @param hidden the hidden of this document library folder
	 */
	public void setHidden(boolean hidden);

	/**
	 * Returns the restriction type of this document library folder.
	 *
	 * @return the restriction type of this document library folder
	 */
	public int getRestrictionType();

	/**
	 * Sets the restriction type of this document library folder.
	 *
	 * @param restrictionType the restriction type of this document library folder
	 */
	public void setRestrictionType(int restrictionType);

	/**
	 * Returns the last publish date of this document library folder.
	 *
	 * @return the last publish date of this document library folder
	 */
	@Override
	public Date getLastPublishDate();

	/**
	 * Sets the last publish date of this document library folder.
	 *
	 * @param lastPublishDate the last publish date of this document library folder
	 */
	@Override
	public void setLastPublishDate(Date lastPublishDate);

	/**
	 * Returns the status of this document library folder.
	 *
	 * @return the status of this document library folder
	 */
	@Override
	public int getStatus();

	/**
	 * Sets the status of this document library folder.
	 *
	 * @param status the status of this document library folder
	 */
	@Override
	public void setStatus(int status);

	/**
	 * Returns the status by user ID of this document library folder.
	 *
	 * @return the status by user ID of this document library folder
	 */
	@Override
	public long getStatusByUserId();

	/**
	 * Sets the status by user ID of this document library folder.
	 *
	 * @param statusByUserId the status by user ID of this document library folder
	 */
	@Override
	public void setStatusByUserId(long statusByUserId);

	/**
	 * Returns the status by user uuid of this document library folder.
	 *
	 * @return the status by user uuid of this document library folder
	 */
	@Override
	public String getStatusByUserUuid();

	/**
	 * Sets the status by user uuid of this document library folder.
	 *
	 * @param statusByUserUuid the status by user uuid of this document library folder
	 */
	@Override
	public void setStatusByUserUuid(String statusByUserUuid);

	/**
	 * Returns the status by user name of this document library folder.
	 *
	 * @return the status by user name of this document library folder
	 */
	@AutoEscape
	@Override
	public String getStatusByUserName();

	/**
	 * Sets the status by user name of this document library folder.
	 *
	 * @param statusByUserName the status by user name of this document library folder
	 */
	@Override
	public void setStatusByUserName(String statusByUserName);

	/**
	 * Returns the status date of this document library folder.
	 *
	 * @return the status date of this document library folder
	 */
	@Override
	public Date getStatusDate();

	/**
	 * Sets the status date of this document library folder.
	 *
	 * @param statusDate the status date of this document library folder
	 */
	@Override
	public void setStatusDate(Date statusDate);

	/**
	 * Returns the trash entry created when this document library folder was moved to the Recycle Bin. The trash entry may belong to one of the ancestors of this document library folder.
	 *
	 * @return the trash entry created when this document library folder was moved to the Recycle Bin
	 */
	@Override
	public TrashEntry getTrashEntry() throws PortalException;

	/**
	 * Returns the class primary key of the trash entry for this document library folder.
	 *
	 * @return the class primary key of the trash entry for this document library folder
	 */
	@Override
	public long getTrashEntryClassPK();

	/**
	 * Returns the trash handler for this document library folder.
	 *
	 * @return the trash handler for this document library folder
	 */
	@Override
	public TrashHandler getTrashHandler();

	/**
	 * Returns <code>true</code> if this document library folder is in the Recycle Bin.
	 *
	 * @return <code>true</code> if this document library folder is in the Recycle Bin; <code>false</code> otherwise
	 */
	@Override
	public boolean isInTrash();

	/**
	 * Returns <code>true</code> if the parent of this document library folder is in the Recycle Bin.
	 *
	 * @return <code>true</code> if the parent of this document library folder is in the Recycle Bin; <code>false</code> otherwise
	 */
	@Override
	public boolean isInTrashContainer();

	@Override
	public boolean isInTrashExplicitly();

	@Override
	public boolean isInTrashImplicitly();

	/**
	 * Returns <code>true</code> if this document library folder is approved.
	 *
	 * @return <code>true</code> if this document library folder is approved; <code>false</code> otherwise
	 */
	@Override
	public boolean isApproved();

	/**
	 * Returns <code>true</code> if this document library folder is denied.
	 *
	 * @return <code>true</code> if this document library folder is denied; <code>false</code> otherwise
	 */
	@Override
	public boolean isDenied();

	/**
	 * Returns <code>true</code> if this document library folder is a draft.
	 *
	 * @return <code>true</code> if this document library folder is a draft; <code>false</code> otherwise
	 */
	@Override
	public boolean isDraft();

	/**
	 * Returns <code>true</code> if this document library folder is expired.
	 *
	 * @return <code>true</code> if this document library folder is expired; <code>false</code> otherwise
	 */
	@Override
	public boolean isExpired();

	/**
	 * Returns <code>true</code> if this document library folder is inactive.
	 *
	 * @return <code>true</code> if this document library folder is inactive; <code>false</code> otherwise
	 */
	@Override
	public boolean isInactive();

	/**
	 * Returns <code>true</code> if this document library folder is incomplete.
	 *
	 * @return <code>true</code> if this document library folder is incomplete; <code>false</code> otherwise
	 */
	@Override
	public boolean isIncomplete();

	/**
	 * Returns <code>true</code> if this document library folder is pending.
	 *
	 * @return <code>true</code> if this document library folder is pending; <code>false</code> otherwise
	 */
	@Override
	public boolean isPending();

	/**
	 * Returns <code>true</code> if this document library folder is scheduled.
	 *
	 * @return <code>true</code> if this document library folder is scheduled; <code>false</code> otherwise
	 */
	@Override
	public boolean isScheduled();

	/**
	 * Returns the container model ID of this document library folder.
	 *
	 * @return the container model ID of this document library folder
	 */
	@Override
	public long getContainerModelId();

	/**
	 * Sets the container model ID of this document library folder.
	 *
	 * @param containerModelId the container model ID of this document library folder
	 */
	@Override
	public void setContainerModelId(long containerModelId);

	/**
	 * Returns the container name of this document library folder.
	 *
	 * @return the container name of this document library folder
	 */
	@Override
	public String getContainerModelName();

	/**
	 * Returns the parent container model ID of this document library folder.
	 *
	 * @return the parent container model ID of this document library folder
	 */
	@Override
	public long getParentContainerModelId();

	/**
	 * Sets the parent container model ID of this document library folder.
	 *
	 * @param parentContainerModelId the parent container model ID of this document library folder
	 */
	@Override
	public void setParentContainerModelId(long parentContainerModelId);

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
		com.liferay.portlet.documentlibrary.model.DLFolder dlFolder);

	@Override
	public int hashCode();

	@Override
	public CacheModel<com.liferay.portlet.documentlibrary.model.DLFolder> toCacheModel();

	@Override
	public com.liferay.portlet.documentlibrary.model.DLFolder toEscapedModel();

	@Override
	public com.liferay.portlet.documentlibrary.model.DLFolder toUnescapedModel();

	@Override
	public String toString();

	@Override
	public String toXmlString();
}