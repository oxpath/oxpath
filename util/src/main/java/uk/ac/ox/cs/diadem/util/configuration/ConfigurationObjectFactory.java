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

package uk.ac.ox.cs.diadem.util.configuration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;
import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.MergeCombiner;
import org.apache.commons.configuration2.tree.NodeCombiner;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.commons.configuration2.tree.UnionCombiner;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.util.exception.UtilRuntimeException;

/**
 * 
 * Factory for creating {@link ConfigurationObject}.
 * 
 * @author Ruslan Fayzrakhmanov
 * 18 Nov 2017
 */
public class ConfigurationObjectFactory {
	
	private static final Logger log = LoggerFactory.getLogger(ConfigurationObject.class);
	
	/**
	 * Create a configuration object.
	 * @param configFile XML file with configurations
	 * @return {@link ConfigurationObject}
	 */
	public ConfigurationObject create(File configFile) {
		return create(new Parameters().xml().setFile(configFile));
	}
	
	/**
	 * Create a configuration object.
	 * @param configURL
	 * @return {@link ConfigurationObject}
	 */
	public ConfigurationObject create(URL configURL) {
		return create(new Parameters().xml().setURL(configURL));
	}
	
	public ConfigurationObject create(String xmlConfig) {
		XMLConfiguration xmlConf = new XMLConfiguration();
		try {
			xmlConf.read(IOUtils.toInputStream(xmlConfig, Charset.defaultCharset()));
			return new ConfigurationObject(xmlConf, this);
		} catch (ConfigurationException | IOException e) {
			throw new UtilRuntimeException("Cannot create a configuration object", e, log);
		}
		
	}
	
	private ConfigurationObject create(XMLBuilderParameters paramsWithSource) {
		FileBasedConfigurationBuilder<XMLConfiguration> builder =
		    new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
		    	.configure(paramsWithSource.setExpressionEngine(new XPathExpressionEngine()));
		try {
			XMLConfiguration config = builder.getConfiguration();
			return new ConfigurationObject(config, this);
		} catch (ConfigurationException e) {
			throw new UtilRuntimeException("Cannot create a configuration object", e, log);
		}
	}
	
	public ConfigurationObject create(BaseHierarchicalConfiguration config) {
		return new ConfigurationObject(config, this);
	}
	
	/**
	 * Merge configurations.
	 * 
	 * @param config1
	 * @param config2
	 * @return
	 * @see https://commons.apache.org/proper/commons-configuration/userguide/howto_combinedconfiguration.html#Combined_Configuration
	 */
	public ConfigurationObject merge(
			BaseHierarchicalConfiguration config1,
			BaseHierarchicalConfiguration config2) {
		return createCombinedConfigurationObject(config1, config2, new MergeCombiner());
	}
	
	/**
	 * Take union of configurations.
	 * 
	 * @param config1
	 * @param config2
	 * @return
	 * @see https://commons.apache.org/proper/commons-configuration/userguide/howto_combinedconfiguration.html#Combined_Configuration
	 */
	public ConfigurationObject union(
			BaseHierarchicalConfiguration config1,
			BaseHierarchicalConfiguration config2) {
		return createCombinedConfigurationObject(config1, config2, new UnionCombiner());
	}
	
	/**
	 * Override configurations.
	 * 
	 * @param config1
	 * @param config2
	 * @return
	 * @see https://commons.apache.org/proper/commons-configuration/userguide/howto_combinedconfiguration.html#Combined_Configuration
	 */
	public ConfigurationObject override(
			BaseHierarchicalConfiguration config1,
			BaseHierarchicalConfiguration config2) {
		return createCombinedConfigurationObject(config1, config2, new OverrideCombiner());
	}
	
	private ConfigurationObject createCombinedConfigurationObject(
			BaseHierarchicalConfiguration config1,
			BaseHierarchicalConfiguration config2,
			NodeCombiner combiner) {
		CombinedConfiguration combinedConfig = new CombinedConfiguration(combiner);
		combinedConfig.setExpressionEngine(new XPathExpressionEngine());
		combinedConfig.addConfiguration(config1);
		combinedConfig.addConfiguration(config2);
		return create(combinedConfig);
	}


}
