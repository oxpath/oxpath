package uk.ac.ox.cs.diadem.webapi.dom.finder;

import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNamedNodeMap;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

/**
 * The {@link XPathFinderByAttribute} attempts to identify the node through a unique attribute.
 * <p>
 * 
 * The algorithm iterates through all attributes of the node. For each attribute name, it checks for the
 * {@link AttributeScore}, namely a predifined choice of html elements and attributes . If unspecified, it continues
 * with the default attribute score from {@link ScoreDefinitions}. If the generated expression selects the node
 * uniquely, an {@link XPathNodePointer} is added to the list. If it selects multiple nodes, the expression is handed to
 * the XPathFinderByPosition with the score changed according to ScoreDefinitions parameters.
 * 
 * @author JochenK
 * @author Giovanni Grasso
 */
class XPathFinderByAttribute {

  private static final String SINGLEQUOTE = "'";
  private static final String DOUBLEQUOTE = "\"";

  /**
   * Gets the list of scored XPath expressions for a DOM node.
   * 
   * @param node
   *          the node
   * @return the XPath candidate list
   */
  public static XPathNodePointerRanking getAllXPathPointers(final DOMNode node, final Integer threshold) {
    return getAllXPathPointers(node, "", threshold);
  }

  /**
   * Gets the candidate list of scored XPath expressions for a DOM node.
   * 
   * @param node
   *          the node
   * @param rootPath
   *          the root path
   * @return the XPath candidate list
   */
  public static XPathNodePointerRanking getAllXPathPointers(final DOMNode node, final String rootPath,
      final Integer threshold) {
    final XPathNodePointerRanking candidateList = XPathNodePointerRankingOnSet.newRank();
    final DOMElement element = FinderUtils.castToElementOrParentOrNull(node);

    if (element != null) {
      // do the actual job
      final DOMNamedNodeMap<DOMNode> attributeList = element.getAttributes();
      if (attributeList != null) {
        final int count = (int) attributeList.getLength();
        if (count > 0) {
          for (int i = 0; i < count; i++) {
            final DOMNode attribute = attributeList.item(i);
            // remove attributes with not valid XML names or containing :
            if (XPathUtil.isInvalidName(attribute.getNodeName())) {
              continue;
            }
            findXPathPointersByAttribute(candidateList, rootPath, element, attribute.getNodeName(), threshold);
            if (FinderUtils.checkThreshold(candidateList, threshold)) {
              break;
            }
          }
        }
      }
    }
    return FinderUtils.manageTextNodeIfAny(node, candidateList);
  }

  /**
   * 
   * @param candidateList
   *          the candidate list
   * @param rootPath
   *          the root path
   * @param element
   *          the DOM element
   * @param attribute
   *          the attribute
   * @return true, if successful
   */
  private static void findXPathPointersByAttribute(final XPathNodePointerRanking candidateList, final String rootPath,
      final DOMElement element, final String attribute, final Integer threshold) {
    // define the robustness score, as defined in class ScoreDefinitions

    final int baseScore = getScoreForAttribute(attribute, element);

    // split multi-valued attributes at spaces to test contains predicates -- and take the entire attribute value as
    // well
    final String[] values = element.getAttribute(attribute).split(" ");
    String value = "";
    String oxPath = "";
    for (int i = 0; (i <= values.length) && (i < ScoreDefinitions.MAX_NUMBER_FOR_ATTRIBUTE_TOKENS); i++) {
      // take the entire attribute value in the last interation
      if (i == values.length) {
        if (i > 1) {
          value = element.getAttribute(attribute);
        } else
          return;
      } else {
        value = values[i];
      }

      // if values exist, calculate the robustness scores
      if ((value != null) && (value.length() > 0) && (value.length() < ScoreDefinitions.MAX_VALUE_LENGTH)) {
        int score = baseScore - value.length();
        // take care of xpath quoting
        String innerQuote = SINGLEQUOTE;
        if (value.contains(SINGLEQUOTE)) {
          innerQuote = DOUBLEQUOTE;
        }
        if ((values.length > 1) && (i < values.length)) {

          oxPath = "//" + FinderUtils.elementName(element) + "[contains(@" + attribute + "," + innerQuote + value
              + innerQuote + ")]";
          score += ScoreDefinitions.ATTRIBUTE_VALUE_CONTAINS;
        } else {
          oxPath = "//" + FinderUtils.elementName(element) + "[@" + attribute + "=" + innerQuote + value + innerQuote
              + "]";
        }
        if (score <= ScoreDefinitions.POSITION_ROOT_REDUCTION) {
          score = ScoreDefinitions.POSITION_ROOT_REDUCTION + 1;
        }

        // if the generated expression uniquely identifies the desired
        // node, add it to the candidate list
        final int count = XPathUtil.count(rootPath + oxPath, element.getOwnerDocument().getEnclosingWindow()
            .getBrowser());
        if (count == 1) {
          candidateList.add(new XPathNodePointerImpl(oxPath, score, XPathNodePointer.Type.ATTRIBUTE));
        }
        // possible stop here
        if (FinderUtils.checkThreshold(candidateList, threshold))
          return;

        if (count > 1) {
          candidateList.addAll(XPathFinderByPosition.resolveXPathPositionForNode(element, rootPath, oxPath, score,
              count));
          // LOWER SCORE
          // candidateList.addAll(XPathFinderByPosition.resolveXPathPositionForNode(element, rootPath, "(" + oxPath +
          // ")",
          // score - ScoreDefinitions.POSITION_ROOT_REDUCTION, count));
        }
      }
    }
  }

  private static int getScoreForAttribute(final String attribute, final DOMElement element) {
    if (attribute.equals("id"))
      return ScoreDefinitions.ID_ATTRIBUTE_SCORE;
    else if (attribute.equals("class"))
      return ScoreDefinitions.CLASS_ATTRIBUTE_SCORE;
    else {
      for (final AttributeScore scoreDefinition : ScoreDefinitions.attributeScores)
        if (element.getNodeName().equals(scoreDefinition.getElementName())
            && attribute.equals(scoreDefinition.getAttributeName()))
          return scoreDefinition.getScore();
    }

    return ScoreDefinitions.UNDEFINED_ATTRIBUTE_SCORE;
  }

}