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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * class encoding OXPath Extraction Markers
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class OXPathExtractionMarker implements OXPathPredicate {

  /**
   * basic constructor
   *
   * @param iLabel
   *          label for the extraction marker
   * @param isAtt
   */
  public OXPathExtractionMarker(final String iLabel, final boolean isAtt) {
    label = iLabel;
    isAttribute = isAtt;
    rebuildFullLabel();
  }

  /**
   * returns the label for the extraction marker
   *
   * @return the label for the extraction marker
   */
  public String getLabel() {
    return fullLabel; // TODO not sure whether this is a good idea
  }

  /**
   * returns {@code true} if this is an attribute extraction marker, {@code false} in case this is a record extraction
   * marker
   *
   * @return {@code true} if this is an attribute extraction marker, {@code false} in case this is a record extraction
   *         marker
   */
  public boolean isAttribute() {
    return isAttribute;
  }

  /**
   * Sets the isAttribute property.
   *
   * @param isAttribute
   *          the new isAttribute.
   */
  public void setAttribute(final boolean isAttribute) {
    this.isAttribute = isAttribute;
  }

  /**
   * Adds a new RDFType
   *
   * @param rdfType
   *          the RDFType to add.
   */
  public void addRdfType(final String rdfType) {
    rdfTypes.add(rdfType);
    rebuildFullLabel();
  }

  private void rebuildFullLabel() {
    if (rdfTypes.isEmpty()) {
      fullLabel = label;
    } else {
      final StringWriter sw = new StringWriter();
      final Iterator<String> iter = rdfTypes.iterator();
      sw.write(label);
      sw.write('(');
      sw.write(iter.next());
      while (iter.hasNext()) {
        sw.write(",");
        sw.write(iter.next());
      }
      sw.write(")");
      fullLabel = sw.toString();
    }
  }

  public Iterator<String> getRdfTypes() {
    return rdfTypes.iterator();
  }

  /**
   * returns the type of the predicate
   *
   * @return the type of the predicate
   */
  @Override
  public OXPathPredicateTypes getType() {
    return OXPathPredicateTypes.EXTRACTIONMARKER;
  }

  /**
   * label for the extraction marker
   */
  private final String label;

  /**
   * label for the extraction marker
   */
  private String fullLabel;

  /**
   * {@code true} if this is an attribute extraction marker, {@code false} in case this is a record extraction marker
   */
  private boolean isAttribute;

  /**
   * the set of RDF Types of this property. Only non-empty for object properties.
   */
  private final List<String> rdfTypes = new ArrayList<String>();
}
