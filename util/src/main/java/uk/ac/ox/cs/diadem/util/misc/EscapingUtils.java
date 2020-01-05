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
package uk.ac.ox.cs.diadem.util.misc;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;

import uk.ac.ox.cs.diadem.util.exception.UtilRuntimeException;

/**
 * A collection of methods for escaping especially useful for dlv
 * @author Giovanni Grasso (giovannigrasso@gmail.com) Oxford University, Department of Computer Science
 */
public class EscapingUtils {
  private static final Logger logger = LoggerFactory.getLogger(EscapingUtils.class);
  
  /**
   * Turns a {@link URL} into a {@link URI}, turning any exceptions into runtime exceptions. NEVER USE
   * {@link URL#toURI()} as that method fails if the URL contains unencoded, unsafe characters such as | which are
   * allowed in java's URL implementation but not in URI.
   */
  public static URI urlToUri(final URL url) {
    try {
      return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(),
          url.getRef());
    } catch (final URISyntaxException e) {
      throw new UtilRuntimeException("URL " + url + " can not be parsed as URI!", e, logger);
    }
  }

  /**
   * Turns a {@link String} into a {@link URI}.
 * @throws MalformedURLException 
   */
  /*public static URI stringToUri(final String urlString) throws MalformedURLException {
    try {
      final URL url = stringToUrl(urlString);
      return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(),
          url.getRef());
    } catch (final URISyntaxException e) {
      throw new DiademRuntimeException("String " + urlString + " can not be parsed as URI!", e, logger);
    }
  }*/

  /**
   * Compares two URLs ignoring # or / at the end.
   */
  public static boolean sameURL(final URL url, final URL ourl) {
    final String u = stripEmptyFragment(url.toString());
    final String v = stripEmptyFragment(ourl.toString());
    return u.equals(v);
  }

  private static String stripEmptyFragment(final String string) {
    while (string.endsWith("#") || string.endsWith("/")) {
      return string.substring(0, string.length() - 1);
    }
    return string;
  }

  /**
   * Turns a {@link String} into a {@link URL}.
 * @throws MalformedURLException 
   */
  public static URL stringToUrl(final String urlString) throws MalformedURLException {
    return new URL(urlString);
  }

  /**
   * Quotes a string if not a representation of an integer number >= 0 (dlv does not support it).
   * @param value the string to quote
   * @return a string if not a representation of an integer number >= 0 (dlv does not support it).
   */
  /*public static String quoteIfNotNumberGreaterThenZero(final String value) {
    try {
      if (Integer.parseInt(value) < 0) {
        return "\"" + value + "\"";
      }
      return value;
    } catch (final NumberFormatException e) {
      return "\"" + EscapingUtils.escapeStringContentForDLV(value) + "\"";
    }
  }*/

  public static boolean isGarbageText(final String value) {
    return value.matches("[\\s]+") || EscapingUtils.isNonBreakingSpace(value);
    // return CharMatcher.WHITESPACE.matchesAllOf(value);
  }

  /**
   * Returns true is the given string is a constant number
   * @param string the input string
   * @return true is the given string is a constant number, false otherwise
   */
  public static boolean isConstantNumber(final String string) {

    return string.matches("^\\d+$");
  }

  /**
   * Wraps a string into double quotes (producing "unquotedString")
   * @param unquotedString the value to quote
   * @return the string "unquotedString"
   */
  public static String quoteUnquotedString(final String unquotedString) {

    return EscapingUtils.DOUBLE_QUOTE_AS_A_STRING + unquotedString + EscapingUtils.DOUBLE_QUOTE_AS_A_STRING;
  }

  /**
   * Checks whether a string is properly quoted (e.g, "quotedStringValue")
   * @param quotedStringValue
   * @return <code>true</code> if quotedStringValue is properly quoted (e.g, "quotedStringValue"), <code>false</code>
   *         otherwise
   */
  public static boolean isQuotedString(final String quotedStringValue) {

    return quotedStringValue.startsWith(EscapingUtils.DOUBLE_QUOTE_AS_A_STRING)
        && quotedStringValue.endsWith(EscapingUtils.DOUBLE_QUOTE_AS_A_STRING);
  }

  /**
   * Checks if the given string matches the regex [\u00A0] for a non-breaking-space
   * @param nodeValue the string to check
   * @return <code>true</code> if the given string matches the regex [\u00A0], <code>false</code> otherwise
   */
  public static boolean isNonBreakingSpace(final String nodeValue) {

    return nodeValue.matches("[" + EscapingUtils.NON_BREAKING_SPACE + "]+");
  }

  public static final char SPECIAL_CHAR = '\u0003';
  public static final char DOUBLE_QUOTE_CHAR = '\"';
  private static final String DOUBLE_QUOTE_AS_A_STRING = "\"";
  public static final String NON_BREAKING_SPACE = "\u00A0";

  public static String normalizeTextNodes(final String nodeValue) {
    // TODO should use CharMatcher.WHITSPACE or something similar.
    return CharMatcher.anyOf("\u00A0\t\f\n\r ").trimAndCollapseFrom(nodeValue, ' ');
  }

  private EscapingUtils() {
  }

}