/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.finder;

import java.util.SortedSet;

import com.google.common.base.Function;
import com.google.common.collect.ForwardingSortedSet;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathNodePointer.Type;

/**
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class XPathNodePointerRankingOnSet extends ForwardingSortedSet<XPathNodePointer> implements
    XPathNodePointerRanking {

  private final SortedSet<XPathNodePointer> delegate;

  // TODO why not a constructor??
  public static XPathNodePointerRankingOnSet newRank() {
    return new XPathNodePointerRankingOnSet(Sets.<XPathNodePointer> newTreeSet());
  }

  private XPathNodePointerRankingOnSet(final SortedSet<XPathNodePointer> elements) {
    // defensive copy
    delegate = elements;
  }

  @Override
  protected SortedSet<XPathNodePointer> delegate() {
    return delegate;
  }

  @Override
  public XPathNodePointer getCanonicalPointer() {
    for (final XPathNodePointer p : this)
      if (p.getType() == Type.CANONICAL) // TODO throw!
        return p;
    return null;
  }

  public static XPathNodePointerRanking transform(final XPathNodePointerRanking candidateList,
      final Function<? super XPathNodePointer, ? extends XPathNodePointer> function) {
    final XPathNodePointerRankingOnSet newRank = XPathNodePointerRankingOnSet.newRank();
    for (final XPathNodePointer p : candidateList)
      newRank.add(function.apply(p));
    return newRank;
  }

  @Override
  public XPathNodePointerRanking normalize() {
    final XPathNodePointerRankingOnSet result = XPathNodePointerRankingOnSet.newRank();
    final XPathNodePointer[] pointers = new XPathNodePointer[size()];
    int i = -1;
    for (final XPathNodePointer pointer : this)
      pointers[++i] = pointer;

    for (i = 0; i < pointers.length; ++i) {
      if (pointers[i] == null)
        continue;
      int scoreSum = pointers[i].getScore();
      int matches = 1;
      Type type = pointers[i].getType();
      for (int j = i + 1; j < pointers.length; ++j)
        if ((pointers[j] != null) && pointers[i].getXPath().equals(pointers[j].getXPath())) {
          if (pointers[j].getType() != type)
            type = Type.GENERALIZER;
          scoreSum += pointers[j].getScore();
          pointers[j] = null;
          ++matches;
        }
      result.add(new XPathNodePointerImpl(pointers[i].getXPath(), scoreSum / matches, type));
    }
    return result;
  }

  @Override
  public XPathNodePointerRanking intersectWith(XPathNodePointerRanking other) {
    assert other != null;
    final XPathNodePointerRankingOnSet result = XPathNodePointerRankingOnSet.newRank();
    for (final XPathNodePointer p : this) {
      int scoreSum = p.getScore();
      int matches = 1;
      Type type = p.getType();
      for (final XPathNodePointer q : other)
        if (p.getXPath().equals(q.getXPath())) {
          if (q.getType() != type)
            type = Type.GENERALIZER;
          scoreSum += q.getScore();
          ++matches;
        }
      if (matches > 1)
        result.add(new XPathNodePointerImpl(p.getXPath(), scoreSum, type));
    }
    return result;
  }

  @Override
  public XPathNodePointerRanking divideScores(int divisor) {
    assert divisor > 0;
    final XPathNodePointerRankingOnSet result = XPathNodePointerRankingOnSet.newRank();
    for (final XPathNodePointer p : this)
      result.add(new XPathNodePointerImpl(p.getXPath(), p.getScore() / divisor, p.getType()));
    return result;
  }

  @Override
  public boolean meetsThreshold(final int threshold) {
    if (isEmpty())
      return false;
    // we can stop if the threshold if reached
    if (first().getScore() >= threshold)
      return true;
    return false;
  }
}
