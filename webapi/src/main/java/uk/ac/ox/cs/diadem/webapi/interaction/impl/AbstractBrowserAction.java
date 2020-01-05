/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
abstract class AbstractBrowserAction {

  protected static final Logger LOG = LoggerFactory.getLogger(AbstractBrowserAction.class);
  static final BlockingQueue<Object> SLEEPER = new ArrayBlockingQueue<Object>(1);
  protected final boolean isFormAction;
  protected final String actiondescription;

  /**
   * @param target
   * @param isFormAction
   * @param actionDescription
   */
  public AbstractBrowserAction(final boolean isFormAction, final String actionDescription) {
    this.isFormAction = isFormAction;
    actiondescription = actionDescription;
  }

  /*
   * (non-Javadoc)
   *
   * @see uk.ac.ox.cs.diadem.webapi.interaction.ExecutableAction#getPMActionString()
   */
  public String getPMActionString() {
    return actiondescription;
  }

  protected boolean checkEnabled(final DOMElement target) {
    return target.isEnabled();
  }

  public abstract ExecutionStatus execute(WebBrowser browser) throws WebAPIInteractionException;

  // @Override
  // public ExecutionStatus executeMock(final DOMElement target, final Map<String, Object> parameters)
  // throws WebAPIInteractionException {
  //
  // return StatusExecution.FACTORY.failedAction("Method to be overridden");
  // }

  @Override
  public String toString() {

    return "Executable action: " + actiondescription;
  }
}
