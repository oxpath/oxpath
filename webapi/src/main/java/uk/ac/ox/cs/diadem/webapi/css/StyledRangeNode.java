/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.css;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Used to overlay text nodes
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface StyledRangeNode extends StyledNode {
  /**
   * xpath locator for the start node of the range
   * 
   * @return
   */
  String getStartNodeLocator();

  /**
   * xpath locator for the end node of the range
   * 
   * @return
   */
  String getEndNodeLocator();

  /**
   * Start and end offset
   * 
   * @return
   */
  Pair<Integer, Integer> range();

}
