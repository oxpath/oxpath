package uk.ac.ox.cs.diadem.webapi.webdriver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import uk.ac.ox.cs.diadem.util.misc.EscapingUtils;
import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMBoundingClientRect;
import uk.ac.ox.cs.diadem.webapi.dom.DOMCSS2Properties.CssProperty;
import uk.ac.ox.cs.diadem.webapi.dom.DOMCSSStyleDeclaration;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNamedNodeMap;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode.Type;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNodeList;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

public class WebDriverOnXVFBTestCase {
	
  static WebBrowser browser;

  @AfterClass
  public static void shutdown() {
    browser.shutdown();
  }

  @BeforeClass
  public static void init() {
	  browser = BrowserInitialiseTestHelper.init();
  }

  @Before
  public void loadPage() {
    browser.navigate("http://diadem.cs.ox.ac.uk/test/re/fast/wwagency/");
  }

  @Test
  public void testCreateElementAndSetGetAttribute() {
    final DOMDocument d = document();
    final DOMElement span = d.createElement("span");
    assertEquals("tagname", "span", span.getLocalName());
    assertEquals("parent tagname", "body", span.getParentNode().getLocalName());
    span.setAttribute("aName", "aValue");
    assertEquals("attribute just set", "aValue", span.getAttribute("aName"));
  }

  @Test
   @Ignore
  public void testWindowEquality() {
    final DOMWindow window1 = window();
    final DOMDocument d1 = window1.getDocument();
    browser.navigate("http://www.harveyscott.co.uk/search/");
    assertTrue("should be stale ", d1.isStale());
    final DOMWindow w2 = window();
    final DOMDocument d2 = w2.getDocument();
    assertTrue("should be not stale ", !d2.isStale());

  }

  @Test
   @Ignore
  public void testNodeVisibility() {
    //browser.navigate("http://www.struttandparker.com/");
    //final DOMNode node = XPathUtil.getFirstNode("//label[@id='residential_buy_label']/preceding-sibling::input[@id='residential_buy']", browser);
	  
	browser.navigate("https://www.w3schools.com/html/html_examples.asp");
    final DOMNode node = XPathUtil.getFirstNode("//div[@id='googleSearch']", browser);
    assertTrue("should be invisible", !node.isVisible());

  }

  @Test
   @Ignore
  public void testVisibility() {
    browser.navigate("http://www.harveyscott.co.uk/search/");
    final DOMElement el = (DOMElement) XPathUtil.getFirstNode("/html/body/div/div/div/div[2]/div[6]/strong/a", browser);
    assertTrue("should be visible", el.isVisible());

  }

  @Test
  @Ignore
  public void testVisibleButNotInScrolledView() {
    browser.navigate("http://www.jonathanleighton.com/projects/date-input/");
    final FirefoxDriver f = (FirefoxDriver) browser.getWindowFrame();
    final WebElement element = f.findElementByXPath(".//*[@id='license']");
    // final DOMElement el = (DOMElement) XPathUtil.getFirstNode(".//*[@id='license']", browser);
    assertTrue("should be visible", element.isDisplayed());

  }

  @Test
  public void testRemoveAttribute() {
    final DOMDocument d = document();
    final DOMElement span = d.createElement("span");
    span.setAttribute("aName", "aValue");
    span.removeAttribute("aName");
    assertNull("null", span.getAttribute("aName"));
  }

  @Test
  public void testInnerHTML() {
    final DOMElement body = body();
    assertEquals("tagname is body", "body", body.getLocalName());
    assertEquals("innerHTML", 6818, body.getInnerHTML().length());

  }

  @Test
  public void testFormElements() {
    final DOMDocument d = document();
    final DOMElement form = (DOMElement) d.getElementsByTagName("form").item(0);
    final Iterable<DOMElement> elements = form.htmlUtil().asHTMLForm().getElements();
    assertEquals("elements are 7", 7, Iterables.size(elements));
  }

  @Test
  public void testDocumentTextContent() {
    final DOMDocument d = document();
    final String textContent = d.getTextContent();
    assertEquals("document text content", null, textContent);

  }

  @Test
  public void testDocumentChildren() {
    final DOMDocument d = document();
    assertNotSame("first and last document children are not the same", d.getFirstChild(), d.getLastChild());
    assertEquals("only two children", 2, d.getChildNodes().getLength());
    assertEquals("only two children", DOMNode.Type.DOCUMENT_TYPE, d.getFirstChild().getNodeType());

  }

  @Test
  public void testCompareDocumentPosition() {

    assertEquals("same node", 0, document().compareDocumentPosition(document()));
    assertEquals("text is contained by and follows (16 + 4) ", 20, document().compareDocumentPosition(firstTextNode()));
    assertEquals("document contains and precedes (8 + 2)", 10, firstTextNode().compareDocumentPosition(document()));

    assertEquals("secondElement is contained and follows document (16 +4)", 20,
        document().compareDocumentPosition(secondElement()));
    assertEquals("document contains and precedes (8 + 2)", 10, secondElement().compareDocumentPosition(document()));

    assertEquals("same node", 0, firstTextNode().compareDocumentPosition(firstTextNode()));
    assertEquals("first precedes second (4)", 4, firstTextNode().compareDocumentPosition(secondElement()));
    assertEquals("second is preceded by first (2)", 2, secondElement().compareDocumentPosition(firstTextNode()));
    assertEquals("body contains and precedes (8 + 2)", 10, secondElement().compareDocumentPosition(body()));

  }

  @Test(timeout = 3000)
  @Ignore
  public void testCompareDocumentPositionDisconnected() {
    final DOMDocument old = document();
    browser.navigate("http://diadem.cs.ox.ac.uk/test/re/fast/oliverjames/");
    assertEquals("disconnected", 1, old.compareDocumentPosition(document()));
  }

  @Test
  public void testDocumentSameNode() {
    final DOMDocument d = document();
    assertTrue("is same node", d.isSameNode(document()));

    assertEquals("is same node", 0, d.compareDocumentPosition(d));
    assertEquals("is same node", 20, d.compareDocumentPosition(body()));

  }

  @Test
  public void testIsSameNode() {
    assertTrue("is same node", body().isSameNode(body()));
    assertFalse("is different node", body().isSameNode(firstTextNode()));
    assertFalse("is different node", body().isSameNode(document()));
    assertFalse("is different node", firstTextNode().isSameNode(body()));
    assertTrue("is same node", firstTextNode().isSameNode(firstTextNode()));
    assertFalse("is different node", firstTextNode().isSameNode(thirdTextNode()));

  }

  @Test
  public void testIsEqualNode() {
    assertTrue("is equal node", document().isEqualNode(document()));
    assertTrue("is equal node", body().isEqualNode(body()));
    assertFalse("is different node", body().isEqualNode(firstTextNode()));
    assertFalse("is different node", body().isEqualNode(document()));
    assertFalse("is different node", firstTextNode().isEqualNode(body()));
    assertTrue("is equal node", firstTextNode().isEqualNode(firstTextNode()));
    assertFalse("is different node", firstTextNode().isEqualNode(thirdTextNode()));

  }

  @Test
  public void testNodeType() {
    assertEquals("node type", document().getNodeType(), DOMNode.Type.DOCUMENT);

    assertEquals("node type", body().getNodeType(), DOMNode.Type.ELEMENT);

    assertEquals("node type", firstTextNode().getNodeType(), DOMNode.Type.TEXT);

  }

  @Test
  public void testDocumentElement() {

    assertEquals("document Element", body().getParentNode(), document().getDocumentElement());

  }

  @Test
  public void testRemoveChildForElement() {
    final DOMElement originalSecond = secondElement();
    final DOMNode removed = body().removeChild(firstTextNode());
    assertFalse("still equals", removed.isEqualNode(firstTextNode()));
    // now is an element
    assertEquals("still equals", originalSecond, body().removeChild(firstTextNode()));

  }

  @Test(expected = WebAPIRuntimeException.class)
  public void testRemoveChildForText() {

    firstTextNode().removeChild(firstTextNode());

  }

  @Test
  @Ignore
  public void testDetachedFromDOM() {
    final DOMNode removed = body().removeChild(firstTextNode());
    assertNull("null sibling", removed.getNextSibling());
    assertNull("null parent", removed.getParentNode());

  }

  @Test
  public void testSiblings() {
    assertEquals("first sibling is header", "header", firstTextNode().getNextSibling().getLocalName());
    assertTrue("second sibling is second text node",
        thirdTextNode().isEqualNode(firstTextNode().getNextSibling().getNextSibling()));
    assertTrue("second sibling is second text node",
        firstTextNode().isEqualNode(thirdTextNode().getPreviousSibling().getPreviousSibling()));

    assertTrue("back and forth", firstTextNode().isEqualNode(firstTextNode().getNextSibling().getPreviousSibling()));

    assertNull("No siblings for body", body().getNextSibling());
    assertNull("No siblings for document", document().getNextSibling());
    assertTrue("previous sibling of body is garbage",
        EscapingUtils.isGarbageText(body().getPreviousSibling().getTextContent()));
    assertNull("No siblings for document", document().getPreviousSibling());
  }

  @Test
  public void testParentNode() {
    assertNull("node parent", document().getParentNode());

    assertEquals("node parent", "html", body().getParentNode().getLocalName());

    assertEquals("node parent", body(), firstTextNode().getParentNode());

  }

  @Test
  public void testHTML() {
    final String innerHTML = document().getDocumentElement().getInnerHTML();
//    System.out.println(innerHTML);
    assertEquals("node parent", 7951, innerHTML.length());

  }

  @Test
  public void testSetTextContent() {
    body().setTextContent("a");
    assertEquals("text is", "a", body().getTextContent());
    firstTextNode().setTextContent("b");
    assertEquals("text is", "b", firstTextNode().getTextContent());

  }

  @Test
  public void testAppendChildForElement() {

    final DOMElement newChild = document().createElement("span");
    final DOMNode appended = body().appendChild(newChild);
    assertEquals("node parent of appendend is body", body(), appended.getParentNode());
    final DOMNode firstTextNode = firstTextNode();
    final DOMNode header = firstTextNode.getNextSibling();
    final DOMNode movedText = header.appendChild(firstTextNode);
    assertEquals("node parent of appendend is body", header, movedText.getParentNode());
  }

  @Test(expected = WebAPIRuntimeException.class)
  public void testAppendChildOnDocumentNonAllowed() {

    final DOMElement newChild = document().createElement("span");
    document().appendChild(newChild);
  }

  @Test(expected = WebAPIRuntimeException.class)
  public void testAppendChildOnTextNodeNonAllowed() {

    final DOMElement newChild = document().createElement("span");
    firstTextNode().appendChild(newChild);
  }

  @Test
  public void testLocalName() {
    assertNull("document local name", document().getLocalName());

    assertEquals("body local name", "body", body().getLocalName());

    assertNull("text node local name", firstTextNode().getLocalName());

  }

  @Test
  public void testNodeName() {
    assertEquals("document node name", "#document", document().getNodeName());

    assertEquals("body node name", "body", body().getNodeName());

    assertEquals("text node name", "#text", firstTextNode().getNodeName());

  }

  @Test
  public void testTextContent() {

    assertEquals("body text content", 1623, body().getTextContent().length());

    assertEquals("text content", 2, firstTextNode().getTextContent().length());

    assertEquals("text content", 442, secondElement().getTextContent().length());
    assertEquals("text content", 3, thirdTextNode().getTextContent().length());
    assertEquals("text content", 18, label().getTextContent().length());
    assertEquals("text content", 18, labelText().getTextContent().length());

  }

  @Test
  public void testXPathLocator() {

    assertEquals("body locator", "/html[1]/body[1]", body().getXPathLocator());

    assertEquals("", "/html[1]/body[1]/text()[1]", firstTextNode().getXPathLocator());

    final DOMNamedNodeMap<DOMNode> attributes = label().getAttributes();
    assertEquals("", "id('srchform')/div[1]/label[1]/@for", attributes.getNamedItem("for").getXPathLocator());
  }

  @Test
  public void testInsertElementBeforeElement() {

    final DOMNode newChild = document().createElement("span");
    assertEquals("inserted is span", "span", body().insertBefore(newChild, secondElement()).getLocalName());
    assertEquals("inserted is span", "span", body().getChildNodes().item(1).getLocalName());
    assertTrue("inserted is equal to newChild", newChild.isEqualNode(body().getChildNodes().item(1)));
    assertTrue("inserted is same of newChild", newChild.isSameNode(body().getChildNodes().item(1)));
  }

  @Test
  public void testInsertTextBeforeElement() {

    final DOMNode originalFirst = firstTextNode();
    final DOMNode added = body().insertBefore(thirdTextNode(), secondElement());
    assertTrue("inserted is text", added.getNodeType() == Type.TEXT);
    assertTrue("first child  is equal to original", firstTextNode().isSameNode(originalFirst));
    assertTrue("inserted is in second position", added.isSameNode(body().getChildNodes().item(1)));

  }

  @Test
  public void testInsertTextBeforeText() {

    final DOMNode originalSecond = secondElement();
    final DOMNode added = body().insertBefore(thirdTextNode(), firstTextNode());
    assertTrue("inserted is text", added.getNodeType() == Type.TEXT);
    assertTrue("third child  is equal to original second", originalSecond.isSameNode(body().getChildNodes().item(2)));
    assertTrue("inserted is in first position", added.isSameNode(body().getChildNodes().item(0)));

  }

  @Test
  @Ignore
  public void testEventListeners() {

    final DOMElement originalSecond = secondElement();
    final FirefoxDriver driver = (FirefoxDriver) browser.getWindowFrame();
    driver.executeScript("getEventListeners(document.body);");

  }

  @Test
  public void testInsertElementBeforeText() {

    final DOMNode newChild = document().createElement("span");
    assertEquals("inserted is span", "span", body().insertBefore(newChild, firstTextNode()).getLocalName());
    assertEquals("inserted is span", "span", body().getChildNodes().item(0).getLocalName());
    assertTrue("inserted is equal to newChild", newChild.isEqualNode(body().getChildNodes().item(0)));
    assertTrue("inserted is same of newChild", newChild.isSameNode(body().getChildNodes().item(0)));
  }

  @Test
  public void testChildrenOfTextNode() {

    final DOMNode textNode = firstTextNode();
    assertNull("first child is null", textNode.getFirstChild());
    assertNull("last child is null", textNode.getLastChild());
    assertEquals("empty", 0, textNode.getChildNodes().getLength());
  }

  @Test
  @Ignore
  public void testBodyBoundingBox() {

    final DOMBoundingClientRect rect = body().getBoundingClientRect();
    assertEquals("bottom", 430, (int) rect.getBottom());
    assertEquals("height", 290, (int) rect.getHeight());
    assertEquals("left", 0, (int) rect.getLeft());
    assertEquals("right", 940, (int) rect.getRight());
    assertEquals("top", 140, (int) rect.getTop());
    assertEquals("width", 940, (int) rect.getWidth());

  }

  @Test
  @Ignore
  public void testRange() {
    final DOMNode labelText = labelText();
    final DOMBoundingClientRect rect = document().createRange(labelText, 0, labelText, 5).getBoundingClientRect();
    assertNotNull(rect);
    assertEquals("left", 0, (int) rect.getLeft());
    assertEquals("top", 209, (int) rect.getTop());
    assertEquals("right", 35, (int) rect.getRight());
    assertEquals("bottom", 225, (int) rect.getBottom());
    assertEquals("width", 35, (int) rect.getWidth());
    assertEquals("height", 16, (int) rect.getHeight());

  }

  @Test
  public void testStyle() {
    final DOMCSSStyleDeclaration style = label().getComputedStyle();
    final String red = "rgba(255, 0, 0, 1)";
    style.setProperty(CssProperty.color.getPropertyName(), red);

    assertEquals("color is red", red, style.getPropertyValue(CssProperty.color.getPropertyName()));
  }

  @Test
  public void testBulkStyle() {
    final DOMCSSStyleDeclaration style = label().getComputedStyle();
    final String red = "rgba(255, 0, 0, 1)";
    final ImmutableMap<String, String> map = ImmutableMap.of(CssProperty.color.getPropertyName(), red,
        CssProperty.border.getPropertyName(), "1px solid red");
    style.setProperties(map);

    assertEquals("color is red", red, style.getPropertyValue(CssProperty.color.getPropertyName()));
  }

  @Test
  public void testPrettyToString() {

    assertEquals("pretty toString", "label", label().toString());
  }

  @Test
  public void testAttributes() {
    assertNull("document attributes", document().getAttributes());
    assertNull("text node attributes", firstTextNode().getAttributes());
    assertEquals("empty", 0, body().getAttributes().getLength());
    assertEquals("attribute for", "sale_type_id", label().getAttributes().getNamedItem("for").getNodeValue());
  }

  @Test
  public void testScroll() {

    assertEquals("no scrolling", 0, window().getScrollX());
    assertEquals("no scrolling", 0, window().getScrollY());
  }

  @Test
  public void testLastChild() {

    final DOMNode lastChild = body().getLastChild();
    final DOMNamedNodeMap<DOMNode> attributes = lastChild.getAttributes();
    final DOMNode namedItem = attributes.getNamedItem("id");
    assertEquals("last child", "colorbox", namedItem.getNodeValue());
    assertEquals("last child", Type.ELEMENT, body().getNodeType());

  }

  @Test
  public void testNodeValue() {
    assertNull("document node type", document().getNodeValue());
    assertNull("body node type", body().getNodeValue());
    assertTrue("text node value", EscapingUtils.isGarbageText(firstTextNode().getNodeValue()));
  }

  @Test
  public void testNoModificationEvents() {
    final DOMNode item = window().getDocument().getElementsByTagName("body").item(0);
    final DOMMutationObserver observer = item.registerMutationObserver(true, true, true, false, null);
    assertTrue("empty", observer.takeRecords().isEmpty());
    observer.disconnect();
  }

  @Test
  @Ignore
  public void testModificationEvents() throws InterruptedException {
    browser.navigate("https://www.google.co.uk/");
    final DOMNode item = window().getDocument().getElementsByTagName("body").item(0);
    final DOMMutationObserver observer = item.registerMutationObserver(true, true, true, false, null);
    final DOMElement node = (DOMElement) XPathUtil.getFirstNode("//*[@id='gbqfq']", browser);
    node.type("g");
    Thread.sleep(1000);
    observer.disconnect();
    assertEquals("not empty", 20, observer.takeRecords().size());
    assertEquals("empty", 0, observer.takeRecords().size());
  }

  @Test
  public void testSerialize() throws InterruptedException {
    browser.navigate("http://diadem.cs.ox.ac.uk/test/re/fast/wwagency/result1.html");

    final DOMElement html = window().getDocument().getDocumentElement();

    final StringBuilder b = new StringBuilder();
    visit(html, b);
    // System.out.println(b.toString());
    assertEquals("empty", 12036, b.toString().length());

  }

  @Test
  public void testClickOnOpacityZero() {
    browser.navigate(WebDriverOnXVFBTestCase.class.getResource("click_jacker.html").getFile());
    final FirefoxDriver driver = (FirefoxDriver) browser.getWindowFrame();
    final WebElement element = driver.findElement(By.id("clickJacker"));
    assertEquals("Precondition failed: clickJacker should be transparent", "0", element.getCssValue("opacity"));
    // assertEquals("", true, element.isDisplayed());
    element.click();
    assertEquals("1", element.getCssValue("opacity"));

  }

  private void visit(final DOMElement element, final StringBuilder b) {
    b.append("<" + element.getNodeName() + ">");
    final DOMNodeList childNodes = element.getChildNodes();

    for (final DOMNode n : childNodes) {
      if (n.getNodeType() == Type.TEXT) {
        b.append(n.getTextContent());
      }
      if (n.getNodeType() == Type.ELEMENT) {
        visit((DOMElement) n, b);
      }
    }

    b.append("</" + element.getNodeName() + ">");
  }

  private DOMDocument document() {
    return window().getDocument();
  }

  private DOMWindow window() {
    return browser.getContentDOMWindow();
  }

  private DOMElement body() {
    return (DOMElement) document().getElementsByTagName("body").item(0);
  }

  private DOMNode firstTextNode() {
    return body().getFirstChild();
  }

  private DOMElement secondElement() {
    return (DOMElement) body().getChildNodes().item(1);
  }

  private DOMNode thirdTextNode() {
    return body().getChildNodes().item(2);
  }

  private DOMElement label() {
    return (DOMElement) XPathUtil.getFirstNode("/html/body/section/div/form/div/label", browser);
  }

  private DOMNode labelText() {
    return XPathUtil.getFirstNode("(//label)[1]/text()", browser);
  }

  @SuppressWarnings("unchecked")
  <T> T js(final FirefoxDriver driver, final String script, final Object... values) {
    return (T) ((JavascriptExecutor) driver).executeScript(script, values);
  }

}