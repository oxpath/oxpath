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
 * Enum type encoding forward axes in OXPath
 * 
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public enum ForwardAxis implements Axis {

  /**
   * attribute axis
   */
  ATTRIBUTE("attribute"),

  /**
   * child axis
   */
  CHILD("child"),

  /**
   * descendant-or-self axis
   */
  DESCENDANTORSELF("descendant-or-self"),

  /**
   * descendant
   */
  DESCENDANT("descendant"),

  /**
   * following-sibling
   */
  FOLLOWINGSIBLING("following-sibling"),

  /**
   * following
   */
  FOLLOWING("following"),

  /**
   * namespace
   */
  NAMESPACE("namespace"),

  /**
   * self
   */
  SELF("self");

  /**
   * basic constructor
   * 
   * @param iValue
   *          name of axis
   */
  private ForwardAxis(final String iValue) {
    value = iValue + AXIS_DELIMITER;
  }

  /**
   * returns value of axis encoded as {@code String}
   * 
   * @return value of axis encoded as {@code String}
   */
  @Override
  public String getValue() {
    return value;
  }

  /**
   * returns the {@code AxisType} of the object
   * 
   * @return the {@code AxisType} of the object
   */
  @Override
  public AxisType getType() {
    return AxisType.FORWARD;
  }

  /**
   * instance field storing {@code String} value of axis
   */
  private final String value;

  /**
   * axis delimiter
   */
  public static final String AXIS_DELIMITER = "::";
}