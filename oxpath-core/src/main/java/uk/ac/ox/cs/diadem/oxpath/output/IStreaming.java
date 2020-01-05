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

package uk.ac.ox.cs.diadem.oxpath.output;

import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;

/**
 * Interface indicating that the output handler streams the OXPath output
 * 
 * @author Ruslan Fayzrakhmanov
 * 14 Feb 2017
 */
public interface IStreaming {
	
	public interface IStreamCloser {
		/**
		 * The stream is initialised and closed by the Output handler.
		 * In case of exceptions not related to the streaming, Output handler does not invoke this method, and it therefore should be invoked by the program, using the OXPath engine.
		 * In case of repeated calls, this functions does nothing and does not throw any exceptions. 
		 */
		public void close();
		/**
		 * @return True, if the stream has been successfully closed.
		 */
		public boolean isClosed();
	}
	
	/**
	 * 
	 * The {@link #close()} should be invoked after receiving the final node {@link OXPathExtractionNode#returnEndNode()}
	 * while executing OXPath.
	 * 
	 * @author Ruslan Fayzrakhmanov
	 * 14 Feb 2017
	 * @param <T>
	 */
	public interface IStreamProcessor<T> extends IStreamCloser {
		/**
		 * @param data data to be streamed.
		 */
		public void process(T data);
	}
	
	public IStreamCloser getStreamCloser();

}
