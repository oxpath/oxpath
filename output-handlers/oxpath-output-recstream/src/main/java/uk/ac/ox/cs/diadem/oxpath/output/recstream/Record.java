/*
 * Copyright (c) 2017, OXPath Team
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

package uk.ac.ox.cs.diadem.oxpath.output.recstream;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @author Ruslan Fayzrakhmanov
 * 14 Feb 2017
 */
public class Record extends ArrayList<List<String>> {
	
	private static final long serialVersionUID = -7640143785153384829L;
	
	private final boolean withId;
	public boolean isWithId() {
		return withId;
	}

	public Record(int size, boolean withId) {
		super(size);
		for (int i=0; i<size; i++) {
			add(new ArrayList<String>());
		}
		this.withId = withId;
	}
	
	public void addValue(int index, String val) {
		get(index).add(val);
	}
	
	public void addSingleValues(List<String> vals) {
		int index=0;
		for (String val: vals) {
			addValue(index, val);
			index++;
		}
	}
	
	public boolean isEmpty() {
		for (List<String> val : this) {
			if (val.size()>0)
				return false;
		}
		return true;
	}
	
	/**
	 * Flattern the record, converting it into the list of atomic values.
	 * @param valueSeparator character to join values of the entity's attribute
	 * @param escapeChar escape character used in string join operation
	 * @return
	 */
	public List<String> flattern(final char valueSeparator, final char escapeChar) {
		return Lists.transform(this, new Function<List<String>, String>() {
			private boolean firstItem = true;
			public String apply(List<String> input) {
				if (input==null)
					return "";
				else {
					if (firstItem && Record.this.isWithId()) {
						firstItem = false;
						return input.get(0);
					} else {
						return StringUtils.join(
								Lists.transform(input, new Function<String, String>() {
									public String apply(String input) {
										return (input == null)?"":applyEscapeChar(input, escapeChar, valueSeparator);
									} }),
								Character.toString(valueSeparator));
					}
				}
			}});
	}
	
	/**
	 * Apply escape character for values
	 * @param value
	 * @param escapeChar
	 * @param charToEscape
	 * @return
	 */
	private String applyEscapeChar(String value, char escapeChar, char charToEscape) {
		if (value == null) {
		      return null;
		    }
		    StringBuilder result = new StringBuilder();
		    for (int i=0; i<value.length(); i++) {
		      char curChar = value.charAt(i);
		      if (curChar == escapeChar || curChar == charToEscape) {
		        result.append(escapeChar);
		      }
		      result.append(curChar);
		    }
		    return result.toString();
	}

}
