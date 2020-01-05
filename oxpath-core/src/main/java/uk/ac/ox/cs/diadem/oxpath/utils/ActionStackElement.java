/*
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science,
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */

package uk.ac.ox.cs.diadem.oxpath.utils;

import uk.ac.ox.cs.diadem.oxpath.model.language.Action;

/**
 *
 * Class for use with the {@code backController} object in the {@code PAATEvalVisitor} class. Encodes waits, if
 * necessary (this is the raison d'etre for the class - otherwise, we could model these elements as integers).
 *
 * @author AndrewJSel
 *
 */
public class ActionStackElement {

  private Action action;

  /**
   * Constructor for standard Action Stack element with no wait component to the action.
   */
  public ActionStackElement() {
    this(0L);
  }

  /**
   * Constructor for Action Stack element with a wait component to the action
   *
   * @param waitTime
   *          the amount of time to wait (in seconds)
   */
  public ActionStackElement(final long waitTime) {
    backActions = 1;
    wait = waitTime;
  }

  /**
   * @param action
   */
  public ActionStackElement(final Action action) {

    this.action = action;
    backActions = 1;
    wait = action.hasWait() ? action.getWait() : 0L;

  }

  /**
   * Used when the current action is skipped and the previous action is returned (preserving the action requires a new
   * element on the stack).
   *
   * @return the same element, now incremented
   */
  public ActionStackElement increment() {
    ++backActions;
    return this;
  }

  /**
   * returns the number of back actions currently associated with this element
   *
   * @return the number of back actions currently associated with this element
   */
  public int getBackNumber() {
    return backActions;
  }

  /**
   * returns the wait associated with this action
   *
   * @return the wait associated with this action
   */
  public long getWait() {
    return wait;
  }

  /**
   * returns if there is a wait associated with this action
   *
   * @return {@code true} if there is a wait associated with this action, {@code false} otherwise
   */
  public boolean hasWait() {
    return wait > 0;
  }

  /**
   * @return the action
   */
  public Action getAction() {
    return action;
  }

  /**
   * instance field encoding the number of back actions to perform
   */
  private int backActions = 0;
  /**
   * the amount of time to wait (in seconds to be consistent with rest of wait)
   */
  private long wait = 0;

}
