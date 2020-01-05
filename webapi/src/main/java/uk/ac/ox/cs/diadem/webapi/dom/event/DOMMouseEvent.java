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
 * 
 * http://www.w3.org/TR/DOM-Level-2-Events/events.html#Events-MouseEvent
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University,
 *         Department of Computer Science
 */
public interface DOMMouseEvent extends DOMUIEvent {

    // enum Kind {
    // click, dblclick, mouseover, mousedown, mouseup, mousemove, mouseout,
    // mouseenter, contextmenu, focus, blur;
    // }
    static String EVENT_TYPE = "MouseEvents";
    static String click = "click";
    static String dblclick = "dblclick";
    static String mouseover = "mouseover";
    static String mousedown = "mousedown";
    static String mouseup = "mouseup";
    static String mousemove = "mousemove";
    static String mouseout = "mouseout";
    static String mouseenter = "mouseenter";
    static String contextmenu = "contextmenu";
    //
    static String mousewheel = "DOMMouseScroll";

    public int getScreenX();

    //
    public int getScreenY();

    //
    public int getClientX();

    //
    public int getClientY();

    //
    public boolean getCtrlKey();

    //
    public boolean getShiftKey();

    //
    public boolean getAltKey();

    //
    public boolean getMetaKey();

    //
    /**
     * The button number that was pressed when the mouse event was fired: Left
     * button=0, middle button=1 (if present), right button=2. For mice
     * configured for left handed use in which the button actions are reversed
     * the values are instead read from right to left.
     */
    public short getButton();
    //
    // public DOMEventTarget getRelatedTarget();
    //
    // public void initMouseEvent(String typeArg,
    // boolean canBubbleArg,
    // boolean cancelableArg,
    // AbstractView viewArg,
    // int detailArg,
    // int screenXArg,
    // int screenYArg,
    // int clientXArg,
    // int clientYArg,
    // boolean ctrlKeyArg,
    // boolean altKeyArg,
    // boolean shiftKeyArg,
    // boolean metaKeyArg,
    // short buttonArg,
    // DOMEventTarget relatedTargetArg);
}