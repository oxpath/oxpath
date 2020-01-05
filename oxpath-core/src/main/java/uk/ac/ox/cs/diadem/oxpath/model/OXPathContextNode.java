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
 * Package containing supporting classes, derived from the OXPath model (which itself extends the XPath model).
 */
package uk.ac.ox.cs.diadem.oxpath.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Table;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.language.AggregatedStep;
import uk.ac.ox.cs.diadem.oxpath.model.language.Axis;
import uk.ac.ox.cs.diadem.oxpath.model.language.AxisType;
import uk.ac.ox.cs.diadem.oxpath.model.language.NodeTest;
import uk.ac.ox.cs.diadem.oxpath.model.language.NodeTestType;
import uk.ac.ox.cs.diadem.oxpath.model.language.OXPathAxis;
import uk.ac.ox.cs.diadem.oxpath.model.language.OXPathNodeTest;
import uk.ac.ox.cs.diadem.oxpath.model.language.Step;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathException;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathResult;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 * Class for representing OXPathContextNode. Acts as a wrapper for DOM nodes, decorated with parent marker and current
 * marker references.
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class OXPathContextNode implements Comparable<OXPathContextNode> {

  private static final Logger LOGGER = LoggerFactory.getLogger(OXPathContextNode.class);

  /**
   * Constructor for the class. Object "glues" together a DomNode in HtmlUnit's implementation, a reference to the
   * parent marker, and a reference to the last marker used
   *
   * @param iNode
   *          DomNode in browser to use
   * @param iParent
   *          reference to parent marker
   * @param iLast
   *          reference to current marker
   */
  public OXPathContextNode(final DOMNode iNode, final int iParent, final int iLast) {
    node = iNode;
    parent = iParent;
    last = iLast;
  }

  /**
   * Gets the object node
   *
   * @return the DomNode
   */
  public DOMNode getNode() {
    return node;
  }

  /**
   * Gets the reference to the parent marker
   *
   * @return reference to the parent marker
   */
  public int getParent() {
    return parent;
  }

  /**
   * Gets the reference to the last marker
   *
   * @return reference to the last marker
   */
  public int getLast() {
    return last;
  }

  @Override
  public String toString() {
    return (this.getClass() + "[" + getNode().toString() + ", " + getParent() + ", " + getLast() + "]");
  }

  /**
   * Returns the node by calling the getByXPath in HtmlUnit. Only use when return value is an XPath nodeset data type.
   *
   * @param stepString
   *          xpath query as a String
   * @param forward
   *          {@code true} for forward navigation, {@code false} otherwise
   * @return OXPathNodeList with all relevant nodes
   * @throws OXPathException
   *           in case of error on adding nodes to the list (return value of xpath call is not a nodelist)
   */
  public OXPathType getByXPath(final String stepString, final boolean forward) throws OXPathException {
    return this.getByXPath(stepString, forward, false);
  }

  /**
   * Returns the node by calling the getByXPath in HtmlUnit. Only use when return value is an XPath nodeset data type.
   *
   * @param stepString
   *          xpath query as a String
   * @param forward
   *          {@code true} for forward navigation, {@code false} otherwise
   * @param returnsNodes
   *          {@code true} if navigation returns a nodeset, {@code false} if this is unknown
   * @return OXPathNodeList with all relevant nodes
   * @throws OXPathException
   *           in case of error on adding nodes to the list (return value of xpath call is not a nodelist)
   */
  public OXPathType getByXPath(final String stepString, final boolean forward, final boolean returnsNodes)
      throws OXPathException {
    if (LOGGER.isInfoEnabled()) LOGGER.info("Evaluating in browser plain xpath {} on context node {}", stepString, this);
    final DOMNode context = getNode();
    final DOMXPathEvaluator xpathEvaluator = context.getXPathEvaluator();

    // final short resultType = (returnsNodes) ? DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE : DOMXPathResult.ANY_TYPE;
    // To limit document position ordering, we rely on the browser
    final short resultType = (returnsNodes) ? DOMXPathResult.ORDERED_NODE_SNAPSHOT_TYPE : DOMXPathResult.ANY_TYPE;
    // since we are passing in XPath, no extraction is encountered, so parent and current are the same
    DOMXPathResult iResult = xpathEvaluator.evaluate(stepString, context, xpathEvaluator.createNSResolver(context),
        resultType, null);

    // build the correct OXPathType from our result
    IOXPathNodeList nodes;
    final long snapshotLength = iResult.getSnapshotLength();

    switch (iResult.getResultType()) {

    case DOMXPathResult.NUMBER_TYPE:
    	if (LOGGER.isDebugEnabled())  LOGGER.debug("getByXPath retrieved {}  as result of {}", iResult.getNumberValue(), stepString);
      return new OXPathType(iResult.getNumberValue());

    case DOMXPathResult.STRING_TYPE:
    	if (LOGGER.isDebugEnabled()) LOGGER.debug("getByXPath retrieved {}  as result of {}", iResult.getStringValue(), stepString);
      return new OXPathType(iResult.getStringValue());

    case DOMXPathResult.BOOLEAN_TYPE:
    	if (LOGGER.isDebugEnabled()) LOGGER.debug("getByXPath retrieved {}  as result of {}", iResult.getBooleanValue(), stepString);
      return new OXPathType(iResult.getBooleanValue());

    case DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE:
      // based on our evaluate method above, this should be the only kind of nodeset we see

    case DOMXPathResult.ORDERED_NODE_SNAPSHOT_TYPE:
    	if (LOGGER.isDebugEnabled()) LOGGER.debug("getByXPath retrieved {} nodes as result of {}", snapshotLength, stepString);
      // nodes = (forward) ? OXPathNodeListFactory.newListOnSortedSet() :
      // OXPathNodeListFactory.newListOnSortedSet(Collections
      // .reverseOrder());

      nodes = OXPathNodeListFactory.newMutableOnLinkedSet();

      if (forward) {

        for (long i = 0; i < snapshotLength; i++) {
          try {
            nodes.add(new OXPathContextNode(iResult.snapshotItem((int) i), getParent(), getLast()));
          } catch (final WebAPIRuntimeException e) {
            LOGGER.error("Skipping unsupported dome element {}", e.getMessage());
            continue;
          }
        }
      } else {
        // reverse order
        for (long i = snapshotLength - 1; i >= 0; i--) {
          try {
            nodes.add(new OXPathContextNode(iResult.snapshotItem((int) i), getParent(), getLast()));
          } catch (final WebAPIRuntimeException e) {
            LOGGER.error("Skipping unsupported dome element {}", e.getMessage());
            continue;
          }
        }
      }

      if (LOGGER.isInfoEnabled()) LOGGER.info("...result node size is {}", nodes.size());
      if (nodes.isEmpty())
        return OXPathType.EMPTYRESULT;
      return new OXPathType(nodes);

    case DOMXPathResult.UNORDERED_NODE_ITERATOR_TYPE:
      nodes = (forward) ? OXPathNodeListFactory.newMutableOnLinkedSet() : OXPathNodeListFactory
          .newMutableOnSortedSet(Collections.reverseOrder());
      boolean done = false;
      boolean rerun = false;
      while (!done) {
        try {
          if (iResult.getInvalidIteratorState()) {
            iResult = xpathEvaluator.evaluate(stepString, context, xpathEvaluator.createNSResolver(context),
                DOMXPathResult.ANY_TYPE, null);
            rerun = true;
            done = true;
          } else {
            DOMNode node = null;
            try {
              node = iResult.iterateNext();
            } catch (final WebAPIRuntimeException e) {
              LOGGER.error("Skipping unsupported dome element {}", e.getMessage());
              done = true;
            }
            if (node == null) {
              done = true;
            } else {
              nodes.add(new OXPathContextNode(node, getParent(), getLast()));
            }
          }
        } catch (final DOMXPathException e) {// it's possible for this to happen between the check and our invocation of
          // iterateNext
          // e.printStackTrace();
          rerun = true;
          done = true;
        }
      }
      if (rerun) {
        iResult = xpathEvaluator.evaluate(stepString, context, xpathEvaluator.createNSResolver(context),
            DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);
        nodes = OXPathNodeListFactory.newMutableOnLinkedSet();
        for (long i = 0; i < snapshotLength; i++) {
          try {
            nodes.add(new OXPathContextNode(iResult.snapshotItem((int) i), getParent(), getLast()));
          } catch (final WebAPIRuntimeException e) {
            LOGGER.error("Skipping unsupported dome element {}", e.getMessage());
            continue;
          }
        }
      }
      if (LOGGER.isDebugEnabled()) LOGGER.debug("getByXPath retrieved {} nodes as result of {}", nodes.size(), stepString);
      if (nodes.isEmpty())
        return OXPathType.EMPTYRESULT;
      return new OXPathType(nodes);// we only sort when necessary
      // if (forward) return new OXPathType(nodes.sortForwardOrder());
      // else return new OXPathType(nodes.sortReverseOrder());
    default:
      throw new OXPathException("The browser broke the contract for the XPath evaluator interface!");
    }

  }

  public OXPathType contextualize(final List<DOMNode> domNodes) {
    if (domNodes == null)
      return null;
    final IOXPathNodeList nodeList = OXPathNodeListFactory.newMutableOnLinkedSet();
    for (final DOMNode domNode : domNodes) {
      nodeList.add(new OXPathContextNode(domNode, getParent(), getLast()));
    }
    return new OXPathType(nodeList);
  }

  /**
   * Returns the node by calling the getByXPath in the browser's XPath API. Only use when return value is an XPath
   * nodeset data type. Shortcut for the more verbose signature of the other {@code getByXPath} method
   *
   * @param stepString
   *          xpath query as a String
   * @return OXPathNodeList with all relevant nodes
   * @throws OXPathException
   *           in case of error on adding nodes to the list (return value of xpath call is not a nodelist)
   */
  public OXPathType getByXPath(final String stepString) throws OXPathException {
    return this.getByXPath(stepString, true);
  }

  /**
   * Returns the node by calling the getByXPath in the browser's XPath API. Only use when return value is an XPath
   * nodeset data type.
   *
   * @param step
   *          the step to pass to the browser's XPath API
   * @return the resulting nodeset as an {@code OXPathType}
   * @throws OXPathException
   *           in case of error on adding nodes to the list (return value of xpath call is not a nodelist)
   */
  public OXPathType getByOXPath(final Step step) throws OXPathException {
    if (step instanceof AggregatedStep)
      return this.getByXPath(step.toOXPath());
    return this.getByOXPath(step.getAxis(), step.getNodeTest());
  }

  /**
   * Returns the node by calling the getByXPath in the browser's XPath API. Only use when return value is an XPath
   * nodeset data type.
   *
   * @param aggregatedStep
   *          the series of steps (as a {@code String}) to pass to the browser's XPath API as a single, consolidated
   *          call.
   * @return the resulting nodeset as an {@code OXPathType}
   * @throws OXPathException
   *           in case of error on adding nodes to the list (return value of xpath call is not a nodelist)
   */
  public OXPathType getByOXPath(final AggregatedStep aggregatedStep) throws OXPathException {
    final boolean isForward = aggregatedStep.getAxis().getType().equals(AxisType.FORWARD);
    return this.getByXPath(aggregatedStep.getPathExpression(), isForward, true);
  }

  /**
   * Returns the node by calling the getByXPath in the browser's XPath API. Only use when return value is an XPath
   * nodeset data type.
   *
   * @param axis
   *          axis for the step to be evaluated
   * @param nodeTest
   *          node test for the step to be evaluated
   * @return the resulting nodeset as an {@code OXPathType}
   * @throws OXPathException
   *           in case of error on adding nodes to the list (return value of xpath call is not a nodelist)
   */
  private OXPathType getByOXPath(final Axis axis, final NodeTest nodeTest) throws OXPathException {
    if ((!axis.getType().equals(AxisType.OXPATH)) && (!nodeTest.getType().equals(NodeTestType.OXPATH))) {
      // this step is just OXPath, so we can use the OXPath engine to get it
      final boolean isForward = axis.getType().equals(AxisType.FORWARD);
      return this.getByXPath(axis.getValue() + nodeTest.getValue(), isForward, true);
    } else if (axis.getType().equals(AxisType.OXPATH))
      return ((OXPathAxis) axis).evaluate(this, nodeTest);
    else
      return ((OXPathNodeTest) nodeTest).evaluate(this, axis);
  }

  /**
   * Returns a unique {@code OXPathContextNode} object, as a placeholder for beginning OXPath expression evaluation
   * before a root node is retrieved via the <tt>doc(uri)</tt> function. Also used as a null context in iterative
   * evaluation, so that null pointers are avoided.
   *
   * @return the unique notional Context
   */
  public static OXPathContextNode getNotionalContext() {
    return OXPathContextNode.notionalContext;
  }

  /**
   * Determines if the implicit parameter is the unique notional context
   *
   * @return {@code true} if this object is the notional context, {@code false} otherwise
   */
  public boolean isNotionalContext() {
    return equals(OXPathContextNode.notionalContext);
  }

  /**
   * Standard comparator for nodes. Unfortunately, the interface does not allow an exception to be raised if the nodes
   * are not comparable, so care should be taken before sorting (perhaps by checking all nodes are in the same
   * document). Currently returns {@code Integer.MAX_VALUE} if not a number
   *
   * @param other
   *          first OXPathContextNode to compare
   * @return standard Java Comparator convention
   */
  @Override
  public int compareTo(final OXPathContextNode other) {
    // compare position of DOMNodes
    // for this op to be consistent with equals, two OXPath nodes with different parent extraction markers but same
    // DOMNode
    // aren't allowed; this won't occur if using regular OXPath
    final DOMNode n1 = getNode();
    final DOMNode n2 = other.getNode();

    short position;
    try {
      position = n1.compareDocumentPosition(n2);
      // System.out.println(n1.getNodeName() + " : " + n2.getNodeName() + " = " + position);
    } catch (final NullPointerException e) {// in case the notional context is being compared to something
      position = DOCUMENT_POSITION_DISCONNECTED;// not if both null, so as to be consistent with equals
    }

    // This seems counterintuitive, but this is what we want based on Java's definition of "natural ordering" - for us,
    // we assume document order to be the natural ordering, so preceding nodes have higher ordering values
    if (position == 0)
      return other.getParent() - getParent();
    else if (((position & DOCUMENT_POSITION_PRECEDING) == DOCUMENT_POSITION_PRECEDING)
        || ((position & DOCUMENT_POSITION_CONTAINS) == DOCUMENT_POSITION_CONTAINS))
      return 1;
    else if (((position & DOCUMENT_POSITION_FOLLOWING) == DOCUMENT_POSITION_FOLLOWING)
        || ((position & DOCUMENT_POSITION_CONTAINED_BY) == DOCUMENT_POSITION_CONTAINED_BY))
      return -1;
    // else return Integer.MAX_VALUE;
    else if ((position & DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC) == DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC) {

      // in order to establish a consistent total ordering (albeit an arbitrary one), we use the hash values of the
      // containing documents
      // final int diff = n1.getOwnerDocument().hashCode() - n2.getOwnerDocument().hashCode();
      // EDIT:@Gio we use the hashcode of the nodes, as it is possible to have disconnected nodes on the same document
      final int diff = n1.hashCode() - n2.hashCode();
      if (diff != 0)
        return diff;
      else
        throw new RuntimeException("Browser returned the same Document hashcode for disconnected nodes.");
    } else
      throw new RuntimeException("Browser broke compareDocumentPosition contract with return value");
  }

  public int compareTo(final OXPathContextNodeConstructed other) {
    return Integer.MAX_VALUE;// we want all Browser-based nodes to be greater than our constructed nodes in the list
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + last;
    result = (prime * result) + ((node == null) ? 0 : node.hashCode());
    result = (prime * result) + parent;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final OXPathContextNode other = (OXPathContextNode) obj;
    if (last != other.last)
      return false;
    if (node == null) {
      if (other.node != null)
        return false;
    } else if (other.node == null)
      return false;
    else if (!node.isSameNode(other.node))
      return false;
    if (parent != other.parent)
      return false;
    return true;
  }

  /**
   * instance field storing the node
   */
  private final DOMNode node;
  /**
   * instance field storing the reference to the parent marker
   */
  private final int parent;
  /**
   * instance field storing the reference to the current marker
   */
  private final int last;
  /**
   * encodes the notional context node for beginning navigation; the parent and last are both 0, the id for the
   * "results" root in the output
   */
  private static final OXPathContextNode notionalContext = new OXPathContextNodeConstructed("notional", "top", 0, 0);

  // DocumentPosition constants
  /**
   * Document position constants
   */
  protected static final short DOCUMENT_POSITION_DISCONNECTED = 0x01;
  /**
   * Document position constants
   */
  protected static final short DOCUMENT_POSITION_PRECEDING = 0x02;
  /**
   * Document position constants
   */
  protected static final short DOCUMENT_POSITION_FOLLOWING = 0x04;
  /**
   * Document position constants
   */
  protected static final short DOCUMENT_POSITION_CONTAINS = 0x08;
  /**
   * Document position constants
   */
  protected static final short DOCUMENT_POSITION_CONTAINED_BY = 0x10;
  /**
   * Document position constants
   */
  protected static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 0x20;

  public Table<DOMNode, String, List<DOMNode>> getByXPathBulk(final String pathToAnchorNode,
      final Set<String> contextualAttributeNodes) {
	  if (LOGGER.isInfoEnabled()) LOGGER.info("getByXPathBulk on {} and relative {} expressions", pathToAnchorNode, contextualAttributeNodes.size());
    final DOMNode context = getNode();
    final DOMXPathEvaluator xPathEvaluator = context.getXPathEvaluator();
    return xPathEvaluator.evaluateBulk(context, pathToAnchorNode, contextualAttributeNodes);
    // TODO evaluate xpath(context,pathToAnchorNode) --> record
    // and then for each contextualAttributes A evaluate xpath(record,A)
    // return a table [ contextNode,relativeXPath,resultNode] where for the record node the relative path it's
    // self::node()
  }

}
