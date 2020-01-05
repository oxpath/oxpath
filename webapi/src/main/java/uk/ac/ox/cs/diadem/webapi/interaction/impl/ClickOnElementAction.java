/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.ElementNotVisibleException;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLInputElement;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class ClickOnElementAction extends AbstractBrowserAction {

  private final DOMElement target;
  private final String xPathLocator;
  private boolean isRadio = false;

  /**
   * @param target2
   * @param xPathLocator2
   */
  public ClickOnElementAction(final DOMElement target, final String xPathLocator, final boolean isFormAction,
      final String pmActionString) {
    super(isFormAction, pmActionString);
    this.target = target;
    this.xPathLocator = xPathLocator;
  }

  /**
   * @param target2
   * @param xPathLocator2
   */
  public ClickOnElementAction(final DOMElement target, final String xPathLocator, final boolean isFormAction,
      final String pmActionString, final boolean isRadioButton) {
    this(target, xPathLocator, isFormAction, pmActionString);
    isRadio = isRadioButton;

  }

  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {

    if (target.isStale())
      return StatusExecution.FACTORY.failedActionOnStaleElement("action id:" + getPMActionString());

    if (!checkEnabled(target)) {
      LOG.info("Cannot execute click on <'{}'> <'{}'>. It is not enabled on the page", target, xPathLocator);
      return StatusExecution.FACTORY.disabledElement();
    }

    try {

      if (isRadio)
        return manageRadioBox();
      else {
        LOG.info("Clicking on element <'{}'>", target);
        target.click(true);
      }
    } catch (final ElementNotVisibleException e) {
      LOG.info("Cannot select options in <'{}'>. It is not visible", target);
      return StatusExecution.FACTORY.invisibleTarget();

    } catch (final RuntimeException e) {
      // cathc only MoveTargetOutOfBoundsException http://code.google.com/p/selenium/issues/detail?id=3075
      if (StringUtils.containsIgnoreCase(ExceptionUtils.getStackTrace(e), "MoveTargetOutOfBoundsException")) {
        LOG.debug("Try to recover from {} with a click in js", "MoveTargetOutOfBoundsException");
        try {
          target.js().click();
        } catch (final RuntimeException e2) {
          LOG.error("Failed click in js on {}", target);
          return StatusExecution.FACTORY.failedClickAction("MoveTargetOutOfBoundsException");
        }
      } else
        throw e;
    }
    return StatusExecution.FACTORY.successAction(actionType());

  }

  private ExecutionStatus manageRadioBox() {
    // for radio we try the JS first, and then click().
    // It also tries to prevent the bug in WebDriver http://code.google.com/p/selenium/issues/detail?id=3075

    LOG.info("Try to select the radio button via javascript <{}>", target);
    final DOMHTMLInputElement radio = target.htmlUtil().asHTMLInputElement();
    if (!radio.getChecked()) {
      LOG.info("Clicking on radio box <'{}'>", target);
      target.click(true);
      // if not success, then try to set via Javascript
      if (!radio.getChecked()) {
        // TODO simulate the JS event sequence
        radio.setChecked(true);
      }
      // fail as no way to check it
      if (!radio.getChecked())
        return StatusExecution.FACTORY.failedClickAction();
    }
    return StatusExecution.FACTORY.successAction(actionType());
  }

  /**
   * @return
   */
  private String actionType() {

    return "clickOnElement";
  }

  @Override
  public String toString() {

    return super.toString() + "\n" + xPathLocator;
  }

}
