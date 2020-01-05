/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom;

/**
 * For more information on this interface please see http://www.w3.org/TR/DOM-Level-2-HTML/
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface DOMHTMLInputElement extends DOMElement, DOMTypeableElement {

  String getDefaultValue();

  void setDefaultValue(String aDefaultValue);

  boolean getDefaultChecked();

  void setDefaultChecked(boolean aDefaultChecked);

  // nsIDOMHTMLFormElement getForm();

  String getAccept();

  void setAccept(String aAccept);

  String getAccessKey();

  void setAccessKey(String aAccessKey);

  String getAlign();

  void setAlign(String aAlign);

  String getAlt();

  void setAlt(String aAlt);

  boolean getChecked();

  void setChecked(boolean aChecked);

  boolean getDisabled();

  void setDisabled(boolean aDisabled);

  int getMaxLength();

  void setMaxLength(int aMaxLength);

  String getName();

  void setName(String aName);

  boolean getReadOnly();

  void setReadOnly(boolean aReadOnly);

  long getSize();

  void setSize(long aSize);

  String getSrc();

  void setSrc(String aSrc);

  int getTabIndex();

  void setTabIndex(int aTabIndex);

  String getType();

  void setType(String aType);

  String getUseMap();

  void setUseMap(String aUseMap);

  String getValue();

  void setValue(String aValue);

  void blur();

  void select();

}
