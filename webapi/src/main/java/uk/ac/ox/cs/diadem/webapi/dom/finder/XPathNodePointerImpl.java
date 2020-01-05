/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.finder;

/**
 * TODO CS: Do we need this separation into implementation and interface?
 * 
 * TODO rename into ScoredOXPath
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class XPathNodePointerImpl implements XPathNodePointer {

  private final String xpath;
  private final Integer score; // TODO score be null?
  private final Type type;

  public XPathNodePointerImpl(final String xpath, final Integer score, final Type type) {
    this.xpath = xpath;
    this.score = score;
    this.type = type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getXPath() {
    return xpath;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer getScore() {
    return score;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return "[ " + xpath + " -- " + type + " -- " + score + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((score == null) ? 0 : score.hashCode());
    result = (prime * result) + ((type == null) ? 0 : type.hashCode());
    result = (prime * result) + ((xpath == null) ? 0 : xpath.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    // TODO class invariant should guarantee that score is non-null; BTW, why is it boxed?
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final XPathNodePointerImpl other = (XPathNodePointerImpl) obj;
    if (score == null) {
      if (other.score != null)
        return false;
    } else if (!score.equals(other.score))
      return false;
    if (type != other.type)
      return false;
    if (xpath == null) {
      if (other.xpath != null)
        return false;
    } else if (!xpath.equals(other.xpath))
      return false;
    return true;
  }

  @Override
  public int compareTo(final XPathNodePointer o) {
    int result = o.getScore() - score;
    if (result == 0) {
      result = xpath.compareTo(o.getXPath());
      if (result == 0)
        return type.compareTo(o.getType());
      return result;
    } else
      return result;
  }

  @Override
  public XPathNodePointer concatChild(XPathNodePointer other) {
    final String resultXPath;
    if (xpath.endsWith("/"))
      resultXPath = xpath + other.getXPath();
    else
      resultXPath = xpath + "/" + other.getXPath();

    return new XPathNodePointerImpl(resultXPath, ScoreDefinitions.getConcatChildScore(getScore(), other.getScore()),
        Type.GENERALIZER);
  }

  @Override
  public XPathNodePointer concatDescendantOrSelf(XPathNodePointer other) {
    final String resultXPath;
    if (xpath.endsWith("/"))
      resultXPath = xpath + other.getXPath();
    else
      resultXPath = xpath + "//" + other.getXPath();

    return new XPathNodePointerImpl(resultXPath, ScoreDefinitions.getConcatDescendentOrSelfScore(getScore(),
        other.getScore()), Type.GENERALIZER);
  }

}
