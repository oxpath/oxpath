/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.impl;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.Executable;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import uk.ac.ox.cs.diadem.webapi.DialogsService;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder.Engine;
import uk.ac.ox.cs.diadem.webapi.configuration.JavaScriptConfiguration;
import uk.ac.ox.cs.diadem.webapi.configuration.PlatformConfiguration;
import uk.ac.ox.cs.diadem.webapi.configuration.RunConfiguration;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
import uk.ac.ox.cs.diadem.webapi.dom.impl.firefoxdriver.ConfigurableConnectFirefoxDriver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationFormObserver;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIPageNavigationRuntimeException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPITimeoutException;
import uk.ac.ox.cs.diadem.webapi.interaction.AdviceProcessor;
import uk.ac.ox.cs.diadem.webapi.interaction.WebActionExecutor;
import uk.ac.ox.cs.diadem.webapi.interaction.impl.WebAdviceProcessor;
import uk.ac.ox.cs.diadem.webapi.interaction.impl.WebDriverActionExecutor;
import uk.ac.ox.cs.diadem.webapi.listener.BrowserLocationListener;
import uk.ac.ox.cs.diadem.webapi.listener.BrowserProgressListener;
import uk.ac.ox.cs.diadem.webapi.listener.BrowserStatusTextListener;
import uk.ac.ox.cs.diadem.webapi.listener.BrowserTitleListener;
import uk.ac.ox.cs.diadem.webapi.listener.OpenNewWindowListener;
import uk.ac.ox.cs.diadem.webapi.utils.JSUtils;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class WebDriverBrowserImpl extends AbstractWebBrowser<FirefoxDriver> {
	
	public static class GeoLocation {
		public GeoLocation(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
		public double getLatitude() {
			return latitude;
		}
		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}
		public double getLongitude() {
			return longitude;
		}
		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}
		private double latitude;
		private double longitude;
		
		@Override
		public String toString() {
			return "GeoLocation [latitude=" + latitude + ", longitude=" + longitude + "]";
		}
	}
	
  FirefoxDriver driver_firefox;
  WebDriverWrapperFactory factory;

  private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverBrowserImpl.class);

//  private final BrowserFactory.BrowserConfiguration preferenceBackup;
  private final PlatformConfiguration platformConfiguration;
  private final RunConfiguration runOptionsConfiguration;
  private final JavaScriptConfiguration javaScriptConfiguration;
  
  Pair<Long, TimeUnit> timeOutPageLoading;

  // WebDriverBrowserImpl() {
  // this(null, null, false, ImmutableSet.<FeatureType> of(), ImmutableSet.<FeatureType> of());
  // }
  //
  // WebDriverBrowserImpl(final Set<FeatureType> disabledFeatures, final Set<FeatureType> enabledFeatures) {
  // this(null, null, disabledFeatures, enabledFeatures);
  // }
  //
  // WebDriverBrowserImpl(final String binary, final String displayNumber, final Set<FeatureType> disabledFeatures,
  // final Set<FeatureType> enabledFeatures) {
  // this(binary, displayNumber, false, disabledFeatures, enabledFeatures);
  // }

  public WebDriverBrowserImpl(
		  PlatformConfiguration platformConfiguration,
		  RunConfiguration runOptionsConfiguration,
		  JavaScriptConfiguration javaScriptConfiguration
		  ) throws ConfigurationException {
	  this.platformConfiguration = platformConfiguration;
	  this.runOptionsConfiguration = runOptionsConfiguration;
	  this.javaScriptConfiguration = javaScriptConfiguration;
//    preferenceBackup = new BrowserFactory.BrowserConfiguration(conf);
    instantiateBrowser(platformConfiguration, runOptionsConfiguration, javaScriptConfiguration);
  }

  /*
   * (non-Javadoc)
   *
   * @see uk.ac.ox.cs.diadem.webapi.MiniBrowser#getActionExecutor()
   */
  @Override
  public WebActionExecutor getActionExecutor() {

    return WebDriverActionExecutor.getInstance(this);
  }

  // WebDriverBrowserImpl(final String binary, final String displayNumber, final boolean useXfvb,
  // final Set<FeatureType> disabledFeatures, final Set<FeatureType> enabledFeatures, final boolean pluginsEnabled,
  // final Set<ContentType> blockedContentTypesAll, final Set<ContentType> blockedContentTypesOnlyThirdParties) {
  // preferenceBackup = backupPreferencesForBrowserReinstantiation(binary, displayNumber, useXfvb, disabledFeatures,
  // enabledFeatures, pluginsEnabled, blockedContentTypesAll, blockedContentTypesOnlyThirdParties);
  // instantiateBrowser(conf);
  // }

  // WebDriverBrowserImpl(final Set<FeatureType> disabledFeatures) {
  // this(null, null, false, disabledFeatures, ImmutableSet.<FeatureType> of());
  // }
  //
  // public WebDriverBrowserImpl(final String binary, final String displayNumber, final boolean useXfvb,
  // final Set<FeatureType> disabledFeatures, final Set<FeatureType> enabledFeatures) {
  // this(binary, displayNumber, useXfvb, disabledFeatures, enabledFeatures, true, ImmutableSet.<ContentType> of(),
  // ImmutableSet.<ContentType> of());
  // }

  //TODO add these paths into the configuration
  private void instantiateBrowser(PlatformConfiguration platformConfiguration,
		  RunConfiguration runOptionsConfiguration,
		  JavaScriptConfiguration javaScriptConfiguration) throws ConfigurationException {
//  	final String FIREBUG_LOCATION = WebDriverUtils.getExtractionPath() + File.separatorChar
//  		      + "firebug-2.0.18-fx.xpi";	
//  	final String FIREPATH_LOCATION = WebDriverUtils.getExtractionPath() + File.separatorChar
//  		      + "firepath-0.9.7.1-fx.xpi";
//  	// eventbug is part of firebug 
//  //  private static final String EVENTBUG_LOCATION = WebDriverUtils.getExtractionPath() + File.separatorChar
////        + "eventbug-0.1b10.xpi";
//    // Ensure that this plugin works for Firefox 47.0.1
//    final String IFRAME_HIGHLIGHT_LOCATION = WebDriverUtils.getExtractionPath() + File.separatorChar
//        + "event-annotations.xpi";
//    final String WEB_DEVELOPER_LOCATION = WebDriverUtils.getExtractionPath() + File.separatorChar
//        + "web_developer-1.2.11-fx+sm.xpi";
//    final String JS_DEMINIFIER_LOCATION = WebDriverUtils.getExtractionPath() + File.separatorChar
//        + "javascript_deminifier-1.0.9-fx.xpi";
//    final String ADBLOCK_LOCATION = WebDriverUtils.getExtractionPath() + File.separatorChar
//        + "adblock_plus-2.8.2-an+fx+sm+tb.xpi";
//    final String FACEBOOK_BLOCK = WebDriverUtils.getExtractionPath() + File.separatorChar
//        + "facebook_blocker-1.4-fx.xpi";
//    final String UBLOCK_ORIGIN_LOCATION = WebDriverUtils.getExtractionPath() + File.separatorChar
//  	      + "ublock_origin-1.10.0-an+fx+sm+tb.xpi";

//    final String displayNumber = p.displayNumber.orNull();
//    final Set<FeatureType> disabledFeatures = p.disabledFeatures;
//    final Set<FeatureType> enabledFeatures = p.enabledFeatures;
//    final boolean pluginsEnabled = p.pluginsEnabled;
//    final Set<ContentType> blockedContentTypesAll = p.blockedContentTypesAll;
//    final Set<ContentType> blockedContentTypesOnlyThirdParties = p.blockedContentTypesOnlyThirdParties;
//    final boolean useXvfb = p.useXvfb;

    collectStats = runOptionsConfiguration.getCollectStatistics();
    System.setProperty("webdriver.firefox.logfile", "/tmp/firefox.log");
    System.setProperty("webdriver.log.file", "/tmp/firefox_js.log");
    System.setProperty("webdriver.reap_profile", "true");
    
    String binaryAbsolutePath = null;
    
    // a custom binary has the precedence
    if (runOptionsConfiguration.getUseDefaultExecutables()) {
    	if (runOptionsConfiguration.getXvfbMode()) {
    		binaryAbsolutePath = platformConfiguration.getBrowserXvfbRunFileAbsolutePath();
                // System.setProperty("webdriver.firefox.bin", this.binary);
          } else {
        	  binaryAbsolutePath = platformConfiguration.getBrowserRunFileAbsolutePath();
          }
    } else {
    	if (runOptionsConfiguration.getUserExecutable() == null) {
    		// fall back to the system executable via selenium
    		binaryAbsolutePath = new Executable(null).getPath();
    	} else {
    		// preferenceBackup.binary=Optional.of(p.binary.get());
        	if (LOGGER.isDebugEnabled()) LOGGER.debug("Using binay {}", runOptionsConfiguration.getUserExecutable());
        	      // System.setProperty("webdriver.firefox.bin", binary);
    	}
    }
    
    // update the backup
//if (LOGGER.isDebugEnabled()) LOGGER.debug("Using browser executable {}", p.binary);
//    preferenceBackup.binary = Optional.of(p.binary.get());

//    if (displayNumber != null) {
//      // this.displayNumber = displayNumber;
//if (LOGGER.isDebugEnabled()) LOGGER.debug("Using display Number {}", displayNumber);
//    }

    // Never use in production https://code.google.com/p/selenium/wiki/FirefoxDriver
    // System.setProperty("webdriver.firefox.useExisting", "true");
    final FirefoxProfile firefoxProfile = getDefaultProfile(
    		runOptionsConfiguration.getUserAgent(),
    		runOptionsConfiguration.getScriptTimeoutSec(),
    		platformConfiguration.getDownloadDirAbsolutePath());
    
    GeoLocation location = runOptionsConfiguration.getGeoLocation();
    if (location == null) {
    	firefoxProfile.setPreference("geo.enabled", false);
    } else {
    	firefoxProfile.setPreference("geo.enabled", true);
        firefoxProfile.setPreference("geo.prompt.testing", true);
        firefoxProfile.setPreference("geo.prompt.testing.allow", true);
        final String lat = Double.toString(location.getLatitude());
        final String lng = Double.toString(location.getLongitude());
        // http://stackoverflow.com/questions/1800990/geolocation-provider-for-firefox-that-allows-manual-input#comments-5644224

        // final String value = "data:application/json,{\"status\": \"OK\", \"accuracy\": 10,\"location\":{\"lat\":" + lat
        // + ",\"lng\":" + lng + ",\"latitude\":" + lat + ",\"longitude\":" + lng + "},\"accuracy\":10}";
        final String value = "{\"status\": \"OK\", \"accuracy\": 10,\"location\":{\"lat\":" + lat + ",\"lng\":" + lng
            + ",\"latitude\":" + lat + ",\"longitude\":" + lng + "},\"accuracy\":10}";
        try {
          final File temp = File.createTempFile("temp-file-name", ".tmp");
          FileUtils.writeStringToFile(temp, value, StandardCharsets.UTF_8.toString());
          firefoxProfile.setPreference("geo.wifi.uri", temp.getAbsolutePath());
        } catch (IOException e) {
      	  throw new WebAPIRuntimeException(String.format("Cannot apply geo-location %s", location.toString()), e, LOGGER);
        }
    }
    
    if (!runOptionsConfiguration.getEnablePlugins()) {
      disablePlugins(firefoxProfile);
    }

    for (final ContentType c : runOptionsConfiguration.getDisabledThirdPartyContentTypes()) {
      switch (c) {
      case IMAGE:
        firefoxProfile.setPreference("permissions.default.image", 3);
        break;
      case OBJECT:
        firefoxProfile.setPreference("permissions.default.object", 3);
        break;
      case SCRIPT:
        firefoxProfile.setPreference("permissions.default.script", 3);
        break;
      case STYLESHEET:
        firefoxProfile.setPreference("permissions.default.stylesheet", 3);
        break;

      case SUBDOCUMENT:
        firefoxProfile.setPreference("permissions.default.subdocument", 3);
        break;

      default:
        LOGGER.warn("Unhandled content type {}", c);
        break;

      }
    }

    for (final ContentType c : runOptionsConfiguration.getDisabledContentTypes()) {
      switch (c) {
      case IMAGE:
        firefoxProfile.setPreference("permissions.default.image", 2);
        break;
      case OBJECT:
        firefoxProfile.setPreference("permissions.default.object", 2);
        break;
      case SCRIPT:
        firefoxProfile.setPreference("permissions.default.script", 2);
        break;
      case STYLESHEET:
        firefoxProfile.setPreference("permissions.default.stylesheet", 2);
        break;

      case SUBDOCUMENT:
        firefoxProfile.setPreference("permissions.default.subdocument", 2);
        break;

      default:
        LOGGER.warn("Unhandled content type {}", c);
        break;

      }
    }

//    // FIXME delete it as deprecated
//    for (final FeatureType f : disabledFeatures) {
//      switch (f) {
//      case DOWNLOAD_IMAGES:
//        firefoxProfile.setPreference("permissions.default.image", 2);
//        break;
//        // firefoxProfile.setPreference("permissions.default.stylesheet", 2);
//        // firefoxProfile.setPreference("permissions.default.image", 2);
//        // break;
//      case JAVASCRIPT:
//        // Can not be done, as we rely on javascript for communication
//        throw new IllegalArgumentException(
//            "Can't disable Javascript for a WebDriver implementation as it is essential for its functioning. ");
//        // firefoxProfile.setPreference("javascript.enabled", false);
//        // break;
//      case PLUGINS:
//        disablePlugins(firefoxProfile);
//
//        break;
//      default:
//        LOGGER.warn("Unhandled FeatureType  {}", f);
//        break;
//      }
//    }

//    // Complete enabledFeatures
//    final SortedSet<FeatureType> sortedFeatures = Sets.newTreeSet(enabledFeatures);
//    for (final FeatureType f : enabledFeatures) {
//      sortedFeatures.addAll(f.getRequirements());
//    }
//    // Now they are iterated in the order defined in FeatureType.
//    for (final FeatureType f : sortedFeatures) {
//      switch (f) {
//      case FIREBUG:
//        // Firebug
//        loadExtension(firefoxProfile, FIREBUG_LOCATION, "Firebug");
//        firefoxProfile.setPreference("extensions.firebug.currentVersion", "1.11.2"); // Avoid startup screen
//        firefoxProfile.setPreference("extensions.firebug.console.enableSites", "true"); // Activate console
//        firefoxProfile.setPreference("extensions.firebug.net.enableSites", "true"); // Activate net
//        firefoxProfile.setPreference("extensions.firebug.script.enableSites", "true"); // Activate script
//        firefoxProfile.setPreference("extensions.firebug.allPagesActivation", "on");
//        firefoxProfile.setPreference("extensions.firebug.previousPlacement", "1");
//        firefoxProfile.setPreference("extensions.firebug.onByDefault", "true");
//        firefoxProfile.setPreference("extensions.firebug.defaultPanelName", "console");
//        break;
//      case FIREBUG_HIDDEN:
//        // Firebug
//        loadExtension(firefoxProfile, FIREBUG_LOCATION, "Firebug");
//        firefoxProfile.setPreference("extensions.firebug.currentVersion", "1.11.2"); // Avoid startup screen
//        break;
//      case FIREPATH:
//        loadExtension(firefoxProfile, FIREPATH_LOCATION, "Firepath");
//        break;
//      case IFRAME_HIGHLIGHT:
//        loadExtension(firefoxProfile, IFRAME_HIGHLIGHT_LOCATION, "IFrame Highlighter");
//        break;
////      case EVENTBUG:
////        loadExtension(firefoxProfile, EVENTBUG_LOCATION, "Eventbug");
////        break;
////      case ADBLOCK:
////        loadExtension(firefoxProfile, ADBLOCK_LOCATION, "Adblock");
////        firefoxProfile.setPreference("extensions.adblockplus.currentVersion", "2.3.1"); // Avoid startup screen
////        firefoxProfile.setPreference("extensions.adblockplus.enabled", "true"); // Avoid startup screen
////        break;
//      case ADBLOCK:
//          loadExtension(firefoxProfile, UBLOCK_ORIGIN_LOCATION, "UBlock_Origin");
//          //firefoxProfile.setPreference("extensions.adblockplus.currentVersion", "2.3.1"); // Avoid startup screen
//          //firefoxProfile.setPreference("extensions.adblockplus.enabled", "true"); // Avoid startup screen
//          break;
//      case FACEBOOK_BLOCK:
//        loadExtension(firefoxProfile, FACEBOOK_BLOCK, "Facebook Blocker");
//        break;
//      case JS_DEMINIFIER:
//        loadExtension(firefoxProfile, JS_DEMINIFIER_LOCATION, "JS Deminifier");
//        break;
//      case WEB_DEVELOPER:
//        loadExtension(firefoxProfile, WEB_DEVELOPER_LOCATION, "Web Developer");
//        firefoxProfile.setPreference("extensions.webdeveloper.version", "1.2"); // Avoid startup screen
//        break;
//      default:
//        LOGGER.error("Unsupported feature: {}", f);
//        break;
//      }
//    }

    newDriverAndFactory(firefoxProfile, binaryAbsolutePath,
    		runOptionsConfiguration.getXvfbMode(), runOptionsConfiguration.getDisplayNumber(),
    		runOptionsConfiguration.getAutoPosition(),
    		runOptionsConfiguration.getPositionXMin(),
    		runOptionsConfiguration.getPositionYMin(),
    		runOptionsConfiguration.getBrowserWidth(),
    		runOptionsConfiguration.getBrowserHeight(),
    		runOptionsConfiguration.getPageLoadTimeoutSec());
    // set the dafault value
    manageOptions().setFallBackToJSExecutionOnNotInteractableElements(true);
  }

  // private void backupPreferencesForBrowserReinstantiation(final boolean useXvfb, final Set<FeatureType>
  // disabledFeatures,
  // final Set<FeatureType> enabledFeatures) {
  // originalDisabledFeatures = disabledFeatures;
  // originalEnabledFeatures = enabledFeatures;
  // this.useXvfb = useXvfb;
  // }

  private void disablePlugins(final FirefoxProfile firefoxProfile) {
    // Specifically flash, probably not necessary, but hey :)
    firefoxProfile.setPreference("dom.ipc.plugins.enabled.libflashplayer.so", false);
    firefoxProfile.setPreference("plugins.click_to_play", true);
    firefoxProfile.setPreference("plugin.default.state", 0);
    firefoxProfile.setPreference("plugin.disable_full_page_plugin_for_types", "application/pdf");
    firefoxProfile.setPreference("pdfjs.disabled", true);
    firefoxProfile.setPreference("pdfjs.firstRun", false);
    firefoxProfile.setPreference("plugin.scan.Acrobat", 999);
    firefoxProfile.setPreference("plugin.scan.Quicktime", 999);
    firefoxProfile.setPreference("plugin.scan.WindowsMediaPlayer", 999);
    firefoxProfile.setPreference("plugin.state.flash", 0);
    firefoxProfile.setPreference("plugin.state.java", 0);
  }

//  private static void loadExtension(final FirefoxProfile firefoxProfile, final String fileName, final String name) {
//    try {
//      firefoxProfile.addExtension(new File(fileName));
//    } catch (final Exception e) {
//      LOGGER.error("Could not load " + name + " extension!");
//    }
//  }

  private void newDriverAndFactory(final FirefoxProfile firefoxProfile,
		  String binaryAbsolutePath,
		  boolean xvfbMode,
		  String displayNumber,
		  boolean autoPosition,
		  int topLeftX,
		  int topLeftY,
		  int width,
		  int height,
		  int pageLoadTimeoutSec) {
//	final XMLConfiguration configuration = ConfigurationObject.getConfiguration("uk/ac/ox/cs/diadem/webapi/Configuration.xml");
    final DesiredCapabilities cap = DesiredCapabilities.firefox();
    cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, org.openqa.selenium.UnexpectedAlertBehaviour.DISMISS);
    cap.setCapability(FirefoxDriver.PROFILE, firefoxProfile);

    final FirefoxBinary firefoxBinary = new FirefoxBinary(new File(binaryAbsolutePath));
    if (xvfbMode) {
//      firefoxBinary.setEnvironmentProperty("DISPLAY", ":" + displayNumber);
      firefoxBinary.setEnvironmentProperty("DISPLAY", displayNumber);
    }
    cap.setCapability(FirefoxDriver.BINARY, firefoxBinary);

    // driver_firefox = new ConfigurableConnectFirefoxDriver(firefoxProfile);
    driver_firefox = new ConfigurableConnectFirefoxDriver(cap);
    // default time out
    /*setPageLoadingTimeout(ConfigurationFacility.getConfiguration().getInt("webdriver.options.timeouts.page-load-sec"),
        TimeUnit.SECONDS);*/
    setPageLoadingTimeout(pageLoadTimeoutSec, TimeUnit.SECONDS);
    
    if (autoPosition) {
    	int[] defPos = getDefaultBrowserPosition();
    	topLeftX = defPos[0];
    	topLeftY = defPos[1];
    }
    setWindowSize(width, height);
    setWindowPosition(topLeftX, topLeftY);
    factory = new WebDriverWrapperFactory(this, javaScriptConfiguration.getJavaScriptTemplateFunctionCalls());
  }

  @Override
  public Options manageOptions() {
    return new Options() {

      @Override
      public void enableOXPathOptimization(final boolean enabled) {
        factory.returnAlwaysFreshDocument = enabled;
      }

      @Override
      public void configureXPathLocatorHeuristics(final boolean useIdAttributeForXPathLocator,
          final boolean useClassAttributeForXPathLocator) {
        factory.useIdAttributeForXPathLocator = useIdAttributeForXPathLocator;
        factory.useClassAttributeForXPathLocator = useClassAttributeForXPathLocator;
      }

      @Override
      public Boolean useIdAttributeForXPathLocator() {

        return Boolean.valueOf(factory.useIdAttributeForXPathLocator);
      }

      @Override
      public Boolean useClassAttributeForXPathLocator() {

        return Boolean.valueOf(factory.useClassAttributeForXPathLocator);
      }

      @Override
      public Boolean fallBackToJSExecutionOnNotInteractableElements() {

        return Boolean.valueOf(factory.fallBackToJSExecutionOnNotInteractableElements);
      }

      @Override
      public void setFallBackToJSExecutionOnNotInteractableElements(final boolean enableJSAsFallBack) {
        factory.fallBackToJSExecutionOnNotInteractableElements = enableJSAsFallBack;

      }
    };
  }
  
	private final Rectangle getDefaultScreenBounds() {
	    final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    final GraphicsDevice gd = ge.getDefaultScreenDevice();
	    return gd.getDefaultConfiguration().getBounds();
	}
  
  private int[] getDefaultBrowserPosition() {
    Rectangle screen = new Rectangle(0, 0, 0, 0);
    try {
      screen = getDefaultScreenBounds();
    } catch (final HeadlessException e) {
      // Ignore ... means there is no screen. Can happen on Hadoop.
    }
    final int x = screen.x + 2;
    final int y = screen.y;
    return new int[] {x,y};
  }

  @Override
  public void setPageLoadingTimeout(final long time, final TimeUnit unit) {
    timeOutPageLoading = Pair.of(Long.valueOf(time), unit);
    driver_firefox.manage().timeouts().pageLoadTimeout(time, unit);
  }

  private static FirefoxProfile getDefaultProfile(
		  String useragent,
		  int scriptTimeoutSec,
		  String downloadDirPath) {
//	final XMLConfiguration configuration = ConfigurationObject.getConfiguration("uk/ac/ox/cs/diadem/webapi/Configuration.xml");
    final FirefoxProfile firefoxProfile = new FirefoxProfile();
    firefoxProfile.setEnableNativeEvents(true);

    // disable addon control dialog
    firefoxProfile.setPreference("extensions.shownSelectionUI", true);
    // geolocation
    // NY city data:application/json,{"location":{"lat":40.711380,"lng":-74.009893},"accuracy":10}

    firefoxProfile.setPreference("browser.sessionstore.postdata", -1);
    // open new window in current window
    // firefoxProfile.setPreference("browser.link.open_newwindow", 1);
    // prevents error on post forms when back action
    // http://support.mozilla.org/en-US/questions/922734?page=1
    firefoxProfile.setPreference("browser.sessionstore.postdata", -1);
    // setting cache
    firefoxProfile.setPreference("browser.cache.disk.enable", false);
    firefoxProfile.setPreference("browser.cache.memory.enable", true);
    firefoxProfile.setPreference("browser.cache.offline.enable", false);
    firefoxProfile.setPreference("network.http.use-cache", true);
    // Disable pdf.js, Firefox's internal PDF viewer
    firefoxProfile.setPreference("pdfjs.disabled", true);
    firefoxProfile.setPreference("plugin.disable_full_page_plugin_for_types", "application/pdf");

    // Check for a new version when the page is out of date
    firefoxProfile.setPreference("browser.cache.check_doc_frequency", 3);
    firefoxProfile.setPreference("plugin.default_plugin_disabled", false);

    // disable plugin related notification
    firefoxProfile.setPreference("plugins.notifyMissingFlash", false);
    firefoxProfile.setPreference("plugins.hide_infobar_for_blocked_plugin", true);
    firefoxProfile.setPreference("plugins.hide_infobar_for_outdated_plugin", true);

    // popus http://superuser.com/questions/697018/how-to-disable-popups-in-firefox-without-add-ons
    firefoxProfile.setPreference("browser.link.open_newwindow.restriction", 0);
    firefoxProfile.setPreference("dom.popup_allowed_events", " ");

    // Disables tooltips on links
    firefoxProfile.setPreference("browser.chrome.toolbar_tips", false);
    firefoxProfile.setPreference("signed.applets.codebase_principal_support", true);
    firefoxProfile.setPreference("capability.policy.default.Window.print", "noAccess");
    firefoxProfile.setPreference("capability.principal.codebase.p0.granted", "UniversalXPConnect");
    firefoxProfile.setPreference("capability.principal.codebase.test.granted", "UniversalXPConnect");
    firefoxProfile.setPreference("capability.principal.codebase.p0.id", "http://diadem.cs.ox.ac.uk/");
    firefoxProfile.setPreference("general.useragent.override", useragent);
    // set_pref("capability.principal.codebase.p0.id", host_base);
    // set_pref("capability.principal.codebase.p0.subjectName", "");

    // Hmm, worth-while thinking about:
    // browser. link. open_newwindow
    // Show an error alert instead of a page: browser. xul. error_pages. enabled

    // javascript.options.showInConsole

    // script timeout, important for errors like "Unresponsive..."
    firefoxProfile.setPreference("dom.max_chrome_script_run_time", scriptTimeoutSec);
    firefoxProfile.setPreference("dom.max_script_run_time", scriptTimeoutSec);
    // Blocks all plugin-initiated popups, even those on whitelisted sites.
    firefoxProfile.setPreference("privacy.popups. disable_from_plugins", 3);
    // browser.sessionstore.postdata

    // dowload automatically there file types
    firefoxProfile.setPreference("browser.download.manager.showAlertOnComplete", false);
    firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
    firefoxProfile.setPreference("browser.download.panel.shown", false);
    firefoxProfile.setPreference("browser.download.folderList", 2);
    firefoxProfile.setPreference("browser.download.manager.showWhenStarting", false);
    firefoxProfile.setPreference("browser.helperApps.alwaysAsk.force", false);

    try {
      final URL resource = Resources.getResource(WebDriverWrapperFactory.class, "mimetypes.txt");
      final List<String> mymeTypes = IOUtils.readLines(resource.openStream(), "UTF-8");
//      final String folder = DiademInstallationSupport.getDiademHome() + File.separatorChar
//          + configuration.getString("download-folder", "download");
      FileUtils.forceMkdir(new File(downloadDirPath));
      firefoxProfile.setPreference("browser.download.dir", downloadDirPath);
      firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk", Joiner.on(",").join(mymeTypes).toString());
    } catch (final Exception e) {
      LOGGER.error("Cannot read file {}. No settings performed for autodownload. Cause may be {} ", e, e.getCause());
    }

    return firefoxProfile;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DOMWindow getContentDOMWindow() {
    return factory.wrapWindow(false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DOMWindow getWindow() {
    return getContentDOMWindow();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setWindowSize(final int width, final int height) {
    driver_firefox.manage().window().setSize(new Dimension(width, height));

    try {
      final File to = new File(platformConfiguration.getBrowserDisplaySizeFileAbsolutePath());
      String dimensionCurrent = null;
      if (to.exists()) {
    	// check if the file on the system is updated
          dimensionCurrent = Files.readFirstLine(to, Charsets.UTF_8);
      }
      final String dimensionToSet = width + "x" + height;
      if (!dimensionToSet.equals(dimensionCurrent)) {
        Files.write(dimensionToSet, to, Charsets.UTF_8);
      }
    } catch (final Exception e) {
      LOGGER.error("Exception writing display size file: {}", e);
    } finally {

    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void navigate(final String uRI) {

    _navigateAndRetry(uRI, true);

  }

  @Override
  public int navigateAndStatus(final URI uri) {
    navigate(uri);
    // TODO implement the status check. Some relevant code is in WebUtils.
    return 200;
  }

  @Override
  public void navigate(final URI uri) {
    if (!uri.isAbsolute())
      throw new WebAPIRuntimeException("Can't navigate to relative URI: " + uri, LOGGER);
    // if (!(uri.getScheme().equals("http") || uri.getScheme().equals("https")))
    // throw new WebAPIRuntimeException("Can't navigate to a non HTTP URI: " + uri, LOGGER);
    this.navigate(uri.toString());
  }
  
  
  /**
   * 
   * Navigate to the target page. Retry once if first load fails (retryOnFailure must be equal to true).
   * 
 * @param targetUrl
 * @param retryOnFailure
 */
private void _navigateAndRetry(final String targetUrl, final boolean retryOnFailure) {

    String startingUrl = null;
    try {
      startingUrl = driver_firefox.getCurrentUrl();
  if (LOGGER.isDebugEnabled()) LOGGER.debug("Browser {} : Current location is about to be changed to {}", driver_firefox, targetUrl);
      driver_firefox.get(targetUrl);
      pages++;
  if (LOGGER.isDebugEnabled()) LOGGER.debug("Browser {} : Location has been changed to {}", driver_firefox, targetUrl);
      // return window for the loaded page
      factory.wrapWindow(true);
    } catch (final org.openqa.selenium.TimeoutException e) {

      LOGGER.error("Loading page <{}> exceded the timeout of {} seconds", targetUrl, timeOutPageLoading.toString());

      throw new WebAPITimeoutException("Loading page <" + targetUrl + "> exceded the timeout of "
          + timeOutPageLoading.toString() + " seconds", LOGGER);

    } catch (final org.openqa.selenium.UnhandledAlertException e) {
      LOGGER.warn("dismissing alert <{}> ", e.getMessage());

      if (didNavigateSuccess(startingUrl, driver_firefox.getCurrentUrl(), targetUrl)) {// this is an hack because
        // firefox 24 cannot set a custom
        // geolocation to prevent
        // modal dialog 'NO LOCATION'
        LOGGER.warn(
            "It seems that navigation to {} was not blocked by the modal dialog, therefore won't retry the action",
            targetUrl);
        return;
      }
      if (retryOnFailure) {
        LOGGER.warn("reperform navigation to {}", targetUrl, e.getMessage());
        _navigateAndRetry(targetUrl, false);
      } else
        throw new WebAPIPageNavigationRuntimeException("Cannot navigate to <" + targetUrl
            + "> due to unhandled alert dialog.", e, LOGGER);
    } catch (final WebDriverException e) {// can happen a bug of selenuim
      // https://code.google.com/p/selenium/issues/detail?id=3544
      LOGGER.error("Unexpected error navigating to <{}>, aborting Error <{}>", targetUrl, e.getMessage());
      throw new WebAPIPageNavigationRuntimeException("Cannot navigate to <" + targetUrl + "> due to error: "
          + e.getMessage(), e, LOGGER);
    }
  }

  /**
   *
   * Check whether we are on the target web page.
   * 
 * @param startingUrl
 * @param currentURL
 * @param targetUrl
 * @return true, if targetUrl==currentURL or targetUrl.getHost()==currentURL.getHost()
 */
private boolean didNavigateSuccess(final String startingUrl, final String currentURL, final String targetUrl) {
    try {
      if (targetUrl.equals(currentURL))
        return true;
      final URI currentURI = new URI(currentURL);
      final URI targetURI = new URI(targetUrl);
      // same host
      if (currentURI.getHost().equals(targetURI.getHost())) {// TODO replace with better similarity
        if (startingUrl == null)
          return false;
        if (startingUrl.equals(currentURL))
          return false;
        // here starting != current but current has same domain of target so we have successfully navigated
        return true;
      }
      return false;
    } catch (final URISyntaxException e) {
      // should not happen but
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void navigate(final String uRI, final boolean waitUntilLoaded) {
    if (waitUntilLoaded) {
      navigate(uRI);
    } else {
      unsupported("Not Blocking Navigation to URL");
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getLocationURL() {
    return _getLocationURL(true);

  }

  private String _getLocationURL(final boolean retryOnFailure) {
    try {
      return driver_firefox.getCurrentUrl().toString();
    } catch (final org.openqa.selenium.UnhandledAlertException e) {
      LOGGER.warn("dismissing alert and reperform getLocationURL");
      if (retryOnFailure)
        return _getLocationURL(false);
      else
        throw new WebAPIRuntimeException("Cannot getLocationURL, due to unhandled alert dialog", e, LOGGER);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
  if (LOGGER.isDebugEnabled()) LOGGER.debug("closing the browser {}", driver_firefox);
    driver_firefox.close();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void back(final boolean waitUntilLoaded) {
    back();
  }

  /*
   * (non-Javadoc)
   *
   * @see uk.ac.ox.cs.diadem.webapi.WebBrowser#back()
   */
  @Override
  public void back() {
    _back(true);

  }

  private void _back(final boolean retryOnFailure) {
    try {
if (LOGGER.isDebugEnabled()) LOGGER.debug("Back: Current location is about to be changed back in the history");
      driver_firefox.navigate().back();
    } catch (final org.openqa.selenium.UnhandledAlertException e) {
      LOGGER.warn("dismissing alert and reperform BACK", e);
      if (retryOnFailure) {
        _back(false);
      } else
        throw new WebAPIRuntimeException("Cannot navigate back, due to unhandled alert dialog", e, LOGGER);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void forward(final boolean waitUntilLoaded) {
  if (LOGGER.isDebugEnabled()) LOGGER.debug("Forward: Current location is about to be changed forward in the history");
    driver_firefox.navigate().forward();

  }

//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  public void enableFeatures(final FeatureType... features) {
//  if (LOGGER.isDebugEnabled()) LOGGER.debug("Not implemented {}, ignore({})", "enableFeatures", features);
//
//    // unsupported("enableFeatures");
//
//  }

//  /**
//   * {@inheritDoc}
//   */
//  @Override
//  public void disableFeatures(final FeatureType... features) {
//    // http://stackoverflow.com/questions/3526361/firefoxdriver-how-to-disable-javascript-css-and-make-sendkeys-type-instantly
//    // http://stackoverflow.com/questions/7157994/do-not-want-images-to-load-and-css-to-render-on-firefox
//  if (LOGGER.isDebugEnabled()) LOGGER.debug("Not implemented {}({}), ignore", "disableFeatures", features);
//    // unsupported("disableFeatures");
//
//  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DOMXPathEvaluator getXPathEvaluator() {
    return factory.new DOMXPathEvaluatorOnWebDriverImpl();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addProgressListener(final BrowserProgressListener listener) {
    unsupported("addProgressListener");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeProgressListener(final BrowserProgressListener listener) {
    unsupported("removeProgressListener");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addLocationListener(final BrowserLocationListener listener) {
    unsupported("addLocationListener");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeLocationListener(final BrowserLocationListener listener) {
    unsupported("removeLocationListener");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addTitleListener(final BrowserTitleListener listener) {
    unsupported("addTitleListener");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeTitleListener(final BrowserTitleListener listener) {
    unsupported("removeTitleListener");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addStatusTextListener(final BrowserStatusTextListener listener) {
    unsupported("addStatusTextListener");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeStatusTextListener(final BrowserStatusTextListener listener) {
    unsupported("removeStatusTextListener");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getWindowFrame() {
    return driver_firefox;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void refresh() {
  if (LOGGER.isDebugEnabled()) LOGGER.debug("Refresh: Current location is about to be refreshed");
    driver_firefox.navigate().refresh();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    // work around for webdriver but. We close and create a new browser
    driver_firefox.quit();
    try {
		instantiateBrowser(platformConfiguration, runOptionsConfiguration, javaScriptConfiguration);
	} catch (ConfigurationException e) {
		e.printStackTrace();
	}
    driver_firefox.get("about:blank");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isBackEnabled() {
  if (LOGGER.isDebugEnabled()) LOGGER.debug("IsBackEnable not supported, return true");
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isForwardEnabled() {
  if (LOGGER.isDebugEnabled()) LOGGER.debug("isForwardEnabled not supported, return true");
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object evaluate(final String script) {
    return factory.js(driver_firefox, true, script);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDialogsService(final DialogsService service) {
    unsupported("setDialogsService");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setZoom(final int zoomRatio) {
    unsupported("setZoom");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enableSilentPromptService(final boolean b) {
    LOGGER.error("not yet implemented '{}' for class '{}'", "enableSilentPromptService", this.getClass());

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addOpenNewWindowListener(final OpenNewWindowListener openNewWindowListener) {
    unsupported("addOpenNewWindowListener");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeOpenNewWindowListener(final OpenNewWindowListener listener) {
    unsupported("removeOpenNewWindowListener");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shutdown() {
  if (LOGGER.isDebugEnabled()) LOGGER.debug("Quit: shut down allElements windows");
    if (driver_firefox.toString() == null) {// TODO are we sure is correct?
      LOGGER.error("Browser is already shutdown ... Can't quit again!");
      return;
    }

    driver_firefox.close();

    try {
      final Alert alert = driver_firefox.switchTo().alert();
      LOGGER.error("Alert is preventing shutdown(). Try to dismiss ther alert");
      alert.dismiss();
    } catch (final NoAlertPresentException e) {

    } catch (final UnreachableBrowserException e) {
      // already closed
    } finally {

      try {
        factory.sleep(TimeUnit.SECONDS, 1);
        driver_firefox.quit();
      } catch (final Throwable e) {
        // it's ok, it was already closed
        // System.out.println(e);
      }

    }

  }

  private static void unsupported(final String msg) {
    throw new WebAPIRuntimeException("Not yet implemented " + msg, LOGGER);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void cleanCache() {
    unsupported("cleanCache");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeAllCookies() {
  if (LOGGER.isDebugEnabled()) LOGGER.debug("Removing Cookies");
    driver_firefox.manage().deleteAllCookies();
  }

  @Override
  public boolean executeJavaScript(final String script) {
    final Object object = driver_firefox.executeScript(script);
    return object != null;
  }

  @Override
  public void saveDocument(final String name) {
    unsupported("saveDocument");
  }

  @Override
  public Engine getEngine() {
    return Engine.WEBDRIVER_FF;
  }

  @Override
  protected FirefoxDriver getBrowser() {
    return driver_firefox;
  }

  @Override
  public JSUtils js() {
    return new JSUtils() {

      @Override
      public List<String> getImageSources() {
        return factory.callJS("getImageSources");
      }

      @Override
      public List<String> getIDAttributes() {
        return factory.callJS("getIDAttributes");
      }

      @Override
      public List<String> getLinkHRefs() {
        return factory.callJS("getLinkHRefs");
      }

      @Override
      public List<String> getClassAttributes() {
        return factory.callJS("getClassAttributes");
      }

      @Override
      public MutationFormObserver observeFormMutation(final DOMElement rootNode) {

        return factory.observeFormMutation(rootNode);
      }

      @Override
      public List<String> testScratch() {
        return factory.callJS("testXPath");
      }

      @Override
      public String makeURLAbsolute(final String url) {
        return factory.callJS("makeURLAbsolute", url);
      }

      @Override
      public String selectText(final DOMNode node) {

        return factory.callJS("selectText", factory.toJS(node));
      }

      /*
       * (non-Javadoc)
       *
       * @see uk.ac.ox.cs.diadem.webapi.utils.JSUtils#asXLM(uk.ac.ox.cs.diadem.webapi.dom.DOMNode)
       */
      @Override
      public String asXLM(final DOMNode subtree) {

        return factory.callJS("asXML", factory.toJS(subtree));
      }

      @Override
      public String selectText(final DOMNode startRange, final DOMNode endNode) {

        // return factory.callJS("makeURLAbsolute", "xx");
        return factory.callJS("selectText", factory.toJS(startRange), factory.toJS(endNode));
      }
    };
  }

  @Override
  public void setWindowPosition(final int x, final int y) {
    driver_firefox.manage().window().setPosition(new Point(x, y));
  }

  @Override
  public File takeScreenshot() {
    return ((TakesScreenshot) driver_firefox).getScreenshotAs(OutputType.FILE);
  }

  void recordStats(final String functionName) {
    if (!collectStats)
      return;
    final Integer c = funnctionCalls.get(functionName);
    if (c == null) {
      funnctionCalls.put(functionName, 1);
    } else {
      funnctionCalls.put(functionName, (c + 1));
    }

  }

  @Override
  public DOMWindow switchToDefaultContent() {
    driver_firefox.switchTo().defaultContent();
    return factory.wrapWindow(true);
  }

  @Override
  public DOMWindow switchToFrame(final DOMElement frameElement) {
    final WebElement frame = factory.castToElementAndGetWrappedNode(frameElement);
    factory.checkIfFrame("switchToFrame", frame);
    driver_firefox.switchTo().frame(frame);
    return factory.wrapWindow(true);
  }

  /* (non-Javadoc)
   * @see uk.ac.ox.cs.diadem.webapi.MiniBrowser#getAdviceExecutor()
   */
  @Override
  public AdviceProcessor getAdviceProcessor() {
	  LOGGER.warn("getAdviceProcessor() not available for WebDriver Browser");
	  return new WebAdviceProcessor();
  }
}
