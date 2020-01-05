/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.exception;

import org.slf4j.Logger;

/**
 * 
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class WebAPIStaleElementRuntimeException extends WebAPIRuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = -7315093315414855203L;

  /**
   * @param message
   * @param logger
   */
  public WebAPIStaleElementRuntimeException(final String message, final Logger logger) {
    super(message, logger);
  }

  /**
   * @param message
   * @param cause
   * @param logger
   */
  public WebAPIStaleElementRuntimeException(final String message, final Throwable cause, final Logger logger) {
    super(message, cause, logger);
  }

}
