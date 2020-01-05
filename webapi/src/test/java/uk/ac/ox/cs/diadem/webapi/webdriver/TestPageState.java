/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.webdriver;

import java.io.IOException;
import java.net.URI;
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
import uk.ac.ox.cs.diadem.webapi.pagestate.SimplePageStateRecorder;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class TestPageState {

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

   @Test
   @Ignore
   public void testOptions() throws Exception {
	   browser = BrowserInitialiseTestHelper.init();
	   browser.navigateAndStatus(new URI("http://www.starbucks.com"));
   
   }

  @Test
  public void test() throws InterruptedException, IOException {
	  browser = BrowserInitialiseTestHelper.init();
    browser.navigate("about:config");
    forceSleep();
  }

  @Test
  @Ignore
  public void testLocation() {
	  browser = BrowserInitialiseTestHelper.init();
    browser.navigate("http://www.outback.com/locations");
    forceSleep();
    final SimplePageStateRecorder state = new SimplePageStateRecorder(browser);
    state.recordPageState();

    final uk.ac.ox.cs.diadem.webapi.dom.DOMElement e = (uk.ac.ox.cs.diadem.webapi.dom.DOMElement) XPathUtil
        .getFirstNode(".//*[@id='category-group-1']/div[1]/a", browser);
    e.click();
    forceSleep();
    state.recordPageState();

    System.out.println("Same location " + state.atSameLocation());
    System.out.println("hasPageChanged" + state.hasPageChanged());
    System.out.println("isPageSimilar" + state.isPageSimilar());
    System.out.println("whyIsNotsimilar" + state.whyIsNotsimilar());

  }

  private void forceSleep() {
    try {
      SLEEPER.poll(5, TimeUnit.SECONDS);
    } catch (final Exception e) {
      // toNothing
    }
  }

}
