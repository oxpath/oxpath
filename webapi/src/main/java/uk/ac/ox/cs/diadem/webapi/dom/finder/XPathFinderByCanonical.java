package uk.ac.ox.cs.diadem.webapi.dom.finder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode.Type;
import uk.ac.ox.cs.diadem.webapi.dom.finder.DOMNodeFinderService.Score;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 * The XPathFinderByCanonical returns exactly one {@link XPathNodePointer} with a constant score defined in
 * ScoreDefinitions.
 * <p>
 *
 * It determines the canonical path by following the parent axis to the given root node. On every level, it counts the
 * siblings of the same local name to determine the positional predicate for that location step.
 *
 * @author JochenK
 * @author Giovanni Grasso
 */
public class XPathFinderByCanonical {

  private static final Logger LOGGER = LoggerFactory.getLogger(XPathFinderByCanonical.class);
  private static Cache<DOMNode, XPathNodePointer> cache = CacheBuilder.newBuilder().maximumSize(0).build();

  public static void enableCache() {
    cache = CacheBuilder.newBuilder().maximumSize(100000).build();
  }

  public static void disableCache() {
    cache = CacheBuilder.newBuilder().maximumSize(0).build();
  }

  private XPathFinderByCanonical() {
    // prevent instantiation
  }

  public static void invalidateCache() {
    cache.invalidateAll();
  }

  /**
   * Gets the absolute canonical XPath expression for a DOM node.
   *
   * @param node
   *          the DOM node
   * @return the XPath
   */
  public static XPathNodePointer getCanonicalXPath(final DOMNode node) {
    try {
      return querycache(node);
    } catch (final ExecutionException e) {
      LOGGER.debug("Error '{}' generating canonical xpath for node {}.", e.getMessage(), node);
      throw new WebAPIRuntimeException("error  generating id for node: " + node, e, LOGGER);
    }
  }

  /**
   * Gets the scored XPath expression candidate for a DOM node.
   *
   * @param node
   *          the DOM node
   * @param rootNode
   *          the root node
   * @return the XPath candidate
   */
  public static XPathNodePointer getCanonicalXPath(final DOMNode node, final DOMNode rootNode) {
    String xpath = computeCanonicalXPath(node);
    final String oxPathRoot = computeCanonicalXPath(rootNode);

    if (xpath.startsWith(oxPathRoot)) {
      xpath = xpath.substring(oxPathRoot.length());
    }
    if (xpath.length() == 0) {
      xpath = "/.";
    }
    if (xpath.startsWith("//")) {
      xpath = xpath.substring(1);
    }
    if (xpath.endsWith("/")) {
      LOGGER.error("Canoncial XPAth ends in '/': '{}', node='{}', rootNode='" + rootNode + "'.", xpath, node);
    }
    return new XPathNodePointerImpl(xpath, ScoreDefinitions.CANONICAL_PATH_SCORE, XPathNodePointer.Type.CANONICAL);
  }

  /**
   * NOT IMPLEMENTED.
   *
   * @param node
   * @param rootPath
   * @param threshold
   * @return
   */
  public static XPathNodePointer getCanNodePointer(final DOMNode node, final String rootPath, final Score threshold) {
    assert false : "not implemented";
    return null;
  }

  private static XPathNodePointer querycache(final DOMNode node) throws ExecutionException {
    XPathNodePointer best;
    if (cache.getIfPresent(node) == null) {
      LOGGER.debug("no inner-cached XPathNodePointer for {}", node);
    } else {
      LOGGER.debug("Found inner-cached XPathNodePointer for {}", node);
    }
    best = cache.get(node, new Callable<XPathNodePointer>() {
      @Override
      public XPathNodePointer call() {

        final Stopwatch w = Stopwatch.createUnstarted();
        w.start();
        LOGGER.debug("invoking XPathNodePointerImpl {}", node);
        final XPathNodePointerImpl p = new XPathNodePointerImpl(computeCanonicalXPath(node),
            ScoreDefinitions.CANONICAL_PATH_SCORE, XPathNodePointer.Type.CANONICAL);
        w.stop();
        LOGGER.debug("therefore computed pointer of type {}, for {}, score {}, in {}ms", new Object[] { p.getType(),
            node, p.getScore(), w.elapsed(TimeUnit.MILLISECONDS) });
        return p;
      }
    });
    return best;
  }

  /**
   * Gets the canonical XPath for a DOM node.
   *
   * @param node
   *          the DOM node
   * @return the canonical XPath
   * @throws WebAPIException
   */
  public static String computeCanonicalXPath(final DOMNode node) {
    String localName = "";
    // end of recursion at document node
    if (node == null)
      return "/"; // TODO screwy way for ending the recursion -- node becomes hybrid

    if (node.getNodeType() == DOMNode.Type.DOCUMENT)
      return "";

    if (node.getNodeType() == DOMNode.Type.COMMENT) {
      // TODO turn into exception, check for other node types
      assert false : "You cannot get a canoncial path to a comment node. This should become an exception.";
    }

    // get the position of the node among its siblings with the same
    // name
    int position = FinderUtils.getSameNameSiblingPosition(node);

    if (node.getNodeType() == DOMNode.Type.ELEMENT) {
      localName = FinderUtils.elementName(node);
    } else if (node.getNodeType() == DOMNode.Type.TEXT) {
      localName = "text()";
    } else {
      // in case of document element
      position = 0;
    }

    String path = computeCanonicalXPath(node.getParentNode());
    if (node.getNodeType() == Type.DOCUMENT)
      return path;

    final String localStep = localName + (position != 0 ? "[" + position + "]" : "");
    if (path.equals("/")) {
      path += localStep;
    } else {
      path += "/" + localStep;
    }

    // caches nodes during recursions
    if (cache.getIfPresent(node) == null) {
      LOGGER.debug("no inner-cached XPathNodePointer for {}, adding it in cache", node);
      cache.put(node, new XPathNodePointerImpl(path, ScoreDefinitions.CANONICAL_PATH_SCORE,
          XPathNodePointer.Type.CANONICAL));
    }
    return path;
  }
}
