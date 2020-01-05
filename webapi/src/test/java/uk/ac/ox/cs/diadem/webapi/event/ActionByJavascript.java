package uk.ac.ox.cs.diadem.webapi.event;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;

public class ActionByJavascript {
	
  private static String URL;
  private static WebBrowser browser;

  @Before
  public void oneTimesetUp() throws Exception {
	URL = ActionByJavascript.class.getResource("basicForm.html").toURI().toString();
	browser = BrowserInitialiseTestHelper.init();
    browser.manageOptions().setFallBackToJSExecutionOnNotInteractableElements(true);
    ActionByJavascript.browser.navigate(ActionByJavascript.URL);
  }

  @After
  public void tearDown() throws Exception {
	  if (browser != null)
		  browser.shutdown();
  }
  
  private void testFormInteraction(boolean visibility, String textTest, int selectId, int radioId, String textVal) throws Exception {
		final DOMDocument doc = ActionByJavascript.browser.getContentDOMWindow().getDocument();
		DOMNode styleEl = doc.getElementsByTagName("style").item(0);
		styleEl.setTextContent("form { visibility: " + (visibility?"visible":"hidden")+";}");
		
		doc.getElementById("text").type(textVal);
		doc.getElementById("select").htmlUtil().asHTMLSelect().selectOptionIndex(selectId);
		doc.getElementById("radio"+radioId).click();
		doc.getElementById("button").click();
		assertEquals(doc.getElementById("out").getTextContent(), textTest);
  }

  @Test
  public void testFormInteractionVisible() throws Exception {
	testFormInteraction(true, "text value-true-true", 2, 2, "text value");
  }
  
  @Test
  public void testFormInteractionHidden() throws Exception {
	  testFormInteraction(false, "text value-true-true", 2, 2, "text value");
  }

}