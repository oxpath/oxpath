package uk.ac.ox.cs.diadem.webapi.exception;

import org.slf4j.Logger;

public class WebAPIUnsupportedJavascriptActionRuntimeException extends WebAPIRuntimeException {
  public WebAPIUnsupportedJavascriptActionRuntimeException(final String message, final Logger logger) {
    super(message, logger);
  }

  public WebAPIUnsupportedJavascriptActionRuntimeException(final String message, final Throwable cause, final Logger logger) {
    super(message, cause, logger);
  }

  private static final long serialVersionUID = 1L; // TODO

}