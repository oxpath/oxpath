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

import java.util.List;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;

/**
 * It combine different output handlers.
 * 
 * @author Ruslan Fayzrakhmanov
 * 8 Feb 2017
 */
public class OutputHandlerRouter implements IAbstractOutputHandler {
	
	private final List<IAbstractOutputHandler> outputHandlers;
	public List<IAbstractOutputHandler> getOutputHandlers() {
		return outputHandlers;
	}

	private boolean hasOuterException = false;
	
	public OutputHandlerRouter(IAbstractOutputHandler... ohs) {
		this.outputHandlers = Lists.newArrayList(ohs);
	}

	@Override
	public void processNode(OXPathExtractionNode node) {
		for (IAbstractOutputHandler oh: outputHandlers) {
			oh.processNode(node);
		}
	}
	
	@Override
	public void receiveOuterException(Throwable e) {
		hasOuterException = true;
		for (IAbstractOutputHandler oh: outputHandlers) {
			oh.receiveOuterException(e);
		}
	}

	@Override
	public boolean hasOuterException() {
		return hasOuterException;
	}

}
