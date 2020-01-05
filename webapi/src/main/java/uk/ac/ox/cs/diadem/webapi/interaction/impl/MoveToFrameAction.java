/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import java.net.MalformedURLException;
import java.net.URL;

import uk.ac.ox.cs.diadem.util.misc.EscapingUtils;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class MoveToFrameAction extends AbstractBrowserAction {

  private final String xPathLocator;
  private final String targetHref;

  /**
   * @param isFormAction
   * @param pmActionString
   * @param target
   * @param xpathLocator2
   * @param iframeUrl
   */
  public MoveToFrameAction(final boolean isFormAction, final String pmActionString, final String xpathLocator,
      final String iframeUrl) {
    super(isFormAction, pmActionString);
    xPathLocator = xpathLocator;
    targetHref = iframeUrl;

  }

  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {
    if (targetHref == null) {
      LOG.info("Cannot execute move to frame on <'{}'> <'{}'>. Src url is not set.", targetHref, xPathLocator);
      final ExecutionStatus status = StatusExecution.FACTORY.failedMoveToFrameAction();
      return status;
    }

    try {
      final URL u = EscapingUtils.stringToUrl(targetHref);
      return NavigateAction._execute(browser, u);
    } catch (final WebAPIRuntimeException | MalformedURLException e) {
      LOG.info("Cannot execute move to frame on <'{}'> <'{}'>. Src url is not valid.", targetHref, xPathLocator);
      final ExecutionStatus status = StatusExecution.FACTORY.failedMoveToFrameAction();
      return status;
    }
  }

  @Override
  public String toString() {

    return super.toString() + "\n" + xPathLocator;
  }

}
