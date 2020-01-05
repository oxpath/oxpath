package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

/**
 * @author AndrewJSel
 *
 */
public class ASTXPathLiteralCustom extends CustomSimpleNode {

  public ASTXPathLiteralCustom(final int id) {
    super(id);
  }

  public ASTXPathLiteralCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setValue(final String iValue) {
    value = iValue.substring(1, iValue.length() - 1);// strip off quotes
  }

  public String getValue() {
    return value;
  }

  private String value;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + value + "]";
  }
}