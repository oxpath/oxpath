/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.interaction;

import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface ExecutionStatus {

  public enum Status {
    /**
     * E.g., a click on a non visible element, or not present
     */
    NON_EXECUTABLE,
    /**
     * The action performed is compatible with the expected behaviour
     */
    SUCCESS,
    /**
     * The execution led to unexpected effect (e.g., typed 'Oxford' but the value set is 'Oxfordshire'
     */
    FAIL,
    /**
     * An error from which it was not possible to recover
     */
    FATAL,
    /**
     * When it's not clear if the observed effect is an error or not. Delegates the decision to other feedback types
     * provided
     */
    UNSPECIFIED;

    public Pair<String, String> asConstant() {
      return Pair.of("status", name().toLowerCase());
    }

    public boolean isOkish() {
      return (this == SUCCESS) || (this == Status.UNSPECIFIED);
    }
  }

  public enum Cause {
    INVISIBLE_TARGET, INCONSISTENT_TYPING, SUCCESS, UNSUPPORTED_ACTION, INCONSISTENT_SELECTION, HTTP_ERROR_CODE, UNKNOWN, TIMEOUT, STALE_DOM_NODE, DISABLED;

    public Pair<String, String> asConstant() {
      return Pair.of("feedback", name().toLowerCase());
    }
  }

  /**
   * String representation
   *
   * @return
   */
  String getReadableInfo();

  /**
   *
   * @return
   */
  Status getStatus();

  Cause getCause();

  /**
   * Internal Use for easier tests
   *
   * @return
   */
  Object forTest();

}
