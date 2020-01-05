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
 * This subpackage includes classes and interface relating to the operators of the OXPath language.
 */
package uk.ac.ox.cs.diadem.oxpath.model.language.operators;

import static uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes.BOOLEAN;
import static uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes.NODESET;
import static uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes.NUMBER;
import static uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes.STRING;

import java.util.ArrayList;
import java.util.Scanner;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathType;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes;

/**
 * Enum encoding realtional operators in OXPath
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public enum EqualityExprOp implements BinaryOperator {

  /**
   * equals operator
   */
  EQUALS("=") {
    /**
     * evaluates input by the operation and returns the result
     *
     * @param lhs
     *          left hand side
     * @param rhs
     *          right hand side
     * @return value of expression
     */
    @Override
    public OXPathType evaluate(final OXPathType lhs, final OXPathType rhs) throws OXPathException {
      return computeEquality(lhs, rhs);
    }
  },

  /**
   * not equals operator
   */
  NOTEQUAL("!=") {
    /**
     * evaluates input by the operation and returns the result
     *
     * @param lhs
     *          left hand side
     * @param rhs
     *          right hand side
     * @return value of expression
     */
    @Override
    public OXPathType evaluate(final OXPathType lhs, final OXPathType rhs) throws OXPathException {
      return computeEquality(lhs, rhs);
    }
  },

  /**
   * wordtest operator
   */
  WORDTEST("~=") {
    /**
     * evaluates input by the operation and returns the result
     *
     * @param lhs
     *          left hand side
     * @param rhs
     *          right hand side
     * @return value of expression
     */
    @Override
    public OXPathType evaluate(final OXPathType lhs, final OXPathType rhs) throws OXPathException {
      final Scanner scan = new Scanner(lhs.string());
      while (scan.hasNext()) {
        if (scan.next().equals(rhs)) {
          scan.close();
          return new OXPathType(true);

        }
      }
      scan.close();
      return new OXPathType(false);
    }
  },

  /**
   * contains operator
   */
  CONTAINS("#=") {
    /**
     * evaluates input by the operation and returns the result
     *
     * @param lhs
     *          left hand side
     * @param rhs
     *          right hand side
     * @return value of expression
     */
    @Override
    public OXPathType evaluate(final OXPathType lhs, final OXPathType rhs) throws OXPathException {
      return new OXPathType(lhs.string().contains(rhs.string()));
    }
  };

  /**
   * basic constructor
   *
   * @param iValue
   *          {@code String} encoding of the operator
   */
  private EqualityExprOp(final String op) {
    operator = op;
  }

  /**
   * Precomputes lists needed by all operators. Comparisons aren't done due to XPath existential semantics, we can
   * return immediately if a comparison succeeds. <b>Use with {@code EQUALITY} and {@code NONEQUALITY} only.</b>
   *
   * @param lhs
   *          left hand side
   * @param rhs
   *          right hand side
   * @return computed answer
   */
  protected OXPathType computeEquality(final OXPathType lhs, final OXPathType rhs) throws OXPathException {
    // don't use with CONTAINS or WORDTEST operators
    if (equals(CONTAINS) || equals(WORDTEST))
      throw new OXPathException("Can't call computeEquality method with non-equality objects");

    final OXPathTypes lhsType = lhs.isType();
    final OXPathTypes rhsType = rhs.isType();
    boolean isTrue = false;

    final boolean hasNodeSet = (lhsType.equals(NODESET) || rhsType.equals(NODESET));
    final boolean hasNumber = (lhsType.equals(NUMBER) || rhsType.equals(NUMBER));
    final boolean hasBoolean = (lhsType.equals(BOOLEAN) || rhsType.equals(BOOLEAN));

    if (hasBoolean)
      return new OXPathType((equals(EQUALS)) ? lhs.booleanValue() == rhs.booleanValue()
          : !(lhs.booleanValue() == rhs.booleanValue()));

    // otherwise, if we have a nodeset, and we need to at least convert the nodeset to string(.) equivalents
    final ArrayList<String> lhsList = new ArrayList<String>();
    final ArrayList<String> rhsList = new ArrayList<String>();
    if (hasNodeSet) {// handle the nodeset comparisons
      if (lhsType.equals(NODESET)) {
        for (final OXPathContextNode o : lhs.nodeList()) {
          lhsList.add(o.getByXPath("string(.)").string());
        }
      }
      if (rhsType.equals(NODESET)) {
        for (final OXPathContextNode o : rhs.nodeList()) {
          rhsList.add(o.getByXPath("string(.)").string());
        }
      }
    }

    if (lhsType.equals(STRING)) {
      lhsList.add(lhs.string());
    }
    if (rhsType.equals(STRING)) {
      rhsList.add(rhs.string());
    }

    if (!hasNumber) {// comparisons can be made directly
      for (final String i : lhsList) {
        for (final String j : rhsList) {
          isTrue = (equals(EQUALS)) ? i.equals(j) : !i.equals(j);
          if (isTrue)
            return new OXPathType(true);
        }
      }
      return new OXPathType(false);
    } else {// we have to deal with number conversions
      if (lhsType.equals(rhsType))
        return new OXPathType((equals(EQUALS)) ? lhs.number().doubleValue() == rhs.number().doubleValue() : lhs
            .number().doubleValue() != rhs.number().doubleValue());
      final double num = (lhsType.equals(NUMBER)) ? lhs.number() : rhs.number();
      final ArrayList<String> list = (lhsType.equals(NUMBER)) ? rhsList : lhsList;
      for (final String i : list) {
        isTrue = (equals(EQUALS)) ? (Double.parseDouble(i) == num) : (Double.parseDouble(i) != num);
        if (isTrue)
          return new OXPathType(true);
      }
      return new OXPathType(false);
    }
  }

  /**
   * returns the {@code String} representation of the operator
   *
   * @return the {@code String} representation of the operator
   */
  @Override
  public String getOperator() {
    return operator;
  }

  /**
   * instance field encoding value of operator
   */
  private final String operator;
}