package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

/**
 * Package containing the JJTree OXPath parser specification.
 * In addition, package contains aspects associated with AST nodes.
 * These utilize crosscuts so generated code is not modified.
 */

import uk.ac.ox.cs.diadem.oxpath.core.PositionFuncEnum;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

/**
 * @author AndrewJSel
 *
 */
public class ASTXPathPredicateCustom extends CustomSimpleNode {

  public ASTXPathPredicateCustom(final int id) {
    super(id);
  }

  public ASTXPathPredicateCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setHasList(final boolean hl) {
    hasList = hl;
  }

  public boolean hasList() {
    return hasList;
  }

  public void setIsOptional(final boolean opt) {
    isOptional = opt;
  }

  public boolean isOptional() {
    return isOptional;
  }

  private boolean hasList = false;
  private boolean isOptional = false;

  public void setSetBasedEval(final PositionFuncEnum set) {
    setEval = set;
  }

  public PositionFuncEnum getSetBasedEval() {
    return setEval;
  }

  private PositionFuncEnum setEval = PositionFuncEnum.NEITHER;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[isOptional=" + isOptional + ",hasList=" + hasList + ",setEval="
        + setEval + "]";
  }
}
