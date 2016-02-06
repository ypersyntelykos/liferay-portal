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

package com.liferay.portlet.messageboards.service.base;

import aQute.bnd.annotation.ProviderType;

import com.liferay.exportimport.kernel.lar.ExportImportHelperUtil;
import com.liferay.exportimport.kernel.lar.ManifestSummary;
import com.liferay.exportimport.kernel.lar.PortletDataContext;
import com.liferay.exportimport.kernel.lar.StagedModelDataHandlerUtil;
import com.liferay.exportimport.kernel.lar.StagedModelType;

import com.liferay.message.boards.kernel.model.MBDiscussion;
import com.liferay.message.boards.kernel.service.MBDiscussionLocalService;
import com.liferay.message.boards.kernel.service.persistence.MBDiscussionPersistence;
import com.liferay.message.boards.kernel.service.persistence.MBMailingListPersistence;
import com.liferay.message.boards.kernel.service.persistence.MBMessageFinder;
import com.liferay.message.boards.kernel.service.persistence.MBMessagePersistence;
import com.liferay.message.boards.kernel.service.persistence.MBThreadFinder;
import com.liferay.message.boards.kernel.service.persistence.MBThreadPersistence;

import com.liferay.portal.kernel.bean.BeanReference;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.SqlUpdate;
import com.liferay.portal.kernel.dao.jdbc.SqlUpdateFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DefaultActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ExportActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Projection;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.module.framework.service.IdentifiableOSGiService;
import com.liferay.portal.kernel.search.Indexable;
import com.liferay.portal.kernel.search.IndexableType;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.model.PersistedModel;
import com.liferay.portal.kernel.service.BaseLocalServiceImpl;
import com.liferay.portal.kernel.service.PersistedModelLocalServiceRegistry;
import com.liferay.portal.kernel.service.persistence.ClassNamePersistence;
import com.liferay.portal.kernel.service.persistence.SubscriptionPersistence;
import com.liferay.portal.kernel.service.persistence.UserFinder;
import com.liferay.portal.kernel.service.persistence.UserPersistence;

import java.io.Serializable;

import java.util.List;

import javax.sql.DataSource;

/**
 * Provides the base implementation for the message boards discussion local service.
 *
 * <p>
 * This implementation exists only as a container for the default service methods generated by ServiceBuilder. All custom service methods should be put in {@link com.liferay.portlet.messageboards.service.impl.MBDiscussionLocalServiceImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see com.liferay.portlet.messageboards.service.impl.MBDiscussionLocalServiceImpl
 * @see com.liferay.message.boards.kernel.service.MBDiscussionLocalServiceUtil
 * @generated
 */
@ProviderType
public abstract class MBDiscussionLocalServiceBaseImpl
	extends BaseLocalServiceImpl implements MBDiscussionLocalService,
		IdentifiableOSGiService {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use {@link com.liferay.message.boards.kernel.service.MBDiscussionLocalServiceUtil} to access the message boards discussion local service.
	 */

	/**
	 * Adds the message boards discussion to the database. Also notifies the appropriate model listeners.
	 *
	 * @param mbDiscussion the message boards discussion
	 * @return the message boards discussion that was added
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public MBDiscussion addMBDiscussion(MBDiscussion mbDiscussion) {
		mbDiscussion.setNew(true);

		return mbDiscussionPersistence.update(mbDiscussion);
	}

	/**
	 * Creates a new message boards discussion with the primary key. Does not add the message boards discussion to the database.
	 *
	 * @param discussionId the primary key for the new message boards discussion
	 * @return the new message boards discussion
	 */
	@Override
	public MBDiscussion createMBDiscussion(long discussionId) {
		return mbDiscussionPersistence.create(discussionId);
	}

	/**
	 * Deletes the message boards discussion with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param discussionId the primary key of the message boards discussion
	 * @return the message boards discussion that was removed
	 * @throws PortalException if a message boards discussion with the primary key could not be found
	 */
	@Indexable(type = IndexableType.DELETE)
	@Override
	public MBDiscussion deleteMBDiscussion(long discussionId)
		throws PortalException {
		return mbDiscussionPersistence.remove(discussionId);
	}

	/**
	 * Deletes the message boards discussion from the database. Also notifies the appropriate model listeners.
	 *
	 * @param mbDiscussion the message boards discussion
	 * @return the message boards discussion that was removed
	 */
	@Indexable(type = IndexableType.DELETE)
	@Override
	public MBDiscussion deleteMBDiscussion(MBDiscussion mbDiscussion) {
		return mbDiscussionPersistence.remove(mbDiscussion);
	}

	@Override
	public DynamicQuery dynamicQuery() {
		Class<?> clazz = getClass();

		return DynamicQueryFactoryUtil.forClass(MBDiscussion.class,
			clazz.getClassLoader());
	}

	/**
	 * Performs a dynamic query on the database and returns the matching rows.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the matching rows
	 */
	@Override
	public <T> List<T> dynamicQuery(DynamicQuery dynamicQuery) {
		return mbDiscussionPersistence.findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.portlet.messageboards.model.impl.MBDiscussionModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @return the range of matching rows
	 */
	@Override
	public <T> List<T> dynamicQuery(DynamicQuery dynamicQuery, int start,
		int end) {
		return mbDiscussionPersistence.findWithDynamicQuery(dynamicQuery,
			start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.portlet.messageboards.model.impl.MBDiscussionModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param dynamicQuery the dynamic query
	 * @param start the lower bound of the range of model instances
	 * @param end the upper bound of the range of model instances (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching rows
	 */
	@Override
	public <T> List<T> dynamicQuery(DynamicQuery dynamicQuery, int start,
		int end, OrderByComparator<T> orderByComparator) {
		return mbDiscussionPersistence.findWithDynamicQuery(dynamicQuery,
			start, end, orderByComparator);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @return the number of rows matching the dynamic query
	 */
	@Override
	public long dynamicQueryCount(DynamicQuery dynamicQuery) {
		return mbDiscussionPersistence.countWithDynamicQuery(dynamicQuery);
	}

	/**
	 * Returns the number of rows matching the dynamic query.
	 *
	 * @param dynamicQuery the dynamic query
	 * @param projection the projection to apply to the query
	 * @return the number of rows matching the dynamic query
	 */
	@Override
	public long dynamicQueryCount(DynamicQuery dynamicQuery,
		Projection projection) {
		return mbDiscussionPersistence.countWithDynamicQuery(dynamicQuery,
			projection);
	}

	@Override
	public MBDiscussion fetchMBDiscussion(long discussionId) {
		return mbDiscussionPersistence.fetchByPrimaryKey(discussionId);
	}

	/**
	 * Returns the message boards discussion matching the UUID and group.
	 *
	 * @param uuid the message boards discussion's UUID
	 * @param groupId the primary key of the group
	 * @return the matching message boards discussion, or <code>null</code> if a matching message boards discussion could not be found
	 */
	@Override
	public MBDiscussion fetchMBDiscussionByUuidAndGroupId(String uuid,
		long groupId) {
		return mbDiscussionPersistence.fetchByUUID_G(uuid, groupId);
	}

	/**
	 * Returns the message boards discussion with the primary key.
	 *
	 * @param discussionId the primary key of the message boards discussion
	 * @return the message boards discussion
	 * @throws PortalException if a message boards discussion with the primary key could not be found
	 */
	@Override
	public MBDiscussion getMBDiscussion(long discussionId)
		throws PortalException {
		return mbDiscussionPersistence.findByPrimaryKey(discussionId);
	}

	@Override
	public ActionableDynamicQuery getActionableDynamicQuery() {
		ActionableDynamicQuery actionableDynamicQuery = new DefaultActionableDynamicQuery();

		actionableDynamicQuery.setBaseLocalService(com.liferay.message.boards.kernel.service.MBDiscussionLocalServiceUtil.getService());
		actionableDynamicQuery.setClassLoader(getClassLoader());
		actionableDynamicQuery.setModelClass(MBDiscussion.class);

		actionableDynamicQuery.setPrimaryKeyPropertyName("discussionId");

		return actionableDynamicQuery;
	}

	@Override
	public IndexableActionableDynamicQuery getIndexableActionableDynamicQuery() {
		IndexableActionableDynamicQuery indexableActionableDynamicQuery = new IndexableActionableDynamicQuery();

		indexableActionableDynamicQuery.setBaseLocalService(com.liferay.message.boards.kernel.service.MBDiscussionLocalServiceUtil.getService());
		indexableActionableDynamicQuery.setClassLoader(getClassLoader());
		indexableActionableDynamicQuery.setModelClass(MBDiscussion.class);

		indexableActionableDynamicQuery.setPrimaryKeyPropertyName(
			"discussionId");

		return indexableActionableDynamicQuery;
	}

	protected void initActionableDynamicQuery(
		ActionableDynamicQuery actionableDynamicQuery) {
		actionableDynamicQuery.setBaseLocalService(com.liferay.message.boards.kernel.service.MBDiscussionLocalServiceUtil.getService());
		actionableDynamicQuery.setClassLoader(getClassLoader());
		actionableDynamicQuery.setModelClass(MBDiscussion.class);

		actionableDynamicQuery.setPrimaryKeyPropertyName("discussionId");
	}

	@Override
	public ExportActionableDynamicQuery getExportActionableDynamicQuery(
		final PortletDataContext portletDataContext) {
		final ExportActionableDynamicQuery exportActionableDynamicQuery = new ExportActionableDynamicQuery() {
				@Override
				public long performCount() throws PortalException {
					ManifestSummary manifestSummary = portletDataContext.getManifestSummary();

					StagedModelType stagedModelType = getStagedModelType();

					long modelAdditionCount = super.performCount();

					manifestSummary.addModelAdditionCount(stagedModelType,
						modelAdditionCount);

					long modelDeletionCount = ExportImportHelperUtil.getModelDeletionCount(portletDataContext,
							stagedModelType);

					manifestSummary.addModelDeletionCount(stagedModelType,
						modelDeletionCount);

					return modelAdditionCount;
				}
			};

		initActionableDynamicQuery(exportActionableDynamicQuery);

		exportActionableDynamicQuery.setAddCriteriaMethod(new ActionableDynamicQuery.AddCriteriaMethod() {
				@Override
				public void addCriteria(DynamicQuery dynamicQuery) {
					portletDataContext.addDateRangeCriteria(dynamicQuery,
						"modifiedDate");

					StagedModelType stagedModelType = exportActionableDynamicQuery.getStagedModelType();

					long referrerClassNameId = stagedModelType.getReferrerClassNameId();

					Property classNameIdProperty = PropertyFactoryUtil.forName(
							"classNameId");

					if ((referrerClassNameId != StagedModelType.REFERRER_CLASS_NAME_ID_ALL) &&
							(referrerClassNameId != StagedModelType.REFERRER_CLASS_NAME_ID_ANY)) {
						dynamicQuery.add(classNameIdProperty.eq(
								stagedModelType.getReferrerClassNameId()));
					}
					else if (referrerClassNameId == StagedModelType.REFERRER_CLASS_NAME_ID_ANY) {
						dynamicQuery.add(classNameIdProperty.isNotNull());
					}
				}
			});

		exportActionableDynamicQuery.setCompanyId(portletDataContext.getCompanyId());

		exportActionableDynamicQuery.setGroupId(portletDataContext.getScopeGroupId());

		exportActionableDynamicQuery.setPerformActionMethod(new ActionableDynamicQuery.PerformActionMethod<MBDiscussion>() {
				@Override
				public void performAction(MBDiscussion mbDiscussion)
					throws PortalException {
					StagedModelDataHandlerUtil.exportStagedModel(portletDataContext,
						mbDiscussion);
				}
			});
		exportActionableDynamicQuery.setStagedModelType(new StagedModelType(
				PortalUtil.getClassNameId(MBDiscussion.class.getName()),
				StagedModelType.REFERRER_CLASS_NAME_ID_ALL));

		return exportActionableDynamicQuery;
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public PersistedModel deletePersistedModel(PersistedModel persistedModel)
		throws PortalException {
		return mbDiscussionLocalService.deleteMBDiscussion((MBDiscussion)persistedModel);
	}

	@Override
	public PersistedModel getPersistedModel(Serializable primaryKeyObj)
		throws PortalException {
		return mbDiscussionPersistence.findByPrimaryKey(primaryKeyObj);
	}

	/**
	 * Returns all the message boards discussions matching the UUID and company.
	 *
	 * @param uuid the UUID of the message boards discussions
	 * @param companyId the primary key of the company
	 * @return the matching message boards discussions, or an empty list if no matches were found
	 */
	@Override
	public List<MBDiscussion> getMBDiscussionsByUuidAndCompanyId(String uuid,
		long companyId) {
		return mbDiscussionPersistence.findByUuid_C(uuid, companyId);
	}

	/**
	 * Returns a range of message boards discussions matching the UUID and company.
	 *
	 * @param uuid the UUID of the message boards discussions
	 * @param companyId the primary key of the company
	 * @param start the lower bound of the range of message boards discussions
	 * @param end the upper bound of the range of message boards discussions (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the range of matching message boards discussions, or an empty list if no matches were found
	 */
	@Override
	public List<MBDiscussion> getMBDiscussionsByUuidAndCompanyId(String uuid,
		long companyId, int start, int end,
		OrderByComparator<MBDiscussion> orderByComparator) {
		return mbDiscussionPersistence.findByUuid_C(uuid, companyId, start,
			end, orderByComparator);
	}

	/**
	 * Returns the message boards discussion matching the UUID and group.
	 *
	 * @param uuid the message boards discussion's UUID
	 * @param groupId the primary key of the group
	 * @return the matching message boards discussion
	 * @throws PortalException if a matching message boards discussion could not be found
	 */
	@Override
	public MBDiscussion getMBDiscussionByUuidAndGroupId(String uuid,
		long groupId) throws PortalException {
		return mbDiscussionPersistence.findByUUID_G(uuid, groupId);
	}

	/**
	 * Returns a range of all the message boards discussions.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.portlet.messageboards.model.impl.MBDiscussionModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param start the lower bound of the range of message boards discussions
	 * @param end the upper bound of the range of message boards discussions (not inclusive)
	 * @return the range of message boards discussions
	 */
	@Override
	public List<MBDiscussion> getMBDiscussions(int start, int end) {
		return mbDiscussionPersistence.findAll(start, end);
	}

	/**
	 * Returns the number of message boards discussions.
	 *
	 * @return the number of message boards discussions
	 */
	@Override
	public int getMBDiscussionsCount() {
		return mbDiscussionPersistence.countAll();
	}

	/**
	 * Updates the message boards discussion in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * @param mbDiscussion the message boards discussion
	 * @return the message boards discussion that was updated
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public MBDiscussion updateMBDiscussion(MBDiscussion mbDiscussion) {
		return mbDiscussionPersistence.update(mbDiscussion);
	}

	/**
	 * Returns the message boards discussion local service.
	 *
	 * @return the message boards discussion local service
	 */
	public MBDiscussionLocalService getMBDiscussionLocalService() {
		return mbDiscussionLocalService;
	}

	/**
	 * Sets the message boards discussion local service.
	 *
	 * @param mbDiscussionLocalService the message boards discussion local service
	 */
	public void setMBDiscussionLocalService(
		MBDiscussionLocalService mbDiscussionLocalService) {
		this.mbDiscussionLocalService = mbDiscussionLocalService;
	}

	/**
	 * Returns the message boards discussion persistence.
	 *
	 * @return the message boards discussion persistence
	 */
	public MBDiscussionPersistence getMBDiscussionPersistence() {
		return mbDiscussionPersistence;
	}

	/**
	 * Sets the message boards discussion persistence.
	 *
	 * @param mbDiscussionPersistence the message boards discussion persistence
	 */
	public void setMBDiscussionPersistence(
		MBDiscussionPersistence mbDiscussionPersistence) {
		this.mbDiscussionPersistence = mbDiscussionPersistence;
	}

	/**
	 * Returns the counter local service.
	 *
	 * @return the counter local service
	 */
	public com.liferay.counter.kernel.service.CounterLocalService getCounterLocalService() {
		return counterLocalService;
	}

	/**
	 * Sets the counter local service.
	 *
	 * @param counterLocalService the counter local service
	 */
	public void setCounterLocalService(
		com.liferay.counter.kernel.service.CounterLocalService counterLocalService) {
		this.counterLocalService = counterLocalService;
	}

	/**
	 * Returns the class name local service.
	 *
	 * @return the class name local service
	 */
	public com.liferay.portal.kernel.service.ClassNameLocalService getClassNameLocalService() {
		return classNameLocalService;
	}

	/**
	 * Sets the class name local service.
	 *
	 * @param classNameLocalService the class name local service
	 */
	public void setClassNameLocalService(
		com.liferay.portal.kernel.service.ClassNameLocalService classNameLocalService) {
		this.classNameLocalService = classNameLocalService;
	}

	/**
	 * Returns the class name persistence.
	 *
	 * @return the class name persistence
	 */
	public ClassNamePersistence getClassNamePersistence() {
		return classNamePersistence;
	}

	/**
	 * Sets the class name persistence.
	 *
	 * @param classNamePersistence the class name persistence
	 */
	public void setClassNamePersistence(
		ClassNamePersistence classNamePersistence) {
		this.classNamePersistence = classNamePersistence;
	}

	/**
	 * Returns the subscription local service.
	 *
	 * @return the subscription local service
	 */
	public com.liferay.portal.kernel.service.SubscriptionLocalService getSubscriptionLocalService() {
		return subscriptionLocalService;
	}

	/**
	 * Sets the subscription local service.
	 *
	 * @param subscriptionLocalService the subscription local service
	 */
	public void setSubscriptionLocalService(
		com.liferay.portal.kernel.service.SubscriptionLocalService subscriptionLocalService) {
		this.subscriptionLocalService = subscriptionLocalService;
	}

	/**
	 * Returns the subscription persistence.
	 *
	 * @return the subscription persistence
	 */
	public SubscriptionPersistence getSubscriptionPersistence() {
		return subscriptionPersistence;
	}

	/**
	 * Sets the subscription persistence.
	 *
	 * @param subscriptionPersistence the subscription persistence
	 */
	public void setSubscriptionPersistence(
		SubscriptionPersistence subscriptionPersistence) {
		this.subscriptionPersistence = subscriptionPersistence;
	}

	/**
	 * Returns the user local service.
	 *
	 * @return the user local service
	 */
	public com.liferay.portal.kernel.service.UserLocalService getUserLocalService() {
		return userLocalService;
	}

	/**
	 * Sets the user local service.
	 *
	 * @param userLocalService the user local service
	 */
	public void setUserLocalService(
		com.liferay.portal.kernel.service.UserLocalService userLocalService) {
		this.userLocalService = userLocalService;
	}

	/**
	 * Returns the user persistence.
	 *
	 * @return the user persistence
	 */
	public UserPersistence getUserPersistence() {
		return userPersistence;
	}

	/**
	 * Sets the user persistence.
	 *
	 * @param userPersistence the user persistence
	 */
	public void setUserPersistence(UserPersistence userPersistence) {
		this.userPersistence = userPersistence;
	}

	/**
	 * Returns the user finder.
	 *
	 * @return the user finder
	 */
	public UserFinder getUserFinder() {
		return userFinder;
	}

	/**
	 * Sets the user finder.
	 *
	 * @param userFinder the user finder
	 */
	public void setUserFinder(UserFinder userFinder) {
		this.userFinder = userFinder;
	}

	/**
	 * Returns the message boards mailing list local service.
	 *
	 * @return the message boards mailing list local service
	 */
	public com.liferay.message.boards.kernel.service.MBMailingListLocalService getMBMailingListLocalService() {
		return mbMailingListLocalService;
	}

	/**
	 * Sets the message boards mailing list local service.
	 *
	 * @param mbMailingListLocalService the message boards mailing list local service
	 */
	public void setMBMailingListLocalService(
		com.liferay.message.boards.kernel.service.MBMailingListLocalService mbMailingListLocalService) {
		this.mbMailingListLocalService = mbMailingListLocalService;
	}

	/**
	 * Returns the message boards mailing list persistence.
	 *
	 * @return the message boards mailing list persistence
	 */
	public MBMailingListPersistence getMBMailingListPersistence() {
		return mbMailingListPersistence;
	}

	/**
	 * Sets the message boards mailing list persistence.
	 *
	 * @param mbMailingListPersistence the message boards mailing list persistence
	 */
	public void setMBMailingListPersistence(
		MBMailingListPersistence mbMailingListPersistence) {
		this.mbMailingListPersistence = mbMailingListPersistence;
	}

	/**
	 * Returns the message-boards message local service.
	 *
	 * @return the message-boards message local service
	 */
	public com.liferay.message.boards.kernel.service.MBMessageLocalService getMBMessageLocalService() {
		return mbMessageLocalService;
	}

	/**
	 * Sets the message-boards message local service.
	 *
	 * @param mbMessageLocalService the message-boards message local service
	 */
	public void setMBMessageLocalService(
		com.liferay.message.boards.kernel.service.MBMessageLocalService mbMessageLocalService) {
		this.mbMessageLocalService = mbMessageLocalService;
	}

	/**
	 * Returns the message-boards message persistence.
	 *
	 * @return the message-boards message persistence
	 */
	public MBMessagePersistence getMBMessagePersistence() {
		return mbMessagePersistence;
	}

	/**
	 * Sets the message-boards message persistence.
	 *
	 * @param mbMessagePersistence the message-boards message persistence
	 */
	public void setMBMessagePersistence(
		MBMessagePersistence mbMessagePersistence) {
		this.mbMessagePersistence = mbMessagePersistence;
	}

	/**
	 * Returns the message-boards message finder.
	 *
	 * @return the message-boards message finder
	 */
	public MBMessageFinder getMBMessageFinder() {
		return mbMessageFinder;
	}

	/**
	 * Sets the message-boards message finder.
	 *
	 * @param mbMessageFinder the message-boards message finder
	 */
	public void setMBMessageFinder(MBMessageFinder mbMessageFinder) {
		this.mbMessageFinder = mbMessageFinder;
	}

	/**
	 * Returns the message boards thread local service.
	 *
	 * @return the message boards thread local service
	 */
	public com.liferay.message.boards.kernel.service.MBThreadLocalService getMBThreadLocalService() {
		return mbThreadLocalService;
	}

	/**
	 * Sets the message boards thread local service.
	 *
	 * @param mbThreadLocalService the message boards thread local service
	 */
	public void setMBThreadLocalService(
		com.liferay.message.boards.kernel.service.MBThreadLocalService mbThreadLocalService) {
		this.mbThreadLocalService = mbThreadLocalService;
	}

	/**
	 * Returns the message boards thread persistence.
	 *
	 * @return the message boards thread persistence
	 */
	public MBThreadPersistence getMBThreadPersistence() {
		return mbThreadPersistence;
	}

	/**
	 * Sets the message boards thread persistence.
	 *
	 * @param mbThreadPersistence the message boards thread persistence
	 */
	public void setMBThreadPersistence(MBThreadPersistence mbThreadPersistence) {
		this.mbThreadPersistence = mbThreadPersistence;
	}

	/**
	 * Returns the message boards thread finder.
	 *
	 * @return the message boards thread finder
	 */
	public MBThreadFinder getMBThreadFinder() {
		return mbThreadFinder;
	}

	/**
	 * Sets the message boards thread finder.
	 *
	 * @param mbThreadFinder the message boards thread finder
	 */
	public void setMBThreadFinder(MBThreadFinder mbThreadFinder) {
		this.mbThreadFinder = mbThreadFinder;
	}

	public void afterPropertiesSet() {
		persistedModelLocalServiceRegistry.register("com.liferay.message.boards.kernel.model.MBDiscussion",
			mbDiscussionLocalService);
	}

	public void destroy() {
		persistedModelLocalServiceRegistry.unregister(
			"com.liferay.message.boards.kernel.model.MBDiscussion");
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return MBDiscussionLocalService.class.getName();
	}

	protected Class<?> getModelClass() {
		return MBDiscussion.class;
	}

	protected String getModelClassName() {
		return MBDiscussion.class.getName();
	}

	/**
	 * Performs a SQL query.
	 *
	 * @param sql the sql query
	 */
	protected void runSQL(String sql) {
		try {
			DataSource dataSource = mbDiscussionPersistence.getDataSource();

			DB db = DBManagerUtil.getDB();

			sql = db.buildSQL(sql);
			sql = PortalUtil.transformSQL(sql);

			SqlUpdate sqlUpdate = SqlUpdateFactoryUtil.getSqlUpdate(dataSource,
					sql, new int[0]);

			sqlUpdate.update();
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
	}

	@BeanReference(type = com.liferay.message.boards.kernel.service.MBDiscussionLocalService.class)
	protected MBDiscussionLocalService mbDiscussionLocalService;
	@BeanReference(type = MBDiscussionPersistence.class)
	protected MBDiscussionPersistence mbDiscussionPersistence;
	@BeanReference(type = com.liferay.counter.kernel.service.CounterLocalService.class)
	protected com.liferay.counter.kernel.service.CounterLocalService counterLocalService;
	@BeanReference(type = com.liferay.portal.kernel.service.ClassNameLocalService.class)
	protected com.liferay.portal.kernel.service.ClassNameLocalService classNameLocalService;
	@BeanReference(type = ClassNamePersistence.class)
	protected ClassNamePersistence classNamePersistence;
	@BeanReference(type = com.liferay.portal.kernel.service.SubscriptionLocalService.class)
	protected com.liferay.portal.kernel.service.SubscriptionLocalService subscriptionLocalService;
	@BeanReference(type = SubscriptionPersistence.class)
	protected SubscriptionPersistence subscriptionPersistence;
	@BeanReference(type = com.liferay.portal.kernel.service.UserLocalService.class)
	protected com.liferay.portal.kernel.service.UserLocalService userLocalService;
	@BeanReference(type = UserPersistence.class)
	protected UserPersistence userPersistence;
	@BeanReference(type = UserFinder.class)
	protected UserFinder userFinder;
	@BeanReference(type = com.liferay.message.boards.kernel.service.MBMailingListLocalService.class)
	protected com.liferay.message.boards.kernel.service.MBMailingListLocalService mbMailingListLocalService;
	@BeanReference(type = MBMailingListPersistence.class)
	protected MBMailingListPersistence mbMailingListPersistence;
	@BeanReference(type = com.liferay.message.boards.kernel.service.MBMessageLocalService.class)
	protected com.liferay.message.boards.kernel.service.MBMessageLocalService mbMessageLocalService;
	@BeanReference(type = MBMessagePersistence.class)
	protected MBMessagePersistence mbMessagePersistence;
	@BeanReference(type = MBMessageFinder.class)
	protected MBMessageFinder mbMessageFinder;
	@BeanReference(type = com.liferay.message.boards.kernel.service.MBThreadLocalService.class)
	protected com.liferay.message.boards.kernel.service.MBThreadLocalService mbThreadLocalService;
	@BeanReference(type = MBThreadPersistence.class)
	protected MBThreadPersistence mbThreadPersistence;
	@BeanReference(type = MBThreadFinder.class)
	protected MBThreadFinder mbThreadFinder;
	@BeanReference(type = PersistedModelLocalServiceRegistry.class)
	protected PersistedModelLocalServiceRegistry persistedModelLocalServiceRegistry;
}