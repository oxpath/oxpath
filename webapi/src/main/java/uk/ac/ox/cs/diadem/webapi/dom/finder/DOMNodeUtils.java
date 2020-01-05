package uk.ac.ox.cs.diadem.webapi.dom.finder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIException;

/**
 * A collection of DOM helpers, such as the lowest common ancestor computation.
 * 
 * @author Christian Schallhart <christian@schallhart.net>
 * 
 */
public class DOMNodeUtils {

  private final static Logger LOGGER = LoggerFactory.getLogger(DOMNodeUtils.class);

  /**
   * Gets the lowest common ancestor of a list of DOM nodes.
   * 
   * @param nodes
   *          the collection of DOM nodes
   * @return the DOM node representing the lowest common ancestor of all DOM nodes in the collection
   * @throws WebAPIException
   *           if the node collections is empty.
   */
  public static DOMNode getLowestCommonAncestor(Collection<DOMNode> nodes) throws WebAPIException {
    assert nodes != null;
    if (nodes.size() == 0)
      throw new WebAPIException("Attmept to find an LCA from an empty node set.", LOGGER);
    if (nodes.size() == 1)
      return nodes.iterator().next();

    // determines the paths from each node to the root
    final List<Stack<DOMNode>> nodePaths = new ArrayList<Stack<DOMNode>>();
    for (final DOMNode node : nodes) {
      final Stack<DOMNode> path = new Stack<DOMNode>();
      path.push(node);
      DOMNode parent = node.getParentNode();
      while (parent != null) {
        path.push(parent);
        parent = parent.getParentNode();
      }
      nodePaths.add(path);
    }

    // finds the shortest path
    int minLength = Integer.MAX_VALUE;
    for (final Stack<DOMNode> path : nodePaths)
      minLength = Math.min(minLength, path.size());

    // pop the stack to find the lca. the lca is the first node where the
    // paths divide.
    DOMNode lca = null;
    for (int i = 0; i < minLength; i++) {
      final DOMNode node = nodePaths.get(0).pop();
      for (int j = 1; j < nodePaths.size(); j++)
        if (!node.equals(nodePaths.get(j).pop())) {
          if (lca == null)
            throw new WebAPIException("No common ancestor found.", LOGGER);
          return lca;
        }
      lca = node;
    }
    return lca;
  }

  public static DOMNode getLowestCommonAncestor(DOMNode node, DOMNode... nodes) throws WebAPIException {
    assert node != null;
    assert nodes != null;
    if (nodes.length == 0)
      return node;
    final Set<DOMNode> nodeSet = new HashSet<DOMNode>();
    nodeSet.add(node);
    for (DOMNode n : nodes)
      nodeSet.add(n);
    return getLowestCommonAncestor(nodeSet);
  }

  public static DOMNode getElementAncestor(DOMNode node) {
    assert node != null;
    for (; (node != null) && !(node.getNodeType().equals(DOMNode.Type.ELEMENT)); node = node.getParentNode())
      ;
    return node;
  }

  // DO NOT DELETE
  //
  // public static String getCanonicalXPath(DOMNode from, DOMNode to) throws WebAPIException {
  // assert from != null;
  // assert to != null;
  // return getCanonicalXPathInternal(from, to);
  // }
  //
  // /**
  // * Computes the canonical XPath from the document root to node.
  // *
  // * @param to
  // * @return
  // * @throws WebAPIException
  // */
  // public static String getCanonicalXPath(final DOMNode to) throws WebAPIException {
  // assert to != null;
  // return getCanonicalXPathInternal(null, to);
  // }
  //
  // /**
  // * Gets the canonical OXPath for a DOM node.
  // *
  // * @param node
  // * the DOM node
  // * @return the canonical XPath
  // * @throws WebAPIException
  // */
  // private static String getCanonicalXPathInternal(DOMNode from, DOMNode to) throws WebAPIException {
  // String localName = null;
  // int position = 0;
  //
  // if (from == to)
  // return "/.";
  //
  // switch (to.getNodeType()) {
  // case ELEMENT:
  // localName = to.getLocalName();
  // position = FinderUtils.getSameNameSiblingPosition(to);
  // break;
  // case TEXT:
  // localName = "text()";
  // position = FinderUtils.getSameNameSiblingPosition(to);
  // break;
  // case DOCUMENT:
  // if (from != null)
  // throw new WebAPIException("Attmept to compute a canonical path from a non-ancestor node.", LOGGER);
  // return "/.";
  //
  // case ATTRIBUTE:
  // case CDATA_SECTION:
  // case COMMENT:
  // case DOCUMENT_FRAGMENT:
  // case DOCUMENT_TYPE:
  // case ENTITY:
  // case ENTITY_REFERENCE:
  // case NOTATION:
  // case PROCESSING_INSTRUCTION:
  // throw new WebAPIException("Cannocial path to a node of unhandled type '" + to.getNodeType() + "'.", LOGGER);
  // }
  // return getCanonicalXPathInternal(from, to.getParentNode()) + "/" + localName + "[" + position + "]";
  // }

}
