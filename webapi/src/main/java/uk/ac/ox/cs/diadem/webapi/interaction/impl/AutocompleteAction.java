/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMBoundingClientRect;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMTypeableElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
import uk.ac.ox.cs.diadem.webapi.dom.HTMLUtil;
import uk.ac.ox.cs.diadem.webapi.dom.utils.WebUtils;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class AutocompleteAction extends AbstractBrowserAction {

  private final DOMElement target;
  private final String value;
  protected static final Logger LOG = LoggerFactory.getLogger(AutocompleteAction.class);

  /**
   * @param target2
   * @param value2
   * @param isFormAction
   * @param pmActionAsString
   */
  public AutocompleteAction(final DOMElement target, final String value, final boolean isFormAction,
      final String pmActionAsString) {
    super(isFormAction, pmActionAsString);
    this.target = target;
    this.value = value;

  }

  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {

    return do_execute(target, value, browser);
  }

  private ExecutionStatus do_execute(final DOMElement targetElement, final String valueToFill, final WebBrowser browser)
      throws WebAPIInteractionException {

    if (targetElement.isStale())
      return StatusExecution.FACTORY.failedActionOnStaleElement("action id:" + actiondescription);

    if (!checkEnabled(targetElement)) {
      LOG.info("Cannot select options in <'{}'>. It is not enabled", targetElement);
      final ExecutionStatus status = StatusExecution.FACTORY.disabledElement();
      return status;
    }

    if (valueToFill.equals("")) {
      LOG.warn("Autocomplete action {} with empty input value!", this);
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

      final DOMBoundingClientRect boundingClientRect = typable.getBoundingClientRect();
      final float width = boundingClientRect.getWidth();
      final float height = boundingClientRect.getHeight();

      final float left = boundingClientRect.getLeft();
      final float top = boundingClientRect.getTop();

      final float offSetX = width / 2;
      final int targetX = (int) (left + offSetX);
      final float offsetY = height * 1.5f;
      final int targetY = (int) (top + offsetY);

      final WebElement element = (WebElement) typable.getWrappedElement();

      final FirefoxDriver driver = WebUtils.castToDriver(browser);
      final String currentUrl = driver.getCurrentUrl();

      final Actions actions = new Actions(driver);
      element.click();

      // now is in view
      final DOMElement elementAtTargetPosition = browser.getContentDOMWindow().getDocument()
          .elementByPosition(targetX, targetY);

      element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
      element.sendKeys(Keys.BACK_SPACE);

      // USES MODIFICATION OBSERVERS
      // DOMMutationObserver bodyObserver = null;
      //
      // if (false) {
      // bodyObserver = WidgetDetectionUtils.installObserverOnBody(browser,
      // WidgetDetectionUtils.mutationObserverOptions(true, true, false, true, null));
      // }

      simulateTyping(actions);

      // USES MODIFICATION OBSERVERS
      // Set<DOMMutationRecord> records = Sets.newHashSet();
      //
      // if (bodyObserver != null) {
      // records = bodyObserver.takeRecords();
      // bodyObserver.disconnect();
      //
      // if (WidgetDetectionUtils.detectListBelow(typable, records)) {
      // LOG.debug("Detected autocomplete list");
      // // final float left = typable.getBoundingClientRect().getLeft();
      // actions.moveToElement(element, (int) offSetX, (int) offsetY).clickAndHold().release().build().perform();
      // }
      // }

      final DOMWindow window = browser.getContentDOMWindow();
      final DOMElement elementAtTargetPositionAfterAction = window.getDocument().elementByPosition(targetX, targetY);

      // if diffent, then there is a list popping up below
      if (!elementAtTargetPositionAfterAction.equals(elementAtTargetPosition)) {
        actions.moveToElement(element, (int) offSetX, (int) offsetY).clickAndHold().release().build().perform();
      }

      if (currentUrl.equals(driver.getCurrentUrl())) {
        // check it's state after
        final String afterValue = typable.getValue();
        if (!afterValue.equals(valueToFill)) {
          LOG.trace("Value: {}, afterValue: {}", valueToFill, afterValue);
          return StatusExecution.FACTORY.successAction(afterValue);
        } else
          return StatusExecution.FACTORY.failedTypeInAction(valueToFill, afterValue);
      } else
        return StatusExecution.FACTORY.successAction(driver.getCurrentUrl());

    } catch (final StaleElementReferenceException e) {
      LOG.trace("DomElement {} is not attached to the page any longer. Likely the page has changed, cannot determine if selection is performed ");
      return StatusExecution.FACTORY.unspecifiedAfterAction("likely the page has changed on action");

    } catch (final ElementNotVisibleException e) {
      LOG.info("Cannot perform typeIn action into <'{}'>. It is not visible", target);
      return StatusExecution.FACTORY.invisibleTarget();
    }

  }

  private void simulateTyping(final Actions actions) {
    for (final Character c : Lists.charactersOf(value)) {
      actions.sendKeys(String.valueOf(c)).perform();
      sleep(300, TimeUnit.MILLISECONDS);
    }

    sleep(1000, TimeUnit.MILLISECONDS);
    actions.sendKeys(Keys.DOWN).build().perform();

    sleep(1000, TimeUnit.MILLISECONDS);
  }

  private void sleep(final long timeout, final TimeUnit unit) {
    try {
      SLEEPER.poll(timeout, unit);
    } catch (final InterruptedException e) {

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
