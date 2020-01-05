/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.mutation;

import java.util.List;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;

/**
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface DOMMutationRecord extends MutationRecord {
  public enum MutationType {
    attributes, childList, characterData;
  }

  MutationType type();

  DOMNode target();

  List<DOMNode> addedNodes();

  List<DOMNode> removedNodes();

  String attributeName();

  String oldValue();

  String newValue();

}
