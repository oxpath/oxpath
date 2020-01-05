/**
 * Header
 */
package uk.ac.ox.cs.diadem.oxpath.model;

import java.util.Comparator;

/**
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public final class OXPathNodeListFactory {

  // private static boolean sorted = false;

  private OXPathNodeListFactory() {
    // prevents instantiation
  }

  public static IOXPathNodeList newMutableOnLinkedSet() {
    // if (sorted)
    // return new OXPathNodeListOnTreeSet();
    // else
    return new OXPathNodeListOnLinkedSet();
  }

  public static IOXPathNodeList newImmutableOnLinkedSet(final OXPathContextNode node) {
    // return new OXPathNodeListOnTreeSet(node); // GIOG we were using treeset everywhere as contextset for single node
    // state.
    return new OXPathNodeImmutableSingletonSet(node);
  }

  public static IOXPathNodeList newImmutableOnSortedSet(final OXPathContextNode node) {
    return new OXPathNodeListOnTreeSet(node); // GIOG this is the original now replcaced by newImmutableOnLinkedSet
  }

  public static IOXPathNodeList newMutableOnSortedSet(final OXPathContextNode newNode) {
    return new OXPathNodeListOnTreeSet(newNode);
  }

  public static IOXPathNodeList newMutableOnSortedSet(final Comparator<Object> comparator) {
    return new OXPathNodeListOnTreeSet(comparator);
  }

  /**
   * To use when there is guarantee will contain no duplicates
   *
   * @return
   */
  public static OXPathNodeListOnLinkedList newMutableOnLinkedList() {
    return new OXPathNodeListOnLinkedList();
  }

}
