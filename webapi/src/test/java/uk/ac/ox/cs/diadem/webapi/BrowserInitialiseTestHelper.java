package uk.ac.ox.cs.diadem.webapi;

import java.util.concurrent.TimeUnit;

import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

public class BrowserInitialiseTestHelper {
	
	private static final Boolean USE_XVFB_OVERRIDE = null;
	private static final String SCREEN = ":0";
	
	public static WebBrowser init() {
		  WebBrowserBuilder builder = new WebBrowserBuilder();
		  boolean hasXvfbMode = false;
		  if (USE_XVFB_OVERRIDE == null) {
			  try {
				  if (builder.getPlatformConfiguration().getBrowserXvfbRunFileAbsolutePath() != null)
					  hasXvfbMode = true;
			  }
			  catch (WebAPIRuntimeException e) {}
		  } else {
			  hasXvfbMode = USE_XVFB_OVERRIDE;
		  }
		  if (hasXvfbMode)
			  builder.getRunConfiguration().setXvfbMode(hasXvfbMode).setDisplayNumber(SCREEN);
		  WebBrowser browser = builder.build();
		  browser.setPageLoadingTimeout(10, TimeUnit.SECONDS);
		  return browser;
	  }
}
