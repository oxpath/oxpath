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
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.finder.DOMNodeFinderService;
import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathNodePointer;
import uk.ac.ox.cs.diadem.webapi.dom.finder.XPathNodePointerRanking;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathNSResolver;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathResult;

/**
 * Implementation based on {@link DOMNodeFinderService}
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public class DOMLookupByRobustXPath implements DOMLookup {

  private static final Logger LOGGER = LoggerFactory.getLogger(DOMLookupByRobustXPath.class);
  private final WebBrowser browser;
  private final boolean throwOnFailure;

  public DOMLookupByRobustXPath(final WebBrowser browser, final boolean throwOnFailure) {
    this.browser = browser;
    this.throwOnFailure = throwOnFailure;

  }

  public DOMLookupByRobustXPath(final WebBrowser browser) {
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
      return new NodeReference() {

        @Override
        public OXPathContextNode getRenderedNodeOrThrow(final DOMDocument document) {
          return OXPathContextNode.getNotionalContext();
        }

        @Override
        public OXPathContextNode getRenderedNodeOrNull(final DOMDocument document) {
          return getRenderedNodeOrThrow(document);
        }
      };
    else
      return new NodeReference() {

        @Override
        public OXPathContextNode getRenderedNodeOrThrow(final DOMDocument document) throws OXPathException {
          final DOMNode target = contextNode.getNode();

          final long start = System.currentTimeMillis();
          final XPathNodePointerRanking pointers = DOMNodeFinderService.computeRobustPointers(target);
          LOGGER.debug("Computing NodeReferences took {}ms, produced {} pointers",
              (System.currentTimeMillis() - start), pointers.size());
          final DOMXPathEvaluator evaluator = document.getXPathEvaluator();
          final DOMXPathNSResolver nsResolver = evaluator.createNSResolver(document);

          for (final XPathNodePointer xPathNodePointer : pointers) {
            final DOMXPathResult result = evaluator.evaluate(xPathNodePointer.getXPath(), document, nsResolver,
                DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);

            if (result != null) {
              final long size = result.getSnapshotLength();
              if (size > 0)
                return new OXPathContextNode(result.snapshotItem(0), contextNode.getParent(), contextNode.getLast());
            }
          }
          LOGGER
              .error(
                  "Cannot retrieve the node {} on the current page. The current page must have different and our heuristics failed. ",
                  contextNode.getNode());

          throw new OXPathException("Cannot retrieve the node <" + contextNode.getNode()
              + "> on the current page. The current page must have different and our heuristics failed");
          // LOGGER.error("Returning an empty set result");
          // return OXPathContextNode.getNotionalContext();

        }

        @Override
        public OXPathContextNode getRenderedNodeOrNull(final DOMDocument document) {
          try {
            return getRenderedNodeOrThrow(document);
          } catch (final OXPathException e) {
            return null;
          }
        }
      };

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

  @Override
  public WebBrowser getWebBrowser() {
    return browser;
  }

}
