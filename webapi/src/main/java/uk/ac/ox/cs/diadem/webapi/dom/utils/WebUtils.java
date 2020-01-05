/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.utils;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder.Engine;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class WebUtils {
//  private static XMLConfiguration configuration = ConfigurationObject.getConfiguration("uk/ac/ox/cs/diadem/webapi/Configuration.xml");
  private static final Logger LOGGER = LoggerFactory.getLogger(WebUtils.class);
  private static Set<String> FORM_TAGS = ImmutableSet.of("form", "input", "textarea", "label", "fieldset", "legend",
      "select", "optgroup", "option", "button", "datalist", "keygen", "output");

  private WebUtils() {
  }

  public static boolean isValidFormTag(final String tag) {
    return FORM_TAGS.contains(tag.toLowerCase());
  }

//  /**
//   * Uses apache http client
//   *
//   * @param resourceUrl
//   * @param timeOut
//   * @return
//   */
//  @SuppressWarnings("deprecation")
//  public static int getHTTPStatus(final URI resourceUrl, final int timeOut) {
//	
//    // httpclient does not support file schema
//    if (resourceUrl.getScheme().equals("file"))
//      return HttpStatus.SC_OK;
//
//    final DefaultHttpClient httpclient = new DefaultHttpClient();
//
//    final HttpParams httpParameters = httpclient.getParams();
//    httpParameters.setParameter(CoreProtocolPNames.USER_AGENT,
//    		configuration.getString("//platform[contains(name, 'Linux')]/webapi/useragent"));
//
//    // Set the timeout in milliseconds until a connection is established.
//    // The default value is zero, that means the timeout is not used.
//
//    int timeoutSec = configuration.getInt("webdriver/options/timeouts/page-load-sec");
//    if (timeOut != -1) {
//      timeoutSec = timeOut;
//    }
//    final int timeoutConnection = 1000 * timeoutSec;
//    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
//
//    // Set the default socket timeout (SO_TIMEOUT)
//    // in milliseconds which is the timeout for waiting for data.
//    final int timeoutSocket = timeoutConnection;
//    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
//
//    // no retry
//    httpclient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
//
//    final boolean[] redirect = new boolean[] { false };
//
//    httpclient.setRedirectStrategy(new DefaultRedirectStrategy() {
//      @Override
//      public boolean isRedirected(final HttpRequest request, final HttpResponse response, final HttpContext context) {
//        boolean isRedirect = false;
//        try {
//          isRedirect = super.isRedirected(request, response, context);
//          redirect[0] = isRedirect;
//        } catch (final ProtocolException e) {
//          //
//        }
//        return isRedirect;
//      }
//    });
//
//    try {
//      final HttpGet get = new HttpGet(resourceUrl);
//      get.getRequestLine();
//      final HttpResponse response = httpclient.execute(get);
//      return response.getStatusLine().getStatusCode();
//    } catch (final ClientProtocolException e) {
//      LOGGER.error("error getting http status for {} <{}>", resourceUrl, e.getMessage());
//      //
//    } catch (final IOException e) {
//      LOGGER.error("error getting http status for {} <{}>", resourceUrl, e);
//    } finally {
//      httpclient.close();
//    }
//
//    // if we get here there was an exception, return failure
//    return HttpStatus.SC_METHOD_FAILURE;
//  }

  /**
   * This works based on Mozilla Firefox error messages
   *
   * @param window
   * @return
   */
  public static boolean isErrorPage(final DOMWindow window) {
    return StringUtils.containsIgnoreCase(window.getTitle(), "Problem loading page");

  }

  /**
   *
   * @param browser
   * @return
   */
  public static FirefoxDriver castToDriver(final WebBrowser browser) {
    if (browser.getEngine() == Engine.WEBDRIVER_FF)
      return (FirefoxDriver) browser.getWindowFrame();
    return null;
  }

  /**
   * Deal with document expired when going back in the browser https://support.mozilla.org/en-US/questions/922734
   *
   * @param browser
   * @return true if successfully submitted, false otherwise
   */
  public static boolean tryDocumentExpiredResubmission(final WebBrowser browser) {

    try {

      browser.refresh();
      LOGGER.trace("Checking for Alert popup ");
      final FirefoxDriver driver = castToDriver(browser);

      if (driver == null)
        return false;
      final Alert alert = driver.switchTo().alert();
      alert.accept();
      return true;

    } catch (final NullPointerException e) {
      // TODO ???
      // no id element means no error
      return true;

    } catch (final NoAlertPresentException e1) {
      // check again
      if (!isErrorPage(browser.getContentDOMWindow()))
        return true;
      LOGGER.error("Expected browser dialog for document experied, but not found. Failed resubmission ");
      return false;
    }

  }
}
