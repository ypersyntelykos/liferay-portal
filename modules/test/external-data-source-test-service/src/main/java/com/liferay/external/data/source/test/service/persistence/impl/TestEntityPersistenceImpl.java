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

package com.liferay.external.data.source.test.service.persistence.impl;

import aQute.bnd.annotation.ProviderType;

import com.liferay.external.data.source.test.exception.NoSuchTestEntityException;
import com.liferay.external.data.source.test.model.TestEntity;
import com.liferay.external.data.source.test.model.impl.TestEntityImpl;
import com.liferay.external.data.source.test.model.impl.TestEntityModelImpl;
import com.liferay.external.data.source.test.service.persistence.TestEntityPersistence;

import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.FinderCache;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.Query;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ReflectionUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.spring.extender.service.ServiceReference;

import java.io.Serializable;

import java.lang.reflect.Field;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The persistence implementation for the test entity service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @see TestEntityPersistence
 * @see com.liferay.external.data.source.test.service.persistence.TestEntityUtil
 * @generated
 */
@ProviderType
public class TestEntityPersistenceImpl extends BasePersistenceImpl<TestEntity>
	implements TestEntityPersistence {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use {@link TestEntityUtil} to access the test entity persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY = TestEntityImpl.class.getName();
	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION = FINDER_CLASS_NAME_ENTITY +
		".List1";
	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION = FINDER_CLASS_NAME_ENTITY +
		".List2";
	public static final FinderPath FINDER_PATH_WITH_PAGINATION_FIND_ALL = new FinderPath(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
			TestEntityModelImpl.FINDER_CACHE_ENABLED, TestEntityImpl.class,
			FINDER_CLASS_NAME_LIST_WITH_PAGINATION, "findAll", new String[0]);
	public static final FinderPath FINDER_PATH_WITHOUT_PAGINATION_FIND_ALL = new FinderPath(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
			TestEntityModelImpl.FINDER_CACHE_ENABLED, TestEntityImpl.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "findAll", new String[0]);
	public static final FinderPath FINDER_PATH_COUNT_ALL = new FinderPath(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
			TestEntityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countAll", new String[0]);

	public TestEntityPersistenceImpl() {
		setModelClass(TestEntity.class);

		try {
			Field field = ReflectionUtil.getDeclaredField(BasePersistenceImpl.class,
					"_dbColumnNames");

			Map<String, String> dbColumnNames = new HashMap<String, String>();

			dbColumnNames.put("id", "id_");
			dbColumnNames.put("data", "data_");

			field.set(this, dbColumnNames);
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug(e, e);
			}
		}
	}

	/**
	 * Caches the test entity in the entity cache if it is enabled.
	 *
	 * @param testEntity the test entity
	 */
	@Override
	public void cacheResult(TestEntity testEntity) {
		entityCache.putResult(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
			TestEntityImpl.class, testEntity.getPrimaryKey(), testEntity);

		testEntity.resetOriginalValues();
	}

	/**
	 * Caches the test entities in the entity cache if it is enabled.
	 *
	 * @param testEntities the test entities
	 */
	@Override
	public void cacheResult(List<TestEntity> testEntities) {
		for (TestEntity testEntity : testEntities) {
			if (entityCache.getResult(
						TestEntityModelImpl.ENTITY_CACHE_ENABLED,
						TestEntityImpl.class, testEntity.getPrimaryKey()) == null) {
				cacheResult(testEntity);
			}
			else {
				testEntity.resetOriginalValues();
			}
		}
	}

	/**
	 * Clears the cache for all test entities.
	 *
	 * <p>
	 * The {@link EntityCache} and {@link FinderCache} are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache() {
		entityCache.clearCache(TestEntityImpl.class);

		finderCache.clearCache(FINDER_CLASS_NAME_ENTITY);
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
	}

	/**
	 * Clears the cache for the test entity.
	 *
	 * <p>
	 * The {@link EntityCache} and {@link FinderCache} are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache(TestEntity testEntity) {
		entityCache.removeResult(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
			TestEntityImpl.class, testEntity.getPrimaryKey());

		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
	}

	@Override
	public void clearCache(List<TestEntity> testEntities) {
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);

		for (TestEntity testEntity : testEntities) {
			entityCache.removeResult(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
				TestEntityImpl.class, testEntity.getPrimaryKey());
		}
	}

	/**
	 * Creates a new test entity with the primary key. Does not add the test entity to the database.
	 *
	 * @param id the primary key for the new test entity
	 * @return the new test entity
	 */
	@Override
	public TestEntity create(long id) {
		TestEntity testEntity = new TestEntityImpl();

		testEntity.setNew(true);
		testEntity.setPrimaryKey(id);

		return testEntity;
	}

	/**
	 * Removes the test entity with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param id the primary key of the test entity
	 * @return the test entity that was removed
	 * @throws NoSuchTestEntityException if a test entity with the primary key could not be found
	 */
	@Override
	public TestEntity remove(long id) throws NoSuchTestEntityException {
		return remove((Serializable)id);
	}

	/**
	 * Removes the test entity with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param primaryKey the primary key of the test entity
	 * @return the test entity that was removed
	 * @throws NoSuchTestEntityException if a test entity with the primary key could not be found
	 */
	@Override
	public TestEntity remove(Serializable primaryKey)
		throws NoSuchTestEntityException {
		Session session = null;

		try {
			session = openSession();

			TestEntity testEntity = (TestEntity)session.get(TestEntityImpl.class,
					primaryKey);

			if (testEntity == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
				}

				throw new NoSuchTestEntityException(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY +
					primaryKey);
			}

			return remove(testEntity);
		}
		catch (NoSuchTestEntityException nsee) {
			throw nsee;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	protected TestEntity removeImpl(TestEntity testEntity) {
		testEntity = toUnwrappedModel(testEntity);

		Session session = null;

		try {
			session = openSession();

			if (!session.contains(testEntity)) {
				testEntity = (TestEntity)session.get(TestEntityImpl.class,
						testEntity.getPrimaryKeyObj());
			}

			if (testEntity != null) {
				session.delete(testEntity);
			}
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}

		if (testEntity != null) {
			clearCache(testEntity);
		}

		return testEntity;
	}

	@Override
	public TestEntity updateImpl(TestEntity testEntity) {
		testEntity = toUnwrappedModel(testEntity);

		boolean isNew = testEntity.isNew();

		Session session = null;

		try {
			session = openSession();

			if (testEntity.isNew()) {
				session.save(testEntity);

				testEntity.setNew(false);
			}
			else {
				testEntity = (TestEntity)session.merge(testEntity);
			}
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}

		finderCache.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);

		if (isNew) {
			finderCache.removeResult(FINDER_PATH_COUNT_ALL, FINDER_ARGS_EMPTY);
			finderCache.removeResult(FINDER_PATH_WITHOUT_PAGINATION_FIND_ALL,
				FINDER_ARGS_EMPTY);
		}

		entityCache.putResult(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
			TestEntityImpl.class, testEntity.getPrimaryKey(), testEntity, false);

		testEntity.resetOriginalValues();

		return testEntity;
	}

	protected TestEntity toUnwrappedModel(TestEntity testEntity) {
		if (testEntity instanceof TestEntityImpl) {
			return testEntity;
		}

		TestEntityImpl testEntityImpl = new TestEntityImpl();

		testEntityImpl.setNew(testEntity.isNew());
		testEntityImpl.setPrimaryKey(testEntity.getPrimaryKey());

		testEntityImpl.setId(testEntity.getId());
		testEntityImpl.setData(testEntity.getData());

		return testEntityImpl;
	}

	/**
	 * Returns the test entity with the primary key or throws a {@link com.liferay.portal.kernel.exception.NoSuchModelException} if it could not be found.
	 *
	 * @param primaryKey the primary key of the test entity
	 * @return the test entity
	 * @throws NoSuchTestEntityException if a test entity with the primary key could not be found
	 */
	@Override
	public TestEntity findByPrimaryKey(Serializable primaryKey)
		throws NoSuchTestEntityException {
		TestEntity testEntity = fetchByPrimaryKey(primaryKey);

		if (testEntity == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			throw new NoSuchTestEntityException(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY +
				primaryKey);
		}

		return testEntity;
	}

	/**
	 * Returns the test entity with the primary key or throws a {@link NoSuchTestEntityException} if it could not be found.
	 *
	 * @param id the primary key of the test entity
	 * @return the test entity
	 * @throws NoSuchTestEntityException if a test entity with the primary key could not be found
	 */
	@Override
	public TestEntity findByPrimaryKey(long id)
		throws NoSuchTestEntityException {
		return findByPrimaryKey((Serializable)id);
	}

	/**
	 * Returns the test entity with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the test entity
	 * @return the test entity, or <code>null</code> if a test entity with the primary key could not be found
	 */
	@Override
	public TestEntity fetchByPrimaryKey(Serializable primaryKey) {
		Serializable serializable = entityCache.getResult(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
				TestEntityImpl.class, primaryKey);

		if (serializable == nullModel) {
			return null;
		}

		TestEntity testEntity = (TestEntity)serializable;

		if (testEntity == null) {
			Session session = null;

			try {
				session = openSession();

				testEntity = (TestEntity)session.get(TestEntityImpl.class,
						primaryKey);

				if (testEntity != null) {
					cacheResult(testEntity);
				}
				else {
					entityCache.putResult(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
						TestEntityImpl.class, primaryKey, nullModel);
				}
			}
			catch (Exception e) {
				entityCache.removeResult(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
					TestEntityImpl.class, primaryKey);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return testEntity;
	}

	/**
	 * Returns the test entity with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param id the primary key of the test entity
	 * @return the test entity, or <code>null</code> if a test entity with the primary key could not be found
	 */
	@Override
	public TestEntity fetchByPrimaryKey(long id) {
		return fetchByPrimaryKey((Serializable)id);
	}

	@Override
	public Map<Serializable, TestEntity> fetchByPrimaryKeys(
		Set<Serializable> primaryKeys) {
		if (primaryKeys.isEmpty()) {
			return Collections.emptyMap();
		}

		Map<Serializable, TestEntity> map = new HashMap<Serializable, TestEntity>();

		if (primaryKeys.size() == 1) {
			Iterator<Serializable> iterator = primaryKeys.iterator();

			Serializable primaryKey = iterator.next();

			TestEntity testEntity = fetchByPrimaryKey(primaryKey);

			if (testEntity != null) {
				map.put(primaryKey, testEntity);
			}

			return map;
		}

		Set<Serializable> uncachedPrimaryKeys = null;

		for (Serializable primaryKey : primaryKeys) {
			Serializable serializable = entityCache.getResult(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
					TestEntityImpl.class, primaryKey);

			if (serializable != nullModel) {
				if (serializable == null) {
					if (uncachedPrimaryKeys == null) {
						uncachedPrimaryKeys = new HashSet<Serializable>();
					}

					uncachedPrimaryKeys.add(primaryKey);
				}
				else {
					map.put(primaryKey, (TestEntity)serializable);
				}
			}
		}

		if (uncachedPrimaryKeys == null) {
			return map;
		}

		StringBundler query = new StringBundler((uncachedPrimaryKeys.size() * 2) +
				1);

		query.append(_SQL_SELECT_TESTENTITY_WHERE_PKS_IN);

		for (Serializable primaryKey : uncachedPrimaryKeys) {
			query.append((long)primaryKey);

			query.append(StringPool.COMMA);
		}

		query.setIndex(query.index() - 1);

		query.append(StringPool.CLOSE_PARENTHESIS);

		String sql = query.toString();

		Session session = null;

		try {
			session = openSession();

			Query q = session.createQuery(sql);

			for (TestEntity testEntity : (List<TestEntity>)q.list()) {
				map.put(testEntity.getPrimaryKeyObj(), testEntity);

				cacheResult(testEntity);

				uncachedPrimaryKeys.remove(testEntity.getPrimaryKeyObj());
			}

			for (Serializable primaryKey : uncachedPrimaryKeys) {
				entityCache.putResult(TestEntityModelImpl.ENTITY_CACHE_ENABLED,
					TestEntityImpl.class, primaryKey, nullModel);
			}
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}

		return map;
	}

	/**
	 * Returns all the test entities.
	 *
	 * @return the test entities
	 */
	@Override
	public List<TestEntity> findAll() {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the test entities.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link TestEntityModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param start the lower bound of the range of test entities
	 * @param end the upper bound of the range of test entities (not inclusive)
	 * @return the range of test entities
	 */
	@Override
	public List<TestEntity> findAll(int start, int end) {
		return findAll(start, end, null);
	}

	/**
	 * Returns an ordered range of all the test entities.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link TestEntityModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param start the lower bound of the range of test entities
	 * @param end the upper bound of the range of test entities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of test entities
	 */
	@Override
	public List<TestEntity> findAll(int start, int end,
		OrderByComparator<TestEntity> orderByComparator) {
		return findAll(start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the test entities.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not {@link QueryUtil#ALL_POS}), then the query will include the default ORDER BY logic from {@link TestEntityModelImpl}. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param start the lower bound of the range of test entities
	 * @param end the upper bound of the range of test entities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param retrieveFromCache whether to retrieve from the finder cache
	 * @return the ordered range of test entities
	 */
	@Override
	public List<TestEntity> findAll(int start, int end,
		OrderByComparator<TestEntity> orderByComparator,
		boolean retrieveFromCache) {
		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
				(orderByComparator == null)) {
			pagination = false;
			finderPath = FINDER_PATH_WITHOUT_PAGINATION_FIND_ALL;
			finderArgs = FINDER_ARGS_EMPTY;
		}
		else {
			finderPath = FINDER_PATH_WITH_PAGINATION_FIND_ALL;
			finderArgs = new Object[] { start, end, orderByComparator };
		}

		List<TestEntity> list = null;

		if (retrieveFromCache) {
			list = (List<TestEntity>)finderCache.getResult(finderPath,
					finderArgs, this);
		}

		if (list == null) {
			StringBundler query = null;
			String sql = null;

			if (orderByComparator != null) {
				query = new StringBundler(2 +
						(orderByComparator.getOrderByFields().length * 2));

				query.append(_SQL_SELECT_TESTENTITY);

				appendOrderByComparator(query, _ORDER_BY_ENTITY_ALIAS,
					orderByComparator);

				sql = query.toString();
			}
			else {
				sql = _SQL_SELECT_TESTENTITY;

				if (pagination) {
					sql = sql.concat(TestEntityModelImpl.ORDER_BY_JPQL);
				}
			}

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				if (!pagination) {
					list = (List<TestEntity>)QueryUtil.list(q, getDialect(),
							start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<TestEntity>)QueryUtil.list(q, getDialect(),
							start, end);
				}

				cacheResult(list);

				finderCache.putResult(finderPath, finderArgs, list);
			}
			catch (Exception e) {
				finderCache.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Removes all the test entities from the database.
	 *
	 */
	@Override
	public void removeAll() {
		for (TestEntity testEntity : findAll()) {
			remove(testEntity);
		}
	}

	/**
	 * Returns the number of test entities.
	 *
	 * @return the number of test entities
	 */
	@Override
	public int countAll() {
		Long count = (Long)finderCache.getResult(FINDER_PATH_COUNT_ALL,
				FINDER_ARGS_EMPTY, this);

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(_SQL_COUNT_TESTENTITY);

				count = (Long)q.uniqueResult();

				finderCache.putResult(FINDER_PATH_COUNT_ALL, FINDER_ARGS_EMPTY,
					count);
			}
			catch (Exception e) {
				finderCache.removeResult(FINDER_PATH_COUNT_ALL,
					FINDER_ARGS_EMPTY);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	@Override
	public Set<String> getBadColumnNames() {
		return _badColumnNames;
	}

	@Override
	protected Map<String, Integer> getTableColumnsMap() {
		return TestEntityModelImpl.TABLE_COLUMNS_MAP;
	}

	/**
	 * Initializes the test entity persistence.
	 */
	public void afterPropertiesSet() {
	}

	public void destroy() {
		entityCache.removeCache(TestEntityImpl.class.getName());
		finderCache.removeCache(FINDER_CLASS_NAME_ENTITY);
		finderCache.removeCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		finderCache.removeCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
	}

	@ServiceReference(type = EntityCache.class)
	protected EntityCache entityCache;
	@ServiceReference(type = FinderCache.class)
	protected FinderCache finderCache;
	private static final String _SQL_SELECT_TESTENTITY = "SELECT testEntity FROM TestEntity testEntity";
	private static final String _SQL_SELECT_TESTENTITY_WHERE_PKS_IN = "SELECT testEntity FROM TestEntity testEntity WHERE id_ IN (";
	private static final String _SQL_COUNT_TESTENTITY = "SELECT COUNT(testEntity) FROM TestEntity testEntity";
	private static final String _ORDER_BY_ENTITY_ALIAS = "testEntity.";
	private static final String _NO_SUCH_ENTITY_WITH_PRIMARY_KEY = "No TestEntity exists with the primary key ";
	private static final Log _log = LogFactoryUtil.getLog(TestEntityPersistenceImpl.class);
	private static final Set<String> _badColumnNames = SetUtil.fromArray(new String[] {
				"id", "data"
			});
}