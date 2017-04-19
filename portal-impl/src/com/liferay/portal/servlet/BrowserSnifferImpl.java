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

package com.liferay.portal.servlet;

import com.liferay.portal.kernel.security.pacl.DoPrivileged;
import com.liferay.portal.kernel.servlet.BrowserSniffer;
import com.liferay.portal.kernel.servlet.BrowserType;
import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.util.CharPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import javax.servlet.http.HttpServletRequest;

/**
 * See http://www.zytrax.com/tech/web/browser_ids.htm for examples.
 *
 * @author Eduardo Lundgren
 * @author Nate Cavanaugh
 */
@DoPrivileged
public class BrowserSnifferImpl implements BrowserSniffer {

	@Override
	public boolean acceptsGzip(HttpServletRequest request) {
		String acceptEncoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);

		if ((acceptEncoding != null) && acceptEncoding.contains("gzip")) {
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String getBrowserId(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		if (browserType.isIe()) {
			return BROWSER_ID_IE;
		}
		else if (browserType.isFirefox()) {
			return BROWSER_ID_FIREFOX;
		}
		else {
			return BROWSER_ID_OTHER;
		}
	}

	@Override
	public BrowserType getBrowserType(HttpServletRequest request) {
		return new BrowserType(getUserAgent(request));
	}

	@Override
	public float getMajorVersion(HttpServletRequest request) {
		return GetterUtil.getFloat(getVersion(request));
	}

	@Override
	public String getRevision(HttpServletRequest request) {
		String revision = (String)request.getAttribute(
			WebKeys.BROWSER_SNIFFER_REVISION);

		if (revision != null) {
			return revision;
		}

		revision = parseVersion(
			getUserAgent(request), revisionLeadings, revisionSeparators);

		request.setAttribute(WebKeys.BROWSER_SNIFFER_REVISION, revision);

		return revision;
	}

	@Override
	public String getVersion(HttpServletRequest request) {
		String version = (String)request.getAttribute(
			WebKeys.BROWSER_SNIFFER_VERSION);

		if (version != null) {
			return version;
		}

		String userAgent = getUserAgent(request);

		version = parseVersion(userAgent, versionLeadings, versionSeparators);

		if (version.isEmpty()) {
			version = parseVersion(
				userAgent, revisionLeadings, revisionSeparators);
		}

		request.setAttribute(WebKeys.BROWSER_SNIFFER_VERSION, version);

		return version;
	}

	@Override
	public boolean isAir(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isAir();
	}

	@Override
	public boolean isAndroid(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isAndroid();
	}

	@Override
	public boolean isChrome(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isChrome();
	}

	@Override
	public boolean isFirefox(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isFirefox();
	}

	@Override
	public boolean isGecko(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isGecko();
	}

	@Override
	public boolean isIe(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isIe();
	}

	@Override
	public boolean isIeOnWin32(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isIeOnWin32();
	}

	@Override
	public boolean isIeOnWin64(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isIeOnWin64();
	}

	@Override
	public boolean isIphone(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isIphone();
	}

	@Override
	public boolean isLinux(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isLinux();
	}

	@Override
	public boolean isMac(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isMac();
	}

	@Override
	public boolean isMobile(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isMobile();
	}

	@Override
	public boolean isMozilla(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isMozilla();
	}

	@Override
	public boolean isOpera(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isOpera();
	}

	@Override
	public boolean isRtf(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isRtf(getVersion(request));
	}

	@Override
	public boolean isSafari(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isSafari();
	}

	@Override
	public boolean isSun(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isSun();
	}

	@Override
	public boolean isWebKit(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isWebKit();
	}

	@Override
	public boolean isWindows(HttpServletRequest request) {
		BrowserType browserType = getBrowserType(request);

		return browserType.isWindows();
	}

	protected static String parseVersion(
		String userAgent, String[] leadings, char[] separators) {

		leading:
		for (String leading : leadings) {
			int index = 0;

			version:
			while (true) {
				index = userAgent.indexOf(leading, index);

				if ((index < 0) ||
					(((index += leading.length()) + 2) > userAgent.length())) {

					continue leading;
				}

				char c1 = userAgent.charAt(index);
				char c2 = userAgent.charAt(++index);

				if (((c2 >= '0') && (c2 <= '9')) || (c2 == '.')) {
					for (char separator : separators) {
						if (c1 == separator) {
							break version;
						}
					}
				}
			}

			// Major

			int majorStart = index;
			int majorEnd = index + 1;

			for (int i = majorStart; i < userAgent.length(); i++) {
				char c = userAgent.charAt(i);

				if ((c < '0') || (c > '9')) {
					majorEnd = i;

					break;
				}
			}

			String major = userAgent.substring(majorStart, majorEnd);

			if (userAgent.charAt(majorEnd) != '.') {
				return major;
			}

			// Minor

			int minorStart = majorEnd + 1;
			int minorEnd = userAgent.length();

			for (int i = minorStart; i < userAgent.length(); i++) {
				char c = userAgent.charAt(i);

				if ((c < '0') || (c > '9')) {
					minorEnd = i;

					break;
				}
			}

			String minor = userAgent.substring(minorStart, minorEnd);

			return major.concat(StringPool.PERIOD).concat(minor);
		}

		return StringPool.BLANK;
	}

	protected String getAccept(HttpServletRequest request) {
		String accept = StringPool.BLANK;

		if (request == null) {
			return accept;
		}

		accept = String.valueOf(request.getAttribute(HttpHeaders.ACCEPT));

		if (Validator.isNotNull(accept)) {
			return accept;
		}

		accept = request.getHeader(HttpHeaders.ACCEPT);

		if (accept != null) {
			accept = StringUtil.toLowerCase(accept);
		}
		else {
			accept = StringPool.BLANK;
		}

		request.setAttribute(HttpHeaders.ACCEPT, accept);

		return accept;
	}

	protected String getUserAgent(HttpServletRequest request) {
		if (request == null) {
			return StringPool.BLANK;
		}

		Object userAgentObject = request.getAttribute(HttpHeaders.USER_AGENT);

		if (userAgentObject != null) {
			return userAgentObject.toString();
		}

		String userAgent = request.getHeader(HttpHeaders.USER_AGENT);

		if (userAgent != null) {
			userAgent = StringUtil.toLowerCase(userAgent);
		}
		else {
			userAgent = StringPool.BLANK;
		}

		request.setAttribute(HttpHeaders.USER_AGENT, userAgent);

		return userAgent;
	}

	/**
	 * @deprecated As of 7.0.0, replaced by {@link BrowserType#isIe()}
	 */
	@Deprecated
	protected boolean isIe(String userAgent) {
		BrowserType browserType = new BrowserType(userAgent);

		return browserType.isIe();
	}

	protected static String[] revisionLeadings = {"rv", "it", "ra", "ie"};
	protected static char[] revisionSeparators =
		{CharPool.BACK_SLASH, CharPool.COLON, CharPool.SLASH, CharPool.SPACE};
	protected static String[] versionLeadings =
		{"version", "firefox", "minefield", "chrome"};
	protected static char[] versionSeparators =
		{CharPool.BACK_SLASH, CharPool.SLASH};

}