/**
 * Header
 */
package uk.ac.ox.cs.diadem.oxpath.core.domlookup;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.IOXPathNodeList;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathNodeListFactory;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.finder.DOMNodeFinderService;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathNSResolver;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathResult;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIStaleElementRuntimeException;

/**
 * Implementation based on {@link DOMNodeFinderService}
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class DOMLookupByUniqueXPath implements DOMLookup {

  private final WebBrowser browser;
  private final boolean throwOnFailure;

  public DOMLookupByUniqueXPath(final WebBrowser browser, final boolean throwOnFailure) {
    this.browser = browser;
    this.throwOnFailure = throwOnFailure;

  }

  public DOMLookupByUniqueXPath(final WebBrowser browser) {
    this(browser, true);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ArrayList<NodeReference> getNodeReferences(final IOXPathNodeList nodes) throws OXPathException {
    final ArrayList<NodeReference> list = Lists.newArrayList();

    for (final OXPathContextNode contextNode : nodes) {
      list.add(getNodeReference(contextNode));
    }
    return list;

  }

  @Override
  public NodeReference getNodeReference(final OXPathContextNode contextNode) {
    if (contextNode.equals(OXPathContextNode.getNotionalContext()))
      return new NotationalNodeReferenceImpl();
    else
      return new NodeReferenceImplementation(contextNode);
  }

  private final class NotationalNodeReferenceImpl implements NodeReference {
    @Override
    public OXPathContextNode getRenderedNodeOrThrow(final DOMDocument document) {
      return OXPathContextNode.getNotionalContext();
    }

    @Override
    public String toString() {

      return "reference for notationalContext";
    }

    @Override
    public OXPathContextNode getRenderedNodeOrNull(final DOMDocument document) {
      return getRenderedNodeOrThrow(document);
    }
  }

  private final class NodeReferenceImplementation implements NodeReference {
    private String locator;
    private final int contextParent;
    private final int contexLast;

    private NodeReferenceImplementation(final OXPathContextNode contextNode) {
      try {
        locator = contextNode.getNode().getXPathLocator();
      } catch (final WebAPIStaleElementRuntimeException e) {
        if (throwOnFailure)
          throw e;
        locator = "/this/path/doesnt/match/nodes/intentionally";
      }
      contextParent = contextNode.getParent();
      contexLast = contextNode.getLast();
    }

    @Override
    public String toString() {
      return "reference for: " + locator;
    }

    @Override
    public OXPathContextNode getRenderedNodeOrThrow(final DOMDocument document) throws OXPathException {

      final DOMXPathEvaluator evaluator = document.getXPathEvaluator();
      final DOMXPathNSResolver nsResolver = evaluator.createNSResolver(document);

      final String locationURL = document.getBrowser().getLocationURL();

      LOGGER.info("Retrieving context note {} on page {}", locator, locationURL);
      final String hack = locator;// "descendant::*[contains(@itemtype,'Product')][2]";
      final DOMXPathResult result = evaluator.evaluate(hack, document, nsResolver,
          DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);

      if (result == null) {
        LOGGER
            .error(
                "XPath returned null for {} on the  page <{}>. Cannot locate the context node, the current page must have changed significantly. ",
                locator, locationURL);

        throw new OXPathException("XPath returned null for <" + locator + "> on the page <" + locationURL
            + ">. Cannot locate the context node, the current page must have changed significantly.");
      } else if (result.getSnapshotLength() == 0) {
        LOGGER
            .error(
                "XPath returned the empty set for {} on the  page <{}>. Cannot locate the context node, the current page must have changed significantly. ",
                locator, locationURL);

        throw new OXPathException("XPath returned the empty set for <" + locator + "> on the page <" + locationURL
            + ">. Cannot locate the context node, the current page must have changed significantly.");
      }

      if (result.getSnapshotLength() > 1) {
        LOGGER
            .warn(
                "XPath {} expected to retrieve only one context node, but got {}. Return the first node, but this can be wrong ",
                locator, result.getSnapshotLength());
      }
      return new OXPathContextNode(result.snapshotItem(0), contextParent, contexLast);

    }

    @Override
    public OXPathContextNode getRenderedNodeOrNull(final DOMDocument document) {
      try {
        return getRenderedNodeOrThrow(document);
      } catch (final OXPathException e) {
        return null;
      }
    }
  }

  @Override
  public IOXPathNodeList getRenderedNodes(final List<NodeReference> refs, final DOMDocument document)
      throws OXPathException {
    // FIXME BULK XPATH
    final IOXPathNodeList list = OXPathNodeListFactory.newMutableOnLinkedList();
    for (final NodeReference nodeReference : refs) {
      if (throwOnFailure) {
        list.add(nodeReference.getRenderedNodeOrThrow(document));
      } else {
        final OXPathContextNode contextNode = nodeReference.getRenderedNodeOrNull(document);
        if (contextNode != null) {
          list.add(contextNode);
        }
      }
    }
    return list;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(DOMLookupByUniqueXPath.class);

  @Override
  public WebBrowser getWebBrowser() {
    return browser;
  }

}
