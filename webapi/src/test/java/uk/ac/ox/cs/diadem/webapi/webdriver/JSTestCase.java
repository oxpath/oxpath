package uk.ac.ox.cs.diadem.webapi.webdriver;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.common.io.Resources;

import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;

public class JSTestCase {

	static WebBrowser browser;
  static FirefoxDriver driver;

  @AfterClass
  public static void shutdown() {
    browser.shutdown();
  }

  @BeforeClass
  public static void init() {
	browser = BrowserInitialiseTestHelper.init();
    driver = (FirefoxDriver) browser.getWindowFrame();

  }

  @Before
  public void loadPage() {
    browser.navigate("http://diadem.cs.ox.ac.uk/test/re/fast/wwagency/");
  }

  @Test
  public void testCreateElementAndSetGetAttribute() {
    final WebElement object = driver.findElementByTagName("header");
    assertEquals(object.getTagName(), "header");

    final WebElement object2 = js(driver, "return document.getElementsByTagName('header')[0]");
    assertEquals(object, object2);
  }

  @Test
  @Ignore
  public void testXPCOM() throws IOException {
    final String functions = Resources.toString(Resources.getResource(getClass(), "xpcom_test.js"),
        Charset.defaultCharset());
    final String call = "return tester();";

    // final WebElement object = driver.findElementByTagName("object");
    // System.out.println(object.getTagName());
    driver.executeScript(functions + "\n" + call);
    try {
      System.in.read();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    // final WebElement = js(driver, "return document.getElementsByTagName('object')[0]");

  }

  @SuppressWarnings("unchecked")
  static <T> T js(final FirefoxDriver driver, final String script, final Object... values) {
    return (T) ((JavascriptExecutor) driver).executeScript(script, values);
  }

  // public static void main(final String[] args) throws InterruptedException {
  // browser = BrowserFactory.newWebBrowser(Engine.WEBDRIVER_FF);
  // browser.navigate("http://diadem.cs.ox.ac.uk/test/re/fast/wwagency/", true);
  // TimeUnit.SECONDS.sleep(2);
  // js((FirefoxDriver) browser.getWindowFrame(), "function f(){while(true){}}; return f();");
  //
  // }
}