package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.model.language.operators.NegativeOperator;
import uk.ac.ox.cs.diadem.oxpath.model.language.operators.UnaryOperator;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

public class ASTXPathUnaryExprCustom extends CustomSimpleNode {

  public ASTXPathUnaryExprCustom(final int id) {
    super(id);
  }

  public ASTXPathUnaryExprCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void incrementOperatorNumber() {
    ++numOps;
  }

  public int getNumberOperators() {
    return numOps;
  }

  public UnaryOperator getUnaryOperator() {
    return NegativeOperator.NEGATIVE;
  }

  private int numOps = 0;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[numOps=" + numOps + "]";
  }

}
