/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode.Type;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class FinderUtils {

  private static final class AppendTextFunction implements Function<XPathNodePointer, XPathNodePointer> {
    private final DOMNode node;

    private AppendTextFunction(final DOMNode node) {
      this.node = node;
    }

    @Override
    public XPathNodePointer apply(final XPathNodePointer input) {
      final String xPath = input.getXPath();
      return new XPathNodePointerImpl(xPath + FinderUtils.getTextNodePositionalStep(node), input.getScore(),
          input.getType());
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(FinderUtils.class);

  private FinderUtils() {
    // prevent instantiation
  }

  static String getTextNodePositionalStep(final DOMNode node) {
    if (node.getNodeType() != DOMNode.Type.TEXT) {
      LOGGER.error("Expected DOM Text node but got {} ", node);
      throw new WebAPIRuntimeException("Expected DOM Text node", LOGGER);
    }
    return "/text()[" + FinderUtils.getSameNameSiblingPosition(node) + "]";
  }

  /**
   * Gets the position of a DOM node among its siblings with the same name. This is used as positional predicate in the
   * canonical XPath expression.
   * 
   * @param node the DOM node
   * @return the position
   */
  public static int getSameNameSiblingPosition(final DOMNode node) {
    int position = 1;
    DOMNode previousSibling = node;

    while (previousSibling != null) {
      LOGGER.debug("asking previous sibling of {}", previousSibling);
      previousSibling = previousSibling.getPreviousSibling();
      if (previousSibling == null) {
        break;
      }
      final boolean elementOrText = (previousSibling.getNodeType() == DOMNode.Type.ELEMENT)
          || (previousSibling.getNodeType() == DOMNode.Type.TEXT);
      if (elementOrText && (previousSibling.getNodeName().equalsIgnoreCase(node.getNodeName()))) {
        position++;
      }
    }
    return position;
  }

  /**
   * If the given node is a {@link DOMElement} it is returned by cast. If it is a textnode, its parent is returned.
   * otherwise null is returned
   * 
   * @param node
   * @return
   */
  static DOMElement castToElementOrParentOrNull(final DOMNode node) {

    if (node.getNodeType() == DOMNode.Type.TEXT)
      return (DOMElement) node.getParentNode();
    else if ((node.getNodeType() == DOMNode.Type.ELEMENT))
      return (DOMElement) node;
    else
      return null;

  }

  public static XPathNodePointerRanking manageTextNodeIfAny(final DOMNode node,
      final XPathNodePointerRanking candidateList) {

    // if is Text Node, we append the remider part for the text node address w.r.t. its parent
    if (node.getNodeType() == DOMNode.Type.TEXT)
      return XPathNodePointerRankingOnSet.transform(candidateList, new AppendTextFunction(node));
    else
      return candidateList;

  }

  public static XPathNodePointer manageTextNodeIfAny(final DOMNode node, final XPathNodePointer candidate) {

    // if is Text Node, we append the remider part for the text node address w.r.t. its parent
    if (node.getNodeType() == DOMNode.Type.TEXT) {
      final XPathNodePointerRankingOnSet newRank = XPathNodePointerRankingOnSet.newRank();
      newRank.add(candidate);
      return XPathNodePointerRankingOnSet.transform(newRank, new AppendTextFunction(node)).first();
    } else
      return candidate;

  }

  public static String elementName(final DOMNode node) {
    if (node.getNodeType() == Type.TEXT)
      return "text()";
    else {
      final String localName = node.getLocalName();
      if (localName.matches(".*\\W+.*"))
        return "*[name()='" + localName + "']";
      else
        return localName;
    }
  }

  public static void main(final String[] args) {
    final String localName = "o:p";
    if (localName.matches(".*\\W+.*")) {
      System.out.println("*[name()='" + localName + "']");
    } else {
      System.out.println(localName);
    }

  }

  public static boolean checkThreshold(final XPathNodePointerRanking candidateList, final Integer threshold) {
    if (candidateList.isEmpty())
      return false;
    // we can stop if the threshold if reached
    if (candidateList.first().getScore() >= threshold)
      return true;
    return false;
  }
}
