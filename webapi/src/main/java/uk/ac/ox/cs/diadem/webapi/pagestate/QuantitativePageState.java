package uk.ac.ox.cs.diadem.webapi.pagestate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationFormObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationFormSummary;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

class QuantitativePageState implements SimplePageState {

  private static final int IMG_MIN_CHANGE = 10;
  private static final int ID_MIN_SIZE = 20;
  private static final int THRESHOLD = 25;
  private static final int THRESHOLD_FOR_VISIBLE_ELEMENTS = 50;
  private static final int THRESHOLD_ID = 5;
  private static final Logger logger = LoggerFactory.getLogger(QuantitativePageState.class);
  private String locationUrl = "xx";
  private String title = null;
  private String content = null;
  private int numberOfLinks = 0;
  private int numberOfImages = 0;;
  private int numebrOfTextNodes = 0;;
  private int numberOfElement = 0;;
  private int contentHash = 0;
  private Set<String> imgSrcs = Sets.newHashSet();
  private Set<String> linkHrefs = Sets.newHashSet();

  private Set<String> ids = Sets.newHashSet();
  protected MajorChangeType differenceType = MajorChangeType.UNSPECIFIED;
  private MutationFormObserver observer;
  private static Set<String> blackListDomains = Sets.newHashSet("twitter", "t.co", "facebook");
  private Long countElementsWithVisibilityChange = -1l;
  private boolean isErroneousState = false;

  // private final Set<String> classes;

  public QuantitativePageState(final WebBrowser browser, final MutationFormObserver sharedObserver) {
    try {
      locationUrl = browser.getLocationURL();
      if (locationUrl == null) {
        content = "";
      } else {
        content = browser.getContentDOMWindow().getDocument().getDocumentElement().getTextContent();
      }
      contentHash = content.hashCode();
      logger.trace("Creating state for url <{}>", locationUrl);
      title = browser.getContentDOMWindow().getTitle();
      numberOfElement = XPathUtil.count("/descendant::*", browser);
      numberOfImages = XPathUtil.count("/descendant::img", browser);
      numberOfLinks = XPathUtil.count("/descendant::a", browser);
      numebrOfTextNodes = XPathUtil.count("/descendant::text()[string-length(.)>10]", browser);
      imgSrcs = Sets.newHashSet(browser.js().getImageSources());
      linkHrefs = Sets.newHashSet(filterBlackList(browser.js().getLinkHRefs()));
      ids = Sets.newHashSet(browser.js().getIDAttributes());

      if (sharedObserver == null) {
        // installs mutation summary on body
        DOMNode target = XPathUtil.getFirstNode("//*[local-name()='body']", browser);
        if (target == null) {
          target = XPathUtil.getFirstNode("//*[local-name()='html']", browser);
          if (target == null) {
            target = XPathUtil.getFirstNode("descendant-or-self::*[1]", browser);
          }
        }
        if (target == null) {
          // dummy one
          observer = new MutationFormObserver() {

            @Override
            public MutationFormSummary takeSummaryAndDisconnect() {
              return null;
            }
          };
        }

        observer = browser.js().observeFormMutation((DOMElement) target);
      } else {
        observer = sharedObserver;
        getCountElementsWithVisibilityChange();
      }
    } catch (final Exception e) {
      logger.warn("Cannot compute a QuantitativePageState, using a NullState");
      isErroneousState = true;
    }

    // } else {
    // extracted();
    //
    // }
    // classes = Sets.newHashSet(browser.js().getClassAttributes());
  }

  private void getCountElementsWithVisibilityChange() {
    final MutationFormSummary summary = observer.takeSummaryAndDisconnect();
    if (summary != null) {
      countElementsWithVisibilityChange = summary.countElementsWithVisibilityChange();
    }
  }

  private List<String> filterBlackList(final List<String> linkHRefs) {
    final ArrayList<String> filtered = Lists.newArrayListWithCapacity(linkHRefs.size());
    for (final String link : linkHRefs) {
      boolean good = true;
      for (final String toRemove : blackListDomains) {
        if (link.contains(toRemove)) {
          good = false;
        }
      }
      if (good) {
        filtered.add(link);
      }
    }
    return filtered;
  }

  @Override
  public boolean identicalTo(final SimplePageState other) {
    if (!(other instanceof QuantitativePageState))
      return false;
    final QuantitativePageState o = (QuantitativePageState) other;
    // if one was erroneous but not the other, then is not identical
    if (isErroneousState != o.isErroneousState)
      return false;

    if (isErroneousState && o.isErroneousState)
      return true;

    // if (!locationUrl.equals(o.locationUrl))
    // return false;

    // logger.info("{}", observer);
    if ((countElementsWithVisibilityChange + o.countElementsWithVisibilityChange) > 0)
      return false;
    if (!title.equals(o.title))
      return false;
    if (numberOfElement != o.numberOfElement)
      return false;
    if (numberOfImages != o.numberOfImages)
      return false;
    if (numberOfLinks != o.numberOfLinks)
      return false;
    if (numebrOfTextNodes != o.numebrOfTextNodes)
      return false;
    if (o.contentHash != contentHash)
      return false;

    if (!ids.equals(o.ids))
      return false;
    // if (!classes.equals(o.classes))
    // return false;
    return true;
  }

  @Override
  public boolean similarTo(final SimplePageState other) {
    if (identicalTo(other))
      return true;
    if (!(other instanceof QuantitativePageState))
      return false;
    if (isEnoughDifferent((QuantitativePageState) other))
      return false;
    return true;
  }

  // @Override
  // public boolean isDifferentPage(final SimplePageState other) {
  // if (other == null)
  // return true;
  // if (!(other instanceof QuantitativePageState))
  // throw new IllegalArgumentException("Cannot compare page states of different types. ");
  // final QuantitativePageState o = (QuantitativePageState) other;
  // if (!locationUrl.equals(o.locationUrl) || !identicalTo(other))
  // return true;
  // return false;
  // }
  @Override
  public MajorChangeType differenceTypeIfAny(final SimplePageState other) {
    // might be set already due to previuos calls
    if (differenceType == MajorChangeType.UNSPECIFIED) {
      if (!atSameLocation(other))
        return differenceType;
      if (!identicalTo(other)) {
        if (isEnoughDifferent((QuantitativePageState) other))
          return differenceType;
      }
    }
    // here is Uspecified
    return differenceType;

  }

  private boolean isEnoughDifferent(final QuantitativePageState o) {

    // specific check on id attributes: if they change we fail,therefore we need to force a major page change
    // TODO do the same for class attributes
    // if (!o.ids.containsAll(ids))
    // return true;

    // if one was erroneous but not the other, then is different enough
    if (isErroneousState != o.isErroneousState)
      return true;
    if (isErroneousState && o.isErroneousState)
      return false;

    final float linkPercentage = (o.numberOfLinks * 100.0f) / numberOfLinks;

    if (Math.abs(linkPercentage - 100) > THRESHOLD) {
      logger.debug("Pages are different enough due to link percentage {} above the threshold {}", linkPercentage,
          THRESHOLD);
      differenceType = MajorChangeType.NUM_OF_LINKS;
      return true;
    }

    final float imgPercentage = (o.numberOfImages * 100.0f) / numberOfImages;
    if (Math.abs(imgPercentage - 100) > THRESHOLD) {
      logger.debug("Pages are different enough due to number of images percentage {} above the threshold {}",
          imgPercentage, THRESHOLD);
      differenceType = MajorChangeType.NUM_OF_IMAGES;
      return true;
    }

    final float textNodesPercentage = (o.numebrOfTextNodes * 100.0f) / numebrOfTextNodes;
    if (Math.abs(textNodesPercentage - 100) > THRESHOLD) {
      logger.debug("Pages are different enough due to number of text nodes percentage {} above the threshold {}",
          textNodesPercentage, THRESHOLD);
      differenceType = MajorChangeType.NUM_OF_TEXTNODES;
      return true;
    }

    final float elementPercentage = (o.numberOfElement * 100.0f) / numberOfElement;
    if (Math.abs(elementPercentage - 100) > THRESHOLD) {
      logger.debug("Pages are different enough due to number of element percentage {} above the threshold {}",
          elementPercentage, THRESHOLD);
      differenceType = MajorChangeType.NUM_OF_ELEMENTS;
      return true;
    }

    if (checkDifferentImages(o)) {
      logger.debug("Pages are different enough due to image files percentage {} above the threshold {}",
          linkPercentage, THRESHOLD);
      differenceType = MajorChangeType.NUM_OF_CHANGED_IMAGES;
      return true;
    }

    final int changedHref = Sets.symmetricDifference(linkHrefs, o.linkHrefs).size();
    final float changedHrefRate = (changedHref * 100.0f) / linkHrefs.size();
    if (changedHrefRate > (THRESHOLD)) {
      logger.debug("Pages are different enough due to  percentage of href changed {}, above the threshold {}",
          linkPercentage, THRESHOLD);
      differenceType = MajorChangeType.NUM_OF_CHANGED_HREFs;
      return true;
    }

    final float idChanged = (Sets.intersection(o.ids, ids).size() * 100.0f) / ids.size();

    if ((ids.size() > ID_MIN_SIZE) && (Math.abs(idChanged - 100) > THRESHOLD_ID)) {
      logger.debug(
          "Pages are different enough due to number od id attributes changed in percentage {}, above the threshold {}",
          linkPercentage, THRESHOLD);
      differenceType = MajorChangeType.NUM_OF_CHANGED_IDs;
      return true;
    }

    // hack
    if ((countElementsWithVisibilityChange + o.countElementsWithVisibilityChange) > (THRESHOLD_FOR_VISIBLE_ELEMENTS + 1)) {
      differenceType = MajorChangeType.NUM_OF_VISIBLE_ELEMENT_CHANGED;
      return true;
    }

    return false;

  }

  private boolean checkDifferentImages(final QuantitativePageState o) {

    final int changedImg = Sets.symmetricDifference(imgSrcs, o.imgSrcs).size();

    final float changedRate = (changedImg * 100.0f) / imgSrcs.size();

    if ((changedImg > IMG_MIN_CHANGE) && (changedRate > THRESHOLD)) {
      if (!isUsingImgHashing(o.imgSrcs))
        return true;
    }

    return false;
  }

  private boolean isUsingImgHashing(final Set<String> fileNames) {
    final Pattern pattern = Pattern.compile("[a-z0-9]{32}+");
    int count = 0;
    for (final String file : fileNames) {
      final Matcher matcher = pattern.matcher(file);
      if (matcher.find()) {
        count++;
      }
    }
    final float matchedRate = (count * 100.0f) / fileNames.size();
    if (matchedRate > THRESHOLD)
      return true;

    return false;
  }

  // private int computeDifference(final List<String> a, final List<String> b) {
  // final ArrayList<String> copyA = Lists.newArrayList(a);
  // final ArrayList<String> copyB = Lists.newArrayList(b);
  // copyA.removeAll(copyB);
  // final int aNotB = copyA.size();
  // copyB.removeAll(a);
  // final int bNotA = copyB.size();
  // return aNotB + bNotA;
  //
  // }

  @Override
  public boolean atSameLocation(final SimplePageState other) {
    if (other == null)
      return false;

    if (!(other instanceof QuantitativePageState))
      throw new IllegalArgumentException("Cannot compare page states of different types. ");
    final QuantitativePageState o = (QuantitativePageState) other;
    if (SimplePageStateRecorder.normalizeURL(getLocationURLOfCurrentPage()).equals(
        SimplePageStateRecorder.normalizeURL(o.getLocationURLOfCurrentPage())))
      return true;
    differenceType = MajorChangeType.DIFFERENT_LOCATION;
    return false;
  }

  @Override
  public String getLocationURLOfCurrentPage() {
    return locationUrl;
  }

  @Override
  public MutationFormObserver getMutationObserver() {
    return observer;
  }

  /*
   * (non-Javadoc)
   * 
   * @see uk.ac.ox.cs.diadem.webapi.pagestate.SimplePageState#isErroneousState()
   */
  @Override
  public boolean isErroneousState() {
    return isErroneousState;
  }

}
