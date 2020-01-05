package uk.ac.ox.cs.diadem.webapi.dom;

public interface DOmElementOnJS extends DOMElement {

  /**
 *
 */
  DOMWindow clickJS();

  /**
   * Checks if the provided nodes are all descendant of the current element.
   * 
   * @param xpathLocators
   *          the xpath for each node to check
   * @return
   */
  boolean isAncestorOf(String... xpathLocators);
}
