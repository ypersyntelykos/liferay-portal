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

package com.liferay.portal.dao.orm.hibernate.event;

import com.liferay.portal.dao.orm.hibernate.SessionFactoryImpl;
import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;
import com.liferay.portal.kernel.model.CacheModel;
import com.liferay.portal.kernel.model.ClassName;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.model.impl.ClassNameImpl;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.List;
import java.util.concurrent.Callable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.event.AutoFlushEventListener;
import org.hibernate.event.EventListeners;
import org.hibernate.event.FlushEventListener;
import org.hibernate.event.def.DefaultAutoFlushEventListener;
import org.hibernate.event.def.DefaultFlushEventListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Matthew Tambara
 */
public class NestableFlushEventListenerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@BeforeClass
	public static void setUpClass() {
		SessionFactoryImpl sessionFactoryImpl =
			(SessionFactoryImpl)PortalBeanLocatorUtil.locate(
				"liferaySessionFactory");

		_sessionFactoryImpl =
			(org.hibernate.impl.SessionFactoryImpl)
				sessionFactoryImpl.getSessionFactoryImplementor();
	}

	@After
	public void tearDown() {
		_session.close();
	}

	@Test
	public void testDefaultAutoFlushEventListener() throws Throwable {
		EventListeners eventListeners = _sessionFactoryImpl.getEventListeners();

		AutoFlushEventListener[] autoFlushEventListeners =
			eventListeners.getAutoFlushEventListeners();

		eventListeners.setAutoFlushEventListeners(
			new AutoFlushEventListener[] {new DefaultAutoFlushEventListener()});

		try {
			testNestableAutoFlushEventListener();

			Assert.fail();
		}
		catch (IndexOutOfBoundsException ioobe) {
		}
		finally {
			eventListeners.setAutoFlushEventListeners(autoFlushEventListeners);
		}
	}

	@Test
	public void testDefaultFlushEventListener() throws Throwable {
		EventListeners eventListeners = _sessionFactoryImpl.getEventListeners();

		FlushEventListener[] flushEventListeners =
			eventListeners.getFlushEventListeners();

		eventListeners.setFlushEventListeners(
			new FlushEventListener[] {new DefaultFlushEventListener()});

		try {
			testNestableFlushEventListener();

			Assert.fail();
		}
		catch (IndexOutOfBoundsException ioobe) {
		}
		finally {
			eventListeners.setFlushEventListeners(flushEventListeners);
		}
	}

	@Test
	public void testNestableAutoFlushEventListener() throws Throwable {
		_flushTest(
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					_session.merge(_className1);
					_session.merge(_className2);

					Query query = _session.createQuery(
						"SELECT className FROM ClassName className");

					query.list();

					return null;
				}

		});
	}

	@Test
	public void testNestableFlushEventListener() throws Throwable {
		_flushTest(
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					_session.merge(_className1);
					_session.merge(_className2);

					_session.flush();

					return null;
				}

		});
	}

	private void _flushTest(Callable<Void> callable) throws Throwable {
		_session = _sessionFactoryImpl.openSession(
			new EmptyInterceptor() {

				@Override
				public String getEntityName(Object object) {
					if (object instanceof TestClassNameImpl) {
						return ClassNameImpl.class.getName();
					}

					return super.getEntityName(object);
				}

			});

		Transaction transaction = _session.beginTransaction();

		try {
			_className1 = new TestClassNameImpl();

			_className1.setPrimaryKey(RandomTestUtil.nextLong());

			_session.save(_className1);

			_className2 = new ClassNameImpl();

			_className2.setPrimaryKey(RandomTestUtil.nextLong());

			_session.save(_className2);
		}
		finally {
			transaction.commit();
		}

		transaction = _session.beginTransaction();

		try {
			_className1.setValue(RandomTestUtil.randomString());

			_className1.setMvccVersion(_className1.getMvccVersion() + 1);

			_className2.setValue(RandomTestUtil.randomString());

			_className2.setMvccVersion(_className1.getMvccVersion() + 1);

			callable.call();
		}
		finally {
			transaction.commit();
		}
	}

	private static org.hibernate.impl.SessionFactoryImpl _sessionFactoryImpl;

	@DeleteAfterTestRun
	private ClassName _className1;

	@DeleteAfterTestRun
	private ClassName _className2;

	private Session _session;

	private class TestClassNameImpl extends ClassNameImpl {

		@Override
			public CacheModel<ClassName> toCacheModel() {
				Query query = _session.createQuery(
					"SELECT release FROM Release release");

				List<?> results = query.list();

				Assert.assertFalse(results.isEmpty());

				return super.toCacheModel();
			}

	}

}