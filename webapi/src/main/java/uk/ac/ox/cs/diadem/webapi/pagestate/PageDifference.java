/*
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science,
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */

package uk.ac.ox.cs.diadem.webapi.pagestate;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;

/**
 * A PageDifference represents a change to a browser page. There are many types of such changes, {@link DifferenceKind}.
 */
class PageDifference {

  public DOMNode getNode() {
    return node;
  }

  public DifferenceKind getKind() {
    return kind;
  }

  public String getOldValue() {
    return oldValue;
  }

  public String getNewValue() {
    return newValue;
  }

  public String getValue() {
    return oldValue + newValue;
  }

  public enum DifferenceKind {
    newPage, newWindow, styleChange, insertedNode, deletedChild, insertedText, deletedText, insertedAttribute, deletedAttribute
  }

  /** Always node in the new DOM */
  private final DOMNode node;
  private final DifferenceKind kind;
  private final String oldValue;
  private final String newValue;

  /**
   * Creates a new page difference, specifying that the difference is on the given node, of the given kind and with the
   * given values. The values are interpreted based on the given {@link DifferenceKind}.
   */
  public PageDifference(final DOMNode node, final DifferenceKind kind, final String oldValue, final String newValue) {
    this.node = node;
    this.kind = kind;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  public String toString() {
    return String.format("PageDifference [node=%s, kind=%s, oldValue=%s, newValue=%s]", node, kind, oldValue, newValue);
  }

}
