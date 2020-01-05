/**
 *
 */
package uk.ac.ox.cs.diadem.oxpath.model.language;

import java.util.List;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.Node;
import uk.ac.ox.cs.diadem.oxpath.utils.PrintVisitor;

/**
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
public class MyLocationPath {

  List<StepOrPredicate> steps = Lists.newArrayList();
  private boolean iHasList;

  public void appendStep(final Step step) {
    steps.add(new StepWrapper(step));
  }

  public Step getFirstStep() {
    for (final StepOrPredicate s : steps) {
      if (s.isStep())
        return s.getStep();
    }
    throw new RuntimeException("Cannot find step in " + toString());
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < steps.size(); i++) {
      final StepOrPredicate obj = steps.get(i);
      if ((i > 0) && (obj.isStep())) {
        buffer.append('/');
      }
      buffer.append(obj);
    }
    return buffer.toString();
  }

  public String toOXPath() {
    return toString();
  }

  public void setHasList(final boolean iHasList) {
    this.iHasList = iHasList;
  }

  public boolean hasList() {
    return iHasList;
  }

  public void addPredicate(final Node predicate) {
    String predicateText;
    try {
      predicateText = "[" + new PrintVisitor(false, true).accept(predicate, null) + "]";
      steps.add(new PredicateWrapper(predicateText));
    } catch (final OXPathException e) {
      throw new RuntimeException(e);
    }
  }

  private static interface StepOrPredicate {
    boolean isStep();

    Step getStep();

    boolean isPredicate();
  }

  private static class PredicateWrapper implements StepOrPredicate {
    private final String text;

    public PredicateWrapper(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }

    @Override
    public boolean isStep() {
      return false;
    }

    @Override
    public boolean isPredicate() {
      return true;
    }

    @Override
    public Step getStep() {
      return null;
    }
  }

  private static class StepWrapper implements StepOrPredicate {
    private final Step step;

    public StepWrapper(final Step step) {
      this.step = step;
    }

    @Override
    public Step getStep() {
      return step;
    }

    @Override
    public String toString() {
      return step.toOXPath();
    }

    @Override
    public boolean isStep() {
      return true;
    }

    @Override
    public boolean isPredicate() {
      return false;
    }

  }

  public void addSelectorPredicate(final SelectorPredicate selPred) {
    steps.add(new PredicateWrapper(selPred.toXPath()));

  }
}
