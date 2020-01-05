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

import java.util.List;
import java.util.Set;

import com.google.common.collect.Table;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;

/**
 * Provides XPath functionalities (http://www.w3.org/TR/DOM-Level-3-XPath/) For examples,
 * https://developer.mozilla.org/en/Introduction_to_using_XPath_in_JavaScript,
 * http://www.wrox.com/WileyCDA/Section/id-291861.html, and http://xqilla.sourceforge
 * .net/docs/dom3-api/classxercesc_1_1DOMXPathEvaluator.html
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Deparment of Computer Science
 */
public interface DOMXPathEvaluator {

  public DOMXPathExpression createExpression(String expression, DOMXPathNSResolver resolver) throws DOMXPathException;

  public DOMXPathNSResolver createNSResolver(DOMNode nodeResolver);

  /**
   * 
   * @param expression
   * @param contextNode
   * @param resolver
   * @param type
   * @param result
   *          NOTE: it is not used therefore null is expected, otherwise is ignored
   * @return
   * @throws DOMXPathException
   */
  public DOMXPathResult evaluate(String expression, DOMNode contextNode, DOMXPathNSResolver resolver, short type,
      Object result) throws DOMXPathException;

  public Table<DOMNode, String, List<DOMNode>> evaluateBulk(DOMNode context, String pathToAnchorNodes,
      Set<String> contextualAttributeNodes);
}