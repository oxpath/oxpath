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
package uk.ac.ox.cs.diadem.webapi.dom.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNamedNodeMap;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNodeList;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMEventListener;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver.MutationObserverOptions;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 * @author Giorgio Orsi <giorgio dot orsi at cs dot ox dot ac dot uk> Oxford University, Department of Computer Science.
 * 
 *         This abstract class implements a generic DOMNode decorator (see Decorator Design Pattern at
 *         http://en.wikipedia.org/wiki/Decorator_pattern). This generic decorator does not provide any additional
 *         method to the DOMNode nor any additional attribute.
 */
public abstract class DOMNodeDecorator implements DOMNode {

  private static final Logger logger = LoggerFactory.getLogger(DOMNodeDecorator.class);
  protected DOMNode decoratedNode;

  public DOMNodeDecorator(final DOMNode node) {

    decoratedNode = node;
  }

  @Override
  public DOMMutationObserver registerMutationObserver(final MutationObserverOptions options) {
    throw new WebAPIRuntimeException("unsupported registerMutationObserver", logger);
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.event.DOMEventTarget#addEventListener(java .lang.String,
   * uk.ac.ox.cs.diadem.webapi.dom.event.DOMEventListener, boolean)
   */
  @Override
  public void addEventListener(final String type, final DOMEventListener listener, final boolean useCapture) {

    decoratedNode.addEventListener(type, listener, useCapture);
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.event.DOMEventTarget#removeEventListener (java.lang.String,
   * uk.ac.ox.cs.diadem.webapi.dom.event.DOMEventListener, boolean)
   */
  @Override
  public void removeEventListener(final String type, final DOMEventListener listener, final boolean useCapture) {

    decoratedNode.removeEventListener(type, listener, useCapture);
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getNodeType()
   */
  @Override
  public Type getNodeType() {

    return (decoratedNode.getNodeType());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getChildNodes()
   */
  @Override
  public DOMNodeList getChildNodes() {

    return (decoratedNode.getChildNodes());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getNodeValue()
   */
  @Override
  public String getNodeValue() {

    return (decoratedNode.getNodeValue());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getParentNode()
   */
  @Override
  public DOMNode getParentNode() {

    return (decoratedNode.getParentNode());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getLocalName()
   */
  @Override
  public String getLocalName() {

    return (decoratedNode.getLocalName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getAttributes()
   */
  @Override
  public DOMNamedNodeMap<DOMNode> getAttributes() {

    return (decoratedNode.getAttributes());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getNodeName()
   */
  @Override
  public String getNodeName() {

    return (decoratedNode.getNodeName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#toPrettyHTML()
   */
  @Override
  public String toPrettyHTML() {

    return (decoratedNode.toPrettyHTML());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getTextContent()
   */
  @Override
  public String getTextContent() {

    return (decoratedNode.getTextContent());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#isDescendant(uk.ac.ox.cs.diadem .webapi.dom.DOMNode)
   */
  @Override
  public boolean isDescendant(final DOMNode node) {

    return (decoratedNode.isDescendant(node));
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#isVisible()
   */
  @Override
  public boolean isVisible() {

    return (decoratedNode.isVisible());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getXPathEvaluator()
   */
  @Override
  public DOMXPathEvaluator getXPathEvaluator() {

    return (decoratedNode.getXPathEvaluator());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#compareDocumentPosition(uk.ac.ox .cs.diadem.webapi.dom.DOMNode)
   */
  @Override
  public short compareDocumentPosition(final DOMNode other) {

    return (decoratedNode.compareDocumentPosition(other));
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getOwnerDocument()
   */
  @Override
  public DOMDocument getOwnerDocument() {

    return (decoratedNode.getOwnerDocument());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#isSameNode(uk.ac.ox.cs.diadem.webapi .dom.DOMNode)
   */
  @Override
  public boolean isSameNode(final DOMNode other) {

    return (decoratedNode.isSameNode(other));
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#appendChild(uk.ac.ox.cs.diadem. webapi.dom.DOMNode)
   */
  @Override
  public DOMNode appendChild(final DOMNode newChild) {

    return (decoratedNode.appendChild(newChild));
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#removeChild(uk.ac.ox.cs.diadem. webapi.dom.DOMNode)
   */
  @Override
  public DOMNode removeChild(final DOMNode child) {

    return (decoratedNode.removeChild(child));
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#replaceChild(uk.ac.ox.cs.diadem .webapi.dom.DOMNode,
   * uk.ac.ox.cs.diadem.webapi.dom.DOMNode)
   */
  @Override
  public DOMNode replaceChild(final DOMNode newChild, final DOMNode oldChild) {

    return (decoratedNode.replaceChild(newChild, oldChild));
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#insertBefore(uk.ac.ox.cs.diadem .webapi.dom.DOMNode,
   * uk.ac.ox.cs.diadem.webapi.dom.DOMNode)
   */
  @Override
  public DOMNode insertBefore(final DOMNode newChild, final DOMNode refChild) {

    return (decoratedNode.insertBefore(newChild, refChild));
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#setTextContent(java.lang.String)
   */
  @Override
  public void setTextContent(final String text) {

    decoratedNode.setTextContent(text);
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getPreviousSibling()
   */
  @Override
  public DOMNode getPreviousSibling() {

    return (decoratedNode.getPreviousSibling());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getNextSibling()
   */
  @Override
  public DOMNode getNextSibling() {

    return (decoratedNode.getNextSibling());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getLastChild()
   */
  @Override
  public DOMNode getLastChild() {

    return (decoratedNode.getLastChild());
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getFirstChild()
   */
  @Override
  public DOMNode getFirstChild() {

    return (decoratedNode.getFirstChild());
  }
}
