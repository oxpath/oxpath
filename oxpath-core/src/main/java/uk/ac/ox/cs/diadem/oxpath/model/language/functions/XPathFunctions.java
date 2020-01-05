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
/**
 * Package containing supporting classes, derived from the OXPath model (which itself extends the XPath model).
 * This subpackage includes classes and interface relating to the functions of the OXPath language.
 */
package uk.ac.ox.cs.diadem.oxpath.model.language.functions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import uk.ac.ox.cs.diadem.oxpath.core.OXPathException;
import uk.ac.ox.cs.diadem.oxpath.core.state.PAATStateEvalSet;
import uk.ac.ox.cs.diadem.oxpath.model.IOXPathNodeList;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathContextNode;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathType;
import uk.ac.ox.cs.diadem.oxpath.model.OXPathType.OXPathTypes;
import uk.ac.ox.cs.diadem.oxpath.utils.OXPathRuntimeException;
import uk.ac.ox.cs.diadem.util.misc.EscapingUtils;
import uk.ac.ox.cs.diadem.webapi.WebBrowser;
import uk.ac.ox.cs.diadem.webapi.WebBrowserBuilder;
import uk.ac.ox.cs.diadem.webapi.dom.DOMDocument;
import uk.ac.ox.cs.diadem.webapi.dom.DOMElement;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode;
import uk.ac.ox.cs.diadem.webapi.dom.DOMNode.Type;

/**
 * @author AndrewJSel
 * @author Giovanni Grasso <gio@oxpath.org>
 */
public enum XPathFunctions implements XPathFunction {

  // | < TOXML : "to-xml" ( < SPACE > | < TAB > | < NEWLINE > )* < OPEN_PARAN > > : DEFAULT

  TOXML("to-xml", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * Return a string serialization of the XML representation of a current element
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      final OXPathContextNode contextNode = checkProconditionOnContextSet(state, getName(), logger);

      OXPathType oxPathType = null;
      if (args.isEmpty()) {
        oxPathType = new OXPathType(contextNode);
      } else {

        oxPathType = args.get(0);
      }
      // if the argument or context is not a node, return empty
      final IOXPathNodeList nodeList = oxPathType.nodeList();
      if (nodeList.isEmpty())
        return new OXPathType();

      final DOMNode arg = nodeList.first().getNode();
      return new OXPathType(arg.getBrowser().js().asXLM(arg));

    }

  },

  ISVISIBLE("is-visible", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     *
     * Returns true if a current element has been visualized by the web browser and has non-empty area.
     * 
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      final OXPathContextNode contextNode = checkProconditionOnContextSet(state, getName(), logger);
      OXPathType oxPathType = null;
      if (args.isEmpty()) {
        oxPathType = new OXPathType(contextNode);
      } else {

        oxPathType = args.get(0);
      }
      // if the argument or context is not a node, return empty
      final IOXPathNodeList nodeList = oxPathType.nodeList();
      if (nodeList.isEmpty())
        return new OXPathType();

      final DOMNode arg = nodeList.first().getNode();
      return new OXPathType(arg.isVisible());

    }
  },

  ISINVISIBLE("is-invisible", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * Returns false if a current element has been visualized by the web browser and has non-empty area.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      return new OXPathType(!ISVISIBLE.evaluate(args, state).booleanValue());

    }
  },

  QUALIFYURL("qualify-url", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     * 
     * Return the URL relative to the URL of a current web page
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
      final OXPathContextNode contextNode = checkProconditionOnContextSet(state, getName(), logger);
      // first argument is the string version of any type passed in
      String url = null;

      if (args.isEmpty()) {
        url = new OXPathType(contextNode).string();
      } else {
        // the argument MUST be a node type
        url = args.get(0).string();
      }

      final WebBrowser browser = contextNode.getNode().getBrowser();
      return new OXPathType(browser.js().makeURLAbsolute(url));

    }
  },
  SELECTTEXT("select-text", 0, 2) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
      final OXPathContextNode contextNode = checkProconditionOnContextSet(state, getName(), logger);
      final WebBrowser browser = contextNode.getNode().getBrowser();

      final int numArgs = args.size();

      if (numArgs <= 1) {// first argument, can be a single node or a node set

        OXPathType firstArgumentType = null;
        if (numArgs == 0) {
          firstArgumentType = new OXPathType(contextNode);
        } else {
          firstArgumentType = args.get(0);// getArgumentAsNodeSet(args, getName(), 0, logger);
        }

        final IOXPathNodeList firstArgumentAsNodes = firstArgumentType.nodeList();

        if (firstArgumentAsNodes.isEmpty()) // empty node set
          return new OXPathType("");

        final DOMNode startRange = firstArgumentAsNodes.first().getNode();
        final int size = firstArgumentAsNodes.size();
        if (size == 1) // single node
          return new OXPathType(browser.js().selectText(startRange).toString());
        else {

          final DOMNode end = firstArgumentAsNodes.last().getNode();
          return new OXPathType(browser.js().selectText(startRange, end).toString());
        }
      }

      else {// two arguments, must be individual nodes, start and end of the range

        final OXPathType firstArgumentType = args.get(0);// getArgumentAsNodeSet(args, getName(), 0, logger);
        final IOXPathNodeList firstArgumentAsNodes = firstArgumentType.nodeList();
        if (firstArgumentAsNodes.isEmpty()) // empty node set
          return new OXPathType("");
        final DOMNode startRange = firstArgumentAsNodes.first().getNode();// getSingleDOMNode(firstArgumentType,
        // getName(), logger);

        final OXPathType secondArgumentType = args.get(1);// getArgumentAsNodeSet(args, getName(), 1, logger);
        final IOXPathNodeList secondArgumentAsNodes = secondArgumentType.nodeList();
        if (secondArgumentAsNodes.isEmpty()) // empty node set
          return new OXPathType("");

        final DOMNode endRange = secondArgumentAsNodes.first().getNode();// getSingleDOMNode(secondArgumentType,
        // getName(), logger);
        return new OXPathType(browser.js().selectText(startRange, endRange).toString());
      }
    }

  },

  DOMPROPERTY("dom-property", 1, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * Returns the property of the current DOM node. For example, dom-property("nodeType") returns the type of an element.
     * 
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
      final OXPathContextNode contextNode = checkProconditionOnContextSet(state, getName(), logger);
      // the argument is the string version of any type passed in
      final String prop = args.get(0).string();
      final String value = contextNode.getNode().getDOMProperty(prop);
      if (value == null)
        throw new OXPathRuntimeException("Property <" + prop + "> undefined for context node " + contextNode.getNode(),
            logger);

      return new OXPathType(value);

    }
  },
  INNERHTML("innerhtml", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     * 
     * Return the innerHTML property of a current node.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      return innerOrOuterHTML(args, state, this);

    }
  },

  OUTERHTML("outerhtml", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     * 
     * Return the outerHTML property of a current node.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      return innerOrOuterHTML(args, state, this);

    }
  },

  /**
   * OXPath <tt>page-content</tt> function
   */
  SAVEPAGE("save-page", 0, 0) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checkesd. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
    	return new OXPathType("function savep-page() non supported");
//      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
//      final OXPathContextNode contextNode = checkProconditionOnContextSet(state, getName(), logger);
//
//      final WebBrowser browser = contextNode.getNode().getBrowser();
//      if (!(browser.getWindowFrame() instanceof WebClient)) // only HTMLUNIT
//        return new OXPathType("function savep-page() non supported");
//
//      final WebClient client = (WebClient) browser.getWindowFrame();
//
//      // final List<WebWindow> allWindows = client.getWebWindows();
//      // for (final WebWindow current : allWindows) {
//      // final WebWindow top = current.getTopWindow();
//      // logger.info("{} {}", top.getClass().getSimpleName(), top.getEnclosedPage().getUrl());
//      // }
//      final WebWindow currentWindow = client.getCurrentWindow().getTopWindow();
//      final HtmlPage currentPage = (HtmlPage) currentWindow.getEnclosedPage();
//
//      final String fileName = currentPage.getUrl().getHost() + "_"
//          + new SimpleDateFormat("ddMMM_HHmmss").format(new Date());
//
//      final File destFile = new File(fileName.replaceAll("\\W", "_") + ".html");
//      try {
//        logger.debug("Saving page {} in {}", currentPage.getUrl(), destFile.getAbsolutePath());
//        currentPage.save(destFile);
//        logger.debug("..done");
//      } catch (final Exception e) {
//        logger.error("Error in function {} <{}>", name(), ExceptionUtils.getStackTrace(e));
//        return new OXPathType("ERROR in " + name());
//      }
//
//      return new OXPathType(destFile.getAbsolutePath());
    }
  },

  /**
   * XPath <tt>JAROWRINKLER</tt> function
   */
  JAROWRINKLER("jaro-wrinkler", 2, 2) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * Returns the similarity between two arguments (DOM nodes or strings) in terms of Jaro-Winkler distance. The value is in the range from 0 to 1, the higher the similarity the higher the value.
     * Two arguments are required.
     * 
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
      final Float distance = new JaroWinklerDistance().getDistance(args.get(0).string(), args.get(1).string());
      // logger.info("JaroWinklerDistance('{}', '{}') --> '{}'", new Object[] { args.get(0).string(),
      // args.get(1).string(), distance });
      return new OXPathType(distance);
    }
  },

  /**
   * OXPath <tt>page-content</tt> function
   */
  PAGECONTENT("page-content", 0, 0) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checkesd. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * It returns a string serialization of HTML of a current state of a web page. It does not require any arguments.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
      final OXPathContextNode contextNode = checkProconditionOnContextSet(state, getName(), logger);
      // TODO
      final String content = contextNode.getNode().getOwnerDocument().getEnclosingWindow().getContentAsString();
      return new OXPathType(content);

    }
  },

  /**
   * OXPath <tt>current-url</tt> function
   */
  CURRENTURL("current-url", 0, 0) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * Return the URL of a current web page. It does not require any arguments.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
      final OXPathContextNode contextNode = checkProconditionOnContextSet(state, name(), logger);
      final String url = contextNode.getNode().getBrowser().getLocationURL();
      return new OXPathType(url);

    }

  },

  /**
   * OXPath <tt>current-url</tt> function
   */
  NOW("now", 0, 0) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * It creates a timestamp in a form {@literal yyyy-MM-dd'T'HH:mm:ss'Z'}. It does not require any arguments.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
      final OXPathContextNode contextNode = checkProconditionOnContextSet(state, getName(), logger);
      // iso8601 format
      return new OXPathType(DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC")));

    }
  },

  URIFY("urify", 1, 1) {
	  private static final String TMP_REPLACEMENT = "45d43366d53411e6881d8b4a5e76f37e";
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * Return the part of the URI after the last {@literal /}, all symbols which are not
     * {@literal \W}, are replaced by {@literal _}.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
      checkProconditionOnContextSet(state, getName(), logger);
      final String val = args.get(0).string();

      if (val.startsWith("http://"))
        return new OXPathType(StringUtils.substringAfterLast(val, "/").replaceAll("\\W+", "_"));
      else {
        String in = val.replaceAll("_", TMP_REPLACEMENT).replaceAll("\\W+", "_").replaceAll(TMP_REPLACEMENT, "_");
        if (!in.isEmpty() && Character.isDigit(in.charAt(0))) {
          in = "a" + in;
        }
        return new OXPathType(in);
      }

    }
  },

  /**
   * OXPath <tt>current-url</tt> function
   */
  SCREENSHOT("screenshot", 0, 0) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     * 
     * TODO: check wether the file copied is removed.
     * 
     * It loads a web page with the URL of a current web page into a new window and makes a screenshot.
     * It returns the path to the file created.
     * The path has the following form {@literal host_timestamp}, in which all characters of
     * {@literal host_timestamp} matching the regular expression {@literal \W}
     * are replaced by {@literal _}, {@literal timestamp} has the format {@literal ddMMM_HHmmss}.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);

      final OXPathContextNode contextNode = checkProconditionOnContextSet(state, getName(), logger);

      final String url = contextNode.getNode().getBrowser().getLocationURL();

      final WebBrowser browser = new WebBrowserBuilder().build();
      browser.navigate(url, true);
      logger.debug("Taking screenshot of {} ", url);
      final File takeScreenshot = browser.takeScreenshot();
      logger.debug("..done");

      final String fileName = contextNode.getNode().getBrowser().getURL().getHost() + "_"
          + new SimpleDateFormat("ddMMM_HHmmss").format(new Date());

      final File destFile = new File(fileName.replaceAll("\\W", "_") + ".png");

      try {
        logger.debug("Creating screenshot of {} in {}", url, destFile.getAbsoluteFile());
        FileUtils.copyFile(takeScreenshot, destFile);
        logger.debug("..done!");
      } catch (final Exception e) {
        logger.error("Error in function {} <{}>", name(), ExceptionUtils.getStackTrace(e));
        return new OXPathType("ERROR");
      } finally {
        browser.shutdown();
      }

      return new OXPathType(destFile.getAbsolutePath());

    }
  },

  /**
   * XPath <tt>position</tt> function
   */
  POSITION("position", 0, 0) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(state.getPosition());
    }
  },

  /**
   * XPath <tt>last</tt> function
   */
  LAST("last", 0, 0) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(state.getLast());
    }
  },

  /**
   * XPath <tt>count</tt> function
   */
  COUNT("count", 1, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(args.get(0).nodeList().size());
    }
  },

  /**
   * XPath <tt>id</tt> function
   */
  ID("id", 1, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      throw new OXPathException("XPath id function not implemented in OXPath - use # selector instead!");
    }
  },

  /**
   * XPath <tt>namespace-uri</tt> function
   */
  NAMESPACEURI("namespace-uri", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      if (args.isEmpty())
        return state.getContextSet().first().getByXPath("namespace-uri(.)");
      else
        return args.get(0).nodeList().first().getByXPath("namespace-uri(.)");
    }
  },

  /**
   * XPath <tt>local-name</tt> function
   */
  LOCALNAME("local-name", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      if (args.isEmpty())
        return state.getContextSet().first().getByXPath("local-name(.)");
      else
        return args.get(0).nodeList().first().getByXPath("local-name(.)");
    }
  },

  /**
   * XPath <tt>name</tt> function
   */
  NAME("name", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      if (args.isEmpty())
        return state.getContextSet().first().getByXPath("name(.)");
      else
        return args.get(0).nodeList().first().getByXPath("name(.)");
    }
  },

  /**
   * XPath <tt>string</tt> function
   */
  STRING("string", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      if (args.isEmpty())
        return new OXPathType(new OXPathType(state.getContextSet()).string());
      else
        return new OXPathType(args.get(0).string());
    }
  },

  /**
   * XPath <tt>string</tt> function
   */
  STRINGJOIN("string-join", 2, 3) {
//	  TODO: add default procedure of extracting textual value of a nodes into the third argument
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * Join textual content of DOM nodes. The first argument is a node set, the second is a string, a separator.
     * The third argument is optional and specifies the procedure of obtaining the textual representation of nodes.
     * The following values are accepted: "to-xml", "normalize-space", "innerhtml", and "outerhtml".
     *
     * 
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
      checkProconditionOnContextSet(state, getName(), logger);

      final OXPathType oxPathType = getArgumentAsNodeSet(args, getName(), 0, logger);

      final IOXPathNodeList nodeList = oxPathType.nodeList();
      if (nodeList.isEmpty())
        return new OXPathType("");

      final String separatorArg = args.get(1).string();

      Function<OXPathContextNode, String> functionToApply = null;

      if (args.size() == 2) {
        functionToApply = new TextContentFunction();
      } else {
        // third argument is the function name to apply
        final String functionArg = args.get(2).string();
        if (XPathFunctions.TOXML.getName().equals(functionArg)) {
          functionToApply = new ToXMLFunction();
        }
        if (XPathFunctions.NORMALIZESPACE.getName().equals(functionArg)) {
          functionToApply = new NormalizeSpaceFunction();
        }
        if (XPathFunctions.INNERHTML.getName().equals(functionArg)) {
          functionToApply = new InnerOrOuterHTMLFunction(XPathFunctions.INNERHTML);
        }
        if (XPathFunctions.OUTERHTML.getName().equals(functionArg)) {
          functionToApply = new InnerOrOuterHTMLFunction(XPathFunctions.OUTERHTML);
        }
        if (functionToApply == null)
          throw new OXPathRuntimeException("Unsupported mapping function <" + functionArg + "> in string-join()",
              logger);

      }
      final Iterable<String> nodesAsStrings = Iterables.transform(nodeList, functionToApply);

      final OXPathType resutl = new OXPathType(Joiner.on(separatorArg).join(nodesAsStrings));
      XPathFunctions.logger.info("Function {} --> {}", name(), resutl.string());
      return resutl;

    }

  },

  /**
   * XPath <tt>concat</tt> function
   */
  CONCAT("concat", 2, Integer.MAX_VALUE) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final StringBuilder sb = new StringBuilder();
      for (final OXPathType arg : args) {
        sb.append(arg.string());
      }
      return new OXPathType(sb.toString());
    }
  },

  /**
   * XPath <tt>starts-with</tt> function
   */
  STARTSWITH("starts-with", 2, 2) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(args.get(0).string().startsWith(args.get(1).string()));
    }
  },

  /**
   * XPath <tt>contains</tt> function
   */
  CONTAINS("contains", 2, 2) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(args.get(0).string().contains(args.get(1).string()));
    }
  },

  MINUS_FUNC("opex:minus", 2, 2) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      return new OXPathType(args.get(0).number() - args.get(1).number());
    }
  },

  PLUS_FUNC("opex:plus", 2, 2) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {

      return new OXPathType(args.get(0).number() + args.get(1).number());
    }
  },

  /**
   * XPath <tt>substring-before-reverse</tt> function
   */
  SUBSTRINGBEFOREREVERSE("substring-before-reverse", 2, 3) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * Return a substring as in {@literal substring-before}, but, in contrast, the search of
     * the "separator" is conducted in a reverse direction, i.e., from the last to
     * the first character of the string.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final String baseString = args.get(0).string();
      final String beforeString = args.get(1).string();
      final int pos = (args.size() == 2) ? 1 : args.get(2).number().intValue();

      if (!(baseString.contains(beforeString)))
        return new OXPathType("");

      if (pos == 0)
        return new OXPathType(baseString);

      int index = -1;
      final int[] indices = new int[baseString.length()];
      int i = -1;
      while (true) {
        index = baseString.indexOf(beforeString, index + 1);
        if (index == -1) {
          break;
        }
        indices[++i] = index;
      }
      indices[++i] = baseString.length();
      final int numOfIndices = i + 1;
      try {
        return new OXPathType(baseString.substring(0, indices[numOfIndices - 1 - pos]));
      } catch (final Exception e) {
        return new OXPathType("");
      }
    }
  },

  /**
   * XPath <tt>substring-after-reverse</tt> function
   */
  SUBSTRINGAFTERREVERSE("substring-after-reverse", 2, 3) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * Return a substring as in {@literal substring-after}, but, in contrast, the search of
     * the "separator" is conducted in a reverse direction,
     * i.e., from the last to the first character of the string.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final String baseString = args.get(0).string();
      final String afterString = args.get(1).string();
      final int pos = (args.size() == 2) ? 1 : args.get(2).number().intValue();

      if (!(baseString.contains(afterString)))
        return new OXPathType("");

      if (pos == 0)
        return new OXPathType("");

      int index = -1;
      final int[] indices = new int[baseString.length()];
      int i = -1;
      while (true) {
        index = baseString.indexOf(afterString, index + 1);
        if (index == -1) {
          break;
        }
        indices[++i] = index;
      }
      indices[++i] = baseString.length();
      final int numOfIndices = i + 1;
      try {
        return new OXPathType(baseString.substring(indices[numOfIndices - 1 - pos] + afterString.length()));
      } catch (final Exception e) {
        return new OXPathType("");
      }
    }
  },

  /**
   * XPath <tt>substring-before</tt> function
   */
  SUBSTRINGBEFORE("substring-before", 2, 3) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * It gets a substring (a string) that is the rest of the string representing the first argument
     * (a DOM node) before a substring given by the second parameter.
     * For example, {@literal substring-before(., "separator")}.
     * The third parameter is optional and specifies the ordinal number of the occurrence of
     * the second argument to be considered as a separator.
     * (It is 1 by default.)
     * For example, {@literal substring-before(., "separator", 5)} returns the substring of
     * the first argument that starts from the beginning of the string and ends just before
     * the 5th occurrence of the substring "separator".
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final String baseString = args.get(0).string();
      final String beforeString = args.get(1).string();
      if (!(baseString.contains(beforeString)))
        return new OXPathType("");
      final int pos = (args.size() == 2) ? 1 : args.get(2).number().intValue();
      if (pos == 0)
        return new OXPathType("");
      int index = -1;
      for (int i = 0; i < pos; ++i) {
        index = baseString.indexOf(beforeString, index + 1);
        if (index == -1)
          return new OXPathType("");
      }
      try {
        return new OXPathType(baseString.substring(0, index));
      } catch (final Exception e) {
        // as for xpath specification
        return new OXPathType("");
      }

    }
  },

  /**
   * XPath <tt>substring-after</tt> function
   */
  SUBSTRINGAFTER("substring-after", 2, 3) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * Get a substring (a string) that is the rest of the string representing the first argument
     * (a DOM node) after a substring given by the second parameter.
     * For example, {@literal substring-after(., "separator")}.
     * The third parameter is optional and specifies the ordinal number of the occurrence
     * of the second argument to be considered as a separator.
     * (It is 1 by default.)
     * For example, {@literal substring-after(., "separator", 5)} returns the substring
     * of the first argument that starts just after the 5th occurrence of the substring
     * "separator" and ends at the end of the string.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final String baseString = args.get(0).string();
      final String afterString = args.get(1).string();
      if (!(baseString.contains(afterString)))
        return new OXPathType("");
      final int pos = (args.size() == 2) ? 1 : args.get(2).number().intValue();
      if (pos == 0)
        return new OXPathType(baseString);
      int index = -1;
      for (int i = 0; i < pos; ++i) {
        index = baseString.indexOf(afterString, index + 1);
        if (index == -1)
          return new OXPathType("");
      }
      try {
        return new OXPathType(baseString.substring(index + afterString.length()));
      } catch (final Exception e) {
        // as for xpath specification
        return new OXPathType("");
      }
    }
  },

  /**
   * XPath <tt>substring</tt> function
   */
  SUBSTRING("substring", 2, 3) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      // -1 because XPath begins string index at 1, java at 0
      final String baseString = args.get(0).string();
      // round as for xpath specification
      int startIndex = (int) Math.round(args.get(1).number()) - 1;

      // as for xpath specification, if negative, it includes the first char
      if (startIndex < 0) {
        startIndex = 0;
      }
      String result = "";
      try {
        if (args.size() == 2) {
          result = baseString.substring(startIndex);
        } else {
          final int length = args.get(2).number().intValue();
          result = baseString.substring(startIndex, startIndex + length);

        }
      } catch (final Exception e) {
        // returning empyt string as XPath specification
      }
      return new OXPathType(result);

    }
  },

  /**
   * XPath extension <tt>substring-reverse</tt> function
   */
  SUBSTRINGREVERSE("substring-reverse", 2, 3) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}.
     * 
     * Returns a substring as XPath's {@literal substring} function, but, in contrast, the input string is reverted.
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final String baseString = args.get(0).string();
      final int startIndex = args.get(1).number().intValue() - 1;
      if (args.size() == 2)
        return new OXPathType(baseString.substring(0, baseString.length() - startIndex));
      else {
        final int length = args.get(2).number().intValue();
        try {
          return new OXPathType(baseString.substring(baseString.length() - startIndex - length, length));
        } catch (final Exception e) {
          return new OXPathType("");
        }
      }
    }
  },

  /**
   * XPath <tt>string-length</tt> function
   */
  SUBSTRINGLENGTH("string-length", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      if (args.isEmpty())
        return new OXPathType(new OXPathType(state.getContextSet()).string().length());
      else
        return new OXPathType(args.get(0).string().length());
    }
  },

  /**
   * XPath <tt>normalize-space</tt> function
   */
  NORMALIZESPACE("normalize-space", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      String text = null;

      if (args.isEmpty()) {
        text = new OXPathType(state.getContextSet()).string();
      } else {
        text = args.get(0).string();
      }

      return new OXPathType(EscapingUtils.normalizeTextNodes(text));
    }
  },

  /**
   * XPath <tt>translate</tt> function
   */
  TRANSLATE("translate", 3, 3) {
//	  TODO Correct function according to https://developer.mozilla.org/en-US/docs/Web/XPath/Functions/translate 
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final String target = args.get(1).string();
      String replacement = args.get(2).string();
      if (replacement.length() > target.length()) {
        replacement = replacement.substring(0, target.length());
      }
      return new OXPathType(args.get(0).string().replace(target, replacement));
    }
  },
  /**
   * XPath 2.0 <tt>replace</tt> function
   */
  REPLACE("replace", 3, 3) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 2.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final String string = args.get(0).string();
      final String pattern = args.get(1).string();
      final String replacement = args.get(2).string();
      return new OXPathType(string.replaceFirst(pattern, replacement));
    }
  },

  /**
   * XPath 2.0<tt>matches</tt> function
   */
  MATCHES("matches", 2, 2) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      final String string = args.get(0).string();
      final String pattern = args.get(1).string();
      return new OXPathType(string.matches(pattern));
    }
  },

  /**
   * XPath <tt>Boolean</tt> function
   */
  BOOLEAN("boolean", 1, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(args.get(0).booleanValue());
    }
  },

  /**
   * XPath <tt>not</tt> function
   */
  NOT("not", 1, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(!args.get(0).booleanValue());
    }
  },

  /**
   * XPath <tt>true</tt> function
   */
  TRUE("true", 0, 0) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(true);
    }
  },

  /**
   * XPath <tt>false</tt> function
   */
  FALSE("false", 0, 0) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(false);
    }
  },

  /**
   * XPath <tt>lang</tt> function
   */
  LANG("lang", 1, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return state.getContextSet().first().getByXPath("lang(" + args.get(0).string() + ")");
    }
  },

  /**
   * XPath <tt>number</tt> function
   */
  NUMBER("number", 0, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      if (args.isEmpty())
        return new OXPathType(new OXPathType(state.getContextSet()).number());
      return new OXPathType(args.get(0).number());
    }
  },

  /**
   * XPath <tt>sum</tt> function
   */
  SUM("sum", 1, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      double sum = 0;
      for (final OXPathContextNode o : args.get(0).nodeList()) {

        final ArrayList<OXPathType> newArrayList = Lists.newArrayList(new OXPathType(o));
        // final double d = o.getByXPath("number(.)").number();
        final double d = XPathFunctions.NUMBER.evaluate(newArrayList, state).number();
        sum += d;
      }
      return new OXPathType(sum);
    }
  },

  /**
   * XPath <tt>floor</tt> function
   */
  FLOOR("floor", 1, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(Math.floor(args.get(0).number()));
    }
  },

  /**
   * XPath <tt>ceiling</tt> function
   */
  CEILING("ceiling", 1, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(Math.ceil(args.get(0).number()));
    }
  },

  /**
   * XPath <tt>round</tt> function
   */
  ROUND("round", 1, 1) {
    /**
     * Evaluates the function (by PAAT with a list of arguments). Number of arguments is not checked. This should be
     * checked beforehand with {@code checkParameterCount(int)}
     *
     * @param args
     *          the list of parameters, computed as a list of {@code OXPathType} objects
     * @param state
     *          the state of the evaluation
     * @return the return value of the function as prescribed in the XPath 1.0 standard {@link http
     *         ://www.w3.org/TR/xpath/}
     * @throws OXPathException
     *           in case of function error
     */
    @Override
    public OXPathType evaluate(final ArrayList<OXPathType> args, final PAATStateEvalSet state) throws OXPathException {
      return new OXPathType(Math.round(args.get(0).number()));
    }
  };

  /**
   * Constructor for each object in the {@code enum}
   *
   * @param iName
   *          name of the function
   * @param minP
   *          minimum number of parameters it accepts
   * @param maxP
   *          maximum number of parameters it accepts
   */
  private XPathFunctions(final String iName, final int minP, final int maxP) {
    name = iName;
    minParam = minP;
    maxParam = maxP;
  }

  /**
   * @param args
   * @param state
   * @param xPathFunctions
   * @return
   * @throws OXPathException
   */
  protected OXPathType innerOrOuterHTML(final ArrayList<OXPathType> args, final PAATStateEvalSet state,
      final XPathFunctions function) throws OXPathException {

    final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);
    final OXPathContextNode contextNode = checkProconditionOnContextSet(state, getName(), logger);
    OXPathType oxPathType = null;
    if (args.isEmpty()) {
      oxPathType = new OXPathType(contextNode);
    } else {
      // the argument MUST be an element type
      oxPathType = args.get(0);// getArgumentAsNodeSet(args, getName(), 0, logger);
    }
    // if the argument or context is not a node, return empty
    final IOXPathNodeList nodeList = oxPathType.nodeList();
    if (nodeList.isEmpty())
      return new OXPathType();

    final DOMNode arg = nodeList.first().getNode();
    return executeInnerOrOuterOnElementOrThrow(function, arg, logger);

  }

  /**
   * Returns the function name (no parentheses)
   *
   * @return the function name (no parentheses)
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Returns the minimum number of parameters this function accepts
   *
   * @return the minimum number of parameters this function accepts
   */
  @Override
  public int getMinParameters() {
    return minParam;
  }

  /**
   * Returns the maximum number of parameters this function accepts
   *
   * @return the maximum number of parameters this function accepts
   */
  @Override
  public int getMaxParameters() {
    return maxParam;
  }

  /**
   * Given the number of parameters, can statically check if this function accepts this number of parameters
   *
   * @param numParam
   *          num of parameters to check in a function call
   * @return {@code true} if the number of parameters are legal; {@code false} otherwise
   */
  @Override
  public boolean checkParameterCount(final int numParam) {
    return ((numParam >= minParam) && (numParam <= maxParam)) ? true : false;
  }

  /**
   * instance field encoding name for each object
   */
  private final String name;
  /**
   * instance field encoding minimum parameters for each object
   */
  private final int minParam;
  /**
   * instance field encoding maximum parameters for each object
   */
  private final int maxParam;

  private static OXPathContextNode checkProconditionOnContextSet(final PAATStateEvalSet state,
      final String functionName, final Logger logger) {
    final IOXPathNodeList nodeList = state.getContextSet();
    if (nodeList.isEmpty())
      throw new OXPathRuntimeException("Cannot evaluate Function: " + functionName + " on an empty context set", logger);
    final OXPathContextNode contextNode = nodeList.first();
    if (contextNode.isNotionalContext())
      throw new OXPathRuntimeException(
          "Cannot evaluate Function: " + functionName + " on the  notational context node", logger);
    return contextNode;
  }

  private static OXPathType getArgumentAsNodeSet(final ArrayList<OXPathType> args, final String functionName,
      final int position, final Logger logger) throws OXPathException {
    final OXPathType oxPathType = args.get(position);
    if (oxPathType.isType() != OXPathTypes.NODESET)
      throw new OXPathRuntimeException("Function " + functionName + " expects a nodeset as argument in position "
          + position + ", but got type: " + oxPathType.toString(), logger);
    return oxPathType;
  }

  private static DOMNode getSingleDOMNode(final OXPathType oxPathType, final String functionName, final Logger logger)
      throws OXPathException {
    if (oxPathType.isType() != OXPathTypes.NODESET)
      throw new OXPathRuntimeException("Function " + functionName
          + " expects a single node as argument, but instead got type: " + oxPathType.toString(), logger);
    if (oxPathType.nodeList().size() != 1)
      throw new OXPathRuntimeException("Function " + functionName
          + " expects a single node as argument, but instead got " + oxPathType.nodeList().size() + " nodes", logger);
    return oxPathType.nodeList().first().getNode();
  }

  /**
   * @param function
   * @param arg
   * @param logger
   * @return
   */
  private static OXPathType executeInnerOrOuterOnElementOrThrow(final XPathFunctions function, final DOMNode arg,
      final Logger logger) {
    if (arg.getNodeType() != Type.ELEMENT)
      throw new OXPathRuntimeException("Function " + function + " is only defined on element nodes, not on "
          + arg.getNodeType(), logger);

    final DOMElement el = (DOMElement) arg;
    if (function == INNERHTML)
      return new OXPathType(el.getInnerHTML());
    else if (function == XPathFunctions.OUTERHTML)
      return new OXPathType(el.getOuterHTML());

    throw new OXPathRuntimeException("Expected innerHTML/outerHTML function call, instead got " + function, logger);

  }

  /**
   * @author Giovanni Grasso <gio@oxpath.org>
   *
   */
  final class InnerOrOuterHTMLFunction implements Function<OXPathContextNode, String> {
    private final XPathFunctions function;

    InnerOrOuterHTMLFunction(final XPathFunctions function) {
      this.function = function;
    }

    @Override
    public String apply(final OXPathContextNode input) {

      try {
        final DOMNode node = input.getNode();
        // this is safe, we build only string types
        return executeInnerOrOuterOnElementOrThrow(function, node, logger).string();
      } catch (final OXPathException e) {
        throw new OXPathRuntimeException("This should not happen! Error performing string() on result of " + function,
            e, logger);
      }

    }
  }

  final class TextContentFunction implements Function<OXPathContextNode, String> {
    @Override
    public String apply(final OXPathContextNode input) {

      final DOMNode node = input.getNode();
      if (node.getNodeType() == Type.DOCUMENT)
        return ((DOMDocument) node).getDocumentElement().getTextContent();
      // return input.getByXPath("string(.)").string();
      else
        return node.getTextContent();

    }
  }

  final class ToXMLFunction implements Function<OXPathContextNode, String> {
    @Override
    public String apply(final OXPathContextNode input) {

      return input.getNode().getBrowser().js().asXLM(input.getNode());

    }
  }

  private static final Logger logger = LoggerFactory.getLogger(XPathFunctions.class);

  final class NormalizeSpaceFunction implements Function<OXPathContextNode, String> {
    @Override
    public String apply(final OXPathContextNode input) {

      return EscapingUtils.normalizeTextNodes(new TextContentFunction().apply(input));

    }
  }
}