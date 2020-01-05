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
package uk.ac.ox.cs.diadem.webapi.dom.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNamedNodeMap;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNodeList;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver.MutationObserverOptions;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 *
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
abstract class AbstractNodeWrapper<N> implements DOMNode {

  static final Logger LOG = LoggerFactory.getLogger(AbstractNodeWrapper.class);

  abstract N getWrappedNode();

  @Override
  public boolean isStale() {
    return false;
  }

  @Override
  public String getXPathLocator() {
    throw new WebAPIRuntimeException("getXPathLocator not implemented yet", LOG);

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {

    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((getWrappedNode() == null) ? 0 : getWrappedNode().hashCode());
    return result;
  }

  @Override
  public String toString() {
    return getNodeType() + " : " + getNodeName();
    // return getWrappedNode().toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(final Object obj) {

    if (this == obj)
      return true;
    if (obj == null)
      return false;
    final AbstractNodeWrapper<N> other = (AbstractNodeWrapper<N>) obj;
    if (getWrappedNode() == null) {
      if (other.getWrappedNode() != null)
        return false;
    } else if (!getWrappedNode().equals(other.getWrappedNode()))
      return false;
    return true;
  }

  @Override
  public String toPrettyHTML() {

    if (Type.CDATA_SECTION == getNodeType())
      return "<![CDATA[" + getNodeValue() + "]]&gt;";
    if (getNodeName().startsWith("#"))
      return "";
    final StringBuffer sb = new StringBuffer();
    sb.append('<').append(getNodeName());
    final DOMNamedNodeMap<?> attrs = getAttributes();
    if (attrs != null) {
      for (int i = 0; i < attrs.getLength(); i++) {
        sb.append(' ').append((attrs.item(i).getNodeName())).append("=\"").append((attrs.item(i)).getNodeValue())
            .append("\"");
      }
    }
    String textContent = null;
    final DOMNodeList children = getChildNodes();
    if (children.getLength() == 0) {
      if (((textContent = getTextContent()) != null) && !"".equals(textContent)) {
        sb.append(textContent).append("</").append(getNodeName()).append('>');
        ;
      } else {
        sb.append("/>");// .append('\n');
      }
    } else {
      sb.append('>');// .append('\n');
      boolean hasValidChildren = false;
      for (int i = 0; i < children.getLength(); i++) {
        final String childToString = children.item(i).toPrettyHTML();
        if (!"".equals(childToString)) {
          sb.append(childToString);
          hasValidChildren = true;
        }
      }
      if (!hasValidChildren && ((textContent = getTextContent()) != null)) {
        sb.append(textContent);
      }
      sb.append("</").append(getNodeName()).append('>');
    }
    return sb.toString();
  }

  @Override
  public boolean isDescendant(final DOMNode node) {

    DOMNode parent = getParentNode();
    while (parent != null) {
      if (parent.equals(node))
        return true;
      parent = parent.getParentNode();
    }
    return false;
  }

  @Override
  public boolean isTextNode() {
    return getNodeType() == Type.TEXT;
  }

  @Override
  public boolean isVisible() {

    assert this != null;
    if (this instanceof DOMDocument)
      return true;
    if (this instanceof DOMElement) {
      final DOMElement el = (DOMElement) this;
      if (el.getComputedStyle().getPropertyValue("display").equals("none"))
        return false;
      if (el.getComputedStyle().getPropertyValue("visibility").equals("hidden"))
        return false;
    }
    final DOMNode parent = getParentNode();
    if (parent == null)
      return true;
    return parent.isVisible();
  }

  @Override
  public DOMMutationObserver registerMutationObserver(final MutationObserverOptions options) {
    LOG.error("Unsupported DOMMutationObserver for this browser, returning null");
    return null;
  }

  @Override
  public DOMMutationObserver registerMutationObserver(final boolean childList, final boolean attributes,
      final boolean subtree, final boolean characterData, final List<String> attributeFilter) {
    LOG.error("Unsupported DOMMutationObserver for this browser, returning null");
    return null;
  }

  // @Override
  // public Object getInternaAPIObject() {
  // return getWrappedNode();
  // }

  @Override
  public Object executeJavaScript(final String script, final Object... arg) {
    LOG.error("not implemented executeJavaScript for this browser");
    return null;
  }

}
