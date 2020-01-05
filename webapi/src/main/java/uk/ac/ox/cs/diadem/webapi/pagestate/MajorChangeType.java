package uk.ac.ox.cs.diadem.webapi.pagestate;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Specifyes the reason why two pages are considered different enough to produce a major change.
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public enum MajorChangeType {
  DIFFERENT_LOCATION, NUM_OF_LINKS, NUM_OF_CHANGED_HREFs, NUM_OF_CHANGED_IMAGES, NUM_OF_ELEMENTS, NUM_OF_TEXTNODES, NUM_OF_IMAGES, NUM_OF_CHANGED_IDs, NUM_OF_VISIBLE_ELEMENT_CHANGED, UNSPECIFIED;

  public Pair<String, String> asConstant() {
    return Pair.of("type", name().toLowerCase());
  }
}