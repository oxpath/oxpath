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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import uk.ac.ox.cs.diadem.util.configuration.ConfigurationObject;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.impl.WebDriverBrowserImpl;

/**
 * 
 * Adapter for the configuration related to the execution of OXPath.
 * 
 * @author Ruslan Fayzrakhmanov
 * 19 Nov 2017
 */
public class RunConfiguration {
	
	public static final String PATH_SEPARATOR = "/";
	public static final String COLLECT_STATISTICS_PATH = "webapi/run-options/collect-stats";
	public static final String USER_AGENT_PATH = "webapi/run-options/user-agent";
	public static final String ENABLE_PLUGINS_PATH = "webapi/run-options/enable-plugins";
	public static final String DISABLED_CONTENT_TYPES_PATH = "webapi/run-options/disabled-content-types";
	public static final String DISABLED_CONTENT_TYPE_RELPATH = "content-type";
	public static final String DISABLED_THIRDPARTY_CONTENT_TYPES_PATH = "webapi/run-options/disabled-thirdparty-content-types";
	public static final String DISABLED_THIRDPARTY_CONTENT_TYPE_RELPATH = "content-type";
//	public static final String FEATURES_PATH = "webapi/run-options/features";
//	public static final String ENABLED_FEATURE_RELPATH = "feature";
	public static final String USE_XVFB_PATH = "webapi/run-options/xvfb/xvfb-mode";
	public static final String DISPLAY_NUMBER_PATH = "webapi/run-options/xvfb/display-number";
	public static final String USE_DEFAULT_EXECUTABLES_PATH = "webapi/run-options/executables/use-default-executables";
	public static final String USER_EXECUTABLE_PATH = "webapi/run-options/executables/user-executable-path";
	public static final String LATITUDE_PATH = "webapi/run-options/geolocation/latitude";
	public static final String LONGITUDE_PATH = "webapi/run-options/geolocation/longitude";
	public static final String SCRIPT_TIMEOUT_SEC_PATH = "webapi/run-options/timeouts/script-timeout-sec";
	public static final String PAGE_LOAD_SEC_PATH = "webapi/run-options/timeouts/page-load-sec";
	public static final String AUTO_POSITION_PATH = "webapi/run-options/window/auto-position";
	public static final String TOP_LEFT_X_PATH = "webapi/run-options/window/x-min";
	public static final String TOP_LEFT_Y_PATH = "webapi/run-options/window/y-min";
	public static final String WIDTH_PATH = "webapi/run-options/window/width";
	public static final String HEIGHT_PATH = "webapi/run-options/window/height";
	
	private final ConfigurationObject config;
	
	/**
	 * @param config Configuration object, wrapping the original configuration.
	 */
	public RunConfiguration(ConfigurationObject config) {
		this.config = config;
	}
	
	/**
	 * "Collect statistics" attribute.
	 * @return
	 */
	public boolean getCollectStatistics() {
    	return config.getConfiguration().getBoolean(COLLECT_STATISTICS_PATH);
    }
    
    /**
     * "Collect statistics" attribute.
     * 
     * @param collectStats
     * @return
     */
    public RunConfiguration setCollectStatistics(boolean collectStats) {
    	config.getConfiguration().setProperty(COLLECT_STATISTICS_PATH, collectStats);
    	return this;
    }
    
	/**
	 * User-Agent HTTP parameter.
	 * @return
	 */
	public String getUserAgent() {
    	return config.getConfiguration().getString(USER_AGENT_PATH);
    }
	
	/**
	 * User-Agent HTTP parameter.
	 * 
	 * @param userAgent
	 * @return
	 */
	public RunConfiguration setUserAgent(final String userAgent) {
    	config.getConfiguration().setProperty(USER_AGENT_PATH, userAgent);
    	return this;
    }
    
	/**
	 * Content types (see {@linkplain WebBrowser.ContentType}) to be disabled.
	 * @return
	 */
	public List<WebBrowser.ContentType> getDisabledContentTypes() {
		String[] rawFs = config.getConfiguration().getStringArray(DISABLED_CONTENT_TYPES_PATH + PATH_SEPARATOR + DISABLED_CONTENT_TYPE_RELPATH);
		List<WebBrowser.ContentType> fs = new ArrayList<WebBrowser.ContentType>(rawFs.length);
		for (String rawF: rawFs) {
			fs.add(WebBrowser.ContentType.valueOf(rawF));
		}
		return fs;
    }
	
	/**
	 * Content types (see {@linkplain WebBrowser.ContentType}) to be disabled.
	 * 
	 * @param contypes
	 * @return
	 */
	public RunConfiguration setDisabledContentTypes(WebBrowser.ContentType... contypes) {
		return setDisabledContentTypes(Arrays.asList(contypes));
    }
	
	/**
	 * Content types (see {@linkplain WebBrowser.ContentType}) to be disabled.
	 * 
	 * @param contypes
	 * @return
	 */
	public RunConfiguration setDisabledContentTypes(Collection<WebBrowser.ContentType> contypes) {
		config.getConfiguration().clearTree(DISABLED_CONTENT_TYPES_PATH + PATH_SEPARATOR + DISABLED_CONTENT_TYPE_RELPATH);
		for (WebBrowser.ContentType contype : contypes) {
			config.getConfiguration().addProperty(
					DISABLED_CONTENT_TYPES_PATH + " " + DISABLED_CONTENT_TYPE_RELPATH
					, contype.name());
		}
      return this;
    }
	
	/**
     * This is a shortcut for the invocation {@link #setDisabledContentTypes(WebBrowser.ContentType.values()))}.
     * This will block the loading of any additional resource resulting only in the raw HTML of the page.
     *
     * @return
     */
    public RunConfiguration loadOnlyRawHtml() {
      return setDisabledContentTypes(WebBrowser.ContentType.values());
    }
	
	/**
	 * Content types (see {@linkplain WebBrowser.ContentType}) of third-party web pages to be disabled.
	 * @return
	 */
	public List<WebBrowser.ContentType> getDisabledThirdPartyContentTypes() {
		String[] rawFs = config.getConfiguration().getStringArray(
				DISABLED_THIRDPARTY_CONTENT_TYPES_PATH + PATH_SEPARATOR + DISABLED_THIRDPARTY_CONTENT_TYPE_RELPATH);
		List<WebBrowser.ContentType> fs = new ArrayList<WebBrowser.ContentType>(rawFs.length);
		for (String rawF: rawFs) {
			fs.add(WebBrowser.ContentType.valueOf(rawF));
		}
		return fs;
    }
	
	/**
	 * Content types (see {@linkplain WebBrowser.ContentType}) of third-party web pages to be disabled.
	 * Blocks the loading of any additional resource of the given content types coming from third-parties (external
     * websites).
     * 
	 * @param contypes
	 * @return
	 */
	public RunConfiguration setDisabledThirdPartyContentTypes(WebBrowser.ContentType... contypes) {
		return setDisabledContentTypes(Arrays.asList(contypes));
    }
	
	/**
	 * Content types (see {@linkplain WebBrowser.ContentType}) of third-party web pages to be disabled.
	 * Blocks the loading of any additional resource of the given content types coming from third-parties (external
     * websites).
     * 
	 * @param contypes
	 * @return
	 */
	public RunConfiguration setDisabledThirdPartyContentTypes(Collection<WebBrowser.ContentType> contypes) {
		config.getConfiguration().clearTree(
				DISABLED_THIRDPARTY_CONTENT_TYPES_PATH + PATH_SEPARATOR + DISABLED_THIRDPARTY_CONTENT_TYPE_RELPATH);
		for (WebBrowser.ContentType contype : contypes) {
			config.getConfiguration().addProperty(
					DISABLED_THIRDPARTY_CONTENT_TYPES_PATH + " " + DISABLED_THIRDPARTY_CONTENT_TYPE_RELPATH
					, contype.name());
		}
      return this;
    }
	
	/**
     * Shortcut for {@link #setDisabledThirdPartyContentTypes(WebBrowser.ContentType.values())}
     * 
     * @return
     */
    public RunConfiguration doNotLoadAnyThirdPartyContent() {
    	return setDisabledThirdPartyContentTypes(WebBrowser.ContentType.values());
    }
	
	/**
	 * "Enable plug-ins" flag.
	 * @return
	 */
	public boolean getEnablePlugins() {
    	return config.getConfiguration().getBoolean(ENABLE_PLUGINS_PATH);
    }
    
    /**
     * 
     * "Enable plug-ins" flag.
     * 
     * @param enablePlugins
     * @return
     */
    public RunConfiguration setEnablePlugins(boolean enablePlugins) {
    	config.getConfiguration().setProperty(ENABLE_PLUGINS_PATH, enablePlugins);
    	return this;
    }
    
//	public List<WebBrowser.FeatureType> getEnabledFeatures() {
//		String[] rawFs = config.getConfiguration().getStringArray(FEATURES_PATH + PATH_SEPARATOR + ENABLED_FEATURE_RELPATH);
//		List<WebBrowser.FeatureType> fs = new ArrayList<WebBrowser.FeatureType>(rawFs.length);
//		for (String rawF: rawFs) {
//			fs.add(WebBrowser.FeatureType.valueOf(rawF));
//		}
//		return fs;
//    }
//	
//	public RunConfigurationAdapter setEnabledFeatures(WebBrowser.FeatureType... features) {
//		return setEnabledFeatures(Arrays.asList(features));
//    }
//	
//	public RunConfigurationAdapter setEnabledFeatures(Collection<WebBrowser.FeatureType> features) {
//		config.getConfiguration().clearTree(FEATURES_PATH + PATH_SEPARATOR + ENABLED_FEATURE_RELPATH);
//		for (WebBrowser.FeatureType feature : features) {
//			config.getConfiguration().addProperty(
//					FEATURES_PATH + " " + ENABLED_FEATURE_RELPATH
//					, feature.name());
//		}
//      return this;
//    }
	
//	public List<WebBrowser.FeatureType> getDisabledFeatures() {
//		Set<WebBrowser.FeatureType> allFeatures = new HashSet<WebBrowser.FeatureType>(Arrays.asList(WebBrowser.FeatureType.values()));
//		Set<WebBrowser.FeatureType> enabledFeatures = new HashSet<WebBrowser.FeatureType>(getEnabledFeatures());
//		return new ArrayList<WebBrowser.FeatureType>(Sets.difference(allFeatures, enabledFeatures));
//    }
//	
//	public RunConfigurationAdapter setDisabledFeatures(WebBrowser.FeatureType... features) {
//		Set<WebBrowser.FeatureType> enabledFeatures = new HashSet<WebBrowser.FeatureType>(getEnabledFeatures());
//		Set<WebBrowser.FeatureType> disabledFeatures = new HashSet<WebBrowser.FeatureType>(Arrays.asList(features));
//		return setEnabledFeatures(Sets.difference(enabledFeatures, disabledFeatures));
//    }
	
	/**
	 * XVFB Mode.
	 * @return
	 */
	public boolean getXvfbMode() {
    	return config.getConfiguration().getBoolean(USE_XVFB_PATH);
    }
	
    /**
     * XVFB Mode.
     * 
     * @param useXvfb
     * @return
     */
    public RunConfiguration setXvfbMode(boolean useXvfb) {
    	config.getConfiguration().setProperty(USE_XVFB_PATH, useXvfb);
    	return this;
    }
    
    /**
     * Display number for the XVFB mode enabled.
     * @return
     */
    public String getDisplayNumber() {
    	return config.getConfiguration().getString(DISPLAY_NUMBER_PATH);
    }
    
    /**
     * Display number for the XVFB mode enabled.
     * @param displayNumber
     * @return
     */
    public RunConfiguration setDisplayNumber(final String displayNumber) {
    	config.getConfiguration().setProperty(DISPLAY_NUMBER_PATH, displayNumber);
    	return this;
    }
    
    /**
     * Use default browser executables (binaries).
     * @return
     */
    public boolean getUseDefaultExecutables() {
    	return config.getConfiguration().getBoolean(USE_DEFAULT_EXECUTABLES_PATH);
    }
    
    /**
     * Use default browser executables (binaries).
     * 
     * @param useDefaultExecutable
     * 				{@code true}: Forces the usage of the provided browser binary,
     * 				{@code false}: user default executables.
     * @return
     */
    public RunConfiguration setUseDefaultExecutables(boolean useDefaultExecutable) {
    	config.getConfiguration().setProperty(USE_DEFAULT_EXECUTABLES_PATH, useDefaultExecutable);
    	return this;
    }
    
    /**
     * User-provided browser executables (binaries).
     * @return
     */
    public String getUserExecutable() {
    	return config.getConfiguration().getString(USER_EXECUTABLE_PATH, null);
    }
    
    /**
     * User-provided browser executables (binaries).
     *
     * @param binaryPath
     * @return
     */
    public RunConfiguration setUserExecutable(final String binaryPath) {
    	config.getConfiguration().setProperty(USER_EXECUTABLE_PATH, binaryPath);
    	return this;
    }
    
	/**
	 * Geolocation.
	 * @return
	 */
	public WebDriverBrowserImpl.GeoLocation getGeoLocation() {
		Double lat = config.getConfiguration().getDouble(LATITUDE_PATH, null);
		Double lon = config.getConfiguration().getDouble(LONGITUDE_PATH, null);
		return (lat!=null && lon!=null)? new WebDriverBrowserImpl.GeoLocation(lat, lon): null;
	}
	
	/**
	 * Specify a Geographical location.
	 * 
	 * @param location
	 * @return
	 */
	public RunConfiguration setGeoLocation(WebDriverBrowserImpl.GeoLocation location) {
		return setGeoLocation(location.getLatitude(), location.getLongitude());
	}
	
	/**
	 * Specify a Geographical location.
	 * 
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public RunConfiguration setGeoLocation(double latitude, double longitude) {
		config.getConfiguration().setProperty(LATITUDE_PATH, latitude);
		config.getConfiguration().setProperty(LONGITUDE_PATH, longitude);
		return this;
	}
	
	/**
	 * @return
	 */
	public int getScriptTimeoutSec() {
    	return config.getConfiguration().getInt(SCRIPT_TIMEOUT_SEC_PATH);
    }
    
    /**
     * @param scriptTimeoutSec
     * @return
     */
    public RunConfiguration setScriptTimeoutSec(int scriptTimeoutSec) {
    	config.getConfiguration().setProperty(SCRIPT_TIMEOUT_SEC_PATH, scriptTimeoutSec);
    	return this;
    }
    
	/**
	 * @return
	 */
	public int getPageLoadTimeoutSec() {
    	return config.getConfiguration().getInt(PAGE_LOAD_SEC_PATH);
    }
    
    /**
     * @param pageLoadTimeoutSec
     * @return
     */
    public RunConfiguration setPageLoadTimeoutSec(int pageLoadTimeoutSec) {
    	config.getConfiguration().setProperty(PAGE_LOAD_SEC_PATH, pageLoadTimeoutSec);
    	return this;
    }
    
    /**
     * @return
     */
    public int getBrowserWidth() {
    	return config.getConfiguration().getInt(WIDTH_PATH);
    }
    
    /**
     * @param width
     * @return
     */
    public RunConfiguration setBrowserWidth(int width) {
    	config.getConfiguration().setProperty(WIDTH_PATH, width);
    	return this;
    }
    
    /**
     * @return
     */
    public int getBrowserHeight() {
    	return config.getConfiguration().getInt(HEIGHT_PATH);
    }
    
    /**
     * @param height
     * @return
     */
    public RunConfiguration setBrowserHeight(int height) {
    	config.getConfiguration().setProperty(HEIGHT_PATH, height);
    	return this;
    }
    
    /**
     * Auto-position flag for positioning window on screen.
     * @return
     */
    public boolean getAutoPosition() {
    	return config.getConfiguration().getBoolean(AUTO_POSITION_PATH);
    }
    
    /**
     * Auto-position flag for positioning window on screen.
     * 
     * @param autoPosition
     * @return
     */
    public RunConfiguration setAutoPosition(boolean autoPosition) {
    	config.getConfiguration().setProperty(AUTO_POSITION_PATH, autoPosition);
    	return this;
    }
    
    /**
     * Leftmost coordinates of the browser window. 
     * @return
     */
    public int getPositionXMin() {
    	return config.getConfiguration().getInt(TOP_LEFT_X_PATH);
    }
    
    /**
     * Leftmost coordinates of the browser window.
     * 
     * @param topLeftX
     * @return
     */
    public RunConfiguration setPositionXMin(int topLeftX) {
    	config.getConfiguration().setProperty(TOP_LEFT_X_PATH, topLeftX);
    	return this;
    }
    
    /**
     * Topmost coordinates of the browser window.
     * @return
     */
    public int getPositionYMin() {
    	return config.getConfiguration().getInt(TOP_LEFT_Y_PATH);
    }
    
    /**
     * Topmost coordinates of the browser window.
     * 
     * @param topLeftY
     * @return
     */
    public RunConfiguration setPositionYMin(int topLeftY) {
    	config.getConfiguration().setProperty(TOP_LEFT_Y_PATH, topLeftY);
    	return this;
    }
}
