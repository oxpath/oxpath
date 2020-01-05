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

package uk.ac.ox.cs.diadem.oxpath.output;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;

/**
 * This abstract class gives the possibilty to execute procesuder just before the first output node
 * and just after the output of the last node.
 * 
 * It also returns the time spent for the output. 
 * 
 * 
 * @author Ruslan Fayzrakhmanov
 * 
 * Jun 18, 2016
 */
public abstract class AAdvancedCustomOutputHandler extends ACustomOutputHandler {
	
	  /**
	 * Time stamp in milliseconds
	 */
	private long firstOutputTimeStamp = -1;
	  /**
	 * @return Time stamp in milliseconds
	 */
	public final long getFirstOutputTimeStamp() {
	    return firstOutputTimeStamp;
	  }
	 /**
	  * 
	 * Time stamp in milliseconds
	 */
	private long lastOutputTimeStamp = -1;
	  /**
	 * @return Time stamp in milliseconds
	 */
	public final long getLastOutputTimeStamp() {
	    return lastOutputTimeStamp;
	  }
	
	private final Stopwatch outputHandlingTime = Stopwatch.createUnstarted();
	
	/**
	 * @return time in milliseconds spent for direct processing of output nodes
	 */
	  public long getOutputHandlingTime() {
	    return outputHandlingTime.elapsed(TimeUnit.MILLISECONDS);
	  }
	
	public static interface Procedure {
		public void invoke();
	}
	
	private final Procedure doJustBeforeFirstOutput;
	private final Procedure doJustAfterLastOutput;
	
	private static final Procedure EMPTY_FUNCTION = new Procedure() {
		@Override public void invoke() {}
	};
	
	public AAdvancedCustomOutputHandler() {
		this(false);
	  }
	  
	  public AAdvancedCustomOutputHandler(boolean allowMultipleValuesPerAttribute) {
		  this(DEFAULT_WRAPPER_ID,
			  allowMultipleValuesPerAttribute);
	  }
	  
	  public AAdvancedCustomOutputHandler(String wrapperId, boolean allowMultipleValuesPerAttribute) {
		  this(wrapperId,
				  allowMultipleValuesPerAttribute,
				  EMPTY_FUNCTION,
				  EMPTY_FUNCTION);
	  }
	  
	  public AAdvancedCustomOutputHandler(
			  boolean allowMultipleValuesPerAttribute,
			  Procedure doJustBeforeFirstOutput,
			  Procedure doJustAfterLastOutput) {
		  this(DEFAULT_WRAPPER_ID,
				  allowMultipleValuesPerAttribute,
				  doJustBeforeFirstOutput,
				  doJustAfterLastOutput);
	  }
	  
	  public AAdvancedCustomOutputHandler(
			  String wrapperId,
			  boolean allowMultipleValuesPerAttribute,
			  Procedure doJustBeforeFirstOutput,
			  Procedure doJustAfterLastOutput) {
		  super(wrapperId, allowMultipleValuesPerAttribute);
		  this.doJustBeforeFirstOutput = doJustBeforeFirstOutput;
		  this.doJustAfterLastOutput = doJustAfterLastOutput;
	  }
	  
	  protected void doJustBeforeFirstOutput() {
		  firstOutputTimeStamp = System.currentTimeMillis(); //System.nanoTime();
		  doJustBeforeFirstOutput.invoke();
	  }
	  
	  protected void doJustAfterLastOutput() {
		  lastOutputTimeStamp = System.currentTimeMillis(); //System.nanoTime();
		  doJustAfterLastOutput.invoke();
	  }
	  
	  
	  private boolean processNodeHasBeenInvoked = false;
	  @Override
	  public void processNode(OXPathExtractionNode node) {
		  if (!processNodeHasBeenInvoked) {
			  doJustBeforeFirstOutput();
			  processNodeHasBeenInvoked = true;
		  }
		  
		  outputHandlingTime.start();
		  super.processNode(node);
		  outputHandlingTime.stop();
		  
		  if (isEndNodeReceived()) {
			  doJustAfterLastOutput();
		  }
		  
	  }
	  
}
