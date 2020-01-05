/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction.impl;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;
import uk.ac.ox.cs.diadem.webapi.interaction.ExecutionStatus;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class NotExecutableAction extends AbstractBrowserAction {

  private final String readableInfo;

  NotExecutableAction(final boolean isFormAction, final String pmActionString, final String readableInfo) {
    super(isFormAction, pmActionString);
    this.readableInfo = readableInfo;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {
    LOG.error("Not executable action {} ", actiondescription);

    final ExecutionStatus executionStatus = new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.FAIL;
      }

      @Override
      public String getReadableInfo() {
        return readableInfo;
      }

      @Override
      public Cause getCause() {
        return Cause.STALE_DOM_NODE;
      }

      @Override
      public Object forTest() {
        return null;
      }
    };

    return executionStatus;
  }

}
