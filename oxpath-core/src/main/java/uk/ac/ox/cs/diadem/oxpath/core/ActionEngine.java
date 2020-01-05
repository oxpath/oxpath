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
 *
 */
package uk.ac.ox.cs.diadem.oxpath.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.oxpath.model.language.Action;
import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLForm;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLSelect;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNamedNodeMap;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode.Type;
import uk.ac.ox.cs.diadem.webapi.dom.DOMTypeableElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMFocusEvent;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMKeyboardEvent;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMMouseEvent;
import uk.ac.ox.cs.diadem.webapi.dom.utils.WebUtils;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPINotVisibleOrDisableElementException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 *
 * Class for enabling actions in web pages
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
public class ActionEngine {

  private static final char newline = '\n';// FIXME use System.lineSeparator();
  private static final Logger LOGGER = LoggerFactory.getLogger(ActionEngine.class);
  private static long delayAfterActionInMillisec = 200;
  private static final BlockingQueue<Object> SLEEPER = new ArrayBlockingQueue<Object>(1);

  public ActionEngine(final long waitAfterActionExecution_ms) {
    delayAfterActionInMillisec = waitAfterActionExecution_ms;
  }

  /**
   * Returns a FieldType based on the HTML data of the node.
   *
   * @param n
   *          Node object to return FieldType
   * @return value from FieldType enum relating HTML form field type of <tt>n</tt>
   */
  public static FieldTypes getFieldType(final uk.ac.ox.cs.diadem.webapi.dom.DOMNode n) {
    FieldTypes ft = null;
    final String nodeName = n.getNodeName().toLowerCase();
    if (nodeName == null)
      throw new NullPointerException("Input parameter cannot be null!");
    // these are all mutually exclusive conditions, ft should be set exactly once
    if (nodeName.equals("input")) {
      final DOMNamedNodeMap<?> attributes = n.getAttributes();
      if (attributes == null)
        return FieldTypes.TEXT;// standard defaults to text if not @type is present}
      final DOMNode typeRaw = attributes.getNamedItem("type");
      if (typeRaw == null)
        return FieldTypes.TEXT;// standard defaults to text if not @type is present
      final String type = typeRaw.getNodeValue().toLowerCase();
      if (type.equals("text"))
        return FieldTypes.TEXT;
      else if (type.equals("password"))
        return FieldTypes.PASSWORD;
      else if (type.equals("checkbox"))
        return FieldTypes.CHECKBOX;
      else if (type.equals("radio"))
        return FieldTypes.RADIOBUTTON;
      else if (type.equals("button"))
        return FieldTypes.INPUTBUTTON;
      else if (type.equals("file"))
        return FieldTypes.INPUTFILE;
      else if (type.equals("image"))
        return FieldTypes.INPUTIMAGE;
      else if (type.equals("submit"))
        return FieldTypes.INPUTSUBMIT;
      else if (type.equals("reset"))
        return FieldTypes.INPUTRESET;
      else
        return FieldTypes.TEXT;
    } else if (nodeName.equals("textarea")) {
      ft = FieldTypes.TEXTAREA;
    } else if (nodeName.equals("select")) {
      ft = FieldTypes.SELECT;
    } else if (nodeName.equals("button")) {
      ft = FieldTypes.BUTTON;
    } else if (nodeName.equals("a")) {
      ft = FieldTypes.HREF;
    } else {
      ft = FieldTypes.CLICKABLE;
    }
    return ft;
  }

  /**
   * This method takes an action on a page, including any relevant javascript events listening for said action. Inputs
   * text. Simulates typing, rather than cutting and pasting. Typing '\n' submits the corresponding form for many
   * elements associated with fields
   *
   * @param context
   *          the context node (upon which to take the action)
   * @param ft
   *          type of the context node
   * @param action
   *          action string of action
   * @return the <tt>page</tt> input parameter after the action has occurred
   * @throws OXPathException
   *           if malformed action token
   * @throws IOException
   *           if IO error occurs
   */
  DOMElement takeActionExplicitly(final DOMElement context, final FieldTypes ft, final Action action)
      throws OXPathException, IOException {
    LOGGER.info("Taking explicit action '{}' on element '{}'", action, context);
    final String value = (String) action.getValue();
    switch (ft) {
    case SELECT:
      // safe because we checked for <select> in the switch above
      final DOMHTMLSelect select = context.htmlUtil().asHTMLSelect();

      final DOMWindow domWindow = select.selectOptionByText(value);
      waitIfRequested(action, context);
      return domWindow.getDocument().getDocumentElement();
    case TEXT:// otherwise, we try and type on the element
    case TEXTAREA:
    case PASSWORD:
    default:
      DOMWindow window = null;
      final DOMTypeableElement inputElement = context.htmlUtil().asTypeableElement();
      inputElement.select();
      if (value.endsWith(ENTERSIGNAL)) {
        window = inputElement.typeAndEnter(value.substring(0, value.lastIndexOf(ENTERSIGNAL)));
      } else {
        window = inputElement.type(value);
      }
      waitIfRequested(action, inputElement);
      return window.getDocument().getDocumentElement();
    }

  }

  /**
   * This method takes an action on a page, including any relevant javascript events listening for said action Handles
   * selection on an HTML <tt>select</tt> element
   *
   * @param context
   *          the context node (upon which to take the action)
   * @param ft
   *          type of the context node
   * @param index
   *          position of action
   * @return the <tt>page</tt> input parameter after the action has occurred
   * @throws OXPathException
   *           if malformed action token
   * @throws IOException
   *           if IO error occurs
   */
  DOMElement takeActionByIndex(final DOMElement context, final FieldTypes ft, final Action action)
      throws OXPathException, IOException {

    final int index = (Integer) action.getValue();

    switch (ft) {// because we checked the node properties in getFieldType(), each of the initial casts is a safe
    // operation
    case SELECT:
      LOGGER.info("Taking POSITION action '{}' on element '{}'", index, context);
      final DOMHTMLSelect select = context.htmlUtil().asHTMLSelect();
      final DOMWindow window = select.selectOptionIndex(index);
      waitIfRequested(action, context);
      return window.getDocument().getDocumentElement();
    default:
      throw new OXPathException(MessageFormat.format(
          "Cannot use position references for HTML elements other than <select>, on page {0}", context.getBrowser()
              .getLocationURL()));
    }
  }

  // /**
  // * This method takes an action on a page, including any relevant javascript events listening for said action. This
  // * method omits the optional {@code key} parameter.
  // *
  // * @param context
  // * the context node (upon which to take the action)
  // * @param ft
  // * type of the context node
  // * @param action
  // * keyword of action
  // * @return the <tt>page</tt> input parameter after the action has occurred
  // * @throws OXPathException
  // * if malformed action token
  // * @throws IOException
  // * if IO error occurs
  // */
  // public static DOMElement takeAction(final DOMElement context, final FieldTypes ft, final ActionKeywords action)
  // throws OXPathException, IOException {
  // return ActionEngine.takeAction(context, ft, action, newline);
  // }

  /**
   * This method takes an action on a page, including any relevant javascript events listening for said action
   *
   * @param context
   *          the context node (upon which to take the action)
   * @param ft
   *          type of the context node
   * @param action
   *          keyword of action
   * @param key
   *          character to press for keyboard events; input ignored for other events
   * @return the <tt>page</tt> input parameter after the action has occurred
   * @throws OXPathException
   *           if malformed action token
   * @throws IOException
   *           if IO error occurs
   */
  DOMElement takeActionByKeyword(final DOMElement context, final FieldTypes ft, final Action actionToPerform)
      throws OXPathException, IOException {
    final ActionKeywords action = (ActionKeywords) actionToPerform.getValue();
    LOGGER.info("Taking action '{}' on element ['{}'] ", action.name(), context);// context.getOuterHTML());
    // so we don't mute reference supplied as parameter
    DOMWindow windowAfterAction = null;
    switch (action) {
    case MOVETOFRAME:
      windowAfterAction = context.moveToFrame();
      break;
    case ENTERFRAME:
      windowAfterAction = context.getBrowser().switchToFrame(context);
      break;
    case MOVETOHREF:
      windowAfterAction = context.moveToHREF();
      break;
    case CLICK:
      windowAfterAction = context.click();
      // windowAfterAction = context.getOwnerDocument().getEnclosingWindow();
      break;
    case NEXTCLICK:
      windowAfterAction = context.click();
      break;
    case PRESSENTER:
      windowAfterAction = context.typeAndEnter("");
      break;
    case CLICKWITHCHANGE:
      if (context.isEnabled()) {
        windowAfterAction = context.click();
        windowAfterAction.getBrowser().stats().incrementPageNumbers();
      } else
        return null;
      break;
    case DBLCLICK:
      windowAfterAction = context.fireMouseEvent(DOMMouseEvent.dblclick);
      break;
    case FOCUS:
      windowAfterAction = context.fireFocusEvent(DOMFocusEvent.focus);
      break;
    case MOUSEDOWN:
      windowAfterAction = context.fireMouseEvent(DOMMouseEvent.mousedown);
      break;
    case MOUSEENTER:
      windowAfterAction = context.fireMouseEvent(DOMMouseEvent.mouseenter);
      break;
    case MOUSEMOVE:
      windowAfterAction = context.fireMouseEvent(DOMMouseEvent.mousemove);
      break;
    case MOUSEOVER:
      windowAfterAction = context.mouseover();
      break;
    case MOUSEOUT:
      windowAfterAction = context.fireMouseEvent(DOMMouseEvent.mouseout);
      break;
    case MOUSEUP:
      windowAfterAction = context.fireMouseEvent(DOMMouseEvent.mouseup);
      break;
    case RIGHTCLICK:
      throw new OXPathException("Right click not implemented!");
      // break;
    case CHECK:
      throw new RuntimeException("Check not implemented!");
      // switch (ft) {
      // case CHECKBOX :
      // returnPage = (HtmlPage) ((HtmlCheckBoxInput) element).setChecked(true);
      // break;
      // case RADIOBUTTON :
      // returnPage = (HtmlPage) ((HtmlRadioButtonInput) element).setChecked(true);
      // break;
      // default :
      // returnPage = element.click();
      // break;
      // }
      // break;
    case UNCHECK:
      throw new RuntimeException("Uncheck not implemented!");
      // switch (ft) {
      // case CHECKBOX :
      // returnPage = (HtmlPage) ((HtmlCheckBoxInput) element).setChecked(false);
      // break;
      // case RADIOBUTTON :
      // returnPage = (HtmlPage) ((HtmlRadioButtonInput) element).setChecked(false);
      // break;
      // default :
      // returnPage = element.click();
      // break;
      // }
      // break;
    case KEYDOWN:
      windowAfterAction = context.fireKeyboardEvent(DOMKeyboardEvent.keydown, newline);
      break;
    case KEYPRESS:
      windowAfterAction = context.fireKeyboardEvent(DOMKeyboardEvent.keypress, newline);
      break;
    case KEYUP:// implemented as part of doType(String) method (protected and incorporated into simulation)
      windowAfterAction = context.fireKeyboardEvent(DOMKeyboardEvent.keyup, newline);
      break;
    case SUBMIT:
      final DOMHTMLForm asHTMLForm = context.htmlUtil().asHTMLForm();
      if (asHTMLForm != null) {
        windowAfterAction = asHTMLForm.submit();
      } else {
        if (context.htmlUtil().asHTMLInputElement() != null) {
          windowAfterAction = context.fireKeyboardEvent(DOMKeyboardEvent.keypress, newline);
        }
      }
      break;
    case WHEEL:
      windowAfterAction = context.fireMouseEvent(DOMMouseEvent.mousewheel);
      break;

    default:// do nothing
      LOGGER.warn("Not handled case for action {}, return the document element on page {}", action, context
          .getBrowser().getLocationURL());
      return context.getOwnerDocument().getDocumentElement();
    }

    waitIfRequested(actionToPerform, context);

    return windowAfterAction.getDocument().getDocumentElement();
  }

  /**
   * Forces waiting after an action if specified
   *
   * @param action
   * @param context
   */
  public void waitIfRequested(final Action action, final DOMNode context) {
    if (action.hasWait() && (action.getWait() > 0)) {
      forceWait(action, context, TimeUnit.SECONDS, action.getWait());
    } else {
      if (delayAfterActionInMillisec > 0) {
        forceWait(action, context, TimeUnit.MILLISECONDS, delayAfterActionInMillisec);
      }
    }
  }

  private void forceWait(final Action action, final DOMNode context, final TimeUnit timeUnit, final long valueToWait) {

    try {
      // otherwise crashes
      if ((context != null) && !context.isStale()) {
        LOGGER.info("Awaiting '{} {}' after action '{}' at '{}'", new Object[] { valueToWait,
            timeUnit.name().toLowerCase(), action, context });
      } else {
        LOGGER.info("Awaiting '{} {}' after action '{}'", new Object[] { valueToWait, timeUnit.name().toLowerCase(),
            action });
      }
      sleep(timeUnit, valueToWait);
    } catch (final UnhandledAlertException e) {
      LOGGER.warn("while waiting after action {}, dismissing alert {}", action, e.getMessage());
    }

  }

  private static void sleep(final TimeUnit timeUnit, final long valueToWait) {
    try {
      SLEEPER.poll(valueToWait, timeUnit);
    } catch (final Exception e) {
      // toNothing
    }
  }

  /**
   * Constant encoding carriage return character
   */
  public static final String ENTERSIGNAL = "\n";

  public DOMElement takeAction(final DOMNode contextNode, final FieldTypes ft, final Action action)
      throws OXPathException {
    DOMElement context = null;

    if (contextNode.getNodeType() != Type.ELEMENT)
      throw new OXPathRuntimeException(MessageFormat.format(
          "Cannot execute action <{0}> on a context node <{1}> of type <{2}>.", action.getValue(), contextNode,
          contextNode.getNodeType().name()), LOGGER);
    else {
      context = (DOMElement) contextNode;
    }

    try {// FIXME this is ugly but necessary until we are able to suppress location modal dialogs in Firefox!

      try {
        switch (action.getActionType()) {
        case POSITION:
          return takeActionByIndex(context, ft, action);
        case EXPLICIT:
          return takeActionExplicitly(context, ft, action);

        case KEYWORD:
          return takeActionByKeyword(context, ft, action);
        default:// in case we have an ungrounded variable action
          return null;
        }
      } catch (final IOException e) {
        LOGGER.error("Error  executing action {} on element {}", action.getActionType(), context);
        throw new OXPathRuntimeException(MessageFormat.format("error executing action <{0}>", action.getActionType()),
            e, LOGGER);
      } catch (final WebAPINotVisibleOrDisableElementException e) {
        LOGGER.error(
            "Error  executing action {} on not visible/enabled element {}. Evaluation continues on the current page",
            action.getActionType(), context);
        // throw new OXPathRuntimeException("error executing action on not visible/enabled element ", e, LOGGER);
        return context.getBrowser().getContentDOMWindow().getDocument().getDocumentElement();
      } catch (final StaleElementReferenceException e) {
        LOGGER.error("Error  executing action {} on stale element {}. Aborting", action.getActionType(),
            context.getTextContent());
        // throw new OXPathRuntimeException("error executing action on not visible/enabled element ", e, LOGGER);
        throw new OXPathRuntimeException(
            MessageFormat.format("Error executing action <{0}> on stale element.", action), e, LOGGER);
      }

      catch (final WebAPIRuntimeException e) {
        LOGGER.error("Error  executing action <{}> on element <{}> on page <{}>", new Object[] {
            action.getActionType(), context, context.getBrowser().getLocationURL() });
        throw new OXPathRuntimeException(MessageFormat.format("error executing action <{0}>", action), e, LOGGER);
      }
    } catch (final UnhandledAlertException e) {
      LOGGER.warn("Try to dismiss alert and continue on current page. <{}>", e.getMessage());
      sleep(TimeUnit.SECONDS, 1);
      return context.getBrowser().getContentDOMWindow().getDocument().getDocumentElement();
    } catch (final WebDriverException e) {// possibly bug of selenuim
      // https://code.google.com/p/selenium/issues/detail?id=3544
      try {
        LOGGER.error("Error computing action <{}>, continue on current page.  Error <{}>", action, e.getMessage());
        return context.getBrowser().getContentDOMWindow().getDocument().getDocumentElement();
      } catch (final UnhandledAlertException ex) {
        // final attempt to get rid of this fucking alert in case of this selenium bug
        LOGGER.warn("Try to dismiss alert and continue on current page. <{}>", ex.getMessage());
        sleep(TimeUnit.SECONDS, 1);
        return context.getBrowser().getContentDOMWindow().getDocument().getDocumentElement();
      }
    }
  }

  private static void waitIfRequested(final Action actionElement) {
    TimeUnit unit = TimeUnit.MILLISECONDS;
    long value = delayAfterActionInMillisec;

    if (actionElement.hasWait()) {
      unit = TimeUnit.SECONDS;
      value = actionElement.getWait();
    }
    LOGGER.info("Awaiting '{} {}' after counter-back action for {} " + actionElement, value, unit);

    sleep(unit, value);

  }

  /**
   * 
   * TODO: can be considered for the solving the problem related to opening a new window. 
   * Back navigation in the browser history.
   * 
   * @param currentBrowser
   * @param action
   * @throws OXPathException
   */
  public static void performBackAction(final WebBrowser currentBrowser, final Action action) throws OXPathException {
    // if(actionElement.getAction().getActionType()==)

    // for all actions we should have an appropried counter action. Default is back button

    switch (action.getActionType()) {
    case KEYWORD:

      final ActionKeywords k = (ActionKeywords) action.getValue();
      if (k == ActionKeywords.ENTERFRAME) {
        LOGGER.info("Switching back on default document for action {}", action);
        currentBrowser.switchToDefaultContent();
        break;
      }// else default

    case POSITION:
    case EXPLICIT:
    default:
      LOGGER.info("Going back in the browser for action {}", action);
      currentBrowser.back();
    }
    // wait on each back if needed
    waitIfRequested(action);

    // necessary for firefox document expired message
    if (WebUtils.isErrorPage(currentBrowser.getContentDOMWindow())) {
      LOGGER.info("Document Expired, try resubmission");
      final boolean success = WebUtils.tryDocumentExpiredResubmission(currentBrowser);
      LOGGER.info("Resubmission success --> {}", success);
      if (!success) {
        LOGGER.error("Error going back due to expired document. Attemp to resubmit it has failed");
        throw new OXPathException("Error going back due to expired document. Attemp to resubmit it has failed");
      }
    }

  }
}