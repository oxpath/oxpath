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
package uk.ac.ox.cs.diadem.webapi.dom;

import java.awt.Dimension;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.css.StyledOverlayBuilder;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMEventTarget;

/**
 *
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface DOMWindow extends DOMEventTarget {

  /**
   * Returns the current document node (that can be stale though @see {@link #getDocument(boolean)}
   *
   * @return
   */
  DOMDocument getDocument();

  /**
   * Returns the document. If it is stale, the new current docuemnt will be returned
   *
   * @param checkIfStale
   * @return
   */
  DOMDocument getDocument(boolean checkIfStale);

  String getName();

  void setName(String name);

  void close();

  /**
   * Return the instance of the browser associated to this window
   *
   * @return
   */
  WebBrowser getBrowser();

  /**
   * Accessor for the current x scroll position in this window in pixels.
   *
   * This attribute is "replaceable" in JavaScript
   */
  int getScrollX();

  /**
   * Accessor for the current y scroll position in this window in pixels.
   *
   * This attribute is "replaceable" in JavaScript
   */
  int getScrollY();

  /**
   * Returns true is the current page is opened in a new fresh window, and not just loaded in the previous window on
   * which an action was performed. IMPORTANT: as side affect, once accessed, following invocation of this method will
   * return false
   *
   * @return true is the current page is opened in a new fresh window, false otherwise
   */
  boolean isJustOpened();

  StyledOverlayBuilder getOverlayBuilder();

  String getTitle();

  String getContentAsString();

  Dimension getDimension();

}