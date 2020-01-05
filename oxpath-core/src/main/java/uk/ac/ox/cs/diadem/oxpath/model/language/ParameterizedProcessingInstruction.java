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
 * Class encoding parameterized processing instruction node tests
 * 
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class ParameterizedProcessingInstruction implements NodeTest {

  /**
   * basic constructor
   * 
   * @param iValue
   *          the parameter associated with the processing instruction
   */
  public ParameterizedProcessingInstruction(final String iValue) {
    value = iValue;
  }

  /**
   * returns the value of the node test encoded as a {@code String} object
   * 
   * @return the value of the node test encoded as a {@code String} object
   */
  @Override
  public String getValue() {
    return "processing-instruction(" + value + ")";
  }

  /**
   * returns the type associated with this node test
   * 
   * @return the type associated with this node test
   */
  @Override
  public NodeTestType getType() {
    return NodeTestType.PARAMETERIZEDPROCESSINGINSTUCTION;
  }

  /**
   * returns the parameter associated with the processing instruction
   * 
   * @return the parameter associated with the processing instruction
   */
  public String getParameter() {
    return value;
  }

  /**
   * instance field encoding the parameter associated with the processing instruction
   */
  private final String value;
}