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
import uk.ac.ox.cs.diadem.oxpath.parse.ast.SimpleNode;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.custom.CustomSimpleNode;
import uk.ac.ox.cs.diadem.oxpath.parse.visitor.OXPathVisitorGenericAdaptor;

/**
 * Simple visitor implementation to print out OXPath expressions into {@code String} objects that are encoded as AST
 * trees.
 *
 * @author AndrewJSel
 *
 */
public class DumpTreeVisitor extends OXPathVisitorGenericAdaptor<Object, String> {

  private static final Logger log = LoggerFactory.getLogger(DumpTreeVisitor.class);
  private int indent = 0;
  private final StringBuilder destination;

  /**
   * Empty constructor
   */
  public DumpTreeVisitor(final StringBuilder destination) {
    this.destination = destination;

  }

  private String indentString() {
    final StringBuffer sb = new StringBuffer();
    for (int i = 0; i < indent; ++i) {
      sb.append(' ');
    }
    return sb.toString();
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
  public String visitNode(final CustomSimpleNode node, final Object data) throws OXPathException {
    return visitNode((SimpleNode) node, data);
  }

  @Override
  public String visitNode(final SimpleNode node, final Object data) throws OXPathException {

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
  public String visitNode(final ASTExpression node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
  }

  private String printAndCarryOn(final CustomSimpleNode node, final Object data) throws OXPathException {
    // System.out.println(indentString() + node);
    destination.append(indentString());
    destination.append(node);
    destination.append("\n");

    ++indent;
    final Object d = node.childrenAccept(this, data);
    --indent;
    return (String) data;
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
  public String visitNode(final ASTRelativeOXPathLocationPath node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTSimpleOXPathStepPath node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTOXPathKleeneStarPath node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTOXPathActionPath node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTOXPathNodeTestOp node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTXPathLiteral node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTXPathPredicate node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTOXPathExtractionMarker node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTBinaryOpExpr node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTXPathUnaryExpr node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTXPathPrimaryExpr node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTXPathNumber node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTXPathFunctionCall node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
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
  public String visitNode(final ASTXPathPathExpr node, final Object data) throws OXPathException {
    return printAndCarryOn(node, data);
  }

  public static final String SLASH = "/";

}
