/**
 * Header
 */
package uk.ac.ox.cs.diadem.webapi.dom.impl;

import java.awt.Dimension;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ImmutableTable.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.io.Resources;

import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.css.StyledOverlayBuilder;
import uk.ac.ox.cs.diadem.webapi.dom.DOMBoundingClientRect;
import uk.ac.ox.cs.diadem.webapi.dom.DOMCSS2Properties.CssProperty;
import uk.ac.ox.cs.diadem.webapi.dom.DOMCSSStyleDeclaration;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocumentType;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLForm;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLInputElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLOptionElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLOptionsCollection;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLSelect;
import uk.ac.ox.cs.diadem.webapi.dom.DOMHTMLTextAreaElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNamedNodeMap;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode.Type;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNodeList;
import uk.ac.ox.cs.diadem.webapi.dom.DOMRange;
import uk.ac.ox.cs.diadem.webapi.dom.DOMTypeableElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMWindow;
import uk.ac.ox.cs.diadem.webapi.dom.DOmElementOnJS;
import uk.ac.ox.cs.diadem.webapi.dom.HTMLUtil;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMEventListener;
import uk.ac.ox.cs.diadem.webapi.dom.event.DOMKeyboardEvent.Key;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.CSSMutationObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.CSSMutationRecord;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationObserver.MutationObserverOptions;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationRecord;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.DOMMutationRecord.MutationType;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationFormObserver;
import uk.ac.ox.cs.diadem.webapi.dom.mutation.MutationFormSummary;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathEvaluator;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathException;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathExpression;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathNSResolver;
import uk.ac.ox.cs.diadem.webapi.dom.xpath.DOMXPathResult;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIFailedDismissModalDialogRuntimeException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIJavascriptRuntimeException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPINotVisibleOrDisableElementException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIRuntimeException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIStaleElementRuntimeException;
import uk.ac.ox.cs.diadem.webapi.exception.WebAPIUnsupportedJavascriptActionRuntimeException;

/**
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 * 
 * It is not a factory anymore. It is container, nesting all DOM-related classes. 
 */
class WebDriverWrapperFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverWrapperFactory.class);
  private final BlockingQueue<Object> SLEEPER = new ArrayBlockingQueue<Object>(1);
  private static final int DEFAULT_WAIT_AFTER_ACTION_MILLIS = 1000;

  private static String functions_script;
  private static String mutationEvent_script;
  private static String mutationSummary_script;

  private final Map<String, String> jsTemplateFunctionCalls;

  private final FirefoxDriver driver;

  private final WebDriverBrowserImpl browserWrapper;
  Map<String, DOMWindowWebDriverImpl> windows = Maps.newHashMap();

  private final AtomicInteger keyGeneratorForDomObservers;
  static final ImmutableBiMap<Long, Type> nodeTypes;
  static {
    nodeTypes = ImmutableBiMap.copyOf(
    		new HashMap<Long, Type>() {
				private static final long serialVersionUID = -2203511809774000069L;
			{
    			put(2L, Type.ATTRIBUTE);
    			put(4L, Type.CDATA_SECTION);
    			put(8L, Type.COMMENT);
    		    put(9L, Type.DOCUMENT);
    		    put(11L, Type.DOCUMENT_FRAGMENT);
    		    put(10L, Type.DOCUMENT_TYPE);
    		    put(1L, Type.ELEMENT);
    		    put(6L, Type.ENTITY);
    		    put(5L, Type.ENTITY_REFERENCE);
    		    put(12l, Type.NOTATION);
    		    put(7l, Type.PROCESSING_INSTRUCTION);
    		    put(3L, Type.TEXT);
    		}} );
  }

  // boolean forceComputation = false;
  // Stopwatch stopwatch = new Stopwatch();

  /**
   * This option forces the API to check the staleness of the document node before returning it to the callee. It may
   * have a significat price to pay in terms of performance though. In OXpath this is crucial, and will be disabled via
   * the proper options
   */
  protected boolean returnAlwaysFreshDocument = true;

  /**
   * Options for method DOMElement.getXPathLocator
   */
  protected boolean useIdAttributeForXPathLocator = true;
  protected boolean useClassAttributeForXPathLocator = true;

  protected boolean fallBackToJSExecutionOnNotInteractableElements;

  WebDriverWrapperFactory(final WebDriverBrowserImpl browserWrapper, Map<String, String> jsTemplateFunctionCalls) {
	this.jsTemplateFunctionCalls = jsTemplateFunctionCalls;
    this.browserWrapper = browserWrapper;
    driver = browserWrapper.driver_firefox;
    keyGeneratorForDomObservers = new AtomicInteger();

    try {

      final String commons_js = Resources.toString(Resources.getResource(WebDriverWrapperFactory.class, "commons.js"),
          Charsets.UTF_8);

      final String isshown_js = Resources.toString(
          Resources.getResource(WebDriverWrapperFactory.class, "isShown_mini.js"), Charsets.UTF_8);

      mutationSummary_script = Joiner.on("\n").join(
          commons_js,
          isshown_js,
          Resources.toString(Resources.getResource(WebDriverWrapperFactory.class, "mutationSummary_mini.js"),
              Charsets.UTF_8));

      functions_script = commons_js + "\n"
          + Resources.toString(Resources.getResource(WebDriverWrapperFactory.class, "functions.js"), Charsets.UTF_8);

      mutationEvent_script = commons_js
          + "\n"
          + Resources.toString(Resources.getResource(WebDriverWrapperFactory.class, "mutationObserverConf_mini.js"),
              Charset.defaultCharset());
    } catch (final IOException e) {
      throw new WebAPIRuntimeException("Error reading javascript resource files", e, LOGGER);
    }

  }

  @SuppressWarnings("unchecked")
  <T> T js(final FirefoxDriver driver, final boolean retryOnFailure, final String script, final Object... values) {
    try {
      // if (browserWrapper.collectStats && !browserWrapper.stopwatch.isRunning()) {
      // browserWrapper.stopwatch.start();
      // }
      return (T) ((JavascriptExecutor) driver).executeScript(script, values);
    } catch (final StaleElementReferenceException e) {
      throw new WebAPIStaleElementRuntimeException("Stale reference on page <" + browserWrapper.getLocationURL()
          + "> On script <" + StringUtils.left(script, 100) + "> EXCEPTION <" + e.getMessage() + ">", e, LOGGER);

    } catch (final UnhandledAlertException e) {
      LOGGER.warn("dismiss alert and reperform action JS command ");
      if (retryOnFailure)
        return js(driver, false, script, values);
      else
        throw new WebAPIFailedDismissModalDialogRuntimeException("Cannot dismiss modal dialog: " + e.getMessage(),
            LOGGER);
    } catch (final WebDriverException e) {
      throw new WebAPIJavascriptRuntimeException("On page <" + browserWrapper.getLocationURL() + "> On script <"
          + StringUtils.left(script, 100) + "> EXCEPTION <" + e.getMessage() + ">", e, LOGGER);
    } catch (final Exception e) {
      throw new WebAPIRuntimeException("On page <" + browserWrapper.getLocationURL() + "> On script <"
          + StringUtils.left(script, 100) + "> EXCEPTION <" + e.getMessage() + ">", e, LOGGER);

    } finally {
      // if (browserWrapper.collectStats && browserWrapper.stopwatch.isRunning()) {
      // browserWrapper.stopwatch.stop();
      // }
    }
  }

  <T> T callJS(final String functionName, final Object... values) {

    final String signature = jsTemplateFunctionCalls.get(functionName);
    if (signature == null) {
      LOGGER.error("Cannot find a signature to call js function '{}'", functionName);
      throw new WebAPIRuntimeException("Cannot find a signature to call js function: " + functionName, LOGGER);
    }

    if (browserWrapper.collectStats) {
      browserWrapper.recordStats(functionName);
    }
    LOGGER.debug("Invoking JS on <{}>", functionName);
    return callJSFromLib("//" + signature + "\n" + functions_script, signature, values);
    // return js(driver, true, functions_script + signature, values);
  }

  <T> T callJSFromLib(final String lib, final String signature, final Object... values) {

    return js(driver, true, lib + signature, values);
  }

  // private DOMNode wrapDocumentOrElement(final Object js) {
  // if (js == null)
  // return wrapDocument();
  // if (js instanceof WebElement) {
  // final WebElement p = (WebElement) js;
  // return wrapElement(p);
  // } else
  // throw new WebAPIRuntimeException("Cannot parse {" + js + "} to DOMNode", LOGGER);
  // }

  // public DOMDocumentWebDriverImpl wrapDocument() {
  // return new DOMDocumentWebDriverImpl(null, (WebElement) callJS("getDocumentElement"));
  // }

  public DOMDocumentWebDriverImpl wrapDocument(final WebElement documentElement) {

    return new DOMDocumentWebDriverImpl(null, documentElement);
  }

  /**
   * Get {@link DOMWindowWebDriverImpl} for the current Browser Window.
   * if shouldBeFresh=true, erase all existing mappings between {@link DOMWindowWebDriverImpl} and windowHandle create new instance of 
   * {@link DOMWindowWebDriverImpl} for the current browser window.
   * 
 * @param shouldBeFresh
 * @return
 */
public DOMWindowWebDriverImpl wrapWindow(final boolean shouldBeFresh) {

    final String windowHandle = driver.getWindowHandle();
    DOMWindowWebDriverImpl w = windows.get(windowHandle);
    if ((w == null) || shouldBeFresh) {
      windows.clear();// only one at a time for the time being
      w = new DOMWindowWebDriverImpl(browserWrapper, windowHandle);
      windows.put(windowHandle, w);
    }

    return w;
  }

  // private DOMElement wrapElement(final WebElement element) {
  // try {
  // return elementCache.get(element, new Callable<DOMElement>() {
  //
  // @Override
  // public DOMElement call() throws Exception {
  // LOGGER.trace("Missed cache");
  // return new DOMElementWrapWebDriverImpl<WebElement>(element);
  // }
  // });
  // } catch (final ExecutionException e) {
  // throw new WebAPIRuntimeException("Failed to retrieve an element from cache for {" + element + "} ", LOGGER);
  // }
  //
  // }

  private DOMElement wrapElement(final WebElement element) {

    return wrapElementOptimized(element, null, null, null);

  }

  private DOMElement wrapElementOptimized(final WebElement element, final WebElement documentElement,
      final String textContent, final String xpathLocator) {

    return new DOMElementWrapWebDriverImpl<WebElement>(element, documentElement, textContent, xpathLocator);

  }

  private final class MutationFormSummaryImpl implements MutationFormSummary {
    private final Map<?, ?> summaryMap;
    private Integer hash = null;

    private MutationFormSummaryImpl(final Map<?, ?> summaryMap) {
      // this.result = {
      // countNodesWithVisibilityChange = 0,
      // fieldsWithNewOptions : [],
      // //newElements : [],
      // newEnabledFields : [],
      // newDisabledFields : [],
      // appearedEnabledFields : [],
      // appearedDisabledFields : [],
      // enabledFields : [],
      // disabledFields : [],
      // // strings
      // textVaried : [],//strings
      // fieldsWithRemovedOptions : [],
      // //removedElements : [],
      // removedFields : [],
      // disappearedFields : [],
      // fieldsWithCSSChange : []
      //
      // };
      this.summaryMap = summaryMap;
    }

    @SuppressWarnings("unchecked")
    private List<List<Object>> castJSArray(final Object facts) {

      if (facts == null)
        return Lists.newArrayList();

      if ((facts instanceof List<?>))
        return (List<List<Object>>) facts;
      else {
        LOGGER.error("Cannot parse Javascript result for mutation summary. Unexpected type: {}", facts.getClass()
            .getName());
        throw new WebAPIJavascriptRuntimeException(
            "Cannot parse Javascript result for mutation summary. Unexpected type: " + facts.getClass().getName(),
            LOGGER);
      }

    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> parseTuples(final List<List<Object>> tuples) {
      final Set<T> elements = Sets.newHashSet();
      for (final List<Object> list : tuples) {
        elements.add((T) parseJavascriptNode(list));
      }
      return elements;
    }

    @Override
    public Set<DOMElement> getFieldsHavingNewOptions() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("fieldsWithNewOptions"));
      final Set<DOMElement> parseElements = parseTuples(tuples);
      return parseElements;
    }

    @Override
    public Set<DOMElement> getNewEnabledFields() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("newEnabledFields"));
      return parseTuples(tuples);
    }

    @Override
    public Set<DOMElement> getNewDisabledFields() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("newDisabledFields"));
      return parseTuples(tuples);
    }

    @Override
    public Set<DOMElement> getAppearedEnabledFields() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("appearedEnabledFields"));
      return parseTuples(tuples);
    }

    @Override
    public Set<DOMElement> getAppearedDisabledFields() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("appearedDisabledFields"));
      return parseTuples(tuples);
    }

    @Override
    public Set<DOMElement> getFieldswithCSSChange() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("fieldsWithCSSChange"));
      return parseTuples(tuples);
    }

    @Override
    public Set<String> getAppearedText() {
      @SuppressWarnings("unchecked")
      final List<String> tuples = (List<String>) summaryMap.get("textVaried");
      return Sets.newHashSet(tuples);
    }

    @Override
    public Long countElementsWithVisibilityChange() {
      return Long.valueOf(summaryMap.get("countElementsWithVisibilityChange").toString());
    }

    @Override
    public Set<DOMElement> getRemovedFields() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("removedFields"));
      return parseTuples(tuples);
    }

    @Override
    public Set<DOMElement> getDisappearedFields() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("disappearedFields"));
      return parseTuples(tuples);
    }

    @Override
    public Set<DOMElement> getFieldsHavingRemovedOptions() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("fieldsWithRemovedOptions"));
      return parseTuples(tuples);
    }

    @Override
    public Set<DOMElement> getEnabledFields() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("enabledFields"));
      return parseTuples(tuples);
    }

    @Override
    public Set<DOMElement> getDisabledFields() {
      final List<List<Object>> tuples = castJSArray(summaryMap.get("disabledFields"));
      return parseTuples(tuples);
    }

    @Override
    public int hashCode() {
      if (hash != null) {
        hash = toString().hashCode();
      }
      return hash;
    }

    @Override
    public boolean equals(final Object obj) {

      if (obj instanceof MutationFormSummary)
        return hashCode() == obj.hashCode();// NOTE: not safe I know, but ok for our cases
      return false;
    }

    @Override
    public String toString() {
      final ToStringHelper toStringBuilder = com.google.common.base.Objects.toStringHelper(getClass().getSimpleName());
      toStringBuilder.add("countElementsWithVisibilityChange", countElementsWithVisibilityChange());
      toStringBuilder.add("newEnabledFields", getNewEnabledFields());
      toStringBuilder.add("newDisabledFields", getNewDisabledFields());
      toStringBuilder.add("appearedEnabledFields", getAppearedEnabledFields());
      toStringBuilder.add("appearedDisabledFields", getAppearedDisabledFields());
      toStringBuilder.add("removedFields", getRemovedFields());
      toStringBuilder.add("disappearedFields", getDisappearedFields());
      toStringBuilder.add("enabledFields", getEnabledFields());
      toStringBuilder.add("disabledFields", getDisabledFields());
      toStringBuilder.add("textAppeared", getAppearedText());
      toStringBuilder.add("fieldsWithNewOptions", getFieldsHavingNewOptions());
      toStringBuilder.add("fieldsWithRemovedOptions", getFieldsHavingRemovedOptions());
      toStringBuilder.add("fieldsWithCSSChange", getFieldswithCSSChange());
      return toStringBuilder.toString();
    }

    @Override
    public boolean isFormChanged() {
      return !summaryMap.isEmpty();
    }

  }

  private class DOMCommentNodeWebDriverImpl extends DOMTextNodeWebDriverImpl {

    DOMCommentNodeWebDriverImpl(final WebElement parent, final String value, final String position,
        final WebElement docelement) {
      super(parent, value, position, docelement);
    }

    @Override
    public Type getNodeType() {
      return Type.COMMENT;
    }

    @Override
    public void setTextContent(final String text) {
      unsupported(this, "setTextContent");
    }

    @Override
    public String getNodeName() {
      return "#comment";
    }

    @Override
    public boolean isVisible() {
      return false;
    }

  }

  private class DOMTextNodeWebDriverImpl extends DOMNodeWebDriverImpl<WebElement> {

    // protected String value;
    protected final WebElement parent;
    protected Integer position = -1;

    DOMTextNodeWebDriverImpl(final WebElement parent, final String value, final String position) {
      this(parent, value, position, null);
    }

    DOMTextNodeWebDriverImpl(final WebElement parent, final String value, final String position,
        final WebElement documentElement) {
      this(parent, value, position, null, null);
    }

    DOMTextNodeWebDriverImpl(final WebElement parent, final String value, final String position,
        final WebElement documentElement, final String xpathLocator) {
      super(null, documentElement, value, xpathLocator);
      this.parent = parent;
      this.position = Integer.parseInt(position);
    }

    @Override
    public boolean isStale() {
      browserWrapper.recordStats("isStaleTextNode");
      if (parent == null)
        return false;
      else
        return getParentNode().isStale();
    }

    // @Override
    // public short compareDocumentPosition(final DOMNode other) {
    // // text to to text
    // if (other.getNodeType() == Type.TEXT) {
    // final DOMTextNodeWebDriverImpl textNode = (DOMTextNodeWebDriverImpl) other;
    // final Long longValue = callJS("compareDocumentPositionTextToText", parent, position, textNode.parent,
    // textNode.position);
    // return longValue.shortValue();
    // }
    // // text to element
    // if (other.getNodeType() == Type.ELEMENT) {
    // final Long longValue = callJS("compareDocumentPositionTextToElement", parent, position,
    // castToElementAndGetWrappedNode(other));
    // return longValue.shortValue();
    // }
    //
    // if (other.getNodeType() == Type.DOCUMENT) {
    // final Long longValue = callJS("compareDocumentPositionTextToElement", parent, position,
    // castToDocumentAndGetWrappedNode(other));
    // return longValue.shortValue();
    // }
    //
    // unsupported(this.getClass(), "compareDocumentPosition for " + other.getNodeType());
    // return -1;
    // }

    // @Override
    // public DOMNode getPreviousSibling() {
    // final Object js = callJS("getPreviousSiblingOfTextNode", parent, position);
    // return wrapDOMNode(js);
    // }

    // @Override
    // public DOMNode getNextSibling() {
    // final Object js = callJS("getNextSiblingOfTextNode", parent, position);
    // return wrapDOMNode(js);
    // }

    // @Override
    // public boolean isSameNode(final DOMNode other) {
    // if (other == null)
    // return false;
    // if (other.getNodeType() != getNodeType())
    // return false;
    // // delegate to JS js
    // final DOMTextNodeWebDriverImpl otherText = (DOMTextNodeWebDriverImpl) other;
    // return WebDriverWrapperFactory.this.<Boolean> callJS("isSameTextNode", parent, position, otherText.parent,
    // otherText.position);
    // }

    // @Override
    // public boolean isEqualNode(final DOMNode other) {
    // if (other == null)
    // return false;
    // if (other.getNodeType() != getNodeType())
    // return false;
    // final DOMTextNodeWebDriverImpl otherText = (DOMTextNodeWebDriverImpl) other;
    // return WebDriverWrapperFactory.this.<Boolean> callJS("isEqualTextNode", parent, position, otherText.parent,
    // otherText.position);
    // }

    @Override
    public DOMNode getFirstChild() {
      return null;
    }

    @Override
    public DOMNode getLastChild() {
      return null;
    }

    @Override
    public DOMNode removeChild(final DOMNode child) {
      throw new WebAPIRuntimeException("Error: child node not found", LOG);
    }

    @Override
    public DOMNode appendChild(final DOMNode newChild) {
      throw new WebAPIRuntimeException("Error: Node cannot be inserted at the specified point in the hierarchy", LOG);
    }

    @Override
    public DOMNode replaceChild(final DOMNode newChild, final DOMNode oldChild) {
      throw new WebAPIRuntimeException("Error: Node cannot be inserted at the specified point in the hierarchy", LOG);
    }

    @Override
    public DOMNode insertBefore(final DOMNode newChild, final DOMNode refChild) {
      throw new WebAPIRuntimeException("Error: Node cannot be inserted at the specified point in the hierarchy", LOG);
    }

    // @Override
    // public void setTextContent(final String text) {
    // value = callJS("setTextContentForTextNode", parent, position, text);
    //
    // }

    @Override
    public DOMNode getParentNode() {
      browserWrapper.recordStats("getParentNodeText");
      if (parent == null)
        return null;
      return wrapElement(parent);
    }

    @Override
    public Type getNodeType() {
      return Type.TEXT;
    }

    @Override
    public DOMNodeList getChildNodes() {
      return buildEmptyDomList();
    }

    @Override
    public String getNodeValue() {
      return textContent;
    }

    @Override
    public String getLocalName() {
      return null;
    }

    @Override
    public DOMNamedNodeMap<DOMNode> getAttributes() {
      return null;
    }

    @Override
    public String getNodeName() {
      return "#text";
    }

    @Override
    public String toPrettyHTML() {

      return getNodeName() + " : " + textContent;
    }

    // @Override
    // public String getTextContent() {
    // return value;
    // }

    @Override
    public boolean isVisible() {
      if (getParentNode() == null)
        return false;
      return getParentNode().isVisible();
      // return parent.isDisplayed();
    }

    @Override
    public String toString() {
      return getNodeName() + "[" + position + "]" + " - content:" + textContent;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + getOuterType().hashCode();
      result = (prime * result) + ((parent == null) ? 0 : parent.hashCode());
      result = (prime * result) + ((position == null) ? 0 : position.hashCode());
      result = (prime * result) + ((textContent == null) ? 0 : textContent.hashCode());
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
      final DOMTextNodeWebDriverImpl other = (DOMTextNodeWebDriverImpl) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (parent == null) {
        if (other.parent != null)
          return false;
      } else if (!parent.equals(other.parent))
        return false;
      if (position == null) {
        if (other.position != null)
          return false;
      } else if (!position.equals(other.position))
        return false;
      if (textContent == null) {
        if (other.textContent != null)
          return false;
      } else if (!textContent.equals(other.textContent))
        return false;
      return true;
    }

  }

  private class DOMNodeWebDriverImpl<N extends WebElement> extends AbstractNodeWrapper<WebElement> {

    protected WebElement wrappedElement;
    // TODO thos should be fine as it's bound to the wrappedElement
    protected DOMDocumentWebDriverImpl ownerDocument = null;
    protected String textContent;
    protected String xpathLocator;
    private String asToString;

    DOMNodeWebDriverImpl(final WebElement wrappedElement) {
      super();
      this.wrappedElement = wrappedElement;
    }

    DOMNodeWebDriverImpl(final WebElement wrappedElement, final WebElement documentElement, final String textContent,
        final String xpathLocator) {
      this(wrappedElement);
      this.xpathLocator = xpathLocator;
      this.ownerDocument = wrapDocument(documentElement);
      this.textContent = textContent;
    }

    DOMNodeWebDriverImpl(final WebElement wrappedElement, final WebElement documentElement, final String textContent) {
      this(wrappedElement, documentElement, textContent, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getDOMPropery(java.lang.String)
     */
    @Override
    public String getDOMProperty(final String domProperty) {
      final Object value = callJS("getProperty", toJS(this), domProperty);
      return value == null ? null : value.toString();
      // return callJS("getProperty", toJS(this));
    }

    @Override
    public WebBrowser getBrowser() {

      return browserWrapper;
    }

    @Override
    public boolean isStale() {
      browserWrapper.recordStats("isStaleDomNode");
      return verifyStaleness(this.wrappedElement);
    }

    protected Boolean verifyStaleness(final WebElement element) {
      try {
        return ExpectedConditions.stalenessOf(element).apply(driver);
      } catch (final WebDriverException e) {// bug of selenuim https://code.google.com/p/selenium/issues/detail?id=3544
        LOGGER.warn("Error checking staleness, return stale=true. Error <{}>", e.getMessage());
        return true;
      }
    }

    protected WebDriverWrapperFactory getOuterType() {
      return WebDriverWrapperFactory.this;
    }

    @Override
    public String toString() {
      if (asToString == null) {
        browserWrapper.recordStats("toString-->TagName");
        final String tagName = wrappedElement.getTagName();
        asToString = (tagName == null ? wrappedElement.toString() : tagName);
      }
      return asToString;
    }

    protected String toStringDebug() {
      return "[node: <" + getXPathLocator() + "> on page: " + getBrowser().getLocationURL() + "]";
    }

    @Override
    public String toPrettyString() {
      return callJS("prettyToString", toJS(this));
    }

    /**
     * @return
     */
    protected List<Object> thisToJS() {
      return toJS(this);
    }

    @Override
    public Object executeJavaScript(final String script, final Object... arg) {
      return js(driver, false, script, wrappedElement, arg);
    }

    @Override
    public DOMMutationObserver registerMutationObserver(final boolean childList, final boolean attributes,
        final boolean subtree, final boolean characterData, final List<String> attributeFilter) {
      try {
        final Integer key = getNextKeyForObserver();

        // var config = {childList: arguments[1], attributes:arguments[2], subtree:arguments[3], characterData:
        // arguments[4], attributeFilter:arguments[5]};
        // var target = arguments[0];
        // var integerKey=arguments[6]
        final Object res = driver.executeScript(mutationEvent_script, wrappedElement, childList, attributes, subtree,
            characterData, attributeFilter, key);

        // check if ok
        if (!((Boolean) res).equals(Boolean.TRUE))
          throw new WebAPIRuntimeException("Error in the script for registering a DOMMutationObserver", LOG);

        return new DOMMutationObserver() {

          @Override
          public Set<DOMMutationRecord> takeRecords() {

            // returns the records and reset the accumulator
            // final Object records = driver
            // .executeScript(
            // "if (typeof window.mutated === 'undefined') return []; if (typeof window.mutated[arguments[0]] === 'undefined') return []; var records = window.mutated[arguments[0]]; window.mutated[arguments[0]]=[]; return records;",
            // key);
            final Object records = callJS("takeRecords", key);
            return parseMutationRecords(records);
          }

          @Override
          public void disconnect() {
            // driver
            // .executeScript(
            // "if (typeof window.observerArray === 'undefined') return; if (typeof window.observerArray[arguments[0]] === 'undefined') return; window.observerArray[arguments[0]].disconnect()",
            // key);
            callJS("disconnect", key);
          }
        };
      } catch (final Exception e) {
        throw new WebAPIRuntimeException("Error reading resource file mutationObserverConf.js", e, LOG);
      }
    }

    @Override
    public DOMMutationObserver registerMutationObserver(final MutationObserverOptions options) {
      return registerMutationObserver(options.childList(), options.attributes(), options.subtree(),
          options.characterData(), options.attributeFilter());
    }

    @SuppressWarnings("unchecked")
    private Set<DOMMutationRecord> parseMutationRecords(final Object recordObjects) {
      final Set<DOMMutationRecord> result = Sets.newLinkedHashSet();
      final ArrayList<ArrayList<Object>> records = (ArrayList<ArrayList<Object>>) recordObjects;
      if (records == null) {
        LOGGER.error("Got Null from Js xpath_script for mutation events");
        return result;
      }

      for (final ArrayList<Object> singleRecord : records) {

        final DOMNode target = wrapDOMNode(singleRecord.get(1));
        // if (singleRecord.get(1) instanceof WebElement) {
        // target = wrapElement((WebElement) singleRecord.get(1));
        // } else {
        // // here is a text node
        // target = parseJavascriptNode(singleRecord.subList(1, singleRecord.size() - 1));
        // }

        // ChildListModRecord[type, tnode, addedNodes, removedNodes]
        if (singleRecord.get(0).toString().equals(MutationType.childList.name())) {

          final DOMMutationRecord mut = new ChildListDOMMutationRecordImpl(MutationType.childList, target,
              singleRecord.get(2), singleRecord.get(3));

          result.add(mut);
        } else if (singleRecord.get(0).toString().equals(MutationType.attributes.name())) {
          final DOMMutationRecord mut = new AttributeDOMMutationRecordImpl(MutationType.attributes, target,
              singleRecord.get(2), singleRecord.get(3), singleRecord.get(4));
          result.add(mut);
        } else
          // DataModRecord[type, targetNode, oldValue,newValue];
          if (singleRecord.get(0).toString().equals(MutationType.characterData.name())) {
            final DOMMutationRecord mut = new CharDataDOMMutationRecordImpl(MutationType.characterData, target,
                singleRecord.get(2), singleRecord.get(3));
            result.add(mut);
          } else
            throw new WebAPIRuntimeException("Error parsing  mutation event of unknown type: " + singleRecord.get(0), LOG);

      }

      return result;
    }

    @Override
    public short compareDocumentPosition(final DOMNode other) {
      final Long longValue = callJS("compareDocumentPosition", toJS(this), toJS(other));
      return longValue.shortValue();
    }

    @Override
    WebElement getWrappedNode() {
      return wrappedElement;
    }

    @Override
    public void addEventListener(final String type, final DOMEventListener listener, final boolean useCapture) {
      unsupported(this, "addEventListener");

    }

    @Override
    public void removeEventListener(final String type, final DOMEventListener listener, final boolean useCapture) {
      unsupported(this, "removeEventListener");

    }

    @Override
    public Type getNodeType() {
      final Long js = callJS("getNodeType", toJS(this));
      return nodeTypes.get(js);
    }

    @Override
    public String getXPathLocator() {
      if (xpathLocator == null) {
        xpathLocator = callJS("getXPathLocator", toJS(this), useIdAttributeForXPathLocator,
            useClassAttributeForXPathLocator);
      }
      return xpathLocator;
    }

    @Override
    public DOMNodeList getChildNodes() {
      // final List<Object> inputs = (List<Object>) ((JavascriptExecutor) driver).executeScript(XX, element);
      // System.out.println(inputs);
      final List<Object> inputs = WebDriverWrapperFactory.this.<List<Object>> callJS("getChildNodes", toJS(this));
      return buildMixedNodeList(inputs);
    }

    @Override
    public String getNodeValue() {
      return callJS("getNodeValue", toJS(this));
    }

    @Override
    public DOMNode getParentNode() {

      final Object js = callJS("getParentNode", toJS(this));

      return wrapDOMNode(js);
    }

    @Override
    public String getLocalName() {
      return callJS("getLocalName", toJS(this));
    }

    @Override
    public DOMNamedNodeMap<DOMNode> getAttributes() {
      final List<List<Object>> attrs = WebDriverWrapperFactory.this.<List<List<Object>>> callJS("getAttributes",
          toJS(this));
      if (attrs == null)
        return null;

      return parseAttributes(attrs);
    }

    private DOMNamedNodeMap<DOMNode> parseAttributes(final List<List<Object>> inputs) {
      final LinkedHashMap<String, String> attrs = Maps.newLinkedHashMap();
      for (final List<Object> attributeTuple : inputs) {
        // [attr.ownerElement, attr.localName, attr.nodeValue, attr.nodeType];
        // map name -->value
        attrs.put(attributeTuple.get(1).toString(), attributeTuple.get(2).toString());
      }

      return new DOMNamedNodeMap<DOMNode>() {

        @Override
        public long getLength() {
          return 0;
        }

        @Override
        public DOMNode item(final int i) {
          final Iterator<String> attrKeys = attrs.keySet().iterator();
          if (i > Iterators.size(attrKeys))
            return null;
          final String name = Iterators.get(attrKeys, 0);
          return wrapAttribute(name, attrs.get(name));
        }

        @Override
        public DOMNode getNamedItem(final String name) {
          return wrapAttribute(name, attrs.get(name));
        }
      };

    }

    protected DOMNode wrapAttribute(final String name, final String string) {
      if ((name == null) || (string == null))
        return null;
      return new AttributeNodeImpl(Pair.of(name, string), this);
    }

    @Override
    public String getNodeName() {
      browserWrapper.recordStats("getNodeName-->tagName");
      // return callJS("getNodeName", toJS(this));
      return wrappedElement.getTagName();
    }

    @Override
    public String toPrettyHTML() {

      return wrappedElement.getTagName();
    }

    @Override
    public String getTextContent() {
      if (textContent != null)
        return textContent;
      return callJS("getTextContent", toJS(this));
    }

    @Override
    public boolean isVisible() {
      return wrappedElement.isDisplayed();
    }

    @Override
    public DOMXPathEvaluator getXPathEvaluator() {
      return new DOMXPathEvaluatorOnWebDriverImpl();
    }

    @Override
    public DOMDocument getOwnerDocument() {

      if ((this.ownerDocument == null) || (returnAlwaysFreshDocument && this.ownerDocument.isStale())) {
        browserWrapper.recordStats("via getOwnerDocument");
        ownerDocument = wrapDocument(null);
      }
      return ownerDocument;
    }

    @Override
    public boolean isEqualNode(final DOMNode other) {
      if (other == null)
        return false;
      if (other.getNodeType() != getNodeType())
        return false;
      // delegate to JS js
      return WebDriverWrapperFactory.this.<Boolean> callJS("isEqualNode", toJS(this), toJS(other));
    }

    @Override
    public boolean isSameNode(final DOMNode other) {
      if (other == null)
        return false;
      if (other.getNodeType() != getNodeType())
        return false;
      // delegate to JS js
      return WebDriverWrapperFactory.this.<Boolean> callJS("isSameNode", toJS(this), toJS(other));
    }

    @Override
    public DOMNode appendChild(final DOMNode newChild) {

      // if (newChild.getNodeType() == Type.ELEMENT) {
      // final WebElement js = callJS("appendChild", nodeToJavascript(this), nodeToJavascript(newChild));
      // return wrapElement(js);
      // }
      // if (newChild.getNodeType() == Type.TEXT) {
      // final DOMTextNodeWebDriverImpl text = (DOMTextNodeWebDriverImpl) newChild;
      // final Object js = callJS("appendTextChild", wrappedElement, text.parent, text.position);
      //
      // return wrapDOMNode(js);
      // }
      //
      // unsupported(this, "appendChild for " + newChild.getNodeType());
      // return null;

      final Object js = callJS("appendChild", toJS(this), toJS(newChild));
      return wrapDOMNode(js);

    }

    @Override
    public DOMNode removeChild(final DOMNode child) {
      // if (child.getNodeType() == Type.ELEMENT) {
      // final WebElement js = callJS("removeChild", wrappedElement, castToElementAndGetWrappedNode(child));
      // return wrapElement(js);
      // }
      //
      // if (child.getNodeType() == Type.TEXT) {
      // final DOMTextNodeWebDriverImpl text = (DOMTextNodeWebDriverImpl) child;
      // final Boolean result = WebDriverWrapperFactory.this.<Boolean> callJS("removeTextChild", wrappedElement,
      // text.parent, text.position);
      // if (result)
      // return new DOMTextNodeWebDriverImpl(null, text.value);
      // else
      // throw new WebAPIRuntimeException("Cannot find node to remove " + child, LOGGER);
      // }
      //
      // unsupported(this, "removeChild for" + child);
      // return null;

      final Object js = callJS("removeChild", toJS(this), toJS(child));
      return wrapDOMNode(js);

    }

    @Override
    public DOMNode replaceChild(final DOMNode newChild, final DOMNode oldChild) {

      unsupported(this, "replaceChild");
      return null;
    }

    @Override
    public DOMNode insertBefore(final DOMNode newChild, final DOMNode refChild) {
      final Object js = callJS("insertBefore", toJS(this), toJS(newChild), toJS(refChild));
      return wrapDOMNode(js);
      // if (newChild.getNodeType() == Type.ELEMENT)
      // if (refChild.getNodeType() == Type.ELEMENT) {
      // final WebElement js = callJS("insertBefore", wrappedElement, castToElementAndGetWrappedNode(newChild),
      // castToElementAndGetWrappedNode(refChild));
      // return wrapElement(js);
      // } else if (refChild.getNodeType() == Type.TEXT) {
      // final DOMTextNodeWebDriverImpl text = (DOMTextNodeWebDriverImpl) refChild;
      // final WebElement js = callJS("insertBeforeText", wrappedElement, castToElementAndGetWrappedNode(newChild),
      // text.position);
      // return wrapElement(js);
      // }
      // if (newChild.getNodeType() == Type.TEXT) {
      //
      // final DOMTextNodeWebDriverImpl text = (DOMTextNodeWebDriverImpl) newChild;
      // if (refChild.getNodeType() == Type.ELEMENT) {
      // final Object js = callJS("insertTextBeforeElement", wrappedElement, text.parent, text.position,
      // castToElementAndGetWrappedNode(refChild));
      // return wrapDOMNode(js);
      // } else if (refChild.getNodeType() == Type.TEXT) {
      // final DOMTextNodeWebDriverImpl textRef = (DOMTextNodeWebDriverImpl) refChild;
      // final Object js = callJS("insertTextBeforeText", wrappedElement, text.parent, text.position, textRef.parent,
      // textRef.position);
      // return wrapDOMNode(js);
      // }
      //
      // }
      //
      // unsupported(this, "appendChild for " + newChild.getNodeType());
      // return null;
    }

    @Override
    public void setTextContent(final String text) {
      callJS("setTextContent", toJS(this), text);

    }

    @Override
    public DOMNode getPreviousSibling() {
      final Object js = callJS("getPreviousSibling", toJS(this));
      return wrapDOMNode(js);
    }

    @Override
    public DOMNode getNextSibling() {
      final Object js = callJS("getNextSibling", toJS(this));
      return wrapDOMNode(js);
    }

    @Override
    public DOMNode getLastChild() {
      final Object js = callJS("getLastChild", toJS(this));
      return wrapDOMNode(js);
    }

    @Override
    public DOMNode getFirstChild() {
      final Object js = callJS("getFirstChild", toJS(this));
      return wrapDOMNode(js);
    }

    @Override
    public boolean isTextNode() {
      return getNodeType() == Type.TEXT;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + getOuterType().hashCode();
      result = (prime * result) + (wrappedElement == null ? 0 : wrappedElement.hashCode());
      return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final DOMNodeWebDriverImpl<WebElement> other = (DOMNodeWebDriverImpl<WebElement>) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (wrappedElement == null) {
        if (other.wrappedElement != null)
          return false;
      } else if (!wrappedElement.equals(other.wrappedElement))
        return false;
      return true;
    }

  }

  private class DOMElementWrapWebDriverImpl<N extends WebElement> extends DOMNodeWebDriverImpl<WebElement> implements
  DOmElementOnJS {

    // all things to be later moved in a proper cache //TODO

    private DOMBoundingClientRect cachedBoundingBox;

    DOMElementWrapWebDriverImpl(final WebElement wrappedElement, final WebElement documentElement,
        final String textContent, final String xpathLocator) {
      super(wrappedElement, documentElement, textContent, xpathLocator);
    }

    DOMElementWrapWebDriverImpl(final WebElement wrappedElement, final WebElement documentElement,
        final String textContent) {
      this(wrappedElement, documentElement, textContent, null);
    }

    @Override
    public CSSMutationObserver observeCSSProperties(final CssProperty... properties) {
      final List<String> propString = Lists.transform(Arrays.asList(properties), new Function<CssProperty, String>() {

        @Override
        public String apply(final CssProperty input) {

          return input.getPropertyName();
        }
      });

      final List<Object> thisToJS = toJS(this);
      final Integer index = getNextKeyForObserver();

      callJS("observeCSSProperties", thisToJS, propString, index);

      return new CSSMutationObserver() {

        @SuppressWarnings("unchecked")
        @Override
        public Set<CSSMutationRecord> takeRecords() {

          final Object records = callJS("takeCSSRecords", thisToJS, index);
          return parseCSSMutationRecords((ArrayList<ArrayList<Object>>) records);
        }

        private Set<CSSMutationRecord> parseCSSMutationRecords(final ArrayList<ArrayList<Object>> records) {
          // var CSSModRecord = function(property, newValue, oldValue) {return [property, newValue, oldValue];};
          final Set<CSSMutationRecord> res = Sets.newHashSet();
          for (final ArrayList<Object> record : records) {
            res.add(new CSSMutationRecord() {

              @Override
              public int hashCode() {
                return Objects.hash(record);
              };

              @Override
              public boolean equals(final Object obj) {
                if (obj instanceof CSSMutationRecord) {
                  final CSSMutationRecord other = (CSSMutationRecord) obj;
                  return Objects.equals(getProperty(), other.getProperty())
                      && Objects.equals(getNewValue(), other.getNewValue())
                      && Objects.equals(getOldValue(), other.getOldValue());
                } else
                  return false;
              };

              @Override
              public String toString() {
                return Objects.toString(record);
              };

              @Override
              public CssProperty getProperty() {

                return CssProperty.valueOf(record.get(0).toString());
              }

              @Override
              public String getOldValue() {
                return record.get(2).toString();
              }

              @Override
              public String getNewValue() {
                return record.get(1).toString();
              }
            });
          }

          return res;
        }
      };
    }

    // @Override
    // public short compareDocumentPosition(final DOMNode other) {
    // // element to element
    // if (other.getNodeType() == Type.ELEMENT) {
    // final Long longValue = callJS("compareDocumentPosition", wrappedElement, castToElementAndGetWrappedNode(other));
    // return longValue.shortValue();
    // }
    // // element to text
    // if (other.getNodeType() == Type.TEXT) {
    // final DOMTextNodeWebDriverImpl otherTextNode = (DOMTextNodeWebDriverImpl) other;
    // final Long longValue = callJS("compareDocumentPositionElementToText", wrappedElement, otherTextNode.parent,
    // otherTextNode.position);
    // return longValue.shortValue();
    // }
    // if (other.getNodeType() == Type.DOCUMENT) {
    // final Long longValue = callJS("compareDocumentPositionElementToDoc", wrappedElement,
    // castToDocumentAndGetWrappedNode(other));
    // return longValue.shortValue();
    // }
    // unsupported(this.getClass(), "compareDocumentPosition for " + other.getNodeType());
    // return -1;
    // }

    public Object getWrappedElement() {
      return getWrappedNode();
    }

    @Override
    public Type getNodeType() {
      return Type.ELEMENT;
    }

    @Override
    public DOMElement querySelector(final String selectors) {

      return wrapElement(wrappedElement.findElement(By.cssSelector(selectors)));
    }

    @Override
    public DOMNodeList querySelectorAll(final String selectors) {
      final List<WebElement> list = wrappedElement.findElements(By.cssSelector(selectors));
      return buildNodeList(list);
    }

    @Override
    public boolean isVisible() {
      final boolean displayed = wrappedElement.isDisplayed();
      if (!displayed)
        return false;
      final DOMBoundingClientRect rect = getBoundingClientRect();
      if ((rect.getWidth() > 0) && (rect.getHeight() > 0))
        return true;
      return false;
    }

    @Override
    public DOMCSSStyleDeclaration getComputedStyle() {
      return new DOMCSSStyleDeclaration() {

        @Override
        public void setProperty(final String name, final String value) {
          // convert in lower camel case
          callJS("setCSSProperty", toJS(DOMElementWrapWebDriverImpl.this), normalize(name), value);
        }

        private String normalize(final String name) {
          if (name.startsWith("-"))
            return "-" + CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, name.substring(1));
          return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, name);
        }

        @Override
        public String getPropertyValue(final String string) {

          return wrappedElement.getCssValue(string);
        }

        @Override
        public void setProperties(final Map<String, String> properties) {
          final ArrayList<Object> names = Lists.newArrayList();
          final ArrayList<Object> values = Lists.newArrayList();
          final Set<String> keySet = properties.keySet();
          for (final String name : keySet) {
            names.add(normalize(name));
            values.add(properties.get(name));
          }
          callJS("setCSSProperties", toJS(DOMElementWrapWebDriverImpl.this), names, values);
        }

      };
    }

    @Override
    public DOMBoundingClientRect getBoundingClientRect() {

      if (cachedBoundingBox != null) {
        LOGGER.trace("Using cached BoundingBox");
        return cachedBoundingBox;
      }

      final List<Object> box = WebDriverWrapperFactory.this.<List<Object>> callJS("getBoundingBox", toJS(this));
      // cachedBoundingBox =
      return new DOMBoundingClientRect() {

        @Override
        public float getLeft() {
          return Float.parseFloat(box.get(0).toString());
        }

        @Override
        public float getTop() {

          return Float.parseFloat(box.get(1).toString());
        }

        @Override
        public float getRight() {
          return Float.parseFloat(box.get(2).toString());
        }

        @Override
        public float getBottom() {
          return Float.parseFloat(box.get(3).toString());
        }

        @Override
        public float getWidth() {

          return Float.parseFloat(box.get(4).toString());
        }

        @Override
        public float getHeight() {
          return Float.parseFloat(box.get(5).toString());
        }

        @Override
        public String toString() {
          // TODO Auto-generated method stub
          return "top " + getTop() + " left " + getLeft() + " bottom " + getBottom() + " right " + getRight()
              + " width " + getWidth() + " height " + getHeight();
        }

      };

      // cachedBoundingBox = new DOMBoundingClientRect() {
      //
      // @Override
      // public float getWidth() {
      //
      // return wrappedElement.getSize().getWidth();
      // }
      //
      // @Override
      // public float getTop() {
      //
      // return wrappedElement.getLocation().getY();
      // }
      //
      // @Override
      // public float getRight() {
      // return getLeft() + getWidth();
      // }
      //
      // @Override
      // public float getLeft() {
      // return wrappedElement.getLocation().getX();
      // }
      //
      // @Override
      // public float getHeight() {
      // return wrappedElement.getSize().getHeight();
      // }
      //
      // @Override
      // public float getBottom() {
      // return getTop() + getHeight();
      // }
      // };

    }

    @Override
    public void setAttribute(final String name, final String value) {
      callJS("setAttribute", toJS(this), name, value);
    }

    @Override
    public List<DOMElement> getNeighbourhood(final int radius) {

      final List<Object> neigh = WebDriverWrapperFactory.this.<List<Object>> callJS("getNeighbourhood", toJS(this),
          radius);
      return Lists.transform(neigh, new Function<Object, DOMElement>() {

        @Override
        public DOMElement apply(final Object input) {
          return wrapElement((WebElement) input);
        }
      });

    }

    @Override
    public void removeAttribute(final String name) {
      callJS("removeAttribute", toJS(this), name);
    }

    @Override
    public String getAttribute(final String name) {
      browserWrapper.recordStats("getAttribute");
      return wrappedElement.getAttribute(name);
    }

    void checkActionPreconditionAndThrowIfNecessary(final String action) {
      if (wrappedElement == null)
        throw new WebAPIRuntimeException("Cannot <" + action + "> on a null element", LOG);
      if (!enabled(wrappedElement))
        throw new WebAPINotVisibleOrDisableElementException("Cannot <" + action + "> [" + toStringDebug()
            + "] as not enabled", LOG);
    }

    private boolean enabled(final WebElement element) {

      // the same of ExpectedConditions.elementToBeClickable(locator)
      return element.isEnabled();
    }

    @Override
    public boolean isEnabled() {
      browserWrapper.recordStats("isEnabled");
      return wrappedElement.isEnabled();
    }

    @Override
    public DOMWindow clickJS() {
      LOGGER.debug("performing a click direcly in javascript on {}", this);
      callJS("clickOnElement", thisToJS());
      return wrapWindow(true);

    }

    @Override
    public boolean isAncestorOf(final String... xpathLocators) {
      if (xpathLocators.length == 0)
        return false;
      return WebDriverWrapperFactory.this.<Boolean> callJS("isAncestorOf", thisToJS(), xpathLocators);
    }

    @Override
    public DOMWindow click() {
      try {
        final String url = driver.getCurrentUrl();
        click(true);
        triggerUnhandledAlertExceptionAfterActionIfAny();
        boolean forceNewDocument = false;
        if (!driver.getCurrentUrl().equals(url) || isStale()) {
          browserWrapper.pages++;
          forceNewDocument = true;
        }
        return wrapWindow(forceNewDocument);

      } catch (final UnhandledAlertException e) {
        LOGGER.warn("dismissing alert after click <{}>", e.getMessage());
        return wrapWindow(true);
      }
    }

    @Override
    // TODO use only one method CLICK
    public boolean click(final boolean waitUntilLoaded) {

      checkActionPreconditionAndThrowIfNecessary("click");

      try {
    	  // clicking on visible elements with the use of WebDriver's API
        wrappedElement.click();
        // new Actions(driver).click(wrappedElement);
        triggerUnhandledAlertExceptionAfterActionIfAny();
      } catch (final ElementNotVisibleException e) {
        if (!fallBackToJSExecutionOnNotInteractableElements)
          throw new WebAPINotVisibleOrDisableElementException("Cannot click on the invisible element "
              + toStringDebug(), e, LOGGER);
        else {
          LOGGER.error("cannot click on not visible element <{}>. Fall back to javascript action", this);
          clickJS();
        }
      } catch (final UnhandledAlertException e) {
        LOGGER.warn("dismissing alert after click <{}>", e.getMessage());

      }
      return true;

    }

    @Override
    public DOMWindow moveToFrame() {

      checkIfFrame("moveToFrame", getWrappedNode());

      final String urlFrame = callJS("getProperty", toJS(this), "src");
      if (urlFrame == null) {
        LOGGER.error("Cannot retrieve url from src property for element ", this);
        throw new WebAPIRuntimeException("Cannot retrieve url from src property for element " + this, LOGGER);
      }
      LOGGER.info("Navigating to frame url {}", urlFrame);
      // navigate to the iframe
      browserWrapper.navigate(urlFrame);

      return wrapWindow(true);
    }

    @Override
    public DOMWindow moveToHREF() {

      final String urlHref = callJS("getProperty", toJS(this), "hrdsdsef");
      if (urlHref == null) {
        LOGGER.error("Cannot retrieve url from href property for element ", this);
        throw new WebAPIRuntimeException("Cannot retrieve url from src property for element " + this, LOGGER);
      }
      LOGGER.info("Navigating to url {}", urlHref);
      browserWrapper.navigate(urlHref);

      return wrapWindow(true);
    }

    @Override
    public DOMWindow fireMouseEvent(final String event) {
      unsupported(this, "fireMouseEvent:" + event);
      return null;
    }

    @Override
    public DOMWindow fireFocusEvent(final String event) {
      if ("focus".equals(event))
        return focus();

      unsupported(this, "fireFocusEvent: " + event);
      return null;
    }

    @Override
    public DOMWindow fireKeyboardEvent(final String event, final char printableChar) {
      unsupported(this, "fireKeyboardEvent:" + event + " " + printableChar);
      return null;
    }

    @Override
    public DOMWindow type(final String content) {
      checkActionPreconditionAndThrowIfNecessary("type");

      try {
        wrappedElement.click();// this can throw UnhandledAlertException
        wrappedElement.sendKeys(content);
        triggerUnhandledAlertExceptionAfterActionIfAny();
        // Hack? since focus may be required?
        // Make the CTRL a optional?
        // new Actions(driver).sendKeys(content).perform();
      } catch (final ElementNotVisibleException e) {
        if (!fallBackToJSExecutionOnNotInteractableElements)

          throw new WebAPINotVisibleOrDisableElementException("Cannot <type> the value " + content
              + " on the invisible element: " + toStringDebug(), e, LOGGER);
        else {
          LOGGER.error("cannot type the value <{}> on not visible element <{}>. Fall back to javascript action",
              content, this);
          callJS("setValue", toJS(this), content);
        }
      } catch (final UnhandledAlertException e) {
        LOGGER.warn("dismissing alert after <typeAndEnter> . <{}>", e.getMessage());
      }
      return wrapWindow(true);
    }

    @Override
    public DOMWindow typeAndEnter(final String content) {

      checkActionPreconditionAndThrowIfNecessary("typeAndEnter");

      try {
        wrappedElement.click();
        wrappedElement.sendKeys(content, Keys.RETURN);
        triggerUnhandledAlertExceptionAfterActionIfAny();
        // wrappedElement.getTagName();
        // Hack? since focus may be required?
        // Make the CTRL a optional?
        // new Actions(driver).sendKeys(content).perform();
      } catch (final ElementNotVisibleException e) {
        if (!fallBackToJSExecutionOnNotInteractableElements)

          throw new WebAPINotVisibleOrDisableElementException("Cannot <typeAndEnter> the value " + content
              + " on the invisible element: " + toStringDebug(), e, LOGGER);
        else {
          LOGGER.error(
              "cannot typeAndEnter the value <{}> on not visible element <{}>. Fall back to javascript action",
              content, this);

          // callJS("setValue", toJS(this), content);
          throw new WebAPIUnsupportedJavascriptActionRuntimeException("Unsupported typeAndEnter action via Javascript",
              LOGGER);

        }

      } catch (final UnhandledAlertException e) {
        LOGGER.warn("dismissing alert after <typeAndEnter> . <{}>", e.getMessage());
      }

      return wrapWindow(true);
    }

    @Override
    public String getInnerHTML() {
      return callJS("getInnerHTML", toJS(this));
    }

    @Override
    public String getOuterHTML() {
      return callJS("getOuterHTML", toJS(this));
    }

    @Override
    public HTMLUtil htmlUtil() {
      final WebElement ownDocEl = ownerDocument == null ? null : ownerDocument.documentElement;
      browserWrapper.recordStats("htmlUtil-->tagName");
      return new HTMLUtil() {

        @Override
        public DOMTypeableElement asTypeableElement() {
          final DOMHTMLInputElement element = asHTMLInputElement();

          return element == null ? asHTMLTextArea() : element;
        }

        @Override
        public DOMHTMLSelect asHTMLSelect() {
          if (wrappedElement.getTagName().equalsIgnoreCase("select"))
            return new DOMHTMLSelectElementWrapWebDriver<WebElement>(wrappedElement, ownDocEl, textContent);
          return null;
        }

        @Override
        public DOMHTMLInputElement asHTMLInputElement() {
          if (wrappedElement.getTagName().equalsIgnoreCase("input"))
            return new DOMHTMLInputElementWrapWebDriver<WebElement>(wrappedElement, ownDocEl, textContent);
          return null;
        }

        @Override
        public DOMHTMLForm asHTMLForm() {
          if (wrappedElement.getTagName().equalsIgnoreCase("form"))
            return new DOMHTMLFormWrapWebDriver<WebElement>(wrappedElement, ownDocEl, textContent);
          return null;
        }

        @Override
        public DOMHTMLTextAreaElement asHTMLTextArea() {
          if (wrappedElement.getTagName().equalsIgnoreCase("textarea"))
            return new DOMHTMLTextAreaElementWrapWebDriver<WebElement>(wrappedElement, ownDocEl, textContent);
          return null;
        }
      };
    }

    @Override
    public DOMWindow mouseover() {
      try {
        checkActionPreconditionAndThrowIfNecessary("mouseover");
        new Actions(driver).moveToElement(wrappedElement).perform();

      } catch (final UnhandledAlertException e) {
        LOGGER.warn("dismissing alert after <mouseover> . <{}>", e.getMessage());
      } catch (final ElementNotVisibleException e) {
        if (!fallBackToJSExecutionOnNotInteractableElements)
          throw new WebAPINotVisibleOrDisableElementException("Cannot <mouseover>  on the invisible element: "
              + toStringDebug(), e, LOGGER);
        else {
          LOGGER.error("cannot mouseover on not visible element <{}>. Fall back to javascript action", this);
          // callJS("setValue", toJS(this), content);
          throw new WebAPIUnsupportedJavascriptActionRuntimeException("Unsupported mouseover action via Javascript",
              LOGGER);
        }
      }

      return wrapWindow(false);
    }

    @Override
    public DOMWindow focus() {
      try {
        checkActionPreconditionAndThrowIfNecessary("focus");
        // see http://stackoverflow.com/questions/11337353/correct-way-to-focus-an-element-using-webdriver
        if ("input".equals(wrappedElement.getTagName())) {
          wrappedElement.sendKeys("");
        } else {
          new Actions(driver).moveToElement(wrappedElement).perform();
        }
      } catch (final UnhandledAlertException e) {
        LOGGER.warn("dismissing alert after <focus> . <{}>", e.getMessage());
      }

      catch (final ElementNotVisibleException e) {
        if (!fallBackToJSExecutionOnNotInteractableElements)
          throw new WebAPINotVisibleOrDisableElementException("Cannot <focus>  on the invisible element: "
              + toStringDebug(), e, LOGGER);
        else {
          LOGGER.error("cannot focus on not visible element <{}>. Fall back to javascript action", this);
          // callJS("setValue", toJS(this), content);
          throw new WebAPIUnsupportedJavascriptActionRuntimeException("Unsupported focus action via Javascript", LOGGER);
        }
      }

      return wrapWindow(false);
    }

    @Override
    public DOMWindow sendClick(final float x, final float y) {
      unsupported(this, "send click");
      return wrapWindow(false);
    }

    @Override
    public DOMWindow keypress(final Key content) {
      Keys k = Keys.TAB;
      switch (content) {
      case ENTER:
        k = Keys.ENTER;
        break;
      case RETURN:
        k = Keys.RETURN;
        break;
      case TAB:
        k = Keys.TAB;
        break;
      default:
        LOGGER.debug("unsupported keypress for {}", content);
        throw new WebAPIRuntimeException("unsupported keypress", LOGGER);
      }
      new Actions(driver).keyDown(k);
      return wrapWindow(false);
    }

    @Override
    public DOmElementOnJS js() {
      return this;
    }

  }

  private final class DOMDocumentTypeWebDriverImpl implements DOMDocumentType, DOMNode {

    private final DOMDocumentWebDriverImpl doc;

    public DOMDocumentTypeWebDriverImpl(final DOMDocumentWebDriverImpl domDocumentWebDriverImpl) {
      doc = domDocumentWebDriverImpl;
    }

    @Override
    public void addEventListener(final String type, final DOMEventListener listener, final boolean useCapture) {
      unsupported(this, "addEventListener");

    }

    @Override
    public WebBrowser getBrowser() {

      return browserWrapper;
    }

    @Override
    public String toPrettyString() {
      return toString();
    }

    @Override
    public String getXPathLocator() {

      return "/";
    }

    @Override
    public void removeEventListener(final String type, final DOMEventListener listener, final boolean useCapture) {
      unsupported(this, "removeEventListener");
    }

    @Override
    public boolean isStale() {
      return false;
    }

    @Override
    public Type getNodeType() {
      return Type.DOCUMENT_TYPE;
    }

    @Override
    public DOMNodeList getChildNodes() {
      unsupported(this, "getChildNodes");
      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getDOMPropery(java.lang.String)
     */
    @Override
    public String getDOMProperty(final String domProperty) {
      unsupported(this, "getDOMProperty");
      return null;
    }

    @Override
    public String getNodeValue() {
      return null;
    }

    @Override
    public DOMNode getParentNode() {
      return doc;
    }

    @Override
    public String getLocalName() {
      return null;
    }

    @Override
    public DOMNamedNodeMap<DOMNode> getAttributes() {
      unsupported(this, "getAttributes");
      return null;
    }

    @Override
    public String getNodeName() {
      unsupported(this, "getNodeName");
      return null;
    }

    @Override
    public String toPrettyHTML() {
      unsupported(this, "toPrettyHTML");
      return null;
    }

    @Override
    public String getTextContent() {
      unsupported(this, "getTextContent");
      return null;
    }

    @Override
    public boolean isDescendant(final DOMNode node) {
      unsupported(this, "isDescendant");
      return false;
    }

    @Override
    public boolean isVisible() {
      return false;
    }

    @Override
    public DOMXPathEvaluator getXPathEvaluator() {
      unsupported(this, "getXPathEvaluator");
      return null;
    }

    @Override
    public short compareDocumentPosition(final DOMNode other) {
      unsupported(this, "compareDocumentPosition");
      return 0;
    }

    @Override
    public DOMDocument getOwnerDocument() {
      return doc;
    }

    @Override
    public boolean isSameNode(final DOMNode other) {
      unsupported(this, "replaceChild");
      return false;
    }

    @Override
    public boolean isEqualNode(final DOMNode other) {
      unsupported(this, "isEqualNode");
      return false;
    }

    @Override
    public DOMNode appendChild(final DOMNode newChild) {
      unsupported(this, "appendChild");
      return null;
    }

    @Override
    public DOMNode removeChild(final DOMNode child) {
      unsupported(this, "removeChild");
      return null;
    }

    @Override
    public DOMNode replaceChild(final DOMNode newChild, final DOMNode oldChild) {
      unsupported(this, "replaceChild");
      return null;
    }

    @Override
    public DOMNode insertBefore(final DOMNode newChild, final DOMNode refChild) {
      unsupported(this, "insertBefore");
      return null;
    }

    @Override
    public void setTextContent(final String text) {
      unsupported(this, "setTextContent");

    }

    @Override
    public DOMNode getPreviousSibling() {
      final Object js = callJS("getPreviousSibling", toJS(this));
      return wrapDOMNode(js);
    }

    @Override
    public DOMNode getNextSibling() {

      final Object js = callJS("getNextSibling", toJS(this));
      return wrapDOMNode(js);
    }

    @Override
    public DOMNode getLastChild() {
      final Object js = callJS("getLastChild", toJS(this));
      return wrapDOMNode(js);
    }

    @Override
    public DOMNode getFirstChild() {
      final Object js = callJS("getFirstChild", toJS(this));
      return wrapDOMNode(js);
    }

    @Override
    public boolean isTextNode() {
      return false;
    }

    @Override
    public DOMMutationObserver registerMutationObserver(final boolean childList, final boolean attributes,
        final boolean subtree, final boolean characterData, final List<String> attributeFilter) {
      unsupported(this, "registerMutationObserver");
      return null;
    }

    @Override
    public DOMMutationObserver registerMutationObserver(final MutationObserverOptions options) {
      unsupported(this, "registerMutationObserver");
      return null;
    }

    @Override
    public Object executeJavaScript(final String script, final Object... arg) {
      unsupported(this, "executeJavaScript");
      return null;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + getOuterType().hashCode();
      result = (prime * result) + ((doc == null) ? 0 : doc.hashCode());
      result = (prime * result) + ((getNodeType() == null) ? 0 : getNodeType().hashCode());
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
      final DOMDocumentTypeWebDriverImpl other = (DOMDocumentTypeWebDriverImpl) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (doc == null) {
        if (other.doc != null)
          return false;
      } else if (!doc.equals(other.doc))
        return false;
      return true;
    }

    private WebDriverWrapperFactory getOuterType() {
      return WebDriverWrapperFactory.this;
    }

  }

  private final class DOMDocumentWebDriverImpl extends DOMNodeWebDriverImpl<WebElement> implements DOMDocument {
    /**
     * We use the document element as representative of the document (which is not supported directly by WebDriver
     */
    private final WebElement documentElement;

    public DOMDocumentWebDriverImpl(final WebElement documentWrappedElement, final WebElement documentElement) {
      super(documentWrappedElement);
      WebElement d = documentElement;
      if (documentElement == null) {
        browserWrapper.recordStats("new Document --> js(getDocumentElement)");
        d = (WebElement) callJS("getDocumentElement");
      }
      this.documentElement = d;
    }

    @Override
    public boolean isStale() {
      browserWrapper.recordStats("isStaleDocument");
      return verifyStaleness(documentElement);
      // return ExpectedConditions.stalenessOf(documentElement).apply(driver);
    }

    @Override
    public DOMRange createRange(final DOMNode startNode, final int startOffset, final DOMNode endNode,
        final int endOffSet) {
      try {
        if ((startNode.getNodeType() == Type.TEXT) && (endNode.getNodeType() == Type.TEXT)) {
          final DOMTextNodeWebDriverImpl textStart = (DOMTextNodeWebDriverImpl) startNode;
          final DOMTextNodeWebDriverImpl textEnd = (DOMTextNodeWebDriverImpl) endNode;
          // the first parameter is ignored
          final List<Object> box = WebDriverWrapperFactory.this.<List<Object>> callJS("createRangeTextToText",
              textStart.parent, textStart.position, startOffset, textEnd.parent, textEnd.position, endOffSet);

          // returns [rect.left,rect.top,rect.right,rect.bottom,rect.width,rect.height]

          return new DOMRange() {

            private DOMBoundingClientRect bb;

            @Override
            public DOMBoundingClientRect getBoundingClientRect() {
              if (bb == null) {
                bb = new DOMBoundingClientRect() {

                  @Override
                  public float getLeft() {
                    return Float.parseFloat(box.get(0).toString());
                  }

                  @Override
                  public float getTop() {

                    return Float.parseFloat(box.get(1).toString());
                  }

                  @Override
                  public float getRight() {
                    return Float.parseFloat(box.get(2).toString());
                  }

                  @Override
                  public float getBottom() {
                    return Float.parseFloat(box.get(3).toString());
                  }

                  @Override
                  public float getWidth() {

                    return Float.parseFloat(box.get(4).toString());
                  }

                  @Override
                  public float getHeight() {
                    return Float.parseFloat(box.get(5).toString());
                  }

                  @Override
                  public String toString() {
                    return "top " + getTop() + " left " + getLeft() + " bottom " + getBottom() + " right " + getRight()
                        + " width " + getWidth() + " height " + getHeight();
                  }

                };
              }
              return bb;
            }

          };
        }
        unsupported(this.getClass(), "createRange Not supported yet for non text nodes");
        return null;
      } catch (final RuntimeException e) {
        LOGGER.error(e.getMessage());
        return null;
      }
    }

    @Override
    public String toString() {
      return "DOCUMENT";
    }

    // @Override
    // public int hashCode() {
    // // for the document node we use the currentURL
    // if (wrappedElement == null)
    // return (getNodeName() + driver.getCurrentUrl()).hashCode();
    // else
    // return (getNodeName() + ((RemoteWebElement) wrappedElement).getId()).hashCode();
    // }
    //
    // @Override
    // public boolean equals(final Object obj) {
    // if (obj instanceof DOMDocumentWebDriverImpl)
    // return isEqualNode((DOMDocumentWebDriverImpl) obj);
    // return false;
    // }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + getOuterType().hashCode();
      result = (prime * result) + ((documentElement == null) ? 0 : documentElement.hashCode());
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
      final DOMDocumentWebDriverImpl other = (DOMDocumentWebDriverImpl) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (documentElement == null) {
        if (other.documentElement != null)
          return false;
      } else if (!documentElement.equals(other.documentElement))
        return false;
      return true;
    }

    @Override
    public DOMElement getDocumentElement() {
      // final WebElement element = js(driver, "return document.documentElement;");
      return wrapElementOptimized(documentElement, documentElement, null, null);
    }

    @Override
    public DOMElement querySelector(final String selectors) {
      final WebElement element = driver.findElementByCssSelector(selectors);
      return wrapElement(element);
    }

    @Override
    public DOMNodeList querySelectorAll(final String selectors) {
      final List<WebElement> list = driver.findElementsByCssSelector(selectors);
      return buildNodeList(list);

    }

    @Override
    public String getNodeName() {
      return "#document";
    }

    @Override
    public DOMNode getParentNode() {
      return null;
    }

    @Override
    public DOMElement selectElementBy(final CRITERIA critera, final String expr) {
      switch (critera) {
      case css:
        return querySelector(expr);
      case id:
        return getElementById(expr);
      case tagname:
        getElementsByTagName(expr).item(0);
      case name:
        return (DOMElement) getElementsByName(expr).item(0);
      case xpath:
        return wrapElement(driver.findElementByXPath(expr));
      case xy:
        unsupported(this, "selectElementBy X,Y");
      default:
        LOGGER.error("Unhandled Criteria {}", critera);
        throw new WebAPIRuntimeException("Unhandled Criteria", LOGGER);
      }
    }

    @Override
    public DOMNodeList getElementsByName(final String name) {
      final List<WebElement> list = driver.findElementsByName(name);
      return buildNodeList(list);
    }

    @Override
    public DOMElement getElementById(final String id) {
      return wrapElement(driver.findElementById(id));
    }

    @Override
    public DOMNodeList getElementsByTagName(final String tagName) {
      final List<WebElement> list = driver.findElementsByTagName(tagName);
      return buildNodeList(list);
    }

    @Override
    public DOMElement elementByPosition(final int x, final int y) {
      final Object elem = callJS("elementFromPosition", x, y);
      return wrapElement((WebElement) elem);
    }

    @Override
    public DOMWindow getEnclosingWindow() {
      return wrapWindow(false);
    }

    @Override
    public DOMElement createElement(final String tagName) {
      final Object e = callJS("createElementAndAppendToBody", toJS(this), tagName);
      return (DOMElement) wrapDOMNode(e);
    }

    // @Override
    // public DOMNodeList getChildNodes() {
    // final List<Object> inputs = WebDriverWrapperFactory.this.<List<Object>> callJS("getChildrenOfDocument",
    // documentElement);
    // return buildMixedNodeList(inputs, wrappedElement);
    // }

    @Override
    public DOMNamedNodeMap<DOMNode> getAttributes() {
      return null;
    }

    @Override
    public Type getNodeType() {
      return Type.DOCUMENT;
    }

    @Override
    public String getNodeValue() {
      return null;
    }

    @Override
    public String getLocalName() {
      return null;
    }

    @Override
    public String toPrettyHTML() {
      return getNodeName();
    }

    @Override
    public String getTextContent() {
      return null;
    }

    @Override
    public boolean isDescendant(final DOMNode node) {
      return false;
    }

    @Override
    public boolean isVisible() {
      return true;
    }

    // @Override
    // public short compareDocumentPosition(final DOMNode other) {
    // if (other == null)
    // throw new WebAPIRuntimeException("other node is null", LOGGER);
    //
    // if (other.getNodeType() == Type.ELEMENT) {
    // final Long longValue = callJS("compareDocumentPositionDocToElement", documentElement,
    // castToElementAndGetWrappedNode(other));
    // return longValue.shortValue();
    // }
    //
    // if (other.getNodeType() == Type.DOCUMENT) {
    // final Long longValue = callJS("compareDocumentPositionDocToDoc", documentElement,
    // castToDocumentAndGetWrappedNode(other));
    // return longValue.shortValue();
    // }
    //
    // if (other.getNodeType() == Type.TEXT) {
    // final DOMTextNodeWebDriverImpl textNode = (DOMTextNodeWebDriverImpl) other;
    // final Long longValue = callJS("compareDocumentPositionElementToText", documentElement, textNode.parent,
    // textNode.position);
    // return longValue.shortValue();
    // }
    // // text to element
    //
    // unsupported(this.getClass(), "compareDocumentPosition for " + other.getNodeType());
    // return -1;
    //
    // }

    @Override
    public DOMDocument getOwnerDocument() {
      return this;
    }

    // @Override
    // public boolean isSameNode(final DOMNode other) {
    // if (other == null)
    // return false;
    // if (other.getNodeType() != getNodeType())
    // return false;
    // // here we are sure they are two Document objects
    // return WebDriverWrapperFactory.this.<Boolean> callJS("isSameDocument", documentElement,
    // castToDocumentAndGetWrappedNode(other));
    // }

    // @Override
    // public boolean isEqualNode(final DOMNode other) {
    // if (other == null)
    // return false;
    // if (other.getNodeType() != getNodeType())
    // return false;
    // // here we are sure they are two Document objects
    // return WebDriverWrapperFactory.this.<Boolean> callJS("isEqualDocument", documentElement,
    // castToDocumentAndGetWrappedNode(other));
    // }

    @Override
    public DOMNode getPreviousSibling() {
      return null;
    }

    @Override
    public DOMNode getNextSibling() {
      return null;
    }

    // @Override
    // public DOMNode getLastChild() {
    // // here we return HTML element but FIREFOx seems to return the DOCUMENTTYPE node (of type 10)
    // return wrapElement((WebElement) callJS("getLastChildOfDocument", documentElement));
    // }

    // @Override
    // public DOMNode getFirstChild() {
    // // here we return HTML element but FIREFOx seems to return the DOCUMENTTYPE node (of type 10)
    // return wrapElement((WebElement) callJS("getFirstChildOfDocument", documentElement));
    // }

    @Override
    public boolean isTextNode() {
      return false;
    }

    @Override
    public DOMNode appendChild(final DOMNode newChild) {
      throw new WebAPIRuntimeException("Error: Node cannot be inserted at the specified point in the hierarchy", LOG);
    }

    @Override
    public DOMNode insertBefore(final DOMNode newChild, final DOMNode refChild) {
      throw new WebAPIRuntimeException("Error: Node cannot be inserted at the specified point in the hierarchy", LOG);
    }

    @Override
    public void setTextContent(final String text) {
      // ignore, there is no effect on the browser
    }

    @Override
    protected WebDriverWrapperFactory getOuterType() {
      return WebDriverWrapperFactory.this;
    }

    @Override
    public Dimension getDimension() {
      @SuppressWarnings("unchecked")
	final List<Long> dimension = (List<Long>) callJS("getDocumentDimension");

      return new Dimension(dimension.get(0).intValue(), dimension.get(1).intValue());
    }
  }

  private final class DOMWindowWebDriverImpl implements DOMWindow {

    private final WebDriverBrowserImpl outerinstance;
    private final String windowHandler;
    private DOMDocument document;

    public DOMWindowWebDriverImpl(final WebDriverBrowserImpl outerinstance, final String windowHandler) {
      this.outerinstance = outerinstance;
      this.windowHandler = windowHandler;
    }

    @Override
    public void removeEventListener(final String type, final DOMEventListener listener, final boolean useCapture) {
      unsupported(this, "removeEventListener");

    }

    @Override
    public void addEventListener(final String type, final DOMEventListener listener, final boolean useCapture) {
      unsupported(this, "addEventListener");

    }

    @Override
    public void setName(final String name) {
      unsupported(this, "setName");

    }

    @Override
    public boolean isJustOpened() {
      LOGGER.debug("Multiple windows not yet supported");
      return false;
    }

    @Override
    public int getScrollY() {
      // https://developer.mozilla.org/en-US/docs/DOM/window.scrollX

      return ((Long) callJS("getScrollY")).intValue();
    }

    @Override
    public int getScrollX() {
      // https://developer.mozilla.org/en-US/docs/DOM/window.scrollY
      return ((Long) callJS("getScrollX")).intValue();
    }

    @Override
    public String getName() {
      // as name we use the hanlde
      return windowHandler;
    }

    @Override
    public String getContentAsString() {

      return driver.getPageSource();
    }

    @Override
    public DOMDocument getDocument() {
      if ((document == null) || (returnAlwaysFreshDocument && document.isStale())) {
        document = wrapDocument(null);
      }
      return document;
    }

    @Override
    public DOMDocument getDocument(final boolean checkIfStale) {
      DOMDocument doc = getDocument();
      if (checkIfStale) {
        if (doc.isStale()) {
          doc = wrapDocument(null);
        }
      }
      return doc;
    }

    @Override
    public String getTitle() {
      return driver.getTitle();
    }

    @Override
    public void close() {
      // FIXME this check was not there before
      if (driver.getWindowHandles().size() > 1) {
        driver.close();
      }
    }

    @Override
    public WebBrowser getBrowser() {
      return outerinstance;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final DOMWindowWebDriverImpl other = (DOMWindowWebDriverImpl) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (outerinstance == null) {
        if (other.outerinstance != null)
          return false;
      } else if (!outerinstance.equals(other.outerinstance))
        return false;

      return windowHandler.equals(other.windowHandler);
    }

    private WebDriverWrapperFactory getOuterType() {
      return WebDriverWrapperFactory.this;
    }

    @Override
    public int hashCode() {
      return windowHandler.hashCode();
    }

    @Override
    public StyledOverlayBuilder getOverlayBuilder() {
      return new StyledOverlayBuilderImpl(outerinstance);
    }

    @Override
    public Dimension getDimension() {
      final org.openqa.selenium.Dimension dimension = driver.manage().window().getSize();
      return new Dimension(dimension.getWidth(), dimension.getHeight());
    }

  }

  private final class AttributeNodeImpl extends AbstractNodeWrapper<Pair<String, String>> {
    private final Pair<String, String> pair;
    private final DOMNode ownerNode;

    public AttributeNodeImpl(final Pair<String, String> pair, final DOMNode ownerNode) {
      this.pair = pair;
      this.ownerNode = ownerNode;
    }

    @Override
    public WebBrowser getBrowser() {

      return browserWrapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.ox.cs.diadem.webapi.dom.DOMNode#getDOMPropery(java.lang.String)
     */
    @Override
    public String getDOMProperty(final String domProperty) {
      return callJS("getProperty", toJS(this));
    }

    @Override
    public String toPrettyString() {
      return toString();
    }

    @Override
    public String getXPathLocator() {

      final String js = callJS("getXPathLocator", toJS(this), useIdAttributeForXPathLocator,
          useClassAttributeForXPathLocator);
      return js;
    }

    @Override
    public String toString() {
      browserWrapper.recordStats("attribute.toString");
      return ownerNode.getNodeName() + " : " + pair.toString();
    }

    @Override
    public DOMMutationObserver registerMutationObserver(final boolean childList, final boolean attributes,
        final boolean subtree, final boolean characterData, final List<String> attributeFilter) {
      unsupported(this.getClass(), "No DOMMutationObserver for attribute nodes");
      return null;
    }

    @Override
    public DOMMutationObserver registerMutationObserver(final MutationObserverOptions options) {
      unsupported(this.getClass(), "No DOMMutationObserver for attribute nodes");
      return null;
    }

    @Override
    public Type getNodeType() {
      return Type.ATTRIBUTE;
    }

    @Override
    public DOMNodeList getChildNodes() {
      final List<WebElement> a = Lists.newArrayList();
      return buildNodeList(a);
    }

    @Override
    public String getNodeValue() {
      return pair.getRight();
    }

    @Override
    public DOMNode getParentNode() {
      return null;
    }

    @Override
    public String getLocalName() {
      return pair.getKey();
    }

    @Override
    public DOMNamedNodeMap<DOMNode> getAttributes() {
      return null;
    }

    @Override
    public String getNodeName() {
      return pair.getKey();
    }

    @Override
    public String getTextContent() {
      return pair.getValue();
    }

    @Override
    public DOMXPathEvaluator getXPathEvaluator() {
      return new DOMXPathEvaluatorOnWebDriverImpl();
    }

    @Override
    public short compareDocumentPosition(final DOMNode other) {
      final Long longValue = callJS("compareDocumentPosition", toJS(this), toJS(other));
      return longValue.shortValue();
    }

    @Override
    public DOMDocument getOwnerDocument() {
      return ownerNode.getOwnerDocument();
    }

    @Override
    public boolean isSameNode(final DOMNode other) {
      if (other.getNodeType() != getNodeType())
        return false;
      if (!other.getNodeName().equals(pair.getKey()))
        return false;
      LOGGER.error("Cannot compute isSameNode() among attributes '{}' and '{}' ", this, other);
      return false;
    }

    @Override
    public boolean isEqualNode(final DOMNode other) {
      if (other.getNodeType() != getNodeType())
        return false;
      final String tagName = pair.getKey();
      if (!other.getNodeName().equals(tagName))
        return false;
      // LOGGER.error("Cannot compute isEqualNode() among attributes '{}' and '{}' ", this, other);
      // return false;

      return WebDriverWrapperFactory.this.<Boolean> callJS("isEqualNode", toJS(this), toJS(other));

    }

    @Override
    public DOMNode appendChild(final DOMNode newChild) {
      LOGGER.warn("Cannot add child nodes to attributes");
      return null;
    }

    @Override
    public DOMNode removeChild(final DOMNode child) {
      LOGGER.warn("Cannot removeChild  nodes from attributes");
      return null;
    }

    @Override
    public DOMNode replaceChild(final DOMNode newChild, final DOMNode oldChild) {
      LOGGER.warn("Cannot replaceChild  nodes from attributes");
      return null;
    }

    @Override
    public DOMNode insertBefore(final DOMNode newChild, final DOMNode refChild) {
      LOGGER.warn("Cannot insertBefore  nodes from attributes");
      return null;
    }

    @Override
    public void setTextContent(final String text) {
      pair.setValue(text);
    }

    @Override
    public DOMNode getPreviousSibling() {
      return null;
    }

    @Override
    public DOMNode getNextSibling() {
      return null;
    }

    @Override
    public DOMNode getLastChild() {
      return null;
    }

    @Override
    public DOMNode getFirstChild() {
      return null;
    }

    @Override
    public void addEventListener(final String type, final DOMEventListener listener, final boolean useCapture) {

    }

    @Override
    public void removeEventListener(final String type, final DOMEventListener listener, final boolean useCapture) {

    }

    @Override
    Pair<String, String> getWrappedNode() {
      return pair;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = (prime * result) + getOuterType().hashCode();
      result = (prime * result) + ((ownerNode == null) ? 0 : ownerNode.hashCode());
      result = (prime * result) + ((pair == null) ? 0 : pair.hashCode());
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
      final AttributeNodeImpl other = (AttributeNodeImpl) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (ownerNode == null) {
        if (other.ownerNode != null)
          return false;
      } else if (!ownerNode.equals(other.ownerNode))
        return false;
      if (pair == null) {
        if (other.pair != null)
          return false;
      } else if (!pair.equals(other.pair))
        return false;
      return true;
    }

    private WebDriverWrapperFactory getOuterType() {
      return WebDriverWrapperFactory.this;
    }

  }

  private DOMNodeList buildEmptyDomList() {
    return new DOMNodeList() {

      @Override
      public Iterator<DOMNode> iterator() {
        return Collections.emptyIterator();
      }

      @Override
      public DOMNode item(final long index) {
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + 0);
      }

      @Override
      public long getLength() {
        return 0;
      }
    };
  }

  protected Integer getNextKeyForObserver() {
    return keyGeneratorForDomObservers.getAndIncrement();
  }

  DOMNode wrapDOMNode(final Object input) {
    if (input == null)
      return null;
    if (input instanceof WebElement)
      return wrapElement((WebElement) input);

    // if (input instanceof List) {
    // final List<Object> tuple = (ArrayList<Object>) input;
    //
    // return wrapTextNode(tuple);
    // }

    return parseJavascriptNode(input);

  }

  List<Object> toJS(final DOMNode node) {

    final Type nodeType = node.getNodeType();
    switch (nodeType) {

    case DOCUMENT:
      // document [documentElement, nodetype]
      return ImmutableList.<Object> of(castToDocumentAndGetWrappedNode(node), nodeType.asNumber());
    case DOCUMENT_TYPE:
      // documentType [documentElement, nodetype]
      return ImmutableList.<Object> of(castToDocumentAndGetWrappedNode(node.getOwnerDocument()), nodeType.asNumber());

    case ELEMENT:
      // element [elementNode]
      return ImmutableList.<Object> of(castToElementAndGetWrappedNode(node), nodeType.asNumber());
    case ATTRIBUTE:
      final AttributeNodeImpl attribute = castToAttribute(node);
      // attribute [ node.ownerElement, node.localName, node.nodeValue ];
      return ImmutableList.<Object> of(castToElementAndGetWrappedNode(attribute.ownerNode), attribute.pair.getLeft(),
          nodeType.asNumber());

    case TEXT:
      final DOMTextNodeWebDriverImpl text = castToTextNode(node);
      // textnode [parentNode, position, NodeType]
      return ImmutableList.<Object> of(text.parent, text.position, nodeType.asNumber());
    case COMMENT:
      final DOMCommentNodeWebDriverImpl comment = castToCommentNode(node);
      // textnode [parentNode, position, NodeType]
      return ImmutableList.<Object> of(comment.parent, comment.position, nodeType.asNumber());

    default:
      throw new WebAPIRuntimeException("Unsupported node type: " + nodeType, LOGGER);
    }
  }

  private DOMNode wrapTextNode(final List<Object> tuple) {
    // parent can be null, e.g., in case of removeChild
    final Object parent = tuple.get(1);
    if (parent instanceof WebElement) {
      // [thisNode.data, thisNode.parentNode, position, documentElement,xpathLocator,thisNode.nodeType];
      final String xpathLocator = tuple.get(4) == null ? null : tuple.get(4).toString();
      return new DOMTextNodeWebDriverImpl((WebElement) parent, tuple.get(0).toString(), tuple.get(2).toString(),
          (WebElement) tuple.get(3), xpathLocator);
    } else
      return new DOMTextNodeWebDriverImpl(null, tuple.get(0).toString(), "-1");
  }

  private DOMNode wrapCommentNode(final List<Object> tuple) {
    // [ thisNode.data, parent, position, thisNode.ownerDocument.documentElement,thisNode.nodeType ];
    return new DOMCommentNodeWebDriverImpl((WebElement) tuple.get(1), tuple.get(0).toString(), tuple.get(2).toString(),
        (WebElement) tuple.get(3));
  }

  private DOMNode wrapAttribute(final List<Object> tuple) {
    // [ attr.ownerElement, attr.localName, attr.nodeValue, documentElement,attr.nodeType ];
    final WebElement ownerEl = (WebElement) tuple.get(0);
    final WebElement documentElement = (WebElement) tuple.get(3);
    return new AttributeNodeImpl(Pair.of(tuple.get(1).toString(), tuple.get(2).toString()), wrapElementOptimized(
        ownerEl, documentElement, null, null));
  }

  @SuppressWarnings("unchecked")
  private DOMNode parseJavascriptNode(final Object rawNode) {
    if (rawNode instanceof List) {
      final List<Object> tuple = (ArrayList<Object>) rawNode;
      final Integer type = Integer.parseInt(Iterables.getLast(tuple).toString());
      // elements [ thisNode, thisNode.ownerDocument.documentElement,thisNode.textContent,xpathLocator,thisNode.nodeType
      // ];
      if (Type.ELEMENT.asNumber() == type) {
        final String xpathLocator = tuple.get(3) == null ? null : tuple.get(3).toString();
        return wrapElementOptimized((WebElement) tuple.get(0), (WebElement) tuple.get(1), tuple.get(2).toString(),
            xpathLocator);
      }
      // in case of text nodes we expect : // TextNodeRepresentative [thisNode.data, thisNode.parentNode, position,
      // nodeType];
      if (Type.TEXT.asNumber() == type)
        // if (tuple.get(1) instanceof WebElement)
        return wrapTextNode(tuple);

      if (Type.COMMENT.asNumber() == type)
        // if (tuple.get(1) instanceof WebElement)
        return wrapCommentNode(tuple);
      // in case of attributes is [attr.ownerElement, attr.localName, attr.nodeValue, documentElement, nodeType];
      if (Type.ATTRIBUTE.asNumber() == type)
        // if (tuple.get(1) instanceof WebElement)
        return wrapAttribute(tuple);
      // DocumentRepresentative [thisDocument.documentElement, nodeType];
      if (Type.DOCUMENT.asNumber() == type)
        return new DOMDocumentWebDriverImpl(null, (WebElement) tuple.get(0));
      // DocumentTypeRepresentative [documentType.ownerDocument.documentElement, nodeType];
      if (Type.DOCUMENT_TYPE.asNumber() == type)
        return new DOMDocumentTypeWebDriverImpl(new DOMDocumentWebDriverImpl(null, (WebElement) tuple.get(0)));
    }
    throw new WebAPIRuntimeException("Cannot parse JS node of : " + rawNode, LOGGER);

  }

  @SuppressWarnings("unchecked")
  private <T> T parseType(final Class<T> class1, final Object res) {
    if (res instanceof List) {
      final List<Object> l = (List<Object>) res;
      return (T) l.get(0);
    } else
      throw new WebAPIRuntimeException("Cannot parse  xpath result from JS: " + res, LOGGER);
  }

  WebElement castToElementAndGetWrappedNode(final DOMNode node) {

    @SuppressWarnings("unchecked")
    final DOMNodeWebDriverImpl<WebElement> cast = (DOMNodeWebDriverImpl<WebElement>) node;
    return cast.wrappedElement;
  }

  private DOMTextNodeWebDriverImpl castToTextNode(final DOMNode node) {

    final DOMTextNodeWebDriverImpl cast = (DOMTextNodeWebDriverImpl) node;
    return cast;
  }

  private DOMCommentNodeWebDriverImpl castToCommentNode(final DOMNode node) {

    final DOMCommentNodeWebDriverImpl cast = (DOMCommentNodeWebDriverImpl) node;
    return cast;
  }

  private AttributeNodeImpl castToAttribute(final DOMNode node) {

    final AttributeNodeImpl attr = (AttributeNodeImpl) node;
    return attr;
  }

  private WebElement castToDocumentAndGetWrappedNode(final DOMNode node) {

    final DOMDocumentWebDriverImpl cast = (DOMDocumentWebDriverImpl) node;
    return cast.documentElement;
  }

  private DOMNodeList buildNodeList(final List<WebElement> list) {
    return new DOMNodeList() {

      @Override
      public Iterator<DOMNode> iterator() {
        final Function<WebElement, DOMNode> function = new Function<WebElement, DOMNode>() {
          @Override
          public DOMNode apply(final WebElement input) {
            return wrapElement(input);
          }
        };
        return Iterators.transform(list.iterator(), function);
      }

      @Override
      public DOMNode item(final long index) {
        return wrapElement(list.get((int) index));
      }

      @Override
      public long getLength() {
        return list.size();
      }

      @Override
      public String toString() {
        return "DOMNodeList of size " + list.size();
        // return "DOMNodeList : " + Joiner.on("/n").join(Iterables.transform(list, new Function<WebElement, String>() {
        //
        // @Override
        // public String apply(final WebElement input) {
        // return input.getTagName();
        // }
        // }));
      }
    };
  }

  private DOMNodeList buildMixedNodeList(final List<Object> list) {
    return new DOMNodeList() {

      @Override
      public Iterator<DOMNode> iterator() {
        final Function<Object, DOMNode> function = new Function<Object, DOMNode>() {
          @Override
          public DOMNode apply(final Object input) {
            return wrapDOMNode(input);
          }
        };
        return Iterators.transform(list.iterator(), function);
      }

      @Override
      public DOMNode item(final long index) {
        final Object input = list.get((int) index);
        return wrapDOMNode(input);
      }

      @Override
      public long getLength() {
        return list.size();
      }
    };
  }

  private class ChildListDOMMutationRecordImpl extends DOMMutationRecordImpl {
    // ChildListModRecord[type, tnode, addedNodes, removedNodes]
    @SuppressWarnings("unchecked")
    ChildListDOMMutationRecordImpl(final MutationType type, final DOMNode target, final Object addedNodes,
        final Object removedNodes) {
      super(type, target);
      if (addedNodes != null) {
        this.addedNodes = Lists.newArrayList(buildMixedNodeList((List<Object>) addedNodes));
      }
      if (removedNodes != null) {
        this.removedNodes = Lists.newArrayList(buildMixedNodeList((List<Object>) removedNodes));
      }
    }
  }

  private class AttributeDOMMutationRecordImpl extends DOMMutationRecordImpl {
    // AttributeModRecord[type, targetNode, attrName,oldValue,newValue];
    AttributeDOMMutationRecordImpl(final MutationType type, final DOMNode target, final Object attrName,
        final Object oldValue, final Object newValue) {
      super(type, target);
      attributeName = attrName != null ? attrName.toString() : null;
      this.oldValue = oldValue != null ? oldValue.toString() : null;
      this.newValue = newValue != null ? newValue.toString() : null;
    }
  }

  private class CharDataDOMMutationRecordImpl extends DOMMutationRecordImpl {
    // DataModRecord[type, targetNode, oldValue,newValue];
    CharDataDOMMutationRecordImpl(final MutationType type, final DOMNode target, final Object oldValue,
        final Object newValue) {
      super(type, target);
      this.oldValue = oldValue != null ? oldValue.toString() : null;
      this.newValue = newValue != null ? newValue.toString() : null;
    }
  }

  private class DOMMutationRecordImpl implements DOMMutationRecord {

    final MutationType type;
    final DOMNode target;
    ArrayList<DOMNode> addedNodes = Lists.newArrayList();
    ArrayList<DOMNode> removedNodes = Lists.newArrayList();
    String attributeName = null;;
    String oldValue = null;
    String newValue = null;

    DOMMutationRecordImpl(final MutationType type, final DOMNode target) {
      this.type = type;
      this.target = target;
    }

    @Override
    public MutationType type() {
      return type;
    }

    @Override
    public DOMNode target() {
      return target;
    }

    @Override
    public List<DOMNode> addedNodes() {
      return addedNodes;
    }

    @Override
    public List<DOMNode> removedNodes() {
      return removedNodes;
    }

    @Override
    public String attributeName() {
      return attributeName;
    }

    @Override
    public String oldValue() {
      return oldValue;
    }

    @Override
    public String newValue() {
      return newValue;
    }

    @Override
    public String toString() {
      return "DOMMutationRecordImpl of type " + type;// new ReflectionToStringBuilder(this,
      // ToStringStyle.MULTI_LINE_STYLE).toString();
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + getOuterType().hashCode();
      result = (prime * result) + (addedNodes == null ? 0 : addedNodes.hashCode());
      result = (prime * result) + (attributeName == null ? 0 : attributeName.hashCode());
      result = (prime * result) + (newValue == null ? 0 : newValue.hashCode());
      result = (prime * result) + (oldValue == null ? 0 : oldValue.hashCode());
      result = (prime * result) + (removedNodes == null ? 0 : removedNodes.hashCode());
      result = (prime * result) + (target == null ? 0 : target.hashCode());
      result = (prime * result) + (type == null ? 0 : type.hashCode());
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
      final DOMMutationRecordImpl other = (DOMMutationRecordImpl) obj;
      if (type != other.type)
        return false;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (addedNodes == null) {
        if (other.addedNodes != null)
          return false;
      } else if (!addedNodes.equals(other.addedNodes))
        return false;
      if (attributeName == null) {
        if (other.attributeName != null)
          return false;
      } else if (!attributeName.equals(other.attributeName))
        return false;
      if (newValue == null) {
        if (other.newValue != null)
          return false;
      } else if (!newValue.equals(other.newValue))
        return false;
      if (oldValue == null) {
        if (other.oldValue != null)
          return false;
      } else if (!oldValue.equals(other.oldValue))
        return false;
      if (removedNodes == null) {
        if (other.removedNodes != null)
          return false;
      } else if (!removedNodes.equals(other.removedNodes))
        return false;
      if (target == null) {
        if (other.target != null)
          return false;
      } else if (!target.equals(other.target))
        return false;

      return true;
    }

    private WebDriverWrapperFactory getOuterType() {
      return WebDriverWrapperFactory.this;
    }

  }

  protected final class DOMXPathEvaluatorOnWebDriverImpl implements DOMXPathEvaluator {

    @SuppressWarnings("unchecked")
    @Override
    public Table<DOMNode, String, List<DOMNode>> evaluateBulk(final DOMNode context, final String pathToAnchorNode,
        final Set<String> contextualAttributeNodes) {

      final List<Object> representative = toJS(context);

      final Map<String, Object> res = WebDriverWrapperFactory.this.<Map<String, Object>> callJS("evalXPathBulk",
          pathToAnchorNode, representative, contextualAttributeNodes, useIdAttributeForXPathLocator,
          useClassAttributeForXPathLocator);

      final List<Object> errors = (List<Object>) res.get("errors"); // arraylist
      final List<List<Object>> records = (List<List<Object>>) res.get("anchorNodes");// arraylist
      final List<List<List<Object>>> allAttributes = (List<List<List<Object>>>) res.get("contextualNodes");

      final Builder<DOMNode, String, List<DOMNode>> btable = ImmutableTable.builder();

      if (!errors.isEmpty()) {
        LOGGER.warn("Bulk xpath has errors {}, returning empty", errors.get(0));
        return btable.build();
      }
      if (records.size() != allAttributes.size()) {
        LOGGER.warn("Bulk xpath has inconsistent results size for records and attributes");
        return btable.build();
      }

      // records
      final Function<List<Object>, DOMNode> parseRawNodeFunction = new Function<List<Object>, DOMNode>() {

        @Override
        public DOMNode apply(final List<Object> input) {

          return parseJavascriptNode(input);
        }
      };

      final Function<List<Object>, List<DOMNode>> parseRawListNodeFunction = new Function<List<Object>, List<DOMNode>>() {

        @Override
        public List<DOMNode> apply(final List<Object> input) {
          final List<DOMNode> nodes = Lists.newLinkedList();
          for (final Object node : input) {
            nodes.add(parseJavascriptNode(node));
          }
          return nodes;
        }
      };

      final List<DOMNode> recoNodes = Lists.transform(records, parseRawNodeFunction);

      btable.put(context, pathToAnchorNode, recoNodes);

      final Iterator<List<List<Object>>> attributesIterator = allAttributes.iterator();

      for (final DOMNode recordNode : recoNodes) {
        final Iterator<String> attrExpreIterator = contextualAttributeNodes.iterator();
        // the attribtues for the current record
        final List<List<Object>> allAttrsForRecord = attributesIterator.next();

        for (final List<Object> attr : allAttrsForRecord) {
          final String attreExpressionForRecord = attrExpreIterator.next();
          final List<DOMNode> nodeList = parseRawListNodeFunction.apply(attr);
          btable.put(recordNode, attreExpressionForRecord, nodeList);

        }
      }

      return btable.build();

      // Table<DOMNode, String, OXPathType> recordAndAttributes

      // anchorNodes : [],
      // contextualNodes : [],
      // errors : []
    }

    @Override
    public DOMXPathResult evaluate(final String expression, final DOMNode contextNode,
        final DOMXPathNSResolver resolver, final short type, final Object result) throws DOMXPathException {

      final List<Object> representative = toJS(contextNode);

      final List<Object> res = WebDriverWrapperFactory.this.<List<Object>> callJS("evalXPath", expression,
          representative, type, useIdAttributeForXPathLocator, useClassAttributeForXPathLocator);

      // LOGGER.debug("xPath evaluation returned '{}' elemens", res.size());
      final Iterator<Object> resIterator = res.iterator();
      return new DOMXPathResult() {

        @Override
        public DOMNode snapshotItem(final int index) throws DOMXPathException {

          return parseJavascriptNode(resIterator.next());
        }

        @Override
        public DOMNode iterateNext() throws DOMXPathException {
          if (resIterator.hasNext())
            return parseJavascriptNode(resIterator.next());
          // as specifics
          return null;
        }

        @Override
        public String getStringValue() throws DOMXPathException {
          return parseType(String.class, res);
        }

        @Override
        public long getSnapshotLength() throws DOMXPathException {
          return res.size();
        }

        @Override
        public DOMNode getSingleNodeValue() throws DOMXPathException {
          return parseJavascriptNode(res.get(0));
        }

        @Override
        public double getNumberValue() throws DOMXPathException {
          return Double.parseDouble(parseType(Long.class, res).toString());
        }

        @Override
        public boolean getInvalidIteratorState() {
          return false;
        }

        @Override
        public Boolean getBooleanValue() throws DOMXPathException {
          return parseType(Boolean.class, res);
        }

        @Override
        public short getResultType() {
          if (res.isEmpty())
            return DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE;
          final Object object = res.get(0);
          if (object instanceof Boolean)
            return DOMXPathResult.BOOLEAN_TYPE;
          if (object instanceof String)
            return DOMXPathResult.STRING_TYPE;
          if (object instanceof Double)
            return DOMXPathResult.NUMBER_TYPE;

          return DOMXPathResult.UNORDERED_NODE_SNAPSHOT_TYPE;
        }

      };
    }

    @Override
    public DOMXPathNSResolver createNSResolver(final DOMNode nodeResolver) {
      return new DOMXPathNSResolver() {

        @Override
        public String lookupNamespaceURI(final String prefix) {
          unsupported(this.getClass(), "lookupNamespaceURI");
          return null;
        }
      };
    }

    @Override
    public DOMXPathExpression createExpression(final String expression, final DOMXPathNSResolver resolver)
        throws DOMXPathException {
      return new DOMXPathExpression() {

        @Override
        public Object evaluate(final DOMNode contextNode, final short type, final Object result)
            throws DOMXPathException {

          unsupported(this.getClass(), "evaluate");
          return null;
        }
      };
    }
  }

  private class DOMHTMLFormWrapWebDriver<N extends WebElement> extends DOMElementWrapWebDriverImpl<WebElement>
  implements DOMHTMLForm {

    public DOMHTMLFormWrapWebDriver(final WebElement form, final WebElement ownDocEl, final String textContent) {
      super(form, ownDocEl, textContent);
    }

    @Override
    public Iterable<DOMElement> getElements() {
      final List<Object> inputs = WebDriverWrapperFactory.this.<List<Object>> callJS("getFormElements", toJS(this));
      return Iterables.transform(inputs, new Function<Object, DOMElement>() {

        @Override
        public DOMElement apply(final Object input) {
          return (DOMElement) wrapDOMNode(input);
        }
      });
    }

    @Override
    public int getLength() {
      unsupported(this.getClass(), "getLength");
      return 0;
    }

    @Override
    public String getName() {
      unsupported(this.getClass(), "getName");
      return null;
    }

    @Override
    public void setName(final String aName) {
      unsupported(this.getClass(), "setName");

    }

    @Override
    public String getAcceptCharset() {
      unsupported(this.getClass(), "getAcceptCharset");
      return null;
    }

    @Override
    public void setAcceptCharset(final String aAcceptCharset) {
      unsupported(this.getClass(), "setAcceptCharset");

    }

    @Override
    public String getAction() {
      unsupported(this.getClass(), "getAction");
      return null;
    }

    @Override
    public void setAction(final String aAction) {
      unsupported(this.getClass(), "setAction");
    }

    @Override
    public String getEnctype() {
      unsupported(this.getClass(), "getEnctype");
      return null;
    }

    @Override
    public void setEnctype(final String aEnctype) {
      unsupported(this.getClass(), "setEnctype");
    }

    @Override
    public String getMethod() {
      return callJS("getFormMethod", toJS(this));
    }

    @Override
    public void setMethod(final String aMethod) {
      unsupported(this.getClass(), "setMethod");
    }

    @Override
    public String getTarget() {
      unsupported(this.getClass(), "getTarget");
      return null;
    }

    @Override
    public void setTarget(final String aTarget) {
      unsupported(this.getClass(), "setTarget");
    }

    @Override
    public DOMWindow submit() {
      checkActionPreconditionAndThrowIfNecessary("submit");
      try {
        wrappedElement.submit();
        return wrapWindow(true);

      } catch (final ElementNotVisibleException e) {
        LOGGER.error("Not visible element: cannot call submit() on element {} ", toStringDebug());
        throw new WebAPINotVisibleOrDisableElementException("Not visible element: cannot call submit() on element "
            + toStringDebug(), e, LOGGER);
      }

    }

    @Override
    public void reset() {
      unsupported(this.getClass(), "getLength");
    }

  }

  private class DOMHTMLTextAreaElementWrapWebDriver<N extends WebElement> extends
  DOMElementWrapWebDriverImpl<WebElement> implements DOMHTMLTextAreaElement {
    public DOMHTMLTextAreaElementWrapWebDriver(final WebElement textarea, final WebElement ownDocEl,
        final String textContent) {
      super(textarea, ownDocEl, textContent);
    }

    @Override
    public String getDefaultValue() {
      return wrappedElement.getAttribute("defaultValue");
    }

    @Override
    public boolean getDisabled() {
      return wrappedElement.getAttribute("disabled") != null;
    }

    @Override
    public void select() {
      checkActionPreconditionAndThrowIfNecessary("select text via CTRL-A");
      try {
        wrappedElement.click();
        wrappedElement.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        wrappedElement.sendKeys(Keys.BACK_SPACE);
      } catch (final ElementNotVisibleException e) {
        if (!fallBackToJSExecutionOnNotInteractableElements)
          throw new WebAPINotVisibleOrDisableElementException(
              "Not visible element: cannot select text via CTRL-A on element " + toStringDebug(), e, LOGGER);
        else {
          LOGGER.error(
              "Not visible element: cannot select text via CTRL-A on element {}. Fall back to javascript action  ",
              this);
          // HACK here we do nothing as oxpath will call setValue later
          // clickJS();
        }

      }

    }

    @Override
    public String getValue() {
      return wrappedElement.getAttribute("value");
    }

    @Override
    public void setValue(final String aValue) {
      callJS("setValue", toJS(this), aValue);
    }

    @Override
    public boolean willValidate() {
      return wrappedElement.getAttribute("willValidate") != null;
    }
  }

  private class DOMHTMLInputElementWrapWebDriver<N extends WebElement> extends DOMElementWrapWebDriverImpl<WebElement>
  implements DOMHTMLInputElement {
    public DOMHTMLInputElementWrapWebDriver(final WebElement htmlSelect, final WebElement ownDocEl,
        final String textContent) {
      super(htmlSelect, ownDocEl, textContent);
    }

    @Override
    public String getDefaultValue() {
      return wrappedElement.getAttribute("defaultValue");
    }

    @Override
    public void setDefaultValue(final String aDefaultValue) {
      callJS("setProperty", toJS(this), "defaultValue", aDefaultValue);
    }

    @Override
    public boolean getDefaultChecked() {
      return wrappedElement.getAttribute("defaultChecked") != null;
    }

    @Override
    public void setDefaultChecked(final boolean aDefaultChecked) {
      callJS("setProperty", toJS(this), "defaultChecked", aDefaultChecked);
    }

    @Override
    public String getAccept() {
      return wrappedElement.getAttribute("accept");
    }

    @Override
    public void setAccept(final String aAccept) {
      unsupported(this.getClass(), "setAccept");

    }

    @Override
    public String getAccessKey() {
      return wrappedElement.getAttribute("accessKey");
    }

    @Override
    public void setAccessKey(final String aAccessKey) {
      unsupported(this.getClass(), "setAccessKey");

    }

    @Override
    public String getAlign() {
      return wrappedElement.getAttribute("align");
    }

    @Override
    public void setAlign(final String aAlign) {
      unsupported(this.getClass(), "setAlign");

    }

    @Override
    public String getAlt() {
      return wrappedElement.getAttribute("alt");
    }

    @Override
    public void setAlt(final String aAlt) {
      unsupported(this.getClass(), "setAlt");

    }

    @Override
    public boolean getChecked() {
      return WebDriverWrapperFactory.this.<Boolean> callJS("getChecked", toJS(this));
    }

    @Override
    public void setChecked(final boolean aChecked) {
      callJS("setChecked", toJS(this), aChecked);

    }

    @Override
    public boolean getDisabled() {
      return wrappedElement.getAttribute("disabled") != null;
    }

    @Override
    public void setDisabled(final boolean aDisabled) {
      unsupported(this.getClass(), "setDisabled");

    }

    @Override
    public int getMaxLength() {
      return Integer.parseInt(wrappedElement.getAttribute("maxLength"));
    }

    @Override
    public void setMaxLength(final int aMaxLength) {
      unsupported(this.getClass(), "setMaxLength");

    }

    @Override
    public String getName() {
      return wrappedElement.getAttribute("name");
    }

    @Override
    public void setName(final String aName) {
      unsupported(this.getClass(), "setName");

    }

    @Override
    public boolean getReadOnly() {
      return wrappedElement.getAttribute("readOnly") != null;
    }

    @Override
    public void setReadOnly(final boolean aReadOnly) {
      unsupported(this.getClass(), "setReadOnly");

    }

    @Override
    public long getSize() {
      return Long.parseLong(wrappedElement.getAttribute("size"));
    }

    @Override
    public void setSize(final long aSize) {
      unsupported(this.getClass(), "setSize");

    }

    @Override
    public String getSrc() {
      return wrappedElement.getAttribute("src");
    }

    @Override
    public void setSrc(final String aSrc) {
      unsupported(this.getClass(), "setSrc");

    }

    @Override
    public int getTabIndex() {
      return Integer.parseInt(wrappedElement.getAttribute("tabIndex"));
    }

    @Override
    public void setTabIndex(final int aTabIndex) {
      unsupported(this.getClass(), "setTabIndex");

    }

    @Override
    public String getType() {
      return wrappedElement.getAttribute("type");
    }

    @Override
    public void setType(final String aType) {
      unsupported(this.getClass(), "setType");

    }

    @Override
    public String getUseMap() {
      return wrappedElement.getAttribute("userMap");
    }

    @Override
    public void setUseMap(final String aUseMap) {
      unsupported(this.getClass(), "setUseMap");
    }

    @Override
    public String getValue() {
      return wrappedElement.getAttribute("value");
    }

    @Override
    public void setValue(final String aValue) {
      callJS("setValue", toJS(this), aValue);
    }

    @Override
    public void blur() {
      unsupported(this.getClass(), "blur");
    }

    @Override
    public void select() {
      checkActionPreconditionAndThrowIfNecessary("select text via CTRL-A");
      try {
        wrappedElement.click();
        wrappedElement.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        wrappedElement.sendKeys(Keys.BACK_SPACE);
      } catch (final ElementNotVisibleException e) {
        LOGGER.error("Not visible element: cannot select text via CTRL-A on element {} ", toStringDebug());
        throw new WebAPINotVisibleOrDisableElementException(
            "Not visible element: cannot select text via CTRL-A on element " + toStringDebug(), e, LOGGER);
      }
    }

  }

  private class DOMHTMLSelectElementWrapWebDriver<N extends WebElement> extends DOMElementWrapWebDriverImpl<WebElement>
  implements DOMHTMLSelect {

    private final Select select;

    public DOMHTMLSelectElementWrapWebDriver(final WebElement htmlSelect, final WebElement documentelement,
        final String textContent) {
      super(htmlSelect, documentelement, textContent);
      this.select = new Select(htmlSelect);
    }

    @Override
    public DOMWindow selectByClick(final int index) {
      try {
        checkActionPreconditionAndThrowIfNecessary("select");
        select.selectByIndex(index);
        triggerUnhandledAlertExceptionAfterActionIfAny();
      } catch (final ElementNotVisibleException e) {
        if (!fallBackToJSExecutionOnNotInteractableElements)
          throw new WebAPINotVisibleOrDisableElementException("Not visible element: cannot SELECT option with index <"
              + index + "> on SELECT element " + toStringDebug(), e, LOGGER);
        else {
          LOGGER
          .error(
              "Not visible element: cannot select option with index <{}> on SELECT element {}. Fall back to Javascript",
              index, this);
          callJS("setSelectedIndex", toJS(this), index);

        }
      } catch (final UnhandledAlertException e) {
        LOGGER.warn("dismissing alert after selectByClick action <{}>", e.getMessage());
      }
      return wrapWindow(true);
    }

    @Override
    public DOMWindow selectByClick(final String optiontext) {
      try {
        checkActionPreconditionAndThrowIfNecessary("selectByClick");
        select.selectByVisibleText(optiontext);
        triggerUnhandledAlertExceptionAfterActionIfAny();
      } catch (final ElementNotVisibleException e) {

        if (!fallBackToJSExecutionOnNotInteractableElements)
          throw new WebAPINotVisibleOrDisableElementException("Not visible element: cannot select text <" + optiontext
              + "> on SELECT element " + toStringDebug(), e, LOGGER);
        else {
          LOGGER
          .error(
              "Not visible element: cannot select option by text <{}> on SELECT element {}. Fall back to Javascript action ",
              optiontext, toStringDebug());
          callJS("setSelectedOptionByText", toJS(this), optiontext);
        }
      } catch (final UnhandledAlertException e) {
        LOGGER.warn("dismissing alert after selectByClick action <{}>", e.getMessage());
      }
      return wrapWindow(true);
    }

    @Override
    public int getSelectedIndex() {
      final WebElement selectedOption = select.getFirstSelectedOption();
      return select.getOptions().indexOf(selectedOption);
    }

    @Override
    public DOMWindow selectOptionIndex(final int aSelectedIndex) {
      return selectByClick(aSelectedIndex);
    }

    @Override
    public void setSelectedIndex(final int aSelectedIndex) {
      selectByClick(aSelectedIndex);
    }

    @Override
    public void setSelectedByText(final String optiontext) {
      select.selectByVisibleText(optiontext);

    }

    @Override
    public String getValue() {
      return select.getFirstSelectedOption().getAttribute("value");
    }

    @Override
    public void setValue(final String aValue) {

      callJS("setAttribute", wrapDOMNode(select.getFirstSelectedOption()), "value", aValue);

    }

    @Override
    public long getLength() {
      return select.getOptions().size();
    }

    @Override
    public void selectAllOptions() {

      final Actions builder = new Actions(driver);

      builder.keyDown(Keys.CONTROL);

      final List<WebElement> options = select.getOptions();
      for (final WebElement op : options) {
        builder.click(op);
      }

      builder.keyUp(Keys.CONTROL);
      builder.perform();

    }

    @Override
    public DOMHTMLOptionsCollection getOptions() {
      return new DOMHTMLOptionsCollection() {

        @Override
        /*
         * This method retrieves a {@link DOMHTMLOptionElement} using a name. It first searches for a node with a
         * matching id attribute. If it doesn't find one, it then searches for a Node with a matching name attribute,
         * but only on those elements that are allowed a name attribute. This method is case insensitive in HTML
         * documents and case sensitive in XHTML documents.
         */
        public DOMHTMLOptionElement namedItem(final String name) {

          unsupported(this.getClass(), "namedItem");
          return null;
        }

        @Override
        public DOMHTMLOptionElement item(final long index) {
          final WebElement option = select.getOptions().get((int) index);
          return new DOMHTMLOptionWrapWebDriver(option, select, ownerDocument == null ? null
              : ownerDocument.documentElement, null);
        }

        @Override
        public long getLength() {

          return select.getOptions().size();
        }

        @Override
        public DOMHTMLOptionElement itemByValue(final String value) {
          unsupported(this.getClass(), "itemByValue");
          return null;
        }

        @Override
        public DOMHTMLOptionElement itemByText(final String text) {
          final Optional<WebElement> option = Iterables.tryFind(select.getOptions(), new Predicate<WebElement>() {

            @Override
            public boolean apply(final WebElement input) {
              return input.getText().equals(text);
            }
          });
          final WebElement orNull = option.orNull();
          if (orNull == null)
            return null;
          return new DOMHTMLOptionWrapWebDriver(orNull, select, ownerDocument == null ? null
              : ownerDocument.documentElement, null);
        }

      };
    }

    @Override
    public boolean getDisabled() {
      return getWrappedNode().getAttribute("disabled") != null;
    }

    @Override
    public boolean getMultiple() {
      return select.isMultiple();
    }

    @Override
    public String getType() {
      if (select.isMultiple())
        return "select-multiple";
      return "select-one";
    }

    @Override
    public String getName() {
      return getWrappedNode().getAttribute("name");
    }

    @Override
    public void setName(final String aName) {
      callJS("setAttribute", getWrappedNode(), "name", aName);

    }

    @Override
    public int getSize() {
      return Integer.parseInt(getWrappedNode().getAttribute("size"));
    }

    @Override
    public void setSize(final int aSize) {
      callJS("setAttribute", toJS(this), "size", aSize);
    }

    @Override
    public int getTabIndex() {

      return Integer.parseInt(getWrappedNode().getAttribute("tabindex"));

    }

    @Override
    public void setTabIndex(final int aTabIndex) {
      callJS("setAttribute", toJS(this), "tabindex", aTabIndex);

    }

    @Override
    public DOMWindow selectOptionByText(final String optiontext) {

      try {
        checkActionPreconditionAndThrowIfNecessary("selectOptionByText");
        select.selectByVisibleText(optiontext);
        triggerUnhandledAlertExceptionAfterActionIfAny();
      } catch (final ElementNotVisibleException e) {

        if (!fallBackToJSExecutionOnNotInteractableElements)
          throw new WebAPINotVisibleOrDisableElementException("Not visible element: cannot select text <" + optiontext
              + "> on SELECT element " + toStringDebug(), e, LOGGER);
        else {
          LOGGER
          .error(
              "Not visible element: cannot select option by text <{}> on SELECT element {}. Fall back to Javascript action ",
              optiontext, toStringDebug());
          callJS("setSelectedOptionByText", toJS(this), optiontext);
        }
      } catch (final UnhandledAlertException e) {
        LOGGER.warn("dismissing alert after selectOptionByText action <{}>", e.getMessage());
      }

      return wrapWindow(true);
    }

  }

  private void unsupported(final Object clazz, final String msg) {
    LOGGER.error("not yet implemented {} for class", msg, clazz.getClass());
    throw new WebAPIRuntimeException("Not yet implemented " + msg + "for class " + clazz.getClass(), LOGGER);

  }

  private class DOMHTMLOptionWrapWebDriver extends DOMElementWrapWebDriverImpl<WebElement> implements
  DOMHTMLOptionElement {

    private final Select select;

    public DOMHTMLOptionWrapWebDriver(final WebElement option, final Select parent, final WebElement ownDocEl,
        final String textContent) {
      super(option, ownDocEl, textContent);
      select = parent;
    }

    @Override
    public boolean getDefaultSelected() {
      return getWrappedNode().getAttribute("selected") != null;
    }

    @Override
    public String getText() {
      return getWrappedNode().getText();
    }

    @Override
    public int getIndex() {
      return select.getOptions().indexOf(getWrappedNode());
    }

    @Override
    public boolean getDisabled() {
      return !getWrappedNode().isEnabled();
    }

    @Override
    public String getLabel() {
      return getWrappedNode().getAttribute("label");
    }

    @Override
    public boolean getSelected() {
      return getWrappedNode().isSelected();
    }

    @Override
    public void setSelected(final boolean aSelected) {
      checkActionPreconditionAndThrowIfNecessary("select ");
      try {
        if (aSelected) {
          if (!getWrappedNode().isSelected()) {
            getWrappedNode().click();
          }
        } else if (getWrappedNode().isSelected()) {
          getWrappedNode().click();
        }
      } catch (final ElementNotVisibleException e) {
        if (!fallBackToJSExecutionOnNotInteractableElements)
          throw new WebAPINotVisibleOrDisableElementException("Not visible element: cannot select element "
              + toStringDebug(), e, LOGGER);
        else {

          LOGGER.error("Not visible element: cannot select element {}. Fall back to javascript action ", this);
          callJS("setSelected", toJS(this), aSelected);
        }
      }

    }

    @Override
    public String getValue() {
      return getWrappedNode().getAttribute("value");
    }

    @Override
    public void setValue(final String aValue) {
      callJS("setAttribute", toJS(this), "value", aValue);
    }

  }

  MutationFormObserver observeFormMutation(final DOMElement rootNode) {

    final List<String> css_prop = Lists.newArrayList(CssProperty.color.getPropertyName(),
        CssProperty.background_color.getPropertyName(), CssProperty.background_image.getPropertyName(),
        CssProperty.background_position.getPropertyName(), CssProperty.margin.getPropertyName(),
        CssProperty.border.getPropertyName(), CssProperty.font_weight.getPropertyName());

    final Integer index = getNextKeyForObserver();

    callJSFromLib(mutationSummary_script, "createSummaryObserver(arguments[0],fromJava(arguments[1]),arguments[2]);",
        index, toJS(rootNode), css_prop);
    // driver.executeScript(mutationSummary_script, wrappedElement, index);

    return new MutationFormObserverImplementation(index, css_prop, rootNode);
  }

  void sleep(final TimeUnit timeUnit, final long valueToWait) {
    try {
      SLEEPER.poll(valueToWait, timeUnit);
    } catch (final Exception e) {
      // toNothing
    }
  }

  void checkIfFrame(final String string, final WebElement webElement) {
    final String nodeName = webElement.getTagName();

    if (!(nodeName.equalsIgnoreCase("iframe") || nodeName.equalsIgnoreCase("frame"))) {

      LOGGER.error("method {} only allowed on Iframe and Frame elements. Invalid current element {}", string, nodeName);
      throw new WebAPIRuntimeException(string
          + "() method only allowed on Iframe and Frame elements. Invalid current element " + nodeName, LOGGER);
    }
  }

  private void triggerUnhandledAlertExceptionAfterActionIfAny() {
    sleep(TimeUnit.MILLISECONDS, DEFAULT_WAIT_AFTER_ACTION_MILLIS);
    // to triegger the UnhandledAlertException if any

    driver.getCurrentUrl();
  }

  private final class MutationFormObserverImplementation implements MutationFormObserver {
    private final Integer index;
    private final List<String> css_prop;
    private final DOMElement rootNode;
    private final String nodeLocator;

    private MutationFormObserverImplementation(final Integer index, final List<String> css_prop,
        final DOMElement rootNode) {
      this.index = index;
      this.css_prop = css_prop;
      this.rootNode = rootNode;
      nodeLocator = rootNode.getXPathLocator();
    }

    @Override
    public MutationFormSummary takeSummaryAndDisconnect() {

      // final Object summary = callJSFromLib(mutationSummary_script,
      // "return takeMutationSummary(arguments[0],fromJava(arguments[1]),arguments[2]);", index,
      // toJS(XPathUtil.getFirstNode(nodeLocator, browserWrapper)), css_prop);
      final Object summary = callJSFromLib(mutationSummary_script,
          "return takeMutationSummary(arguments[0],arguments[1],arguments[2]);", index, "notUsedArgument", css_prop);

      return parseSummary(summary);
      // } catch (final Exception e) {
      // LOGGER.warn("Cannot get any summary for node <{}>. Return empty summary", nodeLocator);
      // return parseSummary(Maps.newHashMap());
      // }
    }

    private MutationFormSummary parseSummary(final Object summary) {
      if (summary instanceof Map) {
        // fieldsWithNewOptions : [],
        // newFields : [],
        // textVaried : [],//strings
        // fieldsWithRemovedOptions : [],
        // removedFields : []

        final Map<?, ?> summaryMap = (Map<?, ?>) summary;

        return new MutationFormSummaryImpl(summaryMap);
      } else {
        LOGGER.error("Cannot parse Javascript result, Unexpected type {}. Return empty results", summary.getClass());
        throw new WebAPIJavascriptRuntimeException(
            "Error parsing MutationFormSummary facts from JS invocation. Unexpected type " + summary.getClass(), LOGGER);
      }
    }
  }

}
