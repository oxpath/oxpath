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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.IOXPathNodeList;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathNodeListFactory;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathType;

/**
 * Enum type encoding OXPath selectors borrowed from CSS
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public enum Selector implements OXPathPredicate {

  /**
   * class selector
   */
  CLASS(".", "class"),

  /**
   * id selector
   */
  ID("#", "id");

  /**
   * basic constructor
   *
   * @param iValue
   *          the {@code String} representation of the operator
   */
  private Selector(final String iValue, final String iAttributeName) {
    operator = iValue;
    attributeName = iAttributeName;
  }

  /**
   * evaluates input by the operation and returns the result; call this method when no <tt>position()</tt> or
   * <tt>last()</tt> node tests occur inside subsequent predicates (or this one) in this list.
   *
   * @param contextNode
   *          context node
   * @param other
   *          will depend on the specific predicate
   * @return operator of expression
   * @throws OXPathException
   *           in case of exception in nested calls
   */
  public OXPathType evaluateIterative(final OXPathContextNode contextNode, final OXPathType other)
      throws OXPathException {
    try {// necessary because getAttributes and getNamedItem can both return null values
      // may be a little expensive creating an extra list, but these selectors aren't true for many nodes in sets
      final String nodeValue = contextNode.getNode().getAttributes().getNamedItem(getAttributeName()).getNodeValue();
      boolean condition = false;
      // for Id we evaluate with equals
      if (operator.equals(ID.operator)) {
        condition = nodeValue.equals(other.string());
      } else if (operator.equals(CLASS.operator)) {
        condition = Lists.newArrayList(Splitter.on(' ').omitEmptyStrings().split(nodeValue)).contains(other.string());
      } else
        throw new OXPathException("Unexpected operator " + operator);

      if (condition)
        return new OXPathType(contextNode);
      return new OXPathType();
    } catch (final NullPointerException e) {
      // FIXME
      return new OXPathType();
    }
  }

  /**
   * evaluates input by the operation and returns the result; call this method when <tt>position()</tt> or
   * <tt>last()</tt> node tests occur inside subsequent predicates (or this one) in this list.
   *
   * @param contextSet
   *          context set
   * @param other
   *          will depend on the specific predicate
   * @return operator of expression
   * @throws OXPathException
   *           in case of exception in nested calls
   */
  public OXPathType evaluateSet(final IOXPathNodeList contextSet, final OXPathType other) throws OXPathException {
    final IOXPathNodeList result = OXPathNodeListFactory.newMutableOnLinkedSet();
    for (final OXPathContextNode i : contextSet) {
      result.addAll(evaluateIterative(i, other).nodeList());
    }
    return new OXPathType(result);
  }

  /**
   * returns the {@code String} representation of the selector
   *
   * @return the {@code String} representation of the selector
   */
  public String getValue() {
    return operator + attributeName;
  }

  /**
   * returns the {@code String} representation of the operator
   *
   * @return the {@code String} representation of the operator
   */
  public String getOperator() {
    return operator;
  }

  /**
   * returns the attribute name corresponding to the selector
   *
   * @return the attribute name corresponding to the selector
   */
  public String getAttributeName() {
    return attributeName;
  }

  /**
   * returns the type of the predicate
   *
   * @return the type of the predicate
   */
  @Override
  public OXPathPredicateTypes getType() {
    return OXPathPredicateTypes.SELECTOR;
  }

  /**
   * instance field encoding operator
   */
  private final String operator;
  /**
   * instance field encoding attribute name corresponding to each object in the {@code enum}
   */
  private final String attributeName;
}