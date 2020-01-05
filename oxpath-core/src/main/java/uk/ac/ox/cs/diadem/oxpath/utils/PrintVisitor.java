/**
 * Supporting Java classes for OXPath
 */
package uk.ac.ox.cs.diadem.oxpath.utils;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.language.ActionType;
import uk.ac.ox.cs.diadem.oxpath.model.language.operators.BinaryOperator;
import uk.ac.ox.cs.diadem.oxpath.model.language.operators.EqualityExprOp;
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
public class PrintVisitor extends OXPathVisitorGenericAdaptor<Object, String> {

  private static final Logger log = LoggerFactory.getLogger(PrintVisitor.class);
  private int indent = 0;
  private final boolean followList;
  private final boolean rewriteOXPathShortcutIntoPlainXpath;

  /**
   * Empty constructor
   */
  public PrintVisitor(final boolean followList, final boolean rewriteOXPathShortcutIntoPlainXpath) {
    this.followList = followList;
    this.rewriteOXPathShortcutIntoPlainXpath = rewriteOXPathShortcutIntoPlainXpath;

  }

  public PrintVisitor() {
    this(true, false);
  }

  private String indent(final int delta) {
    indent += delta;
    final StringBuffer buff = new StringBuffer();

    for (int i = 0; i < indent; i++) {
      buff.append("    ");
    }
    return buff.toString();
  }

  @Override
  public String visitNode(final CustomSimpleNode node, final Object data) throws OXPathException {
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    return accept(node.jjtGetChild(0), null);
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    if (node.isAbsolutePath()) {
      sb.append(SLASH);
    }
    sb.append(accept(node.jjtGetChild(0), null));
    if (node.hasSimplePath() && node.hasComplexPath()) {
      sb.append(SLASH + accept(node.jjtGetChild(1), null));
    }
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    return string;
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    // sb.append(node.getStep().toOXPath());
    sb.append(node.getLocationPath().toOXPath());
    if (node.hasList() && followList) {
      if (OXPathParser.hasChildByName(node, "SimpleOXPathStepPath")) {
        sb.append(SLASH); // compositional break - just so we can make the slash if necessary
      }
      sb.append(accept(node.jjtGetChild(0), null));
    }
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    return string;
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();

    sb.append("(");
    sb.append(accept(node.jjtGetChild(0), null));
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

    if (node.hasFollowingPath() && followList) {
      sb.append(SLASH + accept(node.jjtGetChild(1), null));
    }

    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    return string;
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
    if (node.hasTail() && followList) {
      sb.append(SLASH + accept(node.jjtGetChild(0), null));
    }
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    return string;
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    if (rewriteOXPathShortcutIntoPlainXpath) {
      sb.append(node.getSelectorPredicate().toXPath());
    } else {
      sb.append(node.getSelectorPredicate().getValue());
    }
    if (node.hasList() && followList) {
      if (OXPathParser.hasChildByName(node, "SimpleOXPathStepPath")) {
        sb.append(SLASH); // compositional break - just so we can make the slash if necessary
      }
      sb.append(accept(node.jjtGetChild(0), null));
    }
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    return string;
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final String string = encaseXpathString(node.getValue());
    syso("Expression in " + node.toString() + " is " + string);
    return string;

  }

  public static String encaseXpathString(final String input) {
    // If we don't have any " then encase string in "
    if (!input.contains("\""))
      return MessageFormat.format("\"{0}\"", input);

    // If we have some " but no ' then encase in '
    if (!input.contains("'"))
      return MessageFormat.format("''{0}''", input);

    // If we get here we have both " and ' in the string so must use Concat
    final StringBuilder sb = new StringBuilder("concat(");

    // Going to look for " as they are LESS likely than ' in our data so will minimise
    // number of arguments to concat.
    int lastPos = 0;
    int nextPos = input.indexOf("\"");
    while (nextPos != -1) {
      // If this is not the first time through the loop then seperate arguments with ,
      if (lastPos != 0) {
        sb.append(",");
      }

      sb.append(MessageFormat.format("\"{0}\",'\"'", input.substring(lastPos, nextPos - lastPos)));
      lastPos = ++nextPos;

      // Find next occurance
      nextPos = input.indexOf("\"", lastPos);
    }

    // sb.Append(")");
    sb.append(MessageFormat.format(",\"{0}\")", input.substring(lastPos)));

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
  public String visitNode(final ASTXPathPredicate node, final Object data) throws OXPathException {
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    sb.append(((node.isOptional()) ? "[?" : "[") + accept(node.jjtGetChild(0), null) + "]");
    if (node.hasList() && followList) {
      if (OXPathParser.hasChildByName(node, "SimpleOXPathStepPath")) {
        sb.append(SLASH); // compositional break - just so we can make the slash if necessary
      }
      sb.append(accept(node.jjtGetChild(1), null));
    }
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    return string;
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
      sb.append(accept(node.jjtGetChild(i++), null));
    }
    sb.append(">");
    if (node.hasList() && followList) {
      if (OXPathParser.hasChildByName(node, "SimpleOXPathStepPath")) {
        sb.append(SLASH); // compositional break - just so we can make the slash if necessary
      }
      sb.append(accept(node.jjtGetChild(i), null));
    }
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    return string;
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    String r = null;

    // contains(.,'Next') -->
    final BinaryOperator binaryOperator = node.getBinaryOperator();

    if (rewriteOXPathShortcutIntoPlainXpath) {
      final String container = accept(node.jjtGetChild(0), null);
      final String contained = accept(node.jjtGetChild(1), null);
      if (binaryOperator == EqualityExprOp.CONTAINS) {
        // rewrite .#='Next' into contains(.,'Next')
        r = "contains(" + container + "," + contained + ")";
      } else if (binaryOperator == EqualityExprOp.WORDTEST) {
        // word containement as defined in http://www.w3.org/TR/CSS2/selector.html#matching-attrs
        r = contained + "!= '' and not (contains(" + contained + ",' ')) and contains( concat( ' ', " + container
            + ", ' ' ), concat( ' ', " + contained + ", ' ' ))";
      }
      if (r != null)
        return r;
    }
    // here all the other cases, which are the same even rewritten
    r = accept(node.jjtGetChild(0), null) + binaryOperator.getOperator() + accept(node.jjtGetChild(1), null);
    syso("Expression in " + node.toString() + " is " + r);
    return r;
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < node.getNumberOperators(); i++) {
      sb.append(node.getUnaryOperator().getOperator());
    }
    sb.append(accept(node.jjtGetChild(0), null));// because we count the number of operators, but the index starts
    // at 0
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    return string;
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final String result = accept(node.jjtGetChild(0), null);
    final String string = "(" + result + ")";
    syso("Expression in " + node.toString() + " is " + string);
    return string;

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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final String string = Double.toString(node.getValue());
    syso("Expression in " + node.toString() + " is " + string);
    return string;
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    sb.append(node.getFunction().getName());
    sb.append("(");
    if (node.getNumParameters() > 0) {// only if we have parameters
      for (int i = 0; i < (node.getNumParameters() - 1); i++) {
        sb.append(accept(node.jjtGetChild(i), null));
        sb.append(",");
      }
      sb.append(accept(node.jjtGetChild(node.getNumParameters() - 1), null));
    }
    sb.append(")");
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    return string;
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
    log.debug(indent(1) + node.toString() + ", " + node.jjtGetValue());
    final StringBuilder sb = new StringBuilder();
    sb.append(accept(node.jjtGetChild(0), null));
    sb.append(accept(node.jjtGetChild(1), null));
    if (node.hasComplexList() && node.hasSimpleList() && followList) {
      sb.append(accept(node.jjtGetChild(2), null));
    }
    final String string = sb.toString();
    syso("Expression in " + node.toString() + " is " + string);
    return string;
  }

  private void syso(final String string) {
    if (false) {
      System.out.println(string);
    }
  }

  public static final String SLASH = "/";
}
