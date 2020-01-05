package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.model.language.functions.XPathFunction;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

/**
 * @author AndrewJSel
 *
 */
public class ASTXPathFunctionCallCustom extends CustomSimpleNode {

  public ASTXPathFunctionCallCustom(final int id) {
    super(id);
  }

  public ASTXPathFunctionCallCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setFunction(final XPathFunction iFunction) {
    function = iFunction;
  }

  public XPathFunction getFunction() {
    return function;
  }

  public void addParameter() {
    ++numParameters;
  }

  public int getNumParameters() {
    return numParameters;
  }

  private XPathFunction function;
  private int numParameters = 0;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + function.getName() + ",numParam=" + getNumParameters() + "]";
  }

}
