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
import org.junit.Test;

import com.google.common.io.Resources;

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
public class TestStyledOverlay {
	
	private static final boolean USE_XVFB = false;
	  private static final String SCREEN = ":0";

  static WebBrowser browser;

  @BeforeClass
  public static void oneTimesetUp() throws Exception {
	  WebBrowserBuilder builder = new WebBrowserBuilder();
	  if (USE_XVFB)
		  builder.getRunConfiguration().setXvfbMode(USE_XVFB).setDisplayNumber(SCREEN);
	  browser = builder.build();
	  browser.navigate("http://diadem.cs.ox.ac.uk/test/unit/borderline/spacing-issues.html");
  }

  @AfterClass
  public static void shutdown() {
    browser.shutdown();
  }

  @Before
  public void resetPage() throws Exception {

    browser.navigate("http://diadem.cs.ox.ac.uk/test/unit/borderline/spacing-issues.html");
  }

  // @AfterClass
  // public static void shutdown() {
  // browser.shutdown();
  // }

  @Test
  public void testTwoRangesOnNode() throws InterruptedException, IOException {

    final String cssFile = Resources.toString(Resources.getResource(TestStyledOverlay.class, "test.css"),
        Charset.defaultCharset());

    final DOMWindow contentDOMWindow = browser.getContentDOMWindow();

    final StyledOverlayBuilder b = contentDOMWindow.getOverlayBuilder();
    final StyledNode root = b.createNode("/html/body/div", "record");

    final String p1 = "id('p3')/text()[1]";
    final String p2 = "id('p3')/text()[2]";
    final String p3 = "id('s1')/text()";
    final StyledRangeNode a1 = b.createRangeNode(p1, p1, Pair.of(0, 1), "attribute");
    b.addNode(root, a1);
    b.addInfobox(a1, Pair.of("p1", "0,1"), "info");
    final StyledRangeNode a2 = b.createRangeNode(p2, p3, Pair.of(1, 1), "attribute");
    b.addNode(root, a2);
    b.addInfobox(a2, Pair.of("p3", "5,8"), "info");

    // b.addInfobox(a1, Pair.of("bath", "13"), "info");
    // final StyledRangeNode a2 = b.createRangeNode(locatorForTextNode, Pair.of(0, 1), "attr2");
    // b.addNode(rec, a2);
    final StyledOverlay tree = b.build(cssFile);

    tree.attach();
    // final CSSStyleSheet ss = tree.getCSSStyleSheet();
    // final CSSStyleRule r = ss.findRulesBySelectorText(".attribute").get(0);
  }

}
