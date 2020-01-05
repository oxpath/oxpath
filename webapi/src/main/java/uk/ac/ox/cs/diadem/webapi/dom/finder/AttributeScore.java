/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.finder;

/**
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class AttributeScore {

  private final String elementName;
  private final String attributeName;
  private final int score;

  AttributeScore(final String elementName, final String attributeName, final int score) {
    this.elementName = elementName;
    this.attributeName = attributeName;
    this.score = score;
  }

  /**
   * @return the elementName
   */
  public String getElementName() {
    return elementName;
  }

  /**
   * @return the attributeName
   */
  public String getAttributeName() {
    return attributeName;
  }

  /**
   * @return the score
   */
  public int getScore() {
    return score;
  }

  @Override
  public String toString() {
    return "[" + elementName + " -- " + attributeName + " -- " + score + "]";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + ((attributeName == null) ? 0 : attributeName.hashCode());
    result = (prime * result) + ((elementName == null) ? 0 : elementName.hashCode());
    result = (prime * result) + score;
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final AttributeScore other = (AttributeScore) obj;
    if (attributeName == null) {
      if (other.attributeName != null)
        return false;
    } else if (!attributeName.equals(other.attributeName))
      return false;
    if (elementName == null) {
      if (other.elementName != null)
        return false;
    } else if (!elementName.equals(other.elementName))
      return false;
    if (score != other.score)
      return false;
    return true;
  }

}
