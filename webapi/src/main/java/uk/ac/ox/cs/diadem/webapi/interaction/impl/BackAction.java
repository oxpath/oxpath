/**
 *
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.util.misc.EscapingUtils;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.utils.WebUtils;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class BackAction extends NavigateAction {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractBrowserAction.class);

  /**
   * @param isFormAction
   * @param pmActionAsString
   * @param url
   */
  public BackAction(final boolean isFormAction, final String pmActionAsString, final URL url) {
    super(isFormAction, pmActionAsString, url);
  }

  // FIXME DUPLICATED CODE
  static ExecutionStatus _executeAction(final WebBrowser browser, final URL url) {
    browser.back(true);
    // TimeUnit.SECONDS.sleep(20);
    if (!dealWithDocumentExpiredIfNecessary(browser)) {
      LOG.error("Error page, failed resume");
      final ExecutionStatus status = StatusExecution.FACTORY.failedBackAction(url.toString());
      return status;
    }

    final String firstBack = browser.getLocationURL();
    if (!url.toString().equals(firstBack)) {
      // try another back() as workaround on some pages in which we fail to force reloading
      browser.back(true);
      if (!EscapingUtils.sameURL(url, browser.getURL())) {
        LOG.error("Back action towards {} landend at {}", url.toExternalForm(), browser.getURL());
        // throw new ProcessingException("Back action landend to an unexpected url", LOG);
        final ExecutionStatus status = StatusExecution.FACTORY.failedBackAction(url.toString());
        return status;
      }

    }
    // successful
    final ExecutionStatus status = StatusExecution.FACTORY.successAction(url.toString());
    return status;

  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.processing.actions.executorr.ExecutableAction#execute
   * (uk.ac.ox.cs.diadem.processing.actions.modificationss.FactSerializer)
   */
  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {
    try {
      browser.back(true);
      // TimeUnit.SECONDS.sleep(20);
      if (!dealWithDocumentExpiredIfNecessary(browser)) {
        LOG.error("Error page, failed resume");
        final ExecutionStatus status = StatusExecution.FACTORY.failedBackAction(url.toString());
        return status;
      }

      final String firstBack = browser.getLocationURL();
      if (!url.equals(new URL(firstBack))) {
        // try another back() as workaround on some pages in which we fail to force reloading
        browser.back(true);
        if (!EscapingUtils.sameURL(url, browser.getURL())) {
          LOG.error("Back action towards {} landend at {}", url.toExternalForm(), browser.getURL());
          // throw new ProcessingException("Back action landend to an unexpected url", LOG);
          final ExecutionStatus status = StatusExecution.FACTORY.failedBackAction(url.toString());
          return status;
        }

      }
      // successful
      final ExecutionStatus status = StatusExecution.FACTORY.successAction(url.toString());
      return status;

    } catch (final MalformedURLException e) {
      throw new WebAPIInteractionException("Back action to a malformed url: " + browser.getLocationURL(), LOG);
    }
    // } catch (final InterruptedException e) {
    // throw new ProcessingException("Interrupted sleep on Back action", e, LOG);
    // }
  }

  static boolean dealWithDocumentExpiredIfNecessary(final WebBrowser browser) {

    if (!WebUtils.isErrorPage(browser.getContentDOMWindow()))
      return true;

    final boolean success = WebUtils.tryDocumentExpiredResubmission(browser);
    if (!success) {
      LOG.error("Failed attempt to resubmit a document expired due to back button in the browser, back action failed");
    } else {
      LOG.info("Successfully resubmitted document expired due to back button in the browser");
    }

    return success;

  }
}
