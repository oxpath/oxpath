/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction;

import uk.ac.ox.cs.diadem.webapi.exception.WebAPIInteractionException;

/**
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface WebActionExecutor {

  public static enum ActionType {
    click, pressEnter, submit, selectOption, autoComplete, datePicker, typeIn, navigate, back, moveToFrame, notExecutable;
  }

  // public ActionExecutionReport execute(final ExecutableAction actionWrapper, FactSerializer factSerializer)
  // throws ProcessingException;

  public ActionExecutionReport execute(final ActionSpecification actionWrapper) throws WebAPIInteractionException;
}
