package uk.ac.ox.cs.diadem.oxpath.parse.ast.custom;

import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;

/**
 * @author AndrewJSel
 *
 */
public class ASTRelativeOXPathLocationPathCustom extends CustomSimpleNode {

  public ASTRelativeOXPathLocationPathCustom(final int id) {
    super(id);
  }

  public ASTRelativeOXPathLocationPathCustom(final OXPathParser p, final int id) {
    super(p, id);
  }

  /**
   * Sets if the expression has a simple path
   *
   * @param hasPath
   *          {@code true} for a simple path, {@code false} otherwise
   */
  public void setHasSimplePath(final boolean hasPath) {
    hasSimplePath = hasPath;
  }

  /**
   * Returns if the expression has a simple path
   *
   * @return {@code true} for a simple path, {@code false} otherwise
   */
  public boolean hasSimplePath() {
    return hasSimplePath;
  }

  /**
   * Sets if the expression has a complex path
   *
   * @param hasPath
   *          {@code true} for a complex path, {@code false} otherwise
   */
  public void setHasComplexPath(final boolean hasPath) {
    hasComplexPath = hasPath;
  }

  /**
   * Returns if the expression has a complex path
   *
   * @return {@code true} for a complex path, {@code false} otherwise
   */
  public boolean hasComplexPath() {
    return hasComplexPath;
  }

  /**
   * Sets if the expression is an absolute path
   *
   * @param absPath
   *          {@code true} is an absolute path, {@code false} otherwise
   */
  public void setIsAbsolutePath(final boolean absPath) {
    isAbsolutePath = absPath;
  }

  /**
   * Returns if the expression is an absolute path
   *
   * @return {@code true} is an absolute path, {@code false} otherwise
   */
  public boolean isAbsolutePath() {
    return isAbsolutePath;
  }

  /**
   * instance field encoding if a simple path is part of this node
   */
  private boolean hasSimplePath = false;
  /**
   * instance field encoding if a complex path is part of this node
   */
  private boolean hasComplexPath = false;
  /**
   * instance field encoding if the path is absolute
   */
  private boolean isAbsolutePath = false;

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[hasSimplePath=" + hasSimplePath + ",hasComplexPath=" + hasComplexPath
        + ",isAbsolutePath=" + isAbsolutePath + "]";
  }

}
