package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.model.language.Action;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.Node;

public class ASTOXPathActionPathCustom extends CustomSimpleNode {

  public ASTOXPathActionPathCustom(final int id) {
    super(id);
  }

  public ASTOXPathActionPathCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  public void setAction(final Action act) {
    action = act;
  }

  public Action getAction() {
    return action;
  }

  public void setHasTail(final boolean ht) {
    hasTail = ht;
  }

  public boolean hasTail() {
    return hasTail;
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

  private Action action;
  private boolean hasTail = false;
  private Node insideKleene = null;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[action:" + getAction().getValue() + ", type="
        + getAction().getActionType() + "," + "hasTail=" + hasTail() + ", insideKleene="
        + ((insideKleene != null) ? insideKleene.toString() : "null") + "]";
  }
}
