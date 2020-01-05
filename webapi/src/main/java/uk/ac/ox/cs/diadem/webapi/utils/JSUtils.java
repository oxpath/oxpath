package uk.ac.ox.cs.diadem.webapi.utils;

import java.util.List;

import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationFormObserver;

/**
 * For internal use, subject to change
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface JSUtils {

  List<String> getImageSources();

  List<String> getLinkHRefs();

  List<String> getIDAttributes();

  List<String> getClassAttributes();

  MutationFormObserver observeFormMutation(DOMElement rootNode);

  List<String> testScratch();

  /**
   * @param url
   * @return
   */
  String makeURLAbsolute(String url);

  /**
   * Selects the text via Selection API https://developer.mozilla.org/en-US/docs/Web/API/Selection If the node is a
   * textNode it will select its text, otherwise it will create a range selection all its children
   *
   * @param node
   * @return
   */
  String selectText(DOMNode node);

  /**
   * Selects the text via Selection API https://developer.mozilla.org/en-US/docs/Web/API/Selection
   *
   * @param node
   * @return
   */
  String selectText(DOMNode startRange, DOMNode endNode);

  /**
   * can be used to convert a DOM tree into text XML
   * 
   * @param subtree
   * @return
   */
  String asXLM(DOMNode subtree);

}
