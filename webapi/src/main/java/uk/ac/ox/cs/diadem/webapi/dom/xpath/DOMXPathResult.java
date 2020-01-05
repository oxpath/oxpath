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
package uk.ac.ox.cs.diadem.webapi.dom.xpath;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;

/**
 * 
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface DOMXPathResult {

  // XPathResultType
  public static final short ANY_TYPE = 0;
  public static final short NUMBER_TYPE = 1;
  public static final short STRING_TYPE = 2;
  public static final short BOOLEAN_TYPE = 3;
  public static final short UNORDERED_NODE_ITERATOR_TYPE = 4;
  public static final short ORDERED_NODE_ITERATOR_TYPE = 5;
  public static final short UNORDERED_NODE_SNAPSHOT_TYPE = 6;
  public static final short ORDERED_NODE_SNAPSHOT_TYPE = 7;
  public static final short ANY_UNORDERED_NODE_TYPE = 8;
  public static final short FIRST_ORDERED_NODE_TYPE = 9;

  public short getResultType();

  public double getNumberValue() throws DOMXPathException;

  public String getStringValue() throws DOMXPathException;

  public Boolean getBooleanValue() throws DOMXPathException;

  public DOMNode getSingleNodeValue() throws DOMXPathException;

  public boolean getInvalidIteratorState();

  public long getSnapshotLength() throws DOMXPathException;

  public DOMNode iterateNext() throws DOMXPathException;

  public DOMNode snapshotItem(int index) throws DOMXPathException;
}