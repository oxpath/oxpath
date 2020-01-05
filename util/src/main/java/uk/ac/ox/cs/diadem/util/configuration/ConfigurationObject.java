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

import org.apache.commons.configuration2.BaseHierarchicalConfiguration;

/**
 * 
 * Hierarchical configuration.
 * 
 * @author Ruslan Fayzrakhmanov
 * 18 Nov 2017
 * @see ConfigurationObjectFactory
 */
public class ConfigurationObject {
//	private static final Logger log = LoggerFactory.getLogger(ConfigurationObject2.class);
	
	private final BaseHierarchicalConfiguration configuration;
	private final ConfigurationObjectFactory factory;
	
	public BaseHierarchicalConfiguration getConfiguration() {
		return configuration;
	}

	public ConfigurationObject(BaseHierarchicalConfiguration configuration,
			ConfigurationObjectFactory factory) {
		this.configuration = configuration;
		this.factory = factory;
	}
	
	public ConfigurationObject merge(ConfigurationObject config2) {
		return factory.merge(getConfiguration(), config2.getConfiguration());
	}
	
	public ConfigurationObject union(ConfigurationObject config2) {
		return factory.union(getConfiguration(), config2.getConfiguration());
	}
	
	public ConfigurationObject override(ConfigurationObject config2) {
		return factory.override(getConfiguration(), config2.getConfiguration());
	}
	

}
