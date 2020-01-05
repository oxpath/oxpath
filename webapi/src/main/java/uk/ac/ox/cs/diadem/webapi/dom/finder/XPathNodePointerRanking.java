/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.finder;

import java.util.SortedSet;

/**
 * TODO do we need this split into interface and implementation?
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface XPathNodePointerRanking extends SortedSet<XPathNodePointer> {

  XPathNodePointer getCanonicalPointer();

  /**
   * Creates a new ranking as intersection of this and other. IMPORTANT: the scores are summed up, such that they can be
   * averages after a number of successive intersections.
   * 
   * @param other
   * @return
   */
  XPathNodePointerRanking intersectWith(final XPathNodePointerRanking other);

  /**
   * Creates a new ranking with divided scores.
   * 
   * TODO It does not modify this, as the contracts with clients are unclear. We might want to change this.
   * 
   * @param divisor
   * @return
   */
  XPathNodePointerRanking divideScores(int divisor);

  XPathNodePointerRanking normalize();

  boolean meetsThreshold(int threshold);
}
