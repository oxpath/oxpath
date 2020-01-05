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
package uk.ac.ox.cs.diadem.webapi;

import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;

/**
 * It represents service that allows handling browser dialogs such as JavaScript alert, confirmation, prompt, error
 * dialogs and other. Note that, in allElements cases, the parent window parameter can be null.
 *
 * (Wrapping interface for org.mozilla.interfaces.nsIPromptService)
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University Department of Computer Science
 */
public interface DialogsService {

  /**
   * Puts up an alert dialog with an OK button.
   * 
   * @param aParent
   *          The parent window or null.
   * @param aDialogTitle
   *          Text to appear in the title of the dialog.
   * @param aText
   *          Text to appear in the body of the dialog.
   */
  void alert(DOMWindow aParent, String aDialogTitle, String aText);

  /**
   * Puts up an alert dialog with an OK button and a labeled checkbox.
   * 
   * @param aParent
   *          The parent window or null.
   * @param aDialogTitle
   *          Text to appear in the title of the dialog.
   * @param aText
   *          Text to appear in the body of the dialog.
   * @param aCheckMsg
   *          Text to appear with the checkbox.
   * @param aCheckState
   *          Contains the initial checked state of the checkbox when this method is called and the final checked state
   *          after this method returns.
   */
  void alertCheck(DOMWindow aParent, String aDialogTitle, String aText, String aCheckMsg, boolean[] aCheckState);

  /**
   * Puts up a dialog with OK and Cancel buttons.
   * 
   * @param aParent
   *          The parent window or null.
   * @param aDialogTitle
   *          Text to appear in the title of the dialog.
   * @param aText
   *          Text to appear in the body of the dialog.
   * 
   * @return true for OK, false for Cancel
   */
  boolean confirm(DOMWindow aParent, String aDialogTitle, String aText);

  /**
   * Puts up a dialog with OK and Cancel buttons and a labeled checkbox.
   * 
   * @param aParent
   *          The parent window or null.
   * @param aDialogTitle
   *          Text to appear in the title of the dialog.
   * @param aText
   *          Text to appear in the body of the dialog.
   * @param aCheckMsg
   *          Text to appear with the checkbox.
   * @param aCheckState
   *          Contains the initial checked state of the checkbox when this method is called and the final checked state
   *          after this method returns.
   * 
   * @return true for OK, false for Cancel
   */
  boolean confirmCheck(DOMWindow aParent, String aDialogTitle, String aText, String aCheckMsg, boolean[] aCheckState);

  /**
   * Puts up a dialog with up to 3 buttons and an optional, labeled checkbox.
   * 
   * @param aParent
   *          The parent window or null.
   * @param aDialogTitle
   *          Text to appear in the title of the dialog.
   * @param aText
   *          Text to appear in the body of the dialog.
   * @param aButtonFlags
   *          A combination of Button Flags.
   * @param aButton0Title
   *          Used when button 0 uses TITLE_IS_STRING
   * @param aButton1Title
   *          Used when button 1 uses TITLE_IS_STRING
   * @param aButton2Title
   *          Used when button 2 uses TITLE_IS_STRING
   * @param aCheckMsg
   *          Text to appear with the checkbox. Null if no checkbox.
   * @param aCheckState
   *          Contains the initial checked state of the checkbox when this method is called and the final checked state
   *          after this method returns.
   * 
   * @return index of the button pressed.
   * 
   *         Buttons are numbered 0 - 2. The implementation can decide whether the sequence goes from right to left or
   *         left to right. Button 0 is the default button unless one of the Button Default Flags is specified.
   * 
   *         A button may use a predefined title, specified by one of the Button Title Flags values. Each title value
   *         can be multiplied by a position value to assign the title to a particular button. If BUTTON_TITLE_IS_STRING
   *         is used for a button, the string parameter for that button will be used. If the value for a button position
   *         is zero, the button will not be shown.
   * 
   *         In general, aButtonFlags is constructed per the following example:
   * 
   *         aButtonFlags = (BUTTON_POS_0) * (BUTTON_TITLE_AAA) + (BUTTON_POS_1) * (BUTTON_TITLE_BBB) +
   *         BUTTON_POS_1_DEFAULT;
   * 
   *         where "AAA" and "BBB" correspond to one of the button titles.
   */
  int confirmEx(DOMWindow aParent, String aDialogTitle, String aText, long aButtonFlags, String aButton0Title,
      String aButton1Title, String aButton2Title, String aCheckMsg, boolean[] aCheckState);

  /**
   * Puts up a dialog with an edit field and an optional, labeled checkbox.
   * 
   * @param aParent
   *          The parent window or null.
   * @param aDialogTitle
   *          Text to appear in the title of the dialog.
   * @param aText
   *          Text to appear in the body of the dialog.
   * @param aValue
   *          Contains the default value for the dialog field when this method is called (null value is ok). Upon
   *          return, if the user pressed OK, then this parameter contains a newly allocated string value. Otherwise,
   *          the parameter's value is unmodified.
   * @param aCheckMsg
   *          Text to appear with the checkbox. If null, check box will not be shown.
   * @param aCheckState
   *          Contains the initial checked state of the checkbox when this method is called and the final checked state
   *          after this method returns.
   * 
   * @return true for OK, false for Cancel.
   */
  boolean prompt(DOMWindow aParent, String aDialogTitle, String aText, String[] aValue, String aCheckMsg,
      boolean[] aCheckState);

  /**
   * Puts up a dialog with an edit field, a password field, and an optional, labeled checkbox.
   * 
   * @param aParent
   *          The parent window or null.
   * @param aDialogTitle
   *          Text to appear in the title of the dialog.
   * @param aText
   *          Text to appear in the body of the dialog.
   * @param aUsername
   *          Contains the default value for the username field when this method is called (null value is ok). Upon
   *          return, if the user pressed OK, then this parameter contains a newly allocated string value. Otherwise,
   *          the parameter's value is unmodified.
   * @param aPassword
   *          Contains the default value for the password field when this method is called (null value is ok). Upon
   *          return, if the user pressed OK, then this parameter contains a newly allocated string value. Otherwise,
   *          the parameter's value is unmodified.
   * @param aCheckMsg
   *          Text to appear with the checkbox. If null, check box will not be shown.
   * @param aCheckState
   *          Contains the initial checked state of the checkbox when this method is called and the final checked state
   *          after this method returns.
   * 
   * @return true for OK, false for Cancel.
   */
  boolean promptUsernameAndPassword(DOMWindow aParent, String aDialogTitle, String aText, String[] aUsername,
      String[] aPassword, String aCheckMsg, boolean[] aCheckState);

  /**
   * Puts up a dialog with a password field and an optional, labeled checkbox.
   * 
   * @param aParent
   *          The parent window or null.
   * @param aDialogTitle
   *          Text to appear in the title of the dialog.
   * @param aText
   *          Text to appear in the body of the dialog.
   * @param aPassword
   *          Contains the default value for the password field when this method is called (null value is ok). Upon
   *          return, if the user pressed OK, then this parameter contains a newly allocated string value. Otherwise,
   *          the parameter's value is unmodified.
   * @param aCheckMsg
   *          Text to appear with the checkbox. If null, check box will not be shown.
   * @param aCheckState
   *          Contains the initial checked state of the checkbox when this method is called and the final checked state
   *          after this method returns.
   * 
   * @return true for OK, false for Cancel.
   */
  boolean promptPassword(DOMWindow aParent, String aDialogTitle, String aText, String[] aPassword, String aCheckMsg,
      boolean[] aCheckState);

  /**
   * Puts up a dialog box which has a list box of strings from which the user may make a single selection.
   * 
   * @param aParent
   *          The parent window or null.
   * @param aDialogTitle
   *          Text to appear in the title of the dialog.
   * @param aText
   *          Text to appear in the body of the dialog.
   * @param aCount
   *          The length of the aSelectList array parameter.
   * @param aSelectList
   *          The list of strings to display.
   * @param aOutSelection
   *          Contains the index of the selected item in the list when this method returns true.
   * 
   * @return true for OK, false for Cancel.
   */
  boolean select(DOMWindow aParent, String aDialogTitle, String aText, long aCount, String[] aSelectList,
      int[] aOutSelection);
}