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

package com.liferay.simple.socks.proxy.manager.process.server;

import com.liferay.portal.kernel.test.CaptureHandler;
import com.liferay.portal.kernel.test.JDKLoggerTestUtil;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.test.rule.CodeCoverageAssertor;
import com.liferay.portal.kernel.util.Time;
import com.liferay.simple.socks.proxy.manager.test.util.SocksProxyTestUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.SocketImplFactory;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author Tom Wang
 */
public class SocksProxyServerTest {

	@ClassRule
	public static final CodeCoverageAssertor codeCoverageAssertor =
		CodeCoverageAssertor.INSTANCE;

	@Before
	public void setUp() throws IOException {
		_port = SocksProxyTestUtil.findOpenPort(8888);
	}

	@Test
	public void testCloseWithoutStart() throws Exception {
		SocksProxyServer socksProxyServer = new SocksProxyServer(
			Collections.emptyList(), 10 * Time.MINUTE, _port);

		socksProxyServer.close();
	}

	@Test
	public void testNormalStart() throws Exception {
		SocksProxyServer socksProxyServer = new SocksProxyServer(
			Collections.emptyList(), 10 * Time.MINUTE, _port);

		try (CaptureHandler captureHandler =
				JDKLoggerTestUtil.configureJDKLogger(
					SocksProxyConnection.class.getName(), Level.ALL)) {

			socksProxyServer.start();

			Thread.sleep(10000);

			socksProxyServer.close();

			List<LogRecord> logRecords = captureHandler.getLogRecords();

			Assert.assertEquals(logRecords.toString(), 0, logRecords.size());
		}
	}

	@Test
	public void testServerSocketExceptions() throws Exception {
		SocketImplFactory socketImplFactory = new SocketImplFactory() {

			@Override
			public SocketImpl createSocketImpl() {
				return new SocketImplSetOptionWrap();
			}

		};

		ServerSocket.setSocketFactory(socketImplFactory);

		SocksProxyServer socksProxyServer = new SocksProxyServer(
			Collections.emptyList(), 10 * Time.MINUTE, _port);

		try (CaptureHandler captureHandler =
				JDKLoggerTestUtil.configureJDKLogger(
					SocksProxyServer.class.getName(), Level.ALL)) {

			socksProxyServer.start();

			while (true) {
				try (Socket socket = new Socket("localhost", _port)) {
					break;
				}
				catch (ConnectException ce) {
				}
			}

			socksProxyServer.close();

			List<LogRecord> logRecords = captureHandler.getLogRecords();

			Assert.assertEquals(logRecords.toString(), 1, logRecords.size());

			LogRecord logRecord = logRecords.get(0);

			Assert.assertEquals(
				"Unable to set server socket so timeout",
				logRecord.getMessage());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				ServerSocket.class, "factory", null);
		}
	}

	private int _port;

	@Test
	public void testSocketExceptions() throws Exception {
		SocketImplFactory socketImplFactory = new SocketImplFactory() {

			@Override
			public SocketImpl createSocketImpl() {
				return new SocketImplSetOptionWrap();
			}

		};

		Socket.setSocketImplFactory(socketImplFactory);

		SocksProxyServer socksProxyServer = new SocksProxyServer(
			Collections.emptyList(), 10 * Time.MINUTE, _port);

		try (CaptureHandler captureHandler =
				JDKLoggerTestUtil.configureJDKLogger(
					SocksProxyServer.class.getName(), Level.ALL)) {

			socksProxyServer.start();

			while (true) {
				try (Socket socket = new Socket("localhost", _port)) {
					break;
				}
				catch (ConnectException ce) {
				}
			}

//			Thread.sleep(10000);
//
//			socksProxyServer.close();

			List<LogRecord> logRecords = captureHandler.getLogRecords();

			Assert.assertEquals(logRecords.toString(), 1, logRecords.size());

			LogRecord logRecord = logRecords.get(0);

			Assert.assertEquals(
				"Unable to set server socket so timeout",
				logRecord.getMessage());
		}
		finally {
			ReflectionTestUtil.setFieldValue(
				ServerSocket.class, "factory", null);
		}
	}

//	@Test
//	public void testServerSocketAcceptException() throws Exception {
//		SocketImplFactory socketImplFactory = new SocketImplFactory() {
//
//			@Override
//			public SocketImpl createSocketImpl() {
//				return new SocketImplAcceptWrap();
//			}
//
//		};
//
//		ServerSocket.setSocketFactory(socketImplFactory);
//
//		SocksProxyServer socksProxyServer = new SocksProxyServer(
//			Collections.emptyList(), 10 * Time.MINUTE, _port);
//
//		try (CaptureHandler captureHandler =
//				JDKLoggerTestUtil.configureJDKLogger(
//					SocksProxyServer.class.getName(), Level.ALL)) {
//
//			socksProxyServer.start();
//
//			Thread.sleep(10000);
//
//			socksProxyServer.close();
//
//			List<LogRecord> logRecords = captureHandler.getLogRecords();
//
//			Assert.assertEquals(logRecords.toString(), 1, logRecords.size());
//
//			LogRecord logRecord = logRecords.get(0);
//
//			Assert.assertEquals(
//				"Unable to accept client socket",
//				logRecord.getMessage());
//		}
//		finally {
//			ReflectionTestUtil.setFieldValue(
//				ServerSocket.class, "factory", null);
//		}
//	}

	private class SocketImplAcceptWrap extends SocketImplWrapper {
		@Override
		protected void accept(SocketImpl s) throws IOException {
			throw new IOException();
		}
	}

	private class SocketImplSetOptionWrap extends SocketImplWrapper {
		@Override
		public void setOption(int optID, Object value) throws SocketException {
			throw new SocketException();
		};
	}

	private class SocketImplWrapper extends SocketImpl {
		@Override
		protected void create(boolean stream) throws IOException {
		}

		@Override
		protected void connect(String host, int port) throws IOException {
		}

		@Override
		protected void connect(InetAddress address, int port) throws IOException {
		}

		@Override
		protected void connect(SocketAddress address, int timeout) throws IOException {
		}

		@Override
		protected void bind(InetAddress host, int port) throws IOException {
		}

		@Override
		protected void listen(int backlog) throws IOException {
		}

		@Override
		protected void accept(SocketImpl s) throws IOException {
		}

		@Override
		protected InputStream getInputStream() throws IOException {
			return null;
		}

		@Override
		protected OutputStream getOutputStream() throws IOException {
			return null;
		}

		@Override
		protected int available() throws IOException {
			return -1;
		}

		@Override
		protected void close() throws IOException {
		}

		@Override
		protected void sendUrgentData(int data) throws IOException {
		}

		@Override
		public void setOption(int optID, Object value) throws SocketException {
		}

		@Override
		public Object getOption(int optID) throws SocketException {
			return null;
		}

	}

}