/*
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science,
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */

package uk.ac.ox.cs.diadem.webapi.pagestate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;

/**
 * Hash-based page state: For each node in a DOM, we generate three hashes
 * <ul>
 * <li><em>Structural:</em>One from the String obtained by concatenating {@link DOMNode#getNodeName()}
 * {@link DOMNode#getTextContent()}.
 * <li><em>Attributes:</em>One from the String of the attribute key/values sorted alphabetical by key.
 * <li><em>Style:</em>One from the String of the (major) CSS attribute key/values, again sorted alphabetical by key.
 * </ul>
 * NOT IMPLEMENTED
 */
class StringHashPageState implements PageState {
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(StringHashPageState.class);
  /*@SuppressWarnings("unused")
  private static final Configuration config = ConfigurationFacility.getConfiguration();*/

  /**
   *
   */
  public StringHashPageState() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public boolean identicalTo(final PageState other) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean similarTo(final PageState other) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public List<PageDifference> getDifferences(final PageState other) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getAtomicEditDistance(final PageState other) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getEditDistance(final PageState other) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean isDifferentPage(final PageState other) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean atSameLocation(final PageState other) {
    return false;
  }
}
