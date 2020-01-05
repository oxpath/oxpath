/**
 *
 */
package uk.ac.ox.cs.diadem.webapi.dom.mutation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.openqa.selenium.StaleElementReferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.dom.DOMBoundingClientRect;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNodeList;
import uk.ac.ox.cs.diadem.webapi.dom.DOMTypeableElement;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver.MutationObserverOptions;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationRecord.MutationType;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIStaleElementRuntimeException;
import uk.ac.ox.cs.diadem.webapi.utils.XPathUtil;

/**
 * @author giog
 *
 */
public class MutationDetectionUtils {

  private static final Logger LOG = LoggerFactory.getLogger(MutationDetectionUtils.class);

  private MutationDetectionUtils() {

  }

  public static MutationObserverOptions mutationObserverOptions(final boolean subtree, final boolean childList,
      final boolean characterData, final boolean attributes, final List<String> attributeFilter) {
    return new MutationObserverOptionsImplementation(subtree, childList, characterData, attributes, null);
  }

  public static DOMMutationObserver installObserverOnBody(final WebBrowser browser,
      final MutationObserverOptions options) {
    try {
      // FIXME why we don't use HTMl anyway?
      final DOMNodeList bodies = browser.getContentDOMWindow().getDocument().getElementsByTagName("body");
      DOMNode item = null;
      if (bodies.getLength() > 0) {
        item = bodies.item(0);
      } else {
        item = browser.getContentDOMWindow().getDocument().getElementsByTagName("html").item(0);
      }
      final DOMMutationObserver observer = item.registerMutationObserver(options);
      return observer;
    } catch (final Exception e) {
      LOG.warn("Cannot install MutationObserverOnBody on this page, due to <{}>", e.getMessage());
      return new DOMMutationObserver() {

        @Override
        public Set<DOMMutationRecord> takeRecords() {
          return ImmutableSet.of();
        }

        @Override
        public void disconnect() {
          // do nothing
        }
      };
    }
  }

  public static boolean detectListBelow(final DOMTypeableElement typable, final Set<DOMMutationRecord> bodyRecords) {

    // mapping a node to all its modifications
    final Map<DOMNode, List<DOMMutationRecord>> elementToMutations = getRecordsByTargetNode(bodyRecords);

    final Map<MyTriple, MyTriple> newNodesParent = Maps.newHashMap();
    final Map<MyTriple, MyTriple> newAttrPerParent = Maps.newHashMap();

    for (final DOMNode target : elementToMutations.keySet()) {
      // check is stale and skip in case
      if (target.isStale()) {
        continue;
      }

      final List<DOMMutationRecord> mods = elementToMutations.get(target);
      for (final DOMMutationRecord mod : mods) {
        // if is of type childlist
        if (mod.type() == MutationType.childList) {
          // check the addedNodes
          for (final DOMNode child : mod.addedNodes()) {
            if (child.isStale()) {
              continue;
            }
            // collect the changes of new nodes per childNode and frequency
            final MyTriple triple = new MyTriple(target, child.getLocalName());

            if (newNodesParent.containsKey(triple)) {
              newNodesParent.get(triple).incrementFrequency();
            } else {
              newNodesParent.put(triple, triple);
            }
          }
        }

        // the same for attributes
        if (mod.type() == MutationType.attributes) {
          // // why the parent node??
          final MyTriple triple = new MyTriple(target.getParentNode(), mod.attributeName());
          if (newAttrPerParent.containsKey(triple)) {
            newAttrPerParent.get(triple).incrementFrequency();
          } else {
            newAttrPerParent.put(triple, triple);
          }
        }
        // text nodes changing are uncommon in autocomplete
        if (mod.type() == MutationType.characterData) {
          LOG.debug("Not dealing with {} in autocomplete detection", mod.type());
        }
      }
    }

    if (newNodesParent.isEmpty() && newAttrPerParent.isEmpty()) {
      LOG.debug("no mutation events observed");
      return false;
    }

    // order by frequency
    final Ordering<MyTriple> ordering = Ordering.natural().onResultOf(MyTriple.getFreq());

    // most frequent child node added
    final List<MyTriple> mostFrequentNewNodes = ordering.greatestOf(newNodesParent.keySet(), 1);
    // // most frequent attribute added
    final List<MyTriple> mostFrequentNewAttr = ordering.greatestOf(newAttrPerParent.keySet(), 1);

    final List<MyTriple> candidates = Lists.newArrayList();

    final Iterable<MyTriple> allMostFrequent = Iterables.concat(mostFrequentNewAttr, mostFrequentNewNodes);

    for (final MyTriple possibleCandidate : allMostFrequent) {
      if (possibleCandidate.parentNode.getNodeName().equals("body")) {
        continue;
      }
      if (isBelow((DOMElement) possibleCandidate.parentNode, typable)) {
        candidates.add(possibleCandidate);
      }
    }

    if (candidates.size() == 0) {
      LOG.debug("Cannot find any list below {}", typable);
    }

    if (candidates.size() > 1) {
      LOG.warn("Found {} list below {}", candidates.size(), typable);
    }

    return !candidates.isEmpty();

    // // the two most frequent
    // final List<MyTriple> firstTwoMostFrequents = ordering.greatestOf(candidates, 1);
    // // THE CANDIDATE
    //
    // final MyTriple ultimate = mostFrequentNewNodes.get(0);
    //
    //
    //
    // return isBelow((DOMElement) ultimate.parentNode, typable);

    // if (firstTwoMostFrequents.size() > 1) {
    // ultimate = discriminateByVisualProximity(firstTwoMostFrequents);
    // }
    // // invisible basically
    // if (ultimate == null)
    // return false;

  }

  private static boolean isBelow(final DOMElement below, final DOMTypeableElement above) {

    final DOMBoundingClientRect aboveRect = above.getBoundingClientRect();
    final float above_left = aboveRect.getLeft();
    final float above_bottom = aboveRect.getBottom();
    final float above_height = aboveRect.getHeight();

    final DOMBoundingClientRect belowRect = below.getBoundingClientRect();
    final float below_left = belowRect.getLeft();
    final float below_top = belowRect.getTop();

    final boolean horizAlign = Math.abs(below_left - above_left) < 5;
    final boolean verticAlig = (below_top - above_bottom) < (above_height / 2);

    return horizAlign && verticAlig;
  }

  private static Map<DOMNode, List<DOMMutationRecord>> getRecordsByTargetNode(final Set<DOMMutationRecord> records) {
    final Map<DOMNode, List<DOMMutationRecord>> recordsByNode = Maps.newHashMap();
    // tranform the map
    for (final DOMMutationRecord record : records) {

      final DOMNode target = record.target();

      List<DOMMutationRecord> list = recordsByNode.get(target);
      if (list == null) {
        list = Lists.newArrayList();
        recordsByNode.put(target, list);
      }
      list.add(record);

    }
    return recordsByNode;
  }

  public static class MyTriple {
    private final DOMNode parentNode;
    private final String childTagName;
    private final MutableInt frequency = new MutableInt(1);

    public MyTriple(final DOMNode node, final String tagName) {
      parentNode = node;
      childTagName = tagName;

    }

    public void incrementFrequency() {
      frequency.increment();
    }

    @Override
    public String toString() {

      return Objects.toStringHelper(this).add("parentNode", parentNode).add("childTagName", childTagName)
          .add("freq", frequency).toString();
    }

    public static Function<MyTriple, Integer> getFreq() {
      return new Function<MutationDetectionUtils.MyTriple, Integer>() {

        @Override
        public Integer apply(final MyTriple input) {
          return new Integer(input.frequency.intValue());
        }
      };
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + ((parentNode == null) ? 0 : parentNode.hashCode());
      result = (prime * result) + ((childTagName == null) ? 0 : childTagName.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final MyTriple other = (MyTriple) obj;
      if (childTagName == null) {
        if (other.childTagName != null)
          return false;
      } else if (!childTagName.equals(other.childTagName))
        return false;
      if (parentNode == null) {
        if (other.parentNode != null)
          return false;
      } else if (!parentNode.equals(other.parentNode))
        return false;

      return true;
    }

    public String getChildTagName() {
      return childTagName;
    }

    public DOMNode getParentNode() {
      return parentNode;
    }

  }

  private static final class MutationObserverOptionsImplementation implements MutationObserverOptions {
    private final boolean subtree;
    private final boolean childList;
    private final boolean characterData;
    private final boolean attributes;
    private final List<String> attributeFilter;

    public MutationObserverOptionsImplementation(final boolean subtree, final boolean childList,
        final boolean characterData, final boolean attributes, final List<String> attributeFilter) {
      this.subtree = subtree;
      this.childList = childList;
      this.characterData = characterData;
      this.attributes = attributes;
      this.attributeFilter = attributeFilter;
    }

    @Override
    public Boolean subtree() {
      return subtree;
    }

    @Override
    public Boolean childList() {
      return childList;
    }

    @Override
    public Boolean characterData() {
      return characterData;
    }

    @Override
    public Boolean attributes() {
      return attributes;
    }

    @Override
    public List<String> attributeFilter() {
      return attributeFilter;
    }
  }

  public static DOMNode performAutocompleteIfAny(final DOMMutationObserver bodyObserver, final WebBrowser browser) {
    return checkForAutocomplete(bodyObserver, browser);
  }

  private static DOMNode checkForAutocomplete(final DOMMutationObserver bodyObserver, final WebBrowser browser) {
    try {
      // all page modifications
      final Set<DOMMutationRecord> bodyRecords = bodyObserver.takeRecords();
      bodyObserver.disconnect();// needed for the messages later

      // mapping a node to all its modifications
      final Map<DOMNode, List<DOMMutationRecord>> elementToMutations = getRecordsByTargetNode(bodyRecords);

      final Map<MyTriple, MyTriple> newNodesParent = Maps.newHashMap();
      final Map<MyTriple, MyTriple> newAttrPerParent = Maps.newHashMap();

      for (final DOMNode target : elementToMutations.keySet()) {
        // check is stale and skip in case
        if (target.isStale()) {
          continue;
        }

        final List<DOMMutationRecord> mods = elementToMutations.get(target);
        for (final DOMMutationRecord mod : mods) {
          // if is of type childlist
          if (mod.type() == MutationType.childList) {
            // check the addedNodes
            for (final DOMNode child : mod.addedNodes()) {
              if (child.isStale()) {
                continue;
              }
              // collect the changes of new nodes per type and frequency
              final MyTriple triple = new MyTriple(target, child.getLocalName());
              if (newNodesParent.containsKey(triple)) {

                newNodesParent.get(triple).incrementFrequency();
              } else {
                newNodesParent.put(triple, triple);
              }
            }
          }

          // the same for attributes
          if (mod.type() == MutationType.attributes) {
            // why the parent node??
            final MyTriple triple = new MyTriple(target.getParentNode(), mod.attributeName());
            if (newAttrPerParent.containsKey(triple)) {
              newAttrPerParent.get(triple).incrementFrequency();
            } else {
              newAttrPerParent.put(triple, triple);
            }
          }
          // text nodes changing are uncommon in autocomplete
          if (mod.type() == MutationType.characterData) {
            LOG.debug("Not dealing with {} in autocomplete detection", mod.type());
          }
        }
      }

      if (newNodesParent.isEmpty() && newAttrPerParent.isEmpty())
        return null;

      // order by frequency
      final Ordering<MyTriple> ordering = Ordering.natural().onResultOf(MyTriple.getFreq());

      // most frequent child node added
      final List<MyTriple> mostFrequentNewNodes = ordering.greatestOf(newNodesParent.keySet(), 1);
      // most frequent attribute added
      final List<MyTriple> mostFrequentNewAttr = ordering.greatestOf(newAttrPerParent.keySet(), 1);

      final List<MyTriple> candidates = Lists.newArrayList();
      candidates.addAll(mostFrequentNewNodes);
      candidates.addAll(mostFrequentNewAttr);

      // the two most frequent
      final List<MyTriple> firstTwoMostFrequents = ordering.greatestOf(candidates, 2);
      // THE CANDIDATE
      MyTriple ultimate = firstTwoMostFrequents.get(0);

      if (firstTwoMostFrequents.size() > 1) {
        ultimate = discriminateByVisualProximity(firstTwoMostFrequents);
      }
      // invisible basically
      if (ultimate == null)
        return null;

      final String xpath = "child::" + ultimate.getChildTagName();
      final DOMNode first = XPathUtil.getFirstNode(xpath, ultimate.getParentNode());// (DOMElement)
      if (first == null) {
        LOG.debug("Xpath Query {} does not match any element, no autocomplete", xpath);
        return null;
      }
      final DOMElement el = (DOMElement) first;

      if (el.isStale()) {
        LOG.debug("The element to click on as autocomplete is stale");
        return null;
      }
      if (el.getNodeName().equals("body")) {
        LOG.debug("Autocomplete detection failed, found BODY element");
        return null;
      }
      final String elToString = el.toString();
      if (el.isVisible() && el.isEnabled()) {
        LOG.debug("Autocomplete element is visible and enable, about to click on element {} ", elToString);
        pause();

        el.click();
        // final FirefoxDriver driver = castToDriver(browser);
        // final DefaultSelenium sel = new WebDriverBackedSelenium(driver, driver.getCurrentUrl());
        // sel.focus("descendant::*[@id='SearchAutoCompleteEx_completionListElem']/li[1]");
        // WebElement findElement =
        // driver.findElement(By.xpath());
        // new Actions(driver).moveToElement(
        // findElement)
        // .perform();

        // DOMNode firstNode = XPathUtil.getFirstNode("child::li[@class='highlighted']", ultimate.parentNode);
        // el.js().clickJS();

        // el.type("");

        LOG.debug("..clicked!");
        return el;
      } else {
        LOG.debug("Cannot click on autocomplete element {} as invisible/disabled", elToString);
        // ultimate.node.getFirstChild();
      }
    } catch (final StaleElementReferenceException e) {
      LOG.debug("ignored StaleElementReferenceException {} during click on autocomplete ", e.getMessage());
      // ignored
    } catch (final WebAPIStaleElementRuntimeException e) {
      LOG.debug("ignored WebAPIStaleElementRuntimeException {} during click on autocomplete ", e.getMessage());
      // ignored
    } catch (final WebAPIRuntimeException e) {
      LOG.debug("ignored WebAPIRuntimeException {} during click on autocomplete ", e.getMessage());
      // ignored

    } catch (final Exception e) {
      LOG.error("ignored Exception during click on detected autocomplete element {}. Error: {}", e.getMessage());
      // ignored
    }

    LOG.debug("Autocomplete detection failed, cannot find any ");
    return null;
  }

  public static void pause() {
    try {
      final int millis = 2000;
      LOG.debug("..sleep {}ms!", millis);
      Thread.sleep(millis);
    } catch (final InterruptedException e) {
      // nothing to do here
    }

  }

  private static MyTriple discriminateByVisualProximity(final List<MyTriple> mostFrequents) {

    final Iterable<MyTriple> visible = Iterables.filter(mostFrequents, new Predicate<MyTriple>() {

      @Override
      public boolean apply(final MyTriple input) {
        return input.getParentNode().isVisible();
      }
    });

    if (Iterables.size(visible) == 0)
      return null;

    if (Iterables.size(visible) == 1)
      return visible.iterator().next();

    // TODO more than one, find closest visually

    return mostFrequents.get(0);
  }
}
