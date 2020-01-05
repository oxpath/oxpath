/**
 * Supporting Java classes for OXPath
 */
package uk.ac.ox.cs.diadem.oxpath.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
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
import uk.ac.ox.cs.diadem.oxpath.utils.RelativePathCollector.MyLocalContext;

/**
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
public class RelativePathCollector extends OXPathVisitorGenericAdaptor<MyLocalContext, CustomSimpleNode> {

  private static final Logger log = LoggerFactory.getLogger(RelativePathCollector.class);
  private final int indent = 0;
  private final StringBuilder out = new StringBuilder();

  static class MyLocalContext {

    private ASTSimpleOXPathStepPath node;

    public MyLocalContext(final ASTSimpleOXPathStepPath node) {
      this.node = node;
    }

    public ASTSimpleOXPathStepPath getNode() {
      return node;
    }

    public void setNode(final ASTSimpleOXPathStepPath neWnode) {
      node = neWnode;

    }
  }

  /**
   * Empty constructor
   */
  public RelativePathCollector() {

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
  public CustomSimpleNode visitNode(final SimpleNode node, final MyLocalContext data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final CustomSimpleNode node, final MyLocalContext data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTExpression node, final MyLocalContext data) throws OXPathException {
    accept(node.jjtGetChild(0), data);
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
  public CustomSimpleNode visitNode(final ASTSimpleOXPathStepPath node, final MyLocalContext data)
      throws OXPathException {

    MyLocalContext nextContext = null;
    if (node.isPlainXPath()) {

      if (data != null) {
        final ASTSimpleOXPathStepPath anchor = data.getNode();
        anchor.putContextualAttribute(node.getLocationPath().toOXPath());
      }

      nextContext = new MyLocalContext(node);

    }

    if (node.hasList()) {
      accept(node.jjtGetChild(0), nextContext);

    }
    if (node.getContextualAttributes() != null) {
      annotate(node + " has attributes: " + node.getContextualAttributes());
    }

    return null;
  }

  void annotate(final String string) {
    out.append(string);
    out.append("\n");
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
  public CustomSimpleNode visitNode(final ASTOXPathExtractionMarker node, final MyLocalContext data)
      throws OXPathException {

    int i = 0;
    MyLocalContext nextContext = null;
    if (node.getExtractionMarker().isAttribute()) {
      i++;
    } else {
      nextContext = data;
    }

    if (node.hasList()) {
      final Node child = node.jjtGetChild(i);

      accept(child, nextContext);
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
  public CustomSimpleNode visitNode(final ASTXPathPredicate node, final MyLocalContext data) throws OXPathException {

    accept(node.jjtGetChild(0), data);
    if (node.hasList()) {
      accept(node.jjtGetChild(1), data);
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
  public CustomSimpleNode visitNode(final ASTRelativeOXPathLocationPath node, final MyLocalContext data)
      throws OXPathException {

    accept(node.jjtGetChild(0), data);

    if (node.hasSimplePath() && node.hasComplexPath()) {
      accept(node.jjtGetChild(1), data);
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
  public CustomSimpleNode visitNode(final ASTOXPathKleeneStarPath node, final MyLocalContext data)
      throws OXPathException {

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
  public CustomSimpleNode visitNode(final ASTOXPathActionPath node, final MyLocalContext data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTOXPathNodeTestOp node, final MyLocalContext data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTXPathLiteral node, final MyLocalContext data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTBinaryOpExpr node, final MyLocalContext data) throws OXPathException {
    accept(node.jjtGetChild(0), data);
    accept(node.jjtGetChild(1), data);
    return null;
  }

  public String getResult() {
    return out.toString();
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
  public CustomSimpleNode visitNode(final ASTXPathUnaryExpr node, final MyLocalContext data) throws OXPathException {
    accept(node.jjtGetChild(0), data);// because we count the number of operators, but the index starts
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
  public CustomSimpleNode visitNode(final ASTXPathPrimaryExpr node, final MyLocalContext data) throws OXPathException {
    accept(node.jjtGetChild(0), data);
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
  public CustomSimpleNode visitNode(final ASTXPathNumber node, final MyLocalContext data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTXPathFunctionCall node, final MyLocalContext data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTXPathPathExpr node, final MyLocalContext data) throws OXPathException {
    accept(node.jjtGetChild(0), data);
    accept(node.jjtGetChild(1), data);
    if (node.hasComplexList() && node.hasSimpleList()) {
      accept(node.jjtGetChild(2), data);
    }
    return null;
  }

  private void syso(final String string) {
    System.out.println(string);
  }

  public static final String SLASH = "/";

}
