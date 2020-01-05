/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLOptionsCollection;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLSelect;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class SelectAction extends AbstractBrowserAction {

  private int optionIndex;
  private final DOMElement targetElement;

  /**
   * @param isFormAction
   * @param actionDescription
   * @param target
   * @param optionIndex2
   * @throws ProcessingException
   */
  public SelectAction(final boolean isFormAction, final String actionDescription, final DOMElement targetElement,
      final int optionIndex) {
    super(isFormAction, actionDescription);
    this.targetElement = targetElement;
  }

  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {

    final DOMHTMLSelect selectElement = targetElement.htmlUtil().asHTMLSelect();
    if (selectElement == null)
      throw new WebAPIInteractionException("We are selecting on a non select element: " + targetElement, LOG);

    if (selectElement.isStale())
      return StatusExecution.FACTORY.failedActionOnStaleElement("action id:" + actiondescription);

    if (!checkEnabled(selectElement)) {
      LOG.info("Cannot select options in <'{}'>. It is not enabled", selectElement);
      final ExecutionStatus status = StatusExecution.FACTORY.disabledElement();
      return status;
    }

    // select by index ..we should try by node
    LOG.trace("Selecting option index {} on element {}.", optionIndex, selectElement);
    ExecutionStatus exitStatus;
    try {
      selectElement.setSelectedIndex(optionIndex);
    } catch (final NoSuchElementException e) {
      // Ignore will be reported as failure

    } catch (final ElementNotVisibleException e) {
      LOG.info("Cannot select options in <'{}'>. It is not visible", selectElement);
      return StatusExecution.FACTORY.invisibleTarget();
    }

    try {
      // this action can already trigger the page change or in general the select is stale

      final int realSelectedIndex = selectElement.getSelectedIndex();
      LOG.trace("Selected index: {}", realSelectedIndex);
      final DOMHTMLOptionsCollection options = selectElement.getOptions();
      LOG.trace("Item: {}", options.item(realSelectedIndex));
      // LOG.trace("Value to be selected: {}, value selected: {}", inputValue,
      // iel.getOptions().item(iel.getSelectedIndex()).getTextContent());

      String toSelectText;
      try {
        toSelectText = options.item(optionIndex).getText();
      } catch (final IndexOutOfBoundsException e) {
        // Ignore will remain UNRETRIEVABLE;
        toSelectText = "UNRETRIEVABLE";
      }
      if (realSelectedIndex != optionIndex) {
        exitStatus = StatusExecution.FACTORY
            .failedSelectAction(toSelectText, options.item(realSelectedIndex).getText());
      } else {
        exitStatus = StatusExecution.FACTORY.successAction(toSelectText);
      }

    } catch (final StaleElementReferenceException e) {
      LOG.trace("Select DomElement {} is not attached to the page any longer. Likely the page has changed, cannot determine if selection is performed ");
      exitStatus = StatusExecution.FACTORY.unspecifiedAfterAction("likely page change on action");
    }

    return exitStatus;
  }
}
