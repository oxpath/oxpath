package uk.ac.ox.cs.diadem.webapi.dom.finder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathNodePointer.Type;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

/**
 * The {@link XPathFinderByPosition} addresses the node by its position in a hypothetical list of nodes.
 * <p>
 * 
 * Depending on the expression bracketing, the positional predicate applies to all elements of the same local name
 * either in the whole tree or among siblings only. For both cases, a different score is specified in
 * {@link ScoreDefinitions}. The algorithm determines the length of the list and the position of the node in that list
 * and adjusts the score accordingly.
 * 
 * @author JochenK
 * @author Giovanni Grasso
 */
class XPathFinderByPosition {

  // /**
  // * Gets the candidate list of scored XPath expressions for a DOM node.
  // *
  // * @param node
  // * the DOM node
  // * @return the OXPath candidate list
  // */
  // private static List<XPathNodePointer> getXPathAddresses(final DOMNode node) {
  // return getXPathAddresses(node, "");
  // }

  private static final Logger LOGGER = LoggerFactory.getLogger(XPathFinderByPosition.class);

  public static XPathNodePointerRanking getAllXPathPointers(final DOMNode node, final Integer threshold) {
    return getAllXPathPointers(node, "", threshold);
  }

  /**
   * Gets the candidate list of scored XPath expressions for a DOM node.
   * 
   * @param node
   *          the DOM node
   * @param rootPath
   *          the root path
   * @return the OXPath candidate list
   */
  public static XPathNodePointerRanking getAllXPathPointers(final DOMNode node, final String rootPath,
      final Integer threshold) {
    LOGGER.trace("getAllXPathPointers node='{}', rootPath='{}', " + threshold + "'.", node, rootPath);
    // final List<XPathNodePointer> candidateList = new ArrayList<XPathNodePointer>();
    final XPathNodePointerRanking candidateList = XPathNodePointerRankingOnSet.newRank();
    final DOMElement element = FinderUtils.castToElementOrParentOrNull(node);
    if (element != null) {

      // find OXPath expressions both for local and global positional predicates
      final String desc_self_el = "//" + FinderUtils.elementName(element);
      candidateList.addAll(findXPathPointersByPosition(element, rootPath, desc_self_el,
          ScoreDefinitions.POSITION_PARENT_SCORE, threshold));
      if (FinderUtils.checkThreshold(candidateList, threshold))
        return FinderUtils.manageTextNodeIfAny(node, candidateList);
      else
        candidateList.addAll(findXPathPointersByPosition(element, rootPath, "(" + desc_self_el + ")",
            ScoreDefinitions.POSITION_ROOT_SCORE, threshold));
    }
    final XPathNodePointerRanking result = FinderUtils.manageTextNodeIfAny(node, candidateList);
    LOGGER.trace("getAllXPathPointers done '{}'", result);
    return result;
  }

  /**
   * Generate scored OXPath expressions based on the position of the DOM element in the DOM tree.
   * 
   * @param candidateList
   *          the candidate list
   * @param element
   *          the DOM element
   * @param rootPath
   *          the root path
   * @param oxPath
   *          the OXPath expression
   * @param relativeScore
   *          the robustness score
   * @return true, if successful
   */
  private static XPathNodePointerRanking findXPathPointersByPosition(final DOMNode element, final String rootPath,
      final String oxPath, final int relativeScore, final Integer threshold) {
    // final List<XPathNodePointer> candidateList = Lists.newArrayList();
    final XPathNodePointerRanking candidateList = XPathNodePointerRankingOnSet.newRank();
    if ((rootPath.trim().length() > 0) && oxPath.startsWith("("))
      return candidateList; // XPathNodePointerRankingOnSet.of(candidateList); // no valid OXPath possible
    final int count = XPathUtil.count(rootPath + oxPath, element.getOwnerDocument().getEnclosingWindow().getBrowser());
    if (count == 1)
      // unique OXPath expression => return directly
      candidateList.add(new XPathNodePointerImpl(oxPath, relativeScore, Type.POSITION));
    if (FinderUtils.checkThreshold(candidateList, threshold))
      return candidateList;

    if (count > 1)
      // non-unique OXPath expression => required positional predicate
      candidateList.addAll(XPathFinderByPosition.resolveXPathPositionForNode(element, rootPath, oxPath, relativeScore,
          count));
    return candidateList;// XPathNodePointerRankingOnSet.of(candidateList);
  }

  /**
   * Generate scored OXPath expressions based on the position of the DOM element in the DOM tree.
   * 
   * @param candidateList
   *          the candidate list
   * @param node
   *          the DOM element
   * @param rootPath
   *          the root path
   * @param oxPath
   *          the OXPath expression
   * @param score
   *          the robustness score
   * @param count
   *          the expression count
   * @return true, if successful
   */
  static XPathNodePointerRanking resolveXPathPositionForNode(final DOMNode node, final String rootPath,
      final String oxPath, final int score, final int count) {
    final XPathNodePointerRanking candidateList = XPathNodePointerRankingOnSet.newRank();
    String nodePath = oxPath;
    if (count > ScoreDefinitions.MAX_POSITION_COUNT)
      return candidateList; // no unique OXPath found
    if ((rootPath.trim().length() > 0) && nodePath.startsWith("("))
      return candidateList; // no valid OXPath possible

    // determine the position and append a positional predicate
    WebBrowser browser = node.getOwnerDocument().getEnclosingWindow().getBrowser();

    final String absoluteNodePath = rootPath + nodePath;
    {
      final String candidatePath = absoluteNodePath + "[last()]";
      if (matchedNodeUniquely(candidatePath, node, browser)) {
        final int newScore = ScoreDefinitions.getRelativePositionScore(1, count, score);
        candidateList.add(new XPathNodePointerImpl(candidatePath, newScore, Type.POSITION));
        return candidateList;
      }
    }
    for (int i = 1; i <= count; ++i) {
      final String candidatePath = absoluteNodePath + "[" + i + "]";
      if (matchedNodeUniquely(candidatePath, node, browser)) {
        final int newScore = ScoreDefinitions.getRelativePositionScore(i, count, score);
        candidateList.add(new XPathNodePointerImpl(candidatePath, newScore, Type.POSITION));
        return candidateList;
      }
    }
    // TODO might generate non-OXPath paths
    // for (int i = 1; i <= count; ++i) {
    // final String candidatePath = "(" + absoluteNodePath + ")[" + i + "]";
    // if (matchedNodeUniquely(candidatePath, node, browser)) {
    // final int newScore = ScoreDefinitions.getRelativePositionScore(i, count, score);
    // candidateList.add(new XPathNodePointerImpl(candidatePath, newScore, Type.POSITION));
    // return candidateList;
    // }
    // }
    // LOGGER.warn("no positional disambiguation found: node='{}', rootPath='{}', oxPath='" + oxPath + "', score=" +
    // score
    // + ", count=" + count + ".", node, rootPath);
    return candidateList;

    // int position = XPathUtil.retrieveNodePosition(rootPath + nodePath, node, count);
    // if (position > 0) {
    // if (count == position) {
    // LOGGER.trace("rootPath='" + rootPath + "', nodePath='" + nodePath + "', count={}, position={}.", count,
    // position);
    // nodePath += "[last()]";
    // position = 1;
    // } else
    // nodePath += "[" + position + "]";
    // if (XPathUtil.count(rootPath + nodePath, browser) == 1) {
    // final int newScore = ScoreDefinitions.getRelativePositionScore(position, count, score);
    // candidateList.add(new XPathNodePointerImpl(nodePath, newScore, Type.POSITION));
    // return candidateList;
    // }
    // }
    // return candidateList;
  }

  private static boolean matchedNodeUniquely(final String xpath, final DOMNode node, final WebBrowser browser) {
    final List<DOMNode> nodes = XPathUtil.getNodes(xpath, browser);
    if (nodes.size() == 1)
      return node.equals(nodes.get(0));
    return false;
  }
}