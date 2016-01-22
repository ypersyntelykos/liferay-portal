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

package com.liferay.portal.workflow.kaleo.model;

import aQute.bnd.annotation.ProviderType;

import com.liferay.portal.kernel.annotation.ImplementationPath;
import com.liferay.portal.kernel.util.Accessor;
import com.liferay.portal.model.PersistedModel;

/**
 * The extended model interface for the KaleoTaskAssignmentInstance service. Represents a row in the &quot;KaleoTaskAssignmentInstance&quot; database table, with each column mapped to a property of this class.
 *
 * @author Brian Wing Shun Chan
 * @see KaleoTaskAssignmentInstanceModel
 * @see com.liferay.portal.workflow.kaleo.model.impl.KaleoTaskAssignmentInstanceImpl
 * @see com.liferay.portal.workflow.kaleo.model.impl.KaleoTaskAssignmentInstanceModelImpl
 * @generated
 */
@ProviderType
@ImplementationPath("com.liferay.portal.workflow.kaleo.model.impl.KaleoTaskAssignmentInstanceImpl")
public interface KaleoTaskAssignmentInstance
	extends KaleoTaskAssignmentInstanceModel, PersistedModel {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify this interface directly. Add methods to {@link com.liferay.portal.workflow.kaleo.model.impl.KaleoTaskAssignmentInstanceImpl} and rerun ServiceBuilder to automatically copy the method declarations to this interface.
	 */
	public static final Accessor<KaleoTaskAssignmentInstance, Long> KALEO_TASK_ASSIGNMENT_INSTANCE_ID_ACCESSOR =
		new Accessor<KaleoTaskAssignmentInstance, Long>() {
			@Override
			public Long get(
				KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance) {
				return kaleoTaskAssignmentInstance.getKaleoTaskAssignmentInstanceId();
			}

			@Override
			public Class<Long> getAttributeClass() {
				return Long.class;
			}

			@Override
			public Class<KaleoTaskAssignmentInstance> getTypeClass() {
				return KaleoTaskAssignmentInstance.class;
			}
		};
}