/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface ActionExecutionReport {

  /**
   * A concise {@link ExecutionStatus} on the execution
   *
   * @return a {@link ExecutionStatus} object
   */
  ExecutionStatus getActionExecutionStatus();

  /**
   * Returns a {@link ModificationReport} object to query the observed changes after the action
   *
   * @return
   */
  ModificationReport getModificationReport();

  // PMAction getPMDomAction();

}
