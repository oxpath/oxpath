package uk.ac.ox.cs.diadem.webapi.pagestate;

/*
 * COPYRIGHT (C) 2010-2015 DIADEM Team, Department of Computer Science, Oxford University. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of the DIADEM project ("DIADEM"), Department of Computer Science,
 * Oxford University ("Confidential Information").  You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement you entered into with DIADEM.
 */

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;

/**
 * A page state represents a recording of the state of the browser including URL, size, etc. PageStates can be compared
 * to determine modifications due to actions.
 */
public class PageStateRecorder {
  static final Logger logger = LoggerFactory.getLogger(PageStateRecorder.class);
  /*static final Configuration config = ConfigurationFacility.getConfiguration();*/

  private final WebBrowser browser;
  private PageState last;
  private PageState current;

  public PageStateRecorder(final WebBrowser browser) {
    this.browser = browser;
  }

  public void recordPageState() {
    last = current;
    current = new SimpleStringHashPageState(browser);
  }

  // * siteTree(Site)::page(Site, Page, Url).
  // NOT DONE RIGHT NOW * siteTree1__follows(SId, PIdAfter, PIdBefore).
  // * siteTree1__child(SId, PIdChild, PIdParent, AId).
  // * siteTree1__modificationOf(SId, PIdModified, PIdOriginal).
  // * siteTree1__modificationInNode(SId, PIdModified, PIdOriginal, DOMNodeID, Type, OldValue, NewValue).
  // * siteTree1__action(SId, AId, Type, Generator, SeqNr).
  // * siteTree1__actionParameter(SId, AId, Role, Param).
  // public void generatePageChangeAtoms(final Action lastA, final Model actionFeedback, final History history)
  // throws ProcessingException {
  // if (!hasPageChanged()) {
  // logger.trace("Generate page change atoms called, but no page changed happened.");
  // return;
  // }
  // final String siteId = history.getSiteId();
  // final Model stModel = Processor.SITE_TREE.getSubModel(actionFeedback, siteId);
  // final String oldPageId = history.getLastPageId();
  // final String newPageId = history.getPageId();
  // if (current.isDifferentPage(last)) {
  // NSPredicate.CHILD.addAtom(stModel, newPageId.toString(), oldPageId.toString(), lastA.getId(history));
  // generatePageJustLoadedAtoms(actionFeedback, history);
  // } else {
  // NSPredicate.MODIFICATION_OF.addAtom(stModel, newPageId, oldPageId);
  // final List<PageDifference> diffs = computeDifferences();
  // for (final PageDifference pd : diffs)
  // NSPredicate.MODIFICATION_IN_NODE.addAtom(stModel, newPageId, oldPageId, DOMNodeFinderService.getIdForNode(
  // browser, pd.getNode(), true), pd.getKind().toString(), stModel.getStringLiteral(pd.getOldValue())
  // .toString(), stModel.getStringLiteral(pd.getNewValue()).toString());
  // }
  // }

  // public void generateRootActionAtoms(final Action lastA, final Model actionFeedback, final History history)
  // throws ProcessingException {
  // generatePageJustLoadedAtoms(actionFeedback, history);
  // final Model stModel = Processor.SITE_TREE.getSubModel(actionFeedback, history.getSiteId());
  // NSPredicate.SITE__ROOT.addAtom(stModel, history.getPageId(), lastA.getId(history));
  // }
  //
  // private void generatePageJustLoadedAtoms(final Model actionFeedback, final History history)
  // throws ProcessingException {
  // final Model stModel = Processor.SITE_TREE.getSubModel(actionFeedback, history.getSiteId());
  // NSPredicate.SITE__PAGE.addAtom(stModel, history.getPageId(), stModel.getStringLiteral(browser.getLocationURL())
  // .toString());
  // final Model schedulerModel = Processor.SCHEDULER.getSubModel(actionFeedback, history.getPageId());
  // NSPredicate.PAGE_LOADED.addAtom(schedulerModel);
  // }
  //
  // public void generateFeedbackForAction(final Action a, final Model actionFeedback, final String siteId) {
  // // TODO check the expect information for action and generate according failure atoms.
  //
  // }

  public boolean atSameLocation() {
    return current.atSameLocation(last);
  }

  public boolean hasPageChanged() {
    logger.trace("Differences: {}", computeDifferences());
    if ((last == null) || (current == null))
      return false;
    return !current.identicalTo(last);
  }

  public List<PageDifference> computeDifferences() {
    return current.getDifferences(last);
  }

  public boolean isPageSimilar() {
    logger.trace("Differences: {}", computeDifferences());
    if ((last == null) || (current == null))
      return false;
    return current.similarTo(last);
  }

}