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
 */
package uk.ac.ox.cs.diadem.oxpath.model;

import static uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes.BOOLEAN;
import static uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes.NODESET;
import static uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes.NULL;
import static uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes.NUMBER;
import static uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes.STRING;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode.Type;

/**
 * Class for encoding OXPath return types, including nodesets, strings, numbers, and booleans
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class OXPathType {

  private static final Logger LOG = LoggerFactory.getLogger(OXPathType.class);

  /**
   * Null constructor
   */
  public OXPathType() {
    type = NULL;
  }

  /**
   * Returns a new (defensive) copy of the implicit parameter
   *
   * @param other
   *          {OXPathType to copy}
   */
  public OXPathType(final OXPathType other) {
    this();
    try {
      switch (other.isType()) {
      case NULL:
        return;
      case STRING:
        this.set(other.string());
        break;
      case NUMBER:
        this.set(other.number());
        break;
      case BOOLEAN:
        this.set(other.booleanValue());
        break;
      case NODESET:
        this.set(other.nodeList());
        break;
      }
    } catch (final OXPathException e) {
    }// just to fool compiler; these exceptions won't happen because we check type beforehand
  }

  /**
   * Constructor for nodelists
   *
   * @param in
   *          input nodelist
   */
  public OXPathType(final IOXPathNodeList in) {
    this.set(in);
  }

  /**
   * Constructor for strings
   *
   * @param in
   *          input String
   */
  public OXPathType(final String in) {
    this.set(in);
  }

  /**
   * Constructor for number inputs
   *
   * @param in
   *          input int
   */
  public OXPathType(final double in) {
    this.set(in);
  }

  /**
   * Constructor for booleans
   *
   * @param in
   *          input boolean
   */
  public OXPathType(final boolean in) {
    this.set(in);
  }

  /**
   * Constructor for handling output from the getByXPath function from HtmlUnit
   *
   * @param byXPath
   *          input of List<?> from getByXPath
   * @param parent
   *          reference to parent node of current context
   * @param last
   *          reference to parent node of current context
   */
  public OXPathType(final List<?> byXPath, final int parent, final int last) {
    if (byXPath.isEmpty()) {
      type = NULL;
    } else {
      final Object first = byXPath.get(0);
      if (first instanceof DOMNode) {
        nodes = OXPathNodeListFactory.newMutableOnLinkedSet();
        for (final Object n : byXPath) {
          nodes.add(new OXPathContextNode((DOMNode) n, parent, last));
        }
        type = NODESET;
      } else if (first instanceof String) {
        string = (String) first;
        type = STRING;
      } else if (first instanceof Double) {
        number = (Double) first;
        type = NUMBER;
      } else if (first instanceof Boolean) {
        bool = (Boolean) first;
        type = BOOLEAN;
      } else {
        type = NULL;
      }
    }
  }

  /**
   * Expression for setting state
   *
   * @param j
   *          input NodeList
   */
  public OXPathType(final OXPathContextNode j) {
    nodes = OXPathNodeListFactory.newMutableOnLinkedSet();
    nodes.add(j);
    type = NODESET;
  }

  /**
   * Expression for setting state
   *
   * @param in
   *          input NodeList
   */
  public void set(final IOXPathNodeList in) {
    if (nodes == null) {
      nodes = OXPathNodeListFactory.newMutableOnLinkedSet();
    }

    // TODO here we don't have document order any more if we don't use the treeset
    if (nodes.size() > 0) {
      LOG.error("POTENTIAL ERROR, ADDING NODES WITHOUT DOCUMENT ORDER {}", in);
    }
    nodes.addAll(in);
    type = NODESET;
  }

  /**
   * Expression for setting state
   *
   * @param in
   *          input String
   */
  public void set(final String in) {
    string = in;
    type = STRING;
  }

  /**
   * Expression for setting state
   *
   * @param in
   *          input int
   */
  public void set(final Double in) {
    number = in;
    type = NUMBER;
  }

  /**
   * Expression for setting state
   *
   * @param in
   *          input boolean
   */
  public void set(final boolean in) {
    bool = in;
    type = BOOLEAN;
  }

  /**
   * Expression for setting null state
   */
  public void set(final Object in) {
    type = NULL;
  }

  /**
   * Expression that returns type of Object
   *
   * @return type of implicit parameter
   */
  public OXPathTypes isType() {
    return type;
  }

  /**
   * Casts object as <tt>IOXPathNodeList</tt>
   *
   * @return object as <tt>IOXPathNodeList</tt>
   * @throws OXPathException
   *           if the object is null
   */
  public IOXPathNodeList nodeList() throws OXPathException {
    if (type.equals(NODESET))
      return nodes;
    else
      return OXPathNodeListFactory.newMutableOnLinkedSet();
    // else throw new OXPathException("OXPathType exception - Can't cast " + this.type.toString() + " as " +
    // NODESET.toString());
  }

  /**
   * Casts object as <tt>String</tt>
   *
   * @return object as <tt>String</tt>
   * @throws OXPathException
   *           if the object is null
   */
  public String string() throws OXPathException {
    if (type.equals(STRING))
      return string;
    else if (type.equals(NODESET)) {
      if (nodes.isEmpty())
        return "";
      else {
        // return nodes.first().getByXPath("string(.)").string();
        final OXPathContextNode first = nodes.first();
        if (first.getNode().getNodeType() == Type.DOCUMENT)
          return first.getByXPath("string(.)").string();
        else
          return first.getNode().getTextContent();

      }
    } else if (type.equals(BOOLEAN))
      return (bool) ? "true" : "false";
    else if (type.equals(NUMBER))
      return String.valueOf(number);
    else
      throw new OXPathException("OXPathType exception - Can't cast " + type.toString() + " as " + STRING.toString());
  }

  /**
   * Casts object as <tt>double</tt>
   *
   * @return object as <tt>double</tt>
   * @throws OXPathException
   *           if the object is null
   */
  public Double number() throws OXPathException {
    if (type.equals(NUMBER))
      return number;
    else if (type.equals(STRING)) {
      // System.out.println(this.string);
      if (string().equals("false"))
        return 0.0;
      else if (string().equals("true"))
        return 1.0;
      else
        return getDoubleOrNaN(string);
    } else if (type.equals(BOOLEAN))
      return (bool) ? 1.0 : 0.0;
    else if (type.equals(NODESET)) {
      if (nodes.isEmpty())
        return Double.NaN;

      final String asString = nodes.first().getByXPath("string(.)").string();
      return getDoubleOrNaN(asString);

    } else
      throw new OXPathException("OXPathType exception - Can't cast " + type.toString() + " as " + NUMBER.toString());
  }

  private Double getDoubleOrNaN(final String value) {
    try {
      return Double.valueOf(value);
    } catch (final NumberFormatException e) {
      return Double.NaN;
    }
  }

  /**
   * Casts object as <tt>boolean</tt>
   *
   * @return object as <tt>boolean</tt>
   * @throws OXPathException
   *           if the object is null
   */
  public boolean booleanValue() throws OXPathException {
    if (type.equals(BOOLEAN))
      return bool;
    else if (type.equals(NODESET))
      return (nodes.size() > 0) ? true : false;
    else if (type.equals(STRING))
      return (string.length() > 0);
    else if (type.equals(NUMBER))
      return !number.equals(0);
    else
      return false;
  }

  /**
   * Not class-safe, but returns value based on instantiation of type in the object
   *
   * @return value of object
   */
  public Object getValue() {
    switch (type) {
    case NODESET:
      return nodes;
    case STRING:
      return string;
    case NUMBER:
      return number;
    case BOOLEAN:
      return bool;
    }
    return null;
  }

  /**
   * returns a {@code String} encoding of XPath primatives and concatenated pretty html versions of nodelists Useful for
   * attribute extraction marker output.
   *
   * @return a {@code String} encoding of XPath primatives and concatenated pretty html versions of nodelists
   * @throws OXPathException
   *           if the object is null
   */
  public String toPrettyHtml() throws OXPathException {
    switch (type) {
    case STRING:
    case NUMBER:
    case BOOLEAN:
      return string();
    case NODESET:
      final StringBuilder sb = new StringBuilder();
      for (final OXPathContextNode c : nodes) {
        sb.append(c.getNode().toPrettyHTML());
      }
      return sb.toString();
    }
    return "";
  }

  @Override
  public String toString() {
    switch (isType()) {
    case NODESET:
      try {
        if (nodeList().isEmpty())
          return "Empty Node List returned";
        else {
          final StringBuilder sb = new StringBuilder();
          for (final OXPathContextNode i : nodeList()) {
            sb.append(i.getNode().toString());
            sb.append("\n");
          }
          return sb.toString();
        }
      } catch (final OXPathException e) {
        return "Error on Node reads";
      }
    case STRING:
    case NUMBER:
    case BOOLEAN:
      return getValue().toString();
    }
    return "NULL Context";
  }

  /**
   * enumerated types of different types in OXPath
   *
   * @author AndrewJSel
   *
   */
  public enum OXPathTypes {
    NODESET("node-set"), STRING("string"), NUMBER("number"), BOOLEAN("boolean"), NULL("null");
    OXPathTypes(final String in) {
      name = in;
    }

    @Override
    public String toString() {
      return name;
    }

    private String name;
  }

  /**
   * Instance field for data type
   */
  private OXPathTypes type;

  /**
   * Instance field for storing field, if used
   */
  private IOXPathNodeList nodes;

  /**
   * Instance field for storing field, if used
   */
  private String string;

  /**
   * Instance field for storing field, if used
   */
  private Double number;

  /**
   * Instance field for storing field, if used
   */
  private boolean bool;

  /**
   * premade object that returns empty output
   */
  public static final OXPathType EMPTYRESULT = new OXPathType();

}
