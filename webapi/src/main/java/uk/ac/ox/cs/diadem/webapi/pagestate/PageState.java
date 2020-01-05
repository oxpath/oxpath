/*
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science,
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */

package uk.ac.ox.cs.diadem.webapi.pagestate;

import java.util.List;

/**
 * A page state is a capture of the current state of a browser. It serves primarily to compare two such captures.
 */
public interface PageState {

  /** Up to this many characters may have changed for a document to be "similar" */
  static final int SIMILARITY_THRESHOLD = 10;

  /**
   * Returns a list of actual differences.
   * 
   * @see PageDifference
   */
  List<PageDifference> getDifferences(PageState other);

  /**
   * Returns the edit distance in terms of fine-grained, atomic units such as characters.
   */
  int getAtomicEditDistance(PageState other);

  /**
   * Returns the edit distance in terms of nodes.
   */
  int getEditDistance(PageState other);

  /**
   * Returns true if the other {@link PageState} is identical to this one.
   */
  boolean identicalTo(PageState other);

  /**
   * Returns true if the other {@link PageState} is similar to this one. The specific meaning of similar is determine by
   * the implementation, e.g., through a threshold on the edit distance.
   */
  boolean similarTo(PageState other);

  /**
   * Returns true if the pagestate represents a different page (different location or content).
   */
  boolean isDifferentPage(PageState other);

  /**
   * Returns true if the pagestate represents a page with the same location URL.
   */
  boolean atSameLocation(final PageState other);
}
