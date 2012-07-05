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

package com.liferay.portal.kernel.messaging;

import com.liferay.portal.kernel.messaging.sender.MessageSender;
import com.liferay.portal.kernel.messaging.sender.SynchronousMessageSender;
import com.liferay.portal.kernel.security.annotation.AccessControl;
import com.liferay.portal.kernel.security.pacl.PACLConstants;
import com.liferay.portal.kernel.security.pacl.permission.PortalMessageBusPermission;

import java.security.Permission;

/**
 * @author Michael C. Han
 * @author Shuyang Zhou
 */
@AccessControl
public class MessageBusUtil {

	public static void addDestination(Destination destination) {
		_messageBus.addDestination(destination);
	}

	public static Message createResponseMessage(Message requestMessage) {
		Message responseMessage = new Message();

		responseMessage.setDestinationName(
			requestMessage.getResponseDestinationName());
		responseMessage.setResponseId(requestMessage.getResponseId());

		return responseMessage;
	}

	public static Message createResponseMessage(
		Message requestMessage, Object payload) {

		Message responseMessage = createResponseMessage(requestMessage);

		responseMessage.setPayload(payload);

		return responseMessage;
	}

	public static MessageBus getMessageBus() {
		return _messageBus;
	}

	public static MessageSender getMessageSender() {
		return _messageSender;
	}

	public static boolean hasMessageListener(String destination) {
		return _messageBus.hasMessageListener(destination);
	}

	public static void init(
		MessageBus messageBus, MessageSender messageSender,
		SynchronousMessageSender synchronousMessageSender) {

		_messageBus = messageBus;
		_messageSender = messageSender;
		_synchronousMessageSender = synchronousMessageSender;
	}

	public static void registerMessageListener(
		String destinationName, MessageListener messageListener) {

		_messageBus.registerMessageListener(destinationName, messageListener);
	}

	public static void removeDestination(String destinationName) {
		_messageBus.removeDestination(destinationName);
	}

	public static void sendMessage(String destinationName, Message message) {
		SecurityManager securityManager = System.getSecurityManager();

		if (securityManager != null) {
			Permission permission = new PortalMessageBusPermission(
				PACLConstants.PORTAL_MESSAGE_BUS_PERMISSION_SEND,
				destinationName);

			securityManager.checkPermission(permission);
		}

		_messageBus.sendMessage(destinationName, message);
	}

	public static void sendMessage(String destinationName, Object payload) {
		Message message = new Message();

		message.setPayload(payload);

		sendMessage(destinationName, message);
	}

	public static Object sendSynchronousMessage(
			String destinationName, Message message)
		throws MessageBusException {

		return _synchronousMessageSender.send(destinationName, message);
	}

	public static Object sendSynchronousMessage(
			String destinationName, Message message, long timeout)
		throws MessageBusException {

		return _synchronousMessageSender.send(
			destinationName, message, timeout);
	}

	public static Object sendSynchronousMessage(
			String destinationName, Object payload)
		throws MessageBusException {

		return sendSynchronousMessage(destinationName, payload, null);
	}

	public static Object sendSynchronousMessage(
			String destinationName, Object payload, long timeout)
		throws MessageBusException {

		return sendSynchronousMessage(destinationName, payload, null, timeout);
	}

	public static Object sendSynchronousMessage(
			String destinationName, Object payload,
			String responseDestinationName)
		throws MessageBusException {

		Message message = new Message();

		message.setResponseDestinationName(responseDestinationName);
		message.setPayload(payload);

		return sendSynchronousMessage(destinationName, message);
	}

	public static Object sendSynchronousMessage(
			String destinationName, Object payload,
			String responseDestinationName, long timeout)
		throws MessageBusException {

		Message message = new Message();

		message.setResponseDestinationName(responseDestinationName);
		message.setPayload(payload);

		return sendSynchronousMessage(destinationName, message, timeout);
	}

	public static void shutdown() {
		_messageBus.shutdown();
	}

	public static void shutdown(boolean force) {
		_messageBus.shutdown(force);
	}

	public static boolean unregisterMessageListener(
		String destinationName, MessageListener messageListener) {

		return _messageBus.unregisterMessageListener(
			destinationName, messageListener);
	}

	private static MessageBus _messageBus;
	private static MessageSender _messageSender;
	private static SynchronousMessageSender _synchronousMessageSender;

}