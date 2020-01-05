/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.utils;

import java.util.SortedMap;

/**
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface BrowserStats {

  int visitedPages();

  void incrementPageNumbers();

  SortedMap<String, Integer> jsCalls();
}
