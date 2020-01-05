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


/**
 * 
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface DOMHTMLOptionsCollection {

  long getLength();

  // void setLength(long aLength);
  /**
   * This method retrieves a node specified by ordinal index. Nodes are numbered in tree order (depth-first traversal
   * order).
   */
  DOMHTMLOptionElement item(long index);

  /**
   * This method retrieves a {@link DOMHTMLOptionElement} using a name. It first searches for a node with a matching id
   * attribute. If it doesn't find one, it then searches for a Node with a matching name attribute, but only on those
   * elements that are allowed a name attribute. NOTE: on SWT_MOZILLA browser, this method is case insensitive in HTML
   * documents and case sensitive in XHTML documents. On HTMLUNIT, it is insensitive
   * 
   * @param name
   * @return
   */
  DOMHTMLOptionElement namedItem(String name);

  /**
   * Returns the {@link DOMHTMLOptionElement} object that corresponds to the specified value.
   * 
   * @param value
   *          the value to search by
   * @return the {@link DOMHTMLOptionElement} object that corresponds to the specified value or null if not found
   */
  DOMHTMLOptionElement itemByValue(String value);

  /**
   * Returns the {@link DOMHTMLOptionElement} object that has the specified text.
   * 
   * @param text
   *          the text to search by
   * @return the {@link DOMHTMLOptionElement} object that has the specified text, or null if not found
   */
  DOMHTMLOptionElement itemByText(String text);
}
