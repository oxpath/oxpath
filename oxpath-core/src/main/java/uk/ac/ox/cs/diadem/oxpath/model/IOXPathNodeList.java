/**
 * Header
 */
package uk.ac.ox.cs.diadem.oxpath.model;

import java.util.Collection;

/**
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public interface IOXPathNodeList extends Collection<OXPathContextNode> {

  OXPathContextNode first();

  OXPathContextNode last();

  // void add(OXPathContextNode oxPathContextNode);
  //
  // void addAll(IOXPathNodeList predResult);

  // @Override
  // public boolean add(OXPathContextNode e);
  //
  // @Override
  // public boolean addAll(Collection<? extends OXPathContextNode> c);

}
