/**
 * Package containing a generic adapter for OXPath computation
 */
package uk.ac.ox.cs.diadem.oxpath.parse.visitor;

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
import uk.ac.ox.cs.diadem.oxpath.parse.ast.OXPathParserVisitor;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.SimpleNode;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.custom.CustomSimpleNode;

/**
 *
 * Wrapper allowing generic specification of {@code Visitor} classes for the OXPath AST. Used to General contract for
 * this class is that all accept calls pass {@code data} of type {@code T} and return output of type {@code U}. This is
 * not safe; if the contract is broken, the class will break hard with a {@code CastClassException}. Use only for
 * convenience and test thoroughly. Implement the {@code visitNode} methods and call {@code this.accept(node,data)}
 * instead of {@code node.jjtAccept(visitor,data)} in order to avoid casting in visitor implementations.
 *
 * @author AndrewJSel
 *
 */
public abstract class OXPathVisitorGenericAdaptor<T, U> implements OXPathParserVisitor {

  public abstract U visitNode(CustomSimpleNode node, T data) throws OXPathException;

  public abstract U visitNode(SimpleNode node, T data) throws OXPathException;

  public abstract U visitNode(ASTExpression node, T data) throws OXPathException;

  public abstract U visitNode(ASTRelativeOXPathLocationPath node, T data) throws OXPathException;

  public abstract U visitNode(ASTSimpleOXPathStepPath node, T data) throws OXPathException;

  public abstract U visitNode(ASTOXPathKleeneStarPath node, T data) throws OXPathException;

  public abstract U visitNode(ASTOXPathActionPath node, T data) throws OXPathException;

  public abstract U visitNode(ASTOXPathNodeTestOp node, T data) throws OXPathException;

  public abstract U visitNode(ASTXPathLiteral node, T data) throws OXPathException;

  public abstract U visitNode(ASTXPathPredicate node, T data) throws OXPathException;

  public abstract U visitNode(ASTOXPathExtractionMarker node, T data) throws OXPathException;

  public abstract U visitNode(ASTBinaryOpExpr node, T data) throws OXPathException;

  public abstract U visitNode(ASTXPathUnaryExpr node, T data) throws OXPathException;

  public abstract U visitNode(ASTXPathPrimaryExpr node, T data) throws OXPathException;

  public abstract U visitNode(ASTXPathNumber node, T data) throws OXPathException;

  public abstract U visitNode(ASTXPathFunctionCall node, T data) throws OXPathException;

  public abstract U visitNode(ASTXPathPathExpr node, T data) throws OXPathException;

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final SimpleNode node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  // @Override
  public Object visit(final CustomSimpleNode node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTExpression node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTRelativeOXPathLocationPath node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTSimpleOXPathStepPath node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTOXPathKleeneStarPath node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTOXPathActionPath node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTOXPathNodeTestOp node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTXPathLiteral node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTXPathPredicate node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTOXPathExtractionMarker node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTBinaryOpExpr node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTXPathUnaryExpr node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTXPathPrimaryExpr node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTXPathNumber node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTXPathFunctionCall node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object visit(final ASTXPathPathExpr node, final Object data) throws OXPathException {
    return visitNode(node, (T) data);
  }

  @SuppressWarnings("unchecked")
  public U accept(final Node node, final T data) throws OXPathException {
    return (U) node.jjtAccept(this, data);
  }
}
