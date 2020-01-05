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
 * A <code>StatusTextEvent</code> is sent by a {@link WebBrowser} to
 * {@link BrowserStatusTextListener}'s when the status text is changed. The
 * status text is typically displayed in the status bar of a browser
 * application.
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University,
 *         Department of Computer Science
 */
public class StatusTextEvent {

    /** status text */
    public String text;
    public WebBrowser browser;

    /**
     * Constructs a new instance of this class.
     * 
     * @param widget
     *            the widget that fired the event
     * 
     * @since 3.5
     */
    public StatusTextEvent(final WebBrowser browser) {

        this.browser = browser;
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a string representation of the event
     */
    @Override
    public String toString() {

        return getName();
    }

    /**
     * Returns the name of the event. This is the name of the class without the
     * package name.
     * 
     * @return the name of the event
     */
    String getName() {

        final String string = getClass().getName();
        final int index = string.lastIndexOf('.');
        if (index == -1) {
            return string;
        }
        return string.substring(index + 1, string.length());
    }
}