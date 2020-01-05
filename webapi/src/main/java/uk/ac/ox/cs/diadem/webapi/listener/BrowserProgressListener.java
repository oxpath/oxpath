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
 * 
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University,
 *         Department of Computer Science
 */
public interface BrowserProgressListener extends BrowserListener {

    /**
     * This method is called when a progress is made during the loading of the
     * current location.
     * <p>
     * 
     * <p>
     * The following fields in the <code>ProgressEvent</code> apply:
     * <ul>
     * <li>(in) current the progress for the location currently being loaded
     * <li>(in) total the maximum progress for the location currently being
     * loaded
     * <li>(in) widget the <code>Browser</code> whose current URL is being
     * loaded
     * </ul>
     * 
     * @param event
     *            the <code>ProgressEvent</code> related to the loading of the
     *            current location of a {@link WebBrowser}
     * 
     * 
     */
    public void changed(ProgressEvent event);

    /**
     * This method is called when the current location has been completely
     * loaded.
     * <p>
     * 
     * <p>
     * The following fields in the <code>ProgressEvent</code> apply:
     * <ul>
     * <li>(in) widget the <code>WebBrowser</code> whose current URL has been
     * loaded
     * </ul>
     * 
     * @param event
     *            the <code>ProgressEvent</code> related to the
     *            <code>Browser</code> that has loaded its current URL.
     * 
     * 
     */
    public void completed(ProgressEvent event);
}