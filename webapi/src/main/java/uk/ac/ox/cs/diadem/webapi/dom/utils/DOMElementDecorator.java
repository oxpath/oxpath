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
package uk.ac.ox.cs.diadem.webapi.dom.utils;

import uk.ac.ox.cs.diadem.webapi.dom.DOMBoundingClientRect;
import uk.ac.ox.cs.diadem.webapi.dom.DOMCSSStyleDeclaration;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNodeList;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
import uk.ac.ox.cs.diadem.webapi.dom.HTMLUtil;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMKeyboardEvent;

/**
 * @author Giorgio Orsi <giorgio dot orsi at cs dot ox dot ac dot uk> Oxford
 *         University, Department of Computer Science.
 * 
 */
public abstract class DOMElementDecorator extends DOMNodeDecorator implements DOMElement {

    protected DOMElement decoratedElement;

    /**
     * Constructs a new decorator for the DOMElement given as input
     * 
     * @param element
     */
    public DOMElementDecorator(final DOMElement element) {

        super(element);
        decoratedElement = element;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ox.cs.diadem.webapi.dom.DOMNodeSelector#querySelector(java.lang
     * .String)
     */
    @Override
    public DOMElement querySelector(final String selectors) {

        return (decoratedElement.querySelector(selectors));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ox.cs.diadem.webapi.dom.DOMNodeSelector#querySelectorAll(java.lang
     * .String)
     */
    @Override
    public DOMNodeList querySelectorAll(final String selectors) {

        return (decoratedElement.querySelectorAll(selectors));
    }

    @Override
    public DOMCSSStyleDeclaration getComputedStyle() {

        return (decoratedElement.getComputedStyle());
    }

    @Override
    public DOMBoundingClientRect getBoundingClientRect() {

        return (decoratedElement.getBoundingClientRect());
    }

    @Override
    public void setAttribute(final String name, final String value) {

        decoratedElement.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(final String name) {

        decoratedElement.removeAttribute(name);
    }

    @Override
    public String getAttribute(final String name) {

        return (decoratedElement.getAttribute(name));
    }

    @Override
    public boolean click(final boolean waitUntilLoaded) {

        return (decoratedElement.click(waitUntilLoaded));
    }

    @Override
    public DOMWindow click() {

        return (decoratedElement.click());
    }

    @Override
    public DOMWindow fireMouseEvent(final String event) {

        return (decoratedElement.fireMouseEvent(event));
    }

    @Override
    public DOMWindow fireFocusEvent(final String event) {

        return (decoratedElement.fireFocusEvent(event));
    }

    @Override
    public DOMWindow fireKeyboardEvent(final String event, final char printableChar) {

        return (decoratedElement.fireKeyboardEvent(event, printableChar));
    }

    @Override
    public DOMWindow type(final String content) {

        return (decoratedElement.type(content));
    }

    @Override
    public DOMWindow typeAndEnter(final String content) {

        return (decoratedElement.typeAndEnter(content));
    }

    @Override
    public String getInnerHTML() {

        return (decoratedElement.getInnerHTML());
    }

    @Override
    public HTMLUtil htmlUtil() {

        return (decoratedElement.htmlUtil());
    }

    @Override
    public DOMWindow mouseover() {

        return (decoratedElement.mouseover());
    }

    @Override
    public DOMWindow focus() {

        return (decoratedElement.focus());
    }

    @Override
    public DOMWindow sendClick(final float x, final float y) {

        return (decoratedElement.sendClick(x, y));
    }

    @Override
    public DOMWindow keypress(final DOMKeyboardEvent.Key content) {

        return decoratedElement.keypress(content);
    }
}
