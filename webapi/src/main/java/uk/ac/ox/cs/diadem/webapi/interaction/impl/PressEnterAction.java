/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.StaleElementReferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMTypeableElement;
import uk.ac.ox.cs.diadem.webapi.dom.HTMLUtil;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class PressEnterAction extends AbstractBrowserAction {

  private final DOMElement target;
  // private final String value;
  protected static final Logger LOG = LoggerFactory.getLogger(PressEnterAction.class);

  /**
   * @param target2
   * @param isFormAction
   * @param string
   */
  public PressEnterAction(final DOMElement target, final boolean isFormAction, final String pmActionString) {
    super(isFormAction, pmActionString);
    this.target = target;
  }

  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {

    return do_execute(target);
  }

  private ExecutionStatus do_execute(final DOMElement targetElement) throws WebAPIInteractionException {
    if (targetElement.isStale())
      return StatusExecution.FACTORY.failedActionOnStaleElement("action id:" + actiondescription);
    if (!checkEnabled(targetElement)) {
      LOG.info("Cannot pressEnter on <'{}'>. It is not enabled", targetElement);
      final ExecutionStatus status = StatusExecution.FACTORY.disabledElement();
      return status;
    }

    final DOMTypeableElement typable = getTypebleIfAny(targetElement);

    // no input or textarea element
    if (typable == null) {
      final String nodeName = targetElement.getNodeName();
      LOG.error("Cannot perform <pressEnter> action into a non input/textarea element: {}", nodeName);
      throw new WebAPIInteractionException(
          "Cannot perform typeIn action into a non input/textarea element:" + nodeName, LOG);

    }

    // do the action
    LOG.trace("Pressing Enter into <{}>.", typable);

    try {
      // typable.select();
      typable.typeAndEnter("");

      return StatusExecution.FACTORY.successAction("VK_RETURN");

    } catch (final StaleElementReferenceException e) {
      LOG.trace("DomElement {} is not attached to the page any longer. Likely the page has changed, cannot determine if action is performed ");
      return StatusExecution.FACTORY.unspecifiedAfterAction("likely the page has changed on action");

    } catch (final ElementNotVisibleException e) {
      LOG.info("Cannot perform <pressEnter> action into <'{}'>. It is not visible", target);
      return StatusExecution.FACTORY.invisibleTarget();
    }

  }

  private DOMTypeableElement getTypebleIfAny(final DOMElement targetElement) {
    final HTMLUtil htmlUtil = targetElement.htmlUtil();
    DOMTypeableElement typeble = htmlUtil.asHTMLInputElement();
    if (typeble == null) {
      typeble = htmlUtil.asHTMLTextArea();
    }
    return typeble;
  }
}
