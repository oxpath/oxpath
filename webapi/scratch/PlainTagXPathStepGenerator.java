package uk.ac.ox.cs.diadem.webapi.dom.finder;

import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.finder.DOMNodeFinderService.Score;

public class PlainTagXPathStepGenerator implements XPathLocator {

  private static Score defaulScore = Score.LOW;

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

    if (element != null)
      // TODO use another type
      candidateList.add(new XPathNodePointerImpl(element.getLocalName(), defaulScore.getThresold(),
          XPathNodePointer.Type.GENERALIZER));
    return FinderUtils.manageTextNodeIfAny(node, candidateList);
  }
}
