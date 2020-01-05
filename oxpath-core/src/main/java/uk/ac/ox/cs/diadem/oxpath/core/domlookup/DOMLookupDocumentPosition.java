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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.IOXPathNodeList;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathNodeListFactory;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathResult;

/**
 * An implementation for the DOM Lookup Position based on document order
 *
 * @author AndrewJSel
 * @deprecated it proved to be brittle
 */
@Deprecated
public class DOMLookupDocumentPosition implements DOMLookup {

  private static final Logger LOGGER = LoggerFactory.getLogger(DOMLookupDocumentPosition.class);
  private final boolean throwOnFailure;

  /**
   * empty constructor
   */
  public DOMLookupDocumentPosition(final boolean throwOnFailure) {
    this.throwOnFailure = throwOnFailure;

  }

  /**
   * Creates a list of references to nodes so that they can be found in a new document
   *
   * @param nodes
   *          the list of OXPathNodes
   * @return references to these nodes retrievable in a new document
   * @throws OXPathException
   *           in case of browser error (will carry the throwable cause)
   */
  @Override
  public ArrayList<NodeReference> getNodeReferences(final IOXPathNodeList nodes) throws OXPathException {
    final ArrayList<NodeReference> result = new ArrayList<NodeReference>();
    for (final OXPathContextNode node : nodes) {
      result.add(new NodeReferenceDocumentPosition(node));
    }
    return result;
  }

  @Override
  public NodeReference getNodeReference(final OXPathContextNode context) {
    return new NodeReferenceDocumentPosition(context);
  }

  /**
   * This DOMLookup versions of references
   *
   * @author AndrewJSel
   *
   */
  private class NodeReferenceDocumentPosition implements NodeReference {

    /**
     * Creates a node reference based on document position. Remember to handle the notional root if passed it!
     *
     * @param node
     *          node to reference
     */
    private NodeReferenceDocumentPosition(final OXPathContextNode node) {

      if (node.equals(OXPathContextNode.getNotionalContext())) {
        order = NOTIONALCONTEXTORDER;
        parent = OXPathContextNode.getNotionalContext().getParent();
        last = OXPathContextNode.getNotionalContext().getLast();
      } else {
        final DOMNode domnode = node.getNode();
        final DOMXPathEvaluator xpathnode = domnode.getXPathEvaluator();
        final DOMXPathResult resultvalue = xpathnode.evaluate(STALEQUERY, domnode,
            xpathnode.createNSResolver(domnode.getOwnerDocument()), DOMXPathResult.ANY_TYPE, null);
        order = resultvalue.getNumberValue();
        parent = node.getParent();
        last = node.getLast();
        // Giog: added to sanity check on retrieved nodes
        tag = node.getNode().getNodeName();
      }
    }

    /**
     * Returns the rendered node from the current document based on the reference
     *
     * @param document
     *          the document to find the fresh node
     * @return the rendered node from the current document based on the reference
     */
    @Override
    public OXPathContextNode getRenderedNodeOrThrow(final DOMDocument document) throws OXPathException {
      if (order == NOTIONALCONTEXTORDER)
        return OXPathContextNode.getNotionalContext();
      final DOMXPathEvaluator xpathFresh = document.getXPathEvaluator();
      final DOMXPathResult resultFresh = xpathFresh.evaluate(getFreshQuery(order), document,
          xpathFresh.createNSResolver(document), DOMXPathResult.ANY_TYPE, null);
      final DOMNode fresh = resultFresh.iterateNext();
      // Gio: ADDED CHECK TO MAKE SURE WE CARRY ON WITH THE EXPECTED NODES, OTHERWISE WE ABORT.
      // THIS IS MEANT TO BE FIXED WITH MORE ROBUST NODE RETRIEVE
      if (tag.equals(fresh.getNodeName()))
        return new OXPathContextNode(fresh, parent, last);
      else {
        LOGGER
            .error(
                "Cannot retrieve the same node on the current page: expected node '{}' but retrieved node '{}'. The current page must have different",
                tag, fresh.getNodeName());
        throw new OXPathException("Retrieved node by position with tag name: " + fresh.getNodeName()
            + ", which is different from the expected one: " + tag);
      }
    }

    @Override
    public String toString() {
      return "[parent:" + parent + " order:" + order + " last:" + last + "]";
    }

    /**
     * document order of node
     */
    private final double order;
    /**
     * parent of node
     */
    private final int parent;
    /**
     * last sibling of node
     */
    private final int last;
    /**
     * Added for sanity check on retrieved nodes
     */
    private String tag;

    @Override
    public OXPathContextNode getRenderedNodeOrNull(final DOMDocument document) {
      try {
        return getRenderedNodeOrThrow(document);
      } catch (final OXPathException e) {
        return null;
      }
    }

  }

  /**
   * The notional context reference for this class
   */
  private static final double NOTIONALCONTEXTORDER = -1.0;

  /**
   * Used to construct the fresh query prefix based on the result of the stale query
   *
   * @param order
   *          the document order of the fresh node
   * @return the document order of the fresh node
   */
  private String getFreshQuery(final double order) {
    return FRESHQUERYPREFIX + order + FRESHQUERYSUFFIX;
  }

  /**
   * Query to use from stale nodes to get a document count
   */
  public static final String STALEQUERY = "count(ancestor::*) + count(preceding::*)";
  /**
   * Prefix expression to get fresh nodes
   */
  public static final String FRESHQUERYPREFIX = "descendant::*[";
  /**
   * Suffix expression to get fresh nodes
   */
  public static final String FRESHQUERYSUFFIX = "+1]";

  @Override
  public IOXPathNodeList getRenderedNodes(final List<NodeReference> refs, final DOMDocument document)
      throws OXPathException {
    // FIXME BULK XPATH
    final IOXPathNodeList list = OXPathNodeListFactory.newMutableOnLinkedList();
    for (final NodeReference nodeReference : refs) {
      if (throwOnFailure) {
        list.add(nodeReference.getRenderedNodeOrThrow(document));
      } else {
        final OXPathContextNode contextNode = nodeReference.getRenderedNodeOrNull(document);
        if (contextNode != null) {
          list.add(contextNode);
        }
      }
    }
    return list;
  }

  @Override
  public WebBrowser getWebBrowser() {
    return null;
  }

}
