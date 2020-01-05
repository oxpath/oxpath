package org.oxpath.browserinstaller;

import org.slf4j.Logger;

/**
 * Runtime exception
 * 
 * @author Ruslan Fayzrakhmanov
 * 6 Sep 2018
 */
public class BrowserInstallerRuntimeException extends RuntimeException {
	
	private static final long serialVersionUID = 6684158821273424095L;
	
	public BrowserInstallerRuntimeException(final String message, final Logger logger, Object... arguments) {
		super(message);
		if (logger != null) {
			logger.error(message, arguments);
		}
	}
	
	public BrowserInstallerRuntimeException(final String message, final Throwable cause, final Logger logger, Object... arguments) {
		super(message, cause);
		if (logger != null) {
			logger.error(message, arguments);
		}
	}

}
