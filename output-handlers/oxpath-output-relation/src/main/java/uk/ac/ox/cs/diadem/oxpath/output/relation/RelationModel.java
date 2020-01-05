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

package uk.ac.ox.cs.diadem.oxpath.output.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * 
 * This class models relation with some additional assumptions:
 * 1. items in the header can have the same name;
 * 2. each item within the tuple can have several values.
 * 
 * @author Ruslan Fayzrakhmanov
 * 31 Jan 2017
 */
public class RelationModel {
	
	private final boolean withId;
	public boolean isWithId() {
		return withId;
	}
	
	private final List<Integer> entityIndexes;
	public List<Integer> getEntityIndexes() {
		return entityIndexes;
	}

	private final List<List<String>> columnNamePaths;
	/**
	 * Get list of headers, each header is represented as a list of name components.
	 * @return
	 */
	public List<List<String>> getColumnNamePaths() {
		return columnNamePaths;
	}

	private final List<List<List<String>>> rows;
	public List<List<List<String>>> getRows() {
		return rows;
	}
	
	@SuppressWarnings("unchecked")
	public RelationModel(List<List<String>> headers, boolean withId, List<Integer> entityIndexes) {
		assert headers.size() > 0;
		this.columnNamePaths = headers;
		this.rows = new ArrayList<List<List<String>>>();
		this.withId = withId;
		assert entityIndexes == null || entityIndexes.size() <= headers.size();
		if (entityIndexes == null)
			this.entityIndexes = Collections.EMPTY_LIST;
		else
			this.entityIndexes = entityIndexes;
	}
	
	public void addRow(List<List<String>> row) {
		rows.add(row);
	}
	
	public boolean isEmpty(int rowIndex) {
		List<List<String>> row = rows.get(rowIndex);
		for (List<String> val : row) {
			if (val.size()>0)
				return false;
		}
		return true;
	}
	
	/**
	 * @param pathSeparator character to join components of the path denoting the entity's attribute
	 * @param escapeChar escape character used in string join operation
	 * @return
	 */
	public List<String> flatternColumnNamePaths(final char pathSeparator, final char escapeChar) {
		return Lists.transform(columnNamePaths, new Function<List<String>, String>(){
			public String apply(List<String> input) {
				return StringUtils.join(
						Lists.transform(input, new Function<String, String>(){
							public String apply(String input) {
								return applyEscapeChar(input, escapeChar, pathSeparator);
							}}),
						Character.toString(pathSeparator));
			}});
	}
	
	/**
	 * Flatten the rows, converting them into the list of atomic values.
	 * @param valueSeparator character to join values of the entity's attribute
	 * @param escapeChar escape character used in string join operation
	 * @return
	 */
	public List<List<String>> flatternRows(final char valueSeparator, final char escapeChar) {
		return Lists.transform(rows, new Function<List<List<String>>, List<String>>(){
			public List<String> apply(List<List<String>> input) {
				return Lists.transform(input, new Function<List<String>, String>(){
					public String apply(List<String> input) {
						return (input==null)
							? ""
							: StringUtils.join(
								Lists.transform(input, new Function<String, String>() {
									public String apply(String input) {
										return (input == null)?"":applyEscapeChar(input, escapeChar, valueSeparator);
									} }),
								Character.toString(valueSeparator));
					}});
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
	
	/**
	 * Get last received record
	 * @return
	 */
	public List<List<String>> getLastRow() {
		return (rows.size()>0)?rows.get(rows.size()-1):null;
	}
	/**
	 * Get last received record's IDs
	 * @return
	 */
	public List<Integer> getLastRowIds() {
		final List<List<String>> lastRow = getLastRow();
		if (withId && lastRow != null) {
			return Lists.transform(entityIndexes, new Function<Integer, Integer>(){
				@Override public Integer apply(Integer input) {
					return Integer.parseInt(lastRow.get(input).get(0));
				}});
		} else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		return "RelModel [headers=" + columnNamePaths + ", rows=" + rows + "]";
	}
	
}
