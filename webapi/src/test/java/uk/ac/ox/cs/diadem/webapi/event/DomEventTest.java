package uk.ac.ox.cs.diadem.webapi.event;

import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;

@Ignore
public class DomEventTest {
	
	private static final boolean USE_XVFB = true;
	private static final String SCREEN = ":0";

  private static String URL;
  static {
    try {
      URL = DomEventTest.class.getResource("clickOutsideScroll.html").toURI().toString();
    } catch (final URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  private static WebBrowser browser;

  @BeforeClass
  public static void oneTimesetUp() throws Exception {
	  browser = BrowserInitialiseTestHelper.init();
	  DomEventTest.browser.navigate(DomEventTest.URL);
  }

  @Before
  public void resetPage() throws Exception {

    DomEventTest.browser.navigate(DomEventTest.URL);
    // DOMDocument firstDocument =
    // browser.getContentDOMWindow().getDocument();
    // firstDocument.getElementById("Etype").setTextContent(" ");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    DomEventTest.browser.shutdown();
  }

  @Test
  public void testMouseMove() throws Exception {

    final DOMDocument firstDocument = DomEventTest.browser.getContentDOMWindow().getDocument();
    // on the div we perform a mouse over
    final DOMDocument newDoc = firstDocument.getElementById("myDiv").mouseover().getDocument();
    // check
    final String textContent = newDoc.getElementById("Etype").getTextContent();
    Assert.assertEquals("mouseover mouseenter mousemove mousemove mousemove mousemove mousemove mousemove mousemove",
        textContent);
  }

  @Test
  public void testClickAndFocus() throws Exception {

    final DOMDocument firstDocument = DomEventTest.browser.getContentDOMWindow().getDocument();
    // on the div we perform a mouse over
    final DOMDocument newDoc = firstDocument.getElementById("myDiv").click().getDocument();
    // click on another element
    newDoc.getElementById("in").click().getDocument();
    // check
    final String textContent = newDoc.getElementById("Etype").getTextContent().trim();
    Assert
    .assertEquals(
        "mouseover mouseenter mousemove mousedown mouseup click mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mouseout focus",
        textContent);
    // mouseover mousemove mouseover mousemove mousedown mouseup click focus
    // Assert.assertTrue(checkEventEffect(newDoc,
    // "mouseover mousemove mousedown mouseup click mouseout focus"));
  }

  @Test
  public void testClickOnDiv() throws Exception {

    final DOMDocument firstDocument = DomEventTest.browser.getContentDOMWindow().getDocument();
    // on the div we perform a mouse over
    final DOMDocument newDoc = firstDocument.getElementById("myDiv").click().getDocument();
    // check
    final String textContent = newDoc.getElementById("Etype").getTextContent().trim();
    Assert
    .assertEquals(
        "mouseover mouseenter mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousemove mousedown mouseup click",
        textContent);
  }

  @Test
  public void testFocus() throws Exception {

    final DOMDocument firstDocument = DomEventTest.browser.getContentDOMWindow().getDocument();
    // on the div we perform a mouse over
    final DOMDocument newDoc = firstDocument.getElementById("in").focus().getDocument();
    // check
    final String textContent = newDoc.getElementById("Etype").getTextContent().trim();
    Assert.assertEquals("mouseover mouseenter focus", textContent);
  }
}