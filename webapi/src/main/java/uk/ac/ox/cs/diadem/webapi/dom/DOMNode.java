/**
 *
 */
package uk.ac.ox.cs.diadem.webapi.dom;

import java.util.List;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder.Engine;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMEventTarget;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver.MutationObserverOptions;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 * @param <N>
 *
 */
public interface DOMNode extends DOMEventTarget {

  public static enum Type {
    ELEMENT(1), ATTRIBUTE(2), TEXT(3), CDATA_SECTION(4), ENTITY_REFERENCE(5), ENTITY(6), PROCESSING_INSTRUCTION(7), COMMENT(
        8), DOCUMENT(9), DOCUMENT_TYPE(10), DOCUMENT_FRAGMENT(11), NOTATION(12);

    private final Integer number;

    Type(final int number) {
      this.number = Integer.valueOf(number);

    }

    public Integer asNumber() {
      return number;
    }
  }

  /**
   * Introduced for {@link Engine#WEBDRIVER_FF} to check if an element is stale. Using a stale object throws an
   * exception
   *
   * @return
   */
  boolean isStale();

  /**
   *
   * @return
   */
  Type getNodeType();

  /**
   * returns the string value (calling toString()) of the specified property. It is useful for atomic values, while for
   * arrays and objects it returns their toString()/ It returns null if the property is undefined
   * 
   * @param domProperty
   * @return
   */
  String getDOMProperty(String domProperty);

  /**
   *
   * @return
   */
  DOMNodeList getChildNodes();

  String getNodeValue();

  DOMNode getParentNode();

  String getLocalName();

  /**
   * Be careful can return null TODO: Gio check it!
   *
   * @return
   */
  DOMNamedNodeMap<DOMNode> getAttributes();

  String getNodeName();

  /**
   * Return a html representation of this node
   *
   * @return
   */
  String toPrettyHTML();

  /**
   *
   * @return returns the text content of this node and its descendants.
   */
  String getTextContent();

  boolean isDescendant(DOMNode node);

  boolean isVisible();

  /**
   *
   * @return the {@link DOMXPathEvaluator} associated to this node. There is one DOMXPathEvaluator associated with each
   *         browser instance, therefore this method is equivalent to the same method in {@link WebBrowser}
   */
  DOMXPathEvaluator getXPathEvaluator();

  /**
   * From the DOM standard
   *
   * @param other
   *          other node
   * @return defined in DOM
   */
  short compareDocumentPosition(DOMNode other);

  DOMDocument getOwnerDocument();

  boolean isSameNode(DOMNode other);

  boolean isEqualNode(DOMNode other);

  /**
   * Adds a node to the end of the list of children of a specified parent node. If the node already exists it is removed
   * from current parent node, then added to new parent node.
   * http://www.w3.org/TR/DOM-Level-3-Core/core.html#ID-184E7107
   * https://developer.mozilla.org/en/DOM/element.appendChild
   *
   * @param newChild
   * @return returns the added node
   */
  DOMNode appendChild(DOMNode newChild);

  /**
   * Removes a child node from the DOM. Returns removed node. https://developer.mozilla.org/En/DOM/Node.removeChild
   *
   * @param child
   * @return Returns removed node.
   */
  DOMNode removeChild(DOMNode child);

  /**
   * Replaces one child node of the specified element with another.
   * https://developer.mozilla.org/En/DOM/Node.replaceChild
   *
   * @param newChild
   * @param oldChild
   * @return
   */
  DOMNode replaceChild(DOMNode newChild, DOMNode oldChild);

  /**
   * Inserts the specified node before a reference element as a child of the current node. If refChild is null,
   * newElement is inserted at the end of the list of child nodes.
   *
   * @param newChild
   * @param refChild
   * @return The node being inserted, that is newChild
   */
  DOMNode insertBefore(DOMNode newChild, DOMNode refChild);

  void setTextContent(String text);

  DOMNode getPreviousSibling();

  DOMNode getNextSibling();

  DOMNode getLastChild();

  DOMNode getFirstChild();

  boolean isTextNode();

  DOMMutationObserver registerMutationObserver(boolean childList, boolean attributes, boolean subtree,
      boolean characterData, List<String> attributeFilter);

  DOMMutationObserver registerMutationObserver(MutationObserverOptions options);

  Object executeJavaScript(String script, Object... arg);

  /**
   * Returns a unique xpath locator for this node. It only supports Text, Document and Element node types. for all the
   * other node types will return null
   *
   * @return unique xpath locator for text or element nodes, null otherwise
   */
  String getXPathLocator();

  String toPrettyString();

  WebBrowser getBrowser();

}
