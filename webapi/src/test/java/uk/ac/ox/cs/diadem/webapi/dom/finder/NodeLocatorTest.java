package uk.ac.ox.cs.diadem.webapi.dom.finder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder.Engine;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.testsupport.StandardTestcase;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

/**
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
@Ignore
@RunWith(value = Parameterized.class)
public class NodeLocatorTest extends StandardTestcase {
	
  private static WebBrowser browser;
  final String url = NodeLocatorTest.class.getResource("simple_nodelocator.html").toString();
  private final Engine engineKind;

  public NodeLocatorTest(final Engine engineKind) {
    this.engineKind = engineKind;
  }

  @AfterClass
  public static void shutdown() {

  }

  @Parameters
  public static Collection<Object[]> data() {
	  Collection<Object[]> col = new ArrayList<Object[]>(1);
	  col.add(new Object[] { Engine.WEBDRIVER_FF });
	  return col;
  }

  @Before
  public void resetPage() {
    if (browser == null) {
    	browser = BrowserInitialiseTestHelper.init();
      browser.navigate(url);
    }
  }

  // @AfterClass
  // public static void close() {
  // browser.shutdown();
  // }

  // @Test
  // public void testScratch() throws IOException {
  // database.setKeyPrefix(engineKind.toString());
  // database.setMode(Mode.INTERACTIVE);
  // browser.navigate("http://scholar.google.co.uk/scholar?hl=en&q=xml&btnG=&as_sdt=1%2C5&as_sdtp=", true);
  // final DOMElement element = browser.getContentDOMWindow().getDocument()
  // .selectElementBy(CRITERIA.xpath, "//a[@class='yC0'");
  // final XPathNodePointerRanking canonicalXPath = DOMNodeFinderService.getXPathPointersByAnchor(element);
  // assertTrue(database.check("anchor", canonicalXPath.toString()));
  // }

  @Test
  public void testCanonicalElement() throws IOException {
    database.setKeyPrefix(engineKind.toString());
    final DOMElement element = browser.getContentDOMWindow().getDocument().getElementById("id2");
    final XPathNodePointer canonicalXPath = DOMNodeFinderService.getCanonicalXPath(element);
    assertTrue(database.check("id2", canonicalXPath.toString()));
  }

  @Test
  public void testCanonicalTextNode() throws IOException {
    database.setKeyPrefix(engineKind.toString());
    final DOMElement element = browser.getContentDOMWindow().getDocument().getElementById("id2");

    final DOMNode text1 = element.getChildNodes().item(0);
    assertEquals("text node1", text1.getTextContent(), "text1 ");
    assertTrue(database.check("id2_text1", DOMNodeFinderService.getCanonicalXPath(text1).toString()));

    final DOMNode b = element.getChildNodes().item(1);
    assertTrue(database.check("id2_b", DOMNodeFinderService.getCanonicalXPath(b).toString()));

    final DOMNode text2 = element.getChildNodes().item(2);
    assertEquals("text node2", text2.getTextContent(), " text2");
    assertTrue(database.check("id2_text2", DOMNodeFinderService.getCanonicalXPath(text2).toString()));
  }

  @Test
  public void testTextNodeRetrievial() throws IOException {
    database.setKeyPrefix(engineKind.toString());
    final DOMDocument document = browser.getContentDOMWindow().getDocument();
    final DOMElement element = document.getElementById("id2");

    final DOMNode text1 = element.getChildNodes().item(0);
    assertEquals("text node1", text1.getTextContent(), "text1 ");

    XPathNodePointerRanking ranking = DOMNodeFinderService.getXPathPointersByAttribute(text1);
    assert XPathUtil.checkUniqueMatchingPaths(document, text1, ranking);
    assertTrue(database.check("text1byAttr", ranking.toString()));

    ranking = DOMNodeFinderService.getXPathPointersByAnchor(text1);
    assert XPathUtil.checkUniqueMatchingPaths(document, text1, ranking);
    assertTrue(database.check("text1byAnch", ranking.toString()));

    final DOMNode text2 = element.getChildNodes().item(2);
    assertEquals("text node2", text2.getTextContent(), " text2");

    ranking = DOMNodeFinderService.getXPathPointersByAttribute(text2);
    assert XPathUtil.checkUniqueMatchingPaths(document, text2, ranking);
    assertTrue(database.check("text2byAttr", ranking.toString()));

    ranking = DOMNodeFinderService.getXPathPointersByAnchor(text2);
    assert XPathUtil.checkUniqueMatchingPaths(document, text2, ranking);
    assertTrue(database.check("text2byAnch", ranking.toString()));

    ranking = DOMNodeFinderService.getXPathPointersByPosition(text2);
    assert XPathUtil.checkUniqueMatchingPaths(document, text2, ranking);
    assertTrue(database.check("text2byPos", ranking.toString()));

    // assertTrue(database.check("text2byAttr", DOMNodeFinderService.getXPathPointersByAttribute(text2).toString()));
    // assertTrue(database.check("text2byPos", DOMNodeFinderService.getXPathPointersByPosition(text2).toString()));
    // assertTrue(database.check("id2_text2_pos", DOMNodeFinderService.getXPathAddressesByPosition(text2).toString()));

  }

  @Test
  public void testElementByAttributes() throws IOException {
    database.setKeyPrefix(engineKind.toString());
    final DOMElement element = browser.getContentDOMWindow().getDocument().getElementById("id1");
    assertTrue(database.check("attr_list", DOMNodeFinderService.getXPathPointersByAttribute(element).toString()));
  }

  // @Test
  // public void testTextByAttributes() throws IOException {
  // final DOMNode element = browser.getContentDOMWindow().getDocument().getElementById("id1").getChildNodes().item(0);
  // final List<XPathNodePointer> byAttribute = DOMNodeFinderService.getXPathAddressesByAttribute(element);
  // assertTrue("Expected no results for text nodes", byAttribute.isEmpty());
  //
  // }

  @Test
  public void testElementByPosition() throws IOException {
    database.setKeyPrefix(engineKind.toString());
    final DOMElement element = browser.getContentDOMWindow().getDocument().getElementById("id1");
    assertTrue(database.check("pos_list", DOMNodeFinderService.getXPathPointersByPosition(element).toString()));
  }

  @Test
  public void testElementByText() throws IOException {
    database.setKeyPrefix(engineKind.toString());
    final DOMElement element = browser.getContentDOMWindow().getDocument().getElementById("id1");
    assertTrue(database.check("text_list", DOMNodeFinderService.getXPathPointersByTextContent(element).toString()));
  }

  @Test
  public void testElementBoldByText() throws IOException {
    database.setKeyPrefix(engineKind.toString());
    final DOMElement element = (DOMElement) browser.getContentDOMWindow().getDocument().getElementsByTagName("b")
        .item(0);
    assertTrue(database.check("text_bold_list", DOMNodeFinderService.getXPathPointersByTextContent(element).toString()));
  }

  @Test
  public void testElementBoldByAnchor() throws IOException {
    database.setKeyPrefix(engineKind.toString());
    final DOMDocument document = browser.getContentDOMWindow().getDocument();
    final DOMElement element = (DOMElement) document.getElementsByTagName("b").item(0);

    final XPathNodePointerRanking ranking = DOMNodeFinderService.getXPathPointersByAnchor(element);
    assert XPathUtil.checkUniqueMatchingPaths(document, element, ranking);
    assertTrue(database.check("anchor_bold_list", ranking.toString()));

    // assertTrue(database.check("anchor_bold_list",
    // DOMNodeFinderService.getXPathPointersByAnchor(element).toString()));
  }

  @Test
  public void testElementBoldByPosition() throws IOException {
    database.setKeyPrefix(engineKind.toString());
    final DOMElement element = (DOMElement) browser.getContentDOMWindow().getDocument().getElementsByTagName("b")
        .item(0);
    assertTrue(database.check("bold_pos_list", DOMNodeFinderService.getXPathPointersByPosition(element).toString()));
  }

  @Test
  public void testElementBoldByAll() throws IOException {
    database.setKeyPrefix(engineKind.toString());
    final DOMDocument document = browser.getContentDOMWindow().getDocument();
    final DOMElement element = (DOMElement) document.getElementsByTagName("b").item(0);
    // final XPathNodePointerRanking computeRobustPointers = DOMNodeFinderService.computeRobustPointers(element);
    // System.out.println(computeRobustPointers.size());

    final XPathNodePointerRanking ranking = DOMNodeFinderService.computeRobustPointers(element);
    assert XPathUtil.checkUniqueMatchingPaths(document, element, ranking);
    assertTrue(database.check("bold_robust_list", ranking.toString()));

    // assertTrue(database.check("bold_robust_list", computeRobustPointers.toString()));
  }

  // @Test
  // public void testTextByPosition() throws IOException {
  // database.setKeyPrefix(engineKind.toString());
  // database.setMode(Mode.INTERACTIVE);
  // final DOMNode element = browser.getContentDOMWindow().getDocument().getElementById("id1").getChildNodes().item(0);
  // final List<XPathNodePointer> byAttribute = DOMNodeFinderService.getXPathAddressesByPosition(element);
  // assertTrue(database.check("pos_text", byAttribute.toString()));
  // }

}