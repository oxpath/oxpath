/**
 *
 */
package uk.ac.ox.cs.diadem.oxpath.utils;

import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;

/**
 *
 * @author AndrewJSel
 */
public interface DOMWindow {

  /**
   * Returns the top web page contained inside this web window
   *
   * @return the top web page contained inside this web window
   */
  public DOMDocument getPage();

}
