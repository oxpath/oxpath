package uk.ac.ox.cs.diadem.webapi.webdriver;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder;

@Ignore
public class TestAutoDownload {

  static WebBrowser browser;
  static FirefoxDriver driver;

  @AfterClass
  public static void shutdown() {
	if (browser != null) {
		browser.shutdown();
	}
  }

  @BeforeClass
  public static void init() {
	browser = BrowserInitialiseTestHelper.init();
    driver = (FirefoxDriver) browser.getWindowFrame();

  }

  @Before
  public void loadPage() {
    //browser.navigate("http://support.apple.com/manuals/");
    browser.navigate("http://www.ibanez.com/eu/download/manual/index.html");
  }

  @Test
  public void testOnPDF() {
    final WebElement object = driver.findElementByXPath("descendant::a[substring(@href, string-length(@href)"
    		+ " - string-length('.pdf') +1) = '.pdf'][1]");
    //object.click();
    // or set timeout?
  }
}