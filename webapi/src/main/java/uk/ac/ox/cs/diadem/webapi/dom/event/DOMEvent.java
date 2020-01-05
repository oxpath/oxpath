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
package uk.ac.ox.cs.diadem.webapi.dom.event;

/**
 * The DOMEvent interface is the primary datatype for allElements events in the Document
 * Object Model.
 * 
 * For more information on this interface please see
 * http://www.w3.org/TR/DOM-Level-2-Events/
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University,
 *         Department of Computer Science
 * 
 */
public interface DOMEvent {

    // // PhaseType
    // public static final short CAPTURING_PHASE = 1;
    // public static final short AT_TARGET = 2;
    // public static final short BUBBLING_PHASE = 3;
    //
    /**
     * The name of the event (case-insensitive). The name must be an XML name.
     */
    public String getType();

    /**
     * Used to indicate the EventTarget to which the event was originally
     * dispatched.
     */
    public DOMEventTarget getTarget();
    //
    // public DOMEventTarget getCurrentTarget();
    //
    // public short getEventPhase();
    //
    // public boolean getBubbles();
    //
    // public boolean getCancelable();
    //
    // public long getTimeStamp();
    //
    // public void stopPropagation();
    //
    // public void preventDefault();
    //
    // public void initEvent(String eventTypeArg,
    // boolean canBubbleArg,
    // boolean cancelableArg);
}
