/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.visualization;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.Resources;

import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder;
import uk.ac.ox.cs.diadem.webapi.css.StyledNode;
import uk.ac.ox.cs.diadem.webapi.css.StyledOverlay;
import uk.ac.ox.cs.diadem.webapi.css.StyledOverlayBuilder;
import uk.ac.ox.cs.diadem.webapi.css.StyledRangeNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class StyledOverlayTestcase {

  static WebBrowser browser;

  @BeforeClass
  public static void oneTimesetUp() throws Exception {
	  browser = BrowserInitialiseTestHelper.init();
//	  browser.navigate("http://diadem.cs.ox.ac.uk/test/re/fast/wwagency/result1.html");
  }

  @AfterClass
  public static void shutdown() {
    browser.shutdown();
  }

  @Before
  public void resetPage() throws Exception {
    browser.navigate("http://diadem.cs.ox.ac.uk/test/re/fast/wwagency/result1.html");
  }

  @Test
  public void testScratch() throws InterruptedException, IOException {
    final String cssFile = Resources.toString(Resources.getResource(StyledOverlayTestcase.class, "test.css"),
        Charset.defaultCharset());

    final DOMWindow contentDOMWindow = browser.getContentDOMWindow();

    final StyledOverlayBuilder b = contentDOMWindow.getOverlayBuilder();

    final StyledNode dataArea = b.createNode("html/body[1]/div[@class='subpage-content clearfix'][1]", "dataArea");
    b.addRootNode(dataArea);
    final StyledNode rec = b.createNode(
        "html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]",
        "record");
    b.addNode(dataArea, rec);

    final String locatorForTextNode = "html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_info'][1]/ul[@class='prop_keypoints'][1]/li[2]/strong[1]/text()[1]";
    final StyledRangeNode a1 = b.createRangeNode(locatorForTextNode, Pair.of(1, 2), "attribute");
    b.addNode(rec, a1);

    // b.addInfobox(a1, Pair.of("bath", "13"), "info");

    final StyledRangeNode a2 = b.createRangeNode(locatorForTextNode, Pair.of(0, 1), "attribute");
    b.addNode(rec, a2);
    final StyledOverlay tree = b.build(cssFile);

    tree.attach();
    // final CSSStyleSheet ss = tree.getCSSStyleSheet();
    // final CSSStyleRule r = ss.findRulesBySelectorText(".attribute").get(0);
  }

  @Test
  public void testTwoRangesOnNode() throws InterruptedException, IOException {

    browser.navigate(StyledOverlayTestcase.class.getResource("debugVisualization.html").getFile());
    final String cssFile = Resources.toString(Resources.getResource(StyledOverlayTestcase.class, "test.css"),
        Charset.defaultCharset());

    final DOMWindow contentDOMWindow = browser.getContentDOMWindow();

    final StyledOverlayBuilder b = contentDOMWindow.getOverlayBuilder();
    final StyledNode root = b.createNode("/html/body/div", "record");

    final String p1 = "/html/body/div/p[1]/child::text()";
    final String p2 = "/html/body/div/p[2]/child::text()";
    final StyledRangeNode a1 = b.createRangeNode(p1, p2, Pair.of(0, 1), "attribute");
    b.addNode(root, a1);
    b.addInfobox(a1, Pair.of("x1", "1"), "info");
    final StyledRangeNode a2 = b.createRangeNode(p2, p2, Pair.of(5, 16), "attribute");
    b.addNode(root, a2);
    b.addInfobox(a2, Pair.of("x2", "2"), "info");

    // b.addInfobox(a1, Pair.of("bath", "13"), "info");
    // final StyledRangeNode a2 = b.createRangeNode(locatorForTextNode, Pair.of(0, 1), "attr2");
    // b.addNode(rec, a2);
    final StyledOverlay tree = b.build(cssFile);

    tree.attach();
    // final CSSStyleSheet ss = tree.getCSSStyleSheet();
    // final CSSStyleRule r = ss.findRulesBySelectorText(".attribute").get(0);
  }

  @Test
  public void testHTMLLegenda() throws InterruptedException, IOException {

    browser.navigate(StyledOverlayTestcase.class.getResource("debugVisualization.html").getFile());
    final String cssFile = Resources.toString(Resources.getResource(StyledOverlayTestcase.class, "test.css"),
        Charset.defaultCharset());

    final DOMWindow contentDOMWindow = browser.getContentDOMWindow();

    final StyledOverlayBuilder b = contentDOMWindow.getOverlayBuilder();
    b.addFixedHTMLBox(
        "<div><ul class='info'><li class='attribute'>this is one item</li><li>another item</li></ul></div>",
        b.createRect(100, 100, 650, 500), "record", "attribute");

    // b.addInfobox(a1, Pair.of("bath", "13"), "info");
    // final StyledRangeNode a2 = b.createRangeNode(locatorForTextNode, Pair.of(0, 1), "attr2");
    // b.addNode(rec, a2);
    final StyledOverlay tree = b.build(cssFile);

    tree.attach();
    // final CSSStyleSheet ss = tree.getCSSStyleSheet();
    // final CSSStyleRule r = ss.findRulesBySelectorText(".attribute").get(0);
  }

  @Test
  public void testMultipleOverlayTreesSingleBuilderSingleHookMultipleRoots() throws InterruptedException, IOException {

    browser.navigate(StyledOverlayTestcase.class.getResource("twoOverlayTrees.html").getFile());
    final String cssFile = Resources.toString(Resources.getResource(StyledOverlayTestcase.class, "testpopup.css"),
        Charset.defaultCharset());

    final DOMWindow contentDOMWindow = browser.getContentDOMWindow();

    // Get a styled overlay builder
    final StyledOverlayBuilder b1 = contentDOMWindow.getOverlayBuilder();

    // Get a root
    final StyledNode r1 = b1.createNode("/html/body");

    // Get a node
    final String p1 = "/html/body/p[1]/child::text()";

    final StyledNode a1 = b1.createNode(p1, "annotation-span", "around", "person");
    b1.addNode(r1, a1);
    b1.addInfobox(a1, Pair.of("x1", "1"), "infobox");

    final StyledOverlay t1 = b1.build(cssFile);
    t1.attach();
    t1.getCSSStyleSheet().appendRule("span.annotation-span + div {display: none;}");
    t1.getCSSStyleSheet()
    .appendRule(
        "span.person { border: 2px; border-style: solid; -moz-border-radius: .5em;  border-radius: .5em; border-color: blue; }");

    final StyledOverlayBuilder b2 = contentDOMWindow.getOverlayBuilder();
    final StyledNode r2 = b2.createNode("/html/body");

    final String p2 = "/html/body/p[2]/child::text()";

    final StyledNode a2 = b2.createNode(p2, "annotation-span", "behind", "animate");
    b2.addNode(r2, a2);
    b2.addInfobox(a2, Pair.of("x2", "2"), "infobox");

    final StyledOverlay t2 = b2.build(cssFile);
    t2.attach();
    t2.getCSSStyleSheet().appendRule("span.annotation-span + div {display: none;}");

    final StyledOverlayBuilder b3 = contentDOMWindow.getOverlayBuilder();
    final StyledNode r3 = b3.createNode("/html/body");

    final String p3 = "/html/body/p[3]/child::text()";

    final StyledNode a3 = b3.createNode(p3, "annotation-span");
    b3.addNode(r3, a3);
    b3.addInfobox(a3, Pair.of("x3", "3"), "infobox");

    final StyledOverlay t3 = b3.build(cssFile);
    t3.attach();
    t3.getCSSStyleSheet().appendRule("span.annotation-span + div {display: none;}");
  }

  @Test
  public void testMultipleOverlayMultipleBuildersSingleHookMultipleRootNodes() throws InterruptedException, IOException {

    browser.navigate(StyledOverlayTestcase.class.getResource("twoOverlayTrees.html").getFile());
    final String cssFile = Resources.toString(Resources.getResource(StyledOverlayTestcase.class, "testpopup.css"),
        Charset.defaultCharset());

    final DOMWindow contentDOMWindow = browser.getContentDOMWindow();

    // Get a styled overlay builder
    final StyledOverlayBuilder b1 = contentDOMWindow.getOverlayBuilder();

    // Get a node
    final String p1 = "/html/body/p[1]/child::text()";

    final StyledNode a1 = b1.createNode(p1, "annotation-span", "around", "person");
    b1.addRootNode(a1);
    b1.addInfobox(a1, Pair.of("x1", "1"), "infobox");

    final StyledOverlay t1 = b1.build(cssFile);
    t1.attach();
    t1.getCSSStyleSheet().appendRule("span.annotation-span + div {display: none;}");
    t1.getCSSStyleSheet()
    .appendRule(
        "span.person { border: 2px; border-style: solid; -moz-border-radius: .5em;  border-radius: .5em; border-color: blue; }");

    final String p2 = "/html/body/p[2]/child::text()";

    final StyledOverlayBuilder b2 = contentDOMWindow.getOverlayBuilder();
    final StyledNode a2 = b2.createNode(p2, "annotation-span", "behind", "animate");
    b2.addRootNode(a2);
    b2.addInfobox(a2, Pair.of("x2", "2"), "infobox");

    final StyledOverlay t2 = b2.build(cssFile);
    t2.attach();
    t2.getCSSStyleSheet().appendRule("span.annotation-span + div {display: none;}");

  }

  @Test
  public void testMultipleOverlayTreesMultipleBuildersSingleRoot() throws InterruptedException, IOException {

    browser.navigate(StyledOverlayTestcase.class.getResource("twoOverlayTrees.html").getFile());
    final String cssFile = Resources.toString(Resources.getResource(StyledOverlayTestcase.class, "testpopup.css"),
        Charset.defaultCharset());

    final DOMWindow contentDOMWindow = browser.getContentDOMWindow();

    // Get a styled overlay builder
    final StyledOverlayBuilder b1 = contentDOMWindow.getOverlayBuilder();

    // Get a root
    final StyledNode r1 = b1.createNode("/html/body");

    // Get a node
    final String p1 = "/html/body/p[1]/child::text()";

    final StyledNode a1 = b1.createNode(p1, "annotation-span", "around", "person");
    b1.addNode(r1, a1);
    b1.addInfobox(a1, Pair.of("x1", "1"), "infobox");

    final StyledOverlay t1 = b1.build(cssFile);
    t1.attach();
    t1.getCSSStyleSheet().appendRule("span.annotation-span + div {display: none;}");
    t1.getCSSStyleSheet()
    .appendRule(
        "span.person { border: 2px; border-style: solid; -moz-border-radius: .5em;  border-radius: .5em; border-color: blue; }");

    final StyledOverlayBuilder b2 = contentDOMWindow.getOverlayBuilder();
    final StyledNode r2 = b2.createNode("/html/body");

    final String p2 = "/html/body/p[2]/child::text()";

    final StyledNode a2 = b2.createNode(p2, "annotation-span", "behind");
    b2.addNode(r2, a2);
    b2.addInfobox(a2, Pair.of("x2", "2"), "infobox");

    final StyledOverlay t2 = b2.build(cssFile);
    t2.attach();
    t2.getCSSStyleSheet().appendRule("span.annotation-span + div {display: none;}");
  }

  @Test
  public void testMultipleOverlayTreesMultipleBuildersMultipleRoots() throws InterruptedException, IOException {

    browser.navigate(StyledOverlayTestcase.class.getResource("twoOverlayTrees.html").getFile());
    final String cssFile = Resources.toString(Resources.getResource(StyledOverlayTestcase.class, "testpopup.css"),
        Charset.defaultCharset());

    final DOMWindow contentDOMWindow = browser.getContentDOMWindow();

    // Get a styled overlay builder
    final StyledOverlayBuilder b1 = contentDOMWindow.getOverlayBuilder();

    // Get a node
    final String p1 = "/html/body/p[1]/child::text()";

    final StyledNode a1 = b1.createNode(p1, "annotation-span", "behind");
    b1.addRootNode(a1);
    b1.addInfobox(a1, Pair.of("x1", "1"), "infobox");

    final StyledOverlay t1 = b1.build(cssFile);

    t1.attach();
    t1.getCSSStyleSheet().appendRule("span.annotation-span + div {display: none;}");

    // Get another node
    final StyledOverlayBuilder b2 = contentDOMWindow.getOverlayBuilder();

    final String p2 = "/html/body/p[2]/child::text()";

    final StyledNode a2 = b2.createNode(p2, "annotation-span", "around");
    b2.addRootNode(a2);
    b2.addInfobox(a2, Pair.of("x2", "2"), "infobox");

    final StyledOverlay t2 = b2.build(cssFile);
    t2.attach();
    t2.getCSSStyleSheet().appendRule("span.annotation-span + div {display: none;}");
  }

  @Test
  public void testSingleOverlayTreeMultipleInfoboxEntries() throws InterruptedException, IOException {

    browser.navigate(StyledOverlayTestcase.class.getResource("twoOverlayTrees.html").getFile());
    final String cssFile = Resources.toString(Resources.getResource(StyledOverlayTestcase.class, "testpopup.css"),
        Charset.defaultCharset());

    final DOMWindow contentDOMWindow = browser.getContentDOMWindow();

    // Get a styled overlay builder
    final StyledOverlayBuilder b = contentDOMWindow.getOverlayBuilder();

    // Get a root
    final StyledNode r = b.createNode("/html/body");

    // Get a node
    final String p1 = "/html/body/p[1]/child::text()";

    final StyledNode a1 = b.createNode(p1, "annotation-span", "around");
    b.addNode(r, a1);
    b.addInfobox(a1, Pair.of("x1", "1"), "infobox");

    final String p2 = "/html/body/p[2]/child::text()";

    final StyledNode a2 = b.createNode(p2, "annotation-span", "behind", "animate");
    b.addNode(r, a2);
    b.addInfobox(a2, Pair.of("x2", "1"), "infobox");
    b.addInfobox(a2, Pair.of("x2", "2"), "infobox");

    final String p3 = "/html/body/p[3]/child::text()";

    final StyledNode a3 = b.createNode(p3, "annotation-span");
    b.addNode(r, a3);
    b.addInfobox(a3, Pair.of("x3", "1"), "infobox");
    b.addInfobox(a3, Pair.of("x3", "2"), "infobox");
    b.addInfobox(a3, Pair.of("x3", "3"), "infobox");

    final StyledOverlay t = b.build(cssFile);
    t.attach();
    t.getCSSStyleSheet().appendRule("span.annotation-span + div {display: none;}");

  }

}
