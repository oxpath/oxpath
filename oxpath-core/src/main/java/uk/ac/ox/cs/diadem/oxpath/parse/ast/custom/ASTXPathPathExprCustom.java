package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.core.PositionFuncEnum;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

/**
 * @author AndrewJSel
 *
 */
public class ASTXPathPathExprCustom extends CustomSimpleNode {

  public ASTXPathPathExprCustom(final int id) {
    super(id);
  }

  public ASTXPathPathExprCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setHasSimpleList(final boolean list) {
    hasSimpleList = list;
  }

  public boolean hasSimpleList() {
    return hasSimpleList;
  }

  private boolean hasSimpleList = false;

  public void setHasComplexList(final boolean list) {
    hasComplexList = list;
  }

  public boolean hasComplexList() {
    return hasComplexList;
  }

  private boolean hasComplexList = false;

  public void setSetBasedEval(final PositionFuncEnum set) {
    setEval = set;
  }

  public PositionFuncEnum getSetBasedEval() {
    return setEval;
  }

  private PositionFuncEnum setEval = PositionFuncEnum.NEITHER;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[hasSimpleList" + hasSimpleList + ",hasComplexList=" + hasComplexList
        + ",setEval=" + setEval + "]";
  }

}
