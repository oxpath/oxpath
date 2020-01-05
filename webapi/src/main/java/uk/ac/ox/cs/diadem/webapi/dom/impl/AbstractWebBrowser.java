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
package uk.ac.ox.cs.diadem.webapi.dom.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument.CRITERIA;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPINoSuchElementException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;
import uk.ac.ox.cs.diadem.webapi.utils.BrowserStats;
import uk.ac.ox.cs.diadem.webapi.utils.JSUtils;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 * @param <N> Original web browser enclosed (wrapped) by this class.
 */
public abstract class AbstractWebBrowser<N> implements WebBrowser {

  // private static final String DEFAULT_DIADEM_CONFIGURATION =
  // "diadem-configuration.xml";
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebBrowser.class);

  /**
 * @return Original web browser enclosed (wrapped) by this class.
 */
protected abstract N getBrowser();

  /**
 * TODO describe
 */
protected boolean locationEmpty = true;

  /**
 * For the statistics. Number of pages visited with the use of this browser instance.
 */
int pages = 0;

  
/**
 * True, if statistics is to be collected.
 */
boolean collectStats;
  Map<String, Integer> funnctionCalls = Maps.newTreeMap();

  // Stopwatch stopwatch = Stopwatch.createUnstarted();

  @Override
  public URL getURL() {
    final String url = getLocationURL();
    try {
      return new URL(url);
    } catch (final MalformedURLException e) {
      return null;
      // throw new WebAPIRuntimeException("Can't get current location as URL: " + url, LOGGER);
    }
  }

  @Override
  public BrowserStats stats() {
    return new BrowserStats() {

      @Override
      public int visitedPages() {
        return pages;
      }

      @Override
      public void incrementPageNumbers() {
        pages++;
      }

      @Override
      public SortedMap<String, Integer> jsCalls() {
        funnctionCalls.put("jsTotalTime", -1);// (int) stopwatch.elapsed(TimeUnit.SECONDS));
        return ImmutableSortedMap.copyOf(funnctionCalls, Ordering.natural()
            .onResultOf(Functions.forMap(funnctionCalls)).compound(Ordering.natural()));
      }
    };
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.MiniBrowser#getDOMElementByLocator(java.lang.String)
   */
  @Override
  public DOMElement getDOMElementByLocator(final String xPathLocator) throws WebAPINoSuchElementException {

    try {
      final DOMElement element = getContentDOMWindow().getDocument().selectElementBy(CRITERIA.xpath, xPathLocator);
      if (element == null)
        throw new WebAPINoSuchElementException("Cannot find an element for the xpath locator: " + xPathLocator, LOGGER);
      return element;
    } catch (final Exception e) {
      throw new WebAPINoSuchElementException("Cannot find an element for the xpath locator: " + xPathLocator, e, LOGGER);
    }
  }

  @Override
  public void setPageLoadingTimeout(final long time, final TimeUnit unit) {
    LOGGER.error("Unsupported setPageLoadingTimeout for this browser");
    throw new WebAPIRuntimeException("setPageLoadingTimeout not supported by this browser", LOGGER);
  }

  @Override
  public File takeScreenshot() {
    LOGGER.error("Unsupported Screenshots for this browser");
    throw new WebAPIRuntimeException("Screenshots are not supported by this browser", LOGGER);
  }

  @Override
  public JSUtils js() {
    LOGGER.error("Unsupported JSWrapper for this browser");
    throw new WebAPIRuntimeException("cannot instantiate a JSWrapper for this browser", LOGGER);
  }

  @Override
  public void navigate(final URI uri, final boolean waitUntilLoaded) {
    if (!uri.isAbsolute())
      throw new WebAPIRuntimeException("Can't navigate to relative URI: " + uri, LOGGER);
    if (!(uri.getScheme().equals("http") || uri.getScheme().equals("https")))
      throw new WebAPIRuntimeException("Can't navigate to a non HTTP URI: " + uri, LOGGER);
    this.navigate(uri.toString());
  }

  // protected DOMWalkerBrowserBacked instantiateWalker(final Configuration config, final FactSerializer serializer,
  // final boolean treatNamespaces) {shouldBeFresh
  //
  // checkBrowser();
  // return DOMWalkerBrowserBackedImpl.createDOMWalker(this.getContentDOMWindow().getDocument().getDocumentElement(),
  // serializer, treatNamespaces);
  // }

  // @Override
  // public DOMWalker getDOMWalker() {
  // checkBrowser();
  // return DOMWalkerImpl.createDOMWalker(this.getContentDOMWindow().getDocument().getDocumentElement());
  // }
  //
  // @Override
  // public DOMWalkerBrowserBacked getDOMWalker(final FactSerializer serializer) {
  //
  // checkBrowser();
  // return DOMWalkerBrowserBackedImpl.createDOMWalker(this.getContentDOMWindow().getDocument().getDocumentElement(),
  // serializer, false);
  //
  // // } catch (final ConfigurationException e) {
  // // throw new
  // // RuntimeException("Cannot initialize the walker configuartion", e);
  // // }
  // }
  //
  // @Override
  // public DOMWalkerBrowserBacked getDOMWalker(final Configuration config, final FactSerializer serializer,
  // final boolean treatNamespaces) {
  //
  // return instantiateWalker(config, serializer, treatNamespaces);shouldBeFresh
  // }

  //TODO check how it is used
@Override
  public void enableSilentPromptService(final boolean b) {

    // no op
  }

  //TODO rename to getWindow()
@Override
  public DOMWindow getWindow() {

    return getContentDOMWindow();
  }

  @Override
  public void close() {
    shutdown();

  }

}
