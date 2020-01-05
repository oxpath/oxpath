package uk.ac.ox.cs.diadem.webapi.dom.finder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.WebAPIException;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.finder.DOMNodeFinderService.Score;

public class UnionXPathStepGenerator implements XPathLocator {

  private static final Logger LOGGER = LoggerFactory.getLogger(UnionXPathStepGenerator.class);

  final Set<XPathLocator> generators;

  public UnionXPathStepGenerator() {
    generators = new HashSet<XPathLocator>();
  }

  public UnionXPathStepGenerator(Collection<XPathLocator> generators) {
    this.generators = new HashSet<XPathLocator>(generators);
  }

  public void add(XPathLocator generator) {
    generators.add(generator);
  }

  @Override
  public XPathNodePointerRanking getAllXPathPointers(DOMNode node) throws WebAPIException {
    assert node != null;
    if (generators.isEmpty())
      throw new WebAPIException("No subgenerators configured", LOGGER);
    final XPathNodePointerRanking result = XPathNodePointerRankingOnSet.newRank();
    for (final XPathLocator generator : generators)
      result.addAll(generator.getAllXPathPointers(node));
    return result;
  }

  @Override
  public XPathNodePointerRanking getAllXPathPointers(DOMNode node, Score threshold) throws WebAPIException {
    assert node != null;
    if (generators.isEmpty())
      throw new WebAPIException("No subgenerators configured", LOGGER);
    final XPathNodePointerRanking result = XPathNodePointerRankingOnSet.newRank();
    for (final XPathLocator generator : generators)
      // TODO threshold is applied to each generator -- but we could stop after the first generator was successful
      result.addAll(generator.getAllXPathPointers(node, threshold));
    return result;
  }

}
