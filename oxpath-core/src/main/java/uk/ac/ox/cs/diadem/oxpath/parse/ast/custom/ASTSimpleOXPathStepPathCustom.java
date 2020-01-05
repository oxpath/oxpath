package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import java.util.Set;

import com.google.common.collect.Sets;

import uk.ac.ox.cs.diadem.oxpath.core.PositionFuncEnum;
import uk.ac.ox.cs.diadem.oxpath.model.language.AggregatedStep;
import uk.ac.ox.cs.diadem.oxpath.model.language.MyLocationPath;
import uk.ac.ox.cs.diadem.oxpath.model.language.Step;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

public class ASTSimpleOXPathStepPathCustom extends CustomSimpleNode {

  public ASTSimpleOXPathStepPathCustom(final int id) {
    super(id);
  }

  public ASTSimpleOXPathStepPathCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setStep(final Step iStep) {
    step = iStep;
  }

  public void setLocationPath(final MyLocationPath path) {
    this.path = path;
  }

  public void setIsPlainXPath(final boolean isPlain) {
    isPlainXPath = isPlain;
  }

  public void putContextualAttribute(final String xpath) {
    if (SELF_NODE.equals(xpath))
      return;
    if (contextualAttributes == null) {
      contextualAttributes = Sets.newLinkedHashSet();
    }

    contextualAttributes.add(xpath);
  }

  public Step getStep() {
    if (step != null)
      return step;
    return path.getFirstStep();
  }

  public MyLocationPath getLocationPath() {
    return path;
  }

  public void setHasList(final boolean iHasList) {
    hasList = iHasList;
  }

  public boolean hasList() {
    return hasList;
  }

  public boolean isPlainXPath() {
    return isPlainXPath;
  }

  public boolean hasContextualAttributes() {
    return contextualAttributes != null;
  }

  public Set<String> getContextualAttributes() {
    return contextualAttributes;
  }

  public boolean isSelfNode() {
    return isPlainXPath && SELF_NODE.equals(path.toOXPath());
  }

  // public void setHasTail(boolean iHasTail) {
  // this.hasTail = iHasTail;
  // }
  //
  // public boolean hasTail() {
  // return this.hasTail;
  // }

  public void setSetBasedEval(final PositionFuncEnum set) {
    setEval = set;
  }

  public PositionFuncEnum getSetBasedEval() {
    return setEval;
  }

  private Set<String> contextualAttributes = null;
  private Step step;
  private MyLocationPath path;
  private boolean hasList = false;
  private boolean isPlainXPath = true;
  private PositionFuncEnum setEval = PositionFuncEnum.NEITHER;
  private final String SELF_NODE = "self::node()";

  @Override
  public String toString() {
    String stepTxt = "";
    if (path != null) {
      stepTxt = path.toOXPath();
    } else {
      stepTxt = step.getAxis().getValue() + step.getNodeTest().getValue();
    }
    if (step instanceof AggregatedStep) {
      stepTxt = ((AggregatedStep) step).getPathExpression();
    }
    return this.getClass().getSimpleName() + "[step=" + stepTxt + ",hasList=" + hasList + ",setEval=" + setEval + "]";
  }

}
