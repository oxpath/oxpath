/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.impl;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.diadem.webapi.css.CSSStyleRule;
import uk.ac.ox.cs.diadem.webapi.css.CSSStyleSheet;
import uk.ac.ox.cs.diadem.webapi.css.StyledInfoNode;
import uk.ac.ox.cs.diadem.webapi.css.StyledNode;
import uk.ac.ox.cs.diadem.webapi.css.StyledOverlay;
import uk.ac.ox.cs.diadem.webapi.css.StyledOverlayBuilder;
import uk.ac.ox.cs.diadem.webapi.css.StyledRangeNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMCSS2Properties.CssProperty;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;

/**
 * Builder class for {@link StyledOverlay} objects, to highlight info on the current page by using CSS
 *
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
class StyledOverlayBuilderImpl implements StyledOverlayBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(StyledOverlayBuilderImpl.class);
  private final LinkedHashSet<StyledNode> roots;
  private final LinkedHashMap<StyledNode, LinkedHashSet<StyledNode>> childrenMap;
  private final Map<StyledNode, StyledNode> parentMap;

  private final Map<StyledNode, Pair<List<Pair<String, String>>, List<List<String>>>> infoBox;

  private String cssRules;
  private final WebDriverBrowserImpl browserRef;
  private final Random random;
  private final LinkedList<Object[]> htmlBoxes;

  public StyledOverlayBuilderImpl(final WebDriverBrowserImpl browserRef) {
    random = new Random(System.nanoTime());
    this.browserRef = browserRef;
    roots = Sets.newLinkedHashSet();
    childrenMap = Maps.newLinkedHashMap();
    parentMap = Maps.newHashMap();
    infoBox = Maps.newLinkedHashMap();
    htmlBoxes = Lists.newLinkedList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StyledOverlay build(final String cssRules) {
    this.cssRules = cssRules;
    return new StyledOverlayImpl();
  }

  Collection<StyledNode> getChildrenOf(final StyledNodeImpl node) {
    if (childrenMap.containsKey(node))
      return childrenMap.get(node);
    return ImmutableList.of();
  }

  StyledNode getParentOf(final StyledNodeImpl child) {
    return parentMap.get(child);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StyledOverlayBuilder addRootNode(final StyledNode item) {
    if (roots.contains(item)) {
      LOG.error("Root node {} already existent");
      throw new WebAPIRuntimeException("Cannot add root node as already present: " + item + " among roots " + roots,
          LOG);
    }
    roots.add(item);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StyledOverlayBuilder addNode(final StyledNode parent, final StyledNode node) {

    // if (parent.equals(node)) {
    // LOG.error("Can not visualize node as its own parent: " + parent + " vs. " + node);
    // return this;
    // }

    if (parent.getLocator().equals(node.getLocator())) {
      LOG.error("Can not visualize node as its own parent: " + parent + " vs. " + node);
      return this;
    }
    // assert !parent.equals(node) : "parent " + parent + " is equals to node " + node;

    LinkedHashSet<StyledNode> children = childrenMap.get(parent);
    if (children == null) {
      children = Sets.newLinkedHashSet();
      childrenMap.put(parent, children);
    }

    // for the assert
    final Optional<StyledNode> exists = Iterables.tryFind(children, new Predicate<StyledNode>() {

      @Override
      public boolean apply(final StyledNode child) {
        return parent.getLocator().equals(child.getLocator());
      }
    });

    assert !exists.isPresent() : "children list \n " + children + " contains parent: " + parent;
    children.add(node);
    // store the child-parent relation
    parentMap.put(node, parent);
    // if the parent it isn't yet a root and in turn as no parent, we add it as root
    if (!roots.contains(parent) && !parentMap.containsKey(parent)) {
      addRootNode(parent);
    }

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addInfobox(final StyledNode referenceNode, final Pair<String, String> keyValue,
      final String... classNames) {

    // if (referenceNode.hasInfoBox())
    // LOG.error("Skpping attribute {} as overlapping with another one", keyValue);
    // throw new WebAPIRuntimeException(
    // "The reference node is itself a StyledInfoNode which cannot be further decorated", LOG);

    Pair<List<Pair<String, String>>, List<List<String>>> mapByLists = infoBox.get(referenceNode);
    if (mapByLists == null) {
      final List<Pair<String, String>> left = Lists.newLinkedList();
      final List<List<String>> right = Lists.newLinkedList();
      mapByLists = Pair.of(left, right);
      infoBox.put(referenceNode, mapByLists);
    }
    mapByLists.getLeft().add(keyValue);
    mapByLists.getRight().add(Arrays.asList(classNames));

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StyledNode createNode(final String xpathLocator, final String... classNames) {
    return new StyledNodeImpl(xpathLocator, classNames);
  }

  // todo add other method for attributes and convert varargs to lists

  /**
   * {@inheritDoc}
   */
  @Override
  public StyledRangeNode createRangeNode(final String startTextNodeXPathLocator, final String endTextNodeXPathLocator,
      final Pair<Integer, Integer> range, final String... classNames) {

    return new StyledRangeNodeImpl(startTextNodeXPathLocator, endTextNodeXPathLocator, range, classNames);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public StyledRangeNode createRangeNode(final String locatorForTextNode, final Pair<Integer, Integer> range,
      final String... classNames) {
    return createRangeNode(locatorForTextNode, locatorForTextNode, range, classNames);
  }

  // public void addAttributeValue(final StyledNode node, final Pair<String, String>... attributes) {
  // List<Pair<String, String>> list = node2attributes.get(node);
  // if (list == null) {
  // list = Lists.newArrayList(attributes);
  // node2attributes.put(node, list);
  // } else
  // list.addAll(Arrays.asList(attributes));
  // }

  // private class StyledNodeBuilderImpl implements StyledNodeBuilder {
  //
  // @Override
  // public StyledNodeBuilder appendKeyValueContent(final Pair<String, String> line, final String... classes) {
  // // TODO Auto-generated method stub
  // return null;
  // }
  //
  // @Override
  // public StyledNodeBuilder setAttribute(final Pair<String, String> line) {
  // // TODO Auto-generated method stub
  // return null;
  // }
  //
  // @Override
  // public StyledNodeBuilder setClasses(final String... classes) {
  // // TODO Auto-generated method stub
  // return null;
  // }
  //
  // @Override
  // public StyledNode build() {
  // // TODO Auto-generated method stub
  // return null;
  // }
  //
  // }

  @Override
  public void addFixedHTMLBox(final String innerHTML, final Rect boundingRect, final String... classNames) {
    htmlBoxes.add(new Object[] { boundingRect.asList(), innerHTML, classNames });
  }

  @Override
  public Rect createRect(final int top, final int left, final int width, final int height) {

    return new RectImplementation(top, width, height, left);
  }

  private class StyledNodeImpl implements StyledNode {

    private final String xpathLocator;
    protected final List<String> classNames;

    StyledNodeImpl(final String xpathLocator, final String[] classNames) {
      this.xpathLocator = xpathLocator;
      this.classNames = Arrays.asList(classNames);
    }

    @Override
    public String getLocator() {
      return xpathLocator;
    }

    @Override
    public Collection<String> classes() {
      return classNames;
    }

    @Override
    public Collection<StyledNode> children() {
      return getChildrenOf(this);

    }

    @Override
    public StyledNode parent() {
      return getParentOf(this);
    }

    @Override
    public String toString() {
      return Joiner.on(".").join(xpathLocator, classNames);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + (xpathLocator == null ? 0 : xpathLocator.hashCode());
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
      final StyledNodeImpl other = (StyledNodeImpl) obj;
      if (xpathLocator == null) {
        if (other.xpathLocator != null)
          return false;
      } else if (!xpathLocator.equals(other.xpathLocator))
        return false;
      return true;
    }

    // @Override
    // public void toggleCSSClass(final String className) {
    // browserRef.factory.callJS("toggleCSSClass", getLocator(), className);
    //
    // }
    //
    // @Override
    // public void addCSSClass(final String className) {
    // browserRef.factory.callJS("addCSSClass", getLocator(), className);
    //
    // }
    //
    // @Override
    // public void removeCSSClass(final String className) {
    // browserRef.factory.callJS("removeCSSClass", getLocator(), className);
    // }
    //
    // @Override
    // public boolean containsCSSClass(final String className) {
    // return browserRef.factory.<Boolean> callJS("containsCSSClass", getLocator(), className);
    // }

    @Override
    public boolean hasRange() {
      return false;
    }

    @Override
    public Pair<List<Pair<String, String>>, List<List<String>>> getInfoBox() {

      final Pair<List<Pair<String, String>>, List<List<String>>> pair = infoBox.get(this);
      if (pair == null) {
        final List<Pair<String, String>> a = ImmutableList.of();
        final List<List<String>> b = ImmutableList.of();
        return Pair.of(a, b);
      }
      return pair;

    }

    @Override
    public boolean hasInfoBox() {
      return infoBox.containsKey(this);
    }

    @Override
    public StyledInfoNode getInfoNode() {
      // FIXME
      return null;
    }
  }

  private class StyledOverlayImpl implements StyledOverlay {

    private final String id;
    private CSSStyleSheet ss;

    StyledOverlayImpl() {
      id = "" + random.nextInt();
    }

    @Override
    public Collection<StyledNode> roots() {
      return roots;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + (id == null ? 0 : id.hashCode());
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
      final StyledOverlayImpl other = (StyledOverlayImpl) obj;
      if (id == null) {
        if (other.id != null)
          return false;
      } else if (!id.equals(other.id))
        return false;
      return true;
    }

    private String getId() {
      return id;
    }

    @Override
    public void attach() {

      // doDisplay(overlayId, roots, allNodes, childrenMap, classesMap, rangesMap, cssRules,typesMap,valuesMap,
      // infoClasses);
      final Collection<String> allnodes = Lists.newLinkedList();
      final Collection<String> roots = Lists.newLinkedList();

      final Collection<Collection<String>> childrenMap = Lists.newLinkedList();
      final Collection<Collection<String>> classesMap = Lists.newLinkedList();
      final Collection<Collection<Object>> rangesMap = Lists.newLinkedList();
      final Collection<Collection<String>> typesMap = Lists.newLinkedList();
      final Collection<Collection<String>> valuesMap = Lists.newLinkedList();
      final Collection<Collection<Collection<String>>> infoClasses = Lists.newLinkedList();
      for (final StyledNode node : roots()) {

        LOG.trace("Processing root node {}", node.getLocator());
        // TODO escaping " or '
        roots.add(node.getLocator());
        serialize(node, allnodes, childrenMap, classesMap, rangesMap, typesMap, valuesMap, infoClasses);
      }

      // here now that html-boxes
      // var boxes = [[[200,200,100,100],"<li>this is a list</li>",["attribute","record"]]];
      for (final Object[] box : htmlBoxes) {

      }

      final List<String> log = browserRef.factory.callJS("attachOverlay", getId(), roots, allnodes, childrenMap,
          classesMap, rangesMap, cssRules, typesMap, valuesMap, infoClasses, htmlBoxes);
      for (final String string : log) {
        LOG.error("Visualization in JS returns: <{}>", string);
      }

    }

    private void serialize(final StyledNode currentNode, final Collection<String> allnodes,
        final Collection<Collection<String>> childrenMap, final Collection<Collection<String>> classesMap,
        final Collection<Collection<Object>> rangesMap, final Collection<Collection<String>> typesMap,
        final Collection<Collection<String>> valuesMap, final Collection<Collection<Collection<String>>> infoClasses) {

      // var roots= ['/html/body/div'];
      // var allnodes= ['/html/body/div','/html/body/div/p[1]/child::text()', '/html/body/div/p[2]/child::text()'];
      // var childrenMap = [["/html/body/div/p[1]/child::text()", "/html/body/div/p[2]/child::text()"], [], []];
      // var classesMap = [["record"], ["attribute"], ["attribute"]];
      // var rangesMap = [[], [2,"/html/body/div/p[2]/child::text()",2], [5,"/html/body/div/p[2]/child::text()",8]];
      // var cssRules =
      // ".attribute { z-index: 1; border: 1px solid blue;} .record { z-index: 1; border: 2px solid red;}"
      // var typesMap = [[], [], []];
      // var valuesMap = [[], [], []];
      // var infoClasses = [[], [], []];

      allnodes.add(currentNode.getLocator());

      classesMap.add(currentNode.classes());

      // for each keyvalue pair of content
      final Pair<List<Pair<String, String>>, List<List<String>>> infoBox = currentNode.getInfoBox();
      final Collection<Pair<String, String>> keySet = infoBox.getLeft();
      if (keySet.isEmpty()) {
        typesMap.add(ImmutableList.<String> of());
        valuesMap.add(ImmutableList.<String> of());
        infoClasses.add(ImmutableList.<Collection<String>> of());
      } else {
        final Collection<String> typesForNode = Lists.newLinkedList();
        typesMap.add(typesForNode);
        final Collection<String> valuesForNode = Lists.newLinkedList();
        valuesMap.add(valuesForNode);
        final Collection<Collection<String>> classesForNode = Lists.newLinkedList();
        infoClasses.add(classesForNode);

        final Iterator<List<String>> iteratorOnClasses = infoBox.getRight().iterator();
        for (final Pair<String, String> pair : keySet) {
          typesForNode.add(pair.getKey());
          valuesForNode.add(pair.getValue());
          classesForNode.add(iteratorOnClasses.next());

        }
      }

      if (currentNode.hasRange()) {
        final StyledRangeNode range = (StyledRangeNode) currentNode;
        rangesMap.add(ImmutableList.<Object> of(range.range().getLeft(), range.getEndNodeLocator(), range.range()
            .getRight()));
      } else {
        rangesMap.add(ImmutableList.<Object> of());
      }

      if (currentNode.children().isEmpty()) {
        childrenMap.add(ImmutableList.<String> of());
      } else {
        final Collection<String> childrenLocators = Lists.newLinkedList();

        childrenMap.add(childrenLocators);

        for (final StyledNode child : currentNode.children()) {
          childrenLocators.add(child.getLocator());
          // recursion on the tree
          serialize(child, allnodes, childrenMap, classesMap, rangesMap, typesMap, valuesMap, infoClasses);
        }

      }
    }

    @Override
    public void detach() {
      browserRef.factory.callJS("detachOverlay", getId());
    }

    @Override
    public CSSStyleSheet getCSSStyleSheet() {
      if (ss == null) {
        ss = new CSSStyleSheetImpl(id);
      }
      return ss;
    }

  }

  private class CSSStyleSheetImpl implements CSSStyleSheet {

    private final String styleElementId;
    private boolean listStale = false;

    public CSSStyleSheetImpl(final String styleElementId) {
      this.styleElementId = styleElementId;

    }

    @Override
    public boolean isDisabled() {
      browserRef.factory.<Boolean> callJS("isCSSStyleSheetDisabled", styleElementId);
      return false;
    }

    @Override
    public void enable() {
      browserRef.factory.callJS("enableCSSStyleSheet", styleElementId);

    }

    @Override
    public void disable() {
      browserRef.factory.callJS("disableCSSStyleSheet", styleElementId);
    }

    @Override
    public List<CSSStyleRule> cssRules() {
      final Long length = browserRef.factory.callJS("ruleListLength", styleElementId);
      final LinkedList<CSSStyleRule> rules = Lists.newLinkedList();
      for (int i = 0; i < length.intValue(); i++) {
        rules.add(new CSSStyleRuleImpl(this, i));
      }
      listStale = false;
      return rules;
    }

    @Override
    public List<CSSStyleRule> findRulesBySelectorText(final String text) {
      final List<Long> view = browserRef.factory.callJS("findRulesBySelectorText", styleElementId, text);
      final LinkedList<CSSStyleRule> rules = Lists.newLinkedList();
      for (int i = 0; i < view.size(); i++) {
        rules.add(new CSSStyleRuleImpl(this, view.get(i).intValue()));
      }
      return rules;
    }

    @Override
    public void appendRule(final String cssText) {
      browserRef.factory.callJS("appendRule", styleElementId, cssText);
      listStale = true;
    }

    @Override
    public void deleteRule(final int position) {
      browserRef.factory.callJS("deleteRule", styleElementId, position);
      listStale = true;

    }

    @Override
    public void deleteRule(final CSSStyleRule rule) {
      final CSSStyleRuleImpl r = (CSSStyleRuleImpl) rule;
      deleteRule(r.rulePosition);

    }

  }

  private class CSSStyleRuleImpl implements CSSStyleRule {

    private final int rulePosition;
    private final String styleElementId;
    private final CSSStyleSheetImpl ownerSS;

    public CSSStyleRuleImpl(final CSSStyleSheetImpl ownerSS, final int position) {
      this.ownerSS = ownerSS;
      styleElementId = ownerSS.styleElementId;
      rulePosition = position;

    }

    private void checkStaleness() {
      if (ownerSS.listStale) {
        LOG.error("The current {} is stale", this.getClass());
        throw new WebAPIRuntimeException("Cannot retrieve the CSSStyleRule associated to position " + rulePosition
            + ", as the list has been modified ", LOG);
      }
    }

    @Override
    public String getSelectorText() {
      checkStaleness();
      return browserRef.factory.callJS("getSelectorText", styleElementId, rulePosition);
    }

    @Override
    public String getPropertyValue(final CssProperty property) {
      checkStaleness();
      return browserRef.factory
          .callJS("getRulePropertyValue", styleElementId, rulePosition, property.getPropertyName());
    }

    @Override
    public String removeProperty(final CssProperty property) {
      checkStaleness();
      return browserRef.factory.callJS("removeRuleProperty", styleElementId, rulePosition, property.getPropertyName());
    }

    @Override
    public void setProperty(final CssProperty property, final String value) {
      checkStaleness();
      browserRef.factory
      .callJS("setRulePropertyValue", styleElementId, rulePosition, property.getPropertyName(), value);

    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + getOuterType().hashCode();
      result = (prime * result) + rulePosition;
      result = (prime * result) + (styleElementId == null ? 0 : styleElementId.hashCode());
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
      final CSSStyleRuleImpl other = (CSSStyleRuleImpl) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (rulePosition != other.rulePosition)
        return false;
      if (styleElementId == null) {
        if (other.styleElementId != null)
          return false;
      } else if (!styleElementId.equals(other.styleElementId))
        return false;
      return true;
    }

    private StyledOverlayBuilderImpl getOuterType() {
      return StyledOverlayBuilderImpl.this;
    }

    @Override
    public boolean isStale() {
      return ownerSS.listStale;
    }

  }

  private class StyledRangeNodeImpl extends StyledNodeImpl implements StyledRangeNode {

    private final String startNodeXPathLocator;
    private final String endNodeXPathLocator;
    private final Pair<Integer, Integer> range;

    StyledRangeNodeImpl(final String startNodeXPathLocator, final String endNodeXPathLocator,
        final Pair<Integer, Integer> range, final String[] classNames) {

      super(startNodeXPathLocator, classNames);
      this.startNodeXPathLocator = startNodeXPathLocator;
      this.endNodeXPathLocator = endNodeXPathLocator;
      this.range = range;
    }

    @Override
    public boolean hasRange() {
      return true;
    }

    @Override
    public String getStartNodeLocator() {
      return startNodeXPathLocator;
    }

    @Override
    public String getEndNodeLocator() {
      return endNodeXPathLocator;
    }

    @Override
    public Pair<Integer, Integer> range() {
      return range;
    }

    @Override
    public String toString() {
      if (startNodeXPathLocator.equals(endNodeXPathLocator))
        return Objects.toStringHelper(this).add("FROM", startNodeXPathLocator).add("startOffset", range.getLeft())
            .add("endOffset", range.getRight()).toString();
      else
        return Objects.toStringHelper(this).add("FROM", startNodeXPathLocator).add("startOffset", range.getLeft())
            .add("TO", endNodeXPathLocator).add("endOffset", range.getRight()).toString();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + (endNodeXPathLocator == null ? 0 : endNodeXPathLocator.hashCode());
      result = (prime * result) + (range == null ? 0 : range.hashCode());
      result = (prime * result) + (startNodeXPathLocator == null ? 0 : startNodeXPathLocator.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (!super.equals(obj))
        return false;
      if (getClass() != obj.getClass())
        return false;
      final StyledRangeNodeImpl other = (StyledRangeNodeImpl) obj;
      if (endNodeXPathLocator == null) {
        if (other.endNodeXPathLocator != null)
          return false;
      } else if (!endNodeXPathLocator.equals(other.endNodeXPathLocator))
        return false;
      if (range == null) {
        if (other.range != null)
          return false;
      } else if (!range.equals(other.range))
        return false;
      if (startNodeXPathLocator == null) {
        if (other.startNodeXPathLocator != null)
          return false;
      } else if (!startNodeXPathLocator.equals(other.startNodeXPathLocator))
        return false;
      return true;
    }

  }

  private final class RectImplementation implements Rect {

    private final List<Integer> coord;

    private RectImplementation(final Integer top, final Integer width, final Integer height, final Integer left) {
      coord = Lists.newArrayList(top, left, width, height);

    }

    @Override
    public Integer getWidth() {
      return coord.get(2);
    }

    @Override
    public Integer getTop() {
      return coord.get(0);
    }

    @Override
    public Integer getLeft() {
      return coord.get(1);
    }

    @Override
    public Integer getHeight() {
      return coord.get(3);
    }

    @Override
    public String toString() {
      return MessageFormat.format("top:{0}, left:{1}, width:{3},height:{2}", getTop(), getLeft(), getWidth(),
          getHeight());
    }

    private StyledOverlayBuilderImpl getOuterType() {
      return StyledOverlayBuilderImpl.this;
    }

    @Override
    public List<Integer> asList() {

      return Lists.newArrayList(coord);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + getOuterType().hashCode();
      result = (prime * result) + ((coord == null) ? 0 : coord.hashCode());
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
      final RectImplementation other = (RectImplementation) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (coord == null) {
        if (other.coord != null)
          return false;
      } else if (!coord.equals(other.coord))
        return false;
      return true;
    }

  }
}
