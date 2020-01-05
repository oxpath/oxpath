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

package uk.ac.ox.cs.diadem.oxpath.output.hierarchy.csv;

import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.oxpath.output.IStringSerializable;
import uk.ac.ox.cs.diadem.oxpath.output.OutputHandlerRuntimeException;
import uk.ac.ox.cs.diadem.oxpath.output.relation.RelationModel;
import uk.ac.ox.cs.diadem.oxpath.output.relation.RelationOutputHandler;

/**
 * The output handler to serialise the extraction tree into the SCV format.
 * It uses {@linkplain RelationOutputHandler} for converting the tree into the list of records.
 * 
 * @author Ruslan Fayzrakhmanov
 * 9 Feb 2017
 */
public class HierarchyCSVOutputHandler
	extends RelationOutputHandler
	implements IStringSerializable<RelationModel> {
	private static final Logger log = LoggerFactory.getLogger(HierarchyCSVOutputHandler.class);
	
	public static final char PATH_SEPARATOR_DEFAULT = '_';
	public static final char VALUE_SEPARATOR_DEFAULT = '|';
	public static final char ESCAPE_CHAR_DEFAULT = '\\';
	public static final String NEW_LINE_SEPARATOR_DEFAULT = "\n";
	public static final char DELIMETER_DEFAULT = ',';
	public static final boolean PRINT_HEADER_DEFAULT = true;
	
	private final char pathSeparator;
	private final char valueSeparator;
	private final char escapeChar;
	private final boolean printHeader;
	private CSVFormat csvFileFormat;
	
	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entityRelPathList array of paths of entities to be identified in the extraction tree
	 * @param includeId true: ids corresponding to entities are incorporated into the model
	 * @param includeRecordLabel true: the name of the record nesting attributes and child entities is included into the path name.
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 */
	public HierarchyCSVOutputHandler(
			boolean allowMultipleValuesPerAttribute,
			String[][] entityRelPathList,
			boolean includeId,
			boolean includeRecordLabel,
			IFilter<String> valFilter,
			IFilter<List<List<String>>> rowFilter) {
		this(allowMultipleValuesPerAttribute,
				entityRelPathList, includeId, includeRecordLabel,valFilter, rowFilter,
				PATH_SEPARATOR_DEFAULT, VALUE_SEPARATOR_DEFAULT, ESCAPE_CHAR_DEFAULT, PRINT_HEADER_DEFAULT,
				null);
	}
	
	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entityRelPathList array of paths of entities to be identified in the extraction tree
	 * @param includeId true: ids corresponding to entities are incorporated into the model
	 * @param includeRecordLabel true: the name of the record nesting attributes and child entities is included into the path name.
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 * @param pathSeparator character to join components of the path denoting the entity's attribute
	 * @param valueSeparator character to join values of the entity's attribute
	 * @param escapeChar escape character used in string join operation
	 * @param printHeader true: print the header
	 * @param csvFileFormat CSV format object (it can be null)
	 */
	public HierarchyCSVOutputHandler(
			boolean allowMultipleValuesPerAttribute,
			String[][] entityRelPathList,
			boolean includeId,
			boolean includeRecordLabel,
			IFilter<String> valFilter,
			IFilter<List<List<String>>> rowFilter,
			char pathSeparator,
			char valueSeparator,
			char escapeChar,
			boolean printHeader,
			CSVFormat csvFileFormat) {
		super(allowMultipleValuesPerAttribute,
				entityRelPathList, includeId, includeRecordLabel, valFilter, rowFilter);
		this.pathSeparator = pathSeparator;
		this.valueSeparator = valueSeparator;
		this.escapeChar = escapeChar;
		this.printHeader = printHeader;
		this.csvFileFormat = csvFileFormat;
	}
	
	/**
	 * @param wrapperId
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entityRelPathList array of paths of entities to be identified in the extraction tree
	 * @param includeId true: ids corresponding to entities are incorporated into the model
	 * @param includeRecordLabel true: the name of the record nesting attributes and child entities is included into the path name.
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 */
	public HierarchyCSVOutputHandler(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String[][] entityRelPathList,
			boolean includeId,
			boolean includeRecordLabel,
			IFilter<String> valFilter,
			IFilter<List<List<String>>> rowFilter) {
		this(wrapperId, allowMultipleValuesPerAttribute,
				entityRelPathList, includeId, includeRecordLabel, valFilter, rowFilter,
				PATH_SEPARATOR_DEFAULT, VALUE_SEPARATOR_DEFAULT, ESCAPE_CHAR_DEFAULT, PRINT_HEADER_DEFAULT,
				null);
	}
	
	/**
	 * @param wrapperId
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entityRelPathList array of paths of entities to be identified in the extraction tree
	 * @param includeId true: ids corresponding to entities are incorporated into the model
	 * @param includeRecordLabel true: the name of the record nesting attributes and child entities is included into the path name.
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 * @param pathSeparator character to join components of the path denoting the entity's attribute
	 * @param valueSeparator character to join values of the entity's attribute
	 * @param escapeChar escape character used in string join operation
	 * @param printHeader true: print the header
	 * @param csvFileFormat CSV format object (it can be null)
	 */
	public HierarchyCSVOutputHandler(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String[][] entityRelPathList,
			boolean includeId,
			boolean includeRecordLabel,
			IFilter<String> valFilter,
			IFilter<List<List<String>>> rowFilter,
			char pathSeparator,
			char valueSeparator,
			char escapeChar,
			boolean printHeader,
			CSVFormat csvFileFormat) {
		super(wrapperId, allowMultipleValuesPerAttribute, entityRelPathList, includeId, includeRecordLabel, valFilter, rowFilter);
		this.pathSeparator = pathSeparator;
		this.valueSeparator = valueSeparator;
		this.escapeChar = escapeChar;
		this.printHeader = printHeader;
		this.csvFileFormat = csvFileFormat;
	}

	public String asString() {
		RelationModel m = getAccumulativeOutput();
		
		StringBuilder sb = new StringBuilder();
		csvFileFormat = (csvFileFormat == null)
			? CSVFormat
				.DEFAULT
				.withRecordSeparator(NEW_LINE_SEPARATOR_DEFAULT)
				.withDelimiter(DELIMETER_DEFAULT)
				.withFirstRecordAsHeader()
				.withQuoteMode(QuoteMode.ALL)
			: csvFileFormat;
		CSVPrinter csv = null;
		try {
			csv = new CSVPrinter(sb, csvFileFormat);
			// print the header
			if (printHeader)
				csv.printRecord(m.flatternColumnNamePaths(pathSeparator, escapeChar));
			// print the content
			csv.printRecords(m.flatternRows(valueSeparator, escapeChar));
		} catch (IOException e) {
			throw new OutputHandlerRuntimeException(
					"Error in the CSV printer:\n"+e.getMessage(), log);
		} finally {
			try {
				if (csv != null)
					csv.close();
			} catch (IOException e) {
				throw new OutputHandlerRuntimeException(
						"Error with closing the csv printer:\n"+e.getMessage(), log);
			}
		}
		return sb.toString();
	}
	
}
