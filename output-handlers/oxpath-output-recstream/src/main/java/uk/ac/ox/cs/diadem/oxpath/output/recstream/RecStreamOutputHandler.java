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

import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;
import uk.ac.ox.cs.diadem.oxpath.output.ACustomOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.IStreaming;

/**
 * A class with basic strategy for streaming attributes of a specific entity (record).
 * Serialised attributes are defined as nodes with names from the predefined list of names.
 * All identified attributes are associated with the most recent entity node found in the OXPath output.
 * 
 * To avoid unexpected results, the OXPath extraction tree should not have other nodes, which are not part of the entity to be streamed,
 * with names same as attributes to be serialised.
 * 
 * @author Ruslan Fayzrakhmanov
 * 14 Feb 2017
 */
public class RecStreamOutputHandler
extends ACustomOutputHandler
implements IStreaming {
//	private static final Logger logger = LoggerFactory.getLogger(RecStreamOutputHandler.class);
	
	public static final String ID_LABEL_DEFAULT = "id";
	
	private final boolean includeId;
	private int freeId;
	private final String idLabel;
	public String getIdLabel() {
		return idLabel;
	}
	private final String entity;
	private final List<String> attributes;
	public List<String> getAttributes() {
		return attributes;
	}
	public Record getAttributesAsRecord() {
		Record rez = new Record(attributes.size(), includeId);
		rez.addSingleValues(attributes);
		return rez;
	}
	private final IStreamProcessor<Record> proc;
	private final IFilter<String> valFilter;
	private final IFilter<Record> recFilter;
	
	private Record currOutTuple = null;
	
	public static interface IFilter<T> {
		public boolean filter(T val);
	}
	
	public static IFilter<Record> EMPTY_RECORD_FILTER = new IFilter<Record>(){
		@Override
		public boolean filter(Record val) {
			return val.isEmpty();
		}
	};
	
	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entity the name of the entity
	 * @param attributes attributes to be serialised
	 * @param proc callback function for processing streamed records 
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 * @param includeId true: the id is to be included into the result
	 * @param initId initial id used for records
	 */
	public RecStreamOutputHandler(
			boolean allowMultipleValuesPerAttribute,
			String entity,
			String[] attributes,
			IStreamProcessor<Record> proc,
			boolean includeId,
			int initId,
			IFilter<String> valFilter,
			IFilter<Record> recFilter) {
		super(allowMultipleValuesPerAttribute);
		this.entity = entity;
		this.includeId = includeId;
		this.freeId = initId;
		this.idLabel = ID_LABEL_DEFAULT;
		this.attributes = genAttributes(attributes, includeId, this.idLabel);
		this.proc = proc;
		this.valFilter = valFilter;
		this.recFilter = recFilter;
	}
	
	/**
	 * @param wrapperId
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param entity the name of the entity
	 * @param attributes attributes to be serialised
	 * @param proc callback function for processing streamed records
	 * @param valFilter a function for filtering values (can be null)
	 * @param recFilter a function for filtering records (can be null)
	 * @param includeId true: the id is to be included into the result
	 * @param initId initial id used for records
	 */
	public RecStreamOutputHandler(
			String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			String entity,
			String[] attributes,
			IStreamProcessor<Record> proc,
			boolean includeId,
			int initId,
			IFilter<String> valFilter,
			IFilter<Record> recFilter) {
		super(wrapperId, allowMultipleValuesPerAttribute);
		this.entity = entity;
		this.includeId = includeId;
		this.freeId = initId;
		this.idLabel = ID_LABEL_DEFAULT;
		this.attributes = genAttributes(attributes, includeId, this.idLabel);
		this.proc = proc;
		this.valFilter = valFilter;
		this.recFilter = recFilter;
	}
	
	private List<String> genAttributes(String[] attributes, boolean includeId, String idLabel) {
		List<String> rez = null;
		if (includeId) {
			List<String> attr2 = new ArrayList<String>(attributes.length+1);
			attr2.add(idLabel);
			for (String attr: attributes)
				attr2.add(attr);
			rez = attr2;
		} else {
			rez = Lists.newArrayList(attributes);
		}
		return rez;
	}
	
	@Override
	protected void processNodeImpl(OXPathExtractionNode node) {
		if (isEndNodeReceived()) {
			if (currOutTuple != null && (recFilter==null || !recFilter.filter(currOutTuple)))
				proc.process(currOutTuple);
				proc.close();
          } else {
        	  if (!idLabel.equals(node.getLabel())) {
        		  if (node.isAttribute()) {
            		  if (currOutTuple != null) {
//            			  add it to the existing tuple
                          final int attPos = attributes.indexOf(node.getLabel());
                          if (attPos >= 0) {
                    		  final String content = node.getValue();
                    		  if (valFilter == null || !valFilter.filter(content))
                    			  currOutTuple.addValue(attPos, content);
                          }
            		  }
            	  } else {
            		  if (node.getLabel().equals(entity)) {
            			  if (currOutTuple != null) {
            				  if (recFilter==null || !recFilter.filter(currOutTuple)) {
            					  proc.process(currOutTuple);
            				  } else {
                				  if (includeId) freeId--;
                			  }
            			  } 
                          // create a new tuple
            			  currOutTuple = new Record(attributes.size(), includeId);
            			  if (includeId) {
            				  currOutTuple.addValue(0, Integer.toString(freeId));
            				  freeId++;
            			  }
            		  }
            	  }
        	  }
          }
	}
	
	/**
	 * Get last received record
	 * @return
	 */
	public Record getLastRecord() {
		return currOutTuple;
	}
	
	/**
	 * Get last received record's ID
	 * @return
	 */
	public Integer getLastRecordId() {
		if (currOutTuple != null && currOutTuple.isWithId())
			return Integer.parseInt(currOutTuple.get(0).get(0));
		else
			return null;
	}

	@Override
	public IStreamCloser getStreamCloser() {
		return proc;
	}

}