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
import uk.ac.ox.cs.diadem.oxpath.utils.AttributeCollectorVisitor.MyData;

/**
 * Simple visitor implementation to print out OXPath expressions into {@code String} objects that are encoded as AST
 * trees.
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
public class AttributeCollectorVisitor extends OXPathVisitorGenericAdaptor<MyData, CustomSimpleNode> {

  public static class MyData {

    private Class<?> expected = String.class;
    private boolean nextIsRecord;
    private boolean nextIsAttribute;
    private String attributePath;
    private ASTSimpleOXPathStepPath beforeRecord;

    public MyData() {
    }

    public void expectedClass(final Class<?> expected) {
      this.expected = expected;

    }

    public Class<?> getExpected() {
      return expected;
    }

    public void setExpectRecord(final boolean nextIsRecord) {
      this.nextIsRecord = nextIsRecord;
      nextIsAttribute = false;

    }

    public void setExpectAttribute(final boolean nextIsAttribute) {
      this.nextIsAttribute = nextIsAttribute;
      nextIsRecord = false;

    }

    public boolean nextIsAttribute() {
      return nextIsAttribute;
    }

    public boolean nextIsRecord() {
      return nextIsRecord;
    }

    public String getLastSimplePath() {
      return attributePath;
    }

    public void setLastSimplePath(final String attributePath) {
      this.attributePath = attributePath;

    }

    public void setStartNode(final ASTSimpleOXPathStepPath node) {
      beforeRecord = node;
    }

    public ASTSimpleOXPathStepPath getStartNode() {
      return beforeRecord;
    }

  }

  private static final Logger log = LoggerFactory.getLogger(AttributeCollectorVisitor.class);
  private int indent = 0;
  private final StringBuilder out = new StringBuilder();

  /**
   * Empty constructor
   */
  public AttributeCollectorVisitor() {

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
  public CustomSimpleNode visitNode(final CustomSimpleNode node, final MyData data) throws OXPathException {
    return visitNode((SimpleNode) node, data);
  }

  @Override
  public CustomSimpleNode visitNode(final SimpleNode node, final MyData data) throws OXPathException {

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
  public CustomSimpleNode visitNode(final ASTExpression node, final MyData data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTSimpleOXPathStepPath node, final MyData data) throws OXPathException {

    // if is a possible starting point or we are in the middle of predicates
    final boolean start = !data.getExpected().isAssignableFrom(node.getClass());
    MyData currentData = data;
    if (start) {
      currentData = new MyData();
    }

    if (node.hasList()) {

      final Node child = node.jjtGetChild(0);

      if (node.isPlainXPath()) {
        currentData.expectedClass(ASTOXPathExtractionMarker.class);

        if (start) {
          currentData.setExpectRecord(true);
          currentData.setStartNode(node);
        } else {
          currentData.setExpectAttribute(true);
          currentData.setLastSimplePath(node.getLocationPath().toOXPath());
        }
      }
      accept(child, currentData);

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
  public CustomSimpleNode visitNode(final ASTOXPathExtractionMarker node, final MyData data) throws OXPathException {
    final boolean ok = data.getExpected().isAssignableFrom(node.getClass());

    int i = 0;

    if (node.getExtractionMarker().isAttribute()) {

      if (ok && data.nextIsAttribute()) {

        // data.annotate("Annotate attribute on node <" + data.getStartNode() + ">");
        // data.annotate(node.getExtractionMarker().getLabel() + " --> " + data.getLastSimplePath());
        data.getStartNode().putContextualAttribute(data.getLastSimplePath());

        i++;
        data.expectedClass(String.class);
        // accept(node.jjtGetChild(i++), null);
      }
    } else {
      if (ok && data.nextIsRecord()) {
        // data.annotate("Annotate record" + node.getExtractionMarker().getLabel());
        data.expectedClass(ASTXPathPredicate.class);
      }
    }

    if (node.hasList()) {
      final Node child = node.jjtGetChild(i);
      accept(child, data);
    } else {
      data.expectedClass(String.class);
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
  public CustomSimpleNode visitNode(final ASTXPathPredicate node, final MyData data) throws OXPathException {

    final boolean ok = node.isOptional() && data.getExpected().isAssignableFrom(node.getClass());

    if (ok) {
      data.expectedClass(ASTRelativeOXPathLocationPath.class);
    } else {
      data.expectedClass(String.class); // to fail
    }

    accept(node.jjtGetChild(0), data);

    if (node.hasList()) {
      if (ok) {
        data.expectedClass(ASTXPathPredicate.class);
      } else {
        data.expectedClass(String.class); // to fail
      }
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
  public CustomSimpleNode visitNode(final ASTRelativeOXPathLocationPath node, final MyData data) throws OXPathException {
    final boolean ok = data.getExpected().isAssignableFrom(node.getClass()) && !node.hasComplexPath();
    if (ok) {
      data.expectedClass(ASTSimpleOXPathStepPath.class);
    } else {
      data.expectedClass(String.class); // to fail
    }

    accept(node.jjtGetChild(0), data);

    if (node.hasSimplePath() && node.hasComplexPath()) {
      data.expectedClass(String.class); // to fail
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
  public CustomSimpleNode visitNode(final ASTOXPathKleeneStarPath node, final MyData data) throws OXPathException {

    accept(node.jjtGetChild(0), data);

    if (node.hasFollowingPath()) {
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
  public CustomSimpleNode visitNode(final ASTOXPathActionPath node, final MyData data) throws OXPathException {
    if (node.hasTail()) {
      accept(node.jjtGetChild(0), data);
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
  public CustomSimpleNode visitNode(final ASTOXPathNodeTestOp node, final MyData data) throws OXPathException {
    if (node.hasList()) {
      accept(node.jjtGetChild(0), data);
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
  public CustomSimpleNode visitNode(final ASTXPathLiteral node, final MyData data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTBinaryOpExpr node, final MyData data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTXPathUnaryExpr node, final MyData data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTXPathPrimaryExpr node, final MyData data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTXPathNumber node, final MyData data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTXPathFunctionCall node, final MyData data) throws OXPathException {
    if (node.getNumParameters() > 0) {// only if we have parameters
      for (int i = 0; i < (node.getNumParameters() - 1); i++) {
        accept(node.jjtGetChild(i), data);
      }
      accept(node.jjtGetChild(node.getNumParameters() - 1), data);
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
  public CustomSimpleNode visitNode(final ASTXPathPathExpr node, final MyData data) throws OXPathException {
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
