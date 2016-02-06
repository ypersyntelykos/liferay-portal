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

package com.liferay.dynamic.data.mapping.service.base;

import aQute.bnd.annotation.ProviderType;

import com.liferay.dynamic.data.mapping.model.DDMTemplateLink;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLinkLocalService;
import com.liferay.dynamic.data.mapping.service.persistence.DDMTemplateLinkPersistence;

import com.liferay.portal.kernel.bean.BeanReference;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.jdbc.SqlUpdate;
import com.liferay.portal.kernel.dao.jdbc.SqlUpdateFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DefaultActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Projection;
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
import com.liferay.portal.spring.extender.service.ServiceReference;

import java.io.Serializable;

import java.util.List;

import javax.sql.DataSource;

/**
 * Provides the base implementation for the d d m template link local service.
 *
 * <p>
 * This implementation exists only as a container for the default service methods generated by ServiceBuilder. All custom service methods should be put in {@link com.liferay.dynamic.data.mapping.service.impl.DDMTemplateLinkLocalServiceImpl}.
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see com.liferay.dynamic.data.mapping.service.impl.DDMTemplateLinkLocalServiceImpl
 * @see com.liferay.dynamic.data.mapping.service.DDMTemplateLinkLocalServiceUtil
 * @generated
 */
@ProviderType
public abstract class DDMTemplateLinkLocalServiceBaseImpl
	extends BaseLocalServiceImpl implements DDMTemplateLinkLocalService,
		IdentifiableOSGiService {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use {@link com.liferay.dynamic.data.mapping.service.DDMTemplateLinkLocalServiceUtil} to access the d d m template link local service.
	 */

	/**
	 * Adds the d d m template link to the database. Also notifies the appropriate model listeners.
	 *
	 * @param ddmTemplateLink the d d m template link
	 * @return the d d m template link that was added
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public DDMTemplateLink addDDMTemplateLink(DDMTemplateLink ddmTemplateLink) {
		ddmTemplateLink.setNew(true);

		return ddmTemplateLinkPersistence.update(ddmTemplateLink);
	}

	/**
	 * Creates a new d d m template link with the primary key. Does not add the d d m template link to the database.
	 *
	 * @param templateLinkId the primary key for the new d d m template link
	 * @return the new d d m template link
	 */
	@Override
	public DDMTemplateLink createDDMTemplateLink(long templateLinkId) {
		return ddmTemplateLinkPersistence.create(templateLinkId);
	}

	/**
	 * Deletes the d d m template link with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param templateLinkId the primary key of the d d m template link
	 * @return the d d m template link that was removed
	 * @throws PortalException if a d d m template link with the primary key could not be found
	 */
	@Indexable(type = IndexableType.DELETE)
	@Override
	public DDMTemplateLink deleteDDMTemplateLink(long templateLinkId)
		throws PortalException {
		return ddmTemplateLinkPersistence.remove(templateLinkId);
	}

	/**
	 * Deletes the d d m template link from the database. Also notifies the appropriate model listeners.
	 *
	 * @param ddmTemplateLink the d d m template link
	 * @return the d d m template link that was removed
	 */
	@Indexable(type = IndexableType.DELETE)
	@Override
	public DDMTemplateLink deleteDDMTemplateLink(
		DDMTemplateLink ddmTemplateLink) {
		return ddmTemplateLinkPersistence.remove(ddmTemplateLink);
	}

	@Override
	public DynamicQuery dynamicQuery() {
		Class<?> clazz = getClass();

		return DynamicQueryFactoryUtil.forClass(DDMTemplateLink.class,
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
		return ddmTemplateLinkPersistence.findWithDynamicQuery(dynamicQuery);
	}

	/**
	 * Performs a dynamic query on the database and returns a range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.dynamic.data.mapping.model.impl.DDMTemplateLinkModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
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
		return ddmTemplateLinkPersistence.findWithDynamicQuery(dynamicQuery,
			start, end);
	}

	/**
	 * Performs a dynamic query on the database and returns an ordered range of the matching rows.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.dynamic.data.mapping.model.impl.DDMTemplateLinkModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
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
		return ddmTemplateLinkPersistence.findWithDynamicQuery(dynamicQuery,
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
		return ddmTemplateLinkPersistence.countWithDynamicQuery(dynamicQuery);
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
		return ddmTemplateLinkPersistence.countWithDynamicQuery(dynamicQuery,
			projection);
	}

	@Override
	public DDMTemplateLink fetchDDMTemplateLink(long templateLinkId) {
		return ddmTemplateLinkPersistence.fetchByPrimaryKey(templateLinkId);
	}

	/**
	 * Returns the d d m template link with the primary key.
	 *
	 * @param templateLinkId the primary key of the d d m template link
	 * @return the d d m template link
	 * @throws PortalException if a d d m template link with the primary key could not be found
	 */
	@Override
	public DDMTemplateLink getDDMTemplateLink(long templateLinkId)
		throws PortalException {
		return ddmTemplateLinkPersistence.findByPrimaryKey(templateLinkId);
	}

	@Override
	public ActionableDynamicQuery getActionableDynamicQuery() {
		ActionableDynamicQuery actionableDynamicQuery = new DefaultActionableDynamicQuery();

		actionableDynamicQuery.setBaseLocalService(com.liferay.dynamic.data.mapping.service.DDMTemplateLinkLocalServiceUtil.getService());
		actionableDynamicQuery.setClassLoader(getClassLoader());
		actionableDynamicQuery.setModelClass(DDMTemplateLink.class);

		actionableDynamicQuery.setPrimaryKeyPropertyName("templateLinkId");

		return actionableDynamicQuery;
	}

	@Override
	public IndexableActionableDynamicQuery getIndexableActionableDynamicQuery() {
		IndexableActionableDynamicQuery indexableActionableDynamicQuery = new IndexableActionableDynamicQuery();

		indexableActionableDynamicQuery.setBaseLocalService(com.liferay.dynamic.data.mapping.service.DDMTemplateLinkLocalServiceUtil.getService());
		indexableActionableDynamicQuery.setClassLoader(getClassLoader());
		indexableActionableDynamicQuery.setModelClass(DDMTemplateLink.class);

		indexableActionableDynamicQuery.setPrimaryKeyPropertyName(
			"templateLinkId");

		return indexableActionableDynamicQuery;
	}

	protected void initActionableDynamicQuery(
		ActionableDynamicQuery actionableDynamicQuery) {
		actionableDynamicQuery.setBaseLocalService(com.liferay.dynamic.data.mapping.service.DDMTemplateLinkLocalServiceUtil.getService());
		actionableDynamicQuery.setClassLoader(getClassLoader());
		actionableDynamicQuery.setModelClass(DDMTemplateLink.class);

		actionableDynamicQuery.setPrimaryKeyPropertyName("templateLinkId");
	}

	/**
	 * @throws PortalException
	 */
	@Override
	public PersistedModel deletePersistedModel(PersistedModel persistedModel)
		throws PortalException {
		return ddmTemplateLinkLocalService.deleteDDMTemplateLink((DDMTemplateLink)persistedModel);
	}

	@Override
	public PersistedModel getPersistedModel(Serializable primaryKeyObj)
		throws PortalException {
		return ddmTemplateLinkPersistence.findByPrimaryKey(primaryKeyObj);
	}

	/**
	 * Returns a range of all the d d m template links.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link com.liferay.portal.kernel.dao.orm.QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link com.liferay.dynamic.data.mapping.model.impl.DDMTemplateLinkModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param start the lower bound of the range of d d m template links
	 * @param end the upper bound of the range of d d m template links (not inclusive)
	 * @return the range of d d m template links
	 */
	@Override
	public List<DDMTemplateLink> getDDMTemplateLinks(int start, int end) {
		return ddmTemplateLinkPersistence.findAll(start, end);
	}

	/**
	 * Returns the number of d d m template links.
	 *
	 * @return the number of d d m template links
	 */
	@Override
	public int getDDMTemplateLinksCount() {
		return ddmTemplateLinkPersistence.countAll();
	}

	/**
	 * Updates the d d m template link in the database or adds it if it does not yet exist. Also notifies the appropriate model listeners.
	 *
	 * @param ddmTemplateLink the d d m template link
	 * @return the d d m template link that was updated
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public DDMTemplateLink updateDDMTemplateLink(
		DDMTemplateLink ddmTemplateLink) {
		return ddmTemplateLinkPersistence.update(ddmTemplateLink);
	}

	/**
	 * Returns the d d m template link local service.
	 *
	 * @return the d d m template link local service
	 */
	public DDMTemplateLinkLocalService getDDMTemplateLinkLocalService() {
		return ddmTemplateLinkLocalService;
	}

	/**
	 * Sets the d d m template link local service.
	 *
	 * @param ddmTemplateLinkLocalService the d d m template link local service
	 */
	public void setDDMTemplateLinkLocalService(
		DDMTemplateLinkLocalService ddmTemplateLinkLocalService) {
		this.ddmTemplateLinkLocalService = ddmTemplateLinkLocalService;
	}

	/**
	 * Returns the d d m template link persistence.
	 *
	 * @return the d d m template link persistence
	 */
	public DDMTemplateLinkPersistence getDDMTemplateLinkPersistence() {
		return ddmTemplateLinkPersistence;
	}

	/**
	 * Sets the d d m template link persistence.
	 *
	 * @param ddmTemplateLinkPersistence the d d m template link persistence
	 */
	public void setDDMTemplateLinkPersistence(
		DDMTemplateLinkPersistence ddmTemplateLinkPersistence) {
		this.ddmTemplateLinkPersistence = ddmTemplateLinkPersistence;
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

	public void afterPropertiesSet() {
		persistedModelLocalServiceRegistry.register("com.liferay.dynamic.data.mapping.model.DDMTemplateLink",
			ddmTemplateLinkLocalService);
	}

	public void destroy() {
		persistedModelLocalServiceRegistry.unregister(
			"com.liferay.dynamic.data.mapping.model.DDMTemplateLink");
	}

	/**
	 * Returns the OSGi service identifier.
	 *
	 * @return the OSGi service identifier
	 */
	@Override
	public String getOSGiServiceIdentifier() {
		return DDMTemplateLinkLocalService.class.getName();
	}

	protected Class<?> getModelClass() {
		return DDMTemplateLink.class;
	}

	protected String getModelClassName() {
		return DDMTemplateLink.class.getName();
	}

	/**
	 * Performs a SQL query.
	 *
	 * @param sql the sql query
	 */
	protected void runSQL(String sql) {
		try {
			DataSource dataSource = ddmTemplateLinkPersistence.getDataSource();

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

	@BeanReference(type = com.liferay.dynamic.data.mapping.service.DDMTemplateLinkLocalService.class)
	protected DDMTemplateLinkLocalService ddmTemplateLinkLocalService;
	@BeanReference(type = DDMTemplateLinkPersistence.class)
	protected DDMTemplateLinkPersistence ddmTemplateLinkPersistence;
	@ServiceReference(type = com.liferay.counter.kernel.service.CounterLocalService.class)
	protected com.liferay.counter.kernel.service.CounterLocalService counterLocalService;
	@ServiceReference(type = PersistedModelLocalServiceRegistry.class)
	protected PersistedModelLocalServiceRegistry persistedModelLocalServiceRegistry;
}