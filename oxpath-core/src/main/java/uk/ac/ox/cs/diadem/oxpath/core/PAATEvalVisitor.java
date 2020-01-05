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

import java.io.ObjectOutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import org.openqa.selenium.UnhandledAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import uk.ac.ox.cs.diadem.oxpath.core.OXPath.EngineOptions;
import uk.ac.ox.cs.diadem.oxpath.core.domlookup.DOMLookup;
import uk.ac.ox.cs.diadem.oxpath.core.domlookup.DOMLookupByRobustXPath;
import uk.ac.ox.cs.diadem.oxpath.core.domlookup.DOMLookupByUniqueXPath;
import uk.ac.ox.cs.diadem.oxpath.core.domlookup.NodeReference;
import uk.ac.ox.cs.diadem.oxpath.core.extraction.Extractor;
import uk.ac.ox.cs.diadem.oxpath.core.extraction.NewSimpleExtractor;
import uk.ac.ox.cs.diadem.oxpath.core.state.PAATState;
import uk.ac.ox.cs.diadem.oxpath.core.state.PAATStateEvalIterative;
import uk.ac.ox.cs.diadem.oxpath.core.state.PAATStateEvalSet;
import uk.ac.ox.cs.diadem.oxpath.model.IOXPathNodeList;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathNodeListFactory;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathType;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes;
import uk.ac.ox.cs.diadem.oxpath.model.language.Action;
import uk.ac.ox.cs.diadem.oxpath.model.language.ActionType;
import uk.ac.ox.cs.diadem.oxpath.model.language.OXPathExtractionMarker;
import uk.ac.ox.cs.diadem.oxpath.output.IAbstractOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;
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
import uk.ac.ox.cs.diadem.oxpath.utils.ActionStackElement;
import uk.ac.ox.cs.diadem.oxpath.utils.OXPathMemoizer;
import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;
import uk.ac.ox.cs.diadem.oxpath.utils.RelativePathCollector;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder.Engine;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode.Type;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationDetectionUtils;
import uk.ac.ox.cs.diadem.webapi.pagestate.SimplePageStateRecorder;

/**
 *
 * Class for evaluating OXPath queries over Abstract Syntax Trees. Used by the OXPathNavigator objects for query
 * evaluation. Encodes the eval function of the PAAT Visitor (and calls eval_)
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
class PAATEvalVisitor extends OXPathVisitorGenericAdaptor<PAATStateEvalSet, OXPathType> {

  private final class DocumentProviderImplementation implements OXPathMemoizer.DocumentProvider {
    @Override
    public DOMDocument provideDocument() {
      return currentDocumentReference;
    }
  }

  Set<String> visitedNextLink = Sets.newHashSet();

  boolean continueOnMissingNode;
  
  private boolean autocompleteReaction;

  private ActionEngine actionEngine;

  /**
   * {@code PAATEval_Visitor} for calls of eval_ as dictated by PAAT
   */
  private PAATEval_ eval_visitor;

  /**
   * initial WebClient object for TreeWalker instance, before a page is fetched
   */
  private WebBrowser webclient;

  /**
   * stores the number of the current last node number assigned
   */
  int nodeNum = 0;
  /**
   * handles the extraction
   */
  Extractor extractor;

  /**
   * Holds state for controlling page memory management; synchronizes
   */
  private final Map<WebBrowser, Stack<ActionStackElement>> backController = new HashMap<WebBrowser, Stack<ActionStackElement>>();

  /**
   * (Currently) global object facilitating DOM node refreshes (after back instantiations)
   */
  // private final DOMLookup domlookup = new DOMLookupDocumentPosition();
  DOMLookup domlookup;// = new DOMLookupByRobustXPath();

  /**
   * Holds currently "open" actions, so we know if the freeMem() call at the end of an action sequence is necessary
   */
  private final Set<Integer> openActions = new HashSet<Integer>();

  /**
   * Counter for actions (serves as a unique id for open actions in the {@code openActions} set
   */
  private int currentAction = 0;

  DOMDocument currentDocumentReference;

  private static Logger logger = LoggerFactory.getLogger(PAATEvalVisitor.class);

//  /**
//   * Call this method to instantiate a new {@code PAATEvalVisitor} instance to evaluate OXPath expressions. Implements
//   * the PAAT algorithm detailed in the OXPath VLDB '11 paper. While possible to evaluate multiple OXPath expressions
//   * with the same object, <b>HIGHLY RECOMMEND</b> using a new TreeWalker object for each expression evaluated through
//   * calls to this method, even if the browser, logger, and output stream are all reused.
//   *
//   * @param browser
//   *          browser object to evaluate on OXPath
//   * @param os
//   *          the output stream to pipe away any {@code OXPathExtractionNode} instances
//   * @return a new {@code PAATEvalVisitor} instance for evaluating an OXPath expression
//   */
  // public static PAATEvalVisitor newInstance(final WebBrowser browser, final ObjectOutputStream os) {
  // return PAATEvalVisitor.newInstance(browser, os, false);
  // }

//  /**
//   * See {@link #newInstance(WebBrowser, ObjectOutputStream)} The additional option <code>continueOnMissingNode</code>
//   * if enable allows the evaluation to continue even when some context node from a node set under evalaution is not
//   * retrieved on the page due to a page change (e.g., coming back from a page after an action). An example of this can
//   * be the expression //a/{click} where each click changes the page. It is possible that some of the next 'a' nodes is
//   * not found any more on the original page, therefore, instead of failing the evaluation simply skips this node and
//   * continues with the next.
//   *
//   * @param browser
//   * @param os
//   * @param continueOnMissingNode
//   * @return
//   */
//  public static PAATEvalVisitor newInstance(final WebBrowser browser, final ObjectOutputStream os,
//      final EngineOption options) {
//    return new PAATEvalVisitor(browser, os, options);
//  }

  /**
   * See {@link #newInstance(WebBrowser, ObjectOutputStream)} The additional option <code>continueOnMissingNode</code>
   * if enable allows the evaluation to continue even when some context node from a node set under evalaution is not
   * retrieved on the page due to a page change (e.g., coming back from a page after an action). An example of this can
   * be the expression //a/{click} where each click changes the page. It is possible that some of the next 'a' nodes is
   * not found any more on the original page, therefore, instead of failing the evaluation simply skips this node and
   * continues with the next.
   * 
 * @param browser
 * @param oh
 * @param options
 * @return
 */
public static PAATEvalVisitor newInstance(final WebBrowser browser, final IAbstractOutputHandler oh,
      final EngineOptions options) {
    return new PAATEvalVisitor(browser, oh, options);
  }

  /**
   * Constructor for initiating new PAATEvalVisitor object; must pass the PAAT Visitor a {@code WebBrowser} object to
   * evaluate the expression over, a {@code Logger} environment to pass logging information, and
   * {@code ObjectOutputStream} to pipe away any {@code OXPathExtractionNode} instances.
   * 
 * @param browser
 * @param oh
 * @param options
 */
private PAATEvalVisitor(final WebBrowser browser, final IAbstractOutputHandler oh, final EngineOptions options) {
    final Extractor simpleExtractor = new NewSimpleExtractor(oh);
    instantiateWith(simpleExtractor, browser, options);
  }

  // private final boolean aggregate;

//  /**
//   * Constructor for initiating new PAATEvalVisitor object; must pass the PAAT Visitor a {@code WebBrowser} object to
//   * evaluate the expression over, a {@code Logger} environment to pass logging information, and
//   * {@code ObjectOutputStream} to pipe away any {@code OXPathExtractionNode} instances.
//   *
//   * @param browser
//   *          browser object to evaluate on OXPath
//   * @param os
//   *          the output stream to pipe away any {@code OXPathExtractionNode} instances
//   */
//  private PAATEvalVisitor(final WebBrowser browser, final ObjectOutputStream os, final EngineOption options) {
//
//    final SimpleExtractor simpleExtractor = new SimpleExtractor(os);
//    instantiateWith(simpleExtractor, browser, options);
//
//  }

  private void instantiateWith(final Extractor simpleExtractor, final WebBrowser browser, final EngineOptions options) {
    webclient = browser;
    continueOnMissingNode = options.isDoContinueOnMissingContextNode();
    autocompleteReaction = options.isAutocompleteReaction();
    actionEngine = new ActionEngine(options.getWaitAfterActionExecutionMs());
    final boolean memoizationActive = true;
    if (memoizationActive) {
      // final DocumentProviderImplementation documentProvider = new DocumentProviderImplementation(paatEval_Visitor);
      extractor = OXPathMemoizer.memoize(simpleExtractor, new DocumentProviderImplementation());

      final PAATEval_Visitor paatEval_Visitor = new PAATEval_Visitor(this, extractor);
      eval_visitor = OXPathMemoizer.memoize(paatEval_Visitor, new DocumentProviderImplementation());
    } else {
      extractor = simpleExtractor;
      eval_visitor = new PAATEval_Visitor(this, extractor);
    }
    final boolean useRobustReferenceNode = false;
    if (useRobustReferenceNode) {
      // VERY HEAVY, will make WEBDRIVER version NOT usable
    	// This was used for HTMLUnit
      domlookup = new DOMLookupByRobustXPath(webclient, !continueOnMissingNode);// new DOMLookupDocumentPosition();
    } else {
      domlookup = new DOMLookupByUniqueXPath(webclient, !continueOnMissingNode);
    }

  }

  /**
   * Evaluates <tt>SimpleNode</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final CustomSimpleNode node, final PAATStateEvalSet data) throws OXPathException {
    // this should never happen
    throw new OXPathException("Evaluated SimpleNode visit in PAATEvalVisitor - unexpected AST node" + node.getClass()
        + " parsed!");
  }

  @Override
  public OXPathType visitNode(final SimpleNode node, final PAATStateEvalSet data) throws OXPathException {
    throw new OXPathException("Evaluated SimpleNode visit in PAATEvalVisitor - unexpected AST node" + node.getClass()
        + " parsed!");
  }

  /**
   * Evaluates <tt>ASTScript</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTExpression node, final PAATStateEvalSet data) throws OXPathException {

    // if (aggregate && false) {
    // if (logger.isDebugEnabled()) logger.debug("Collapsing steps in the expression");
    // new StepCollapseVisitor().accept(node, null);
    // }
    // decorate the tree with positional function information
    new PrePAATVisitor().accept(node, null);
    if ((webclient.getEngine() == Engine.WEBDRIVER_FF)) {
      // new AttributeCollectorVisitor().accept(node, new MyData());
      new RelativePathCollector().accept(node, null);
    }
    final OXPathType result = accept(node.jjtGetChild(0), data);
    endWalk();
    return result;
  }

  /**
   * Evaluates <tt>ASTRelativeOXPathLocationPath</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTRelativeOXPathLocationPath node, final PAATStateEvalSet data)
      throws OXPathException {
    int numChild = 0;
    if (data.getContextSet().isEmpty())
      return OXPathType.EMPTYRESULT;
    // first handle, if this is an absolute path
    IOXPathNodeList context;
    if (node.isAbsolutePath()) {
      context = OXPathNodeListFactory.newMutableOnLinkedSet();
      final OXPathContextNode firstDomNode = data.getContextSet().first();
      DOMElement documentElement = firstDomNode.getNode().getOwnerDocument().getDocumentElement();
      if (documentElement.isStale()) {
        // here is possible that the context set refers to nodes of another page already closed (kleene star case).
        // for this reason we retrieve the current document node.
        documentElement = firstDomNode.getNode().getBrowser().getContentDOMWindow().getDocument(true)
            .getDocumentElement();
        if (documentElement.isStale())
          throw new OXPathException("Context node is stale, attempt to find the current documentElement failed");
      }
      context.add(new OXPathContextNode(documentElement, firstDomNode.getParent(), firstDomNode.getLast()));
    } else {
      context = data.getContextSet();
    }
    // next, handle any simple expression via eval_
    IOXPathNodeList simpleResult;
    if (node.hasSimplePath()) {
      simpleResult = OXPathNodeListFactory.newMutableOnLinkedSet();
      final int astSimple = numChild++;
      final Iterator<OXPathContextNode> iterator = context.iterator();
      while (iterator.hasNext()) {
        final OXPathContextNode c = iterator.next();
        final boolean newProtect = iterator.hasNext() ? true : data.isDocumentProtected();
        final PAATStateEvalIterative newState = new PAATState.Builder(data).setContextNode(c)
            .setDocumentProtect(newProtect).buildNode();
        simpleResult.addAll(eval_visitor.eval_(c.getNode(), node.jjtGetChild(astSimple), newState).nodeList());
      }
    } else {
      simpleResult = context;
    }
    // finally, handle any complex expression
    OXPathType complexResult;
    if (node.hasComplexPath()) {
      complexResult = accept(node.jjtGetChild(numChild++), new PAATState.Builder(data).setContextSet(simpleResult)
          .buildSet());
      // FIXME here the complexResult may be STALE due to change page in a predicate
    } else {
      complexResult = new OXPathType(simpleResult);
    }
    return complexResult;
  }

  /**
   * Evaluates <tt>ASTSimpleOXPathStepPathAspect</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTSimpleOXPathStepPath node, final PAATStateEvalSet data) throws OXPathException {
    // this should never happen
    throw new OXPathException("Evaluated simple oxpath evaluation with set-based eval function in PAAT");
  }

  /**
   * Evaluates <tt>ASTOXPathKleeneStarPath</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTOXPathKleeneStarPath node, final PAATStateEvalSet data) throws OXPathException {
    IOXPathNodeList context = data.getContextSet();

    final IOXPathNodeList result = OXPathNodeListFactory.newMutableOnLinkedSet();
    if (context.isEmpty())
      return OXPathType.EMPTYRESULT;
    if (OXPathParser.hasActionOnMainPath(node.jjtGetChild(0))) {

      // necassary as the page will change due to actions on the main path
      final ArrayList<NodeReference> references = domlookup.getNodeReferences(context);

      final int lower = node.getLowerBound();
      final int higher = node.getUpperBound();
      // the only time we evaluate following, otherwise the inner actions do
      if ((lower == 0) && node.hasFollowingPath()) {
        result.addAll(accept(node.jjtGetChild(1),
            new PAATState.Builder(data).setContextSet(context).setDocumentProtect(true).buildSet()).nodeList());
        // the inner actions evaluate the rest
      }

      // here the page
      // refresh the context on this page
      context = domlookup.getRenderedNodes(references, domlookup.getWebBrowser().getContentDOMWindow()
          .getDocument(true));
      result.addAll(accept(node.jjtGetChild(0),
          new PAATState.Builder(data).setContextSet(context).setNumKleeneStarIterations(higher).buildSet()).nodeList());
      return new OXPathType(result);
    } else {
      if (node.getLowerBound() < 1) {
        result.addAll(context);
      }
      for (int i = 0; (i < node.getUpperBound()) && !context.isEmpty(); i++) {
        final PAATStateEvalSet state = new PAATState.Builder(data)
            .setContextSet(context)
            .setDocumentProtect(
                node.hasFollowingPath() || (i < (node.getUpperBound() - 1)) ? true : data.isDocumentProtected())
            .buildSet();
        context = accept(node.jjtGetChild(0), state).nodeList();
        if (i >= node.getLowerBound()) {
          context.removeAll(result);
          result.addAll(context);
        }
      }
      if (!node.hasFollowingPath())
        return new OXPathType(result);
      else
        return accept(node.jjtGetChild(1), new PAATState.Builder(data).setContextSet(result).buildSet());
    }
  }

  /**
   * Evaluates <tt>ASTOXPathActionPath</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTOXPathActionPath node, final PAATStateEvalSet data) throws OXPathException {
    if (data.isActionFreeNavigation()) {
      if (data.getActionFreePrefixEnd().equals(node) || !node.hasTail())
        return new OXPathType(data.getContextSet());
      else
        return accept(node.jjtGetChild(0), data);
    } else {
      final IOXPathNodeList context = data.getContextSet();
      final IOXPathNodeList result = OXPathNodeListFactory.newMutableOnLinkedSet();
      if (context.isEmpty())
        return OXPathType.EMPTYRESULT;
      final WebBrowser actionSetBrowser = node.getAction().getActionType().equals(ActionType.URL) ? null : context
          .first().getNode().getBrowser();

      if (logger.isInfoEnabled()) logger.info("storing references for context node {}", context);

      final ArrayList<NodeReference> references = domlookup.getNodeReferences(context);

      for (int i = 0; i < context.size(); i++) {

        if (logger.isInfoEnabled()) logger.info("Starting evaluation of action <{}> on node <{}> out of context set of size <{}>", new Object[] {
            node.getAction(), i + 1, context.size() });

        OXPathContextNode c = null;
        final String referenceToString = references.get(i).toString();
        if (node.getAction().getActionType().equals(ActionType.URL)) {
          c = OXPathContextNode.getNotionalContext();
        } else {
          if (i == 0) {
            c = context.first();
          } else {
            c = retrieveBackNodeOnCurrentDocument(actionSetBrowser.getContentDOMWindow().getDocument(),
                references.get(i));
            if (c == null) {
              if (logger.isInfoEnabled()) logger.info("Cannot retrieve <{}> on current page", referenceToString);
              if (continueOnMissingNode) {
                if (logger.isInfoEnabled()) logger.info("Skip <{}> and continue the evaluation on the remaining nodes", referenceToString);
                continue;
              } else {
                logger.error("Failed retrieving <{}> on current page, aborting on missing nodes", referenceToString);
                throw new OXPathException(MessageFormat.format("Failed retrieving <{0}> node on current page",
                    referenceToString));
              }
            }
          }
        }

        final boolean newProtect = i < (context.size() - 1) ? true : data.isDocumentProtected();
        if (logger.isDebugEnabled()) logger.debug("Taking action on contect node {} in position", context, i);
        if (logger.isDebugEnabled()) logger.debug("document is protected : {}", newProtect);
        // treating the exit in case of nextclick action
        // if (c.getNode().isStale()) {
        //
        // }
        OXPathContextNode newNode = takeAction(c, node.getAction(), newProtect, data.getCurrentAction());

        if (newNode == null)
          return OXPathType.EMPTYRESULT;
        else {
          // new page, we also force deletion of local caches
          eval_visitor.signalNewPageDueToAction(newNode.getNode().getOwnerDocument());

          //
        }

        final int newCurrentAction = currentAction;

        if (!node.getAction().isAbsoluteAction()) {// calculate AFP
          if (logger.isInfoEnabled()) logger.info("Computing Action Free Prefix after contextual action {}", data.getCurrentAction());

          // GIOG: replace original AFP as buggy on expressions like //a/{mouseover}/./{mouseover}
          // what happens computing the AFP for the second action is that we recompute //a but we don't track the
          // position in this set of the current node under evaluation, therefore
          // it will always pick the first node in the node set.
          // I replace it with simply evaluating the locator for the current node on the current dom, that is what we do
          // when the page changes and we need to find again the context node/

          // newNode = performAFP(data, c, newNode, node, newCurrentAction, i);

          final OXPathContextNode sameNode = retrieveBackNodeOnCurrentDocument(actionSetBrowser.getContentDOMWindow()
              .getDocument(), references.get(i));
          if (sameNode == null) {
            logger.error("Failed retrieving AFP {}", references.get(i));
            if (continueOnMissingNode) {
              if (logger.isInfoEnabled()) logger.info("Skip <{}> node and continue the evaluation on the remaining nodes", referenceToString);
              continue;
            } else {
              logger.error("Failed retrieving {} on current page, aborting on missing nodes", referenceToString);
              throw new OXPathException(MessageFormat.format("Failed retrieving <{0}> node on current page",
                  referenceToString));
            }
          } else {
            // make sure we preserve the context parent and last
            newNode = new OXPathContextNode(sameNode.getNode(), c.getParent(), c.getLast());
          }
          if (logger.isInfoEnabled()) logger.info("AFP has found the node via node reference");
        }

        PAATStateEvalSet actionState;
        if (node.getAction().isAbsoluteAction()) {
          actionState = new PAATState.Builder(data).setContextSet(newNode).setDocumentProtect(newProtect)
              .setActionFreePrefix(node).setCurrentAction(newCurrentAction).buildSet();
        } else {
          actionState = new PAATState.Builder(data).setContextSet(newNode).setDocumentProtect(newProtect)
              .setCurrentAction(newCurrentAction).buildSet();
        }
        IOXPathNodeList predResult;
        final boolean evalAsKleene = node.isInsideKleeneStar() && (data.getNumKleeneStarIterations() > 0);
        if (node.hasTail()) {
          predResult = accept(node.jjtGetChild(0), actionState).nodeList();
          if (!evalAsKleene) {
            result.addAll(predResult);
          }
        } else {
          predResult = OXPathNodeListFactory.newImmutableOnLinkedSet(newNode);
          if (!evalAsKleene) {
            result.add(newNode);
          }
        }
        if (evalAsKleene && !predResult.isEmpty()) {
          // we do the rest from the Kleene-star; this is another area where we break compositionality of the language;
          // we additionally protect the page as it is part of the Kleene's recurring context
          final ASTOXPathKleeneStarPath containingKleene = (ASTOXPathKleeneStarPath) node.insideKleeneStar();
          // we only do the following if we've done lower the specified number of times (since we already checked for
          // the 0 unwinding in the Kleene node, we've done 1 unwinding at this recursion level
          final boolean doneLower = (containingKleene.getLowerBound() - (containingKleene.getUpperBound() - data
              .getNumKleeneStarIterations())) <= 1;
          if (containingKleene.hasFollowingPath() && doneLower) {
            result.addAll(accept(
                node.insideKleeneStar().jjtGetChild(1),
                new PAATState.Builder(actionState).setDocumentProtect(true).setContextSet(predResult)
                    .setNumKleeneStarIterations(0).setCurrentAction(newCurrentAction).buildSet()).nodeList());
          } else {
            result.addAll(predResult);
          }
          final int newNumKleeneStarIterations = data.getNumKleeneStarIterations() - 1;
          if (newNumKleeneStarIterations > 0) {
            accept(containingKleene.jjtGetChild(0),
                new PAATState.Builder(actionState).setDocumentProtect(false).setContextSet(predResult)
                    .setNumKleeneStarIterations(newNumKleeneStarIterations).setCurrentAction(newCurrentAction)
                    .buildSet());
          }
        }
        if (openActions.contains(newCurrentAction)) {
          this.freeMem(newNode, newCurrentAction);
          // if (newNode.getNode().isStale()) {
          // logger.error("After cleaning the memory and going back in the browser, the context node is stale");
          // }
        }
      }

      // FIXME HERE result can contains stale elements!!!
      return new OXPathType(result);
    }
  }

  private OXPathContextNode retrieveBackNodeOnCurrentDocument(final DOMDocument document,
      final NodeReference nodeReference) {

    return nodeReference.getRenderedNodeOrNull(document);

  }

  private OXPathContextNode performAFP(final PAATStateEvalSet data, final OXPathContextNode c,
      final OXPathContextNode newNode, final ASTOXPathActionPath node, final int newCurrentAction, final int i)
      throws OXPathException {
    final PAATStateEvalSet afpState = new PAATState.Builder(data).setContextSet(newNode)
    // FIXME
        .setIsActionFreeNavigation(true).setCurrentAction(newCurrentAction).setActionFreePrefixEnd(node).buildSet();
    final IOXPathNodeList afpSet = accept((Node) data.getActionFreePrefix(), afpState).nodeList();
    // multi-way set based evaluation doesn't happen often and aren't big sets, but they are expensive
    if (afpSet.isEmpty()) {
      if (logger.isInfoEnabled()) logger.info("AFP NOT found");
      return null;// we continue if there is no element after this AFP
    }
    final Iterator<OXPathContextNode> iterator = afpSet.iterator();
    OXPathContextNode afpNode = iterator.next();
    try {
      for (int j = 0; j < i; j++) {
        afpNode = iterator.next();
      }
    } catch (final NoSuchElementException e) {
      if (logger.isInfoEnabled()) logger.info("AFP NOT found");
      return null;// we continue if there is no element after this AFP
    }
    // because we don't do the extraction markers, these won't come back correct if there are extraction markers
    // in the AFP
    if (logger.isInfoEnabled()) logger.info("AFP found");
    return new OXPathContextNode(afpNode.getNode(), c.getParent(), c.getLast());
  }

  /**
   * Evaluates <tt>ASTOXPathNodeTestOp</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTOXPathNodeTestOp node, final PAATStateEvalSet data) throws OXPathException {
    // because this node can't contain a position function, we assume a later node contains the function and make the
    // set call of the next node
    if (!node.hasList() && !node.getSetBasedEval().equals(PositionFuncEnum.NEITHER))
      throw new OXPathException("Unexpected call in PAAT to set-based OXPathNodeTestOp node");
    final IOXPathNodeList context = data.getContextSet();
    final OXPathType result = node.getSelectorPredicate().evaluateSet(context);
    final PAATStateEvalSet newState = new PAATState.Builder(data).setContextSet(result.nodeList()).buildSet();
    return node.hasList() ? accept(node.jjtGetChild(0), newState) : result;
  }

  /**
   * Evaluates <tt>ASTXPathLiteral</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTXPathLiteral node, final PAATStateEvalSet data) throws OXPathException {
    return new OXPathType(node.getValue());
  }

  /**
   * Evaluates <tt>ASTXPathPredicate</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTXPathPredicate node, final PAATStateEvalSet data) throws OXPathException {
    // since we are doing set-based predicate eval, each node will need position and last assignment
    final IOXPathNodeList context = data.getContextSet();
    final IOXPathNodeList result = OXPathNodeListFactory.newMutableOnLinkedSet();
    final Iterator<OXPathContextNode> iteratorContext = context.iterator();
    int positionCount = 1;

    while (iteratorContext.hasNext()) {
      final OXPathContextNode c = iteratorContext.next();

      // position is i+1 because XPath counting begins at 1, not 0
      final PAATStateEvalSet predState = new PAATState.Builder(data).setPosition(positionCount).setLast(context.size())
          .setDocumentProtect(node.hasList() || iteratorContext.hasNext() ? true : data.isDocumentProtected())
          .setContextSet(new OXPathContextNode(c.getNode(), c.getLast(), c.getLast()))
          .setDocumentProtect(node.hasList() ? true : data.isDocumentProtected()).setNumKleeneStarIterations(0)
          .buildSet();
      final OXPathType predResult = accept(node.jjtGetChild(0), predState);
      if (predResult.isType().equals(OXPathTypes.NUMBER)) {
        if (new Integer(positionCount).doubleValue() == predResult.number()) {
          result.add(c);
        }
      } else if (predResult.booleanValue() || node.isOptional()) {
        result.add(c);
      }
      positionCount++;
    }
    // what we do with the result set depends on the whether the next node exists and if it is set-based or not
    if (!node.hasList() || result.isEmpty())
      return new OXPathType(result);
    else if (!node.getSetBasedEval().equals(PositionFuncEnum.NEITHER))
      return accept(node.jjtGetChild(1), new PAATState.Builder(data).setContextSet(result).buildSet());
    else {
      final IOXPathNodeList finalResult = OXPathNodeListFactory.newMutableOnLinkedSet();
      final Iterator<OXPathContextNode> iteratorResult = result.iterator();
      while (iteratorResult.hasNext()) {
        final OXPathContextNode r = iteratorResult.next();
        // we need to account for the last in the set as this wasn't done in the step
        final PAATStateEvalIterative listState = new PAATState.Builder(data).setContextNode(r)
            .setDocumentProtect(iteratorResult.hasNext() ? true : data.isDocumentProtected()).buildNode();
        finalResult.addAll(eval_visitor.eval_(r.getNode(), node.jjtGetChild(1), listState).nodeList());
      }
      return new OXPathType(finalResult);
    }
  }

  /**
   * Evaluates <tt>ASTOXPathExtractionMarker</tt> types in the AST
   *
   * @param nodeextractor
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTOXPathExtractionMarker node, final PAATStateEvalSet data) throws OXPathException {
    final IOXPathNodeList contextSet = data.getContextSet();
    final OXPathExtractionMarker marker = node.getExtractionMarker();
    // we avoid all of this if in AFP
    if (data.isActionFreeNavigation())
      if (node.hasList())
        return accept(node.jjtGetChild(marker.isAttribute() ? 1 : 0), data);
      else
        return new OXPathType(contextSet);
    // apply the extraction marker for each node in the set
    final IOXPathNodeList newContext = OXPathNodeListFactory.newMutableOnLinkedSet();
    final Iterator<OXPathContextNode> iteratorContext = contextSet.iterator();
    int positionCount = 1;
    while (iteratorContext.hasNext()) {
      final OXPathContextNode context = iteratorContext.next();
      int numChild = 0;
      int newLastSibling;
      if (marker.isAttribute()) {
        final PAATStateEvalSet newState = new PAATState.Builder(data).setContextSet(context).setPosition(positionCount)
            .setLast(contextSet.size())
            // i+1 for position due to OXPath counting beginning at 1
            .setDocumentProtect(node.hasList() || iteratorContext.hasNext() ? true : data.isDocumentProtected())
            .buildSet();
        newLastSibling = extractor.extractNode(context.getNode(), marker.getLabel(), context.getParent(),
            accept(node.jjtGetChild(numChild++), newState).toPrettyHtml());
      } else {
        newLastSibling = extractor.extractNode(context.getNode(), marker.getLabel(), context.getParent());
      }
      // new last has to be accounted for
      newContext.add(new OXPathContextNode(context.getNode(), context.getParent(), newLastSibling));
      positionCount++;
    }
    if (node.hasList()) {// if there are following simple parts of the expression
      if (!node.getSetBasedEval().equals(PositionFuncEnum.NEITHER))
        return accept(node.jjtGetChild(marker.isAttribute() ? 1 : 0),
            new PAATState.Builder(data).setContextSet(newContext).buildSet());
      else {// the positional predicate was in the attribute and we switch back to iterative evaluation
        final IOXPathNodeList finalResult = OXPathNodeListFactory.newMutableOnLinkedSet();
        final Iterator<OXPathContextNode> iteratorResult = newContext.iterator();
        while (iteratorResult.hasNext()) {
          final OXPathContextNode newNode = iteratorResult.next();
          final PAATStateEvalIterative newState = new PAATState.Builder(data).setContextNode(newNode)
              .setDocumentProtect(iteratorResult.hasNext() ? true : data.isDocumentProtected()).buildNode();
          finalResult.addAll(eval_visitor.eval_(newNode.getNode(), node.jjtGetChild(marker.isAttribute() ? 1 : 0),
              newState).nodeList());
        }
        return new OXPathType(finalResult);
      }
    } else
      return new OXPathType(newContext);
  }

  /**
   * Evaluates <tt>ASTXPathUnaryExpr</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTXPathUnaryExpr node, final PAATStateEvalSet data) throws OXPathException {
    OXPathType result = accept(node.jjtGetChild(0), data);
    for (int i = 0; i < node.getNumberOperators(); i++) {
      result = node.getUnaryOperator().evaluate(result);
    }
    return result;
  }

  /**
   * Evaluates <tt>ASTXPathPrimaryExpr</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTXPathPrimaryExpr node, final PAATStateEvalSet data) throws OXPathException {
    return accept(node.jjtGetChild(0), data);
  }

  /**
   * Evaluates <tt>ASTXPathNumber</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTXPathNumber node, final PAATStateEvalSet data) throws OXPathException {
    return new OXPathType(node.getValue());
  }

  /**
   * Evaluates <tt>ASTXPathFunctionCall</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTXPathFunctionCall node, final PAATStateEvalSet data) throws OXPathException {
    if (!node.getFunction().checkParameterCount(node.getNumParameters()))
      throw new OXPathException(MessageFormat.format("Unexpected number of parameters: {0} for function: {1}",
          node.getNumParameters(), node.getFunction().getName()));
    final ArrayList<OXPathType> args = new ArrayList<OXPathType>();
    for (int i = 0; i < node.getNumParameters(); i++) {
      args.add(accept(node.jjtGetChild(i), data));
    }
    return node.getFunction().evaluate(args, data);
  }

  /**
   * Evaluates <tt>ASTXPathUnionExpr</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTBinaryOpExpr node, final PAATStateEvalSet data) throws OXPathException {
    return node.getBinaryOperator().evaluate(accept(node.jjtGetChild(0), data), accept(node.jjtGetChild(1), data));
  }

  /**
   * Evaluates <tt>ASTXPathPathExpr</tt> types in the AST
   *
   * @param node
   *          query node
   * @param data
   *          the PAAT-specific state information at {@code node}
   * @return the result of the evaluation at {@code node}
   */
  @Override
  public OXPathType visitNode(final ASTXPathPathExpr node, final PAATStateEvalSet data) throws OXPathException {
    // This one is basically the set-based version of RelativeOXPath expression; due to JavaCC node creation, it was
    // unclear how to replace the second part with a RelativeOXPath node
    final IOXPathNodeList context = accept(node.jjtGetChild(0), data).nodeList();
    int numChild = 1;// we've already evaluated the first child
    // next, handle any simple expression via eval_
    IOXPathNodeList simpleResult;
    if (node.hasSimpleList()) {
      simpleResult = OXPathNodeListFactory.newMutableOnLinkedSet();
      final int astSimple = numChild++;
      final Iterator<OXPathContextNode> iterator = context.iterator();
      while (iterator.hasNext()) {
        final OXPathContextNode c = iterator.next();
        final boolean newProtect = iterator.hasNext() ? true : data.isDocumentProtected();
        final PAATStateEvalIterative newState = new PAATState.Builder(data).setContextNode(c)
            .setDocumentProtect(newProtect).buildNode();
        simpleResult.addAll(eval_visitor.eval_(c.getNode(), node.jjtGetChild(astSimple), newState).nodeList());
      }
    } else {
      simpleResult = context;
    }
    // finally, handle any complex expression
    OXPathType complexResult;
    if (node.hasComplexList()) {
      complexResult = accept(node.jjtGetChild(numChild++), new PAATState.Builder(data).setContextSet(simpleResult)
          .buildSet());
    } else {
      complexResult = new OXPathType(simpleResult);
    }
    return complexResult;
  }

  /**
   * In order to trigger the proxy override via reflection (which doesn't happen with internal calls), this method is
   * used whenever eval_ needs to be called (for either PAAT visitor).
   *
   * @param context
   *          the context node (this is redundant info - already encoded in state - but allows the proxy to cache the
   *          values in such a way that they can be cleared by page.
   * @param astNode
   *          the node in the AST where we are at for evaluation
   * @param state
   *          the EvalState at this point
   * @return the output of eval_ at this point
   * @throws OXPathException
   *           in case of exception
   */
  public OXPathType eval_(final DOMNode context, final Node astNode, final PAATStateEvalIterative state)
      throws OXPathException {
    if (logger.isDebugEnabled()) logger.debug("Starting EVAL MINUS on '{}' at '{}'", context.getNodeName(), astNode);
    return eval_visitor.eval_(context, astNode, state);
  }

  boolean isNextClickAction(final Action action) {

    if (action.getActionType().equals(ActionType.KEYWORD)) {
      final ActionKeywords k = (ActionKeywords) action.getValue();
      if (k == ActionKeywords.NEXTCLICK)
        return true;
    }
    return false;
  }

  boolean isClickWithChangeAction(final Action action) {

    if (action.getActionType().equals(ActionType.KEYWORD)) {
      final ActionKeywords k = (ActionKeywords) action.getValue();
      if (k == ActionKeywords.CLICKWITHCHANGE)
        return true;
    }
    return false;
  }

  /**
   * Facilitates action evaluation
   *
   * @param contextNode
   *          node for action
   * @param action
   *          action to perform
   * @param protect
   *          {@code true} if page is protected, {@code false} otherwise
   * @param actionID
   *          identifier for the action to close
   * @return the notional root element of the resulting document
   * @throws OXPathException
   *           in case of other exception
   */
  private OXPathContextNode takeAction(final OXPathContextNode contextNode, final Action action, final boolean protect,
      final int actionID) throws OXPathException {
    // final long start = System.currentTimeMillis();

    // first, handle URL actions
    if (action.getActionType().equals(ActionType.URL)) {
    	DOMNode newRoot = null; 
      try {
        if (logger.isInfoEnabled()) logger.info("Navigating to '{}'", action.getValue());
        webclient.navigate((String) action.getValue());
        actionEngine.waitIfRequested(action, null);
        if (logger.isInfoEnabled()) logger.info("...navigated successfully to '{}'", action.getValue());
      } catch (final UnhandledAlertException e) {
        logger.warn("After action execution: trying dismissing alert: {}", e.getMessage());
      }
      try {
          newRoot = webclient.getContentDOMWindow().getDocument().getDocumentElement();
          // TODO: All Selenium-related exceptions should be in the web-api code. Maybe write wrappers for exceptions? 
        } catch (final UnhandledAlertException e) { //Most of the time, this exception is caused by the geolocation related problems
          logger.warn("After document access: trying dismissing alert: {}", e.getMessage());
          if (newRoot == null)
        	  newRoot = webclient.getContentDOMWindow().getDocument().getDocumentElement();
        }
      if (newRoot == null)
    	  throw new OXPathRuntimeException("I cannot dismiss alert", logger);
		return new OXPathContextNode(newRoot, contextNode.getParent(), contextNode.getLast());
    }

    final DOMNode context = contextNode.getNode();
    final int parentExtract = contextNode.getParent();
    final int lastExtract = contextNode.getLast();
    final DOMDocument ownerDocument = context.getOwnerDocument();
    final DOMElement page = ownerDocument.getDocumentElement();

    final FieldTypes ft = ActionEngine.getFieldType(context);

    // needed for checking same page or differences
    final SimplePageStateRecorder state = new SimplePageStateRecorder(ownerDocument.getBrowser());
    state.recordPageState();

    DOMMutationObserver bodyObserver = null;
    if (autocompleteReaction && isActionCompatibleWithAutocomplete(action.getActionType(), context)) {
      // install also on body for general changes
      bodyObserver = MutationDetectionUtils.installObserverOnBody(webclient,
          MutationDetectionUtils.mutationObserverOptions(true, true, true, false, null));
    }

    DOMElement newPage = actionEngine.takeAction(context, ft, action);

    
  // here we try possibly autocomplete
  if (autocompleteReaction && bodyObserver != null) {
	try {
        if (logger.isInfoEnabled()) logger.info("Looking for autocomplete list after action <{}>...", action);
        final DOMNode found = MutationDetectionUtils.performAutocompleteIfAny(bodyObserver, webclient);

        if (found != null) {
          if (logger.isInfoEnabled()) logger.info("...autocomplete list found and clicked first element <{}>", found.getTextContent());
          actionEngine.waitIfRequested(action, context);
        } else {
          if (logger.isInfoEnabled()) logger.info("...no autocomplete list found");
        }
    } catch (final UnhandledAlertException e) {
      logger.warn("dismissing alert after autocomplete <{}>", e.getMessage());
    }
  }
    

    // state after the action
    state.recordPageState();

    final String urlAfterAction = footprintPage();

    if (isClickWithChangeAction(action)) {
      if (newPage == null) {
        if (logger.isDebugEnabled()) logger.debug("Stopping evaluation as action {} wants to click on a disabled element {}",
            ActionKeywords.CLICKWITHCHANGE, contextNode.getNode().toPrettyString());
        return null;
      } else {
        if (!state.hasPageChanged())
          return null;
      }
    }
    // FIXME why?
    if (newPage == null) {
      newPage = page;
    }

    // treating the nextclickaction
    if (isNextClickAction(action)) {
      if (state.atSameLocation() || visitedNextLink.contains(urlAfterAction)) {
        if (logger.isInfoEnabled()) logger.info("Stopping evaluation as action {} is going to visit again the url {}", ActionKeywords.NEXTCLICK,
            urlAfterAction);
        return null;
      } else {
        visitedNextLink.add(urlAfterAction);
      }
    }
    // final long currentTimeMillis = System.nanoTime();
    // while ((System.nanoTime() - currentTimeMillis) < 3000)
    // if (logger.isInfoEnabled()) logger.info("Forced Awaiting 3000ms after '{}'", action);

    openActions.add(++currentAction);
    // waitIfRequested(action, context);
    final DOMDocument newDocument = newPage.getOwnerDocument();
    final DOMWindow newWindow = newDocument.getEnclosingWindow();
    final WebBrowser newBrowser = newWindow.getBrowser();

    if (newWindow.isJustOpened()) {
      if (logger.isDebugEnabled()) logger.debug("Newly opend window '{}'", newWindow);
      if (!protect) {
        freeMem(page.getOwnerDocument(), actionID);
      }
    } else if (protect) {

      if (state.hasPageChanged()) {// only if the action produced a page change

        markBackActionIfNecessary(action, newBrowser);

        // backController.get(newBrowser).push(
        // action.hasWait() ? new ActionStackElement(action.getWait()) : new ActionStackElement()); // we clear nothing
      }
    } else {
      if (logger.isDebugEnabled()) logger.debug("clearing local memory for the page {}", page.getBrowser().getLocationURL());
      clearObjectMem(page.getOwnerDocument(), actionID);

      incrementBackActionsIfNecessary(action, newBrowser);
    }
    return new OXPathContextNode(newPage, parentExtract, lastExtract);
  }

  private void incrementBackActionsIfNecessary(final Action action, final WebBrowser newBrowser) {

    if (!action.isBackActionNeeded())
      return;

    if (backController.containsKey(newBrowser) && !backController.get(newBrowser).isEmpty()) {
      final ActionStackElement ae = backController.get(newBrowser).pop().increment();
      backController.get(newBrowser).push(ae);
    }
  }

  private void markBackActionIfNecessary(final Action action, final WebBrowser newBrowser) {

    if (!action.isBackActionNeeded())
      return;
    if (logger.isDebugEnabled()) logger.debug("New page is protected, add back controller");
    if (!backController.containsKey(newBrowser)) {
      backController.put(newBrowser, new Stack<ActionStackElement>());
    }
    backController.get(newBrowser).push(new ActionStackElement(action));
  }

  private boolean isActionCompatibleWithAutocomplete(final ActionType actionType, final DOMNode target) {
    switch (actionType) {
    case EXPLICIT:
      if (target.getNodeType() == Type.ELEMENT)
        return ((DOMElement) target).htmlUtil().asTypeableElement() != null;
      else
        return false;
    default:// in case we have an ungrounded variable action
      return false;
    }
  }

  private boolean isSamePage(final String urlBeforeAction, final String urlAfterAction) {
    return urlAfterAction.equals(urlBeforeAction);
  }

  private String footprintPage() {
    return webclient.getURL().toString();
  }

  /**
   * Clears local object memory associated with the page
   *
   * @param page
   *          the page to clear memory from local data structures
   * @param actionID
   *          identifier for the action to close
   */
  private void clearObjectMem(final DOMDocument page, final int actionID) {
    evictCachesForPage(page);
    openActions.remove(actionID);
  }

  void evictCachesForPage(final DOMDocument page) {
    eval_visitor.clear(page);
    extractor.clear(page);
  }

  /**
   * This method clears buffered information associated with a specific web page (closes page and any memoized info)
   *
   * @param page
   *          the page to close and remove buffered info from
   * @param actionID
   *          identifier for the action to close
   * @throws OXPathException
   */
  private void freeMem(final DOMDocument page, final int actionID) throws OXPathException {
    try {
      if (logger.isDebugEnabled()) logger.debug("Freeing memory for page '{}'", page.getBrowser().getLocationURL());

      clearObjectMem(page, actionID);
      final WebBrowser currentBrowser = page.getBrowser();

      if (backController.containsKey(currentBrowser) && !backController.get(currentBrowser).isEmpty()) {
        final ActionStackElement actionElement = backController.get(currentBrowser).pop();
        final int backs = actionElement.getBackNumber();
        // final long start = System.currentTimeMillis();
        for (int i = 0; i < backs; i++) {
          ActionEngine.performBackAction(currentBrowser, actionElement.getAction());
        }
      } else {
        backController.remove(currentBrowser);
        page.getEnclosingWindow().close();
      }
    } catch (final NullPointerException e) {
      if (logger.isDebugEnabled()) logger.debug("Trying to close a window that was already closed!");
    }
  }

  /**
   * free Memory from a page given an arbitrary {@code OXPathContextNode} from the page.
   *
   * @param context
   *          an {@code OXPathContextNode} from which to clear associated page memory
   * @param actionID
   *          identifier for the action to close
   * @throws OXPathException
   */
  private void freeMem(final OXPathContextNode context, final int actionID) throws OXPathException {
    try {
      if (context.getNode().getOwnerDocument() != null) {// the page hasn't been closed yet
        final DOMDocument oldPage = context.getNode().getOwnerDocument();
        this.freeMem(oldPage, actionID);
      }
    } catch (final NullPointerException e) {// we don't care if something was null; we've already cleared it
      logger.warn("Ignoring deliberately NullpointerException on freeing memory");
    }
  }

  /**
   * Closes open windows and finishes query evaluation
   *
   * @return same <tt>TreeWalker</tt> object with WebClient closed properly
   * @throws OXPathException
   *           in case of problems closing objects/stream
   */
  private void endWalk() throws OXPathException {
    extractor.endExtraction();
  }

}
