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
 * Package containing core OXPath functionality
 */
package uk.ac.ox.cs.diadem.oxpath.core.state;

import static uk.ac.ox.cs.diadem.oxpath.core.state.PAATStateType.ITERATIVE;
import static uk.ac.ox.cs.diadem.oxpath.core.state.PAATStateType.SET;

import uk.ac.ox.cs.diadem.oxpath.model.IOXPathNodeList;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathNodeListFactory;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.Node;

/**
 *
 * Class intending for use by PAATEvalVisitor, with instantiations for calls on each node. Use {@code Builder} class for
 * construction; classes themselves become immutable once made with {@code Builder.build()} method. Will create the
 * proper object based on the context (for either eval or eval_ for the PAAT algorithm).
 * <p>
 * IMPORTANT: {@code hashcode} and {@code equals(other)} have been overridden to only treat {@code position} and
 * {@code last} as the only components of equality in order for the memoization to work correctly for the
 * {@code PAATStateEvalIterate} class. For this reason, these should both be 0 when position() and last() are not called
 * (the AST get this info from an invocation of the {@code PrePAATVisitor}) so as to maximize the buffering that occurs.
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
public abstract class PAATState {

  /**
   * Standard <tt>Builder</tt> object for constructing {@code PAATState} objects
   *
   * @author AndrewJSel
   *
   */
  public final static class Builder {

    // Builder constructors

    /**
     * Signature for building new PAATState object based on minimal constructor; meant for first call when no AFP state,
     * output is yet recorded
     *
     * @param iContext
     *          context
     */
    public Builder(final OXPathContextNode iContext) {
      contextNode = iContext;
      // default initial values
      actionFreePrefix = null;
      actionFreePrefixEnd = null;
      actionFreeNavigation = false;
      position = 0;
      last = 0;
      protect = false;
      type = ITERATIVE;
      higher = 0;
      currentAction = 0;
    }

    /**
     * Signature for building new PAATState object based on minimal constructor; meant for first call when no AFP state,
     * output is yet recorded. (we don't make defensive copies of the context set for performance reasons, so be aware).
     *
     * @param iContext
     *          context
     */
    public Builder(final IOXPathNodeList iContext) {
      contextSet = iContext;
      // default initial values
      actionFreePrefix = null;
      actionFreePrefixEnd = null;
      actionFreeNavigation = false;
      position = 0;
      last = 0;
      protect = false;
      type = SET;
      higher = 0;
      currentAction = 0;
    }

    /**
     *
     * Signature for building verbose new PAATState object
     *
     * @param iContext
     *          context
     * @param iAFP
     *          action free prefix position in AST
     * @param iAFPE
     *          action free prefix end position in AST
     * @param iIsAFP
     *          determines if current navigation is by Action Free Prefix
     * @param iPosition
     *          position in parent context
     * @param iLast
     *          last position in parent context
     * @param iProtect
     *          determines if the document containing {@code context} is protected or not
     */
    public Builder(final OXPathContextNode iContext, final Node iAFP, final Node iAFPE, final boolean iIsAFP,
        final int iPosition, final int iLast, final boolean iProtect) {
      contextNode = iContext;
      actionFreePrefix = iAFP;
      actionFreePrefixEnd = iAFPE;
      actionFreeNavigation = iIsAFP;
      position = iPosition;
      last = iLast;
      protect = iProtect;
      type = ITERATIVE;
      // these shouldn't be used
      higher = 0;
      currentAction = 0;
    }

    /**
     *
     * Signature for building verbose new PAATState object (we don't make defensive copies of the context set for
     * performance reasons, so be aware)
     *
     * @param iContext
     *          context
     * @param iAFP
     *          action free prefix position in AST
     * @param iAFPE
     *          action free prefix end position in AST
     * @param iIsAFP
     *          determines if current navigation is by Action Free Prefix
     * @param iPosition
     *          position in parent context
     * @param iLast
     *          last position in parent context
     * @param iProtect
     *          determines if the document containing {@code context} is protected or not
     * @param numHigher
     *          number of Kleene star iterations to perform by inner actions (only applicable to final inner action
     *          inside each Kleene star)
     * @param currAction
     *          the id of the current action
     */
    public Builder(final IOXPathNodeList iContext, final Node iAFP, final Node iAFPE, final boolean iIsAFP,
        final int iPosition, final int iLast, final boolean iProtect, final int numHigher, final int currAction) {
      contextSet = iContext;
      actionFreePrefix = iAFP;
      actionFreePrefixEnd = iAFPE;
      actionFreeNavigation = iIsAFP;
      position = iPosition;
      last = iLast;
      protect = iProtect;
      type = SET;
      higher = numHigher;
      currentAction = currAction;
    }

    /**
     *
     * Signature for building new PAATState object copying state from existing object (we don't make defensive copies of
     * input objects for performance reasons, so be aware)
     *
     * @param state
     *          state whose content is copied
     */
    public Builder(final PAATState state) {
      if (state.getType().equals(SET)) {
        contextSet = ((PAATStateEvalSet) state).getContextSet();
        type = SET;
        higher = ((PAATStateEvalSet) state).getNumKleeneStarIterations();
        currentAction = ((PAATStateEvalSet) state).getCurrentAction();
      } else {
        contextNode = ((PAATStateEvalIterative) state).getContextNode();
        type = ITERATIVE;
      }
      actionFreePrefix = state.getActionFreePrefix();
      actionFreePrefixEnd = state.getActionFreePrefixEnd();
      actionFreeNavigation = state.isActionFreeNavigation();
      position = state.getPosition();
      last = state.getLast();
      protect = state.isDocumentProtected();
    }

    // state setters

    /**
     * Sets the protection of the document in recursive calls of the {@code eval()} method
     *
     * @param prot
     *          {@code true} to protect the page, {@code false} to remove protection
     * @return the implicit parameter, muted by the state change indicated by {@code prot}
     */
    public Builder setDocumentProtect(final boolean prot) {
      protect = prot;
      return this;
    }

    /**
     * Sets context at state
     *
     * @param o
     *          context node
     * @return same object with update applied
     */
    public Builder setContextNode(final OXPathContextNode o) {
      contextNode = o;
      type = ITERATIVE;
      return this;
    }

    /**
     * Sets context at state (we don't make a defensive copy for performance reasons, so be aware)
     *
     * @param o
     *          context set
     * @return same object with update applied
     */
    public Builder setContextSet(final IOXPathNodeList o) {
      contextSet = o;
      type = SET;
      return this;
    }

    public Builder setContextSet(final OXPathContextNode c) {
      contextSet = OXPathNodeListFactory.newImmutableOnLinkedSet(c);
      type = SET;
      return this;
    }

    /**
     * Sets the action free prefix of current context
     *
     * @param n
     *          the action free prefix of current context
     * @return same object with update applied
     */
    public Builder setActionFreePrefix(final Object n) {
      actionFreePrefix = n;
      return this;
    }

    /**
     * Sets the end of the action free prefix of current context
     *
     * @param n
     *          the end of the action free prefix of current context
     * @return same object with update applied
     */
    public Builder setActionFreePrefixEnd(final Node n) {
      actionFreePrefixEnd = n;
      return this;
    }

    /**
     * Sets if current navigation is action-free (i.e. retracing)
     *
     * @param b
     *          if current navigation is action-free (i.e. retracing)
     * @return same object with update applied
     */
    public Builder setIsActionFreeNavigation(final boolean b) {
      actionFreeNavigation = b;
      return this;
    }

    /**
     * Sets current position of evaluated "parent" context within for filter for position function
     *
     * @param iPosition
     *          current position of evaluated "parent" context within for filter for position function
     * @return same object with update applied
     */
    public Builder setPosition(final int iPosition) {
      position = iPosition;
      return this;
    }

    /**
     * Sets the last position of evaluated "parent" context within filter expression
     *
     * @param iLast
     *          last position of evaluated "parent" context within filter expression
     * @return same object with update applied
     */
    public Builder setLast(final int iLast) {
      last = iLast;
      return this;
    }

    /**
     * Set the number of Kleene Star Iterations for inner actions
     *
     * @param numHigher
     *          num of of Kleene Star iterations
     * @return same object with update applied
     */
    public Builder setNumKleeneStarIterations(final int numHigher) {
      higher = numHigher;
      return this;
    }

    /**
     * Set the current action
     *
     * @param currAction
     *          the id of the current action
     * @return same object with update applied
     */
    public Builder setCurrentAction(final int currAction) {
      currentAction = currAction;
      return this;
    }

    public Builder setNodePositionInNodeSetUnderEvaluation(final int pos) {
      nodePositionInNodeSetUnderEvaluation = pos;
      return this;
    }

    // build() method to be called for every PAATState object creation

    /**
     * Returns new {@code PAATState} object parameterized by the builder; only means of producing {@code PAATState}
     * objects outside the {@code PAATState} class
     *
     * @return new {@code PAATState} object parameterized by the builder
     */
    public PAATState build() {
      switch (type) {
      case ITERATIVE:
        return new PAATStateEvalIterative(this, contextNode);
      case SET:
      default:
        return new PAATStateEvalSet(this, contextSet, higher, currentAction);
      }
    }

    /**
     * Returns new {@code PAATStateEvalIterative} object parameterized by the builder; only means of producing
     * {@code PAATStateEvalIterative} objects outside the {@code PAATState} class
     *
     * @return new {@code PAATStateEvalIterative} object parameterized by the builder
     */
    public PAATStateEvalIterative buildNode() {
      switch (type) {
      case ITERATIVE:
        return new PAATStateEvalIterative(this, contextNode);
      case SET:// in case of a set, the first node in the set becomes the iterative node
      default:
        return new PAATStateEvalIterative(this, (contextSet.isEmpty()) ? OXPathContextNode.getNotionalContext()
            : contextSet.first());
      }
    }

    /**
     * Returns new {@code PAATStateEvalSet} object parameterized by the builder; only means of producing
     * {@code PAATStateEvalSet} objects outside the {@code PAATState} class
     *
     * @return new {@code PAATStateEvalSet} object parameterized by the builder
     */
    public PAATStateEvalSet buildSet() {
      switch (type) {
      case ITERATIVE:
        return new PAATStateEvalSet(this, contextNode, higher, currentAction);
      case SET:
      default:
        return new PAATStateEvalSet(this, contextSet, higher, currentAction);
      }
    }

    // instance fields

    /**
     * context at current "step" in query (for eval_ state)
     */
    private OXPathContextNode contextNode;
    /**
     * context set at current "step" in query (for eval state)
     */
    private IOXPathNodeList contextSet;

    /**
     * records the previous node for action free navigation root
     */
    private Object actionFreePrefix;

    /**
     * records the end node for action free navigation
     */
    private Object actionFreePrefixEnd;

    /**
     * Records if current navigation is action-free (i.e. retracing)
     */
    private boolean actionFreeNavigation;

    /**
     * Records current node's position in the nodeset
     */
    private int position;

    /**
     * Records current nodeset's last position
     */
    private int last;

    /**
     * Instance field recording if the current page (defined by the context) is protected
     */
    private boolean protect;

    /**
     * encodes if the object to be built is set-based or not
     */
    private PAATStateType type;

    /**
     * number of Kleene star iterations to perform (communicated by outer Kleene to inner action)
     */
    private int higher;

    /**
     * identifier of the current action
     */
    private int currentAction;
    private int nodePositionInNodeSetUnderEvaluation;

  }

  // constructor for Build class

  /**
   * Creates a new object with the same state as the input parameter; meant to be called only with
   * {@code Builder.build()} method
   *
   * @param builder
   *          implicit {@code Builder} object for this {@code PAATState} object
   */
  protected PAATState(final Builder builder) {
    actionFreePrefix = builder.actionFreePrefix;
    actionFreePrefixEnd = builder.actionFreePrefixEnd;
    actionFreeNavigation = builder.actionFreeNavigation;
    position = builder.position;
    last = builder.last;
    protect = builder.protect;
  }

  // getters

  /**
   * Returns if the document containing the <tt>context</tt> is protected
   *
   * @return if the document containing the <tt>context</tt> is protected
   */
  public boolean isDocumentProtected() {
    return protect;
  }

  /**
   * Returns the action free prefix of current context
   *
   * @return action free prefix as a String object
   */
  public Object getActionFreePrefix() {
    return actionFreePrefix;
  }

  /**
   * Returns the point in the AST where Action Free Navigation should end
   *
   * @return the point in the AST where Action Free Navigation should end
   */
  public Object getActionFreePrefixEnd() {
    return actionFreePrefixEnd;
  }

  /**
   * Returns if current navigation is action-free (i.e. retracing)
   *
   * @return boolean <tt>true</tt> if retracing action free prefix; <tt>false</tt> otherwise
   */
  public boolean isActionFreeNavigation() {
    return actionFreeNavigation;
  }

  /**
   * Returns current position of evaluated "parent" context within for filter for position function
   *
   * @return position of the node within the "parent" context
   */
  public int getPosition() {
    return position;
  }

  /**
   * Returns last position of evaluated "parent" context within filter expression
   *
   * @return last position of the node within the "parent" context
   */
  public int getLast() {
    return last;
  }

  /**
   * Returns the type of the object
   *
   * @return the type of the object
   */
  public abstract PAATStateType getType();

  // instance fields

  /**
   * records the previous node for action free navigation root
   */
  private final Object actionFreePrefix;

  /**
   * records the end node for action free navigation
   */
  private final Object actionFreePrefixEnd;

  /**
   * Records if current navigation is action-free (i.e. retracing)
   */
  private final boolean actionFreeNavigation;

  /**
   * Records current node's position in the nodeset
   */
  private final int position;

  /**
   * Records current nodeset's last position
   */
  private final int last;

  /**
   * Instance field recording if the current page (defined by the context) is protected
   */
  private final boolean protect;

  /**
   * Overridden to only treat {@code position} and {@code last} as the only components of equality in order for the
   * memoization to work correctly for the {@code PAATStateEvalIterate} class.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + last;
    result = (prime * result) + position;
    return result;
  }

  /**
   * Overridden to only treat {@code position} and {@code last} as the only components of equality in order for the
   * memoization to work correctly for the {@code PAATStateEvalIterate} class.
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final PAATState other = (PAATState) obj;
    if (last != other.last)
      return false;
    if (position != other.position)
      return false;
    return true;
  }

}
