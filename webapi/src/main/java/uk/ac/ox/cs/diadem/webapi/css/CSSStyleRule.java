/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.css;

import uk.ac.ox.cs.diadem.webapi.dom.DOMCSS2Properties.CssProperty;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 * 
 * A partial and adapted implementation of http://www.w3.org/TR/DOM-Level-2-Style/css.html#CSS-CSSStyleRule
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface CSSStyleRule {
  /**
   * For instance, for a rule "div > p { color:red;}" it returns "div > p" {@link WebAPIRuntimeException}
   * 
   * @return
   * @throws a
   *           {@link WebAPIRuntimeException} if the current rule is stale
   */
  String getSelectorText();

  /**
   * Returns the property value for the current rule. For instance, for a rule "div > p { color:red;}" it returns 'red'
   * for the property 'color'
   * 
   * @param property
   * @return
   * @throws a
   *           {@link WebAPIRuntimeException} if the current rule is stale
   */
  String getPropertyValue(CssProperty property);

  /**
   * 
   * @param property
   * @return the value of the removed property
   * @throws a
   *           {@link WebAPIRuntimeException} if the current rule is stale
   */
  String removeProperty(CssProperty property);

  /**
   * set the new value for the given property
   * 
   * @param property
   * @param value
   * @throws a
   *           {@link WebAPIRuntimeException} if the current rule is stale
   */
  void setProperty(CssProperty property, String value);

  /**
   * Checks is this pointer is stale (e.g., its position in the list of rules is not correct due to possible
   * addition/removal of rules. If it's stale, a fresh list of rules must be obtained from {@link CSSStyleSheet}
   * 
   * @return true is the object is stale, false otherwise
   */
  boolean isStale();

}
