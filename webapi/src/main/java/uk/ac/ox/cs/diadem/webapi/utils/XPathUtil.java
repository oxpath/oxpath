package uk.ac.ox.cs.diadem.webapi.utils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.xerces.util.XMLChar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathFinderByCanonical;
import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathNodePointer;
import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathNodePointerRanking;
import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathNodePointerRankingOnSet;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathNSResolver;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathResult;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 * Utils class to execute XPath queries
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class XPathUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(XPathUtil.class);

  private static Cache<String, Integer> cache = CacheBuilder.newBuilder().maximumSize(0).build(); // look Ma, no

  public static void enableCache() {
    cache = CacheBuilder.newBuilder().maximumSize(1000000).build();
  }

  public static void disableCache() {
    cache = CacheBuilder.newBuilder().maximumSize(0).build();
  }

  // CacheLoader

  public static void invalidateCache() {
    cache.invalidateAll();
  }

  /**
   * Executes the given query and returns the textual representation of the result (not useful to retrieve nodes)
   * 
   * @param expression
   *          the query
   * @param typeResult
   *          accepted values as "STRING" "BOOLEAN" "NUM" "NODESET"
   * @param browser
   * @return the textual representation of the result (not useful to retrieve nodes)
   */
  public static String doQuery(final String expression, final String typeResult, final WebBrowser browser) {

    final DOMXPathEvaluator e = browser.getXPathEvaluator();
    final DOMDocument document = browser.getContentDOMWindow().getDocument();
    short t = DOMXPathResult.ANY_TYPE;
    if (typeResult.equals("STRING"))
      t = DOMXPathResult.STRING_TYPE;
    if (typeResult.equals("BOOLEAN"))
      t = DOMXPathResult.BOOLEAN_TYPE;
    if (typeResult.equals("NUM"))
      t = DOMXPathResult.NUMBER_TYPE;
    if (typeResult.equals("NODESET"))
      t = DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE;
    final DOMXPathResult r = e.evaluate(expression, document, e.createNSResolver(document), t, null);
    return XPathUtil.manageResult(r, expression, typeResult.toString());
  }

  private static String manageResult(final DOMXPathResult r, final String expression, final String resultType) {

    if (resultType.equals("STRING"))
      return r.getStringValue();
    if (resultType.equals("BOOLEAN"))
      return new Boolean(r.getBooleanValue()).toString();
    if (resultType.equals("NUM"))
      return Double.toString(r.getNumberValue());
    if (resultType.equals("NODESET")) {
      // final boolean oneRes = false;
      final StringBuilder b = new StringBuilder();
      final long size = r.getSnapshotLength();
      for (int i = 0; i < size; i++) {
        final DOMNode value = r.snapshotItem(i);
        // if (value == null){
        // if (oneRes)
        // break;
        // else
        // b.append("Null result on " + expression);
        // }
        // oneRes = true;
        b.append(value.getLocalName());
      }
      return b.toString();
    }
    return "";
  }

  /**
   * Returns the value of the xpath expression count(myexpression). It throws a {@link DiademRuntimeException} if any
   * error occurs.
   * 
   * TODO why Integer and not int as return value?
   * 
   * @param expression
   *          the expression to use as argument for count
   * @param browser
   *          the browser to use
   * @return the value of the xpath expression count(myexpression), or -1 if any error occurs
   */
  public static Integer count(final String expression, final WebBrowser browser) {

    // If the key wasn't in the "easy to compute" group, we need to
    // do things the hard way.
    try {
      return cache.get(expression, new Callable<Integer>() {
        @Override
        public Integer call() {
          return executeCount(expression, browser);
        }
      });
    } catch (final ExecutionException e) {
      LOGGER.debug("Error '{}' doing XPath query count({}).", e.getMessage(), expression);
      throw new WebAPIRuntimeException("error evaluating xpath: " + expression + " " + e.getMessage(), e, LOGGER);
    }

  }

  private static Integer executeCount(final String expression, final WebBrowser browser) {
    try {
      final String floatOrLong = doQuery("count(" + expression + ")", "NUM", browser);
      LOGGER.debug("Doing XPath query count({}).", expression);

      // it's a float, I need to convert into integer and want to avoid casting
      int lenght = floatOrLong.length();
      if (floatOrLong.contains("."))
        lenght = floatOrLong.lastIndexOf(".");

      return Integer.parseInt(floatOrLong.substring(0, lenght));

    } catch (final Exception e) {

      LOGGER.debug("Error '{}' doing XPath query count({}). ", e.getMessage(), expression);
      throw new WebAPIRuntimeException("error evaluating xpath: " + "count(" + expression + ") :" + e.getMessage(), e,
          LOGGER);
    }
  }

  /**
   * Gets the position of the selected DOM node by adding a positional predicate to the xpath expression. If is not
   * found, it returns -1
   * 
   * @param xpath
   *          the xpath expression
   * @param node
   *          the target DOM node
   * @param count
   *          the count of the OXPath expression
   * @return the position
   */
  public static int retrieveNodePosition(final String xpath, final DOMNode target, final int count) {
    final DOMDocument document = target.getOwnerDocument();
    final DOMXPathEvaluator evaluator = document.getXPathEvaluator();
    // get the position of the selected DOM node by adding a
    // positional predicate to the given OXPath expression
    final DOMXPathNSResolver createNSResolver = evaluator.createNSResolver(document);
    for (int position = 1; position <= count; position++) {
      final DOMXPathResult result = evaluator.evaluate(xpath + "[" + position + "]", document, createNSResolver,
          DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);

      if (result != null) {
        final long size = result.getSnapshotLength();
        for (int i = 0; i < size; i++) {
          final DOMNode resultNode = result.snapshotItem(i);
          // only return position at node equality
          if ((resultNode != null) && target.equals(resultNode))
            return position;
        }
      }
    }

    return -1;
  }

  /**
   * Gets the first node returned by an xpath expression, or null if any
   * 
   * @param xpath
   *          the OXPath expression
   * @param node
   *          the DOM node
   * @return the first node
   */
  public static DOMNode getFirstNode(final String xpath, final WebBrowser browser) {

    final DOMDocument contextNode = browser.getContentDOMWindow().getDocument();
    return getFirstNode(xpath, contextNode);
  }

  public static DOMNode getFirstNode(final String xpath, final DOMNode contextNode) {
    final DOMXPathEvaluator evaluator = contextNode.getXPathEvaluator();
    // get the position of the selected DOM node by adding a
    // positional predicate to the given OXPath expression
    final DOMXPathNSResolver createNSResolver = evaluator.createNSResolver(contextNode);
    final DOMXPathResult result = evaluator.evaluate(xpath, contextNode, createNSResolver,
        DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);

    if (result != null) {
      final long size = result.getSnapshotLength();
      if (size > 0)
        return result.snapshotItem(0);
    }
    return null;
  }

  public static DOMNode getUniqueNode(final String xpath, final DOMNode contextNode) throws WebAPIException {
    final DOMXPathEvaluator evaluator = contextNode.getXPathEvaluator();
    // get the position of the selected DOM node by adding a
    // positional predicate to the given OXPath expression
    final DOMXPathNSResolver createNSResolver = evaluator.createNSResolver(contextNode);
    final DOMXPathResult result = evaluator.evaluate(xpath, contextNode, createNSResolver,
        DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);

    if (result != null) {
      final long size = result.getSnapshotLength();
      if (size == 0)
        throw new WebAPIException("Found no node for xpath='" + xpath + "'.", LOGGER);
      if (size > 1)
        throw new WebAPIException("Found several nodes for xpath='" + xpath + "'.", LOGGER);
      return result.snapshotItem(0);
    }
    return null;
  }

  public static boolean isUniquelyMatching(final String xpath, final DOMNode targetNode) {
    assert targetNode != null;
    final DOMXPathEvaluator evaluator = targetNode.getXPathEvaluator();
    // get the position of the selected DOM node by adding a
    // positional predicate to the given OXPath expression
    final DOMXPathNSResolver createNSResolver = evaluator.createNSResolver(targetNode);
    final DOMXPathResult result = evaluator.evaluate(xpath, targetNode, createNSResolver,
        DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);

    if (result != null) {
      final long size = result.getSnapshotLength();
      if (size != 1)
        return false;
      return result.snapshotItem(0).equals(targetNode);
    }
    return false;
  }

  /**
   * Gets the first node returned by an xpath expression, or null if any
   * 
   * @param xpath
   *          the OXPath expression
   * @param node
   *          the DOM node
   * @return the first node
   */
  public static List<DOMNode> getNodes(final String xpath, final WebBrowser browser) {
    final DOMDocument document = browser.getContentDOMWindow().getDocument();
    final DOMXPathEvaluator evaluator = document.getXPathEvaluator();
    // get the position of the selected DOM node by adding a
    // positional predicate to the given OXPath expression
    final DOMXPathResult result = evaluator.evaluate(xpath, document, evaluator.createNSResolver(document),
        DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);

    final List<DOMNode> l = Lists.newArrayList();
    if (result != null) {
      final long length = result.getSnapshotLength();
      for (int i = 0; i < length; i++)
        l.add(result.snapshotItem(i));
    }
    return l;
  }

  /**
   * Gets the first node returned by an xpath expression, or null if any
   * 
   * @param xpath
   *          the OXPath expression
   * @param node
   *          the DOM node
   * @return the first node
   */
  public static List<DOMNode> getNodes(final String xpath, final DOMNode contextNode) {
    // final DOMDocument document = browser.getContentDOMWindow().getDocument();
    final DOMXPathEvaluator evaluator = contextNode.getXPathEvaluator();
    // get the position of the selected DOM node by adding a
    // positional predicate to the given OXPath expression
    final DOMXPathNSResolver createNSResolver = evaluator.createNSResolver(contextNode);
    final DOMXPathResult result = evaluator.evaluate(xpath, contextNode, createNSResolver,
        DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);

    final List<DOMNode> l = Lists.newArrayList();
    if (result != null) {
      final long length = result.getSnapshotLength();
      for (int i = 0; i < length; i++)
        l.add(result.snapshotItem(i));
    }
    return l;
  }

  public static List<DOMNode> getNodes(final String xpath, final DOMNode node, final WebBrowser browser) {
    return getNodes(xpath, node);
  }

  public static List<DOMNode> getNodes(final Iterable<String> xpathExpressions, final WebBrowser browser) {
    final StringBuilder b = new StringBuilder();
    for (final String string : xpathExpressions)
      b.append("(" + string + ")|");
    b.append("true");
    return getNodes(b.toString(), browser);
  }

  public static boolean isInvalidName(final String nodeName) {
    return ((!XMLChar.isValidName(nodeName)) || (nodeName.contains(":")));
  }

  public static boolean checkUniqueMatchingPaths(final DOMNode from, final DOMNode to,
      final XPathNodePointerRanking paths) {
    boolean result = true;
    for (final XPathNodePointer pointer : paths) {
      final String fromPath = XPathFinderByCanonical.getCanonicalXPath(from).getXPath();
      final String toPath = pointer.getXPath();
      final DOMDocument document;
      if (from.getNodeType() == DOMNode.Type.DOCUMENT)
        document = ((DOMDocument) from);
      else
        document = from.getOwnerDocument();
      final WebBrowser browser = document.getEnclosingWindow().getBrowser();
      final List<DOMNode> shouldBeToList = XPathUtil.getNodes(fromPath + toPath, browser);
      if (shouldBeToList.size() != 1) {
        result = false;
        LOGGER.error("XPath '" + fromPath + "'+'" + toPath
            + "' does not identify node '{}' uniquely, but delivers '{}'.", to, shouldBeToList);
      } else {
        final DOMNode shouldBeTo = shouldBeToList.iterator().next();
        if (!shouldBeTo.equals(to)) {
          result = false;
          LOGGER.error("XPath '" + fromPath + "'+'" + toPath + "' does not identify node '{}' but delivers '{}'.", to,
              shouldBeToList);
        }
      }
    }
    return result;
  }

  public static XPathNodePointerRanking uniqueMatchingPaths(final DOMNode from, final DOMNode to,
      final XPathNodePointerRanking paths) {
    final XPathNodePointerRanking result = XPathNodePointerRankingOnSet.newRank();

    for (final XPathNodePointer pointer : paths) {
      final String fromPath = XPathFinderByCanonical.getCanonicalXPath(from).getXPath();
      final String toPath = pointer.getXPath();
      final DOMDocument document;
      if (from.getNodeType() == DOMNode.Type.DOCUMENT)
        document = ((DOMDocument) from);
      else
        document = from.getOwnerDocument();
      final WebBrowser browser = document.getEnclosingWindow().getBrowser();
      final List<DOMNode> shouldBeToList = XPathUtil.getNodes(fromPath + toPath, browser);
      if (shouldBeToList.size() != 1)
        LOGGER.info("XPath '" + fromPath + "'+'" + toPath
            + "' does not identify node '{}' uniquely, but delivers '{}'.", to, shouldBeToList);
      else {
        final DOMNode shouldBeTo = shouldBeToList.iterator().next();
        if (!shouldBeTo.equals(to))
          LOGGER.info("XPath '" + fromPath + "'+'" + toPath + "' does not identify node '{}' but delivers '{}'.", to,
              shouldBeToList);
        else
          result.add(pointer);
      }
    }
    return result;
  }

}
