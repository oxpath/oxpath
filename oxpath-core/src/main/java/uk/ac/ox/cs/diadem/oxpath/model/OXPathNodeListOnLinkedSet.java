/**
 * Header
 */
package uk.ac.ox.cs.diadem.oxpath.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;

/**
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 */
class OXPathNodeListOnLinkedSet extends ForwardingSet<OXPathContextNode> implements IOXPathNodeList {

  private static final Logger LOG = LoggerFactory.getLogger(OXPathNodeListOnLinkedSet.class);
  private final LinkedHashSet<OXPathContextNode> delegate;

  @Override
  protected Set<OXPathContextNode> delegate() {
    return delegate;
  }

  protected OXPathNodeListOnLinkedSet() {
    delegate = Sets.newLinkedHashSet();
  }

  protected OXPathNodeListOnLinkedSet(final OXPathContextNode node) {
    delegate = Sets.newLinkedHashSet(ImmutableList.of(node));
  }

  protected OXPathNodeListOnLinkedSet(final Comparator<Object> comparator) {
    throw new OXPathRuntimeException("Unsupported, should not be called", LOG);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(final OXPathContextNode e) {
    return delegate().add(e);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addAll(final Collection<? extends OXPathContextNode> c) {
    if (size() > 0) {
      // LOG.warn("Potential lost of document order using linked list");
    }
    return delegate().addAll(c);
  }

  @Override
  public OXPathContextNode first() {
    return Iterables.get(this, 0);
  }

  @Override
  public OXPathContextNode last() {

    return Iterables.get(this, size() - 1);
  }
}
