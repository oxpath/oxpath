package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.core.PositionFuncEnum;
import uk.ac.ox.cs.diadem.oxpath.model.language.SelectorPredicate;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

/**
 * @author AndrewJSel
 *
 */
public class ASTOXPathNodeTestOpCustom extends CustomSimpleNode {

  public ASTOXPathNodeTestOpCustom(final int id) {
    super(id);
  }

  public ASTOXPathNodeTestOpCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setSelectorPredicate(final SelectorPredicate select) {
    selector = select;
  }

  public SelectorPredicate getSelectorPredicate() {
    return selector;
  }

  public void setHasList(final boolean hl) {
    hasList = hl;
  }

  public boolean hasList() {
    return hasList;
  }

  private SelectorPredicate selector;
  private boolean hasList;

  public void setSetBasedEval(final PositionFuncEnum set) {
    setEval = set;
  }

  public PositionFuncEnum getSetBasedEval() {
    return setEval;
  }

  private PositionFuncEnum setEval = PositionFuncEnum.NEITHER;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[selector=" + selector.getValue() + ",hasList=" + hasList + ",setEval="
        + setEval + "]";
  }

}
