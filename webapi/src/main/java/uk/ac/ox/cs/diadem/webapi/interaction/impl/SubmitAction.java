/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import org.openqa.selenium.ElementNotVisibleException;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class SubmitAction extends AbstractBrowserAction {

  private final DOMElement target;

  SubmitAction(final DOMElement target, final boolean isFormAction, final String pmActionString) {
    super(isFormAction, pmActionString);
    this.target = target;
  }

  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {
    if (target.isStale())
      return StatusExecution.FACTORY.failedActionOnStaleElement("action id:" + actiondescription);

    if (!checkEnabled(target)) {

      LOG.info("Cannot execute submit on <'{}'>. It is not enabled", target);
      final ExecutionStatus status = StatusExecution.FACTORY.invisibleTarget();
      return status;
    }

    LOG.info("Clicking on element <'{}'>", target);

    try {
      target.click(true);
    } catch (final ElementNotVisibleException e) {
      LOG.info("Cannot execute submit on <'{}'>. It is not visible", target);
      return StatusExecution.FACTORY.invisibleTarget();
    }

    // TODO we might try form.submit
    final ExecutionStatus successAction = StatusExecution.FACTORY.successAction("submitAction");
    return successAction;
  }

}
