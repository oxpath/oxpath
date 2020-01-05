package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.model.language.operators.BinaryOperator;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

public class ASTBinaryOpExprCustom extends CustomSimpleNode {

  public ASTBinaryOpExprCustom(final int id) {
    super(id);
  }

  public ASTBinaryOpExprCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setBinaryOperator(final BinaryOperator oper) {
    op = oper;
  }

  public BinaryOperator getBinaryOperator() {
    return op;
  }

  private BinaryOperator op;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + getBinaryOperator().getOperator() + "]";
  }
}
