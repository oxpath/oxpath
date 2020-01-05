/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.webdriver;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import junit.framework.Assert;
import uk.ac.ox.cs.diadem.webapi.BrowserInitialiseTestHelper;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
@Ignore
public class TestAlertUnhandled {

  static WebBrowser browser;

  @BeforeClass
  public static void oneTimesetUp() throws Exception {
	  browser = BrowserInitialiseTestHelper.init();
  }

  @AfterClass
  public static void shutdown() {
    browser.shutdown();
  }

  @Test
  @Ignore
  public void test() throws InterruptedException, IOException {
    browser.navigate("http://web.archive.org/web/20090827015310/http://www.bhphotovideo.com:80/");
  }

}
