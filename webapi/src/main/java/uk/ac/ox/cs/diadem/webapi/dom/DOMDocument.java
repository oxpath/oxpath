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

/**
 * Wrapping interface for http://www.w3.org/2003/01/dom2-javadoc/org/w3c/dom/Document.html
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 *
 */
public interface DOMDocument extends DOMNode, DOMNodeSelector {

  public static enum CRITERIA {
    id, css, xpath, name, xy, tagname;
  }

  /**
   * Retrieves an element by CRITERIA. Note that the expression must select an DOM element. If it selects a DOM node
   * (e.g., by xpath) a runtime exception is raised. For {@link CRITERIA#xy} the expression expected is in the form X:Y
   * (e.g., 200:400), that specifies the point via coordinates, in CSS pixels, relative to the upper-left-most point in
   * the window or frame containing the document. If the expression selects a list of elements, the first is returned.
   *
   * @param critera
   *          the type of query to execute
   * @param expr
   *          the expression
   * @return the retrieved {@link DOMElement} or null if not found.
   */
  DOMElement selectElementBy(DOMDocument.CRITERIA critera, String expr);

  /**
   * Returns the document element
   *
   * @return the document element
   */
  DOMElement getDocumentElement();

  /**
   * Retrieves the list of elements by name
   *
   * @param name
   * @return the list of elements by name
   */
  DOMNodeList getElementsByName(String name);

  /**
   * Retrieves an element by id
   *
   * @param id
   * @return an element by id
   */
  DOMElement getElementById(String id);

  /**
   * Retrieves the list of elements by tagName
   *
   * @param tagName
   * @return the list of elements by tagName
   */
  DOMNodeList getElementsByTagName(String tagName);

  /**
   * Returns the element from the document whose elementFromPoint method is being called which is the topmost element
   * which lies under the given point. To get an element, specify the point via coordinates, in CSS pixels, relative to
   * the upper-left-most point in the window or frame containing the document.
   *
   * @param x
   * @param y
   * @return the element in position x,y , or null if
   */
  DOMElement elementByPosition(int x, int y);

  /**
   * Returns the {@link DOMWindow} object associated with the document or null if none available. from
   * https://developer.mozilla.org/en/DOM/document.defaultView
   *
   * @return the {@link DOMWindow} object associated with the document or null if none available.
   */
  DOMWindow getEnclosingWindow();

  /**
   * Creates an element of the type specified belonging to this document
   * http://www.w3.org/TR/2000/REC-DOM-Level-2-Core-20001113/core.html#ID- 1334481328
   *
   * @param tagName
   * @return the created element
   */
  DOMElement createElement(String tagName);

  /**
   *
   * @return
   */
  DOMRange createRange(DOMNode startNode, int startOffset, DOMNode endNode, int endOffSet);

  /**
   * Get the current computed dimension for this document
   * 
   * @return the current computed dimension for this document
   */
  Dimension getDimension();

}
