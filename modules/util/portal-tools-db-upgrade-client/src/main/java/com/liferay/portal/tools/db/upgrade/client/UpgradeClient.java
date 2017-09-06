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

package com.liferay.portal.tools.db.upgrade.client;

import com.liferay.portal.tools.db.upgrade.client.util.GogoTelnetClient;
import com.liferay.portal.tools.db.upgrade.client.util.Properties;
import com.liferay.portal.tools.db.upgrade.client.util.TeePrintStream;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;

import java.net.URISyntaxException;
import java.net.URL;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.security.CodeSource;
import java.security.ProtectionDomain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jline.console.ConsoleReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author David Truong
 */
public class UpgradeClient {

	public static void main(String[] args) {
		try {
			Options options = _getOptions();

			CommandLineParser commandLineParser = new DefaultParser();

			CommandLine commandLine = commandLineParser.parse(options, args);

			if (commandLine.hasOption("help")) {
				HelpFormatter helpFormatter = new HelpFormatter();

				helpFormatter.printHelp(
					"Liferay Portal Tools Database Upgrade Client", options);

				return;
			}

			String jvmOpts = null;

			if (commandLine.hasOption("jvm-opts")) {
				jvmOpts = commandLine.getOptionValue("jvm-opts");
			}
			else {
				jvmOpts =
					"-Dfile.encoding=UTF8 -Duser.country=US " +
						"-Duser.language=en -Duser.timezone=GMT -Xmx2048m ";
			}

			File logFile = null;

			if (commandLine.hasOption("log-file")) {
				logFile = new File(
					_jarDir, commandLine.getOptionValue("log-file"));
			}
			else {
				logFile = new File(_jarDir, "upgrade.log");
			}

			if (logFile.exists()) {
				String logFileName = logFile.getName();

				logFile.renameTo(
					new File(
						_jarDir, logFileName + "." + logFile.lastModified()));

				logFile = new File(_jarDir, logFileName);
			}

			boolean shell = false;

			if (commandLine.hasOption("shell")) {
				shell = true;
			}

			UpgradeClient upgradeClient = new UpgradeClient(
				jvmOpts, logFile, shell);

			upgradeClient.upgrade();
		}
		catch (ParseException pe) {
			System.err.println("Unable to parse command line properties");

			pe.printStackTrace();
		}
		catch (Exception e) {
			System.err.println("Error running upgrade");

			e.printStackTrace();
		}
	}

	public UpgradeClient(String jvmOpts, File logFile, boolean shell)
		throws IOException {

		_jvmOpts = jvmOpts;
		_logFile = logFile;
		_shell = shell;

		_appServerPropertiesFile = new File(_jarDir, "app-server.properties");

		_appServerProperties = _readProperties(_appServerPropertiesFile);

		_portalUpgradeDatabasePropertiesFile = new File(
			_jarDir, "portal-upgrade-database.properties");

		_portalUpgradeDatabaseProperties = _readProperties(
			_portalUpgradeDatabasePropertiesFile);

		_portalUpgradeExtPropertiesFile = new File(
			_jarDir, "portal-upgrade-ext.properties");

		_portalUpgradeExtProperties = _readProperties(
			_portalUpgradeExtPropertiesFile);
	}

	public void upgrade() throws IOException {
		verifyProperties();

		System.setOut(
			new TeePrintStream(new FileOutputStream(_logFile), System.out));

		ProcessBuilder processBuilder = new ProcessBuilder();

		List<String> commands = new ArrayList<>();

		if (_JAVA_HOME != null) {
			commands.add(_JAVA_HOME + "/bin/java");
		}
		else {
			commands.add("java");
		}

		commands.add("-cp");
		commands.add(_getBootstrapClassPath());

		Collections.addAll(commands, _jvmOpts.split(" "));

		commands.add("-Dexternal-properties=portal-upgrade.properties");
		commands.add(
			"-Dserver.detector.server.id=" +
				_appServer.getServerDetectorServerId());
		commands.add(DBUpgraderLauncher.class.getName());

		processBuilder.command(commands);
		processBuilder.directory(_jarDir);

		processBuilder.redirectErrorStream(true);

		Process process = processBuilder.start();

		try (ObjectOutputStream bootstrapObjectOutputStream =
				new ObjectOutputStream(process.getOutputStream());
			InputStreamReader inputStreamReader = new InputStreamReader(
				process.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(
				inputStreamReader)) {

			bootstrapObjectOutputStream.writeObject(_getClassPath());

			bootstrapObjectOutputStream.flush();

			String line = null;

			while ((line = bufferedReader.readLine()) != null) {
				if (line.equals(
						"Running modules upgrades. Connect to Gogo shell to " +
							"check the status.")) {

					break;
				}
				else {
					System.out.println(line);
				}
			}

			System.out.flush();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}

		try (GogoTelnetClient gogoTelnetClient = new GogoTelnetClient()) {
			if (_shell || !_isFinished(gogoTelnetClient)) {
				System.out.println("You are connected to Gogo shell.");

				_printHelp();

				_consoleReader.setPrompt("g! ");

				String line;

				while ((line = _consoleReader.readLine()) != null) {
					if (line.equals("exit") || line.equals("quit")) {
						break;
					}
					else {
						System.out.println(gogoTelnetClient.send(line));
					}
				}
			}
		}
		catch (Exception e) {
		}

		_close(process.getErrorStream());
		_close(process.getInputStream());
		_close(process.getOutputStream());

		process.destroy();
	}

	public void verifyProperties() {
		try {
			_verifyAppServerProperties();
			_verifyPortalUpgradeDatabaseProperties();
			_verifyPortalUpgradeExtProperties();

			_saveProperties();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static Options _getOptions() {
		Options options = new Options();

		options.addOption(
			new Option("h", "help", false, "Print this message."));
		options.addOption(
			new Option(
				"j", "jvm-opts", true,
				"Set the JVM_OPTS used for the upgrade."));
		options.addOption(
			new Option("l", "log-file", true, "Set the name of log file."));
		options.addOption(
			new Option(
				"s", "shell", false, "Automatically connect to GoGo shell."));

		return options;
	}

	private void _appendClassPath(StringBuilder sb, File dir)
		throws IOException {

		if (dir.exists() && dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				String fileName = file.getName();

				if (file.isFile() && fileName.endsWith("jar")) {
					sb.append(file.getCanonicalPath());
					sb.append(File.pathSeparator);
				}
				else if (file.isDirectory()) {
					_appendClassPath(sb, file);
				}
			}
		}
	}

	private void _appendClassPath(StringBuilder sb, List<File> dirs)
		throws IOException {

		for (File dir : dirs) {
			_appendClassPath(sb, dir);
		}
	}

	private void _close(Closeable closeable) throws IOException {
		closeable.close();
	}

	private String _getBootstrapClassPath() throws IOException {
		StringBuilder sb = new StringBuilder();

		_appendClassPath(sb, _jarDir);

		return sb.toString();
	}

	private String _getClassPath() throws IOException {
		StringBuilder sb = new StringBuilder();

		String liferayClassPath = System.getenv("LIFERAY_CLASSPATH");

		if ((liferayClassPath != null) && !liferayClassPath.isEmpty()) {
			sb.append(liferayClassPath);
			sb.append(File.pathSeparator);
		}

		_appendClassPath(sb, new File(_jarDir, "lib"));
		_appendClassPath(sb, _jarDir);
		_appendClassPath(sb, _appServer.getGlobalLibDir());
		_appendClassPath(sb, _appServer.getExtraLibDirs());

		sb.append(_appServer.getPortalClassesDir());
		sb.append(File.pathSeparator);

		_appendClassPath(sb, _appServer.getPortalLibDir());

		return sb.toString();
	}

	private boolean _isFinished(GogoTelnetClient gogoTelnetClient)
		throws IOException {

		System.out.print("Checking to see if all upgrades have completed...");

		String upgradeCheck = gogoTelnetClient.send("upgrade:check");

		String upgradeSteps = gogoTelnetClient.send(
			"upgrade:list | grep Registered | grep step");

		if (!upgradeCheck.equals("upgrade:check") ||
			upgradeSteps.contains("true")) {

			System.out.println(
				" your upgrades have failed, have not started, or are still " +
					"running.");

			return false;
		}
		else {
			System.out.println(" done.");

			return true;
		}
	}

	private void _printHelp() {
		System.out.println(
			"\nType \"help\" to get available upgrade and verify commands.");

		System.out.println(
			"Type \"help {command}\" to get additional information about the " +
				"command. For example, \"help upgrade:list\".");

		System.out.println("Enter \"exit\" or \"quit\" to exit.");
	}

	private Properties _readProperties(File file) {
		Properties properties = new Properties();

		if (file.exists()) {
			try {
				properties.load(file);
			}
			catch (IOException ioe) {
				System.err.println("Unable to load " + file);
			}
		}

		return properties;
	}

	private void _saveProperties() throws IOException {
		_appServerProperties.store(_appServerPropertiesFile);
		_portalUpgradeDatabaseProperties.store(
			_portalUpgradeDatabasePropertiesFile);
		_portalUpgradeExtProperties.store(_portalUpgradeExtPropertiesFile);
	}

	private void _verifyAppServerProperties() throws IOException {
		String value = _appServerProperties.getProperty(
			"server.detector.server.id");

		if ((value == null) || value.isEmpty()) {
			String response = null;

			while (_appServer == null) {
				System.out.print("[ ");

				for (String appServer : _appServers.keySet()) {
					System.out.print(appServer + " ");
				}

				System.out.println("]");
				System.out.println(
					"Please enter your application server (tomcat): ");

				response = _consoleReader.readLine();

				if (response.isEmpty()) {
					response = "tomcat";
				}

				_appServer = _appServers.get(response);

				if (_appServer == null) {
					System.err.println(
						response + " is an unsupported application server.");
				}
			}

			File dir = _appServer.getDir();

			System.out.println(
				"Please enter your application server directory (" + dir +
					"): ");

			response = _consoleReader.readLine();

			if (!response.isEmpty()) {
				_appServer.setDirName(response);
			}

			System.out.println(
				"Please enter your extra library directories in application " +
					"server directory (" + _appServer.getExtraLibDirNames() +
						"): ");

			response = _consoleReader.readLine();

			if (!response.isEmpty()) {
				_appServer.setExtraLibDirNames(response);
			}

			System.out.println(
				"Please enter your global library directory in application " +
					"server directory (" + _appServer.getGlobalLibDirName() +
						"): ");

			response = _consoleReader.readLine();

			if (!response.isEmpty()) {
				_appServer.setGlobalLibDirName(response);
			}

			System.out.println(
				"Please enter your portal directory in application server " +
					"directory (" + _appServer.getPortalDirName() + "): ");

			response = _consoleReader.readLine();

			if (!response.isEmpty()) {
				_appServer.setPortalDirName(response);
			}

			_appServerProperties.setProperty("dir", dir.getCanonicalPath());
			_appServerProperties.setProperty(
				"extra.lib.dirs", _appServer.getExtraLibDirNames());
			_appServerProperties.setProperty(
				"global.lib.dir", _appServer.getGlobalLibDirName());
			_appServerProperties.setProperty(
				"portal.dir", _appServer.getPortalDirName());
			_appServerProperties.setProperty(
				"server.detector.server.id",
				_appServer.getServerDetectorServerId());
		}
		else {
			String dirString = _appServerProperties.getProperty("dir");

			File dirFile = new File(dirString);

			if (!dirFile.isAbsolute()) {
				dirFile = new File(_jarDir, dirString);
			}

			dirString = dirFile.getCanonicalPath();

			_appServerProperties.setProperty("dir", dirString);

			_appServer = new AppServer(
				dirString, _appServerProperties.getProperty("extra.lib.dirs"),
				_appServerProperties.getProperty("global.lib.dir"),
				_appServerProperties.getProperty("portal.dir"), value);
		}
	}

	private void _verifyPortalUpgradeDatabaseProperties() throws IOException {
		String value = _portalUpgradeDatabaseProperties.getProperty(
			"jdbc.default.driverClassName");

		if ((value == null) || value.isEmpty()) {
			String response = null;

			Database dataSource = null;

			while (dataSource == null) {
				System.out.print("[ ");

				for (String database : _databases.keySet()) {
					System.out.print(database + " ");
				}

				System.out.println("]");

				System.out.println("Please enter your database (mysql): ");

				response = _consoleReader.readLine();

				if (response.isEmpty()) {
					response = "mysql";
				}

				dataSource = _databases.get(response);

				if (dataSource == null) {
					System.err.println(
						response + " is an unsupported database.");
				}
			}

			System.out.println(
				"Please enter your database JDBC driver class name (" +
					dataSource.getClassName() + "): ");

			response = _consoleReader.readLine();

			if (!response.isEmpty()) {
				dataSource.setClassName(response);
			}

			System.out.println(
				"Please enter your database JDBC driver protocol (" +
					dataSource.getProtocol() + "): ");

			response = _consoleReader.readLine();

			if (!response.isEmpty()) {
				dataSource.setProtocol(response);
			}

			System.out.println(
				"Please enter your database host (" + dataSource.getHost() +
					"): ");

			response = _consoleReader.readLine();

			if (!response.isEmpty()) {
				dataSource.setHost(response);
			}

			String port = null;

			if (dataSource.getPort() > 0) {
				port = String.valueOf(dataSource.getPort());
			}
			else {
				port = "none";
			}

			System.out.println(
				"Please enter your database port (" + port + "): ");

			response = _consoleReader.readLine();

			if (!response.isEmpty()) {
				if (response.equals("none")) {
					dataSource.setPort(0);
				}
				else {
					dataSource.setPort(Integer.parseInt(response));
				}
			}

			System.out.println(
				"Please enter your database name (" +
					dataSource.getDatabaseName() + "): ");

			response = _consoleReader.readLine();

			if (!response.isEmpty()) {
				dataSource.setDatabaseName(response);
			}

			System.out.println("Please enter your database username: ");

			String username = _consoleReader.readLine();

			System.out.println("Please enter your database password: ");

			String password = _consoleReader.readLine('*');

			_portalUpgradeDatabaseProperties.setProperty(
				"jdbc.default.driverClassName", dataSource.getClassName());
			_portalUpgradeDatabaseProperties.setProperty(
				"jdbc.default.password", password);
			_portalUpgradeDatabaseProperties.setProperty(
				"jdbc.default.url", dataSource.getURL());
			_portalUpgradeDatabaseProperties.setProperty(
				"jdbc.default.username", username);
		}
	}

	private void _verifyPortalUpgradeExtProperties() throws IOException {
		String value = _portalUpgradeExtProperties.getProperty("liferay.home");

		File baseDir = new File(".");

		if ((value == null) || value.isEmpty()) {
			File defaultLiferayHomeDir = new File(_jarDir, "../../");

			System.out.println(
				"Please enter your Liferay home (" +
					defaultLiferayHomeDir.getCanonicalPath() + "): ");

			value = _consoleReader.readLine();

			if (value.isEmpty()) {
				value = defaultLiferayHomeDir.getCanonicalPath();
			}
		}
		else {
			baseDir = _jarDir;
		}

		File liferayHome = new File(value);

		if (!liferayHome.isAbsolute()) {
			liferayHome = new File(baseDir, value);
		}

		_portalUpgradeExtProperties.setProperty(
			"liferay.home", liferayHome.getCanonicalPath());
	}

	private static final String _JAVA_HOME = System.getenv("JAVA_HOME");

	private static final Map<String, AppServer> _appServers =
		new LinkedHashMap<>();
	private static final Map<String, Database> _databases =
		new LinkedHashMap<>();
	private static File _jarDir;

	static {
		_appServers.put("jboss", AppServer.getJBossEAPAppServer());
		_appServers.put("jonas", AppServer.getJOnASAppServer());
		_appServers.put("resin", AppServer.getResinAppServer());
		_appServers.put("tcserver", AppServer.getTCServerAppServer());
		_appServers.put("tomcat", AppServer.getTomcatAppServer());
		_appServers.put("weblogic", AppServer.getWebLogicAppServer());
		_appServers.put("websphere", AppServer.getWebSphereAppServer());
		_appServers.put("wildfly", AppServer.getWildFlyAppServer());

		_databases.put("db2", Database.getDB2Database());
		_databases.put("mariadb", Database.getMariaDBDatabase());
		_databases.put("mysql", Database.getMySQLDatabase());
		_databases.put("oracle", Database.getOracleDataSource());
		_databases.put("postgresql", Database.getPostgreSQLDatabase());
		_databases.put("sqlserver", Database.getSQLServerDatabase());
		_databases.put("sybase", Database.getSybaseDatabase());

		ProtectionDomain protectionDomain =
			UpgradeClient.class.getProtectionDomain();

		CodeSource codeSource = protectionDomain.getCodeSource();

		URL url = codeSource.getLocation();

		try {
			Path path = Paths.get(url.toURI());

			File jarFile = path.toFile();

			_jarDir = jarFile.getParentFile();
		}
		catch (URISyntaxException urise) {
			throw new ExceptionInInitializerError(urise);
		}
	}

	private AppServer _appServer;
	private final Properties _appServerProperties;
	private final File _appServerPropertiesFile;
	private final ConsoleReader _consoleReader = new ConsoleReader();
	private final String _jvmOpts;
	private final File _logFile;
	private final Properties _portalUpgradeDatabaseProperties;
	private final File _portalUpgradeDatabasePropertiesFile;
	private final Properties _portalUpgradeExtProperties;
	private final File _portalUpgradeExtPropertiesFile;
	private final boolean _shell;

}