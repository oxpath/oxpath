/**
 * Supporting Java classes for OXPath
 */
package uk.ac.ox.cs.diadem.oxpath.utils;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.language.ActionType;
import uk.ac.ox.cs.diadem.oxpath.model.language.AggregatedXPathStep;
import uk.ac.ox.cs.diadem.oxpath.model.language.AxisType;
import uk.ac.ox.cs.diadem.oxpath.model.language.NodeTestType;
import uk.ac.ox.cs.diadem.oxpath.model.language.SelectorPredicate;
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
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
public class SimpleXPathAggregatorVisitor extends OXPathVisitorGenericAdaptor<List<String>, CustomSimpleNode> {

  private static final Logger log = LoggerFactory.getLogger(SimpleXPathAggregatorVisitor.class);
  private int indent = 0;

  /**
   * Empty constructor
   */
  public SimpleXPathAggregatorVisitor() {

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
  public CustomSimpleNode visitNode(final SimpleNode node, final List<String> data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final CustomSimpleNode node, final List<String> data) throws OXPathException {
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
  public CustomSimpleNode visitNode(final ASTExpression node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());

    return accept(node.jjtGetChild(0), data);
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
  public CustomSimpleNode visitNode(final ASTRelativeOXPathLocationPath node, final List<String> data)
      throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    if (node.isAbsolutePath()) {
      sb.append(SLASH);
    }
    final CustomSimpleNode child = accept(node.jjtGetChild(0), data);
    sb.append(data.remove(0));
    if (node.hasSimplePath() && node.hasComplexPath()) {
      accept(node.jjtGetChild(1), data);
      sb.append(SLASH + data.remove(0));
      return clear(data);
    }
    if (child == null)
      return clear(data);
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    data.add(string);
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
  public CustomSimpleNode visitNode(final ASTSimpleOXPathStepPath node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    // final StringBuilder sb = new StringBuilder();
    // sb.append(node.getStep().getAxis().getValue() + node.getStep().getNodeTest().getValue());

    CustomSimpleNode childNode = null;

    if (node.hasList()) {

      final Node child = node.jjtGetChild(0);
      // return null when it's not possible to aggregate
      childNode = accept(child, data);
      if ((childNode == null) && !node.jjtGetParent().toString().contains("ASTSimpleOXPathStepPath"))
        return clear(data);
    }

    if (node.getStep().getAxis().getType() == AxisType.OXPATH)
      return clear(data);
    if (node.getStep().getNodeTest().getType() == NodeTestType.OXPATH) {
      clear(data);

    }

    // here aggregation if possible
    if (childNode instanceof ASTSimpleOXPathStepPath) {
      final ASTSimpleOXPathStepPath astSimple = (ASTSimpleOXPathStepPath) childNode;
      // not really used, but necessary to clean the stack
      data.remove(0);

      astSimple.setStep(new AggregatedXPathStep.Builder(node.getStep()).aggregateStep(
          SLASH + astSimple.getStep().toOXPath()).buildStep());

      node.mergeWith(childNode);

      final String string = astSimple.getStep().toOXPath();
      data.add(string);
      return astSimple;
      // node.setHasList(astSimple.hasList());
      // node.nullifyChildren();
    } else if (childNode instanceof ASTOXPathNodeTestOp) {
      // reat like another ASTSimpleOXPathStepPath
      final ASTOXPathNodeTestOp nodeTest = (ASTOXPathNodeTestOp) childNode;
      node.setStep(new AggregatedXPathStep.Builder(node.getStep()).aggregateStep(data.remove(0)).buildStep());
      node.removeChild(nodeTest);
      node.setHasList(false);
      // if (!node.hasList()) {
      // node.removeChild(nodeTest);
      // } else {
      // childNode.mergeWith(childNode.jjtGetChild(0));
      // }
      final String string = node.getStep().toOXPath();
      data.add(string);
      return node;

    } else if (childNode instanceof ASTXPathPredicate) {
      // TODO check if condition for aggregation of last step
      final ASTXPathPredicate pred = (ASTXPathPredicate) childNode;
      node.setStep(new AggregatedXPathStep.Builder(node.getStep()).aggregateStep(data.remove(0)).buildStep());
      node.removeChild(pred);
      node.setHasList(false);
      final String string = node.getStep().toOXPath();
      data.add(string);
      return node;

    }

    // else we compute the string to aggregate in the caller
    final String string = node.getStep().toOXPath();
    data.add(string);
    syso("Expression in " + node + " is " + string);
    return node;
  }

  private CustomSimpleNode clear(final List<String> data) {
    data.clear();
    data.add("");
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
  public CustomSimpleNode visitNode(final ASTOXPathKleeneStarPath node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();

    sb.append("(");
    accept(node.jjtGetChild(0), data);
    sb.append(data.remove(0));
    sb.append(")*");

    if (node.hasLowerBound()) {
      sb.append("{");
      sb.append(node.getLowerBound());
      if (node.hasUpperBound()) {
        sb.append(",");
        sb.append(node.getUpperBound());
      }
      sb.append("}");
    }

    if (node.hasFollowingPath()) {
      accept(node.jjtGetChild(1), data);
      sb.append(SLASH + data.remove(0));
    }

    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    data.add(string);
    return clear(data);
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
  public CustomSimpleNode visitNode(final ASTOXPathActionPath node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    switch (node.getAction().getActionType()) {
    case URL:
      sb.append("doc(\"");
      sb.append(node.getAction().getValue());
      sb.append("\"");
      if (node.getAction().hasWait()) {
        sb.append(",[wait=" + node.getAction().getWait() + "]");
      }
      sb.append(")");
      break;
    case EXPLICIT:
    case KEYWORD:
    case POSITION:
    case VARIABLE:
      sb.append("{");
      if (node.getAction().getActionType().equals(ActionType.EXPLICIT)) {
        sb.append("\"");
      }
      sb.append(node.getAction().getValue());
      if (node.getAction().getActionType().equals(ActionType.EXPLICIT)) {
        sb.append("\"");
      }
      if (node.getAction().hasWait()) {
        sb.append("[wait=" + node.getAction().getWait() + "]");
      }
      if (node.getAction().isAbsoluteAction()) {
        sb.append(SLASH);
      }
      sb.append("}");
      break;
    }
    if (node.hasTail()) {
      accept(node.jjtGetChild(0), data);
      sb.append(SLASH + data.remove(0));
    }
    final String string = sb.toString();
    // not propagate
    syso("Expression in " + node.toString() + " is " + string);

    return clear(data);
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
  public CustomSimpleNode visitNode(final ASTOXPathNodeTestOp node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();

    final SelectorPredicate predicate = node.getSelectorPredicate();

    sb.append(predicate.toXPath());
    if (node.hasList()) {
      if (OXPathParser.hasChildByName(node, "SimpleOXPathStepPath")) {
        sb.append(SLASH); // compositional break - just so we can make the slash if necessary
      }
      final CustomSimpleNode child = accept(node.jjtGetChild(0), data);
      sb.append(data.remove(0));
      if (child == null)
        return clear(data);
    }
    final String string = sb.toString();
    data.add(string);
    syso("Expression in " + node.toString() + " is " + string);
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
  public CustomSimpleNode visitNode(final ASTXPathLiteral node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final String string = "'" + node.getValue() + "'";
    syso("Expression in " + node.toString() + " is " + string);
    data.add(string);
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
  public CustomSimpleNode visitNode(final ASTXPathPredicate node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();

    final CustomSimpleNode childZero = accept(node.jjtGetChild(0), data);

    sb.append(((node.isOptional()) ? "[?" : "[") + data.remove(0) + "]");
    if (node.hasList()) {
      if (OXPathParser.hasChildByName(node, "SimpleOXPathStepPath")) {
        sb.append(SLASH); // compositional break - just so we can make the slash if necessary
      }
      final CustomSimpleNode child = accept(node.jjtGetChild(1), data);
      sb.append(data.remove(0));
      if (child == null)
        return clear(data);
    }
    if ((childZero == null) || node.isOptional())
      return clear(data);

    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    data.add(string);
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
  public CustomSimpleNode visitNode(final ASTOXPathExtractionMarker node, final List<String> data)
      throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    int i = 0;
    final StringBuilder sb = new StringBuilder();
    sb.append(":<");
    sb.append(node.getExtractionMarker().getLabel());

    // final Iterator<String> iter = node.getExtractionMarker().getRdfTypes();
    // if (iter.hasNext()) {
    // sb.append('(');
    // sb.append(iter.next());
    // while (iter.hasNext()) {
    // sb.append(", ");
    // sb.append(iter.next());
    // }
    // sb.append(')');
    // }

    if (node.getExtractionMarker().isAttribute()) {
      sb.append("=");
      accept(node.jjtGetChild(i++), data);
      sb.append(data.remove(0));
    }
    sb.append(">");
    if (node.hasList()) {
      if (OXPathParser.hasChildByName(node, "SimpleOXPathStepPath")) {
        sb.append(SLASH); // compositional break - just so we can make the slash if necessary
      }
      accept(node.jjtGetChild(i), data);
      sb.append(data.remove(0));
    }
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    data.add(string);
    return clear(data);
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
  public CustomSimpleNode visitNode(final ASTBinaryOpExpr node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    accept(node.jjtGetChild(0), data);

    String expt = data.remove(0) + node.getBinaryOperator().getOperator();
    accept(node.jjtGetChild(1), data);
    expt += data.remove(0);
    data.add(expt);
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
  public CustomSimpleNode visitNode(final ASTXPathUnaryExpr node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < node.getNumberOperators(); i++) {
      sb.append(node.getUnaryOperator().getOperator());
    }
    accept(node.jjtGetChild(0), data);

    sb.append(data.remove(0));// because we count the number of operators, but the index starts
    // at 0
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    data.add(string);
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
  public CustomSimpleNode visitNode(final ASTXPathPrimaryExpr node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    accept(node.jjtGetChild(0), data);
    final String string = "(" + data.remove(0) + ")";
    syso("Expression in " + node.toString() + " is " + string);
    data.add(string);
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
  public CustomSimpleNode visitNode(final ASTXPathNumber node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final String string = Double.toString(node.getValue());
    syso("Expression in " + node.toString() + " is " + string);
    data.add(string);
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
  public CustomSimpleNode visitNode(final ASTXPathFunctionCall node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    sb.append(node.getFunction().getName());
    sb.append("(");
    if (node.getNumParameters() > 0) {// only if we have parameters
      for (int i = 0; i < (node.getNumParameters() - 1); i++) {
        accept(node.jjtGetChild(i), data);
        sb.append(data.remove(0));
        sb.append(",");
      }
      accept(node.jjtGetChild(node.getNumParameters() - 1), data);
      sb.append(data.remove(0));
    }
    sb.append(")");
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    data.add(string);
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
  public CustomSimpleNode visitNode(final ASTXPathPathExpr node, final List<String> data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    accept(node.jjtGetChild(0), data);
    sb.append(data.remove(0));
    accept(node.jjtGetChild(1), data);
    sb.append(data.remove(0));

    if (node.hasComplexList() && node.hasSimpleList()) {
      accept(node.jjtGetChild(2), data);
      sb.append(data.remove(0));
    }
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    data.add(string);
    return node;
  }

  private void syso(final String string) {
    System.out.println(string);

  }

  public static final String SLASH = "/";
}
