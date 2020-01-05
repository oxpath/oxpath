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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import uk.ac.ox.cs.diadem.util.configuration.ConfigurationObject;

public class JavaScriptConfiguration {
	
	public static final String PATH_SEPARATOR = "/";
	public static final String JS_FUNCTIONS_PATH = "webapi/js-template-function-calls";
	
	private final ConfigurationObject config;
	
	public JavaScriptConfiguration(ConfigurationObject config) {
		this.config = config;
	}
	
	public Map<String, String> getJavaScriptTemplateFunctionCalls() {
		Map<String, String> tmplCalls = new HashMap<>(102);
		final Iterator<String> keys = config.getConfiguration().getKeys(JS_FUNCTIONS_PATH);
	    // skip the first
	    keys.next();
	    while (keys.hasNext()) {
	      final String key = keys.next();
	      final String function = key.substring(JS_FUNCTIONS_PATH.length() + PATH_SEPARATOR.length());
	      tmplCalls.put(function, config.getConfiguration().getString(key));
	    }
	    return tmplCalls;
	}
	
	

}
