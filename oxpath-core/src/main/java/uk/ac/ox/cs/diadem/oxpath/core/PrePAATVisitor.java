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

import static uk.ac.ox.cs.diadem.oxpath.core.PositionFuncEnum.LAST;
import static uk.ac.ox.cs.diadem.oxpath.core.PositionFuncEnum.NEITHER;
import static uk.ac.ox.cs.diadem.oxpath.core.PositionFuncEnum.POSITION;

import uk.ac.ox.cs.diadem.oxpath.model.language.functions.XPathFunctions;
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

/**
 * Visitor meant to prepare an AST for PAAT by annotating specific AST nodes with contextual information required during
 * PAAT evaluation to allow PAAT to otherwise evaluate compositionally (with the minor additional exception being the
 * evaluation of the Action-Free Prefix). Conceptually, this Visitor must annotate specific nodes so that:
 * <p>
 * (1) Step and predicates are exposed to whether or not a positional function (<tt>position()</tt> or <tt>last()</tt> )
 * is called within the predicate.
 * <p>
 * (2) Actions are exposed to whether or not they are being evaluated inside Kleene stars.
 * <p>
 * Decoration of positional functions (1) is accomplished through a bottom-up process (as ancestor nodes in the AST
 * require information about descendants). This is communicated, then, through the return values of node visits. In
 * contrast, decoration of action nodes is accomplished via a top-down process (as these nodes are descendants of the
 * Kleene-star nodes that necessarily modify their evaluation). This data if communicated by input values to the visits.
 * In this way, the two tasks are complementary and can be viewed as separate processes utilizing separate channels of
 * visit inputs and outputs.
 * <p>
 *
 * Visitor meant to prepare an AST for PAAT by annotating the appropriate steps and predicates in order to facilitate
 * set-based evaluation for handling <tt>position()</tt> and <tt>last()</tt> functions. Here {@code T} is not used
 * (coded as {@code Object}) and {@code U} is return type of {@code PositionFuncEnum}.
 *
 * Intuition: this is necessary because <tt>last()</tt> requires a <tt>count()</tt> of the context set before
 * evaluation. We do the same for <tt>position()</tt> because it requires a sorting of the context set (which the DIADEM
 * common web browsing API does not guarantee). Therefore, we have to sort after an OXPath axis navigation when made.
 * Only individual steps and step predicates (predicates '[expr]', optional predicates '[?expr]', node test operators
 * '#NCNAME'|'.NCNAME', and extraction markers ':&gt label &lt'.
 *
 * A predicate (optional predicate) is selected for set-based evaluation if its main path contains positional functions.
 * In addition, a predicate is selected for set-based evaluation if it occurs in the AST between a previously selected
 * predicate and its nearest-ancestor step (i.e. the step the predicates filter). Further, a step is marked if it has
 * any marked predicates. As <tt>position()</tt> and <tt>last()</tt> are functions (which, when encountered, pass
 * {@code true}), higher order productions pass results of children, while lower order productions than functions pass
 * {@code false}.
 *
 * <p>
 * The inputs for actions are generally determined by parents. They are initially {@code null} (set in the
 * {@code ASTExpression} node) and most nodes just pass the input through to child visits. Both actions and Kleene-stars
 * must be annotated, so that actions aren't closed if they are inside Kleene-stars (line 16 of eval in PAAT) at the
 * action or inside a nested Kleene star, but only after of <i>t</i> in the PAAT evaluation. In case of nested Kleene
 * stars, the pointer to the outer-most star is used, as the context is preserved through outer cleaner stars. This
 * pointer refers to memory that must be cleared only after evaluating the "rest" of the Kleene star expression.
 *
 *
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
class PrePAATVisitor extends OXPathVisitorGenericAdaptor<Node, PositionFuncEnum> {

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final CustomSimpleNode node, final Node data) throws OXPathException {
    // this should never happen
    throw new OXPathException("Evaluated CustomSimpleNode visit in PrePAATVisitor - unexpected AST node"
        + node.getClass() + " parsed!");
  }

  @Override
  public PositionFuncEnum visitNode(final SimpleNode node, final Node data) throws OXPathException {
    // this should never happen
    throw new OXPathException("Evaluated SimpleNode visit in PrePAATVisitor - unexpected AST node" + node.getClass()
        + " parsed!");
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTExpression node, final Node data) throws OXPathException {
    return accept(node.jjtGetChild(0), null);// we are not in a Kleene-star at the beginning
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTRelativeOXPathLocationPath node, final Node data) throws OXPathException {
    // already below functions in the binding of AST nodes
    accept(node.jjtGetChild(0), (node.hasSimplePath()) ? null : data);// we only care about actions/Kleene stars on
    // main paths
    if (node.hasComplexPath() && node.hasSimplePath()) {
      accept(node.jjtGetChild(1), data);
    }
    return NEITHER;
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTSimpleOXPathStepPath node, final Node data) throws OXPathException {
    // already below functions in the binding of AST nodes
    int childCount = 0;
    if (node.hasList()) {
      node.setSetBasedEval(accept(node.jjtGetChild(childCount++), data));
    }
    return NEITHER;
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTOXPathKleeneStarPath node, final Node data) throws OXPathException {
    // handle nested Kleene-star
    node.setInsideKleeneStar(data);
    // already below functions in the binding of AST nodes
    accept(node.jjtGetChild(0), node);// this is expression is inside this Kleene-star
    if (node.hasFollowingPath()) {
      accept(node.jjtGetChild(1), data);// in case of nesting, we are inside another Kleene-star
    }
    return NEITHER;
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTOXPathActionPath node, final Node data) throws OXPathException {
    // handle case where inside Kleene-star
    final boolean evalInsideKleene = !node.hasTail() || !OXPathParser.hasActionOnMainPath(node.jjtGetChild(0)); // only
    // the
    // last action
    // on the main
    // path needs
    // to consider
    // this
    node.setInsideKleeneStar((evalInsideKleene) ? data : null);
    // already below functions in the binding of AST nodes
    if (node.hasTail()) {
      accept(node.jjtGetChild(0), data);
    }
    return NEITHER;
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTOXPathNodeTestOp node, final Node data) throws OXPathException {
    if (node.hasList()) {
      final PositionFuncEnum result = accept(node.jjtGetChild(0), data);
      node.setSetBasedEval(result);
      return result;
    } else
      return NEITHER;
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTXPathLiteral node, final Node data) throws OXPathException {
    // already below functions in the binding of AST nodes
    return NEITHER;
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTXPathPredicate node, final Node data) throws OXPathException {
    final PositionFuncEnum result = accept(node.jjtGetChild(0), data);
    if (node.hasList()) {
      final PositionFuncEnum tailResult = accept(node.jjtGetChild(1), data);
      node.setSetBasedEval(tailResult);// since this was passed a set-based implementation, we only set this again based
      // on the tail
      return result.combine(tailResult);
    } else
      return result;
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTOXPathExtractionMarker node, final Node data) throws OXPathException {
    int child = 0;
    PositionFuncEnum result = NEITHER;
    if (node.getExtractionMarker().isAttribute()) {
      result = accept(node.jjtGetChild(child++), data);
    }
    if (node.hasList()) {
      final PositionFuncEnum tailResult = accept(node.jjtGetChild(child), data);
      node.setSetBasedEval(tailResult);// since this was passed a set-based implementation, we only set this again based
      // on the tail
      return result.combine(tailResult);
    } else
      return result;
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTBinaryOpExpr node, final Node data) throws OXPathException {
    return accept(node.jjtGetChild(0), data).combine(accept(node.jjtGetChild(1), data));
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTXPathUnaryExpr node, final Node data) throws OXPathException {
    return accept(node.jjtGetChild(0), data);
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTXPathPrimaryExpr node, final Node data) throws OXPathException {
    return accept(node.jjtGetChild(0), data);
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTXPathNumber node, final Node data) throws OXPathException {
    return POSITION;
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTXPathFunctionCall node, final Node data) throws OXPathException {
    // functions can occur as this call or as inputs to the function
    PositionFuncEnum result = NEITHER;
    if (node.getFunction().equals(XPathFunctions.POSITION)) {
      result = POSITION;
    } else if (node.getFunction().equals(XPathFunctions.LAST)) {
      result = LAST;
    }
    for (int i = 0; i < node.getNumParameters(); i++) {
      result = result.combine(accept(node.jjtGetChild(i), data));
    }
    return result;
  }

  /**
   * Applies PrePAAT to this node in the AST
   *
   * @param node
   *          the node on which to apply PrePAAT
   * @param data
   *          not used in this Visitor (vestigial part of the design pattern); can safely be {@code null}
   * @return encoding of whether predicates containing this expression have <tt>position()</tt> or <tt>last()</tt>
   *         functions
   * @throws OXPathException
   *           in case of malformed AST
   */
  @Override
  public PositionFuncEnum visitNode(final ASTXPathPathExpr node, final Node data) throws OXPathException {
    int numChild = 0;
    final PositionFuncEnum result = accept(node.jjtGetChild(numChild++), data);
    if (node.hasSimpleList()) {
      node.setSetBasedEval(accept(node.jjtGetChild(numChild++), data));
    }
    if (node.hasComplexList()) {
      accept(node.jjtGetChild(numChild++), data);
    }
    return result;
  }
}
