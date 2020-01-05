/**
 *
 */
package uk.ac.ox.cs.diadem.webapi.interaction;

import java.util.Map;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;

/**
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
public interface ObservedInteraction {

  /**
   * @return
   */
  boolean watchAlert();

  /**
   * @return
   */
  boolean watchForNextPage();

  /**
   * @return
   */
  boolean watchForPageChanges();

  /**
   * @return
   */
  boolean watchAutocomplete();

  /**
   * @return
   */
  Boolean watchForm();

  /**
   * Maps PMNodeId --> DOMNOde
   * 
   * @return
   */
  Map<String, DOMNode> getFormNodesToObserve();

  /**
   * @return
   */
  String getFormNodeLocalId();

}
