/**
 * Header
 */
package uk.ac.ox.cs.diadem.oxpath.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;

/**
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 */
class OXPathNodeListOnLinkedList extends ForwardingList<OXPathContextNode> implements IOXPathNodeList {

  private static final Logger LOG = LoggerFactory.getLogger(OXPathNodeListOnLinkedList.class);
  private final LinkedList<OXPathContextNode> delegate;

  @Override
  protected List<OXPathContextNode> delegate() {
    return delegate;
  }

  protected OXPathNodeListOnLinkedList() {
    delegate = Lists.newLinkedList();
  }

  protected OXPathNodeListOnLinkedList(final OXPathContextNode node) {
    delegate = Lists.newLinkedList(ImmutableList.of(node));
  }

  protected OXPathNodeListOnLinkedList(final Comparator<Object> comparator) {
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
