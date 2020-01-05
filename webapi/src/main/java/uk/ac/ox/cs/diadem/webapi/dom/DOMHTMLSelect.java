/*
 * Copyright (c)2011, DIADEM Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the DIADEM team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DIADEM Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.ox.cs.diadem.webapi.dom;

/**
 * 
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface DOMHTMLSelect extends DOMElement {

  /**
   * Simulates a real selection event (click on the select and clik on the option). It is a shortcut for clicking on
   * this element and then on the option to select. It waits until the document at the new location (if any), is fully
   * loaded. If the event triggers opening a new window, it is returned as result, otherwise the current window is
   * returned
   * 
   * @param index
   *          the index position
   * @return If a window has been opened due to the event, the new window is returned, otherwise the current window is
   *         returned (which may have a new location).
   */
  DOMWindow selectByClick(int index);

  /**
   * Simulates a real selection event (click on the select and clik on the option). It is a shortcut for clicking on
   * this element and then on the option to select. It waits until the document at the new location (if any), is fully
   * loaded. If the event triggers opening a new window, it is returned as result, otherwise the current window is
   * returned
   * 
   * @param index
   *          the option displayed text
   * @return If a window has been opened due to the event, the new window is returned, otherwise the current window is
   *         returned (which may have a new location).
   */
  DOMWindow selectByClick(String optiontext);

  /**
   * 
   * 
   * @return The form control's type. When multiple is true, it returns select-multiple; otherwise, it returns
   *         select-one.
   */
  String getType();

  int getSelectedIndex();

  /**
   * 
   * @param optiontext
   * @deprecated use {@link #selectOptionByText(String)}
   */
  @Deprecated
  void setSelectedByText(String optiontext);

  /**
   * 
   * @param aSelectedIndex
   * @deprecated use {@link #selectOptionIndex(int)}
   */
  @Deprecated
  void setSelectedIndex(int aSelectedIndex);

  String getValue();

  void setValue(String aValue);

  long getLength();

  // void setLength(long aLength);
  // nsIDOMHTMLFormElement getForm();
  DOMHTMLOptionsCollection getOptions();

  boolean getDisabled();

  // void setDisabled(boolean aDisabled);
  boolean getMultiple();

  // void setMultiple(boolean aMultiple);
  String getName();

  void setName(String aName);

  int getSize();

  void setSize(int aSize);

  int getTabIndex();

  void setTabIndex(int aTabIndex);

  void selectAllOptions();

  DOMWindow selectOptionIndex(int aSelectedIndex);

  DOMWindow selectOptionByText(String text);
}
