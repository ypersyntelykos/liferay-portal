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

package com.liferay.users.admin.indexer.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistry;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author André de Oliveira
 */
@RunWith(Arquillian.class)
@Sync
public class UserIndexerTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		Registry registry = RegistryUtil.getRegistry();

		_userLocalService = registry.getService(UserLocalService.class);

		IndexerRegistry indexerRegistry = registry.getService(
			IndexerRegistry.class);

		_indexer = indexerRegistry.getIndexer(User.class);
	}

	@Test
	public void testEmailAddress() throws Exception {
		_expectedUser = UserTestUtil.addUser();

		String expectedEmailAddress = _expectedUser.getEmailAddress();

		User actualUser = assertSearchOneUser(
			StringUtil.toUpperCase(expectedEmailAddress), _expectedUser);

		Assert.assertEquals(expectedEmailAddress, actualUser.getEmailAddress());
	}

	@Test
	public void testEmailAddressField() throws Exception {
		_expectedUser = UserTestUtil.addUser();

		String expectedEmailAddress = _expectedUser.getEmailAddress();

		User actualUser = assertSearchOneUser(
			"emailAddress", expectedEmailAddress, _expectedUser);

		Assert.assertEquals(expectedEmailAddress, actualUser.getEmailAddress());
	}

	@Test
	public void testEmailAddressPrefix() throws Exception {
		_expectedUser = UserTestUtil.addUser();

		String expectedEmailAddress = _expectedUser.getEmailAddress();

		User actualUser = assertSearchOneUser(
			StringUtil.removeSubstring(expectedEmailAddress, "@liferay.com"),
			_expectedUser);

		Assert.assertEquals(expectedEmailAddress, actualUser.getEmailAddress());
	}

	@Test
	public void testEmailAddressSubstring() throws Exception {
		_expectedUser = UserTestUtil.addUser();

		String expectedEmailAddress = _expectedUser.getEmailAddress();

		User actualUser = assertSearchOneUser(
			expectedEmailAddress.substring(
				4, expectedEmailAddress.length() - 7),
			_expectedUser);

		Assert.assertEquals(expectedEmailAddress, actualUser.getEmailAddress());
	}

	@Test
	public void testEmptyQuery() throws Exception {
		_expectedUser = UserTestUtil.addUser();

		assertSearch(StringPool.BLANK, _expectedUser);
	}

	@Test
	public void testFirstNameExactPhrase() throws Exception {
		String firstName = "Mary Jane";
		String middleName = "Watson";
		String lastName = "Parker";

		_expectedUser = UserTestUtil.addUser();

		_expectedUser.setFirstName(firstName);
		_expectedUser.setMiddleName(middleName);
		_expectedUser.setLastName(lastName);

		_userLocalService.updateUser(_expectedUser);

		User actualUser = assertSearchOneUser(
			"firstName", "\"Mary Jane\"", _expectedUser);

		Assert.assertEquals(firstName, actualUser.getFirstName());
	}

	@Test
	public void testFirstNameMixedExactPhrase() throws Exception {
		String firstName = "Mary Jane Watson";
		String middleName = "Joanne";
		String lastName = "Parker";

		_expectedUser = UserTestUtil.addUser();

		_expectedUser.setFirstName(firstName);
		_expectedUser.setMiddleName(middleName);
		_expectedUser.setLastName(lastName);

		_userLocalService.updateUser(_expectedUser);

		assertNoHits("firstName", "\"Mary Watson\"");
		assertNoHits("firstName", "\"Mary Jane\" Missingword");

		User actualUser = assertSearchOneUser(
			"firstName", "Mary \"Jane Watson\"", _expectedUser);

		Assert.assertEquals(firstName, actualUser.getFirstName());
	}

	@Test
	public void testLikeCharacter() throws Exception {
		_expectedUser = UserTestUtil.addUser();

		assertSearch(StringPool.PERCENT, _expectedUser);

		assertNoHits(StringPool.PERCENT + RandomTestUtil.randomString());
	}

	@Test
	public void testLuceneQueryParserUnfriendlyCharacters() throws Exception {
		_expectedUser = UserTestUtil.addUser();

		assertSearch(StringPool.AT, _expectedUser);

		assertNoHits(StringPool.AT + RandomTestUtil.randomString());
		assertNoHits(StringPool.EXCLAMATION);
		assertNoHits(StringPool.EXCLAMATION + RandomTestUtil.randomString());
	}

	@Test
	public void testNameFieldsNotTokenized() throws Exception {
		String firstName = "Liferay7";
		String lastName = "dell'Apostrophe";
		String middleName = "ALLOY_4";

		testNameFields(firstName, lastName, middleName);
	}

	@Test
	public void testNameFieldsNotTokenizedLowercase() throws Exception {
		String firstName = "liferay7";
		String lastName = "dell'apostrophe";
		String middleName = "alloy_4";

		testNameFields(firstName, lastName, middleName);
	}

	@Test
	public void testNamesPrefix() throws Exception {
		String firstName = "First";
		String lastName = "Last";
		String middleName = "Middle";

		_expectedUser = UserTestUtil.addUser();

		_expectedUser.setFirstName(firstName);
		_expectedUser.setMiddleName(middleName);
		_expectedUser.setLastName(lastName);

		_userLocalService.updateUser(_expectedUser);

		User actualUser = assertSearchOneUser("Fir", _expectedUser);

		Assert.assertEquals("First", actualUser.getFirstName());

		actualUser = assertSearchOneUser("LasT", _expectedUser);

		Assert.assertEquals("Last", actualUser.getLastName());

		actualUser = assertSearchOneUser("midd", _expectedUser);

		Assert.assertEquals("Middle", actualUser.getMiddleName());
	}

	@Test
	public void testNamesSubstring() throws Exception {
		String firstName = "First";
		String lastName = "Last";
		String middleName = "Middle";

		_expectedUser = UserTestUtil.addUser();

		_expectedUser.setFirstName(firstName);
		_expectedUser.setMiddleName(middleName);
		_expectedUser.setLastName(lastName);

		_userLocalService.updateUser(_expectedUser);

		User actualUser = assertSearchOneUser("Fir", _expectedUser);

		Assert.assertEquals("First", actualUser.getFirstName());

		actualUser = assertSearchOneUser("asT", _expectedUser);

		Assert.assertEquals("Last", actualUser.getLastName());

		actualUser = assertSearchOneUser("idd", _expectedUser);

		Assert.assertEquals("Middle", actualUser.getMiddleName());
	}

	@Test
	public void testScreenName() throws Exception {
		_expectedUser = UserTestUtil.addUser(
			"Open4Life", new long[] {TestPropsValues.getGroupId()});

		User actualUser = assertSearchOneUser("Open4Life", _expectedUser);

		Assert.assertEquals("open4life", actualUser.getScreenName());
	}

	@Test
	public void testScreenNameField() throws Exception {
		_expectedUser = UserTestUtil.addUser(
			"Open4Life", new long[] {TestPropsValues.getGroupId()});

		User actualUser = assertSearchOneUser(
			"screenName", "open4life", _expectedUser);

		Assert.assertEquals("open4life", actualUser.getScreenName());
	}

	@Test
	public void testScreenNameSubstring() throws Exception {
		_expectedUser = UserTestUtil.addUser(
			"Open4Life", new long[] {TestPropsValues.getGroupId()});

		User actualUser = assertSearchOneUser("open lite", _expectedUser);

		Assert.assertEquals("open4life", actualUser.getScreenName());

		actualUser = assertSearchOneUser("OPE", _expectedUser);

		Assert.assertEquals("open4life", actualUser.getScreenName());

		actualUser = assertSearchOneUser("4lif", _expectedUser);

		Assert.assertEquals("open4life", actualUser.getScreenName());
	}

	protected void assertLength(Hits hits, int length) {
		Assert.assertEquals(hits.toString(), length, hits.getLength());
	}

	protected void assertNoHits(String keywords) throws Exception {
		Hits hits = search(keywords);

		assertLength(hits, 0);
	}

	protected void assertNoHits(String field, String value) throws Exception {
		Hits hits = search(field, value);

		assertLength(hits, 0);
	}

	protected List<User> assertSearch(Hits hits, User... expectedUsers)
		throws Exception {

		List<User> actualUsers = getUsers(hits);

		List<String> actualScreenNames = getScreenNames(actualUsers);

		List<String> expectedScreenNames = getScreenNames(
			Arrays.asList(expectedUsers));

		if (!actualScreenNames.containsAll(expectedScreenNames)) {
			Assert.assertEquals(
				toString(expectedScreenNames), toString(actualScreenNames));
		}

		return actualUsers;
	}

	protected List<User> assertSearch(String keywords, User... expectedUsers)
		throws Exception {

		Hits hits = search(keywords);

		return assertSearch(hits, expectedUsers);
	}

	protected User assertSearchOneUser(String field, String value, User user)
		throws Exception {

		Hits hits = search(field, value);

		List<User> actualUsers = assertSearch(hits, user);

		return findByUserId(actualUsers, user.getUserId());
	}

	protected User assertSearchOneUser(String keywords, User user)
		throws Exception {

		List<User> actualUsers = assertSearch(keywords, user);

		return findByUserId(actualUsers, user.getUserId());
	}

	protected User findByUserId(List<User> users, long userId) {
		for (User user : users) {
			if (user.getUserId() == userId) {
				return user;
			}
		}

		return null;
	}

	protected List<String> getScreenNames(List<User> users) {
		List<String> screenNames = new ArrayList<>(users.size());

		for (User user : users) {
			screenNames.add(user.getScreenName());
		}

		return screenNames;
	}

	protected SearchContext getSearchContext() throws Exception {
		SearchContext searchContext = new SearchContext();

		searchContext.setCompanyId(TestPropsValues.getCompanyId());
		searchContext.setGroupIds(new long[] {TestPropsValues.getGroupId()});

		return searchContext;
	}

	protected User getUser(Document document) throws Exception {
		long userId = GetterUtil.getLong(document.get(Field.USER_ID));

		return _userLocalService.getUser(userId);
	}

	protected List<User> getUsers(Hits hits) throws Exception {
		Document[] documents = hits.getDocs();

		List<User> users = new ArrayList<>(documents.length);

		for (Document document : documents) {
			users.add(getUser(document));
		}

		return users;
	}

	protected Hits search(SearchContext searchContext) throws Exception {
		return _indexer.search(searchContext);
	}

	protected Hits search(String keywords) throws Exception {
		SearchContext searchContext = getSearchContext();

		searchContext.setKeywords(keywords);

		return search(searchContext);
	}

	protected Hits search(String field, String value) throws Exception {
		SearchContext searchContext = getSearchContext();

		searchContext.setAttribute(field, value);

		return search(searchContext);
	}

	protected void testNameFields(
			String firstName, String lastName, String middleName)
		throws Exception {

		_expectedUser = UserTestUtil.addUser();

		_expectedUser.setFirstName(firstName);
		_expectedUser.setMiddleName(middleName);
		_expectedUser.setLastName(lastName);

		_userLocalService.updateUser(_expectedUser);

		User actualUser = assertSearchOneUser(
			"firstName", firstName, _expectedUser);

		Assert.assertEquals(firstName, actualUser.getFirstName());

		actualUser = assertSearchOneUser("lastName", lastName, _expectedUser);

		Assert.assertEquals(lastName, actualUser.getLastName());

		actualUser = assertSearchOneUser(
			"middleName", middleName, _expectedUser);

		Assert.assertEquals(middleName, actualUser.getMiddleName());
	}

	protected String toString(List<String> strings) {
		Collections.sort(strings);

		return strings.toString();
	}

	@DeleteAfterTestRun
	private User _expectedUser;

	private Indexer<User> _indexer;
	private UserLocalService _userLocalService;

}