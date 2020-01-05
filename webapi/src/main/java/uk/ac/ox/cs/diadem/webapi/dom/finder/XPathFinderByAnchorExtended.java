package uk.ac.ox.cs.diadem.webapi.dom.finder;

import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathNodePointer.Type;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

/**
 * The XPathFinderByAnchor searches for suitable anchor nodes.
 * <p>
 * 
 * The search moves through the tree for a maximum number of steps and evaluates all nodes for their robustness. For a
 * maximum number of scores above a minimum score threshold, the combined expression scores are calculated as the sum of
 * the anchor score and the score of the relative path from the anchor to the target node, both multiplied with weights
 * specified in ScoreDefinitions. Before the multiplication, the anchor score is adjusted based on the distance to the
 * target and the axis between them.
 * <p>
 * 
 * After all expressions have been scored, the XPathFinderByAnchor passes the list of OXPathCandidates to the
 * OXPathGeneralizer. The OXPathGeneralizer prunes location steps, where reasonable, and adjusts the score based on
 * rules set in ScoreDefinitions.
 * 
 * @author JochenK
 * 
 * @see OXPathCandidate OXPathCandidate
 * @see OXPathGeneralizer OXPathGeneralizer
 * @see ScoreDefinitions ScoreDefinitions
 */
class XPathFinderByAnchorExtended {
  private static final char SINGLEQUOTE = '\'';
  private static final char DOUBLEQUOTE = '"';
  private static final Logger LOGGER = LoggerFactory.getLogger(XPathFinderByAnchorExtended.class);
  private static final int MAX_FOLLOWING_SIBLING_STEPS = 8;
  private static final int MAX_PREVIOUS_SIBLING_STEPS = 8;

  /**
   * Gets the candidate list of scored OXPath expressions for a DOM node.
   * 
   * @param node
   *          the DOM node
   * @param rootNode
   *          the root node
   * @param rootPath
   *          the root path
   * @param maxAxisSteps
   *          the maximum number of steps when finding an anchor in the DOM tree
   * @return the OXPath candidate list
   */
  public static XPathNodePointerRanking getAllXPathPointers(final DOMNode node, final DOMNode rootNode,
      final String rootPath, final int maxAxisSteps, final int threshold) {
    LOGGER.trace("getAllXPathPointers node='{}', rootNode='{}', rootPath='" + rootPath + "', maxAxisSteps='"
        + maxAxisSteps + "' threshold='" + threshold + "'.", node, rootNode);
    final XPathNodePointerRanking targetCandidateList = XPathNodePointerRankingOnSet.newRank();

    final DOMElement targetElement = FinderUtils.castToElementOrParentOrNull(node);
    if (targetElement != null) {
      // find an OXPath expression through anchors on the parent axis, then
      // generalize the expression by purging location steps
      final XPathNodePointerRanking anchorPaths = findOXPathWithPositionWeight(rootNode, rootPath, targetElement,
          maxAxisSteps, threshold);
      LOGGER.debug("Found initial path {}", anchorPaths);
      targetCandidateList.addAll(anchorPaths);
      if (FinderUtils.checkThreshold(targetCandidateList, threshold))
        return FinderUtils.manageTextNodeIfAny(node, targetCandidateList);

      final XPathNodePointerRanking purged = purgeLocationSteps(anchorPaths, targetElement, rootPath);
      LOGGER.debug("Purged location steps to obtain path {}", purged);
      targetCandidateList.addAll(purged);
    }
    final XPathNodePointerRanking result = FinderUtils.manageTextNodeIfAny(node, targetCandidateList);
    LOGGER.trace("getAllXPathPointers done '{}'", result);
    return result;
  }

  /**
   * Generate OXPath expressions through anchors on the parent axis. Include the position weight into the robustness
   * score.
   * 
   * @param targetCandidateList
   *          the target candidate list
   * @param rootNode
   *          the root node
   * @param rootPath
   *          the root path
   * @param targetElement
   *          the target element
   * @param maxAxisSteps
   *          the maximum number of steps when finding an anchor in the DOM tree
   * @return true, if successful
   */
  private static XPathNodePointerRanking findOXPathWithPositionWeight(final DOMNode rootNode, final String rootPath,
      final DOMElement targetElement, final int maxAxisSteps, final int threshold) {
    assert targetElement != null;
    assert rootNode != null;
    DOMNode node = targetElement.getParentNode();
    while (!node.equals(rootNode))
      if ((node = node.getParentNode()) == null)
        break;
    assert node != null : "targetElement must be a (non-equal) descendant of rootNode";
    // the second condition is redundant to the first
    assert node.equals(rootNode) : "violated invariance.";

    final XPathNodePointerRanking result = XPathNodePointerRankingOnSet.newRank();

    // anchor via child axis
    final DOMNode parentNode = targetElement.getParentNode();
    if (parentNode != null) {
      String anchorToTargetPath = "/" + FinderUtils.elementName(targetElement);
      result.addAll(findOXPathViaAnchor(rootNode, rootPath, targetElement, parentNode, anchorToTargetPath,
          maxAxisSteps, threshold));
    }
    LOGGER.trace("Ranking after child axis: '{}'.", result);

    // anchor via following-sibling axis
    DOMNode leftSiblingNode = targetElement.getPreviousSibling();
    for (int i = 0; i < MAX_FOLLOWING_SIBLING_STEPS; ++i) {
      if (leftSiblingNode == null)
        break;

      if (leftSiblingNode.getNodeType() == DOMNode.Type.ELEMENT) {
        final String anchorToTargetPath = "/following-sibling::" + FinderUtils.elementName(targetElement);
        result.addAll(findOXPathViaAnchor(rootNode, rootPath, targetElement, leftSiblingNode, anchorToTargetPath,
            maxAxisSteps, threshold));
      }
      leftSiblingNode = leftSiblingNode.getPreviousSibling();
    }
    LOGGER.trace("Ranking after following-sibling axis: '{}'.", result);

    // anchor via preceding-sibling axis
    DOMNode rightSiblingNode = targetElement.getNextSibling();
    for (int i = 0; i < MAX_PREVIOUS_SIBLING_STEPS; ++i) {
      if (rightSiblingNode == null)
        break;

      if (rightSiblingNode.getNodeType() == DOMNode.Type.ELEMENT) {
        final String anchorToTargetPath = "/preceding-sibling::" + FinderUtils.elementName(targetElement);
        result.addAll(findOXPathViaAnchor(rootNode, rootPath, targetElement, rightSiblingNode, anchorToTargetPath,
            maxAxisSteps, threshold));
      }
      rightSiblingNode = rightSiblingNode.getNextSibling();
    }
    LOGGER.trace("Ranking after preceding-sibling axis: '{}'.", result);

    return result;
  }

  private static XPathNodePointerRanking findOXPathViaAnchor(final DOMNode rootNode, final String rootPath,
      final DOMElement targetNode, final DOMNode anchorNode, String anchorToTargetPath, final int maxAxisSteps,
      final int threshold) {
    LOGGER.trace("findOXPathViaAnchor rootNode='{}', rootPath='{}', anchorNode='" + anchorNode + "', targetElement='"
        + targetNode + "', anchorToTargetPath=" + anchorToTargetPath, rootNode, rootPath);

    final XPathNodePointerRanking result = XPathNodePointerRankingOnSet.newRank();

    // We ignore null anchor nodes, cases when anchor and rootNode are identical, when maxAxisSteps == 0, or when the
    // anchorNode is the html or body element.
    if ((anchorNode == null) || anchorNode.equals(rootNode) || (maxAxisSteps == 0))
      return result; // TODO do we return the empty ranking?
    final String canonicalPathToAnchorNode = DOMNodeFinderService.getCanonicalXPath(anchorNode).getXPath();
    if (canonicalPathToAnchorNode.equals("/html[1]") || canonicalPathToAnchorNode.equals("/html[1]/body[1]"))
      return result;

    // determining the browser where anchorNode lives
    final WebBrowser browser;
    final DOMDocument document;
    if (anchorNode.getNodeType() == DOMNode.Type.DOCUMENT)
      document = ((DOMDocument) anchorNode);
    else
      document = anchorNode.getOwnerDocument();
    browser = document.getEnclosingWindow().getBrowser();

    // provisional path to targetNode via anchorNode (no positional qualification, using a canonical path to the anchor)
    final String provisionalRootToTargerPath = rootPath
        + XPathFinderByCanonical.getCanonicalXPath(anchorNode, rootNode).getXPath() + anchorToTargetPath;
    LOGGER.trace("provisionalRootToTargetPath='{}'", provisionalRootToTargerPath);

    // adding a positional predicate
    // final List<DOMNode> listWithTarget = XPathUtil.getNodes(provisionalRootToTargerPath, browser);
    // int position = 1;
    // for (final DOMNode node : listWithTarget)
    // if (node.isEqualNode(targetNode))
    // break;
    // else
    // ++position;
    // final int count = listWithTarget.size();
    final int count = XPathUtil.getNodes(provisionalRootToTargerPath, browser).size();
    int position = XPathUtil.retrieveNodePosition(provisionalRootToTargerPath, targetNode, count);
    if (count == 0)
      LOGGER
          .error("provisionalRootToTargetPath='" + provisionalRootToTargerPath + "' does not match any node.", LOGGER);
    else
      LOGGER.debug("provisionalRootToTargetPath: count={}, position={}", count, position);

    // TODO this goes potentially wrong: it might select multiple nodes in after a //tag -- so we could go for
    // (//tag)[pos] or not. Reasons for not doing so: more robustness with the subsequent expression.
    if ((count == position) && (position > 1)) {
      anchorToTargetPath += "[last()]";
      position = 1; // for score computation
    } else
      anchorToTargetPath += "[" + position + "]";

    // computing the scores (didn't look at)
    final int relativePathScore = ScoreDefinitions.getRelativePositionScore(position, count,
        ScoreDefinitions.POSITION_ROOT_SCORE);

    // computing paths to the anchor node
    final XPathNodePointerRanking rootToAnchorPaths = DOMNodeFinderService.computeRankingUsingTypesExtended(anchorNode,
        rootNode, rootPath, maxAxisSteps - 1, EnumSet.allOf(Type.class), threshold);

    // ignore the canonical path to the anchor TODO taken over from original source -- drop canonical.
    // pathsToAnchorNode.remove(pathsToAnchorNode.getCanonicalPointer());

    // calculate the robustness scores and update the candidate list
    int candidatesAdded = 0;
    for (final XPathNodePointer rootToAnchorPath : rootToAnchorPaths) {
      final int score = Math.round((rootToAnchorPath.getScore() * ScoreDefinitions.PARENT_AXIS_WEIGHT)
          + (relativePathScore * ScoreDefinitions.RELATIVE_PATH_WEIGHT));
      if ((score < ScoreDefinitions.MIN_RELATIVE_SCORE)
          || (candidatesAdded > ScoreDefinitions.MAX_RELATIVE_EXPRESSIONS))
        break;
      final String rootToTargetPath = rootToAnchorPath.getXPath() + anchorToTargetPath;
      result.add(new XPathNodePointerImpl(rootToTargetPath, score, Type.ANCHOR));
      ++candidatesAdded;
    }

    final XPathNodePointerRanking purgedResults = XPathUtil.uniqueMatchingPaths(rootNode, targetNode, result);
    LOGGER.trace("findOXPathViaAnchor done: '{}'", purgedResults);
    return purgedResults;
  }

  /**
   * Purge location steps, as long as the generated OXPath expression still selects the same DOM node.
   * 
   * @param targetCandidateList
   *          the list of OXPath candidates
   * @param targetElement
   *          the addressed DOM element
   * @param rootPath
   *          the root path for relative expressions
   */
  static XPathNodePointerRanking purgeLocationSteps(final XPathNodePointerRanking targetCandidateList,
      final DOMElement targetElement, final String rootPath) {
    final XPathNodePointerRanking optimizedCandidateList = XPathNodePointerRankingOnSet.newRank();
    for (final XPathNodePointer candidateNode : targetCandidateList) {
      final String longPath = candidateNode.getXPath();
      int removedLocationSteps = 0;

      // start directly after the anchor node
      int anchorIndex = indexOfOnMainPath(longPath, '/');
      if (anchorIndex >= 0) {
        anchorIndex = longPath.indexOf("/", anchorIndex + 2);
        if (anchorIndex >= 0) {
          final String anchor = longPath.substring(0, anchorIndex) + "/";
          if (!anchor.equals("/html[1]/")) {
            String tail = longPath.substring(anchorIndex);
            int tailIndex = indexOfFromOnMainPath(tail, '/', 1);

            // remove as many location steps as possible
            while ((tailIndex >= 0) && (removedLocationSteps < ScoreDefinitions.MAX_STEP_REMOVALS)) {
              removedLocationSteps++;
              tail = tail.substring(tailIndex);
              final String shortPath = anchor + tail;

              // check if the generated OXPath expression still
              // selects the same DOM node
              final int count = XPathUtil.count(rootPath + shortPath, targetElement.getOwnerDocument()
                  .getEnclosingWindow().getBrowser());
              if (count == 1) {
                final DOMNode resultNode = XPathUtil.getFirstNode(rootPath + shortPath, targetElement
                    .getOwnerDocument().getEnclosingWindow().getBrowser());
                if (targetElement.equals(resultNode)) {

                  // adjust score based on the number of
                  // removed location steps
                  final int score = candidateNode.getScore()
                      + (removedLocationSteps * ScoreDefinitions.REMOVE_STEP_BONUS);
                  optimizedCandidateList.add(new XPathNodePointerImpl(shortPath, score, Type.GENERALIZER));
                }
              }
              tailIndex = indexOfFromOnMainPath(tail, '/', 1);
            }
          }
        }
      }
    }
    // targetCandidateList.addAll(optimizedCandidateList);
    return optimizedCandidateList;
  }

  /**
   * computes the indexOf a char taking care of quoted text
   * 
   * @param longPath
   * @param slash
   * @param from
   * @return
   */
  private static int indexOfFromOnMainPath(final String longPath, final char slash, final int from) {
    if (!(longPath.contains("'") || longPath.contains("\"")))
      return longPath.indexOf(slash, from);
    else {
      boolean inQuoteState = false;
      boolean inDoubleQuoteState = false;
      for (int i = from; i < longPath.length(); i++) {

        final char c = longPath.charAt(i);
        if (c == SINGLEQUOTE)
          if (!inDoubleQuoteState)
            inQuoteState = !inQuoteState;
        if (c == DOUBLEQUOTE)
          if (!inQuoteState)
            inDoubleQuoteState = !inDoubleQuoteState;
        if (c == slash)
          if (!(inDoubleQuoteState || inQuoteState))
            return i;
      }
      return -1;
    }
  }

  private static int indexOfOnMainPath(final String longPath, final char slash) {
    return indexOfFromOnMainPath(longPath, slash, 0);
  }
}