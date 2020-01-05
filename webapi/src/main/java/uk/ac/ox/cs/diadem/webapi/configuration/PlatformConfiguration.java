/*
 * Copyright (c) 2016, OXPath Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the OXPath team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL OXPath Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.ox.cs.diadem.webapi.configuration;

import java.io.File;

import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.util.configuration.ConfigurationObject;
import uk.ac.ox.cs.diadem.util.misc.OSType;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 * @author Ruslan Fayzrakhmanov
 * 19 Nov 2017
 */
public class PlatformConfiguration {
	
	private static final Logger log = LoggerFactory.getLogger(PlatformConfiguration.class);

	public static final String PATH_SEPARATOR = "/";
	public static final String PLATFORM_PATH_TEMPLATE = "webapi/platforms/platform[@os-type=\"%s\"]";
	public static final String PROJECT_HOME_PATH_RELPATH = "home";
	public static final String PROJECT_HOME_REL_RELPATH = "home/@user-home-rel";
	public static final String BROWSER_HOME_RELPATH = "browser/relpath";
	public static final String BROWSER_RUN_FILE_RELPATH = "browser/run-file-relpath";
	public static final String BROWSER_RUN_FILE_PATH = "browser/run-file-path";
	public static final String BROWSER_XVFB_RUN_FILE_RELPATH = "browser/run-xvfb-file-relpath";
	public static final String DISPLAY_SIZE_FILE_RELPATH = "browser/display-size-file-relpath";
	public static final String DOWNLOAD_DIR_RELPATH = "browser/download-dir-relpath";
	
	private final String PLATFORM_PATH;
	
	private final ConfigurationObject config;
	private final OSType osType;
	public OSType getOsType() {
		return osType;
	}
	
	public PlatformConfiguration(ConfigurationObject config, OSType osType) {
		this.config = config;
		this.osType = osType;
		PLATFORM_PATH = String.format(PLATFORM_PATH_TEMPLATE, osType.name());
	}
	
	private String getStringPath(String relPath) {
		return config.getConfiguration().getString(PLATFORM_PATH + PATH_SEPARATOR + relPath);
	}
	
	public String getProjectHomeAbsolutePath() {
		String homePath = getStringPath(PROJECT_HOME_PATH_RELPATH);
		if (homePath == null)
			throw new WebAPIRuntimeException("homePath "+PROJECT_HOME_PATH_RELPATH+" is empty", log);
		boolean userHomeRelative = config.getConfiguration().getBoolean(PLATFORM_PATH + PATH_SEPARATOR + PROJECT_HOME_REL_RELPATH, false);
		if (userHomeRelative)
			return FilenameUtils.separatorsToSystem(System.getProperty("user.home") + File.separator + homePath);
		else
			return FilenameUtils.separatorsToSystem(homePath);
	}
	
	public String getBrowserHomeAbsolutePath() {
		String browserHomeRelPath = getStringPath(BROWSER_HOME_RELPATH);
		if (browserHomeRelPath == null)
			throw new WebAPIRuntimeException("browserHomeRelPath "+BROWSER_HOME_RELPATH+" is empty", log);
	  	String browserHomeAbsolutePath =  FilenameUtils.separatorsToSystem(getProjectHomeAbsolutePath() + File.separator + browserHomeRelPath);
	  	return browserHomeAbsolutePath;
	  }
	
	public String getBrowserRunFileAbsolutePath() {
		String path = getStringPath(BROWSER_RUN_FILE_RELPATH);
		if (path == null) {
			String browserRunFileAbsolutePath = getStringPath(BROWSER_RUN_FILE_PATH);
			if (browserRunFileAbsolutePath == null)
				throw new WebAPIRuntimeException("browserRunFileAbsolutePath "+BROWSER_RUN_FILE_PATH+" is empty", log);
			return FilenameUtils.separatorsToSystem(browserRunFileAbsolutePath);
		}
		else
			return FilenameUtils.separatorsToSystem(getBrowserHomeAbsolutePath() + File.separator + path);
	}
	
	public String getBrowserXvfbRunFileAbsolutePath() {
		String browserXvfbRunFileRelPath = getStringPath(BROWSER_XVFB_RUN_FILE_RELPATH);
		if (browserXvfbRunFileRelPath == null)
			throw new WebAPIRuntimeException("browserXvfbRunFileRelPath "+BROWSER_XVFB_RUN_FILE_RELPATH+" is empty", log);
		return FilenameUtils.separatorsToSystem(
				getBrowserHomeAbsolutePath() + File.separator + browserXvfbRunFileRelPath);
	}
	
	public String getBrowserDisplaySizeFileAbsolutePath() {
		String browserDisplaySizeFileRelPath = getStringPath(DISPLAY_SIZE_FILE_RELPATH);
		if (browserDisplaySizeFileRelPath == null)
			throw new WebAPIRuntimeException("browserDisplaySizeFileRelPath "+DISPLAY_SIZE_FILE_RELPATH+" is empty", log);
		return FilenameUtils.separatorsToSystem(
				getBrowserHomeAbsolutePath() + File.separator + browserDisplaySizeFileRelPath);
	}
	
	public String getDownloadDirAbsolutePath() {
		String downloadDirRelPath = getStringPath(DOWNLOAD_DIR_RELPATH);
		if (downloadDirRelPath == null)
			throw new WebAPIRuntimeException("downloadDirRelPath "+DOWNLOAD_DIR_RELPATH+" is empty", log);
		return FilenameUtils.separatorsToSystem(
				getBrowserHomeAbsolutePath() + File.separator + downloadDirRelPath);
	}

}
