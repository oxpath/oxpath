/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.finder;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;

/**
 * Addresses a {@link DOMNode} via xpath. the expression is associated to a score that measures the robustness of the
 * expression, as defined by our framework.
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface XPathNodePointer extends Comparable<XPathNodePointer> {
  enum Type {
    CANONICAL, ATTRIBUTE, POSITION, TEXT, ANCHOR, GENERALIZER
  }

  /**
   * Returns the xpath expression associated to the node
   * 
   * @return the xpath expression associated to the node
   */
  String getXPath();

  /**
   * Return the robustness score associated to the expression
   * 
   * @return the robustness score associated to the expression
   */
  Integer getScore();

  /**
   * Return the expression's {@link Type}, namely which method is used to compute the address
   * 
   * @return the expression's {@link Type}, namely which method is used to compute the address
   */
  Type getType();

  /**
   * Creates a new XPathNodePointer by concatenating this and other via '/'.
   * 
   * @param other
   * @return
   */
  XPathNodePointer concatChild(XPathNodePointer other);

  /**
   * Creates a new XPathNodePointer by concatenating this and other via '//'.
   * 
   * @param other
   * @return
   */
  XPathNodePointer concatDescendantOrSelf(XPathNodePointer other);
}
