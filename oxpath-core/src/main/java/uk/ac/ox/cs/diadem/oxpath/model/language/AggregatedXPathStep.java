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
 * Class for encoding aggregated simple steps, which are constructed with the {@code SimpleStepAggregationVisitor}.
 * These steps represent one or more simple OXPath steps (read: steps which can be passed directly to the web browser's
 * XPath API for processing). Useful when using a browser API that processes XPath quickly, but may have significant
 * overhead costs to each invokation of the {@code evaluate} method. Important note: using aggregated steps results in
 * the OXPath engine inheriting the runtime complexity characterizations of the browser's XPath API (rather than those
 * of PAAT). Be aware, some engines (e.g., XALAN in HtmlUnit) have known exponential worst-case complexity.
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class AggregatedXPathStep implements AggregatedStep {

  /**
   * empty constructor
   */
  private AggregatedXPathStep(final String iExpr, final Axis iAxis, final NodeTest iNodeTest) {
    expression = iExpr;
    axis = iAxis;
    nodetest = iNodeTest;
  }

  /**
   * Returns the axis associated with the first step in the aggregation
   *
   * @return the axis associated with the first step in the aggregation
   */
  @Override
  public Axis getAxis() {
    return axis;
  }

  /**
   * Returns the nodetest associated with the first step in the aggregation
   *
   * @return the nodetest associated with the first step in the aggregation
   */
  @Override
  public NodeTest getNodeTest() {
    return nodetest;
  }

  /**
   * Returns the full path expression, expressed as a {@code String} object
   */
  @Override
  public String getPathExpression() {
    return expression;
  }

  /**
   * immutable state encoding the String expression
   */
  private final String expression;

  /**
   * the first axis in the aggregated step
   */
  private final Axis axis;

  /**
   * the first node test in the aggregated step
   */
  private final NodeTest nodetest;

  /**
   * AggregatedSimpleSteps are immutable objects. They are constructed with builder objects.
   *
   * @author andrewjsel
   *
   */
  public static class Builder {

    /**
     * constructor with an initial step
     *
     * @param initialStep
     *          an initial step to begin aggregation from
     */
    public Builder(final Step initialStep) {

      expressionBuilder.append(initialStep.getAxis().getValue() + initialStep.getNodeTest().getValue());
      axis = initialStep.getAxis();
      nodetest = initialStep.getNodeTest();
    }

    /**
     * Method for aggregating a step to the current simple OXPath step sequence
     *
     * @param step
     *          a step to aggregrate to the current sequence
     * @return the same builder object
     */
    public Builder aggregateStep(final String step) {
      expressionBuilder.append(step);
      return this;
    }

    /**
     * Builds the step, finalizing the aggregation
     *
     * @return an aggregated sequence of OXPath steps
     */
    public AggregatedXPathStep buildStep() {
      return new AggregatedXPathStep(expressionBuilder.toString(), axis, nodetest);
    }

    /**
     * a {@code StringBuilder} object to build our steps from
     */
    private final StringBuilder expressionBuilder = new StringBuilder();

    /**
     * the axis associated with the first step in the aggregation
     */
    private final Axis axis;

    /**
     * the nodetest associated with the first step in the aggregation
     */
    private final NodeTest nodetest;
  }

  @Override
  public String toOXPath() {
    return getPathExpression();
  }

  @Override
  public boolean isPlainXPath() {
    return ((!axis.getType().equals(AxisType.OXPATH)) && (!nodetest.getType().equals(NodeTestType.OXPATH)));
  }

}