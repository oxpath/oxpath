/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMTypeableElement;
import uk.ac.ox.cs.diadem.webapi.dom.HTMLUtil;
import uk.ac.ox.cs.diadem.webapi.dom.utils.WebUtils;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class DatePickerAction extends AbstractBrowserAction {

  private final DOMElement target;
  private final String value;
  protected static final Logger LOG = LoggerFactory.getLogger(DatePickerAction.class);

  DatePickerAction(final DOMElement target, final String value, final boolean isFormAction,
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
      LOG.warn("TypeInAction action {} with empty input value!", this);
    }

    // do the action
    LOG.trace("Typing <{}> into <{}>.", targetElement, valueToFill);

    try {

      final DOMTypeableElement typable = getTypebleIfAny(targetElement);

      // no input or textarea element
      if (typable == null) {
        final String nodeName = targetElement.getNodeName();
        LOG.error("Cannot perform typeIn action into a non input/textarea element: {}", nodeName);
        throw new WebAPIInteractionException("Cannot perform typeIn action into a non input/textarea element:"
            + nodeName, LOG);

      }

      final FirefoxDriver driver = WebUtils.castToDriver(browser);
      final String currentUrl = driver.getCurrentUrl();
      final int radius = 100;
      final List<DOMElement> neighbourBefore = typable.getNeighbourhood(radius);

      // visible nodes
      final Predicate<DOMElement> predicate = new Predicate<DOMElement>() {

        @Override
        public boolean apply(final DOMElement input) {
          return !input.isVisible();
        }
      };
      final List<DOMElement> similarNodesToFillBeforeAction = Lists.transform(
          XPathUtil.getNodes("descendant-or-self::*[./text() = " + valueToFill + "]", browser),
          new Function<DOMNode, DOMElement>() {

            @Override
            public DOMElement apply(final DOMNode input) {

              return (DOMElement) input;
            }
          });

      Iterables.removeIf(similarNodesToFillBeforeAction, predicate);

      final WebElement element = (WebElement) typable.getWrappedElement();
      final Actions actions = new Actions(driver);
      // actions.moveToElement(element).clickAndHold().release().build().perform();
      element.click();
      sleep(1, TimeUnit.SECONDS);
      final List<DOMElement> neighbourAfterAction = typable.getNeighbourhood(radius);

      final DOMElement hook = getJustAppearedIfAny(neighbourBefore, neighbourAfterAction);

      if (hook == null) {
        // "No date picker popped up, hanlding the inline case by clicking on the fisr node that was present before the
        // action
        // this can be refined by filtering by proximity

        if (similarNodesToFillBeforeAction.isEmpty())
          return StatusExecution.FACTORY.failedAction("No date picker recognized");
        else {
          similarNodesToFillBeforeAction.get(0).click();
          sleep(1, TimeUnit.SECONDS);
          return checkActualExecutionState(valueToFill, typable, driver, currentUrl);
        }
      } else {
        // here something popped up and was recognized
        final List<DOMNode> candidateForFilling = XPathUtil.getNodes("ancestor-or-self::*[./text() = '" + valueToFill
            + "'][1] | descendant::*[./text() = " + valueToFill + "][1] | following::*[./text() = " + valueToFill
            + "][1] | preceding::*[./text() = " + valueToFill + "][1]", hook);

        if (candidateForFilling.isEmpty()) {
          LOG.debug("Cannot find a node <{}> to click on", valueToFill);
          return StatusExecution.FACTORY.failedAction("Cannot find a node to click on containing " + valueToFill);
        }

        boolean clicked = false;
        for (final DOMNode domNode : candidateForFilling) {
          if (!similarNodesToFillBeforeAction.contains(domNode)) {

            final DOMElement found = (DOMElement) domNode;
            if (found.isVisible()) {
              LOG.debug("Found {} and clicking on", found);
              found.click();
              clicked = true;
            }
          }
          if (clicked) {
            break;
          }
        }

        if (!clicked)
          return StatusExecution.FACTORY.failedAction("No date picker found");

        sleep(1, TimeUnit.SECONDS);

        return checkActualExecutionState(valueToFill, typable, driver, currentUrl);
      }

    } catch (final StaleElementReferenceException e) {
      LOG.trace("DomElement {} is not attached to the page any longer. Likely the page has changed, cannot determine if selection is performed ");
      return StatusExecution.FACTORY.unspecifiedAfterAction("likely the page has changed on action");

    } catch (final ElementNotVisibleException e) {
      LOG.info("Cannot perform typeIn action into <'{}'>. It is not visible", target);
      return StatusExecution.FACTORY.invisibleTarget();
    }

  }

  private ExecutionStatus checkActualExecutionState(final String valueToFill, final DOMTypeableElement typable,
      final FirefoxDriver driver, final String currentUrl) {
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
  }

  private DOMElement getJustAppearedIfAny(final List<DOMElement> beforeAction, final List<DOMElement> afterAction) {

    for (int i = 0; i < beforeAction.size(); i++) {
      final DOMElement before = beforeAction.get(i);
      final DOMElement after = afterAction.get(i);
      if (!after.equals(before))
        return after;

    }
    return null;
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
