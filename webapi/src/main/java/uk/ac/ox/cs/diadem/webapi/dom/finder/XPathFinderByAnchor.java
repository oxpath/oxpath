package uk.ac.ox.cs.diadem.webapi.dom.finder;

import java.util.EnumSet;

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
class XPathFinderByAnchor {
  private static final char SINGLEQUOTE = '\'';
  private static final char DOUBLEQUOTE = '"';

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
      final String rootPath, final int maxAxisSteps, final Integer threshold) {
    final XPathNodePointerRanking targetCandidateList = XPathNodePointerRankingOnSet.newRank();

    final DOMElement targetElement = FinderUtils.castToElementOrParentOrNull(node);
    if (targetElement != null) {
      // find an OXPath expression through anchors on the parent axis, then
      // generalize the expression by purging location steps
      final XPathNodePointerRanking positionWeight = findOXPathOnParentAxisWithPositionWeight(rootNode, rootPath,
          targetElement, maxAxisSteps, threshold);
      targetCandidateList.addAll(positionWeight);
      if (FinderUtils.checkThreshold(targetCandidateList, threshold))
        return FinderUtils.manageTextNodeIfAny(node, targetCandidateList);

      final XPathNodePointerRanking purged = purgeLocationSteps(positionWeight, targetElement, rootPath);
      targetCandidateList.addAll(purged);
    }
    return FinderUtils.manageTextNodeIfAny(node, targetCandidateList);
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
  private static XPathNodePointerRanking findOXPathOnParentAxisWithPositionWeight(final DOMNode rootNode,
      final String rootPath, final DOMElement targetElement, final int maxAxisSteps, final Integer threshold) {
    final DOMNode parentNode = targetElement.getParentNode();
    if (parentNode != null) {
      String oxPathFromParentToTarget = "/" + FinderUtils.elementName(targetElement);
      // include the position weight into the robustness score
      int position = FinderUtils.getSameNameSiblingPosition(targetElement);
      final String path = rootPath + XPathFinderByCanonical.getCanonicalXPath(parentNode).getXPath()
          + oxPathFromParentToTarget;
      final WebBrowser browser;

      if (parentNode.getNodeType() == DOMNode.Type.DOCUMENT)
        browser = ((DOMDocument) parentNode).getEnclosingWindow().getBrowser();
      else
        browser = parentNode.getOwnerDocument().getEnclosingWindow().getBrowser();

      final int count = XPathUtil.count(path, browser);
      if ((count == position) && (position > 1)) {
        oxPathFromParentToTarget += "[last()]";
        position = 1;
      } else
        oxPathFromParentToTarget += "[" + position + "]";
      final int relativePathScore = ScoreDefinitions.getRelativePositionScore(position, count,
          ScoreDefinitions.POSITION_ROOT_SCORE);
      // parent node is considered anchor here
      return findOXPathWithAnchor(rootNode, rootPath, parentNode, oxPathFromParentToTarget, relativePathScore,
          ScoreDefinitions.PARENT_AXIS_WEIGHT, maxAxisSteps, threshold);
    }
    return XPathNodePointerRankingOnSet.newRank();
  }

  /**
   * Find OXPath expressions for a given anchor node.
   * 
   * @param targetCandidateList
   *          the target candidate list
   * @param rootNode
   *          the root node
   * @param rootPath
   *          the root path
   * @param anchorNode
   *          the anchor node
   * @param oxPathFromAnchorToTarget
   *          the OXPath expression from the anchor node to the target node
   * @param relativePathScore
   *          the relative path score
   * @param absolutePathWeight
   *          the absolute path weight
   * @param maxAxisSteps
   *          the maximum number of steps when finding an anchor in the DOM tree
   * @return true, if successful
   */
  private static XPathNodePointerRanking findOXPathWithAnchor(final DOMNode rootNode, final String rootPath,
      final DOMNode anchorNode, final String oxPathFromAnchorToTarget, final int relativePathScore,
      final float absolutePathWeight, final int maxAxisSteps, final Integer threshold) {
    final XPathNodePointerRanking targetCandidateList = XPathNodePointerRankingOnSet.newRank();
    int candidatesAdded = 0;
    int maxSteps = maxAxisSteps;
    if ((anchorNode != null) && !anchorNode.equals(rootNode) && (maxSteps > 0)) {

      // final XPathNodePointerRanking addressedAnchorNode = DOMNodeFinderService.computeAllPointers(anchorNode,
      // rootNode,
      // rootPath, --maxSteps);

      final XPathNodePointerRanking addressedAnchorNode = DOMNodeFinderService.computeRankingUsingTypes(anchorNode,
          rootNode, rootPath, --maxSteps, EnumSet.of(Type.CANONICAL, Type.ATTRIBUTE, Type.ANCHOR, Type.POSITION),
          threshold);

      // ignore html and body elements as anchors, since they exist for
      // all nodes in the DOM tree
      if (!(addressedAnchorNode.getCanonicalPointer().getXPath().equals("/html[1]") || addressedAnchorNode
          .getCanonicalPointer().getXPath().equals("/html[1]/body[1]"))) {
        // final List<XPathNodePointer> anchorCandidateList = addressedAnchorNode.getAllOXPathCandidates();
        addressedAnchorNode.remove(addressedAnchorNode.getCanonicalPointer());

        // calculate the robustness scores and update the candidate list
        for (final XPathNodePointer anchorCandidate : addressedAnchorNode) {
          final int score = Math.round((anchorCandidate.getScore() * absolutePathWeight)
              + (relativePathScore * ScoreDefinitions.RELATIVE_PATH_WEIGHT));
          if ((score < ScoreDefinitions.MIN_RELATIVE_SCORE)
              || (candidatesAdded > ScoreDefinitions.MAX_RELATIVE_EXPRESSIONS))
            break;
          final String path = anchorCandidate.getXPath() + oxPathFromAnchorToTarget;
          targetCandidateList.add(new XPathNodePointerImpl(path, score, Type.ANCHOR));
          candidatesAdded++;

        }
      }
    }
    return targetCandidateList;
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

  static XPathNodePointerRanking purgeLocationStepsOld(final XPathNodePointerRanking targetCandidateList,
      final DOMElement targetElement, final String rootPath) {
    final XPathNodePointerRanking optimizedCandidateList = XPathNodePointerRankingOnSet.newRank();
    for (final XPathNodePointer candidateNode : targetCandidateList) {
      final String longPath = candidateNode.getXPath();
      int removedLocationSteps = 0;

      // start directly after the anchor node
      int anchorIndex = longPath.indexOf("/");
      if (anchorIndex >= 0) {
        anchorIndex = longPath.indexOf("/", anchorIndex + 2);
        if (anchorIndex >= 0) {
          final String anchor = longPath.substring(0, anchorIndex) + "/";
          if (!anchor.equals("/html[1]/")) {
            String tail = longPath.substring(anchorIndex);
            int tailIndex = tail.indexOf("/", 1);

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
              tailIndex = tail.indexOf("/", 1);
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

  // /**
  // * Generate OXPath expressions through anchors on the parent axis.
  // *
  // * @param targetCandidateList
  // * the target candidate list
  // * @param rootNode
  // * the root node
  // * @param rootPath
  // * the root path
  // * @param targetElement
  // * the target element
  // * @param maxAxisSteps
  // * the maximum number of steps when finding an anchor in the DOM
  // * tree
  // * @return true, if successful
  // */
  // public static boolean findOXPathOnParentAxis(List<OXPathCandidate> targetCandidateList, DOMNode rootNode,
  // String rootPath, DOMElement targetElement, int maxAxisSteps) {
  // final DOMNode parentNode = targetElement.getParentNode();
  // if (parentNode != null) {
  // final int position = CanonicalOXPathGenerator.getSameNameSiblingPosition(targetElement);
  // final String oxPathFromParentToTarget = "/" + targetElement.getLocalName() + "[" + position + "]";
  //
  // return findOXPathWithAnchor(targetCandidateList, rootNode, rootPath, parentNode, oxPathFromParentToTarget,
  // ScoreDefinitions.POSITION_ROOT_SCORE, ScoreDefinitions.PARENT_AXIS_WEIGHT, maxAxisSteps);
  // }
  // return false;
  // }

}