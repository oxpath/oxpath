/**
 *
 */
package uk.ac.ox.cs.diadem.webapi.exception;

import org.slf4j.Logger;

/**
 * @author giog
 *
 */
public class WebAPINotVisibleOrDisableElementException extends WebAPIRuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 4012052488729344098L;

  /**
   * @param message
   * @param logger
   */
  public WebAPINotVisibleOrDisableElementException(final String message, final Logger logger) {
    super(message, logger);
  }

  /**
   * @param message
   * @param cause
   * @param logger
   */
  public WebAPINotVisibleOrDisableElementException(final String message, final Throwable cause, final Logger logger) {
    super(message, cause, logger);
  }

}
