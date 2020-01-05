/**
 * Header
 */
package uk.ac.ox.cs.diadem.oxpath.model;

import java.util.Comparator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ForwardingSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;

/**
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 */
class OXPathNodeImmutableSingletonSet extends ForwardingSet<OXPathContextNode> implements IOXPathNodeList {

  private static final Logger LOG = LoggerFactory.getLogger(OXPathNodeImmutableSingletonSet.class);
  private final ImmutableSet<OXPathContextNode> delegate;

  @Override
  protected Set<OXPathContextNode> delegate() {
    return delegate;
  }

  protected OXPathNodeImmutableSingletonSet() {
    delegate = ImmutableSet.of();
  }

  protected OXPathNodeImmutableSingletonSet(final OXPathContextNode node) {
    delegate = ImmutableSet.of(node);
  }

  protected OXPathNodeImmutableSingletonSet(final Comparator<Object> comparator) {
    throw new OXPathRuntimeException("Unsupported, should not be called", LOG);
  }

  @Override
  public OXPathContextNode first() {
    return Iterables.get(this, 0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.oxpath.model.IOXPathNodeList#last()
   */
  @Override
  public OXPathContextNode last() {

    return Iterables.get(this, size() - 1);
  }
}
