/*
 * Copyright (c)2011, DIADEM Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the DIADEM team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DIADEM Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.ox.cs.diadem.webapi;

import java.awt.Composite;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder.Engine;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;
import uk.ac.ox.cs.diadem.webapi.listener.BrowserLocationListener;
import uk.ac.ox.cs.diadem.webapi.listener.BrowserProgressListener;
import uk.ac.ox.cs.diadem.webapi.listener.BrowserStatusTextListener;
import uk.ac.ox.cs.diadem.webapi.listener.BrowserTitleListener;
import uk.ac.ox.cs.diadem.webapi.listener.OpenNewWindowListener;
import uk.ac.ox.cs.diadem.webapi.utils.BrowserStats;
import uk.ac.ox.cs.diadem.webapi.utils.JSUtils;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public interface WebBrowser extends AutoCloseable, MiniBrowser {

  enum ContentType {

    // FONT,
    IMAGE,
    /**
     * Indicates a video or audio
     */
    // MEDIA,
    /**
     * Indicates a generic object (plugin-handled content typically falls under this category)
     */
    OBJECT,
    /**
     * Indicates an executable script (such as JavaScript).
     */
    SCRIPT,
    /**
     * Indicates a stylesheet
     */
    STYLESHEET,
    /**
     * indicates a document contained within another document (for example, IFRAMEs, FRAMES, and OBJECTs)
     */
    SUBDOCUMENT,

    // XMLHTTPREQUEST
  }

//  //@formatter:off
//  enum FeatureType {
//
//    /**
//     * disables execution of javascript
//     * @deprecated use WebBrowserBuilder to configure javascript
//     */
//    JAVASCRIPT,
//    DOWNLOAD_IMAGES,
//    /**
//     * Execution of plugins
//     */
//    PLUGINS,
//    /**
//     * Loads and activates Firebug firefox extension
//     */
//    FIREBUG,
//    /**
//     * Loads but does not activate Firebug for firefox
//     */
//    FIREBUG_HIDDEN,
//    /**
//     * Load and activates Firepath for firefox
//     */
//    FIREPATH(FIREBUG_HIDDEN),
//    /**
//     * Load and activates WebDeveloper for firefox
//     */
//    WEB_DEVELOPER,
//    /**
//     * Load and activates JS_DEMINIFIER for firefox
//     */
//    JS_DEMINIFIER(FIREBUG_HIDDEN),
//    /**
//     * Load and EVENTBUG WebDeveloper for firefox
//     */
//    EVENTBUG(FIREBUG_HIDDEN),
//    /**
//     * Load and activates IFRAME_HIGHLIGHT
//     */
//    IFRAME_HIGHLIGHT,
//    /**
//     * Load and activates ADBLOCK for firefox
//     */
//    ADBLOCK,
//    /**
//     * Load and activates FACEBOOK_BLOCK for firefox
//     */
//    FACEBOOK_BLOCK;
//    //@formatter:on
//    FeatureType(final FeatureType... requireds) {
//      this.requireds = Sets.newHashSet(requireds);
//    }
//
//    private final Set<FeatureType> requireds;
//
//    public Set<FeatureType> getRequirements() {
//      return requireds;
//    }
//  }

  Engine getEngine();

  BrowserStats stats();

  /**
   * Return the top-level dom window associated to this browser
   *
   * @return the top-level dom window associated to this browser
   */
  DOMWindow getContentDOMWindow();

  /**
   * Return the top-level dom window associated to this browser. It is a synonym of {@link #getContentDOMWindow()}
   *
   * @return the top-level dom window associated to this browser
   */
  DOMWindow getWindow();

  /**
   * Sets the dimension of the browser window, overriding the default values.
 * @param width
 * @param height
 */
void setWindowSize(int width, int height);

  // /**
  // * Returns a {@link DOMWalkerBrowserBacked} object using the default configuration
  // *
  // * @param serializer
  // * @return a {@link DOMWalkerBrowserBacked} object using the default configuration
  // */
  // DOMWalkerBrowserBacked getDOMWalker(Object serializer);
  //
  // /**
  // * Returns a {@link DOMWalker} object
  // *
  // * @returna {@link DOMWalker} object
  // */
  // DOMWalker getDOMWalker();
  /**
   * Uses {@link #navigate(URI)} to navigates on the page and returns the HTTP status code (which might require another
   * page fetching in case of BrowserFactory.Engine.WEBDRIVER_FF)
   *
   * @param uri
   * @return
   */
  int navigateAndStatus(URI uri);

  /**
   * Navigate to the given URL.
   *
   * @param waitUntilLoaded
   *          if true, it runs synchronously, waiting for the fully loaded page. This is always the case for the browser
   *          {@link Engine#WEBDRIVER_FF}
   * @param uRI
   * @throws a
   *           WebAPITimeoutException if the set timeout is exceeded
   * @deprecated replaced by {@link #navigate(String, boolean)()}
   */
  @Deprecated
  void navigate(String uRI, boolean waitUntilLoaded);

  /**
   * Navigate to the given URI.
   *
   * @param waitUntilLoaded
   *          if true, it runs synchronously, waiting for the fully loaded page. This is always the case for the browser
   *          {@link Engine#WEBDRIVER_FF}
   * @param uRI
   * @throws a
   *           WebAPITimeoutException if the set timeout is exceeded
   */
  @Deprecated
  void navigate(URI uri, boolean waitUntilLoaded);

  /**
   * Set the timeout for page loading. If set, methods like {{@link #navigate(String, boolean)} will throw exceptions.
   *
   * @param time
   * @param unit
   */
  void setPageLoadingTimeout(long time, TimeUnit unit);

  /**
   * Closes the browser. it has the same effect of closing the {@link DOMWindow} calling this
   * {@link #getContentDOMWindow().close()}
   */
  @Override
  void close();

  /**
   * Forces back button on the browser
   *
   * @param waitUntilLoaded
   *          if true, it runs synchronously, waiting for the fully loaded page
   * @Deprecated use {@link #back()} instead
   */
  void back(boolean waitUntilLoaded);

  /**
   * Forces forward button on the browser
   *
   * @param waitUntilLoaded
   *          if true, it runs synchronously, waiting for the fully loaded page
   */
  void forward(boolean waitUntilLoaded);

//  /**
//   * @param features
//   * @deprecated does nothing!
//   */
//  @Deprecated
//  void enableFeatures(FeatureType... features);

//  /**
//   * @param features
//   * @deprecated does nothing!
//   */
//  @Deprecated
//  void disableFeatures(FeatureType... features);

  /**
   * @return an DOMXPathEvaluator for this browser
   */
  DOMXPathEvaluator getXPathEvaluator();

  void addProgressListener(BrowserProgressListener listener);

  void removeProgressListener(BrowserProgressListener listener);

  void addLocationListener(BrowserLocationListener listener);

  void removeLocationListener(BrowserLocationListener listener);

  void addTitleListener(BrowserTitleListener listener);

  void removeTitleListener(BrowserTitleListener listener);

  void addStatusTextListener(BrowserStatusTextListener listener);

  void removeStatusTextListener(BrowserStatusTextListener listener);

  /**
   * INTERNAL API SUBJECT TO CHANGE Return the Window frame used by the underlying browser instantiated. For
   * {@link Engine#SWT_MOZILLA} it returns a {@link Composite} while for {@link Engine#HTMLUNIT} it returns the
   * {@link com.gargoylesoftware.htmlunit.WebClient}, and for WebDriver returns the underlying driver
   *
   * @return
   */
  Object getWindowFrame();

  void refresh();

  void stop();

  boolean isBackEnabled();

  boolean isForwardEnabled();

  /**
   *
   Returns the result, if any, of executing the specified java script.
   *
   * @param script
   */
  Object evaluate(String script);

  /**
   * Sets a new DialogsService. You can use this method to register your own {@link DialogsService} implementation in
   * which you can specify the required behavior during displaying any browser dialog such as alert, confirmation,
   * JavaScript error dialog.
   *
   * @param service
   *          the service to set
   */
  void setDialogsService(DialogsService service);

  /**
   * Set the zoom ratio, currenlty works only on Mozilla
   *
   * @param zoomRatio
   */
  public void setZoom(int zoomRatio);

  /**
   * if enabled, blocks allElements javascript alerts and popups. NOTE: cannot be reverted once enabled on one browser
   * instance
   *
   * @param b
   *          true to enable
   */
  void enableSilentPromptService(boolean b);

  /**
   * @param openNewWindowListener
   */
  void addOpenNewWindowListener(OpenNewWindowListener openNewWindowListener);

  void removeOpenNewWindowListener(OpenNewWindowListener listener);

  /**
   * Kills the current browser and allElements other instances, closing the application. Use this{@link #close()} to
   * close only the current browser instance.
   */
  @Override
  void shutdown();

  /**
   * Removes allElements cache entries
   */
  void cleanCache();

  /**
   * Removes allElements cookies
   */
  void removeAllCookies();

  boolean executeJavaScript(String script);

  /**
   * For debug only, not rely on it as it can be removed from here.
   *
   * @param name
   */
  void saveDocument(String name);

  /**
   * Returns the same as {@link #getLocationURL()} but as a {@link URL}. Returns {@code null} if the URL is not valid.
   */
  @Override
  URL getURL();

  JSUtils js();

  /**
   * Sets the position of the browser window.
   */
  void setWindowPosition(int i, int j);

  /**
   * Returns a file (created in the temporary file location as indicated by the browser) with a screenshot of the
   * current browser window.
   */
  File takeScreenshot();

  /**
   * General option configuration
   *
   * @return
   */
  Options manageOptions();

  interface Options {

    /**
     *
     * @return
     */
    Boolean fallBackToJSExecutionOnNotInteractableElements();

    /**
     *
     * Default value is true, that is will try to perform javascript actions when the element is not visible/enabled
     *
     * @param enableJSAsFallBack
     */
    void setFallBackToJSExecutionOnNotInteractableElements(boolean enableJSAsFallBack);

    /**
     * This should be used only when the browser is used for OXPath.
     *
     * @param enabled
     */
    void enableOXPathOptimization(boolean enabled);

    /**
     * Enables/Disables usage of id and class attributes in computing xpath locators for nodes
     *
     * @param useIdAttributeForXPathLocator
     * @param useClassAttributeForXPathLocator
     */
    void configureXPathLocatorHeuristics(boolean useIdAttributeForXPathLocator, boolean useClassAttributeForXPathLocator);

    Boolean useIdAttributeForXPathLocator();

    Boolean useClassAttributeForXPathLocator();

  }

  /**
   * http://selenium.googlecode.com/svn/trunk/docs/api/java/org/openqa/selenium/WebDriver.TargetLocator.html Selects
   * either the first frame on the page, or the main document when a page contains iframes.
   */
  DOMWindow switchToDefaultContent();

  /**
   * Select a frame using its previously located elements
   *
   * @param frameElement
   * @return the
   *
   * @throws WebAPIRuntimeException
   *           If the given element is neither an IFRAME nor a FRAME element.
   */
  DOMWindow switchToFrame(DOMElement frameElement);

}
