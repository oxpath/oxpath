/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.mutation;

import java.util.List;
import java.util.Set;

/**
 * See https://developer.mozilla.org/en-US/docs/DOM/MutationObserver
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface DOMMutationObserver extends MutationObserver {

  public static interface MutationObserverOptions {
    Boolean childList();

    Boolean attributes();

    /**
     * If null then no filter, else only specified attributes (i.e., empy list means NO ATTRIBUTES)
     * 
     * @return
     */
    List<String> attributeFilter();

    Boolean subtree();

    Boolean characterData();
  }

  /**
   * Stops the MutationObserver instance from receiving notifications of DOM mutations. Until the observe() method is
   * used again, observer's callback will not be invoked.
   */
  void disconnect();

  /**
   * Empties the MutationObserver instance's record queue and returns what was in there.
   * 
   * @return
   */
  Set<DOMMutationRecord> takeRecords();

}
