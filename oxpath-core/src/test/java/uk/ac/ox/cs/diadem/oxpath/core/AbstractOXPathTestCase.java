package uk.ac.ox.cs.diadem.oxpath.core;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import uk.ac.ox.cs.diadem.oxpath.testsupport.StandardTestcase;


public class AbstractOXPathTestCase extends StandardTestcase {

//  protected static WebBrowser browser;

  // private ObjectOutputStream os;
  @AfterClass
  public static void shutdown() {
//	if (browser != null) {
//		browser.close();
//	} 
  }

  @BeforeClass
  public static void init() {
//    browser = BrowserFactory.newWebBrowser(Engine.HTMLUNIT, true);
  }

}
