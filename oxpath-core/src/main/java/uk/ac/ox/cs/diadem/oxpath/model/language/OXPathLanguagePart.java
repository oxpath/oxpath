package uk.ac.ox.cs.diadem.oxpath.model.language;

/**
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
public interface OXPathLanguagePart {

  public String toOXPathString();

  public boolean hasSimplePath();

  public boolean hasComplexPath();

  public void setHasSimplePath(boolean b);

  public void setHasComplexPath(boolean b);

}
