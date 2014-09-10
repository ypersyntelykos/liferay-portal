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

import net.sourceforge.cobertura.coveragedata.TouchCollector;
import net.sourceforge.cobertura.instrument.pass3.FastArrayCodeProvider;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author Shuyang Zhou
 */
public class LiteralClassCodeProvider extends FastArrayCodeProvider {

	@Override
	protected void generateRegisterClass(
		MethodVisitor methodVisitor, String className) {

		methodVisitor.visitLdcInsn(Type.getObjectType(className));
		methodVisitor.visitMethodInsn(
			Opcodes.INVOKESTATIC, Type.getInternalName(TouchCollector.class),
			"registerClass", "(Ljava/lang/Class;)V", false);
	}

}