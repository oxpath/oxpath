/**
 *
 */
package uk.ac.ox.cs.diadem.oxpath.core.extraction;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathExtractionNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;

/**
 * Extractor implementation. We assume all method calls(except the constructor) are called by a proxy object generated
 * by {@code OXPathExtractor}. Therefore, we don't check if the same node has already been extracted (the memoizer does
 * this for us).
 *
 * @author Giovanni Grasso <gio@oxpath.org>
 *
 */
@Deprecated
public class SimpleExtractor implements Extractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleExtractor.class);

  /**
   * basic constructor, assigns
   *
   * @param iOS
   */
  public SimpleExtractor(final ObjectOutputStream iOS) {
    os = iOS;
  }

  /*
   * (non-Javadoc)
   *
   * @see uk.ac.ox.comlab.diadem.oxpath.core.extraction.Extractor#extractNode(diadem.common.web.dom.DOMNode,
   * java.lang.String, int)
   */
  /**
   * Allows the extraction of the node specified by the pair <tt>(context,label)</tt> and returns a unique identifier
   * (as an {@code int}) that uniquely identifies this extraction marker. May be a previously occurring identifier if
   * the <tt>(context,label)</tt> has already been extracted (via a call to this method with this object).
   *
   * @param context
   *          the context node in this label
   * @param label
   *          the label of this node in the extraction marker
   * @param parent
   *          the parent of the extraction node specified by <tt>(context,label)</tt>
   * @return a unique identifier for this extraction (though by OXPath's merge semantics, this extraction node may have
   *         already been created - in this case, the previous identifier is reused)
   * @throws OXPathException
   *           in case the parent of this node is inconsistent with a recurring node (one already created with a
   *           previous call to this method)
   */
  @Override
  public Integer extractNode(final DOMNode context, final String label, final Integer parent) throws OXPathException {
    try {
      final OXPathExtractionNode node = new OXPathExtractionNode(++lastNode, parent, label);
      os.writeObject(node);
      // if (LOGGER.isDebugEnabled()) {
      LOGGER.info("Extracted Record node '{}'", label);
      // }
      return lastNode;
    } catch (final IOException e) {
      throw new OXPathException("IOException when trying to write extraction node " + label + " to output stream");
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see uk.ac.ox.comlab.diadem.oxpath.core.extraction.Extractor#extractNode(diadem.common.web.dom.DOMNode,
   * java.lang.String, int, java.lang.String)
   */
  /**
   * Allows the extraction of the node specified by the pair <tt>(context,label)</tt> and returns a unique identifier
   * (as an {@code int}) that uniquely identifies this extraction marker. May be a previously occurring identifier if
   * the <tt>(context,label)</tt> has already been extracted (via a call to this method with this object).
   *
   * @param context
   *          the context node in this label
   * @param label
   *          the label of this node in the extraction marker
   * @param parent
   *          the parent of the extraction node specified by <tt>(context,label)</tt>
   * @param value
   *          the value associated with this extraction node
   * @return a unique identifier for this extraction (though by OXPath's merge semantics, this extraction node may have
   *         already been created - in this case, the previous identifier is reused)
   * @throws OXPathException
   *           in case the parent of this node is inconsistent with a recurring node (one already created with a
   *           previous call to this method)
   */
  @Override
  public Integer extractNode(final DOMNode context, final String label, final Integer parent, final String value)
      throws OXPathException {
    try {
      final OXPathExtractionNode node = new OXPathExtractionNode(++lastNode, parent, label, value);
      os.writeObject(node);
      // if (LOGGER.isDebugEnabled()) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Extracted attribute '{}' = {}...", label, StringUtils.left(value, 50));
      }
      // }
      return lastNode;
    } catch (final IOException e) {
      throw new OXPathException("IOException when trying to write extraction node <" + label + ">=<" + value
          + "> to output stream.");
      // Error: " + e.getCause().getMessage());
    }
  }

  /**
   * Signals end of extraction
   */
  @Override
  public void endExtraction() throws OXPathException {
    try {
      os.writeObject(OXPathExtractionNode.returnEndNode());
    } catch (final IOException e) {
      throw new OXPathException("IOException when trying to write final extraction node to output stream");
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see uk.ac.ox.comlab.diadem.oxpath.utils.OXPathCache#clear(diadem.common.web.dom.DOMDocument)
   */
  /**
   * Clears all memoized data for the input {@code DOMDocument}. Will be overridden as long as a proxy object is used
   * (should always be used for OXPath classes overridding this method).
   *
   * @param page
   *          {@code DOMDocument} we are removing all memoized results for, presumably because the page is being closed
   *          in PAAT
   */
  @Override
  public Boolean clear(final DOMDocument page) {
    // This should never be called outside the proxy
    // throw new RuntimeException("Don't use the clear(page) method of the " + this.getClass().toString()
    // + " outside of a proxy class");
    LOGGER.warn("Clear on '{}' without a proxy has not effect ", this.getClass().toString());
    return true;
  }

  private final ObjectOutputStream os;
  private int lastNode = 0;// this is the results node, so we increment before giving the number to a new node

}
