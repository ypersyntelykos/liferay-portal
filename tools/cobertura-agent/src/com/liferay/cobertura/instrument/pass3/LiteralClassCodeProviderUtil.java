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

package com.liferay.cobertura.instrument.pass3;

import java.lang.reflect.Field;

import net.sourceforge.cobertura.instrument.pass3.InjectCodeClassInstrumenter;
import net.sourceforge.cobertura.instrument.pass3.InjectCodeTouchPointListener;

/**
 * @author Shuyang Zhou
 */
public class LiteralClassCodeProviderUtil {

	public static void install(
		InjectCodeClassInstrumenter injectCodeClassInstrumenter) {

		Class<?> clazz = InjectCodeClassInstrumenter.class;

		try {
			Field codeProviderField = clazz.getDeclaredField("codeProvider");

			codeProviderField.setAccessible(true);

			codeProviderField.set(
				injectCodeClassInstrumenter, _literalClassCodeProvider);

			Field touchPointListenerField = clazz.getDeclaredField(
				"touchPointListener");

			touchPointListenerField.setAccessible(true);

			InjectCodeTouchPointListener injectCodeTouchPointListener =
				(InjectCodeTouchPointListener)touchPointListenerField.get(
					injectCodeClassInstrumenter);

			clazz = InjectCodeTouchPointListener.class;

			codeProviderField = clazz.getDeclaredField("codeProvider");

			codeProviderField.setAccessible(true);

			codeProviderField.set(
				injectCodeTouchPointListener, _literalClassCodeProvider);
		}
		catch (ReflectiveOperationException roe) {
			throw new RuntimeException(roe);
		}
	}

	private static final LiteralClassCodeProvider _literalClassCodeProvider =
		new LiteralClassCodeProvider();

}