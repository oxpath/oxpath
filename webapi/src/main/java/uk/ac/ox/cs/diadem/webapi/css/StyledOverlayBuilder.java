package uk.ac.ox.cs.diadem.webapi.css;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 * Builder to construct overlays in {@link DOMWindow}, to display informations
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface StyledOverlayBuilder {

  static interface Rect {

    Integer getLeft();

    Integer getTop();

    Integer getHeight();

    Integer getWidth();

    List<Integer> asList();
  }

  public Rect createRect(int top, int left, int width, int height);

  /**
   * Builds the (immutable) {@link StyledOverlay}
   *
   * @param string
   *
   * @return
   */
  public StyledOverlay build(final String cssRules);

  /**
   * Add a {@link StyledNode} as root
   *
   * @param item
   *          the root node to add
   * @throws WebAPIRuntimeException
   *           if the root node is already present
   * @return the builder
   */
  public StyledOverlayBuilder addRootNode(final StyledNode item);

  /**
   * Add a node as child of another one. If the parent node is not already part of the tree, it is added as root.
   *
   * @param parent
   *          the parent node
   * @param node
   *          the node to add
   * @return the builder
   */
  public StyledOverlayBuilder addNode(final StyledNode parent, final StyledNode node);

  // TODO public StyledNodeBuilder newNodeBuilder(final String xpathLocator);

  /**
   * Decorates this overlay with an additional one to show textual information, in form of key/value pairs, each of them
   * associated with css classes for styling purpose
   *
   * @param referenceNode
   * @param keyValues
   * @param classNames
   */
  public void addInfobox(final StyledNode referenceNode, final Pair<String, String> keyValues,
      final String... classNames);

  /**
   * Crate a {@link StyledNode} associated to the given CSS class names. This node refers a {@link DOMNode} addressed by
   * xpath.
   *
   * @param xpathLocator
   *          the expression locating one {@link DOMNode} in the current page.
   * @param classNames
   *          list of CSS class names for the created node
   * @return a new {@link StyledNode}, not attached to the {@link StyledOverlay} being build
   */
  public StyledNode createNode(final String xpathLocator, final String... classNames);

  /**
   *
   * @param startTextNodeXPathLocator
   * @param endTextNodeXPathLocator
   * @param range
   * @param classNames
   * @return
   */
  public StyledRangeNode createRangeNode(final String startTextNodeXPathLocator, final String endTextNodeXPathLocator,
      final Pair<Integer, Integer> range, final String... classNames);

  /**
   *
   * @param locatorForTextNode
   * @param range
   * @param classNames
   * @return
   */
  public StyledRangeNode createRangeNode(final String locatorForTextNode, final Pair<Integer, Integer> range,
      final String... classNames);

  public void addFixedHTMLBox(String innerHTML, Rect boundingRect, String... classNames);

  // /**
  // * Adds the given attributes to this node. This enables to use attribute selectors in {@link CSSStyleRule} See for
  // * instance https://developer.mozilla.org/en-US/docs/CSS/Attribute_selectors
  // *
  // * @param node
  // * @param attributes
  // */
  // public void addAttributeValue(StyledNode node, final Pair<String, String>... attributes);
}