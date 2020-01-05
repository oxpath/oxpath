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
class TypeInAction extends AbstractBrowserAction {

  private final DOMElement target;
  private final String value;
  protected static final Logger LOG = LoggerFactory.getLogger(TypeInAction.class);

  TypeInAction(final DOMElement target, final String value, final boolean isFormAction, final String pmActionString) {
    super(isFormAction, pmActionString);
    this.target = target;
    this.value = value;

  }

  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {

    return do_execute(target, value);
  }

  private ExecutionStatus do_execute(final DOMElement targetElement, final String valueToFill)
      throws WebAPIInteractionException {
    if (targetElement.isStale())
      return StatusExecution.FACTORY.failedActionOnStaleElement("action id:" + actiondescription);
    if (!checkEnabled(targetElement)) {
      LOG.info("Cannot select options in <'{}'>. It is not enabled", targetElement);
      final ExecutionStatus status = StatusExecution.FACTORY.disabledElement();
      return status;
    }

    if (valueToFill.equals("")) {
      LOG.warn("TypeInAction action {} with empty input value!", this);
    }

    final DOMTypeableElement typable = getTypebleIfAny(targetElement);

    // no input or textarea element
    if (typable == null) {
      final String nodeName = targetElement.getNodeName();
      LOG.error("Cannot perform typeIn action into a non input/textarea element: {}", nodeName);
      throw new WebAPIInteractionException(
          "Cannot perform typeIn action into a non input/textarea element:" + nodeName, LOG);

    }

    // do the action
    LOG.trace("Typing <{}> into <{}>.", typable, valueToFill);

    try {
      typable.select();
      typable.type(valueToFill);
      // check it's state after
      final String afterValue = typable.getValue();
      if (!afterValue.equals(valueToFill)) {
        LOG.trace("Value: {}, afterValue: {}", valueToFill, afterValue);
        return StatusExecution.FACTORY.failedTypeInAction(valueToFill, afterValue);
      } else
        return StatusExecution.FACTORY.successAction(valueToFill);

    } catch (final StaleElementReferenceException e) {
      LOG.trace("DomElement {} is not attached to the page any longer. Likely the page has changed, cannot determine if selection is performed ");
      return StatusExecution.FACTORY.unspecifiedAfterAction("likely the page has changed on action");

    } catch (final ElementNotVisibleException e) {
      LOG.info("Cannot perform typeIn action into <'{}'>. It is not visible", target);
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
