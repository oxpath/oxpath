/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

import uk.ac.ox.cs.diadem.util.misc.EscapingUtils;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.CSSMutationObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.CSSMutationRecord;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver.MutationObserverOptions;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationRecord;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationRecord.MutationType;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationDetectionUtils;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationRecord;
import uk.ac.ox.cs.diadem.webapi.dom.utils.WebUtils;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIStaleElementRuntimeException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPITimeoutException;
import uk.ac.ox.cs.diadem.webapi.interaction.ActionExecutionReport;
import uk.ac.ox.cs.diadem.webapi.interaction.ActionSpecification;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus.Cause;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus.Status;
import uk.ac.ox.cs.diadem.webapi.interaction.ObservedInteraction;
import uk.ac.ox.cs.diadem.webapi.interaction.Reports;
import uk.ac.ox.cs.diadem.webapi.interaction.Reports.ActionExecutionReportBuilder;
import uk.ac.ox.cs.diadem.webapi.interaction.Reports.ModificationReportBuilder;
import uk.ac.ox.cs.diadem.webapi.interaction.Reports.ModificationReportBuilder.BuilderForChangeOnFormInfo_OLD;
import uk.ac.ox.cs.diadem.webapi.interaction.WebActionExecutor;
import uk.ac.ox.cs.diadem.webapi.interaction.change.FactSerializer;
import uk.ac.ox.cs.diadem.webapi.pagestate.SimplePageStateRecorder;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 *
 */
public class WebDriverActionExecutor implements WebActionExecutor {

  /**
   * Singleton instance
   */

  private static final int SLEEP_AFTER_ACTION_MS = 1000;
  /**
   * Logging
   */
  private static final Logger LOG = LoggerFactory.getLogger(WebDriverActionExecutor.class);
  private static final BlockingQueue<Object> SLEEPER = new ArrayBlockingQueue<Object>(1);
  private final WebBrowser browser;

  /**
   *
   */
  private WebDriverActionExecutor(final WebBrowser browser) {
    this.browser = browser;

  }

  public static WebDriverActionExecutor getInstance(final WebBrowser browser) {
    return new WebDriverActionExecutor(browser);
  }

  @Override
  public ActionExecutionReport execute(final ActionSpecification actionWrapper) throws WebAPIInteractionException {
    return execute(actionWrapper, new FactSerializer() {

      @Override
      public void outputFact(final String relationName, final List<Pair<String, String>> params) {
        // ignore

      }

      @Override
      public void outputFact(final String relationName, final Pair<String, String>... params) {
        // ignore

      }
    });
  }

  ActionExecutionReport execute(final ActionSpecification actionWrapper, final FactSerializer serializer)
      throws WebAPIInteractionException {
    // builder for feedback on execution
    final ActionExecutionReportBuilder actionReportBuilder = Reports.newActionModificationReportBuilder();
    try {

      // FIXME HACK
      final FirefoxDriver driver = WebUtils.castToDriver(browser);

      // feedback for change
      final ModificationReportBuilder changeReportBuilder = actionReportBuilder.createOrGetModificationReportBuilder();

      // needed for checking same page or differences
      final SimplePageStateRecorder state = new SimplePageStateRecorder(browser);
      state.recordPageState();

      // check if a new window opened
      Integer numberOfWindowsBeforeAction = 1;
      if (driver != null) {
        numberOfWindowsBeforeAction = driver.getWindowHandles().size();
      }

      final ObservedInteraction observedChanges = actionWrapper.getChangesToWatch();

      // install observers on form if requested
      // it maps (PMNodeId,Node) --> MutationObserver
      SetMultimap<Entry<String, DOMNode>, MutationObserver> formObservers = HashMultimap.create();

      if (observedChanges.watchForm()) {

        formObservers = installObserverOnFormIfRequested(observedChanges.getFormNodesToObserve());
      }

      // install also on body for general changes
      final DOMMutationObserver bodyObserver = MutationDetectionUtils.installObserverOnBody(browser,
          MutationDetectionUtils.mutationObserverOptions(true, true, true, false, null));

      final URL currentUrl = browser.getURL();

      try {
        // execute the action
        final ExecutionStatus status = performAction(actionWrapper);
        sleep(SLEEP_AFTER_ACTION_MS, TimeUnit.MILLISECONDS);
        // report the action status
        actionReportBuilder.actionStatus(status);

        // we stop if not success
        if (!status.getStatus().isOkish())
          return actionReportBuilder.build();

      } catch (final WebAPIStaleElementRuntimeException e) {
        LOG.warn("Action {} performed on a stale element, report failure", actionWrapper.getPMActionString());
        actionReportBuilder.actionStatus(StatusExecution.FACTORY
            .failedActionOnStaleElement("The dom element is no longer attached to the DOM"));
        return actionReportBuilder.build();

      } catch (final StaleElementReferenceException e) {
        LOG.warn("Action {} performed on a stale element, report failure", actionWrapper.getPMActionString());
        actionReportBuilder.actionStatus(StatusExecution.FACTORY
            .failedActionOnStaleElement("The dom element is no longer attached to the DOM"));
        return actionReportBuilder.build();
      } catch (final WebAPITimeoutException e) {
        LOG.warn("Action {} exceeded timeout ", actionWrapper.getPMActionString());

        boolean done = false;

        // we try to leave the browser in a consistent state
        if (browser.isBackEnabled()) {

          LOG.trace("browser going back to previous state");
          final ExecutionStatus backStatus = BackAction._executeAction(browser, currentUrl);
          if (backStatus.getStatus() == Status.SUCCESS) {
            done = true;
          } else {
            LOG.trace("landing page after back differs from previous one, forcing navigation to {}", currentUrl);
            final ExecutionStatus navigate = NavigateAction._execute(browser, currentUrl);
            if (navigate.getStatus() == Status.SUCCESS) {
              done = true;
            }
          }
        }

        if (!done) { // it's a FATAL error we create a new browser set to blank and feedback FATAL
          browser.stop();
          actionReportBuilder.actionStatus(StatusExecution.FACTORY.failedActionTimeout(Status.FATAL, Cause.TIMEOUT));
        } else {
          // we report the main reason, that is TIMEOUT
          actionReportBuilder.actionStatus(StatusExecution.FACTORY.failedActionTimeout(Status.FAIL, Cause.TIMEOUT));
        }
        return actionReportBuilder.build();
      } catch (final WebAPIRuntimeException e) {
        LOG.warn("Error performing action <{}> reporting failure. Reason is <{}>", actionWrapper.getPMActionString(),
            e.getMessage());
        actionReportBuilder.actionStatus(StatusExecution.FACTORY.failedAction(e.getMessage()));
        return actionReportBuilder.build();
      }

      // alert, in which case we dismiss
      checkIfAlert(observedChanges, driver, changeReportBuilder);

      // // alert stops the rest
      // if (alertDetected)
      // return actionReportBuilder.build();

      final boolean newWin = checkNewWin(driver, changeReportBuilder, numberOfWindowsBeforeAction);
      if (newWin)
        return actionReportBuilder.build();

      // possible effect after the action is a new page
      if (!state.currentStateIsAtLocation(browser.getLocationURL())) {
        LOG.trace("A new page has been loaded '<{}>'", browser.getLocationURL());
        changeReportBuilder.pageModification(driver.getCurrentUrl(), driver.getTitle());
        return actionReportBuilder.build();
      }

      // here we are safe we are on the same page

      // check for autocomplete to click on
      // TODO maybe integrate also DATEPICKER or OTHER
      if (observedChanges.watchAutocomplete()) {
        final DOMNode autocompleteElementClicked = MutationDetectionUtils.performAutocompleteIfAny(bodyObserver,
            browser);
        if (autocompleteElementClicked != null) {
          LOG.trace("Autocomplete detected on execution of action {}", actionWrapper);
          actionReportBuilder.actionStatus(StatusExecution.FACTORY.autocompleteSuccess(autocompleteElementClicked
              .getTextContent()));

        }
      }

      // state after action and after autocomplete
      // its possible the submit has gone through direclty
      state.recordPageState();
      // possible effect after the action is a new page
      if (!state.atSameLocation()) {
        LOG.trace("A new page has been loaded '<{}>'", browser.getLocationURL());
        changeReportBuilder.pageModification(driver.getCurrentUrl(), driver.getTitle());
        return actionReportBuilder.build();
      }

      // else changes in the page
      if (state.hasPageChanged()) {
        if (!state.isPageSimilar()) { // minor change is ok
          LOG.trace("Major changes (total:<{}>) on page after action'<{}>'", 30, actionWrapper);
          changeReportBuilder.majorPageModification(state.whyIsNotsimilar());
        } else {
          LOG.trace("Only small changes on page after action  {}", actionWrapper);
        }
        return actionReportBuilder.build();
      }

      // else check for form changes
      if (observedChanges.watchForm())
        if (checkFormChanges(formObservers, changeReportBuilder, observedChanges.getFormNodeLocalId())) {
          LOG.trace("Changes on form detected after execution of {}", actionWrapper);
          return actionReportBuilder.build();
        }

      if (actionReportBuilder.getCurrentExecutionStatus() == null) {
        LOG.error("Unexpected null for action execution status in ActionExecutor, forcing failing state.");
        actionReportBuilder.actionStatus(StatusExecution.FACTORY.failedAction("forced due to unexpected null"));
      }
      return actionReportBuilder.build();
    }
    // catch (final DDMNoSuchNodeException e) {
    //
    // throw new ProcessingException("Error performing action: " + actionWrapper.getPMActionString(), e, LOG);
    //
    // } catch (final PMUniquenessViolationException e) {
    // throw new ProcessingException("Error performing action: " + actionWrapper.getPMActionString(), e, LOG);
    // }
    catch (final Exception e) {
      LOG.error("Error on Action {} <{}>", actionWrapper.getPMActionString(), ExceptionUtils.getStackTrace(e));
      actionReportBuilder.actionStatus(StatusExecution.FACTORY.failedAction("Error " + e.getMessage()));
      return actionReportBuilder.build();
    }
  }

  /**
   * @param actionWrapper
   * @return
   * @throws WebAPIInteractionException
   */
  private ExecutionStatus performAction(final ActionSpecification action) throws WebAPIInteractionException {

    final ActionType type = action.getActionType();
    final String value = action.getParamenterValue().orNull();
    final String xPathLocator = action.getTargetXPathLocator().orNull();

    DOMElement target = action.getTargetElement().orNull();
    if ((target == null) && (xPathLocator != null)) {
      target = browser.getDOMElementByLocator(xPathLocator);
    }

    final boolean isFormAction = action.isFormAction();
    final String actionDescription = action.getActionDescription();
    final URL url = action.getParamenterURL().orNull();

    switch (type) {
    case autoComplete:
      return new AutocompleteAction(target, value, isFormAction, actionDescription).execute(browser);
    case back:
      return new BackAction(isFormAction, actionDescription, url).execute(browser);
    case click:
      return new ClickOnElementAction(target, xPathLocator, isFormAction, actionDescription).execute(browser);
    case datePicker:
      return new DatePickerAction(target, value, isFormAction, actionDescription).execute(browser);
    case moveToFrame:
      return new MoveToFrameAction(isFormAction, actionDescription, xPathLocator, value).execute(browser);
    case navigate:
      return new NavigateAction(isFormAction, actionDescription, url).execute(browser);
    case notExecutable:
      return new NotExecutableAction(isFormAction, actionDescription, action.getErrorMessage().get()).execute(browser);
    case pressEnter:
      return new PressEnterAction(target, isFormAction, actionDescription).execute(browser);
    case selectOption:
      return new SelectAction(isFormAction, actionDescription, target, action.getOptionIndex().get()).execute(browser);
    case submit:
      return new SubmitAction(target, isFormAction, actionDescription).execute(browser);
    case typeIn:
      return new TypeInAction(target, value, isFormAction, actionDescription).execute(browser);
    default:
      throw new WebAPIInteractionException("Unknown action type: " + type.name(), LOG);
    }
  }

  // private static boolean checkForErrorMessages(final DOMMutationObserver bodyObserver, final Set<DOMElement> exclude,
  // final ModificationReport report, final Widget widget) {
  //
  // // here only text inserted classified as error
  // final Set<DOMMutationRecord> records = bodyObserver.takeRecords();
  // bodyObserver.disconnect();
  //
  // final BuilderForTextualAdditions b = new ModificationReportImpl.BuilderForTextualAdditions(widget);
  // // tranform the map
  // for (final DOMMutationRecord mod : records) {
  // if (exclude.contains(mod.target())) {
  // continue;
  // }
  // if (mod.type() == MutationType.childList) {
  // final List<DOMNode> addedNodes = mod.addedNodes();
  // for (final DOMNode domNode : addedNodes)
  // if (domNode.isVisible()) {
  // b.addTextIfError(domNode.getTextContent());
  // }
  // }
  // if (mod.type() == MutationType.characterData) {
  // final DOMNode charData = mod.target();
  // if (charData.isVisible()) {
  // b.addTextIfError(charData.getTextContent());
  // }
  // }
  // }
  //
  // final TextualInfo textualInfo = b.build();
  // report.newErrorTextInfo(textualInfo);
  //
  // return textualInfo.isError();
  // }

  private void sleep(final long timeout, final TimeUnit unit) {
    try {
      SLEEPER.poll(timeout, unit);
    } catch (final InterruptedException e) {

    }
  }

  private static Map<DOMNode, List<DOMMutationRecord>> getRecordsByTargetNode(final Set<DOMMutationRecord> records) {
    final Map<DOMNode, List<DOMMutationRecord>> recordsByNode = Maps.newHashMap();
    // tranform the map
    for (final DOMMutationRecord record : records) {

      final DOMNode target = record.target();

      List<DOMMutationRecord> list = recordsByNode.get(target);
      if (list == null) {
        list = Lists.newArrayList();
        recordsByNode.put(target, list);
      }
      list.add(record);

    }
    return recordsByNode;
  }

  private static boolean checkFormChanges(final SetMultimap<Entry<String, DOMNode>, MutationObserver> formObservers,
      final ModificationReportBuilder changeReportBuilder, final String formNodeLocalId) {
    try {
      boolean mod = false;

      final Map<DOMNode, List<MutationRecord>> modificationsPerNode = Maps.newHashMap();

      // NOde to PMNodeID
      final Map<DOMNode, String> observedFormNodes = Maps.newHashMap();

      for (final Entry<String, DOMNode> formItemObserved : formObservers.keySet()) {

        final DOMNode domeElement = formItemObserved.getValue();

        if (domeElement.isStale()) {
          // formObservers.removeAll(observedItem);
          LOG.debug("Element {} is stale, continue", formItemObserved.getKey());
          continue;
        }
        // invert the Entry to a map
        observedFormNodes.put(domeElement, formItemObserved.getKey());

        LOG.debug("Checking modification of {}", formItemObserved.getKey());

        final Set<MutationObserver> mutationObservers = formObservers.get(formItemObserved);

        for (final MutationObserver mutationObserver : mutationObservers) {
          if (mutationObserver instanceof DOMMutationObserver) {
            handleDomMutation((DOMMutationObserver) mutationObserver, formItemObserved.getKey(), modificationsPerNode);
          } else if (mutationObserver instanceof CSSMutationObserver) {
            handleCSSMutation();
          }
        }

      }
      if (!modificationsPerNode.isEmpty()) {
        mod = createChangeReportForForm(observedFormNodes, modificationsPerNode, changeReportBuilder, formNodeLocalId);
      }

      return mod;
    } catch (final WebAPIStaleElementRuntimeException e) {

      LOG.warn("Ignored StaleElementReferenceException during form change detection {}", e);
    }
    return false;
  }

  private static void handleCSSMutation() {
    // TODO

  }

  private static void handleDomMutation(final DOMMutationObserver mutationObserver, final String nodeId,
      final Map<DOMNode, List<MutationRecord>> modificationsPerNode) {

    final Set<DOMMutationRecord> records = mutationObserver.takeRecords();
    mutationObserver.disconnect();

    // tranform the map
    for (final DOMMutationRecord record : records) {

      final DOMNode target = record.target();
      // check is stale and skip in case
      if (target.isStale()) {
        LOG.trace("Stale target node for modification {} on node {}, continue", record.type(), nodeId);
        continue;
      }
      List<MutationRecord> list = modificationsPerNode.get(target);
      if (list == null) {
        list = Lists.newArrayList();
        modificationsPerNode.put(target, list);
      }
      list.add(record);
    }

  }

  private static boolean createChangeReportForForm(final Map<DOMNode, String> observedFormNodes,
      final Map<DOMNode, List<MutationRecord>> modificationsPerNode, final ModificationReportBuilder report,
      final String formNodeLocalId) {
    final boolean[] changeOnForm = new boolean[] { false };

    // this will remove nodes from the map modificationsPerNode
    // visitWidgetFieldsAndBuildReport(formRootNode, modificationsPerNode, report, browser, changeOnForm);
    buildReportForObservedFormNodes(observedFormNodes, modificationsPerNode, report, changeOnForm);

    // here the remaining modifications in modificationsPerNode
    final Set<DOMNode> keySet = modificationsPerNode.keySet();

    for (final DOMNode domNode : keySet) {

      if (domNode.isStale()) {
        continue;
      }

      final List<MutationRecord> nonWidget = modificationsPerNode.get(domNode);
      for (final MutationRecord domMutationRecord : nonWidget) {
        final boolean chnage = checkOtherModificationsOnForm(domMutationRecord, report, formNodeLocalId);
        if (chnage) {
          changeOnForm[0] = true;
        }
      }
    }
    return changeOnForm[0];
  }

  //
  private static boolean checkOtherModificationsOnForm(final MutationRecord modRecord,
      final ModificationReportBuilder report, final String formNodeLocalId) {
    boolean change = false;
    final BuilderForChangeOnFormInfo_OLD builder = report.new BuilderForChangeOnFormInfo_OLD(formNodeLocalId);
    if (modRecord instanceof CSSMutationRecord) {
      // TODO
    }
    // dom
    else if (modRecord instanceof DOMMutationRecord) {

      final DOMMutationRecord mod = (DOMMutationRecord) modRecord;
      if (mod.type() == MutationType.childList) {
        if (!mod.addedNodes().isEmpty()) {

          final List<DOMNode> addedNodes = mod.addedNodes();

          for (final DOMNode domNode : addedNodes) {
            if (domNode.isStale()) {
              continue;
            }
            if (!domNode.isVisible()) {
              continue;
            }

            if ((domNode instanceof DOMElement) && WebUtils.isValidFormTag(domNode.getLocalName())) {
              // why this check??
              if (mod.target().isVisible()) {
                LOG.trace("New form field detected {} for widget {}", domNode.getLocalName(), formNodeLocalId);
                builder.reportNewField(domNode.getLocalName());
                change = true;
              }
            } else {

              final String normalizeTextNodes = EscapingUtils.normalizeTextNodes(domNode.getTextContent());
              if (normalizeTextNodes.length() > 0) {
                LOG.trace("New element appeared with text {} for widget {}", normalizeTextNodes, formNodeLocalId);
                // report new text appearing if any
                builder.reportTextChange("", normalizeTextNodes);
                change = true;
              }

            }
          }
        }

        if (!mod.removedNodes().isEmpty()) {
          final List<DOMNode> removedNodes = mod.removedNodes();
          for (final DOMNode removed : removedNodes) {
            // removed
            if (removed.isStale()) {
              continue;
            }
            if ((removed instanceof DOMElement) && WebUtils.isValidFormTag(removed.getLocalName())) {
              LOG.trace("Node deletion {} on form {}", removed, formNodeLocalId);
              builder.reportRemovedField(removed.getLocalName());
              change = true;
            }

          }
        }

      }

      if (mod.type() == MutationType.attributes) {

        final String localName = mod.target().getLocalName();

        if (WebUtils.isValidFormTag(localName)) {

          if (mod.target().isVisible()) {
            LOG.trace("New form field detected {} for widget {}", localName, formNodeLocalId);
            builder.reportNewField(localName);
            change = true;
          }
        } else {
          LOG.trace("Ignore Attribute modification {} on non field element {}", mod.attributeName(), localName);
        }
      }

      if (mod.type() == MutationType.characterData) {
        final String normalizeTextNodes = EscapingUtils.normalizeTextNodes(mod.target().getTextContent());
        if (normalizeTextNodes.length() > 0) {
          builder.reportTextChange(mod.oldValue(), normalizeTextNodes);
          change = true;
        }
      }

      if (change) {
        report.newFormChange(builder.build());
      }
    }
    return change;
  }

  //

  private static SetMultimap<Entry<String, DOMNode>, MutationObserver> installObserverOnFormIfRequested(
      final Map<String, DOMNode> nodesToObserve) {

    final SetMultimap<Entry<String, DOMNode>, MutationObserver> observed = HashMultimap.create();

    final MutationObserverOptions options = MutationDetectionUtils
        .mutationObserverOptions(true, true, true, true, null);

    // on all elements we register an observer
    for (final Entry<String, DOMNode> field : nodesToObserve.entrySet()) {
      observed.put(field, field.getValue().registerMutationObserver(options));
      // TODO add CSS MUTATION HERE
    }
    return observed;
  }

  /**
   * @param observedFormNodes
   * @param modificationsPerNode
   * @param report
   * @param browser2
   * @param changeOnForm
   */
  private static void buildReportForObservedFormNodes(final Map<DOMNode, String> observedFormNodes,
      final Map<DOMNode, List<MutationRecord>> modificationsPerNode, final ModificationReportBuilder report,
      final boolean[] changeOnForm) {

    for (final Entry<DOMNode, String> currentDomField : observedFormNodes.entrySet()) {
      if (modificationsPerNode.containsKey(currentDomField.getKey())) {
        // remove and build report
        final List<MutationRecord> removed = modificationsPerNode.remove(currentDomField.getKey());
        buildChangeReportForNode(currentDomField.getValue(), removed, report);// TODO check
        changeOnForm[0] = true;
      }
    }

  }

  private static void buildChangeReportForNode(final String currentFormNodeLocalId,
      final List<MutationRecord> mutationsForCurrentNode, final ModificationReportBuilder report) {

    if (mutationsForCurrentNode == null)
      return;

    LOG.trace("form field  <{}> has been modified <{}>", currentFormNodeLocalId, mutationsForCurrentNode);

    final BuilderForChangeOnFormInfo_OLD builder = report.new BuilderForChangeOnFormInfo_OLD(currentFormNodeLocalId);
    boolean change = false;

    for (final MutationRecord modRec : mutationsForCurrentNode) {

      if (modRec instanceof DOMMutationRecord) {
        final DOMMutationRecord mod = (DOMMutationRecord) modRec;
        if (mod.type() == MutationType.childList)
          if (!mod.addedNodes().isEmpty()) {
            builder.reportOptionAddition();
            change = true;
          }
        if (!mod.removedNodes().isEmpty()) {
          builder.reportOptionDeletion();
          change = true;
        }
        if (mod.type() == MutationType.attributes) {
          //
          change = change || builder.reportAttributeChange(mod.attributeName(), mod.oldValue(), mod.newValue());

        }
        if (mod.type() == MutationType.characterData) {

          final String normalizeTextNodes = EscapingUtils.normalizeTextNodes(mod.target().getTextContent());
          if (normalizeTextNodes.length() > 0) {
            builder.reportTextChange(mod.oldValue(), normalizeTextNodes);
            change = true;
          }
        }
      } else if (modRec instanceof CSSMutationRecord) {
        // TODO
      }
    }

    if (change) {
      report.newFormChange(builder.build());
    }
  }

  private static boolean checkNewWin(final FirefoxDriver driver, final ModificationReportBuilder report,
      final Integer numberOfWindows) {
    // check for new windows
    if (driver != null) {
      final int size = driver.getWindowHandles().size();
      if (size > numberOfWindows) {
        LOG.trace("A new window has been opened");
        final String thisWindow = driver.getWindowHandle();
        final String newWin = Iterables.get(driver.getWindowHandles(), size - 1);
        final WebDriver newDriver = driver.switchTo().window(newWin);

        final String newURL = newDriver.getCurrentUrl();
        report.newWindowOpened(newURL, newDriver.getTitle());
        LOG.trace("closing the new window");
        newDriver.close();
        LOG.trace("Switch to the original window");
        // back to the current
        driver.switchTo().window(thisWindow);
        driver.get(newURL);
        return true;
      }
    }

    return false;
  }

  private static boolean checkIfAlert(final ObservedInteraction watcher, final FirefoxDriver driver,
      final ModificationReportBuilder report) {
    // we check the alert before all
    if (watcher.watchAlert()) {
      try {
        LOG.trace("Checking for Alert popup ");
        // can fail if no alert is set
        final Alert alert = driver.switchTo().alert();
        final String text = alert.getText();
        report.alertDetected(text);
        LOG.trace("..found alert with message '<{}>' ", text);
        LOG.trace("dismissing alert");
        alert.dismiss();
        return true;
      } catch (final NoAlertPresentException e) {
        // no alert
        LOG.trace("..No alerts found");
      }
    }
    return false;
  }

  // private static void visitWidgetFieldsAndBuildReport(final PMFormNode currentFormNode,
  // final Map<DOMNode, List<MutationRecord>> modificationsPerNode, final ModificationReportBuilder report,
  // final WebBrowser browser, final boolean[] changeOnForm) throws ProcessingException {
  //
  // final Set<? extends PMFormNode> children = currentFormNode.getChildren();
  //
  // for (final PMFormNode child : children) {
  // visitWidgetFieldsAndBuildReport(child, modificationsPerNode, report, browser, changeOnForm);
  // }
  //
  // LOG.trace("Checking Mutations for {}", currentFormNode);
  //
  // try {
  // if (!currentFormNode.getNodes().isEmpty()) {
  // final DOMElement currentDomField = (DOMElement) currentFormNode.getFirstNode().getNode().asDOMNode(browser);
  // // if there are modifications detected on this element
  // if (modificationsPerNode.containsKey(currentDomField)) {
  // // remove and build report
  // final List<MutationRecord> removed = modificationsPerNode.remove(currentDomField);
  // buildChangeReportForNode(currentFormNode, removed, report);// TODO check
  // changeOnForm[0] = true;
  // }
  // } else {// the same for members
  // final SortedSet<PMFormMember> options = currentFormNode.getMembers();
  // for (final PMFormMember widgetOption : options) {
  // final DOMElement opt = (DOMElement) widgetOption.getFirstNode().getNode().asDOMNode(browser);
  // if (modificationsPerNode.containsKey(opt)) {
  // buildChangeReportForNode(currentFormNode, modificationsPerNode.remove(opt), report);
  // changeOnForm[0] = true;
  // }
  // }
  // }
  // } catch (final DDMNoSuchNodeException e) {
  // LOG.debug(e.getMessage());
  // LOG.debug("Ignoring this form node fields");
  // return;
  // }
  //
  // }

}
