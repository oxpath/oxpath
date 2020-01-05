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

package uk.ac.ox.cs.diadem.oxpath.output.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;
import uk.ac.ox.cs.diadem.oxpath.output.ACustomOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.output.IStringSerializable;
import uk.ac.ox.cs.diadem.oxpath.output.OutputHandlerRuntimeException;

/**
 * Used to handle output from OXPath expressions and return the output as a JSON object.
 * 
 * Returns output of query. Builds the document from the nodes received on the output stream
 * 
 * @author Ruslan Fayzrakhmanov
 * 12 Jan 2017
 */
public class JsonOutputHandler
	extends ACustomOutputHandler
	implements IStringSerializable<JsonObject> {
	private static final Logger log = LoggerFactory.getLogger(JsonOutputHandler.class);
	
	private final boolean useArrayForValues;
	public boolean isUseArrayForValues() {
		return useArrayForValues;
	}

	private final boolean prettyPrint;
	public boolean isPrettyPrint() {
		return prettyPrint;
	}
	
	/**
	* Instance field holding the output of the nodes received from the output stream
	*/
	private List<OXPathExtractionNode> nodes = new Vector<OXPathExtractionNode>();
	private JsonObject root = null;
	
	public JsonOutputHandler() {
		super(false);
		useArrayForValues = false;
		prettyPrint = false;
	}
	
	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param useArrayForValues use array for storing values.
	 * @param prettyPrint true: use indentation in the serialized JSON
	 */
	public JsonOutputHandler(
			boolean allowMultipleValuesPerAttribute,
			boolean useArrayForValues,
			boolean prettyPrint) {
		super(allowMultipleValuesPerAttribute);
		if (allowMultipleValuesPerAttribute && !useArrayForValues) 
			throw new OutputHandlerRuntimeException("The serialization of multiple values needs the array constructor", log);
		this.useArrayForValues = useArrayForValues;
		this.prettyPrint = prettyPrint;
	}
	
	/**
	 * @param wrapperId
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 * @param useArrayForValues use array for storing values.
	 * @param prettyPrint true: use indentation in the serialized JSON
	 */
	public JsonOutputHandler(String wrapperId,
			boolean allowMultipleValuesPerAttribute,
			boolean useArrayForValues,
			boolean prettyPrint) {
		super(wrapperId, allowMultipleValuesPerAttribute);
		if (allowMultipleValuesPerAttribute && !useArrayForValues) 
			throw new OutputHandlerRuntimeException("The serialization of multiple values needs the array constructor", log);
		this.useArrayForValues = useArrayForValues;
		this.prettyPrint = prettyPrint;
	}
	
	@Override
	protected void processNodeImpl(OXPathExtractionNode node) {
		if (!node.isEndNode()) {
			assert nodes.size()+1 == node.getId();
  			nodes.add(node);
  	    }
	}
	
	public JsonObject getAccumulativeOutput() {
    	if (root==null) {
    		root=convertToJson(nodes);
    		nodes.clear(); nodes = null;
    	}
    	return root;
	}
	
	public String asString() {
		if (prettyPrint) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			return gson.toJson(getAccumulativeOutput());
		} else {
			return getAccumulativeOutput().toString();
		}
	}
	
	private JsonObject convertToJson(List<OXPathExtractionNode> nodes) {
		JsonObject root = new JsonObject();
	    final ArrayList<JsonObject> elements = new ArrayList<JsonObject>();
	    // elements are identified by the position rather than by the name
	    elements.add(root);
	    for (final OXPathExtractionNode o : nodes) {
	    	boolean isRoot = o.getParentId() == 0;
	    	OXPathExtractionNode parentExtrNode = isRoot?null:nodes.get(o.getParentId()-1);
	    	assert isRoot || parentExtrNode.getId() == o.getParentId();
	    	if (!isRoot && parentExtrNode.isAttribute())
	    		throw new OutputHandlerRuntimeException(
						"An attribute node cannot have children", log);
	    	JsonObject parent = elements.get(o.getParentId());
	    	JsonElement sameParamVal = parent.get(o.getLabel());
	    	JsonObject oJson = null;
	    	if (!o.isAttribute()) {
	    		oJson = new JsonObject();
	    	}
	    	elements.add(o.getId(), oJson);
	    	if (useArrayForValues) {
	    		JsonArray vals = null;
	    		if (sameParamVal == null) {
	    			vals = new JsonArray();
	    			parent.add(o.getLabel(), vals);
	    		} else {
	    			vals = (JsonArray)sameParamVal;
	    		}
	    		if (o.isAttribute()) {
	    			vals.add(o.getValue());
	    		} else {
	    			vals.add(oJson);
	    		}
	    	} else {
	    		if (sameParamVal == null) {
	    			if (o.isAttribute()) {
	    				parent.addProperty(o.getLabel(), o.getValue());
		    		} else {
		    			parent.add(o.getLabel(), oJson);
		    		}
	    		} else {
	    			throw new OutputHandlerRuntimeException(
	    					"The serialization of multiple values needs the array constructor", log);
	    		}
	    	}
	    }
	    return root;
	}

}
