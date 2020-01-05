/*
 * Copyright (c) 2013, DIADEM Team
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
/*
 * Copyright (c) 2016, OXPath Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the OXPath team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL OXPath Team BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package uk.ac.ox.cs.diadem.oxpath.core;

import java.io.File;
import java.io.Reader;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import uk.ac.ox.cs.diadem.oxpath.core.state.PAATState;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathType;
import uk.ac.ox.cs.diadem.oxpath.output.IAbstractOutputHandler;
import uk.ac.ox.cs.diadem.oxpath.parse.OXPathParser;
import uk.ac.ox.cs.diadem.oxpath.parse.ast.Node;
import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder;

/**
 * Singleton OXPath ENGINE, main API access point.
 *
 * @author Giovanni Grasso {@literal <gio@oxpath.org>}
 * @author Ruslan Fayzrakhmanov
 */
public enum OXPath {
  /**
   * Singleton instance
   */
  ENGINE;
	
  /**
   * Logging
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(OXPath.class);

  /**
   * Options of the OXPath engine
   * Default values:
   * {@code doContinueOnMissingContextNode = false},
   * {@code useIdAttributeForXPathLocator = true},
   * {@code useClassAttributeForXPathLocator = true},
   * {@code waitAfterActionExecutionMs = 200}.
   * 
 * @author ruslan
 * 24 Nov 2016
 */
public class EngineOptions {
	  private static final boolean doContinueOnMissingContextNodeDefault = false;
	  private static final boolean useIdAttributeForXPathLocatorDefault = true;
	  private static final boolean useClassAttributeForXPathLocatorDefault = true;
	  private static final long waitAfterActionExecutionMsDefault = 200;
	  private static final boolean autocompleteReactionDefault = false;
	  /**
     * this option, if enabled, forces the evaluation to continue when a context node on a previous page is not found,
     * e.g., because the page has changed and the XPath locator fails. Default is false
     */
	private boolean doContinueOnMissingContextNode;
	public boolean isDoContinueOnMissingContextNode() {
		return doContinueOnMissingContextNode;
	}
	public EngineOptions setDoContinueOnMissingContextNode(boolean doContinueOnMissingContextNode) {
		this.doContinueOnMissingContextNode = doContinueOnMissingContextNode;
		return this;
	}
	/**
     * By default xpath locators consider the id attribute of a node.
     */
	private boolean useIdAttributeForXPathLocator;
	public boolean isUseIdAttributeForXPathLocator() {
		return useIdAttributeForXPathLocator;
	}
	public EngineOptions setUseIdAttributeForXPathLocator(boolean useIdAttributeForXPathLocator) {
		this.useIdAttributeForXPathLocator = useIdAttributeForXPathLocator;
		return this;
	}
	/**
     * By default xpath locators consider the class attribute of a node, if the id is not available
     */
	private boolean useClassAttributeForXPathLocator;
	public boolean isUseClassAttributeForXPathLocator() {
		return useClassAttributeForXPathLocator;
	}
	public EngineOptions setUseClassAttributeForXPathLocator(boolean useClassAttributeForXPathLocator) {
		this.useClassAttributeForXPathLocator = useClassAttributeForXPathLocator;
		return this;
	}
	/**
     * Milliseconds to wait after each action is performed
     */
	private long waitAfterActionExecutionMs;
	public long getWaitAfterActionExecutionMs() {
		return waitAfterActionExecutionMs;
	}
	public EngineOptions setWaitAfterActionExecutionMs(long waitAfterActionExecutionMs) {
		this.waitAfterActionExecutionMs = waitAfterActionExecutionMs;
		return this;
	}
	
	/**
	 * React on autocomplete elements.
	 * If true, the OXPath engine will click on the first element which appears just after the typing action
	 */
	private boolean autocompleteReaction;
	public boolean isAutocompleteReaction() {
		return autocompleteReaction;
	}
	public void setAutocompleteReaction(boolean autocompleteReaction) {
		this.autocompleteReaction = autocompleteReaction;
	}
	public EngineOptions() {
		  doContinueOnMissingContextNode = doContinueOnMissingContextNodeDefault;
		  useIdAttributeForXPathLocator = useIdAttributeForXPathLocatorDefault;
		  useClassAttributeForXPathLocator = useClassAttributeForXPathLocatorDefault;
		  waitAfterActionExecutionMs = waitAfterActionExecutionMsDefault;
		  autocompleteReaction = autocompleteReactionDefault;
	  }
	@Override
	public String toString() {
		return "EngineOptions [doContinueOnMissingContextNode=" + doContinueOnMissingContextNode
				+ ", useIdAttributeForXPathLocator=" + useIdAttributeForXPathLocator
				+ ", useClassAttributeForXPathLocator=" + useClassAttributeForXPathLocator
				+ ", waitAfterActionExecutionMs=" + waitAfterActionExecutionMs + ", autocompleteReaction="
				+ autocompleteReaction + "]";
	}

  }
  
  private final EngineOptions options = new EngineOptions();
  /**
 * @return options of the OXPath engines.
 * Any changes of the parameters are directly reflected on the configuration of the OXPath parser.
 */
  public EngineOptions getOptions() {
	  return options;
  }

  private RuntimeException writeErrorAndThrowRuntime(final Exception e, IAbstractOutputHandler oh, final String wrapper) {
	  return new OXPathRuntimeException(
		  String.format("Error executing wrapper:\n%s\n---", wrapper)
			  , e, LOGGER);
  }

  /**
   * 
   * Evaluates a query contained in a {@link File} (in the UTF8 encoding) , using the given {@link WebBrowser}. The extracted data (if any) is
   * managed by the provided {@link IAbstractOutputHandler}. It throws a {@link RuntimeException} in case the execution does not
   * terminate correctly. It returns an object {@link OXPathType} that encodes the returned result types.
   * 
   * @param in
   *          the {@link Reader} to read the query from
   * @param builder
   *          the {@code BrowserBuilder} object to use for the evaluation
   * @param oh the {@link IAbstractOutputHandler} to handle the output format
   * @return {@link OXPathType} that encodes the returned result types
   * @throws A
   *           OXPathRuntimeException if something goes wrong
 */
  public OXPathType evaluate(final File in, final WebBrowserBuilder builder, final IAbstractOutputHandler oh) {
	  String wrapper = null;
	    try {
	      wrapper = Files.toString(in, Charsets.UTF_8);
if (LOGGER.isInfoEnabled()) LOGGER.info("starting OXPath evaluation on file {}", in.getAbsolutePath());
	      final Node node = OXPathParser.getJJTree(in.getAbsolutePath());
	      return evaluateWithBrowserBuilder(node, builder, oh);
	    } catch (final Exception e) {
	      throw writeErrorAndThrowRuntime(e, oh, wrapper);
	    }
  }
  
  public OXPathType evaluateWithBrowserBuilder(Node node, WebBrowserBuilder builder, final IAbstractOutputHandler oh) {
	  if (LOGGER.isDebugEnabled())
    	  LOGGER.info("START: Initializing the browser");
	  WebBrowser browser = builder.build();
	  if (LOGGER.isDebugEnabled())
    	  LOGGER.info("DONE: Initializing the browser");
	  if (LOGGER.isDebugEnabled())
    	  LOGGER.info("START: OXPath evaluation");
      OXPathType result = invoke(node, browser, oh);
      if (LOGGER.isDebugEnabled())
    	  LOGGER.info("DONE: OXPath evaluation");
      if (LOGGER.isDebugEnabled())
    	  LOGGER.info("START: Shutting down the browser");
      browser.shutdown();
      if (LOGGER.isDebugEnabled())
    	  LOGGER.info("DONE: Shutting down the browser");
      return result;
  }
  
  /**
   * Evaluates a query contained in a {@link File} (in the UTF8 encoding) , using the given {@link WebBrowser}. The extracted data (if any) is
   * managed by the provided {@link IAbstractOutputHandler}. It throws a {@link RuntimeException} in case the execution does not
   * terminate correctly. It returns an object {@link OXPathType} that encodes the returned result types.
   *
   * @param in
   *          the {@link Reader} to read the query from
   * @param browser
   *          the {@code WebBrowser} object to use for the evaluation
   * @param oh the {@link IAbstractOutputHandler} to handle the output format
   * @return {@link OXPathType} that encodes the returned result types
   * @throws A
   *           OXPathRuntimeException if something goes wrong
   */
  public OXPathType evaluate(final File in, final WebBrowser browser, final IAbstractOutputHandler oh) {
    String wrapper = null;
    try {
      wrapper = Files.toString(in, Charsets.UTF_8);
if (LOGGER.isInfoEnabled()) LOGGER.info("starting OXPath evaluation on file {}", in.getAbsolutePath());
      final Node node = OXPathParser.getJJTree(in.getAbsolutePath());
      if (LOGGER.isDebugEnabled())
    	  LOGGER.info("START: OXPath evaluation");
      OXPathType result = invoke(node, browser, oh);
      if (LOGGER.isDebugEnabled())
    	  LOGGER.info("DONE: OXPath evaluation");
      return result;
    } catch (final Exception e) {
      throw writeErrorAndThrowRuntime(e, oh, wrapper);
    }
  }
  
  /**
   * Evaluates a query provided as a {@link Reader}, using the given {@link WebBrowser}. The extracted data (if any) is
   * managed by the provided {@link IAbstractOutputHandler}. It throw a {@link RuntimeException} in case the execution does not
   * terminate correctly. It returns an object {@link OXPathType} that encodes the returned result types.
   *
   * @param in
   *          the {@link Reader} to read the query from
   * @param builder
   *          the {@link WebBrowserBuilder} object to use for the evaluation
   * @param oh
   *          the {@link IAbstractOutputHandler} to handle the output format
   * @return {@link OXPathType} that encodes the returned result types
   * @throws A
   *           OXPathRuntimeException if something goes wrong
   */
  public OXPathType evaluate(final Reader in, final WebBrowserBuilder builder, final IAbstractOutputHandler oh) {
	  try {
  if (LOGGER.isInfoEnabled()) LOGGER.info("starting OXPath evaluation on reader {}", in);
  	      final Node node = OXPathParser.getJJTreeFromReader(in);
  	      return evaluateWithBrowserBuilder(node, builder, oh);
  	    } catch (final Exception e) {
  	      throw writeErrorAndThrowRuntime(e, oh, null);
  	    }
	  }
  
  /**
   * Evaluates a query provided as a {@link Reader}, using the given {@link WebBrowser}. The extracted data (if any) is
   * managed by the provided {@link IAbstractOutputHandler}. It throw a {@link RuntimeException} in case the execution does not
   * terminate correctly. It returns an object {@link OXPathType} that encodes the returned result types.
   *
   * @param in
   *          the {@link Reader} to read the query from
   * @param browser
   *          the WebBrowser object to use for the evaluation
   * @param oh
   *          the {@link IAbstractOutputHandler} to handle the output format
   * @return {@link OXPathType} that encodes the returned result types
   * @throws A
   *           OXPathRuntimeException if something goes wrong
   */
	public OXPathType evaluate(final Reader in, final WebBrowser browser, final IAbstractOutputHandler oh) {
	    try {
if (LOGGER.isInfoEnabled()) LOGGER.info("starting OXPath evaluation on reader {}", in);
	      final Node node = OXPathParser.getJJTreeFromReader(in);
	      if (LOGGER.isDebugEnabled())
	    	  LOGGER.info("START: OXPath evaluation");
	      OXPathType result = invoke(node, browser, oh);
	      if (LOGGER.isDebugEnabled())
	    	  LOGGER.info("DONE: OXPath evaluation");
	      return result;
	    } catch (final Exception e) {
	      throw writeErrorAndThrowRuntime(e, oh, null);
	    }
  }
	
	/**
	   * Evaluates a query provided as a {@link Reader}, using the given {@link WebBrowser}. The extracted data (if any) is
	   * managed by the provided {@link IAbstractOutputHandler}. It throw a {@link RuntimeException} in case the execution does not
	   * terminate correctly. It returns an object {@link OXPathType} that encodes the returned result types.
	   *
	   * @param in
	   *          the {@link Reader} to read the query from
	   * @param builder
	   *          the {@link WebBrowserBuilder} object to use for the evaluation
	   * @param oh
	   *          the {@link IAbstractOutputHandler} to handle the output format
	   * @return {@link OXPathType} that encodes the returned result types
	   * @throws A
	   *           OXPathRuntimeException if something goes wrong
	   */
	public OXPathType evaluate(final String wrapper, final WebBrowserBuilder builder, final IAbstractOutputHandler oh) {
		try {
	if (LOGGER.isInfoEnabled()) LOGGER.info("starting OXPath evaluation on expression <{}>", wrapper);
	      final Node node = OXPathParser.getJJTreeFromString(wrapper);
	      return evaluateWithBrowserBuilder(node, builder, oh);
	    } catch (final Exception e) {
	      throw writeErrorAndThrowRuntime(e, oh, wrapper);
	    }
  }

  /**
   * Evaluates a query provided as string, using the given {@link WebBrowser}. The extracted data (if any) is managed by
   * the provided {@link IAbstractOutputHandler}. It throw a {@link RuntimeException} in case the execution does not terminate
   * correctly. It returns an object {@link OXPathType} that encodes the returned result types.
   *
   * @param wrapper
   *          the {@link Reader} to read the query from
   * @param browser
   *          the WebBrowser object to use for the evaluation
   * @param oh
   *          the {@link IAbstractOutputHandler} to handle the output format
   * @return {@link OXPathType} that encodes the returned result types
   * @throws An {@link OXPathRuntimeException} if something goes wrong
   */
  public OXPathType evaluate(final String wrapper, final WebBrowser browser, final IAbstractOutputHandler oh) {
    try {
if (LOGGER.isInfoEnabled()) LOGGER.info("starting OXPath evaluation on expression <{}>", wrapper);
      final Node node = OXPathParser.getJJTreeFromString(wrapper);
      if (LOGGER.isDebugEnabled())
    	  LOGGER.info("START: OXPath evaluation");
      OXPathType result = invoke(node, browser, oh);
      if (LOGGER.isDebugEnabled())
    	  LOGGER.info("DONE: OXPath evaluation");
      return result;
    } catch (final Exception e) {
      throw writeErrorAndThrowRuntime(e, oh, wrapper);
    }
  }
  
  /**
   * Throws {@link OXPathRuntimeException} if something goes wrong
   *
   * @param n
   * @param browser
   * @param oh
   * @return
   */
  private OXPathType invoke(final Node n, final WebBrowser browser, final IAbstractOutputHandler oh) {
    try {
      browser.manageOptions().enableOXPathOptimization(true);
      browser.manageOptions().configureXPathLocatorHeuristics(options.isUseIdAttributeForXPathLocator(),
          options.isUseClassAttributeForXPathLocator());
      browser.manageOptions().setFallBackToJSExecutionOnNotInteractableElements(true);

      final PAATEvalVisitor pv = PAATEvalVisitor.newInstance(browser, oh, options);
      final OXPathType result = pv.accept(n, new PAATState.Builder(OXPathContextNode.getNotionalContext()).buildSet());
      return result;
    }
    catch (final OXPathException e) {
      LOGGER.error("Error executing OXPath {}", ExceptionUtils.getMessage(e));
      throw new OXPathRuntimeException("Error executing OXPath " + ExceptionUtils.getMessage(e), e, LOGGER);
    } finally {
        browser.manageOptions().enableOXPathOptimization(false);
    }
  }

}
