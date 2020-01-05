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
class AlwaysFailAction extends AbstractBrowserAction {

  AlwaysFailAction(final String pmActionString) {
    super(false, pmActionString);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExecutionStatus execute(final WebBrowser browser) throws WebAPIInteractionException {
    LOG.error("Not yet supported  {}", actiondescription);

    final ExecutionStatus executionStatus = new ExecutionStatus() {

      @Override
      public Status getStatus() {
        return Status.FAIL;
      }

      @Override
      public String getReadableInfo() {
        return "Unsupported";
      }

      @Override
      public Cause getCause() {
        return Cause.UNSUPPORTED_ACTION;
      }

      @Override
      public Object forTest() {
        return null;
      }
    };

    return executionStatus;
  }

}
