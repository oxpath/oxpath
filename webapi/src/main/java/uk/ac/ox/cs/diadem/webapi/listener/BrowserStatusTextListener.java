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
package uk.ac.ox.cs.diadem.webapi.listener;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;

/**
 * This listener interface may be implemented in order to receive a {@link StatusTextEvent} notification when the status
 * text for a {@link WebBrowser} is changed.
 * 
 * @see WebBrowser#addStatusTextListener(StatusTextListener)
 * @see WebBrowser#removeStatusTextListener(StatusTextListener)
 * 
 *      * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface BrowserStatusTextListener extends BrowserListener {

  /**
   * This method is called when the status text is changed. The status text is typically displayed in the status bar of
   * a browser application.
   * <p>
   * 
   * <p>
   * The following fields in the <code>StatusTextEvent</code> apply:
   * <ul>
   * <li>(in) text the modified status text
   * <li>(in) browser the <code>WebBrowser</code> whose status text is changed
   * </ul>
   * 
   * @param event
   *          the <code>StatusTextEvent</code> that contains the updated status description of a <code>WebBrowser</code>
   * 
   */
  public void changed(StatusTextEvent event);
}