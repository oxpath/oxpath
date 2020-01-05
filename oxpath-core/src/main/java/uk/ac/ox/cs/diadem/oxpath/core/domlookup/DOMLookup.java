/**
 * Package supporting core OXPath functionality.  Contains the interface and implementation for
 * retrieving current DOM references from references on old DOMs (obtained when the DOM was
 * previously rendered before a {@code browser.back()} call.
 */
package uk.ac.ox.cs.diadem.oxpath.core.domlookup;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.IOXPathNodeList;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;

/**
 * These interface specifies classes that compensate for an issue (observed in Mozilla), where DOM references perpetuate
 * after new pages are loaded and the old page is revisited by invoking {@code browser.back()}. Prepare references with
 * {@code getNodeReferences(nodes)}. Retrieve new nodes via a call to {@code getRenderedNode(staleNode,newDocument)}.
 *
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */

public interface DOMLookup {

  /**
   * Creates a list of references to nodes so that they can be found in a new document. Remember to handle the notional
   * root if passed it!
   *
   * @param nodes
   *          the list of OXPathNodes
   * @return references to these nodes retrievable in a new document
   * @throws OXPathException
   *           in case of browser error (will carry the throwable cause)
   */
  public ArrayList<NodeReference> getNodeReferences(IOXPathNodeList nodes) throws OXPathException;

  public IOXPathNodeList getRenderedNodes(List<NodeReference> refs, DOMDocument document) throws OXPathException;

  WebBrowser getWebBrowser();

  public NodeReference getNodeReference(OXPathContextNode context);

}
