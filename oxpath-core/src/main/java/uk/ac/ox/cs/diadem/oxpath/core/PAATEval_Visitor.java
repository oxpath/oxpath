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
 * Package containing core OXPath functionality
 */
package uk.ac.ox.cs.diadem.oxpath.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

import uk.ac.ox.cs.diadem.oxpath.core.domlookup.NodeReference;
import uk.ac.ox.cs.diadem.oxpath.core.extraction.Extractor;
import uk.ac.ox.cs.diadem.oxpath.core.state.PAATState;
import uk.ac.ox.cs.diadem.oxpath.core.state.PAATStateEvalIterative;
import uk.ac.ox.cs.diadem.oxpath.core.state.PAATStateEvalSet;
import uk.ac.ox.cs.diadem.oxpath.model.IOXPathNodeList;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathNodeListFactory;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathType;
import uk.ac.ox.cs.diadem.oxpath.model.language.OXPathExtractionMarker;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTBinaryOpExpr;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTExpression;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTOXPathActionPath;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTOXPathExtractionMarker;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTOXPathKleeneStarPath;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTOXPathNodeTestOp;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTRelativeOXPathLocationPath;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTSimpleOXPathStepPath;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTXPathFunctionCall;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTXPathLiteral;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTXPathNumber;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTXPathPathExpr;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTXPathPredicate;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTXPathPrimaryExpr;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.ASTXPathUnaryExpr;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.Node;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.SimpleNode;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.custom.CustomSimpleNode;
import uk.ac.ox.cs.diadem.oxpath.parse.visitor.OXPathVisitorGenericAdaptor;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIStaleElementRuntimeException;

/**
 *
 * Visitor encoding the eval_ function of the PAAT algorithm. Called by the PAATEvalVisitor, but wrapped in a dynamic
 * proxy of a {PAATEval_Wrapper} object.
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
class PAATEval_Visitor extends OXPathVisitorGenericAdaptor<PAATStateEvalIterative, OXPathType> implements PAATEval_ {

  private static final Logger LOGGER = LoggerFactory.getLogger(PAATEval_Visitor.class);
  /**
   * The eval visitor that calls this method (the complementary object in the mutually recursive pair)
   */
  private final PAATEvalVisitor paatSet;
  /**
   * The object responsible for piping out OXPath extraction nodes
   */
  private final Extractor extractor;

  final Multimap<DOMDocument, DOMNode> documentToRecords = HashMultimap.create();
  Table<DOMNode, String, List<DOMNode>> recordsToAttributes = HashBasedTable.create();
  Deque<MutableBoolean> predicateStack = new ArrayDeque<MutableBoolean>();

  private DOMDocument currentDocumentReference;

  /**
   * basic constructor, with references to the calling visitor, the extractor, and the wrapper object
   *
   * @param paateval
   *          the calling {@code PAATEvalVisitor}
   * @param ext
   *          the extractor that handles output
   */
  public PAATEval_Visitor(final PAATEvalVisitor paateval, final Extractor ext) {
    paatSet = paateval;
    extractor = ext;
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final CustomSimpleNode node, final PAATStateEvalIterative data) throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  @Override
  public OXPathType visitNode(final SimpleNode node, final PAATStateEvalIterative data) throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTExpression node, final PAATStateEvalIterative data) throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTRelativeOXPathLocationPath node, final PAATStateEvalIterative data)
      throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTSimpleOXPathStepPath node, final PAATStateEvalIterative data)
      throws OXPathException {
    // get the results by OXPath step first
    OXPathType newContext = null;
    final OXPathContextNode contextNode = data.getContextNode();
    final DOMNode domContextNode = contextNode.getNode();

    try {
      if (node.isPlainXPath()) {// plain xpath means that we aggregated more steps together to be executed as a whole

        // first check if the nodes are in the cache for previous bulk loading
        if (isCached(domContextNode, node.getLocationPath().toOXPath())) {
          if (LOGGER.isDebugEnabled()) LOGGER.debug("Found cached for context:<{}> path:<{}>", domContextNode, node.getLocationPath().toOXPath());
          newContext = contextNode.contextualize(lookupRecordCache(domContextNode, node.getLocationPath().toOXPath()));
          if (LOGGER.isDebugEnabled()) LOGGER.debug("<{}> nodes cached for <{}>", newContext.nodeList().size(), node.getLocationPath().toOXPath());

        } else {
        	if (LOGGER.isDebugEnabled()) LOGGER.debug("Nothing cached for context:<{}> path:<{}>", domContextNode, node.getLocationPath().toOXPath());
          // no cached nodes, we go on normally
          boolean cachingWasOk = false;
          if (node.getContextualAttributes() != null) {// if the current AST as been decorated via
            // AttributeCollectorVisitor, we can perform a bulk xpath for all
            // records and respective attributes
            final String locationPath = node.getLocationPath().toOXPath();
            final Set<String> contextualAttributes = node.getContextualAttributes();
            final DOMDocument currentDocument = domContextNode.getOwnerDocument();
            refreshCurrentDocumentReference(currentDocument);
            cacheRecordAndAttributes(currentDocument, contextNode, locationPath, contextualAttributes);
            newContext = contextNode
                .contextualize(lookupRecordCache(domContextNode, node.getLocationPath().toOXPath()));
            if (newContext != null) {// it's possible that the bulk xpath generated an error, if so we go on normally
              cachingWasOk = true;
            }
          }

          if (!cachingWasOk) {
            if (node.isSelfNode()) {
              newContext = new OXPathType(contextNode);
            } else {
              final String plainXPath = node.getLocationPath().toOXPath();
              newContext = contextNode.getByXPath(plainXPath, true, true);
            }
          }
        }

      } else { // only this single step is evaluated in

        newContext = contextNode.getByOXPath(node.getStep());
      }

    } catch (final WebAPIStaleElementRuntimeException e) {
      LOGGER.error("Stale Element retrieving node by XPath. Return empty set. Error<{}>", e);
      return OXPathType.EMPTYRESULT;
    }

    // immediately return if no results or no further path
    if (newContext.nodeList().isEmpty() || !node.hasList())
      return newContext;
    final IOXPathNodeList result = OXPathNodeListFactory.newMutableOnLinkedSet();
    // we apply PAAT eval_ as normal on the ramaining T
    if (node.getSetBasedEval().equals(PositionFuncEnum.NEITHER)) {

      final java.util.Iterator<OXPathContextNode> iterator = newContext.nodeList().iterator();

      final ArrayList<NodeReference> references = paatSet.domlookup.getNodeReferences(newContext.nodeList());
      final int contextSetSize = newContext.nodeList().size();
      openNewPageChangeWatcher();

      for (int i = 0; i < contextSetSize; i++) {

        OXPathContextNode c = iterator.next();
        if (hasPageChangedDuringEvaluation()) {
        	if (LOGGER.isInfoEnabled()) LOGGER.info("There has been a page change, try to retrieve again the context node");

          final DOMDocument currentDocument = paatSet.domlookup.getWebBrowser().getContentDOMWindow().getDocument();
          c = references.get(i).getRenderedNodeOrNull(currentDocument);
          if (paatSet.continueOnMissingNode) {
            if (c == null) {
            	if (LOGGER.isInfoEnabled()) LOGGER.info("Cannot retrieve node {}, skip it and continue evaluation with the successive nodes",
                  references.get(i));
              continue;
            }
          }
          // update the reference
          refreshCurrentDocumentReference(currentDocument);
        }

        final boolean hasNext = i < (contextSetSize - 1);
        final boolean newProtect = hasNext ? true : data.isDocumentProtected();
        final PAATStateEvalIterative newState = new PAATState.Builder(data).setContextNode(c)
            .setDocumentProtect(newProtect).buildNode();
        final IOXPathNodeList nodeList = paatSet.eval_(c.getNode(), node.jjtGetChild(0), newState).nodeList();
        if (!nodeList.isEmpty()) {
          result.addAll(nodeList);
        }
      }
      closePageChangeWatcher();
    }
    // otherwise, we take a set based approach
    else {
      // //JavaScript returns unsorted lists - We move the sorting here (this is the only time we need to do this
      // because of position() and last())
      // if (node.getStep().getAxis().getType().equals(AxisType.BACKWARD)) newContext.nodeList().sortReverseOrder();
      final PAATStateEvalSet newState = new PAATState.Builder(data).setContextSet(newContext.nodeList()).buildSet();
      result.addAll(paatSet.accept(node.jjtGetChild(0), newState).nodeList());
    }
    return new OXPathType(result);
  }

 /**
  * 
  * Update eval and eval_ with the reference to the current DOM Document.
  * 
 * @param currentDocument
 */
private void refreshCurrentDocumentReference(final DOMDocument currentDocument) {

    currentDocumentReference = currentDocument;
    paatSet.currentDocumentReference = currentDocument;
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTOXPathKleeneStarPath node, final PAATStateEvalIterative data)
      throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTOXPathActionPath node, final PAATStateEvalIterative data) throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTOXPathNodeTestOp node, final PAATStateEvalIterative data) throws OXPathException {
    final OXPathType result = node.getSelectorPredicate().evaluateIterative(data.getContextNode());
    if (result.nodeList().isEmpty())
      return OXPathType.EMPTYRESULT;
    if (node.hasList())
      return paatSet.eval_(data.getContextNode().getNode(), node.jjtGetChild(0), data);
    else
      return result;
    /*
     * we don 't have to worry about set - based eval here , as this is only called when not a set and a later predicate
     * in same step is not set - based eval
     */
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTXPathLiteral node, final PAATStateEvalIterative data) throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTXPathPredicate node, final PAATStateEvalIterative data) throws OXPathException {

    openNewPageChangeWatcher();

    final OXPathContextNode context = data.getContextNode();

    final NodeReference referenceToContext = paatSet.domlookup.getNodeReference(context);

    final PAATStateEvalSet predState = new PAATState.Builder(data).setPosition(0).setLast(0)
        .setDocumentProtect((node.hasList()) ? true : data.isDocumentProtected())
        .setContextSet(new OXPathContextNode(context.getNode(), context.getLast(), context.getLast()))
        .setDocumentProtect((node.hasList()) ? true : data.isDocumentProtected()).buildSet();
    final OXPathType predResult = paatSet.accept(node.jjtGetChild(0), predState);
    if (!predResult.booleanValue() && !node.isOptional())
      return OXPathType.EMPTYRESULT;

    if (node.hasList()) {

      OXPathContextNode contextToCarryOn = context;

      if (hasPageChangedDuringEvaluation()) {
    	  if (LOGGER.isInfoEnabled()) LOGGER.info("There has been a page change, try to retrieve again the context node");
        contextToCarryOn = referenceToContext.getRenderedNodeOrThrow(paatSet.domlookup.getWebBrowser()
            .getContentDOMWindow().getDocument());

      }

      final PAATStateEvalIterative newState = new PAATState.Builder(data).setContextNode(contextToCarryOn)
          .setDocumentProtect(data.isDocumentProtected()).buildNode();
      final OXPathType eval_ = paatSet.eval_(contextToCarryOn.getNode(), node.jjtGetChild(1), newState);
      closePageChangeWatcher();
      return eval_;
    } else {
      closePageChangeWatcher();
    }
    return new OXPathType(context);
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTOXPathExtractionMarker node, final PAATStateEvalIterative data)
      throws OXPathException {/*
       * we don 't have to worry about set - based eval here , as this is only called when not a
       * set and a later predicate in same step is not set - based eval
       */
    final OXPathContextNode context = data.getContextNode();
    final OXPathExtractionMarker marker = node.getExtractionMarker();
    if (data.isActionFreeNavigation())
      if (node.hasList())
        return paatSet.eval_(context.getNode(), node.jjtGetChild((marker.isAttribute()) ? 1 : 0), data);
      else
        return new OXPathType(context);
    int numChild = 0;
    int newLastSibling;
    if (marker.isAttribute()) {
      final PAATStateEvalSet newState = new PAATState.Builder(data).setContextSet(context)
          .setDocumentProtect((node.hasList()) ? true : data.isDocumentProtected()).buildSet();
      newLastSibling = extractor.extractNode(context.getNode(), marker.getLabel(), context.getParent(),
          paatSet.accept(node.jjtGetChild(numChild++), newState).toPrettyHtml());
    } else {
      newLastSibling = extractor.extractNode(context.getNode(), marker.getLabel(), context.getParent());
    }
    // new last has to be accounted for
    final OXPathContextNode newContext = new OXPathContextNode(context.getNode(), context.getParent(), newLastSibling);
    if (node.hasList()) {
      final PAATStateEvalIterative newState = new PAATState.Builder(data).setContextNode(newContext).buildNode();
      return paatSet.eval_(newContext.getNode(), node.jjtGetChild(numChild++), newState);
    } else
      return new OXPathType(newContext);
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTBinaryOpExpr node, final PAATStateEvalIterative data) throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTXPathUnaryExpr node, final PAATStateEvalIterative data) throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTXPathPrimaryExpr node, final PAATStateEvalIterative data) throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTXPathNumber node, final PAATStateEvalIterative data) throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTXPathFunctionCall node, final PAATStateEvalIterative data)
      throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /**
   * Computes the values for the AST at this given state as dictated by eval_ in PAAT
   *
   * @param node
   *          the AST node upon which evaluation occurs
   * @param data
   *          state information at this point in the evaluation
   * @return the result of evaluation at this node
   * @throws OXPathException
   *           in case of OXPath processing error
   */
  @Override
  public OXPathType visitNode(final ASTXPathPathExpr node, final PAATStateEvalIterative data) throws OXPathException {
    throw new OXPathException("Unexpected call made to eval_ over node type " + node.getClass());
  }

  /*
   * (non-Javadoc)
   *
   * @see uk.ac.ox.cs.diadem.oxpath.utils.OXPathCache#clear(diadem.common.web.dom.DOMDocument)
   */
  /**
   * Clears all memoized data for the input {@code DOMDocument}. Will be overridden as long as a proxy object is used
   * (should always be used for OXPath classes overridding this method).
   *
   * @param page
   *          {@code DOMDocument} we are removing all memoized results for, presumably because the page is being closed
   *          in PAAT
   */
  @Override
  public Boolean clear(final DOMDocument page) {

    proxied("clear");
    return true;
  }

  private void proxied(final String method) {
    // This methods should never be called outside the proxy
    LOGGER.error("the method {} on '{}' is invoked outside a proxy. It has not effect ", method, this.getClass()
        .toString());
  }

  /**
   * DOM-related caches are removed. This can be related to the navigation to the new page or any action which potentially changes the DOM tree.
 * @param page
 */
public void clearCachedRecords(final DOMDocument page) {
    final Collection<DOMNode> recordToRemove = documentToRecords.removeAll(page);
    if (LOGGER.isDebugEnabled()) LOGGER.debug("clearing {} cached records and respective attributes for current page", recordToRemove.size());
    for (final DOMNode record : recordToRemove) {
      recordsToAttributes.row(record).clear();
    }
  }

  // public void cacheRecordAndAttributes(final DOMDocument ownerDocument, final OXPathContextNode contextNode,
  // final ASTSimpleOXPathStepPath node) {
  // if (!documentToRecords.isEmpty() && !documentToRecords.containsKey(ownerDocument)) {
  // // it's a good moment to clean the cache from a previous document, it will be stale anyway
  // clearCachedRecords(ownerDocument);
  // }
  //
  // final String locationPath = node.getLocationPath().toOXPath();
  // Set<String> contextualAttributes = node.getContextualAttributes();
  // final Table<DOMNode, String, List<DOMNode>> recordAndAttributes = contextNode.getByXPathBulk(locationPath,
  // contextualAttributes);
  // LOGGER.info("caching a total of {} records attributes via bulk xpath from node {}", recordAndAttributes.size(),
  // locationPath);
  // recordsToAttributes.putAll(recordAndAttributes);
  // for (final DOMNode record : recordAndAttributes.rowKeySet()) {
  // documentToRecords.put(ownerDocument, record);
  // }
  //
  // }

  public void cacheRecordAndAttributes(final DOMDocument ownerDocument, final OXPathContextNode contextNode,
      final String locationPath, final Set<String> contextualAttributes) {
    if (!documentToRecords.isEmpty() && !documentToRecords.containsKey(ownerDocument)) {
      // it's a good moment to clean the cache from a previous document, it will be stale anyway
      clearCachedRecords(ownerDocument);
    }
    final Table<DOMNode, String, List<DOMNode>> recordAndAttributes = contextNode.getByXPathBulk(locationPath,
        contextualAttributes);
    if (LOGGER.isInfoEnabled()) LOGGER.info("caching a total of {} records attributes via bulk xpath from node {}", recordAndAttributes.size(),
        locationPath);
    recordsToAttributes.putAll(recordAndAttributes);
    for (final DOMNode record : recordAndAttributes.rowKeySet()) {
      documentToRecords.put(ownerDocument, record);
    }

  }

  public final List<DOMNode> lookupRecordCache(final DOMNode domContextNode, final String oxPath) {
    return recordsToAttributes.row(domContextNode).get(oxPath);

  }

  private boolean isCached(final DOMNode domContextNode, final String oxPath) {
    final Map<String, List<DOMNode>> row = recordsToAttributes.row(domContextNode);
    return row.containsKey(oxPath);
  }

  /**
   * Need to call this method to check children so that memoization is applied
   *
   * @param context
   *          the context node
   * @param astNode
   *          the node in the AST to evaluate over
   * @param state
   *          the state of the evaluation
   * @return evaluation result
   * @throws OXPathException
   *           in case of exception in evaluation
   */
  @Override
  public OXPathType eval_(final DOMNode context, final Node astNode, final PAATStateEvalIterative state)
      throws OXPathException {

    return accept(astNode, state);
  }

  boolean openNewPageChangeWatcher() {
    // if (predicateStack.isEmpty()) {
    predicateStack.addFirst(new MutableBoolean());
    return true;
    // }
    // return false;
  }

  boolean closePageChangeWatcher() {
    return predicateStack.removeFirst().toBoolean();
  }

  // Remove all the browser-related caches for the current web page. 
@Override
  public void signalNewPageDueToAction(final DOMDocument document) {
    for (final MutableBoolean mutableBoolean : predicateStack) {
      mutableBoolean.setValue(true);
    }
    clearCachedRecords(document);
    refreshCurrentDocumentReference(document);
    paatSet.evictCachesForPage(document); // clear memoization-related caches 
  }

  boolean hasPageChangedDuringEvaluation() {
    return predicateStack.getFirst().booleanValue();
  }

  DOMDocument getCurrentDocumentReference() {

    return currentDocumentReference;
  }

}
