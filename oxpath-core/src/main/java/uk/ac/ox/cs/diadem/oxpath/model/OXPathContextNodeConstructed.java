/**
 *
 */
package uk.ac.ox.cs.diadem.oxpath.model;

import java.util.List;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNamedNodeMap;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNodeList;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMEventListener;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver.MutationObserverOptions;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;

/**
 *
 * Wrapper for DOM nodes that do not come from the browser. Currently, this includes notional root and the style nodes,
 * as they have no analogue in DOM. Provides a minimal construction of a DOM Node so {@code compareTo} works correctly
 * for the sorted node list
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class OXPathContextNodeConstructed extends OXPathContextNode {

  /**
   * Constructer for wrapped node.
   *
   * @param iKey
   *          display property name
   * @param iValue
   *          display value of the node
   * @param iParent
   *          extracted parent id
   * @param iLast
   *          last-sibling extracted id
   */
  public OXPathContextNodeConstructed(final String iKey, final String iValue, final int iParent, final int iLast) {
    super(null, iParent, iLast);
    key = iKey;
    value = iValue;
  }

  /**
   * Evaluates XPath expression. Since minimal wrapper, every expression returns the value of the node.
   *
   * @param path
   *          input XPath expression
   * @return always the string value
   */
  @Override
  public OXPathType getByXPath(final String path) {
    return new OXPathType(value);
  }

  /**
   * {@code compareTo} is implemented so as to establish natural ordering compatible with equals.
   *
   * @param other
   *          the other node to compare
   * @return standard {@code compareTo} contract
   */
  @Override
  public int compareTo(final OXPathContextNode other) {
    return Integer.MIN_VALUE;// we want all Browser-based nodes to be greater than our constructed nodes in the list
  }

  /**
   * {@code compareTo} is implemented so as to establish natural ordering compatible with equals.
   *
   * @param other
   *          the other node to compare
   * @return standard {@code compareTo} contract
   */
  @Override
  public int compareTo(final OXPathContextNodeConstructed other) {
    if (equals(other))
      return 0;
    final int keyDiff = key.compareTo(other.key);
    if (keyDiff != 0)
      return keyDiff;
    else {
      final int valDiff = value.compareTo(other.key);
      if (valDiff != 0)
        return valDiff;
      else
        return other.getParent() - getParent();
    }
  }

  /**
   * Returns our implementation of the wrapped node. At present, this is very minimal (only {@code toPrettyHtml} is
   * implemented).
   */
  @Override
  public DOMNode getNode() {
    final String nodeKey = key;
    final String nodeValue = value;
    return new DOMNode() {

      @Override
      public void addEventListener(final String arg0, final DOMEventListener arg1, final boolean arg2) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public void removeEventListener(final String arg0, final DOMEventListener arg1, final boolean arg2) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNode appendChild(final DOMNode arg0) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public short compareDocumentPosition(final DOMNode arg0) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNamedNodeMap<DOMNode> getAttributes() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNodeList getChildNodes() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNode getFirstChild() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNode getLastChild() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public String getLocalName() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNode getNextSibling() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public String getNodeName() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public Type getNodeType() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public String getNodeValue() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMDocument getOwnerDocument() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNode getParentNode() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNode getPreviousSibling() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public String getTextContent() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMXPathEvaluator getXPathEvaluator() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNode insertBefore(final DOMNode arg0, final DOMNode arg1) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public boolean isDescendant(final DOMNode arg0) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public boolean isSameNode(final DOMNode arg0) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public boolean isVisible() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNode removeChild(final DOMNode arg0) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMNode replaceChild(final DOMNode arg0, final DOMNode arg1) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public void setTextContent(final String arg0) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");

      }

      @Override
      public String toPrettyHTML() {
        return "<" + nodeKey + ">" + nodeValue + "</" + nodeKey + ">";
      }

      @Override
      public boolean isEqualNode(final DOMNode arg0) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public boolean isTextNode() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public Object executeJavaScript(final String arg0, final Object... arg1) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMMutationObserver registerMutationObserver(final MutationObserverOptions arg0) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public DOMMutationObserver registerMutationObserver(final boolean arg0, final boolean arg1, final boolean arg2,
          final boolean arg3, final List<String> arg4) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public boolean isStale() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public String getXPathLocator() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public String toPrettyString() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public WebBrowser getBrowser() {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

      @Override
      public String getDOMProperty(final String domProperty) {
        throw new RuntimeException("Method for Constructed node wrapper not yet implmented!");
      }

    };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = (prime * result) + ((key == null) ? 0 : key.hashCode());
    result = (prime * result) + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    final OXPathContextNodeConstructed other = (OXPathContextNodeConstructed) obj;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals(other.key))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  /**
   * display property name
   */
  private final String key;
  /**
   * display property value
   */
  private final String value;
}
