/*
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science,
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */

package uk.ac.ox.cs.diadem.webapi.pagestate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.pagestate.PageDifference.DifferenceKind;
import uk.ac.ox.cs.diadem.webapi.utils.diff_match_patch;
import uk.ac.ox.cs.diadem.webapi.utils.diff_match_patch.Diff;

/**
 * Simple hash-based page state, only supporting {@link DifferenceKind#newPage}, {@link DifferenceKind#newWindow}, and
 * {@link DifferenceKind#textChange} on the document element.
 * 
 * Next step could be to use visible text content.
 */
class SimpleStringHashPageState implements PageState {
  private static final Logger logger = LoggerFactory.getLogger(SimpleStringHashPageState.class);
  /*@SuppressWarnings("unused")
  private static final Configuration config = ConfigurationFacility.getConfiguration();*/

  private final String locationUrl;
  final int contentHash;
  private final String content;
  private final Map<PageState, List<PageDifference>> differences;
  private final WebBrowser browser;
  private final Function<Diff, PageDifference> converter;
  private final int windowHash;

  public SimpleStringHashPageState(final WebBrowser browser) {
    this.browser = browser;
    locationUrl = browser.getLocationURL();
    logger.trace("Creating state for url <{}>", locationUrl);
    if (locationUrl == null) {
      windowHash = -1;
      content = "";
    } else {
      windowHash = browser.getContentDOMWindow().hashCode();
      content = browser.getContentDOMWindow().getDocument().getDocumentElement().getTextContent();
    }
    contentHash = content.hashCode();
    converter = new DiffToDifferenceConverter();
    logger.trace("New SimpleStringHashPageState for '{}' with {}", locationUrl, "window='" + windowHash
        + "', content='" + contentHash + "'");
    differences = new HashMap<PageState, List<PageDifference>>();
  }

  @Override
  public boolean identicalTo(final PageState other) {
    if (!(other instanceof SimpleStringHashPageState))
      return false;
    final SimpleStringHashPageState o = (SimpleStringHashPageState) other;
    if (o.contentHash == contentHash)
      return true;
    return false;
  }

  @Override
  public boolean similarTo(final PageState other) {
    if (identicalTo(other))
      return true;
    if (!(other instanceof SimpleStringHashPageState))
      return false;
    if (getAtomicEditDistance(other) > SIMILARITY_THRESHOLD)
      return false;
    return true;
  }

  @Override
  public int getAtomicEditDistance(final PageState other) {
    if (identicalTo(other))
      return 0;
    if (!(other instanceof SimpleStringHashPageState))
      return Integer.MAX_VALUE;
    final List<PageDifference> diffs = getDifferences(other);
    // bug not fixed in lambdaj
    // see http://code.google.com/p/lambdaj/issues/detail?id=22
    // final int sum = Lambda.sum(diffs, Lambda.on(PageDifference.class).getValue().length());
    int sum = 0;
    for (final PageDifference pageDifference : diffs) {
      sum += pageDifference.getValue().length();
    }
    return sum;
  }

  @Override
  public int getEditDistance(final PageState other) {
    if (identicalTo(other))
      return 0;
    if (!(other instanceof SimpleStringHashPageState))
      return Integer.MAX_VALUE;
    return getDifferences(other).size();
  }

  @Override
  public List<PageDifference> getDifferences(final PageState other) {
    List<PageDifference> differsIn = new LinkedList<PageDifference>();
    if (other == null)
      return differsIn;
    if (!differences.containsKey(other)) {
      if (!(other instanceof SimpleStringHashPageState))
        throw new IllegalArgumentException("Cannot compare page states of different types. ");
      final SimpleStringHashPageState o = (SimpleStringHashPageState) other;
      if (windowHash != o.windowHash) {
        differsIn.add(new PageDifference(browser.getContentDOMWindow().getDocument(), DifferenceKind.newWindow,
            o.locationUrl, locationUrl));
      }
      if (!locationUrl.equals(o.locationUrl)) {
        differsIn.add(new PageDifference(browser.getContentDOMWindow().getDocument(), DifferenceKind.newPage,
            o.locationUrl, locationUrl));
      }
      if (differsIn.size() == 0) {
        final diff_match_patch diff = new diff_match_patch();

        final LinkedList<Diff> diffs = diff.diff_main(content, o.content);
        diff.diff_cleanupSemantic(diffs);

        differsIn = Lists.newArrayList(
        		Iterables.filter(Collections2.transform(diffs, converter), Predicates.notNull()));
      }
      differences.put(other, differsIn);
    }
    return differences.get(other);
  }

  @Override
  public boolean isDifferentPage(final PageState other) {
    if (other == null)
      return true;
    if (!(other instanceof SimpleStringHashPageState))
      throw new IllegalArgumentException("Cannot compare page states of different types. ");
    final SimpleStringHashPageState o = (SimpleStringHashPageState) other;
    if (!locationUrl.equals(o.locationUrl) || (contentHash != o.contentHash))
      return true;
    return false;
  }

  @Override
  public boolean atSameLocation(final PageState other) {
    if (other == null)
      return false;
    if (!(other instanceof SimpleStringHashPageState))
      throw new IllegalArgumentException("Cannot compare page states of different types. ");
    final SimpleStringHashPageState o = (SimpleStringHashPageState) other;
    if (locationUrl.equals(o.locationUrl))
      return true;
    return false;
  }

  class DiffToDifferenceConverter implements Function<Diff, PageDifference> {
    @Override
    public PageDifference apply(final Diff diff) {
      final DOMNode node = browser.getContentDOMWindow().getDocument();
      PageDifference diffNew = null;
      switch (diff.operation) {
      case DELETE:
        diffNew = new PageDifference(node, PageDifference.DifferenceKind.deletedText, diff.text, "");
        break;
      case INSERT:
        diffNew = new PageDifference(node, PageDifference.DifferenceKind.insertedText, "", diff.text);
        break;
      case EQUAL:
        // Just skip "equal" nodes.
        break;
      }
      return diffNew;
    }
  }
}
