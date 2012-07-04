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

package com.liferay.portal.aspectj;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.process.ClassPathUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.PortalClassLoaderUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.security.lang.PortalSecurityManagerThreadLocal;
import com.liferay.portal.security.pacl.PACLPolicy;
import com.liferay.portal.security.pacl.PACLPolicyManager;
import com.liferay.portal.security.pacl.aspect.AcceptStatus;
import com.liferay.portal.security.pacl.aspect.PACLAspect;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PropsValues;
import com.liferay.util.bean.PortletBeanLocatorUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.security.ProtectionDomain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.lang.Aspects;
import org.aspectj.lang.NoAspectBoundException;
import org.aspectj.weaver.CrosscuttingMembersSet;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.bcel.BcelObjectType;
import org.aspectj.weaver.tools.GeneratedClassHandler;
import org.aspectj.weaver.tools.WeavingAdaptor;

/**
 * @author Shuyang Zhou
 */
public class WeavingClassLoader extends URLClassLoader {

	public static final String ASPECTJ_RT_JAR = "aspectj-rt.jar";

	public WeavingClassLoader(
		ServletContext servletContext, ClassLoader webappClassLoader,
		List<Class<? extends PACLAspect>> aspectClasses) {

		super(new URL[0], webappClassLoader.getParent());

		_webappClassLoader = webappClassLoader;

		for (Class<? extends PACLAspect> aspectClass : aspectClasses) {
			if (!PACLAspect.class.isAssignableFrom(aspectClass)) {
				throw new IllegalArgumentException(
					"Aspect class is not type of " + PACLAspect.class);
			}

			try {
				PACLAspect aspectInstance = aspectClass.newInstance();

				_aspectCheckers.put(aspectClass, aspectInstance);
			}
			catch (Exception e) {
				throw new IllegalArgumentException(
					"Unable to create default instance for " + aspectClass, e);
			}

			_aspectClasses.put(aspectClass.getName(), aspectClass);
		}

		String docRoot = servletContext.getRealPath(StringPool.BLANK);

		String dumpFolder =
			PropsValues.PORTAL_SECURITY_ASPECTJ_WOVEN_DUMP_FOLDER;

		if (Validator.isNotNull(dumpFolder)) {
			dumpFolder = docRoot.concat(dumpFolder);

			_dumpFolder = new File(dumpFolder);

			FileUtil.deltree(_dumpFolder);

			_dumpFolder.mkdirs();

			if (!_dumpFolder.exists() || !_dumpFolder.isDirectory()) {
				throw new IllegalArgumentException(
					"Can not create dump folder " + dumpFolder);
			}
		}
		else {
			_dumpFolder = null;
		}

		_portalClassLoader = PortalClassLoaderUtil.getClassLoader();
		_weavingClassPathURLs = _getClassPathURLs(webappClassLoader, docRoot);
	}

	@Override
	public URL getResource(String name) {
		return _webappClassLoader.getResource(name);
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return _webappClassLoader.getResourceAsStream(name);
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return _webappClassLoader.getResources(name);
	}

	protected synchronized Class<?> loadClass(String name, boolean resolve)
		throws ClassNotFoundException {

		// 1) Aspect class
		Class<?> clazz = _aspectClasses.get(name);

		if (clazz != null) {
			return clazz;
		}

		// 2) AspectJ library class
		if (name.startsWith("org.aspectj.")) {
			return _portalClassLoader.loadClass(name);
		}

		// 3) Local cache
		clazz = _localClassCache.get(name);

		if (clazz == null) {
			// 4) JVM cache
			clazz = findLoadedClass(name);
		}

		if (clazz == null) {
			boolean enabled = PortalSecurityManagerThreadLocal.isEnabled();

			PortalSecurityManagerThreadLocal.setEnabled(false);

			try {
				clazz = _webappClassLoader.loadClass(name);

				if (clazz.getClassLoader() == _webappClassLoader) {
					// 5) Webapp class
					String resourcePath = _getResourcePathFromClassName(name);

					InputStream inputStream =
						_webappClassLoader.getResourceAsStream(resourcePath);

					try {
						byte[] data = FileUtil.getBytes(inputStream);

						clazz = _generateClass(name, data);
					}
					catch (IOException ioe) {
						// This should never happen, as Class is already in
						// memory, there is no way that we can not reload its
						// data as byte[]
						throw new ClassNotFoundException(name, ioe);
					}
				}

				// Exam class for weaving
				PACLPolicy paclPolicy = PACLPolicyManager.getPACLPolicy(this);

				if ((paclPolicy != null) && paclPolicy.isActive()) {
					List<Class<? extends PACLAspect>> aspectClasses =
						new ArrayList<Class<? extends PACLAspect>>();

					boolean rejectClass = true;

					for (Map.Entry
						<Class<? extends PACLAspect>, PACLAspect> entry :
							_aspectCheckers.entrySet()) {

						PACLAspect paclAspect = entry.getValue();

						AcceptStatus acceptStatus = paclAspect.acceptClass(
							paclPolicy, clazz);

						if (acceptStatus != AcceptStatus.FULL_ACCESS) {
							aspectClasses.add(entry.getKey());
						}

						if (acceptStatus != AcceptStatus.REJECT_ACCESS) {
							rejectClass = false;
						}
					}

					if (rejectClass) {
						throw new SecurityException(
							"Undeclared access to class " + clazz);
					}

					if (aspectClasses.isEmpty()) {
						return clazz;
					}

					clazz = weaveClass(clazz, aspectClasses, paclPolicy);
				}
			}
			finally {
				PortalSecurityManagerThreadLocal.setEnabled(enabled);
			}

			_localClassCache.put(name, clazz);
		}

		if (resolve) {
			resolveClass(clazz);
		}

		_localClassCache.put(name, clazz);

		return clazz;
	}

	protected Class<?> weaveClass(
			Class<?> clazz, List<Class<? extends PACLAspect>> aspectClasses,
			PACLPolicy paclPolicy)
		throws ClassNotFoundException {

		String name = clazz.getName();
		String resourcePath = _getResourcePathFromClassName(name);

		InputStream inputStream = _webappClassLoader.getResourceAsStream(
			resourcePath);

		byte[] data = null;

		try {
			if (inputStream == null) {
				// On missing could be a generated inner class
				data = _generatedClasses.remove(name);
			}
			else {
				data = FileUtil.getBytes(inputStream);
			}

			if (data != null) {
				byte[] oldData = data;

				long startTime = System.currentTimeMillis();

				// TODO optimize, try to reuse URLWeavingAdaptor
				WeavingAdaptor weavingAdaptor = new URLWeavingAdaptor(
					_weavingClassPathURLs, aspectClasses);

				data = weavingAdaptor.weaveClass(name, data, false);

				if (!Arrays.equals(oldData, data)) {
					long duration = System.currentTimeMillis() - startTime;

					if (_dumpFolder != null) {
						File dumpFile = new File(_dumpFolder, resourcePath);

						FileUtil.write(dumpFile, data);

						if (_log.isInfoEnabled()) {
							_log.info(
								"Woven class " + name + " with aspects " +
									aspectClasses + " in " + duration + " ms" +
										", dump woven result into " +
											dumpFile.getCanonicalPath());
						}
					}
					else {
						if (_log.isInfoEnabled()) {
							_log.info(
								"Woven class " + name + " with aspects " +
								aspectClasses + " in " + duration + " ms");
						}
					}
				}

				Class<?> wovenClass = _generateClass(name, data);

				for (Class<? extends PACLAspect> aspectClass : aspectClasses) {
					try {
						PACLAspect paclAspect = Aspects.aspectOf(
							aspectClass, wovenClass);

						paclAspect.setPACLPolicy(paclPolicy);
					}
					catch (NoAspectBoundException nabe) {
						_log.error(
							"Aspect " + aspectClass +
								" is not bounded to class " + wovenClass, nabe);
					}
				}

				_copyStaticFields(clazz, wovenClass);

				return wovenClass;
			}
			else {
				throw new ClassNotFoundException(name);
			}
		}
		catch (IOException ioe) {
			throw new ClassNotFoundException(name, ioe);
		}
	}

	private void _copyStaticFields(Class<?> clazz, Class<?> wovenClass) {
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			int modifiers = field.getModifiers();

			if (Modifier.isStatic(modifiers)) {

				try {
					field.setAccessible(true);

					Object obj = field.get(null);

					Field wovenField = wovenClass.getDeclaredField(
						field.getName());

					wovenField.setAccessible(true);

					Field modifiersField = Field.class.getDeclaredField(
						"modifiers");

					modifiersField.setAccessible(true);

					modifiersField.setInt(
						wovenField,
						wovenField.getModifiers() & ~Modifier.FINAL);

					wovenField.set(null, obj);

					if (_log.isDebugEnabled()) {
						_log.debug("Copied field " + field);
					}
				}
				catch (Exception e) {
					if (_log.isWarnEnabled()) {
						_log.warn("Failed to copy static filed " + field, e);
					}
				}
			}
		}
	}

	private Class<?> _generateClass(String name, byte[] data) {
		Class<?> clazz = defineClass(
			name, data, 0, data.length, (ProtectionDomain)null);

		String packageName = null;

		int index = name.lastIndexOf('.');

		if (index != -1) {
			packageName = name.substring(0, index);
		}

		if (packageName != null) {
			Package pkg = getPackage(packageName);

			if (pkg == null) {
				definePackage(
					packageName, null, null, null, null, null, null, null);
			}
		}

		return clazz;
	}

	private URL[] _getClassPathURLs(ClassLoader classLoader, String docRoot) {
		List<File> files = new ArrayList<File>();

		File[] appServerGlobalJarFiles = ClassPathUtil.getClassPathFiles(
			_portalClassLoader, ServletException.class.getName());

		files.addAll(Arrays.asList(appServerGlobalJarFiles));

		File[] portalGlobalJarFiles = ClassPathUtil.getClassPathFiles(
			_portalClassLoader, PortalException.class.getName());

		files.addAll(Arrays.asList(portalGlobalJarFiles));

		File[] pluginJarFiles = ClassPathUtil.getClassPathFiles(
			classLoader, PortletBeanLocatorUtil.class.getName());

		files.addAll(Arrays.asList(pluginJarFiles));

		files.add(new File(docRoot, "WEB-INF/classes"));

		Set<URL> urlSet = new LinkedHashSet<URL>();

		for (File file : files) {
			try {
				URL url = file.toURI().toURL();

				urlSet.add(url);
			}
			catch (MalformedURLException murle) {
				_log.error("Can not get URL for file " + file, murle);
			}
		}

		try {
			URL aspectJRTJarURL = new URL(
				"file:" + PortalUtil.getPortalLibDir() + ASPECTJ_RT_JAR);

			urlSet.add(aspectJRTJarURL);
		}
		catch (MalformedURLException murle) {
			throw new RuntimeException(
				"Can not build URL to " + ASPECTJ_RT_JAR, murle);
		}

		while (classLoader != null) {
			if (classLoader instanceof URLClassLoader) {
				URL[] urls = ((URLClassLoader)classLoader).getURLs();

				urlSet.addAll(Arrays.asList(urls));
			}
			else if (_log.isDebugEnabled()) {
				_log.debug(
					"Can not determine classpath info for non-URLClassLoader " +
					classLoader);
			}

			classLoader = classLoader.getParent();
		}

		String bootClassPath = System.getProperty("sun.boot.class.path");
		String[] classPathElements = StringUtil.split(
			bootClassPath, File.pathSeparator);

		for (String classPathElement : classPathElements) {
			try {
				URL fileURL = new URL("file:" + classPathElement);

				urlSet.add(fileURL);
			}
			catch (MalformedURLException murle) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Can not covert boot class path element " +
							classPathElement + " into a file URL.", murle);
				}
			}

		}

		return urlSet.toArray(new URL[urlSet.size()]);
	}

	private String _getResourcePathFromClassName(String className) {
		String resourcePath = className.replace('.', '/');

		return resourcePath.concat(".class");
	}

	private static Log _log = LogFactoryUtil.getLog(WeavingClassLoader.class);

	private final Map<Class<? extends PACLAspect>, PACLAspect> _aspectCheckers =
		new HashMap<Class<? extends PACLAspect>, PACLAspect>();
	private final Map<String, Class<? extends PACLAspect>> _aspectClasses =
		new HashMap<String, Class<? extends PACLAspect>>();
	private final File _dumpFolder;
	private final Map<String, byte[]> _generatedClasses =
		new HashMap<String, byte[]>();
	private final Map<String, Class<?>> _localClassCache =
		new HashMap<String, Class<?>>();
	private final ClassLoader _portalClassLoader;
	private final URL[] _weavingClassPathURLs;
	private final ClassLoader _webappClassLoader;

	private class RecordGeneratedClassHandler implements GeneratedClassHandler {

		public void acceptClass(String name, byte[] bytes) {
			_generatedClasses.put(name, bytes);
		}

	}

	private class URLWeavingAdaptor extends WeavingAdaptor {

		public URLWeavingAdaptor(
			URL[] classURLs, List<Class<? extends PACLAspect>> aspectClasses) {

			super(null, classURLs, new URL[0]);

			generatedClassHandler = new RecordGeneratedClassHandler();

			for (Class<?> aspectClass : aspectClasses) {
				addAspectClass(aspectClass);
			}

			weaver.prepareForWeave();
		}

		public final void addAspectClass(Class<?> aspectClass) {
			Class<?> currentClass = aspectClass;

			while (true) {
				Class<?>[] interfaceClasses = currentClass.getInterfaces();

				for (Class<?> interfaceClass : interfaceClasses) {
					JavaClass javaClass = _classToJavaClass(interfaceClass);

					if (javaClass != null) {
						bcelWorld.addSourceObjectType(javaClass, false);
					}
				}

				currentClass = currentClass.getSuperclass();

				if (currentClass != null) {
					JavaClass javaClass = _classToJavaClass(currentClass);

					if (javaClass != null) {
						bcelWorld.addSourceObjectType(javaClass, false);
					}
				}
				else {
					break;
				}
			}

			JavaClass javaClass = _classToJavaClass(aspectClass);

			BcelObjectType bcelObjectType = bcelWorld.addSourceObjectType(
				javaClass, false);

			ResolvedType resolvedType = bcelObjectType.getResolvedTypeX();

			if (resolvedType.isAspect()) {
				CrosscuttingMembersSet crosscuttingMembersSet =
					bcelWorld.getCrosscuttingMembersSet();

				crosscuttingMembersSet.addOrReplaceAspect(resolvedType);
			}
			else {
				throw new IllegalArgumentException(
					"Class object " + aspectClass +
						" is not an AspectJ aspect");
			}
		}

		private JavaClass _classToJavaClass(Class<?> aspectClass) {
			ClassLoader aspectClassLoader = aspectClass.getClassLoader();

			if (aspectClassLoader == null) {
				return null;
			}

			String resourcePath = _getResourcePathFromClassName(
				aspectClass.getName());

			ByteArrayInputStream byteArrayInputStream = null;

			InputStream inputStream = aspectClassLoader.getResourceAsStream(
				resourcePath);

			if (inputStream instanceof ByteArrayInputStream) {
				byteArrayInputStream = (ByteArrayInputStream)inputStream;
			}
			else {
				try {
					byte[] classData = FileUtil.getBytes(inputStream);

					byteArrayInputStream = new ByteArrayInputStream(classData);
				}
				catch (IOException ioe) {
					// This should never happen, as Class is already in memory,
					// there is no way that we can not reload its data as byte[]
					throw new RuntimeException(
						"Unable to reload class data", ioe);
				}
			}

			String fileName = aspectClass.getSimpleName().concat(".class");

			ClassParser classParser = new ClassParser(
				byteArrayInputStream, fileName);

			try {
				return classParser.parse();
			}
			catch (Exception e) {
				// This should never happen, as Class is already in memory,
				// there is no way that we can not parse its data to JavaClass
				throw new RuntimeException("Unable to parse class data", e);
			}
		}
	}

}