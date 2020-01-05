/*
 * Copyright (c)2013, DIADEM Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the DIADEM team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DIADEM Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Copyright (c)2017, OXPath Team
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

package uk.ac.ox.cs.diadem.oxpath.output;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;
import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;

/**
 * This class implements basic functionality for handling the OXPath output
 * and can be sub-classed by specific implementations,
 * processing {@link OXPathExtractionNode}.
 * 
 * @author Giovanni Grasso <gio@oxpath.org>
 * @author Ruslan Fayzrakhmanov
 */
public abstract class ACustomOutputHandler implements IOutputHandler {
  private static final Logger logger = LoggerFactory.getLogger(ACustomOutputHandler.class);
  
  public final static String DEFAULT_WRAPPER_ID = "UNKNOWN_ID";
  
  private boolean endNodeReceived;
  @Override
	public boolean isEndNodeReceived() {
		return endNodeReceived;
	}
  
  private final String wrapperId;
  
  private final Set<String> attributesSeenPerTuple;
  protected Set<String> getAttributesSeenPerTuple() {
	  return attributesSeenPerTuple;
  }
  private final boolean allowMultipleValuesPerAttribute;
  protected boolean getAllowMultipleValuesPerAttribute() {
	  return allowMultipleValuesPerAttribute;
  }
  
  private final List<Throwable> exceptions;

  /**
	 * Only one value is allowed per attribute
	 */
	protected ACustomOutputHandler() {
		  this(false);
	  }
  
	/**
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 */
	protected ACustomOutputHandler(boolean allowMultipleValuesPerAttribute) {
	  this(DEFAULT_WRAPPER_ID, allowMultipleValuesPerAttribute);
	}
  
	  /**
	 * @param wrapperId The ID of the execution.
	 * @param allowMultipleValuesPerAttribute true: multiple values are allowed per attribute
	 */
	protected ACustomOutputHandler(String wrapperId, boolean allowMultipleValuesPerAttribute) {
	  this.wrapperId = wrapperId;
	  this.allowMultipleValuesPerAttribute = allowMultipleValuesPerAttribute;
	  this.attributesSeenPerTuple = Sets.newHashSet();
	  this.endNodeReceived = false;
	  this.exceptions = new LinkedList<Throwable>();
  }
  
	private Integer prevNodeId = null;
  @Override
  public void processNode(OXPathExtractionNode node) {
	  if (node == null) {
		throw new OXPathRuntimeException("Received a null node as an output", logger);
	  }
	  assert node.getId()>node.getParentId();
	  assert (prevNodeId==null)?node.getId()==1:node.isEndNode()?true:prevNodeId+1 == node.getId();
	  prevNodeId = node.getId();
	  if (endNodeReceived) {
		  throw new OXPathRuntimeException("Output have already received the end node and cannot process nodes anymore", logger);
	  }
      validateNode(node);
	  if (node.isEndNode()) {
	    endNodeReceived = true;
	  }
	  processNodeImpl(node);
  }
  
  abstract protected void processNodeImpl(OXPathExtractionNode node);

  @Override
  public String getWrapperId() {
	  return wrapperId;
  }

  private void validateNode(final OXPathExtractionNode node) {
    if (node.isEndNode())
      return;
    if (node.isAttribute()) {
      validateAttribute(node.getLabel());
    } else {
    	attributesSeenPerTuple.clear();
    }
  }

  private void validateAttribute(final String attributeLabel) {
    if (allowMultipleValuesPerAttribute) return;
    if (attributesSeenPerTuple.contains(attributeLabel)) {
      throw new OXPathRuntimeException("Wrapper <'"+getWrapperId()+"'> has multiple values for attribute <'"+attributeLabel+"'>", logger);
    } else {
      attributesSeenPerTuple.add(attributeLabel);
    }
  }
  
  @Override
	public void receiveOuterException(Throwable e) {
	  exceptions.add(e);
	}
  
  @Override
	public boolean hasOuterException() {
		return exceptions.size()>0;
	}
  
  @Override
	public List<Throwable> getOuterExceptions() {
		return ImmutableList.copyOf(exceptions);
	}
  
  @Override
  public String getOuterExceptionStackTracesAsString() {
	  return Arrays.toString(exceptions.toArray(new StackTraceElement[exceptions.size()]));
	}
  
  /**
   * Helper function: Log message with wrapper id attached
   * @param string
   */
  protected void logInfo(final String message, final Logger logger) {
    logger.info("<'{}'>: {}", getWrapperId(), message);
  }
  
}
