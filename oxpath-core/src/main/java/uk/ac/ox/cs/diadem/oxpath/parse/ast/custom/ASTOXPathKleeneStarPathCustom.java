package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.Node;

/**
 * @author AndrewJSel
 *
 */
public class ASTOXPathKleeneStarPathCustom extends CustomSimpleNode {

  public ASTOXPathKleeneStarPathCustom(final int id) {
    super(id);
  }

  public ASTOXPathKleeneStarPathCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setHasLowerBound(final boolean hasLower) {
    hasLowerBound = hasLower;
  }

  public void setHasUpperBound(final boolean hasUpper) {
    hasUpperBound = hasUpper;
  }

  public void setLowerBound(final int lower) {
    lowerBound = lower;
  }

  public void setUpperBound(final int upper) {
    upperBound = upper;
  }

  public void setHasFollowingPath(final boolean hasfollowing) {
    hasFollowingPath = hasfollowing;
  }

  public boolean hasLowerBound() {
    return hasLowerBound;
  }

  public boolean hasUpperBound() {
    return hasUpperBound;
  }

  public int getLowerBound() {
    return lowerBound;
  }

  public int getUpperBound() {
    return upperBound;
  }

  public boolean hasFollowingPath() {
    return hasFollowingPath;
  }

  public void setInsideKleeneStar(final Node kleene) {
    insideKleene = kleene;
  }

  public boolean isInsideKleeneStar() {
    return insideKleene != null;
  }

  /**
   * Returns the outermost Kleene star node (for closing actions) Check for {@code null} values when using this or call
   * {@code node.isInsideKleeneStar()}
   *
   * @return the outermost Kleene star node (for closing actions)
   */
  public Node insideKleeneStar() {
    return insideKleene;
  }

  private boolean hasLowerBound = false;
  private boolean hasUpperBound = false;
  private int lowerBound = 0;
  private int upperBound = Integer.MAX_VALUE;
  private boolean hasFollowingPath = false;
  private Node insideKleene = null;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" + "Lower=" + lowerBound + ",Upper=" + upperBound
        + ",hasFollowingPath=" + hasFollowingPath + ",outerKleene="
        + ((isInsideKleeneStar()) ? insideKleene.toString() : "null") + "]";
  }

}
