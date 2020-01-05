package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

/**
 * @author AndrewJSel
 *
 */
public class ASTXPathNumberCustom extends CustomSimpleNode {

  public ASTXPathNumberCustom(final int id) {
    super(id);
  }

  public ASTXPathNumberCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setValue(final double iValue) {
    value = iValue;
  }

  public double getValue() {
    return value;
  }

  private double value;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + value + "]";
  }

}