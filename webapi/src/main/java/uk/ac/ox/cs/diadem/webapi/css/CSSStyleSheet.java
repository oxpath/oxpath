/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.css;

import java.util.List;

/**
 * Partial implementation of http://www.w3.org/TR/DOM-Level-2-Style/css.html#CSS-CSSStyleSheet
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface CSSStyleSheet {
  /**
   * 
   * @return
   */
  boolean isDisabled();

  /**
   * Enables the stylesheet
   */
  void enable();

  /**
   * Disables the stylesheet
   */
  void disable();

  /**
   * All rules of tihs stylesheet
   * 
   * @return
   */
  List<CSSStyleRule> cssRules();

  /**
   * Append (e.g., in last position) a new css rule by its text. This will trigger all references to
   * {@link CSSStyleRule} to be stale
   * 
   * @param cssText
   */
  void appendRule(String cssText);

  /**
   * Removes the rule in the given position.This will trigger all references to {@link CSSStyleRule} to be stale,
   * 
   * @param position
   */
  void deleteRule(int position);

  /**
   * 
   * @param rule
   */
  void deleteRule(CSSStyleRule rule);

  /**
   * Filters rules by selectorText (e.g., '.myClass')
   * 
   * @param selectorText
   * @return
   */
  List<CSSStyleRule> findRulesBySelectorText(String selectorText);
}
