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

package uk.ac.ox.cs.diadem.webapi;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.util.configuration.ConfigurationObject;
import uk.ac.ox.cs.diadem.util.configuration.ConfigurationObjectFactory;
import uk.ac.ox.cs.diadem.util.misc.OSType;
import uk.ac.ox.cs.diadem.webapi.configuration.JavaScriptConfiguration;
import uk.ac.ox.cs.diadem.webapi.configuration.PlatformConfiguration;
import uk.ac.ox.cs.diadem.webapi.configuration.RunConfiguration;
import uk.ac.ox.cs.diadem.webapi.dom.impl.WebDriverBrowserImpl;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 * 
 * Factory for creating a {@linkplain WebBrowser}.
 * 
 * @author Ruslan Fayzrakhmanov
 * 11 Nov 2017
 */
public class WebBrowserBuilder {
	
	private static final Logger log = LoggerFactory.getLogger(WebBrowserBuilder.class);
	
	public static final String DEFAULT_CONFIGURATION_PATH = "/uk/ac/ox/cs/diadem/webapi/FirefoxDefaultConfiguration.xml";
	
	private final ConfigurationObject configuration;
	
	@Deprecated
	public enum Engine {
		WEBDRIVER_FF
	}
	
	public enum ECombineConfigurationMode {
		OVERRIDE
	}
	
	private final PlatformConfiguration platformConfiguration;
	private final JavaScriptConfiguration javaScriptConfiguration;
	private final RunConfiguration runConfiguration;
	public RunConfiguration getRunConfiguration() {
		return runConfiguration;
	}
	public PlatformConfiguration getPlatformConfiguration() {
		return platformConfiguration;
	}
	
	public WebBrowserBuilder() {
		ConfigurationObjectFactory configFactory = new ConfigurationObjectFactory();
		configuration = configFactory.create(this.getClass().getResource(DEFAULT_CONFIGURATION_PATH));
		platformConfiguration = new PlatformConfiguration(configuration, OSType.getOsType());
		javaScriptConfiguration = new JavaScriptConfiguration(configuration);
		runConfiguration = new RunConfiguration(configuration);
	}
	
	/**
	 * TODO: Create a method with the possibility to set a mode for combining configurations. E.g.:
	 * {@code public WebBrowserBuilder(ConfigurationObject configuration, ECombineConfigurationMode mode)}
	 * 
	 * @param configuration
	 * @param mode
	 */
	public WebBrowserBuilder(ConfigurationObject configuration) {
		ConfigurationObject configurationDef = new ConfigurationObjectFactory()
				.create(this.getClass().getResource(DEFAULT_CONFIGURATION_PATH));
		if (configuration == null)
			this.configuration = configurationDef;
		else
			this.configuration = configuration.override(configurationDef);
		platformConfiguration = new PlatformConfiguration(this.configuration, OSType.getOsType());
		javaScriptConfiguration = new JavaScriptConfiguration(this.configuration);
		runConfiguration = new RunConfiguration(this.configuration);
	}
	
	public WebBrowser build() {
		try {
			return new WebDriverBrowserImpl(platformConfiguration, runConfiguration, javaScriptConfiguration);
		} catch (ConfigurationException e) {
			throw new WebAPIRuntimeException("Cannot instantiate the browser", e, log);
		}
	}
	
	
}
