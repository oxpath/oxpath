package uk.ac.ox.cs.diadem.webapi.dom;

import java.util.List;

import uk.ac.ox.cs.diadem.webapi.dom.DOMCSS2Properties.CssProperty;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMFocusEvent;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMKeyboardEvent;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMMouseEvent;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.CSSMutationObserver;

/**
 * Wrapping interface for http://www.w3.org/2003/01/dom2-javadoc/org/w3c/dom/Element.html
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 *
 */
public interface DOMElement extends DOMNode, DOMNodeSelector {

  /**
   * Puts the given css properties under observation, in particular compares the current state with the one at the
   * moment {@link CSSMutationObserver#takeRecords()} is called
   *
   * @param properties
   * @return a {@link CSSMutationObserver} as handle for the observed properties
   */
  CSSMutationObserver observeCSSProperties(CssProperty... properties);

  /**
   * Gets the computed style object
   *
   * @return the computed style object
   */
  DOMCSSStyleDeclaration getComputedStyle();

  /**
   * Gets the {@link DOMBoundingClientRect} for the element, relative to the document, i.e., taking into account window
   * scrolling
   *
   * @return the {@link DOMBoundingClientRect} for the element
   */
  DOMBoundingClientRect getBoundingClientRect();

  /**
   * set an attribute on the element.
   *
   * @param name
   *          attribute name
   * @param value
   *          the value to set
   */
  void setAttribute(String name, String value);

  /**
   * remove an attribute by name
   *
   * @param name
   */
  void removeAttribute(String name);

  /**
   * Gets an attribute by name
   *
   * @param name
   * @return the attribute or null if not found
   */
  public String getAttribute(String name);

  /**
   * Simulates a real mouse left click on an element. It generates mousemove mousedown and mouseup. Also, proper focus
   * and blur events are generatedexecuteJavaScript
   *
   * @param waitUntilLoaded
   *          if true, the method is performed synchronously and waits until the document at the new location (if any),
   *          is fully loaded.
   * @return in case the method is performed synchronously, it returns true if a new url navigation has been triggered,
   *         false otherwise.
   */
  boolean click(boolean waitUntilLoaded);

  /**
   * Simulates a real mouse left click on an element. It generates mousemove mousedown and mouseup. Also, proper focus
   * and blur events are generated It waits until the document at the new location (if any), is fully loaded. If the
   * click triggers opening a new window, it is returned as result, otherwise the current winndow is returned
   *
   * @return If a new window has been opened due to the click, the new window is returned, otherwise the current window
   *         is returned (which may have a new location).
   */
  DOMWindow click();

  /**
   * Performs only the specified mouse event (see {@link DOMMouseEvent}) on this element element and waits until the
   * document at the new location (if any), is fully loaded. If the event triggers opening a new window, it is returned
   * as result, otherwise the current window is returned. Note: this method won't generate related events, e.g., for
   * click it won't simulate the correct sequence, but only the click event.
   *
   * @param event
   *          (see {@link DOMMouseEvent})
   * @return If a window has been opened due to the event, the new window is returned, otherwise the current window is
   *         returned (which may have a new location).
   */
  DOMWindow fireMouseEvent(String event);

  /**
   * Performs the specified focus/blur event (see {@link DOMFocusEvent}) on this element element and waits until the
   * document at the new location (if any), is fully loaded. If the event triggers opening a new window, it is returned
   * as result, otherwise the current window is returned. To simulate the user focus action, use the method
   * <code>focus()</code>
   *
   * @param event
   *          (see {@link DOMFocusEvent})
   * @return If a window has been opened due to the event, the new window is returned, otherwise the current window is
   *         returned (which may have a new location).
   */
  DOMWindow fireFocusEvent(String event);

  /**
   * Performs the specified focus event (see {@link DOMKeyboardEvent}) on this element element and waits until the
   * document at the new location (if any), is fully loaded. If the event triggers opening a new window, it is returned
   * as result, otherwise the current window is returned
   *
   * @param event
   *          (see {@link DOMKeyboardEvent})
   * @param printableChar
   *          the char related to the key event.
   * @return If a window has been opened due to the event, the new window is returned, otherwise the current window is
   *         returned (which may have a new location).
   */
  public DOMWindow fireKeyboardEvent(String event, char printableChar);

  /**
   * Enter the given text of characters on the element (usually an input text). This simulates the sequence of acquiring
   * the focus and generating keydown,keypress,keyup for each key of the content. It waits until the document at the new
   * location (if any), is fully loaded. If the event triggers opening a new window, it is returned as result, otherwise
   * the current window is returned
   *
   * @param content
   *          the text to enter
   * @return If a window has been opened due to the event, the new window is returned, otherwise the current window is
   *         returned (which may have a new location).
   */
  public DOMWindow type(String content);

  /**
   * As the method <code>type(String content)</code>, it types the given text on the element (usually an input text) and
   * then generates the "VK_RETURN" keypress event at the end. It waits until the document at the new location (if any),
   * is fully loaded. If the event triggers opening a new window, it is returned as result, otherwise the current window
   * is returned
   *
   * @param content
   *          the text to enter
   * @return If a window has been opened due to the event, the new window is returned, otherwise the current window is
   *         returned (which may have a new location).
   */
  public DOMWindow typeAndEnter(String content);

  /**
   * Inner html
   *
   * @return
   */
  public String getInnerHTML();

  /**
   *
   * @return
   */
  public String getOuterHTML();

  /**
   * Returns an object that allows to cast down this element to HTML DOM specific implementations
   */
  public HTMLUtil htmlUtil();

  /**
   * Simulates a real mouseover event (mousemove and mouseover). It waits until the document at the new location (if
   * any), is fully loaded. If the event triggers opening a new window, it is returned as result, otherwise the current
   * window is returned
   *
   * @return If a window has been opened due to the event, the new window is returned, otherwise the current window is
   *         returned (which may have a new location).executeJavaScript
   */
  public DOMWindow mouseover();

  /**
   * Simulates a real focus event (also generates the blur on the current active element if any). It waits until the
   * document at the new location (if any), is fully loaded. If the event triggers opening a new window, it is returned
   * as result, otherwise the current window is returned
   *
   * @return If a window has been opened due to the event, the new window is returned, otherwise the current window is
   *         returned (which may have a new location).
   */
  public DOMWindow focus();

  public boolean isEnabled();

  /**
   * Sends a click event on the given coordinates
   *
   * @param x
   * @param y
   * @return If a window has been opened due to theObject executeJavaScript(String script, Object... arg); event, the
   *         new window is returned, otherwise the current window is returned (which may have a new location).
   */
  DOMWindow sendClick(float x, float y);

  public abstract DOMWindow keypress(final DOMKeyboardEvent.Key content);

  /**
   * When the current {@link DOMElement} is either IFrame of Frame, this method navigates to the enclosed page if they
   * have the proerty src set. Throws a WebAPIRuntimeException if the element is not a IFrame or Frame, and if the src
   * property is not defined
   *
   * @return
   */
  DOMWindow moveToFrame();

  /**
   * When the current {@link DOMElement} has the href property set, this method navigates to its specified URL. Throws a
   * WebAPIRuntimeException if the href property is not defined for this node.
   *
   * @return
   */
  DOMWindow moveToHREF();

  DOmElementOnJS js();

  /**
   * Returns elements appearing whithin the radius of this target elements. In particular, it returns elements in the
   * following order: north,north-east,east,south-east,south,south-west,west,north-west
   *
   * @param radius
   * @return a list of {@link DOMElement} in the oder: north,north-east,east,south-east,south,south-west,west,north-west
   */
  List<DOMElement> getNeighbourhood(int radius);

}
