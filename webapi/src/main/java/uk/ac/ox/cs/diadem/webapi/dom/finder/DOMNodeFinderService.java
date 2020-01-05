/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.finder;

import java.util.Comparator;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathNodePointer.Type;

/**
 * Utility class
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class DOMNodeFinderService {
//  static XMLConfiguration configuration = ConfigurationObject.getConfiguration("uk/ac/ox/cs/diadem/webapi/Configuration.xml");
	
  public enum Score {

    MAX(1000), HIGH(400), MID(180), LOW(100); // TODO are this score definitions useful? Should we not use simple
                                              // constants for that purpose? Why restricting the specifiable thresholds
                                              // to four values?
    private int threshold;

    Score(final int thresold) {
      threshold = thresold;
    }

    public int getThresold() {
      return threshold;
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(DOMNodeFinderService.class);

  public static XPathNodePointerRanking computeRobustPointers(final DOMNode target) {
    return computeAllPointers(target, null, "", ScoreDefinitions.MAX_AXIS_STEPS);

  }

  public static XPathNodePointer computeBestPointer(final DOMNode target) {
    return computeAllPointers(target, null, "", ScoreDefinitions.MAX_AXIS_STEPS).first();
  }

  public static XPathNodePointerRanking computeAllPointers(final DOMNode target, final DOMNode rootNode,
      final String rootPath, final int maxSteps) {
    final XPathNodePointerRanking candidates = XPathNodePointerRankingOnSet.newRank();
    candidates.add(getCanonicalXPath(target));
    candidates.addAll(XPathFinderByAttribute.getAllXPathPointers(target, rootPath, Score.MAX.getThresold()));
    candidates.addAll(XPathFinderByAnchor.getAllXPathPointers(target, rootNode, rootPath, maxSteps,
        Score.MAX.getThresold()));
    candidates.addAll(XPathFinderByPosition.getAllXPathPointers(target, rootPath, Score.MAX.getThresold()));
    candidates.addAll(XPathFinderByTextContent.getAllXPathPointers(target, rootPath, Score.MAX.getThresold()));
    // the original method removes duplicated expressions replacing them with only one with score equals to the average
    // we keep all of them as we are interested in the top ranked

    return candidates;
  }

  public static XPathNodePointerRanking computeAllPointersExtended(final DOMNode target, final DOMNode rootNode,
      final String rootPath, final int maxSteps) {
    final XPathNodePointerRanking candidates = XPathNodePointerRankingOnSet.newRank();
    candidates.add(XPathFinderByCanonical.getCanonicalXPath(target, rootNode));
    LOGGER.trace("Found canonical path '{}'", candidates);

    final XPathNodePointerRanking attributePaths = XPathFinderByAttribute.getAllXPathPointers(target, rootPath,
        Score.MAX.getThresold());
    candidates.addAll(attributePaths);
    LOGGER.trace("Found attribute {} paths '{}'.", attributePaths.size(), attributePaths);

    // extension
    final XPathNodePointerRanking anchorPaths = XPathFinderByAnchorExtended.getAllXPathPointers(target, rootNode,
        rootPath, maxSteps, Score.MAX.getThresold());
    candidates.addAll(anchorPaths);
    LOGGER.trace("Found anchor {} paths '{}'.", anchorPaths.size(), anchorPaths);

    // candidates.addAll(XPathFinderByAnchor.getAllXPathPointers(target, rootNode, rootPath, maxSteps,
    // Score.MAX.getThresold()));

    // XPathNodePointerRanking extended = XPathFinderByAnchorExtended.getAllXPathPointers(target, rootNode, rootPath,
    // maxSteps, Score.MAX.getThresold());
    // XPathNodePointerRanking plain = XPathFinderByAnchor.getAllXPathPointers(target, rootNode, rootPath, maxSteps,
    // Score.MAX.getThresold());
    //
    // LOGGER.trace("EXTENDED: {}", extended);
    // LOGGER.trace("PLAIN   : {}", plain);
    // plain.removeAll(extended);
    // LOGGER.trace("PLAIN-EXTENDED: {}", plain);

    final XPathNodePointerRanking positionPaths = XPathFinderByPosition.getAllXPathPointers(target, rootPath,
        Score.MAX.getThresold());
    candidates.addAll(positionPaths);
    LOGGER.trace("Found position {} paths '{}'.", positionPaths.size(), positionPaths);

    final XPathNodePointerRanking textPaths = XPathFinderByTextContent.getAllXPathPointers(target, rootPath,
        Score.MAX.getThresold());
    candidates.addAll(textPaths);
    LOGGER.trace("Found text {} paths '{}'.", textPaths.size(), textPaths);

    // the original method removes duplicated expressions replacing them with only one with score equals to the average
    // we keep all of them as we are interested in the top ranked

    return candidates;
  }

  static XPathNodePointerRanking computeRankingUsingTypes(final DOMNode target, final DOMNode rootNode,
      final String rootPath, final int maxSteps, final EnumSet<XPathNodePointer.Type> types, final Integer threshold) {
    final XPathNodePointerRanking candidates = XPathNodePointerRankingOnSet.newRank();
    if (types.contains(Type.CANONICAL))
      candidates.add(getCanonicalXPath(target));
    if (types.contains(Type.ATTRIBUTE))
      candidates.addAll(XPathFinderByAttribute.getAllXPathPointers(target, rootPath, threshold));
    if (types.contains(Type.ANCHOR))
      candidates.addAll(XPathFinderByAnchor.getAllXPathPointers(target, rootNode, rootPath, maxSteps, threshold));
    if (types.contains(Type.POSITION))
      candidates.addAll(XPathFinderByPosition.getAllXPathPointers(target, rootPath, threshold));
    if (types.contains(Type.TEXT))
      candidates.addAll(XPathFinderByTextContent.getAllXPathPointers(target, rootPath, threshold));
    // the original method removes duplicated expressions replacing them with only one with score equals to the average
    // we keep all of them as we are interested in the top ranked

    return candidates;
  }

  // TODO to disappear
  public static XPathNodePointerRanking computeRankingUsingTypesExtended(final DOMNode target, final DOMNode rootNode,
      final String rootPath, final int maxSteps, final EnumSet<XPathNodePointer.Type> types, final Integer threshold) {
    LOGGER.trace("computeRankingUsingTypesExtended taget='{}', rootNode='{}', rootPath='" + rootPath + "', maxSteps="
        + maxSteps + ", type='" + types + "', threshold=" + threshold + ".", target, rootNode);
    final XPathNodePointerRanking candidates = XPathNodePointerRankingOnSet.newRank();
    if (types.contains(Type.CANONICAL))
      candidates.add(XPathFinderByCanonical.getCanonicalXPath(target, rootNode));
    if (types.contains(Type.ATTRIBUTE))
      candidates.addAll(XPathFinderByAttribute.getAllXPathPointers(target, rootPath, threshold));
    if (types.contains(Type.ANCHOR))
      candidates.addAll(XPathFinderByAnchorExtended
          .getAllXPathPointers(target, rootNode, rootPath, maxSteps, threshold));
    if (types.contains(Type.POSITION))
      candidates.addAll(XPathFinderByPosition.getAllXPathPointers(target, rootPath, threshold));
    if (types.contains(Type.TEXT)) {
      final XPathNodePointerRanking textPaths = XPathFinderByTextContent.getAllXPathPointers(target, rootPath,
          threshold);

      for (final XPathNodePointer pointer : textPaths)
        if (candidates.contains(pointer))
          LOGGER.warn("Path '{}' already contained.", pointer);

      candidates.addAll(textPaths);
    }
    // the original method removes duplicated expressions replacing them with only one with score equals to the average
    // we keep all of them as we are interested in the top ranked

    LOGGER.trace("computeRankingUsingTypesExtended done '{}'.", candidates);
    return candidates;
  }

  /**
   * Gets the absolute canonical XPath expression for a DOM node.
   * 
   * @param target
   *          the target node
   * @return an {@link XPathNodePointer} object representing the canonical xpath and its robustness score
   */
  public static XPathNodePointer getCanonicalXPath(final DOMNode target) {
    return XPathFinderByCanonical.getCanonicalXPath(target);
  }

  public static XPathNodePointer getCanonicalXPath(final DOMNode from, final DOMNode target) {
    final XPathNodePointer fromPath = XPathFinderByCanonical.getCanonicalXPath(from);
    final XPathNodePointer targetPath = XPathFinderByCanonical.getCanonicalXPath(target);
    if (targetPath.getXPath().startsWith(fromPath.getXPath()))
      return new XPathNodePointerImpl(targetPath.getXPath().substring(fromPath.getXPath().length()),
          targetPath.getScore(), targetPath.getType());
    assert false : "target '" + target + "' is not an ancestor of from '" + from + "'.";
    return null;
  }

  /**
   * Gets a {@link XPathNodePointerRanking} (sorted set of {@link XPathNodePointer}) for the target node, using
   * attributes as main criteria for identification. The result set is sorted by robustness as defined by our framework.
   * In case of text node, it uses its parent to perform the evaluation but still addressing the text node as in:
   * ./a[@id='xx']/text()[1]
   * 
   * @param target
   *          the target element
   * @return a {@link XPathNodePointerRanking} for the target element, using attributes as main criteria for
   *         identification.
   */
  public static XPathNodePointerRanking getXPathPointersByAttribute(final DOMNode target) {
    return XPathFinderByAttribute.getAllXPathPointers(target, Score.MAX.getThresold());
  }

  /**
   * Same as {@link DOMNodeFinderService#getXPathPointersByAttribute(DOMNode)} but with a threshold that stops the
   * process as soon as the first expression above the threshold if found
   * 
   * @param target
   * @param score
   * @return
   */
  public static XPathNodePointerRanking getXPathPointersByAttribute(final DOMNode target, final Integer threshold) {
    return XPathFinderByAttribute.getAllXPathPointers(target, threshold);
  }

  /**
   * Gets a {@link XPathNodePointerRanking} (sorted set of {@link XPathNodePointer}) for the target node, using position
   * (not canonical xpath) as main criteria for identification. The result set is sorted by robustness as defined by our
   * framework. In case of text node, it uses its parent to perform the evaluation but still addressing the text node as
   * in: ./a[@id='xx']/text()[1]
   * 
   * @param target
   *          the target element
   * @return a {@link XPathNodePointerRanking} for the target element, using position as main criteria for
   *         identification.
   */
  public static XPathNodePointerRanking getXPathPointersByPosition(final DOMNode target) {
    return XPathFinderByPosition.getAllXPathPointers(target, "", Score.MAX.getThresold());
  }

  /**
   * Same as {@link DOMNodeFinderService#getXPathPointersByAttribute(DOMNode)} but with a threshold that stops the
   * process as soon as the first expression above the threshold if found
   * 
   * @param target
   *          the target element
   * @param threshold
   * @return a {@link XPathNodePointerRanking} for the target element, using position as main criteria for
   *         identification.
   */
  public static XPathNodePointerRanking getXPathPointersByPosition(final DOMNode target, final Integer threshold) {
    return XPathFinderByPosition.getAllXPathPointers(target, "", threshold);
  }

  /**
   * Gets a {@link XPathNodePointerRanking} (sorted set of {@link XPathNodePointer}) for the target element, using its
   * text children nodes as main criteria for identification. The result list is sorted by robustness as defined by our
   * framework
   * 
   * @param target
   *          the target node
   * @return a {@link XPathNodePointerRanking} for the target element, using text clues as main criteria for
   *         identification.
   */
  public static XPathNodePointerRanking getXPathPointersByTextContent(final DOMNode target) {
    return getXPathPointersByTextContent(target, Score.MAX.getThresold());
  }

  /**
   * As {@link DOMNodeFinderService#getXPathPointersByTextContent(DOMElement)} but with a threshold for robusteness
   * 
   * @param node
   *          the target node
   * @param threshold
   * @return a {@link XPathNodePointerRanking} for the target element, using text clues as main criteria for
   *         identification.
   */
  public static XPathNodePointerRanking getXPathPointersByTextContent(final DOMNode node, final Integer threshold) {
    return XPathFinderByTextContent.getAllXPathPointers(node, threshold);
  }

  /**
   * prevents instantiation
   */
  private DOMNodeFinderService() {
    // prevent instantiation
  }

  /**
   * Gets a {@link XPathNodePointerRanking} (sorted set of {@link XPathNodePointer}) for the target node, using anchor
   * nodes as main criteria for identification. The result set is sorted by robustness as defined by our framework. In
   * case of text node, it uses its parent to perform the evaluation but still addressing the text node as in:
   * ./a[@id='xx']/text()[1]
   * 
   * @param target
   *          the target element
   * @return a {@link XPathNodePointerRanking} for the target element, using position as main criteria for
   *         identification.
   */
  public static XPathNodePointerRanking getXPathPointersByAnchor(final DOMNode target) {
    return XPathFinderByAnchor.getAllXPathPointers(target, null, "", ScoreDefinitions.MAX_AXIS_STEPS,
        Score.MAX.getThresold());
  }

  /**
   * Same as {@link DOMNodeFinderService#getXPathPointersByAnchor(DOMNode)} but with a threshold that stops the process
   * as soon as the first expression above the threshold if found
   * 
   * @param target
   *          the target element
   * @param threshold
   *          the threshold value
   * @return a {@link XPathNodePointerRanking} for the target element, using position as main criteria for
   *         identification.
   */
  public static XPathNodePointerRanking getXPathPointersByAnchor(final DOMNode target, final Integer threshold) {
    return XPathFinderByAnchor.getAllXPathPointers(target, null, "", ScoreDefinitions.MAX_AXIS_STEPS, threshold);
  }

//  /**
//   * Returns the start-based ID for the given node. If the domFactFinder project is loaded, uses the configured id
//   * prefix. Otherwise "e_" or "t_" (for text nodes). Unfortunately, very brittle. Please use with care!
//   * 
//   * @Deprecated
//   */
//  @Deprecated
//  public static String getIdForNode(final WebBrowser browser, final DOMNode node, final boolean skipGarbage) {
//    final String prefix = node.isTextNode() ? configuration.getString("facts.dom.html-text.idprefix", "t_") : 
//    	configuration.getString("facts.dom.html-element.idprefix", "e_");
//    LOGGER.trace("Computing ID for element {} ", node); // -------------------------
//    final DOMXPathEvaluator e = browser.getOXPathEvaluator();
//    final DOMDocument document = browser.getContentDOMWindow().getDocument();
//    final String xpath = "count(ancestor::node()) + 2 * count(preceding::* | preceding::text(){0})";
//    final String predicate = skipGarbage ? "[not(normalize-space(.) = '' or normalize-space(.) = '\u00A0')]" : "";
//
//    final DOMXPathResult r = e.evaluate(MessageFormat.format(xpath, predicate), node, e.createNSResolver(document),
//        DOMXPathResult.NUMBER_TYPE, null);
//    String id = prefix + Math.round(r.getNumberValue());
//    if (!node.isTextNode())
//      id = id + '_' + node.getLocalName();
//    return id;
//  }

//  @Deprecated
//  public static Comparator<String> getComparatorBasedOnStart() {
//
//    return new Comparator<String>() {
//
//      @Override
//      public int compare(final String o1, final String o2) {
//        final Iterator<String> iterator = Splitter.on('_').trimResults().omitEmptyStrings().split(o1).iterator();
//        iterator.next();
//        final int start = Integer.parseInt(iterator.next());
//
//        final Iterator<String> iteratorOther = Splitter.on('_').trimResults().omitEmptyStrings().split(o2).iterator();
//        iteratorOther.next();
//        final int startO = Integer.parseInt(iteratorOther.next());
//        return start - startO;
//      }
//    };
//  }

  public static Comparator<Long> getComparatorBasedOnStartEncoding() {

    return new Comparator<Long>() {

      @Override
      public int compare(final Long o1, final Long o2) {

        return (int) (o1 - o2);
      }
    };
  }

  public static Comparator<DOMNode> getComparatorBasedOnDocumentPosition() {
    return new Comparator<DOMNode>() {

      @Override
      public int compare(final DOMNode n1, final DOMNode n2) {
        // compare position of DOMNodes

        // if equals we return 0, althugh compareto might do the same as well.
        if (n1.equals(n2))
          return 0;
        // FROM HERE ON is OXPATH CODE
        short position;
        try {
          position = n1.compareDocumentPosition(n2);
          // System.out.println(n1.getNodeName() + " : " + n2.getNodeName() + " = " + position);
        } catch (final NullPointerException e) {// in case the notional context is being compared to something
          position = DOCUMENT_POSITION_DISCONNECTED;// not if both null, so as to be consistent with equals
        }

        // This seems counterintuitive, but this is what we want based on Java's definition of "natural ordering" - for
        // us,
        // we assume document order to be the natural ordering, so preceding nodes have higher ordering values
        if (position == 0)
          return compare(n1.getParentNode(), n2.getParentNode());
        else if (((position & DOCUMENT_POSITION_PRECEDING) == DOCUMENT_POSITION_PRECEDING)
            || ((position & DOCUMENT_POSITION_CONTAINS) == DOCUMENT_POSITION_CONTAINS))
          return 1;
        else if (((position & DOCUMENT_POSITION_FOLLOWING) == DOCUMENT_POSITION_FOLLOWING)
            || ((position & DOCUMENT_POSITION_CONTAINED_BY) == DOCUMENT_POSITION_CONTAINED_BY))
          return -1;
        // else return Integer.MAX_VALUE;
        else if ((position & DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC) == DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC) {

          // in order to establish a consistent total ordering (albeit an arbitrary one), we use the hash values of the
          // containing documents
          // final int diff = n1.getOwnerDocument().hashCode() - n2.getOwnerDocument().hashCode();
          // EDIT:@Gio we use the hashcode of the nodes, as it is possible to have disconnected nodes on the same
          // document
          final int diff = n1.hashCode() - n2.hashCode();
          if (diff != 0)
            return diff;
          else
            throw new RuntimeException("Browser returned the same Document hashcode for disconnected nodes.");
        } else
          throw new RuntimeException("Browser broke compareDocumentPosition contract with return value");
      }
    };
  }

  // DocumentPosition constants
  /**
   * Document position constants
   */
  private static final short DOCUMENT_POSITION_DISCONNECTED = 0x01;
  /**
   * Document position constants
   */
  private static final short DOCUMENT_POSITION_PRECEDING = 0x02;
  /**
   * Document position constants
   */
  private static final short DOCUMENT_POSITION_FOLLOWING = 0x04;
  /**
   * Document position constants
   */
  private static final short DOCUMENT_POSITION_CONTAINS = 0x08;
  /**
   * Document position constants
   */
  private static final short DOCUMENT_POSITION_CONTAINED_BY = 0x10;
  /**
   * Document position constants
   */
  private static final short DOCUMENT_POSITION_IMPLEMENTATION_SPECIFIC = 0x20;

}
