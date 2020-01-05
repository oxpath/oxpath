package uk.ac.ox.cs.diadem.webapi.dom.impl;

import org.slf4j.Logger;

import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

public class WebAPIBrowserBinaryNotFound extends WebAPIRuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = -6988319914151875857L;

  public WebAPIBrowserBinaryNotFound(final String message, final Logger logger) {
    super(message, logger);
  }

  public WebAPIBrowserBinaryNotFound(final String message, final Throwable cause, final Logger logger) {
    super(message, cause, logger);
  }

}
