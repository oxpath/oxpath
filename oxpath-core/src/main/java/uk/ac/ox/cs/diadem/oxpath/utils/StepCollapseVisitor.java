/**
 * Supporting Java classes for OXPath
 */
package uk.ac.ox.cs.diadem.oxpath.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.language.AggregatedXPathStep;
import uk.ac.ox.cs.diadem.oxpath.model.language.AxisType;
import uk.ac.ox.cs.diadem.oxpath.model.language.NodeTestType;
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
import uk.ac.ox.cs.diadem.oxpath.parse.ast.SimpleNode;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.custom.CustomSimpleNode;
import uk.ac.ox.cs.diadem.oxpath.parse.visitor.OXPathVisitorGenericAdaptor;

/**
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
public class StepCollapseVisitor extends OXPathVisitorGenericAdaptor<Object, CustomSimpleNode> {

  private static final Logger log = LoggerFactory.getLogger(StepCollapseVisitor.class);
  private int indent = 0;

  /**
   * Empty constructor
   */
  public StepCollapseVisitor() {

  }

  private String indent(final int delta) {
    indent += delta;
    final StringBuffer buff = new StringBuffer();

    for (int i = 0; i < indent; i++) {
      buff.append("    ");
    }
    return buff.toString();
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final SimpleNode node, final Object data) throws OXPathException {
    // This method should never happen
    assert false : "Unexpected Node type encountered.  Program exiting...";
  return null;// for compiler only
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final CustomSimpleNode node, final Object data) throws OXPathException {
    return visitNode((SimpleNode) node, data);
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTExpression node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    accept(node.jjtGetChild(0), null);
    return null;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTRelativeOXPathLocationPath node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    accept(node.jjtGetChild(0), null);
    if (node.hasSimplePath() && node.hasComplexPath()) {
      accept(node.jjtGetChild(1), null);
    }
    return null;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTSimpleOXPathStepPath node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    CustomSimpleNode childNode = null;
    if (node.hasList()) {
      childNode = accept(node.jjtGetChild(0), null);
    }

    if (node.getStep().getAxis().getType() == AxisType.OXPATH)
      return null;
    if (node.getStep().getNodeTest().getType() == NodeTestType.OXPATH)
      return null;

    // here aggregation if possible
    if (childNode instanceof ASTSimpleOXPathStepPath) {
      final ASTSimpleOXPathStepPath astSimple = (ASTSimpleOXPathStepPath) childNode;

      astSimple.setStep(new AggregatedXPathStep.Builder(node.getStep()).aggregateStep(
          SLASH + astSimple.getStep().toOXPath()).buildStep());

      node.mergeWith(childNode);
      return astSimple;
    } else if (childNode instanceof ASTOXPathNodeTestOp)
      return null;
    else if (childNode instanceof ASTXPathPredicate)
      return null;
    else if (childNode instanceof ASTOXPathExtractionMarker)
      return null;

    return node;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTOXPathKleeneStarPath node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());

    accept(node.jjtGetChild(0), null);

    if (node.hasFollowingPath()) {
      accept(node.jjtGetChild(1), null);
    }

    return null;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTOXPathActionPath node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    if (node.hasTail()) {
      accept(node.jjtGetChild(0), null);
    }
    return null;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTOXPathNodeTestOp node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    if (node.hasList()) {
      accept(node.jjtGetChild(0), null);
    }
    return node;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTXPathLiteral node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    return null;

  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTXPathPredicate node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    accept(node.jjtGetChild(0), null);
    if (node.hasList()) {
      accept(node.jjtGetChild(1), null);
    }
    return node;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTOXPathExtractionMarker node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    int i = 0;

    if (node.getExtractionMarker().isAttribute()) {
      accept(node.jjtGetChild(i++), null);
    }
    if (node.hasList()) {
      accept(node.jjtGetChild(i), null);
    }
    return node;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTBinaryOpExpr node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    accept(node.jjtGetChild(0), null);
    accept(node.jjtGetChild(1), null);
    return null;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTXPathUnaryExpr node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    accept(node.jjtGetChild(0), null);// because we count the number of operators, but the index starts
    return null;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTXPathPrimaryExpr node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    accept(node.jjtGetChild(0), null);
    return null;

  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTXPathNumber node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    return null;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTXPathFunctionCall node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    if (node.getNumParameters() > 0) {// only if we have parameters
      for (int i = 0; i < (node.getNumParameters() - 1); i++) {
        accept(node.jjtGetChild(i), null);
      }
      accept(node.jjtGetChild(node.getNumParameters() - 1), null);
    }
    return null;
  }

  /**
   * Constructs the {@code String} representation of the subtree rooted at {@code node}
   *
   * @param node
   *          the subtree root the visitor prints
   * @param data
   *          <i>not used in this visitor</i>
   * @return the {@code String} representation of the expression subtree as determined by the visitor
   */
  @Override
  public CustomSimpleNode visitNode(final ASTXPathPathExpr node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    accept(node.jjtGetChild(0), null);
    accept(node.jjtGetChild(1), null);
    if (node.hasComplexList() && node.hasSimpleList()) {
      accept(node.jjtGetChild(2), null);
    }
    return null;
  }

  private void syso(final String string) {
    System.out.println(string);
  }

  public static final String SLASH = "/";
}
