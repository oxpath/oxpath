/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface DOMTypeableElement extends DOMElement {

  String getValue();

  @Override
  public DOMWindow type(String content);

  void setValue(String aValue);

  void select();

  /**
   * Internal use
   * 
   * @return
   */
  Object getWrappedElement();

}
