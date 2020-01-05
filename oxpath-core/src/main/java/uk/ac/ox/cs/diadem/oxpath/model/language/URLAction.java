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
/**
 * Package containing supporting classes, derived from the OXPath model (which itself extends the XPath model).
 * This subpackage includes classes and interface relating to the OXPath language.
 */
package uk.ac.ox.cs.diadem.oxpath.model.language;

/**
 * class encoding url actions in OXPath
 * 
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class URLAction extends AbstractAction {

  /**
   * basic constructor
   * 
   * @param iURL
   *          {@code String} encoding of the url
   */
  public URLAction(final String iURL) {
    url = iURL;
    setIsAbsoluteAction(true);
  }

  /**
   * returns the {@code String} value of the url
   * 
   * @return the {@code String} value of the url
   */
  @Override
  public Object getValue() {
    return url;
  }

  /**
   * returns the type of action
   * 
   * @return the type of action
   */
  @Override
  public ActionType getActionType() {
    return ActionType.URL;
  }

  /**
   * returns {@code true} if the action is absolute, {@code false} otherwise
   * 
   * @return {@code true} if the action is absolute, {@code false} otherwise
   */
  @Override
  public boolean isAbsoluteAction() {
    return true; // all url actions are absolute
  }

  private final String url;
}
