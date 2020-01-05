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

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Object for holding a set of OXPath context nodes. We use inheritance instead of composition as we don't make any
 * internal dependendency on the class implementation.
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
class OXPathNodeListOnTreeSet extends TreeSet<OXPathContextNode> implements IOXPathNodeList {

  private static final Logger LOG = LoggerFactory.getLogger(OXPathNodeListOnTreeSet.class);
  private boolean shouldBeImmutable;

  protected OXPathNodeListOnTreeSet() {
    super();
  }

  protected OXPathNodeListOnTreeSet(final OXPathContextNode node) {
    // super(ImmutableList.of(node));
    super();
    add(node);
    shouldBeImmutable = true;
  }

  protected OXPathNodeListOnTreeSet(final Comparator<Object> comparator) {
    super(comparator);
  }

  /**
   * Default id to get rid of compiler warning. Recommend not serializing - not tested!
   */
  private static final long serialVersionUID = 1L;

  @Override
  public boolean add(final OXPathContextNode e) {
    if (shouldBeImmutable && (size() > 1))
      throw new RuntimeException("It's actually not immutable as thought");
    return super.add(e);
  }

  @Override
  public boolean addAll(final Collection<? extends OXPathContextNode> c) {

    return super.addAll(c);
  }
}
