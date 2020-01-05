package uk.ac.ox.cs.diadem.webapi.exception;

import org.slf4j.Logger;

public class WebAPITimeoutException extends WebAPIRuntimeException {
  public WebAPITimeoutException(final String message, final Logger logger) {
    super(message, logger);
  }

  public WebAPITimeoutException(final String message, final Throwable cause, final Logger logger) {
    super(message, cause, logger);
  }

  private static final long serialVersionUID = 1L; // TODO

}