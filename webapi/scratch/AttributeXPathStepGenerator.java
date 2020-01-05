package uk.ac.ox.cs.diadem.webapi.dom.finder;

import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNamedNodeMap;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.finder.DOMNodeFinderService.Score;

/**
 * The {@link AttributeXPathStepGenerator} attempts to identify the node through a unique attribute.
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
class AttributeXPathStepGenerator implements XPathLocator {

  private static final String SINGLEQUOTE = "'";
  private static final String DOUBLEQUOTE = "\"";
  private static Score defaulScore = Score.MAX;

  /*
   * (non-Javadoc)
   * 
   * @see
   * uk.ac.ox.cs.diadem.webapi.dom.finder.XPathStepBuilder#getAllXPathPointers(uk.ac.ox.cs.diadem.webapi.dom.DOMNode)
   */
  @Override
  public XPathNodePointerRanking getAllXPathPointers(final DOMNode node) {
    return getAllXPathPointers(node, defaulScore);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * uk.ac.ox.cs.diadem.webapi.dom.finder.XPathStepBuilder#getAllXPathPointers(uk.ac.ox.cs.diadem.webapi.dom.DOMNode,
   * uk.ac.ox.cs.diadem.webapi.dom.finder.DOMNodeFinderService.Score)
   */
  @Override
  public XPathNodePointerRanking getAllXPathPointers(final DOMNode node, final Score threshold) {
    final XPathNodePointerRanking candidateList = XPathNodePointerRankingOnSet.newRank();
    final DOMElement element = FinderUtils.castToElementOrParentOrNull(node);

    if (element != null) {
      // do the actual job
      @SuppressWarnings("unchecked")
      final DOMNamedNodeMap<DOMNode> attributeList = element.getAttributes();
      if (attributeList != null) {
        final int count = (int) attributeList.getLength();
        if (count > 0)
          for (int i = 0; i < count; i++) {
            final DOMNode attribute = attributeList.item(i);
            findXPathPointersByAttribute(candidateList, element, attribute.getNodeName(), threshold);
            if (FinderUtils.checkThreshold(candidateList, threshold.getThresold()))
              break;
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
  private static void findXPathPointersByAttribute(final XPathNodePointerRanking candidateList,
      final DOMElement element, final String attribute, final Score threshold) {
    // define the robustness score, as defined in class ScoreDefinitions

    final int baseScore = getScoreForAttribute(attribute, element);

    // split multi-valued attributes at spaces to test contains predicates
    final String[] values = element.getAttribute(attribute).split(" ");
    String value = "";
    String oxPath = "";
    for (int i = 0; (i <= values.length) && (i < ScoreDefinitions.MAX_NUMBER_FOR_ATTRIBUTE_TOKENS); i++) {
      if (i == values.length) {
        if (i > 1)
          value = element.getAttribute(attribute);
        else
          return;
      } else
        value = values[i];

      // if values exist, calculate the robustness scores
      if ((value != null) && (value.length() > 0) && (value.length() < ScoreDefinitions.MAX_VALUE_LENGTH)) {
        int score = baseScore - value.length();
        // take care of xpath quoting
        String innerQuote = SINGLEQUOTE;
        if (value.contains(SINGLEQUOTE))
          innerQuote = DOUBLEQUOTE;
        if ((values.length > 1) && (i < values.length)) {

          oxPath = FinderUtils.elementName(element) + "[contains(@" + attribute + "," + innerQuote + value + innerQuote
              + ")]";
          score += ScoreDefinitions.ATTRIBUTE_VALUE_CONTAINS;
        } else
          oxPath = FinderUtils.elementName(element) + "[@" + attribute + "=" + innerQuote + value + innerQuote + "]";
        if (score <= ScoreDefinitions.POSITION_ROOT_REDUCTION)
          score = ScoreDefinitions.POSITION_ROOT_REDUCTION + 1;

        // add candidate to the list
        candidateList.add(new XPathNodePointerImpl(oxPath, score, XPathNodePointer.Type.ATTRIBUTE));
        // possible stop here
        if (FinderUtils.checkThreshold(candidateList, threshold.getThresold()))
          return;
      }
    }
  }

  private static int getScoreForAttribute(final String attribute, final DOMElement element) {
    if (attribute.equals("id"))
      return ScoreDefinitions.ID_ATTRIBUTE_SCORE;
    else if (attribute.equals("class"))
      return ScoreDefinitions.CLASS_ATTRIBUTE_SCORE;
    else
      for (final AttributeScore scoreDefinition : ScoreDefinitions.attributeScores)
        if (element.getNodeName().equals(scoreDefinition.getElementName())
            && attribute.equals(scoreDefinition.getAttributeName()))
          return scoreDefinition.getScore();

    return ScoreDefinitions.UNDEFINED_ATTRIBUTE_SCORE;
  }

}