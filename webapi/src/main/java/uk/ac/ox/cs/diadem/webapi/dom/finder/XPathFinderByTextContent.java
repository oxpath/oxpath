package uk.ac.ox.cs.diadem.webapi.dom.finder;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNodeList;
import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathNodePointer.Type;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

/**
 * The {@link XPathFinderByTextContent} iterates through all child nodes of type text.
 * <p>
 * 
 * The algorithm then attempts to find strings within that text content that uniquely identify the node in the whole
 * tree. The robustness score is adjusted according to the parameters in {@link ScoreDefinitions}. The final score
 * depends on the length of the unique substring and its XPath function, such as equals, contains or starts-with.
 * 
 * @author JochenK
 * @author Giovanni Grasso
 * 
 */
class XPathFinderByTextContent {

  private static final String SINGLEQUOTE = "'";
  private static final String DOUBLEQUOTE = "\"";

  /**
   * Gets the candidate list of scored XPath expressions for a DOM node.
   * 
   * @param node
   *          the DOM node
   * @return the XPath candidate list
   */
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
   * @return the XPath candidate list
   */
  public static XPathNodePointerRanking getAllXPathPointers(final DOMNode node, final String rootPath,
      final Integer threshold) {
    return findXPathPointerForText(node, rootPath, threshold);
  }

  /**
   * Generate scored XPath expressions based on the text content of the DOM node.
   * 
   * @param targetCandidateList
   *          the XPath candidate list
   * @param node
   *          the DOM node
   * @param rootPath
   *          the root path
   */
  private static XPathNodePointerRanking findXPathPointerForText(final DOMNode node, final String rootPath,
      final Integer threshold) {
    // final List<XPathNodePointer> targetCandidateList = Lists.newArrayList();
    final XPathNodePointerRanking targetCandidateList = XPathNodePointerRankingOnSet.newRank();
    final DOMNodeList childNodes = node.getChildNodes();
    if (childNodes != null)
      for (final DOMNode childNode : childNodes) {
        final WebBrowser browser = childNode.getOwnerDocument().getEnclosingWindow().getBrowser();
        if (childNode.getNodeType() == DOMNode.Type.TEXT) {
          String text = childNode.getNodeValue();
          if (text != null) {
            text = text.replaceAll("[\\s|\u00A0]+", " ").trim();
            if ((text.trim().length() > 0) && (text.length() <= ScoreDefinitions.MAX_TEXTCONTENT_LENGTH)) {
              // take care of xpath quoting
              String innerQuote = SINGLEQUOTE;
              if (text.contains(SINGLEQUOTE))
                innerQuote = DOUBLEQUOTE;

              final String desc_self_el = "//" + FinderUtils.elementName(node);

              // TODO add treatment of substrings

              // TODO add positional disambiguation
              // check for equal text content
              String oxPath = desc_self_el + "[.=" + innerQuote + text + innerQuote + "]";
              if (XPathUtil.count(rootPath + oxPath, browser) == 1) {
                final int score = ScoreDefinitions.TEXTCONTENT_BASE_SCORE
                    - (text.length() * ScoreDefinitions.TEXTCONTENT_CHAR_REDUCTION);
                targetCandidateList.add(new XPathNodePointerImpl(oxPath, score, Type.TEXT));
              }

              if (FinderUtils.checkThreshold(targetCandidateList, threshold))
                return targetCandidateList;

              // TODO add positional disambiguation
              // check for contains text content
              oxPath = desc_self_el + "[contains(.," + innerQuote + text + innerQuote + ")]";
              if (XPathUtil.count(rootPath + oxPath, browser) == 1) {
                final int score = (ScoreDefinitions.TEXTCONTENT_BASE_SCORE - (text.length() * ScoreDefinitions.TEXTCONTENT_CHAR_REDUCTION))
                    + ScoreDefinitions.CONTAINS_BONUS;
                targetCandidateList.add(new XPathNodePointerImpl(oxPath, score, Type.TEXT));
              }

              if (FinderUtils.checkThreshold(targetCandidateList, threshold))
                return targetCandidateList;

              // check for starts-with text content
              oxPath = desc_self_el + "[starts-with(.," + innerQuote + text + innerQuote + ")]";
              if (XPathUtil.count(rootPath + oxPath, browser) == 1) {
                final int score = (ScoreDefinitions.TEXTCONTENT_BASE_SCORE - (text.length() * ScoreDefinitions.TEXTCONTENT_CHAR_REDUCTION))
                    + ScoreDefinitions.STARTS_WITH_BONUS;
                targetCandidateList.add(new XPathNodePointerImpl(oxPath, score, Type.TEXT));
              }
            }
          }
        }
      }
    return targetCandidateList;
  }

}