package uk.ac.ox.cs.diadem.webapi.pagestate;

/*
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science,
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationFormObserver;

/**
 * A page state represents a recording of the state of the browser including URL, size, etc. PageStates can be compared
 * to determine modifications due to actions.
 */
public class SimplePageStateRecorder {

  static final Logger logger = LoggerFactory.getLogger(SimplePageStateRecorder.class);

  private final WebBrowser browser;
  private SimplePageState last;
  private SimplePageState current;

  public SimplePageStateRecorder(final WebBrowser browser) {
    this.browser = browser;
  }

  public void recordPageState() {
    last = current;
    MutationFormObserver installed = null;
    if (last != null) {
      installed = last.getMutationObserver();
    }

    current = new QuantitativePageState(browser, installed);
  }

  public boolean atSameLocation() {
    return current.atSameLocation(last);
  }

  public boolean hasPageChanged() {
    if ((last == null) || (current == null))
      return false;
    return !current.identicalTo(last);
  }

  public boolean isPageSimilar() {
    if ((last == null) || (current == null))
      return false;
    return current.similarTo(last);
  }

  public boolean currentStateIsAtLocation(final String locationURL) {

    return normalizeURL(current.getLocationURLOfCurrentPage()).equals(normalizeURL(locationURL));
  }

  public static String normalizeURL(final String locationURL) {
    // TODO here a full url normalization should be applied, but hey,...
    if (locationURL.endsWith("#"))
      return StringUtils.chop(locationURL);
    return locationURL;
  }

  /**
   * @return
   */
  public MajorChangeType whyIsNotsimilar() {
    return current.differenceTypeIfAny(last);
  }
}