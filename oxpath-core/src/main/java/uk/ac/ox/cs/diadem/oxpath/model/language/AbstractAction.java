/*
 * Copyright (c)2011, DIADEM Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the DIADEM team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DIADEM Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Package containing supporting classes, derived from the OXPath model (which itself extends the XPath model).
 * This subpackage includes classes and interface relating to the OXPath language.
 */
package uk.ac.ox.cs.diadem.oxpath.model.language;

/**
 * abstract class to exploit commonalities among OXPath actions
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public abstract class AbstractAction implements Action {

  private boolean hasSimplePath;

  private boolean hasComplexPath;

  @Override
  public boolean hasSimplePath() {

    return hasSimplePath;
  }

  @Override
  public boolean hasComplexPath() {
    return hasComplexPath;
  }

  @Override
  public void setHasSimplePath(final boolean hasSimplePath) {
    this.hasSimplePath = hasSimplePath;
  }

  @Override
  public void setHasComplexPath(final boolean hasComplexPath) {
    this.hasComplexPath = hasComplexPath;

  }

  /**
   * sets whether the action is absolute
   *
   * @param absolute
   *          {@code true} if the action is absolute, {@code false} otherwise
   */
  @Override
  public void setIsAbsoluteAction(final boolean absolute) {
    isAbsolute = absolute;
  }

  /**
   * returns {@code true} if the action is absolute, {@code false} otherwise
   *
   * @return {@code true} if the action is absolute, {@code false} otherwise
   */
  @Override
  public boolean isAbsoluteAction() {
    return isAbsolute;
  }

  /**
   * sets the wait time (ms) after the action is executed before continuing
   *
   * @param wait
   *          the wait time (ms) after the action is executed before continuing
   */
  @Override
  public void setWait(final long wait) {
    waitTime = wait;
    if (waitTime > 0) {
      hasWait = true;
    }
  }

  /**
   * returns the wait time (ms) after the action is executed before continuing
   *
   * @return the wait time (ms) after the action is executed before continuing
   */
  @Override
  public long getWait() {
    return waitTime;
  }

  /**
   * returns {@code true} if there is a wait between action execution and continued evaluation
   *
   * @return {@code true} if there is a wait between action execution and continued evaluation
   */
  @Override
  public boolean hasWait() {
    return hasWait;
  }

  @Override
  public String toOXPathString() {
    final StringBuilder sb = new StringBuilder();

    sb.append("/{");
    // if (getActionType().equals(ActionType.EXPLICIT)) {
    // sb.append("\"");
    // }
    sb.append(getValue());
    // if (getActionType().equals(ActionType.EXPLICIT)) {
    // sb.append("\"");
    // }
    if (hasWait()) {
      sb.append("[wait=" + getWait() + "]");
    }

    if (isAbsoluteAction()) {
      sb.append("/");
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public boolean isBackActionNeeded() {

    return isBackActionNeeded;
  }

  @Override
  public void isBackActionNeeded(final boolean isBackActionNeeded) {
    this.isBackActionNeeded = isBackActionNeeded;

  }

  /**
   * instance field encoding absolute action state
   */
  private boolean isAbsolute = false;

  private long waitTime = 0;

  private boolean hasWait = false;

  private boolean isBackActionNeeded = true;
}
