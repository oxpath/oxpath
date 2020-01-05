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

package uk.ac.ox.cs.diadem.oxpath.output.recstream.csv;

import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;
import uk.ac.ox.cs.diadem.oxpath.output.IOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.IStreaming;
import uk.ac.ox.cs.diadem.oxpath.output.OutputHandlerRuntimeException;
import uk.ac.ox.cs.diadem.oxpath.output.recstream.RecStreamOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.recstream.RecStreamOutputHandler.IFilter;
import uk.ac.ox.cs.diadem.oxpath.output.recstream.Record;

/**
 * Simple records are streamed in the CSV format. 
 * 
 * @author Ruslan Fayzrakhmanov
 * 15 Feb 2017
 */
public class RecStreamCSVOutputHandler
	implements IOutputHandler, IStreaming {
//	private static final Logger log = LoggerFactory.getLogger(RecStreamCSVOutputHandler.class);
	
	public static final char VALUE_SEPARATOR_DEFAULT = '|';
	public static final char ESCAPE_CHAR_DEFAULT = '\\';
	public static final String NEW_LINE_SEPARATOR_DEFAULT = "\n";
	public static final char DELIMETER_DEFAULT = ',';
	
	private RecStreamOutputHandler oh;
	
	private class StreamSCVProcessor implements IStreamProcessor<Record> {
		private final Logger log = LoggerFactory.getLogger(StreamSCVProcessor.class);
		
		private final char valueSeparator;
		private final char escapeChar;
		private final boolean printHeader;
		private CSVFormat csvFileFormat;
		
		private final Appendable output;
		private CSVPrinter csv;
		
		private boolean streamClosed = false;
		
		public StreamSCVProcessor(
				char valueSeparator,
				char escapeChar,
				boolean printHeader,
				CSVFormat csvFileFormat,
				Appendable output) {
			this.valueSeparator = valueSeparator;
			this.escapeChar = escapeChar;
			this.printHeader = printHeader;
			this.csvFileFormat = csvFileFormat;
			this.output = output;
		}
		
		public StreamSCVProcessor init() {
			csvFileFormat = (csvFileFormat == null)
				? CSVFormat
					.DEFAULT
					.withRecordSeparator(NEW_LINE_SEPARATOR_DEFAULT)
					.withDelimiter(DELIMETER_DEFAULT)
					.withFirstRecordAsHeader()
					.withQuoteMode(QuoteMode.ALL)
				: csvFileFormat;
			try {
				csv = new CSVPrinter(output, csvFileFormat);
			} catch (IOException e) {
				throw new OutputHandlerRuntimeException(
						"Error in creating the CSV printer:\n"+e.getMessage(), log);
			}
			return this;
		}

		public void processHeader(Record header) {
			// print the header
			if (printHeader) {
				try {
					csv.printRecord(
						Iterables.transform(header, new Function<List<String>, String>(){
							public String apply(List<String> input) {
								return input.get(0);
					}}) );
					csv.flush();
				} catch (IOException e) {
					close();
					throw new OutputHandlerRuntimeException(
							"Error in the CSV printer:\n"+e.getMessage(), log);
				}
			}
		}

		public void process(final Record data) {
			// print the content
			try {
				csv.printRecord(
					Iterables.transform(data.flattern(valueSeparator, escapeChar),
							new Function<String, Object>() {
						private boolean firstItem = true;
						public Object apply(String input) {
								if (firstItem && data.isWithId()) {
									firstItem = false;
									return Integer.parseInt(input);
								} else {
									return input;
								}
				} } ) );
				csv.flush();
			} catch (IOException e) {
				close();
				throw new OutputHandlerRuntimeException(
						"Error in the CSV printer:\n"+e.getMessage(), log);
			}
		}
		
		public void close() {
			try {
				if (csv != null && !streamClosed) {
					csv.close();
					streamClosed = true;
				}
			} catch (IOException e) {
				throw new OutputHandlerRuntimeException(
						"Error with closing the csv printer:\n"+e.getMessage(), log);
			}
		}

		@Override
		public boolean isClosed() {
			return streamClosed;
		}
	}

	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entity the name of the entity
	 * @param attributes attributes to be serialised
	 * @param output the consumer of the output records
	 * @param includeId true: the id is to be included into the result
	 * @param initId initial id used for records
	 * @param printHeader true: output headers first
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 */
	public RecStreamCSVOutputHandler(
			boolean allowMultipleValuesPerAttribute,
			String entity,
			String[] attributes,
			Appendable output,
			boolean includeId,
			int initId,
			boolean printHeader,
			IFilter<String> valFilter,
			IFilter<Record> recFilter) {
		this(null, allowMultipleValuesPerAttribute,
				entity, attributes, output, includeId, initId, printHeader, valFilter, recFilter,
				VALUE_SEPARATOR_DEFAULT, ESCAPE_CHAR_DEFAULT, null);
	}
	
	/**
	 * @param wrapperId
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entity the name of the entity
	 * @param attributes attributes to be serialised
	 * @param output the consumer of the output records
	 * @param includeId true: the id is to be included into the result
	 * @param initId initial id used for records
	 * @param printHeader true: output headers first
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 */
	public RecStreamCSVOutputHandler(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String entity,
			String[] attributes,
			Appendable output,
			boolean includeId,
			int initId,
			boolean printHeader,
			IFilter<String> valFilter,
			IFilter<Record> recFilter) {
		this(wrapperId, allowMultipleValuesPerAttribute,
				entity, attributes, output, includeId, initId, printHeader, valFilter, recFilter,
				VALUE_SEPARATOR_DEFAULT, ESCAPE_CHAR_DEFAULT, null);
	}
	
	/**
	 * @param wrapperId
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entity the name of the entity
	 * @param attributes attributes to be serialised
	 * @param output the consumer of the output records
	 * @param includeId true: the id is to be included into the result
	 * @param initId initial id used for records
	 * @param printHeader true: output headers first
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 * @param valueSeparator character to join values of the entity's attribute
	 * @param escapeChar escape character used in string join operation
	 * @param csvFileFormat CSV format object (it can be null)
	 */
	public RecStreamCSVOutputHandler(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String entity,
			String[] attributes,
			Appendable output,
			boolean includeId,
			int initId,
			boolean printHeader,
			IFilter<String> valFilter,
			IFilter<Record> recFilter,
			char valueSeparator,
			char escapeChar,
			CSVFormat csvFileFormat) {
		this.oh = initOh(wrapperId, allowMultipleValuesPerAttribute,
				entity, attributes, output, includeId, initId, printHeader, valFilter, recFilter,
				valueSeparator, escapeChar, csvFileFormat);
	}
	
	private RecStreamOutputHandler initOh(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String entity,
			String[] attributes,
			Appendable output,
			boolean includeId,
			int initId,
			boolean printHeader,
			IFilter<String> valFilter,
			IFilter<Record> recFilter,
			char valueSeparator,
			char escapeChar,
			CSVFormat csvFileFormat) {
		StreamSCVProcessor streamSCVProcessor = new StreamSCVProcessor(valueSeparator, escapeChar, printHeader, csvFileFormat, output).init();
		RecStreamOutputHandler oh = (wrapperId == null)
				? new RecStreamOutputHandler(wrapperId, allowMultipleValuesPerAttribute,
						entity, attributes, streamSCVProcessor, includeId, initId, valFilter, recFilter)
				: new RecStreamOutputHandler(allowMultipleValuesPerAttribute,
						entity, attributes, streamSCVProcessor, includeId, initId, valFilter, recFilter);
		streamSCVProcessor.processHeader(oh.getAttributesAsRecord());
		return oh;
	}
	
	/**
	 * Get last received record
	 * @return
	 */
	public Record getLastRecord() {
		return oh.getLastRecord();
	}
	/**
	 * Get last received record's ID
	 * @return
	 */
	public Integer getLastRecordId() {
		return oh.getLastRecordId();
	}

	public void processNode(OXPathExtractionNode node) {
		oh.processNode(node);
	}

	public void receiveOuterException(Throwable e) {
		oh.receiveOuterException(e);
	}

	public boolean hasOuterException() {
		return oh.hasOuterException();
	}

	public boolean isEndNodeReceived() {
		return oh.isEndNodeReceived();
	}

	public String getWrapperId() {
		return oh.getWrapperId();
	}

	public List<Throwable> getOuterExceptions() {
		return oh.getOuterExceptions();
	}

	public String getOuterExceptionStackTracesAsString() {
		return oh.getOuterExceptionStackTracesAsString();
	}

	@Override
	public IStreamCloser getStreamCloser() {
		return oh==null?null:oh.getStreamCloser();
	}

}
