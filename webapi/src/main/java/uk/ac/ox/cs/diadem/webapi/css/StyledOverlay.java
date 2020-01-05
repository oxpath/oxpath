package uk.ac.ox.cs.diadem.webapi.css;

import java.util.Collection;

import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;

/**
 * 
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface StyledOverlay {

  /**
   * Returns the root(s) node(s) to navigate the tree
   * 
   * @return
   */
  Collection<StyledNode> roots();

  /**
   * the {@link CSSStyleSheet} to control the style of this overlay
   * 
   * @return
   */
  CSSStyleSheet getCSSStyleSheet();

  /**
   * Applies the overlay on the underlying {@link DOMWindow}. This modifies the DOM as it creates nodes and stylesheets
   * to display the overlay
   */
  void attach();

  /**
   * It removes all the nodes created and restores the original DOM
   */
  void detach();

}
