package uk.ac.ox.cs.diadem.webapi.dom;

/**
 * 
 * For more information on this interface please see http://www.w3.org/TR/DOM-Level-2-Traversal-Range/
 * 
 */
public interface DOMRange {

  // void setStart(nsIDOMNode refNode, int offset);
  //
  // void setEnd(nsIDOMNode refNode, int offset);
  //
  // void selectNode(nsIDOMNode refNode);
  //
  // void selectNodeContents(nsIDOMNode refNode);

  // void detach();

  DOMBoundingClientRect getBoundingClientRect();
}
