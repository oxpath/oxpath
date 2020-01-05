/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.webdriver;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
@Ignore
public class TestBrowserOptions {
	
  static WebBrowser browser;
  private static final BlockingQueue<Object> SLEEPER = new ArrayBlockingQueue<Object>(1);

  @BeforeClass
  public static void oneTimesetUp() throws Exception {

  }

  @AfterClass
  public static void shutdown() {
    if (browser != null) {
      browser.shutdown();
    }
  }

  // @Test
  // public void testOptions() throws Exception {
  // browser = BrowserFactory.newBuilder(Engine.WEBDRIVER_FF).useXvfb(false).pluginsEnabled(false).newWebBrowser();
  // browser.manageOptions().configureXPathLocatorHeuristics(true, false);
  // browser.navigate("http://www.chuckecheese.com/experience/locations");
  // }

  // @AfterClass
  // public static void shutdown() {
  // browser.shutdown();
  // }

  @Test
  public void test() throws InterruptedException, IOException {
	  browser = BrowserInitialiseTestHelper.init();
    browser.navigate("about:config");
    forceSleep();
  }

  @Test
  public void testLocation() {
	  browser = BrowserInitialiseTestHelper.init();
    browser.navigate("http://ctrlq.org/maps/where/");
    forceSleep();
  }

  private void forceSleep() {
    try {
      SLEEPER.poll(5, TimeUnit.SECONDS);
    } catch (final Exception e) {
      // toNothing
    }
  }

}
