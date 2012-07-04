/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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

package com.liferay.portal.kernel.util;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.annotation.AccessControl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Brian Wing Shun Chan
 * @author Shuyang Zhou
 */
@AccessControl
public class LocaleUtil {

	public static Locale fromLanguageId(String languageId) {
		if (languageId == null) {
			return _locale;
		}

		Locale locale = _locales.get(languageId);

		if (locale != null) {
			return locale;
		}

		try {
			int pos = languageId.indexOf(CharPool.UNDERLINE);

			if (pos == -1) {
				locale = new Locale(languageId);
			}
			else {
				String[] languageIdParts = StringUtil.split(
					languageId, CharPool.UNDERLINE);

				String languageCode = languageIdParts[0];
				String countryCode = languageIdParts[1];

				String variant = null;

				if (languageIdParts.length > 2) {
					variant = languageIdParts[2];
				}

				if (Validator.isNotNull(variant)) {
					locale = new Locale(languageCode, countryCode, variant);
				}
				else {
					locale = new Locale(languageCode, countryCode);
				}
			}

			if (_locales.size() < _MAX_LOCALES) {
				_locales.put(languageId, locale);
			}
			else {
				if (_log.isWarnEnabled()) {
					_log.warn("There are too many entries in the locales map");
				}
			}
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(languageId + " is not a valid language id");
			}
		}

		if (locale == null) {
			locale = _locale;
		}

		return locale;
	}

	public static Locale[] fromLanguageIds(String[] languageIds) {
		Locale[] locales = new Locale[languageIds.length];

		for (int i = 0; i < languageIds.length; i++) {
			locales[i] = fromLanguageId(languageIds[i]);
		}

		return locales;
	}

	public static Locale getDefault() {
		Locale locale = LocaleThreadLocal.getDefaultLocale();

		if (locale != null) {
			return locale;
		}

		return _locale;
	}

	public static LocaleUtil getInstance() {
		// Don't try to change this to a static field, in pacl env, all static
		// fields will be copied to modified verion of this class, holding a
		// reference of original class instance will cause ClassCastException.
		return new LocaleUtil();
	}

	public static Map<String, String> getISOLanguages(Locale locale) {
		Map<String, String> isoLanguages = new TreeMap<String, String>(
			String.CASE_INSENSITIVE_ORDER);

		for (String isoLanguageId : Locale.getISOLanguages()) {
			Locale isoLocale = fromLanguageId(isoLanguageId);

			isoLanguages.put(
				isoLocale.getDisplayLanguage(locale), isoLanguageId);
		}

		return isoLanguages;
	}

	public static void setDefault(
		String userLanguage, String userCountry, String userVariant) {

		if (Validator.isNotNull(userLanguage) &&
			Validator.isNull(userCountry) && Validator.isNull(userVariant)) {

			_locale = new Locale(userLanguage);
		}
		else if (Validator.isNotNull(userLanguage) &&
				 Validator.isNotNull(userCountry) &&
				 Validator.isNull(userVariant)) {

			_locale = new Locale(userLanguage, userCountry);
		}
		else if (Validator.isNotNull(userLanguage) &&
				 Validator.isNotNull(userCountry) &&
				 Validator.isNotNull(userVariant)) {

			_locale = new Locale(userLanguage, userCountry, userVariant);
		}
	}

	public static String toLanguageId(Locale locale) {
		if (locale == null) {
			locale = _locale;
		}

		String country = locale.getCountry();

		boolean hasCountry = false;

		if (country.length() != 0) {
			hasCountry = true;
		}

		String variant = locale.getVariant();

		boolean hasVariant = false;

		if (variant.length() != 0) {
			hasVariant = true;
		}

		if (!hasCountry && !hasVariant) {
			return locale.getLanguage();
		}

		int length = 3;

		if (hasCountry && hasVariant) {
			length = 5;
		}

		StringBundler sb = new StringBundler(length);

		sb.append(locale.getLanguage());

		if (hasCountry) {
			sb.append(StringPool.UNDERLINE);
			sb.append(country);
		}

		if (hasVariant) {
			sb.append(StringPool.UNDERLINE);
			sb.append(variant);
		}

		return sb.toString();
	}

	public static String[] toLanguageIds(Locale[] locales) {
		String[] languageIds = new String[locales.length];

		for (int i = 0; i < locales.length; i++) {
			languageIds[i] = toLanguageId(locales[i]);
		}

		return languageIds;
	}

	public static String toW3cLanguageId(Locale locale) {
		return toW3cLanguageId(toLanguageId(locale));
	}

	public static String toW3cLanguageId(String languageId) {
		return StringUtil.replace(
			languageId, CharPool.UNDERLINE, CharPool.MINUS);
	}

	public static String[] toW3cLanguageIds(Locale[] locales) {
		return toW3cLanguageIds(toLanguageIds(locales));
	}

	public static String[] toW3cLanguageIds(String[] languageIds) {
		String[] w3cLanguageIds = new String[languageIds.length];

		for (int i = 0; i < languageIds.length; i++) {
			w3cLanguageIds[i] = toW3cLanguageId(languageIds[i]);
		}

		return w3cLanguageIds;
	}

	private static final int _MAX_LOCALES = 1000;

	private static Log _log = LogFactoryUtil.getLog(LocaleUtil.class);

	private static Locale _locale = new Locale("en", "US");;
	private static Map<String, Locale> _locales = new HashMap<String, Locale>();

}