package uk.ac.ox.cs.diadem.webapi.css;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMRange;

/**
 * Represents an overlay of a given {@link DOMNode}, styled by CSS rules
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface StyledNode {

  // public static interface StyledNodeBuilder {
  //
  // StyledNodeBuilder appendKeyValueContent(Pair<String, String> line, String... classes);
  //
  // StyledNodeBuilder setAttribute(Pair<String, String> line);
  //
  // StyledNodeBuilder setClasses(String... classes);
  //
  // StyledNode build();
  //
  // }

  /**
   * The xpath locator for the node to overlay
   * 
   * @return
   */
  String getLocator();

  /**
   * the list of css classes for the current node
   * 
   * @return
   */
  Collection<String> classes();

  /**
   * Navigates the tree
   * 
   * @return
   */
  Collection<StyledNode> children();

  /**
   * 
   * @return
   */
  StyledNode parent();

  /**
   * In case of text nodes, we create {@link DOMRange}. See {@link StyledRangeNode}
   * 
   * @return
   */
  boolean hasRange();

  boolean hasInfoBox();

  StyledInfoNode getInfoNode();

  /**
   * The information to display as additional overlay. They are in form of key/value data, each of them associated with
   * a list of css classes
   * 
   * @return
   */
  Pair<List<Pair<String, String>>, List<List<String>>> getInfoBox();

  // void toggleCSSClass(String className);
  //
  // void addCSSClass(String className);
  //
  // void removeCSSClass(String className);
  //
  // boolean containsCSSClass(String className);

}
