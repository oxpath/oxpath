/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom;

/**
 * Partially implemented. For more information on this interface please see
 * https://developer.mozilla.org/en-US/docs/DOM/HTMLTextAreaElement
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface DOMHTMLTextAreaElement extends DOMTypeableElement {

  String getDefaultValue();

  // DOMHTMLFormElement getForm();

  boolean getDisabled();

  @Override
  void select();

  @Override
  String getValue();

  @Override
  void setValue(String aValue);

  boolean willValidate();

}
