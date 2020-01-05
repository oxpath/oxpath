/**
 *
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.util.misc.EscapingUtils;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPITimeoutException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus.Cause;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus.Status;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class NavigateAction extends AbstractBrowserAction {
  private static final Logger LOG = LoggerFactory.getLogger(NavigateAction.class);

  protected URL url;

  /**
   * @param isFormAction
   * @param pmActionAsString
   * @param url2
   */
  public NavigateAction(final boolean isFormAction, final String pmActionAsString, final URL url) {
    super(isFormAction, pmActionAsString);
    this.url = url;
  }

  static ExecutionStatus _execute(final WebBrowser browser, final URL url) {
    LOG.info("About to navigate to <{}>", url);
    int httpStatus = 200;
    try {
      final boolean disableHttpStatusCheck = false;
      if (disableHttpStatusCheck) {
        browser.navigate(EscapingUtils.urlToUri(url));
      } else {
        httpStatus = browser.navigateAndStatus(EscapingUtils.urlToUri(url));
      }
    } catch (final WebAPITimeoutException e) {
      LOG.error("Page loading exceeded the timeout, failed navigate action");
      browser.stop();
      return StatusExecution.FACTORY.failedActionTimeout(Status.FAIL, Cause.TIMEOUT);
    } catch (final WebAPIRuntimeException e) {
      LOG.warn("Cannot get http status for page {}. Will consider OK. Cause {}", url, e.getMessage());
    }
    // try {
    // // use another call to get http status (not feasible with WebDriver)
    // httpStatus = WebUtils.getHTTPStatus(EscapingUtils.urlToUri(url));
    // } catch (final DiademRuntimeException e) {
    // // Ignore and set status to ok.
    // }

    // http status 200 and similar
    // boolean success = code == 2;
    ExecutionStatus status;

    if (((httpStatus >= 300) || (httpStatus < 200)) && (httpStatus != 420) && (httpStatus != 429)) {
      // we treat redirect as error
      LOG.error("Failed to navigate to [{}], http status [{}].", url, httpStatus);
      return StatusExecution.FACTORY.failedNavigateAction(Status.FAIL, url.toString(), browser.getLocationURL(),
          httpStatus, Cause.HTTP_ERROR_CODE);
    }

    // here is success
    LOG.info("Successfully navigated to [{}]", url);
    status = StatusExecution.FACTORY.successAction(url.toString());
    return status;
  }

  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {
    return _execute(browser, url);

  }
  // /**
  // * Returns a URL for the target of this action. Removes possible leading and trailing " and uses {@link URL} for
  // * testing if it's a URL. Does not use base URL's at the moment.
  // * @throws MalformedURLException
  // */
  // protected URL getURLForTarget() throws MalformedURLException {
  //
  // if (urlString.startsWith("\"") || urlString.startsWith("'"))
  // return new URL(urlString.substring(1, urlString.length() - 1));
  // return new URL(urlString);
  // }
}
