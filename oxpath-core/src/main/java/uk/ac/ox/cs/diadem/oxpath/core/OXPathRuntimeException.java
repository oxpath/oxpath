package uk.ac.ox.cs.diadem.oxpath.core;
/*
 * Copyright (c)2011, DIADEM Team
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
/**
 * Package with utilities for supporting OXPath expression evaluation
 */


import org.slf4j.Logger;

/**
 * class describing runtime exception thrown if user-supplied OXPath script contains errors/is missing data
 * 
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class OXPathRuntimeException extends RuntimeException {

  /*public OXPathRuntimeException(final String message, final Logger logger) {
    super(message, logger);
    logger.error(message);
  }

  public OXPathRuntimeException(final String message, final Throwable cause, final Logger logger) {
    super(message, cause, logger);
    // logger.error(message);
  }*/
	
  public OXPathRuntimeException(final String message, final Logger logger) {
    super(message);
    if (logger != null) {
      logger.error(message);
    }
  }

  public OXPathRuntimeException(final String message, final Throwable cause, final Logger logger) {
    super(message, cause);
    if (logger != null) {
      logger.error(message);
    }
  }

  // boilerplate for serialize interface
  private static final long serialVersionUID = 1L;
}
