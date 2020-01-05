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
 * Package supporting core OXPath functionality.  Contains the interface and implementation for
 * retrieving current DOM references from references on old DOMs (obtained when the DOM was
 * previously rendered before a {@code browser.back()} call.
 */
package uk.ac.ox.cs.diadem.oxpath.core.domlookup;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;

/**
 * Supports the DOM lookup functionality that creates unique references for finding fresh nodes in new documents.
 * Remember to handle the notional root if passed it!
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
public interface NodeReference {

  /**
   * Returns the rendered node from the current document based on the reference
   *
   * @param document
   *          the document to find the fresh node
   * @return the rendered node from the current document based on the reference
   * @throws an
   *           OXPathException if its not possible to retrieve the node on the document
   */
  public OXPathContextNode getRenderedNodeOrThrow(DOMDocument document) throws OXPathException;

  public OXPathContextNode getRenderedNodeOrNull(DOMDocument document);

}
