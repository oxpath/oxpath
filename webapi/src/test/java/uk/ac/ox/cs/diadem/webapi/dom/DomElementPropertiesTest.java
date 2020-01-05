package uk.ac.ox.cs.diadem.webapi.dom;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;

public class DomElementPropertiesTest {
  static final Logger logger = LoggerFactory.getLogger(DomElementPropertiesTest.class);
  /*PropertyConfigurator.configure("TestLog4j.properties");*/
  
  private WebBrowser browser;

  @Before
  public void setUp() throws Exception {
	  browser = BrowserInitialiseTestHelper.init();
	  final String url = DomElementPropertiesTest.class.getResource("simple.html").toURI().toString();
	  browser.navigate(url);
  }

  @Test
  public void testGetInner() throws Exception {
    final DOMWindow contentDOMWindow = browser.getContentDOMWindow();
    final DOMDocument document = contentDOMWindow.getDocument();
    final DOMElement elementById = document.getElementById("homepageprimarycontent");
    final String innerHTML = elementById.getInnerHTML();
    assertEquals("giovanni", innerHTML);
    browser.shutdown();
  }

}
