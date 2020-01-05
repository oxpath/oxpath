/**
 * isShown.JS
 * THIS FILE IS NOT USED DIRECTLY. INSTEAD isShown_mini.js IS ACTUALLY USED
 * AS GENERATED VIA http://closure-compiler.appspot.com/home
 */

/**
 * Determines whether an element is what a user would call "shown". This means
 * that the element is shown in the viewport of the browser, and only has
 * height and width greater than 0px, and that its visibility is not "hidden"
 * and its display property is not "none".
 * Options and Optgroup elements are treated as special cases: they are
 * considered shown iff they have a enclosing select element that is shown.
 *
 * @param {!Element} elem The element to consider.
 * @param {boolean=} opt_ignoreOpacity Whether to ignore the element's opacity
 *     when determining whether it is shown; defaults to false.
 * @return {boolean} Whether or not the element is visible.
 */
var bot_dom_isShown = function(elem, opt_ignoreOpacity) {
  if (!bot_dom_isElement(elem)) {
    throw new Error('Argument to isShown must be of type Element');
  }

  // Option or optgroup is shown iff enclosing select is shown (ignoring the
  // select's opacity).
  if (bot_dom_isElement(elem, "OPTION") ||
      bot_dom_isElement(elem, "OPTGROUP")) {
    var select = /**@type {Element}*/ (goog_dom_getAncestor(elem, function(e) {
      return bot_dom_isElement(e, "SELECT");
    }));
    return !!select && bot_dom_isShown(select, /*ignoreOpacity=*/true);
  }

  // Image map elements are shown if image that uses it is shown, and
  // the area of the element is positive.
  var imageMap = bot_dom_maybeFindImageMap_(elem);
  if (imageMap) {
    return !!imageMap.image &&
           imageMap.rect.width > 0 && imageMap.rect.height > 0 &&
           bot_dom_isShown(imageMap.image, opt_ignoreOpacity);
  }

  // Any hidden input is not shown.
  if (bot_dom_isElement(elem, "INPUT") &&
      elem.type.toLowerCase() == 'hidden') {
    return false;
  }

  // Any NOSCRIPT element is not shown.
  if (bot_dom_isElement(elem, "NOSCRIPT")) {
    return false;
  }

  // Any element with hidden visibility is not shown.
  if (bot_dom_getEffectiveStyle(elem, 'visibility') == 'hidden') {
    return false;
  }

  // Any element with a display style equal to 'none' or that has an ancestor
  // with display style equal to 'none' is not shown.
  function displayed(e) {
    if (bot_dom_getEffectiveStyle(e, 'display') == 'none') {
      return false;
    }
    var parent = bot_dom_getParentElement(e);
    return !parent || displayed(parent);
  }
  if (!displayed(elem)) {
    return false;
  }

  // Any transparent element is not shown.
  if (!opt_ignoreOpacity && bot_dom_getOpacity(elem) == 0) {
    return false;
  }

  // Any element without positive size dimensions is not shown.
  function positiveSize(e) {
    var rect = bot_dom_getClientRect(e);
    if (rect.height > 0 && rect.width > 0) {
      return true;
    }
    // A vertical or horizontal SVG Path element will report zero width or
    // height but is "shown" if it has a positive stroke-width.
    if (bot_dom_isElement(e, 'PATH') && (rect.height > 0 || rect.width > 0)) {
      var strokeWidth = bot_dom_getEffectiveStyle(e, 'stroke-width');
      return !!strokeWidth && (parseInt(strokeWidth, 10) > 0);
    }
    // Zero-sized elements should still be considered to have positive size
    // if they have a child element or text node with positive size, unless
    // the element has an 'overflow' style of 'hidden'.
    return bot_dom_getEffectiveStyle(e, 'overflow') != 'hidden' &&
    goog_array_some(e.childNodes, function(n) {
          return n.nodeType == Node.TEXT_NODE ||
                 (bot_dom_isElement(n) && positiveSize(n));
        });
  }
  if (!positiveSize(elem)) {
    return false;
  }

  // Elements that are hidden by overflow are not shown.
  function hiddenByOverflow(e) {
    return bot_dom_getOverflowState(e) == bot_dom_OverflowState.HIDDEN &&
        goog_array_every(e.childNodes, function(n) {
          return !bot_dom_isElement(n) || hiddenByOverflow(n);
        });
  }
  return !hiddenByOverflow(elem);
};

/**
 * Checks whether the element is currently scrolled into the parent's overflow
 * region, such that the offset given, relative to the top-left corner of the
 * element, is currently in the overflow region.
 * 
 * @param {!Element}
 *            element The element to check.
 * @param {!goog_math.Coordinate=}
 *            opt_coords Coordinate in the element, relative to the top-left
 *            corner of the element, to check. If none are specified, checks
 *            that the center of the element is in in the overflow.
 * @return {boolean} Whether the coordinates specified, relative to the element,
 *         are scrolled in the parent overflow.
 */
 var bot_dom_isInParentOverflow = function (element, opt_coords) {
   var parent = goog_style_getOffsetParent(element);
   var parentNode = bot_dom_getParentElement(element);/*
														 * goog_userAgent.GECKO ||
														 * goog_userAgent.IE ||
														 * goog_userAgent.OPERA ?
														 * bot_dom_getParentElement(element) :
														 * parent;
														 */

   // Gecko will skip the BODY tag when calling getOffsetParent. However,
 // the
   // combination of the overflow values on the BODY _and_ HTML tags
 // determine
   // whether scroll bars are shown, so we need to guarantee that both
 // values
   // are checked.
   // GIOG
   // if ((goog_userAgent.GECKO || goog_userAgent.IE ||goog_userAgent.OPERA) &&
   if(bot_dom_isElement(parentNode, "BODY")) {
     parent = parentNode;
   }

   if (parent && (bot_dom_getEffectiveStyle(parent, 'overflow') == 'scroll' ||
                  bot_dom_getEffectiveStyle(parent, 'overflow') == 'auto')) {
     var sizeOfParent = bot_dom_getElementSize(parent);
     var locOfParent = goog_style_getClientPosition(parent);
     var locOfElement = goog_style_getClientPosition(element);
     var offsetX, offsetY;
     if (opt_coords) {
       offsetX = opt_coords.x;
       offsetY = opt_coords.y;
     } else {
       var sizeOfElement = bot_dom_getElementSize(element);
       offsetX = sizeOfElement.width / 2;
       offsetY = sizeOfElement.height / 2;
     }
     var elementPointX = locOfElement.x + offsetX;
     var elementPointY = locOfElement.y + offsetY;
     if (elementPointX >= locOfParent.x + sizeOfParent.width) {
       return true;
     }
     if (elementPointX <= locOfParent.x) {
       return true;
     }
     if (elementPointY >= locOfParent.y + sizeOfParent.height) {
       return true;
     }
     if (elementPointY <= locOfParent.y) {
       return true;
     }
     return bot_dom_isInParentOverflow(parent);
   }
   return false;
 };

 /**
	 * @param {!Element}
	 *            element The element to use.
	 * @return {!goog_math.Size} The dimensions of the element.
	 */
 var bot_dom_getElementSize = function(element) {
   if (goog_isFunction(element['getBBox'])) {
     try {
       var bb = element['getBBox']();
       if (bb) {
         // Opera will return an undefined bounding box for SVG elements.
         // Which makes sense, but isn't useful.
         return bb;
       }
     } catch (e) {
       // Firefox will always throw for certain SVG elements,
       // even if the function exists.
     }
   }

   // If the element is the BODY, then get the visible size.
   if (bot_dom_isElement(element, "BODY")) {
     var doc = goog_dom_getOwnerDocument(element);
     var win = goog_dom_getWindow(doc) || undefined;
     if (!bot_dom_isBodyScrollBarShown_(element)) {
       return goog_dom_getViewportSize(win);
     }
     return bot_window_getInteractableSize(win);
   }

   return goog_style_getSize(element);
 };

 
 /**
	 * Determine the size of the window that a user could interact with. This
	 * will be the greatest of document.body.(width|scrollWidth), the same for
	 * document.documentElement or the size of the viewport.
	 * 
	 * @param {!Window=}
	 *            opt_win Window to determine the size of. Defaults to
	 *            bot_getWindow().
	 * @return {!goog_math.Size} The calculated size.
	 */
 var bot_window_getInteractableSize = function(opt_win) {
   var win = opt_win || bot_getWindow();
   var doc = win.document;
   var elem = doc.documentElement;
   var body = doc.body;

   var widths = [
       elem.clientWidth, elem.scrollWidth, elem.offsetWidth,
       body.scrollWidth, body.offsetWidth
   ];
   var heights = [
       elem.clientHeight, elem.scrollHeight, elem.offsetHeight,
       body.scrollHeight, body.offsetHeight
   ];

   var sortFunc = function(a, b) {
     return a - b;
   };

   var width = /** @type {number} */goog_array_peek(widths.sort(sortFunc)) || 0;
   var height = /** @type {number} */goog_array_peek(heights.sort(sortFunc)) || 0;

   return new goog_math.Size(width, height);
 };

 
 /**
	 * Gets the client rectangle of the DOM element. It often returns the same
	 * value as Element.getBoundingClientRect, but is "fixed" for various
	 * scenarios: 1. Like goog_style_getClientPosition, it adjusts for the inset
	 * border in IE. 2. Gets a rect for <map>'s and <area>'s relative to the
	 * image using them. 3. Gets a rect for SVG elements representing their true
	 * bounding box. 4. Defines the client rect of the <html> element to be the
	 * window viewport.
	 * 
	 * @param {!Element}
	 *            elem The element to use.
	 * @return {!goog_math.Rect} The interaction box of the element.
	 */
 var bot_dom_getClientRect = function(elem) {
   var imageMap = bot_dom_maybeFindImageMap_(elem);
   if (imageMap) {
     return imageMap.rect;
   } else if (bot_dom_isElement(elem, "HTML")) {
     // Define the client rect of the <html> element to be the viewport.
     var doc = goog_dom_getOwnerDocument(elem);
     var viewportSize = goog_dom_getViewportSize(goog_dom_getWindow(doc));
     return new goog_math_Rect(0, 0, viewportSize.width, viewportSize.height);
   } else {
     var nativeRect;
     try {
       // TODO: in IE and Firefox, getBoundingClientRect includes stroke
 // width,
       // but getBBox does not.
       nativeRect = elem.getBoundingClientRect();
     } catch (e) {
       // On IE < 9, calling getBoundingClientRect on an orphan element
 // raises
       // an "Unspecified Error". All other browsers return zeros.
       return new goog_math_Rect(0, 0, 0, 0);
     }

     var rect = new goog_math_Rect(nativeRect.left, nativeRect.top,
    	        nativeRect.right - nativeRect.left, nativeRect.bottom - nativeRect.top);

     // In IE, the element can additionally be offset by a border around the
     // documentElement or body element that we have to subtract.
     if (/* goog_userAgent.IE */false && elem.ownerDocument.body) {
       var doc = goog_dom_getOwnerDocument(elem);
       rect.left -= doc.documentElement.clientLeft + doc.body.clientLeft;
       rect.top -= doc.documentElement.clientTop + doc.body.clientTop;
     }

     // Opera sometimes falsely report zero size bounding rects.
     if (false && goog_userAgent.OPERA) {
       if (rect.width == 0 && elem.offsetWidth > 0) {
         rect.width = elem.offsetWidth;
       }
       if (rect.height == 0 && elem.offsetHeight > 0) {
         rect.height = elem.offsetHeight;
       }
     }

     /*
		 * GIOG : REMOVED // On Gecko < 12, getBoundingClientRect does not
		 * account for CSS transforms. // TODO: Remove this when we drop support
		 * for FF3.6 and FF10. if (goog_userAgent.GECKO &&
		 * !bot.userAgent.isEngineVersion(12)) {
		 * transformLegacyFirefoxClientRect(elem); }
		 */

     return rect;
   }

  
 };

 
 /**
	 * Trims leading and trailing whitespace from strings, leaving non-breaking
	 * space characters in place.
	 * 
	 * @param {string}
	 *            str The string to trim.
	 * @return {string} str without any leading or trailing whitespace
	 *         characters except non-breaking spaces.
	 * @private
	 */
 var bot_dom_trimExcludingNonBreakingSpaceCharacters_ = function(str) {
   return str.replace(/^[^\S\xa0]+|[^\S\xa0]+$/g, '');
 };


 /**
	 * Gets the opacity of a node (x-browser). This gets the inline style
	 * opacity of the node and takes into account the cascaded or the computed
	 * style for this node.
	 * 
	 * @param {!Element}
	 *            elem Element whose opacity has to be found.
	 * @return {number} Opacity between 0 and 1.
	 */
 var bot_dom_getOpacity = function(elem) {
   
 // GIOG:Changed to avoid IE stuff
 return bot_dom_getOpacityNonIE_(elem);
/*
 * // TODO: Does this need to deal with rgba colors? if
 * (!bot.userAgent.IE_DOC_PRE10) { return bot_dom_getOpacityNonIE_(elem); } else {
 * if (bot_dom_getEffectiveStyle(elem, 'position') == 'relative') { // Filter
 * does not apply to non positioned elements. return 1; }
 * 
 * var opacityStyle = bot_dom_getEffectiveStyle(elem, 'filter'); var groups =
 * opacityStyle.match(/^alpha\(opacity=(\d*)\)/) || opacityStyle.match(
 * /^progid:DXImageTransform.Microsoft.Alpha\(Opacity=(\d*)\)/);
 * 
 * if (groups) { return Number(groups[1]) / 100; } else { return 1; // Opaque. } }
 */
 };

 /**
	 * Implementation of getOpacity for browsers that do support the "opacity"
	 * style.
	 * 
	 * @param {!Element}
	 *            elem Element whose opacity has to be found.
	 * @return {number} Opacity between 0 and 1.
	 * @private
	 */
 var bot_dom_getOpacityNonIE_ = function(elem) {
   // By default the element is opaque.
   var elemOpacity = 1;

   var opacityStyle = bot_dom_getEffectiveStyle(elem, 'opacity');
   if (opacityStyle) {
     elemOpacity = Number(opacityStyle);
   }

   // Let's apply the parent opacity to the element.
   var parentElement = bot_dom_getParentElement(elem);
   if (parentElement) {
     elemOpacity = elemOpacity * bot_dom_getOpacityNonIE_(parentElement);
   }
   return elemOpacity;
 };

 /**
	 * Would a user see scroll bars on the BODY element? In the case where the
	 * BODY has "overflow: hidden", and HTML has "overflow: auto" or "overflow:
	 * scroll" set, there's a scroll bar, so it's as if the BODY has "overflow:
	 * auto" set. In all other cases where BODY has "overflow: hidden", there
	 * are no scrollbars. http://www.w3.org/TR/CSS21/visufx.html#overflow
	 * 
	 * @param {!Element}
	 *            bodyElement The element, which must be a BODY element.
	 * @return {boolean} Whether scrollbars would be visible to a user.
	 * @private
	 */
 var bot_dom_isBodyScrollBarShown_ = function(bodyElement) {
   if (!bot_dom_isElement(bodyElement, "BODY")) {
     // bail
     }

   var bodyOverflow = bot_dom_getEffectiveStyle(bodyElement, 'overflow');
   if (bodyOverflow != 'hidden') {
     return true;
   }

   var html = bot_dom_getParentElement(bodyElement);
   if (!html || !bot_dom_isElement(html, "HTML")) {
     return true; // Seems like a reasonable default.
   }

   var viewportOverflow = bot_dom_getEffectiveStyle(html, 'overflow');
   return viewportOverflow == 'auto' || viewportOverflow == 'scroll';
 };

 
 /**
	 * Returns the parent element of the given node, or null. This is required
	 * because the parent node may not be another element.
	 * 
	 * @param {!Node}
	 *            node The node who's parent is desired.
	 * @return {Element} The parent element, if available, null otherwise.
	 */
 var bot_dom_getParentElement = function(node) {
   var elem = node.parentNode;

   while (elem &&
          elem.nodeType != Node.ELEMENT_NODE &&
          elem.nodeType != Node.DOCUMENT_NODE &&
          elem.nodeType != Node.DOCUMENT_FRAGMENT_NODE) {
     elem = elem.parentNode;
   }
   return /** @type {Element} */ (bot_dom_isElement(elem) ? elem : null);
 };


 /**
	 * @param {!Element}
	 *            elem The element to consider.
	 * @return {string} visible text.
	 */
 var bot_dom_getVisibleText = function(elem) {
   var lines = [];
   bot_dom_appendVisibleTextLinesFromElement_(elem, lines);
   lines = goog_array_map(
       lines,
       bot_dom_trimExcludingNonBreakingSpaceCharacters_);
   var joined = lines.join('\n');
   var trimmed = bot_dom_trimExcludingNonBreakingSpaceCharacters_(joined);

   // Replace non-breakable spaces with regular ones.
   return trimmed.replace(/\xa0/g, ' ');
 };


 /**
	 * @param {!Element}
	 *            elem Element.
	 * @param {!Array.
	 *            <string>} lines Accumulated visible lines of text.
	 * @private
	 */
 var bot_dom_appendVisibleTextLinesFromElement_ = function(elem, lines) {
   function currLine() {
     return (/** @type {string|undefined} */ goog_array_peek(lines)) || '';
   }

   // TODO(gdennis): Add case here for textual form elements.
   if (bot_dom_isElement(elem, "BR")) {
     lines.push('');
   } else {
     // TODO: properly handle display:run-in
     var isTD = bot_dom_isElement(elem, "TD");
     var display = bot_dom_getEffectiveStyle(elem, 'display');
     // On some browsers, table cells incorrectly show up with block styles.
     var isBlock = !isTD &&
         !goog_array_contains(bot_dom_INLINE_DISPLAY_BOXES_, display);

     // Add a newline before block elems when there is text on the current
 // line,
     // except when the previous sibling has a display: run-in.
     // Also, do not run-in the previous sibling if this element is floated.

     var previousElementSibling = goog_dom_getPreviousElementSibling(elem);
     var prevDisplay = (previousElementSibling) ?
         bot_dom_getEffectiveStyle(previousElementSibling, 'display') : '';
     // TODO(dawagner): getEffectiveStyle should mask this for us
     var thisFloat = bot_dom_getEffectiveStyle(elem, 'float') ||
         bot_dom_getEffectiveStyle(elem, 'cssFloat') ||
         bot_dom_getEffectiveStyle(elem, 'styleFloat');
     var runIntoThis = prevDisplay == 'run-in' && thisFloat == 'none';
     if (isBlock && !runIntoThis && !goog_string_isEmpty(currLine())) {
       lines.push('');
     }

     // This element may be considered unshown, but have a child that is
     // explicitly shown (e.g. this element has "visibility:hidden").
     // Nevertheless, any text nodes that are direct descendants of this
     // element will not contribute to the visible text.
     var shown = bot_dom_isShown(elem);

     // All text nodes that are children of this element need to know the
     // effective "white-space" and "text-transform" styles to properly
     // compute their contribution to visible text. Compute these values
 // once.
     var whitespace = null, textTransform = null;
     if (shown) {
       whitespace = bot_dom_getEffectiveStyle(elem, 'white-space');
       textTransform = bot_dom_getEffectiveStyle(elem, 'text-transform');
     }

     goog_array_forEach(elem.childNodes, function(node) {
       if (node.nodeType == Node.TEXT_NODE && shown) {
         var textNode = (/** @type {!Text} */ node);
         bot_dom_appendVisibleTextLinesFromTextNode_(textNode, lines,
             whitespace, textTransform);
       } else if (bot_dom_isElement(node)) {
         var castElem = (/** @type {!Element} */ node);
         bot_dom_appendVisibleTextLinesFromElement_(castElem, lines);
       }
     });

     var line = currLine();

     // Here we differ from standard innerText implementations (if there were
     // such a thing). Usually, table cells are separated by a tab, but we
     // normalize tabs into single spaces.
     if ((isTD || display == 'table-cell') && line &&
         !goog_string_endsWith(line, ' ')) {
       lines[lines.length - 1] += ' ';
     }

     // Add a newline after block elems when there is text on the current
 // line,
     // and the current element isn't marked as run-in.
     if (isBlock && display != 'run-in' && !goog_string_isEmpty(line)) {
       lines.push('');
     }
   }
 };

 /**
  * @param {!Text} textNode Text node.
  * @param {!Array.<string>} lines Accumulated visible lines of text.
  * @param {?string} whitespace Parent element's "white-space" style.
  * @param {?string} textTransform Parent element's "text-transform" style.
  * @private
  */
 bot_dom_appendVisibleTextLinesFromTextNode_ = function(textNode, lines,
     whitespace, textTransform) {
   // First, remove zero-width characters. Do this before regularizing spaces as
   // the zero-width space is both zero-width and a space, but we do not want to
   // make it visible by converting it to a regular space.
   // The replaced characters are:
   //   U+200B: Zero-width space
   //   U+200E: Left-to-right mark
   //   U+200F: Right-to-left mark
   var text = textNode.nodeValue.replace(/[\u200b\u200e\u200f]/g, '');

   // Canonicalize the new lines, and then collapse new lines
   // for the whitespace styles that collapse. See:
   // https://developer.mozilla.org/en/CSS/white-space
   text = goog_string_canonicalizeNewlines(text);
   if (whitespace == 'normal' || whitespace == 'nowrap') {
     text = text.replace(/\n/g, ' ');
   }

   // For pre and pre-wrap whitespace styles, convert all breaking spaces to be
   // non-breaking, otherwise, collapse all breaking spaces. Breaking spaces are
   // converted to regular spaces by getVisibleText().
   if (whitespace == 'pre' || whitespace == 'pre-wrap') {
     text = text.replace(/[ \f\t\v\u2028\u2029]/g, '\xa0');
   } else {
     text = text.replace(/[\ \f\t\v\u2028\u2029]+/g, ' ');
   }

   if (textTransform == 'capitalize') {
     text = text.replace(/(^|\s)(\S)/g, function() {
       return arguments[1] + arguments[2].toUpperCase();
     });
   } else if (textTransform == 'uppercase') {
     text = text.toUpperCase();
   } else if (textTransform == 'lowercase') {
     text = text.toLowerCase();
   }

   var currLine = lines.pop() || '';
   if (goog_string_endsWith(currLine, ' ') &&
       goog_string_startsWith(text, ' ')) {
     text = text.substr(1);
   }
   lines.push(currLine + text);
 };

 /**
  * Replaces Windows and Mac new lines with unix style: \r or \r\n with \n.
  * @param {string} str The string to in which to canonicalize newlines.
  * @return {string} {@code str} A copy of {@code} with canonicalized newlines.
  */
 var goog_string_canonicalizeNewlines = function(str) {
   return str.replace(/(\r\n|\r|\n)/g, '\n');
 };
 
 /**
	 * Fast suffix-checker.
	 * 
	 * @param {string}
	 *            str The string to check.
	 * @param {string}
	 *            suffix A string to look for at the end of {@code str}.
	 * @return {boolean} True if {@code str} ends with {@code suffix}.
	 */
 var goog_string_endsWith = function(str, suffix) {
   var l = str.length - suffix.length;
   return l >= 0 && str.indexOf(suffix, l) == l;
 };
 
 /**
	 * Elements with one of these effective "display" styles are treated as
	 * inline display boxes and have their visible text appended to the current
	 * line.
	 * 
	 * @private {!Array.<string>}
	 * @const
	 */
 var bot_dom_INLINE_DISPLAY_BOXES_ = [
   'inline',
   'inline-block',
   'inline-table',
   'none',
   'table-cell',
   'table-column',
   'table-column-group'
 ];


 /**
	 * Calls a function for each element in an array. Skips holes in the array.
	 * See {@link http://tinyurl.com/developer-mozilla-org-array-foreach}
	 * 
	 * @param {Array.
	 *            <T>|goog_array.ArrayLike} arr Array or array like object over
	 *            which to iterate.
	 * @param {?function(this:
	 *            S, T, number, ?): ?} f The function to call for every element.
	 *            This function takes 3 arguments (the element, the index and
	 *            the array). The return value is ignored.
	 * @param {S=}
	 *            opt_obj The object to be used as the value of 'this' within f.
	 * @template T,S
	 */
 var goog_array_forEach = /*
							 * goog_NATIVE_ARRAY_PROTOTYPES &&
							 * goog_array.ARRAY_PROTOTYPE_.forEach ?
							 * function(arr, f, opt_obj) {
							 * goog_asserts.assert(arr.length != null);
							 * 
							 * goog_array.ARRAY_PROTOTYPE_.forEach.call(arr, f,
							 * opt_obj); } :
							 */
     function(arr, f, opt_obj) {
       var l = arr.length;  // must be fixed during loop... see docs
       var arr2 = goog_isString(arr) ? arr.split('') : arr;
       for (var i = 0; i < l; i++) {
         if (i in arr2) {
           f.call(opt_obj, arr2[i], i, arr);
         }
       }
     };
     
 /**
	 * Checks if a string is empty or contains only whitespaces.
	 * 
	 * @param {string}
	 *            str The string to check.
	 * @return {boolean} True if {@code str} is empty or whitespace only.
	 */
 var goog_string_isEmpty = function(str) {
   // testing length == 0 first is actually slower in all browsers (about
 // the
   // same in Opera).
   // Since IE doesn't include non-breaking-space (0xa0) in their \s
 // character
   // class (as required by section 7.2 of the ECMAScript spec), we
 // explicitly
   // include it in the regexp to enforce consistent cross-browser
 // behavior.
   return /^[\s\xa0]*$/.test(str);
 };
 
 
 /**
	 * Returns the first previous sibling that is an element.
	 * 
	 * @param {Node}
	 *            node The node to get the previous sibling element of.
	 * @return {Element} The first previous sibling of {@code node} that is an
	 *         element.
	 */
 var goog_dom_getPreviousElementSibling = function(node) {
   if (node.previousElementSibling != undefined) {
     return /** @type {Element} */(node).previousElementSibling;
   }
   return goog_dom_getNextElementNode_(node.previousSibling, false);
 };


 /**
	 * Returns the first node that is an element in the specified direction,
	 * starting with {@code node}.
	 * 
	 * @param {Node}
	 *            node The node to get the next element from.
	 * @param {boolean}
	 *            forward Whether to look forwards or backwards.
	 * @return {Element} The first element.
	 * @private
	 */
 var goog_dom_getNextElementNode_ = function(node, forward) {
   while (node && node.nodeType != Node.ELEMENT_NODE) {
     node = forward ? node.nextSibling : node.previousSibling;
   }

   return /** @type {Element} */ (node);
 };
 
 /**
	 * Whether the array contains the given object.
	 * 
	 * @param {goog_array.ArrayLike}
	 *            arr The array to test for the presence of the element.
	 * @param {*}
	 *            obj The object for which to test.
	 * @return {boolean} true if obj is present.
	 */
 var goog_array_contains = function(arr, obj) {
   return goog_array_indexOf(arr, obj) >= 0;
 };
 
 /**
	 * Returns the index of the first element of an array with a specified
	 * value, or -1 if the element is not present in the array.
	 * 
	 * See {@link http://tinyurl.com/developer-mozilla-org-array-indexof}
	 * 
	 * @param {goog_array.ArrayLike}
	 *            arr The array to be searched.
	 * @param {*}
	 *            obj The object for which we are searching.
	 * @param {number=}
	 *            opt_fromIndex The index at which to start the search. If
	 *            omitted the search starts at index 0.
	 * @return {number} The index of the first matching array element.
	 */
 var goog_array_indexOf = /*
							 * goog_NATIVE_ARRAY_PROTOTYPES &&
							 * goog_array.ARRAY_PROTOTYPE_.indexOf ?
							 * function(arr, obj, opt_fromIndex) {
							 * goog_asserts.assert(arr.length != null);
							 * 
							 * return
							 * goog_array.ARRAY_PROTOTYPE_.indexOf.call(arr,
							 * obj, opt_fromIndex); } :
							 */
     function(arr, obj, opt_fromIndex) {
       var fromIndex = opt_fromIndex == null ?
           0 : (opt_fromIndex < 0 ?
                Math.max(0, arr.length + opt_fromIndex) : opt_fromIndex);

       if (goog_isString(arr)) {
         // Array.prototype.indexOf uses === so only strings should be found.
         if (!goog_isString(obj) || obj.length != 1) {
           return -1;
         }
         return arr.indexOf(obj, fromIndex);
       }

       for (var i = fromIndex; i < arr.length; i++) {
         if (i in arr && arr[i] === obj)
           return i;
       }
       return -1;
     };
     
 var goog_dom_getOwnerDocument = function(node) {
   // TODO(arv): Remove IE5 code.
   // IE5 uses document instead of ownerDocument
   return /** @type {!Document} */ (
       node.nodeType == Node.DOCUMENT_NODE ? node :
       node.ownerDocument || node.document);
 };


 var bot_dom_isElement = function(node, opt_tagName) {
   return !!node && node.nodeType == 1 &&
       (!opt_tagName || node.tagName.toUpperCase() == opt_tagName);
 };



 /**
	 * Walks up the DOM hierarchy returning the first ancestor that passes the
	 * matcher function.
	 * 
	 * @param {Node}
	 *            element The DOM node to start with.
	 * @param {function(Node) :
	 *            boolean} matcher A function that returns true if the passed
	 *            node matches the desired criteria.
	 * @param {boolean=}
	 *            opt_includeNode If true, the node itself is included in the
	 *            search (the first call to the matcher will pass startElement
	 *            as the node to test).
	 * @param {number=}
	 *            opt_maxSearchSteps Maximum number of levels to search up the
	 *            dom.
	 * @return {Node} DOM node that matched the matcher, or null if there was no
	 *         match.
	 */
 var goog_dom_getAncestor = function(
     element, matcher, opt_includeNode, opt_maxSearchSteps) {
   if (!opt_includeNode) {
     element = element.parentNode;
   }
   var ignoreSearchSteps = opt_maxSearchSteps == null;
   var steps = 0;
   while (element && (ignoreSearchSteps || steps <= opt_maxSearchSteps)) {
     if (matcher(element)) {
       return element;
     }
     element = element.parentNode;
     steps++;
   }
   // Reached the root of the DOM without a match
   return null;
 };
 
 /**
	 * Retrieves the implicitly-set, effective style of an element, or null if
	 * it is unknown. It returns the computed style where available; otherwise
	 * it looks up the DOM tree for the first style value not equal to
	 * 'inherit,' using the IE currentStyle of each node if available, and
	 * otherwise the inline style. Since the computed, current, and inline
	 * styles can be different, the return value of this function is not always
	 * consistent across browsers. See:
	 * http://code.google.com/p/doctype/wiki/ArticleComputedStyleVsCascadedStyle
	 * 
	 * @param {!Element}
	 *            elem Element to get the style value from.
	 * @param {string} propertyName Name of the CSS property.
	 * @return {?string} The value of the style property, or null.
	 */
 var bot_dom_getEffectiveStyle = function(elem, propertyName) {
   var styleName = goog_string_toCamelCase(propertyName);
   if (styleName == 'float' ||
       styleName == 'cssFloat' ||
       styleName == 'styleFloat') {
     // GIOG REMOVED styleName = bot.userAgent.IE_DOC_PRE9 ? 'styleFloat'
 // : 'cssFloat';
   styleName = 'cssFloat';
   }
   var style = goog_style_getComputedStyle(elem, styleName) ||
       bot_dom_getCascadedStyle_(elem, styleName);
   if (style === null) {
     return null;
   }
   return bot_color_standardizeColor(styleName, style);
 };

 

 
 
 /**
	 * Retrieves a computed style value of a node. It returns empty string if
	 * the value cannot be computed (which will be the case in Internet
	 * Explorer) or "none" if the property requested is an SVG one and it has
	 * not been explicitly set (firefox and webkit).
	 * 
	 * @param {Element}
	 *            element Element to get style of.
	 * @param {string}
	 *            property Property to get (camel-case).
	 * @return {string} Style value.
	 */
 var goog_style_getComputedStyle = function(element, property) {
   var doc = goog_dom_getOwnerDocument(element);
   if (doc.defaultView && doc.defaultView.getComputedStyle) {
     var styles = doc.defaultView.getComputedStyle(element, null);
     if (styles) {
       // element.style[..] is undefined for browser specific styles
       // as 'filter'.
       return styles[property] || styles.getPropertyValue(property) || '';
     }
   }

   return '';
 };
 
 /**
	 * Looks up the DOM tree for the first style value not equal to 'inherit,'
	 * using the currentStyle of each node if available, and otherwise the
	 * inline style.
	 * 
	 * @param {!Element}
	 *            elem Element to get the style value from.
	 * @param {string}
	 *            styleName CSS style property in camelCase.
	 * @return {?string} The value of the style property, or null.
	 * @private
	 */
 var bot_dom_getCascadedStyle_ = function(elem, styleName) {
   var style = elem.currentStyle || elem.style;
   var value = style[styleName];
   if (!goog_isDef(value) && goog_isFunction(style['getPropertyValue'])) {
     value = style['getPropertyValue'](styleName);
   }

   if (value != 'inherit') {
     return goog_isDef(value) ? value : null;
   }
   var parent = bot_dom_getParentElement(elem);
   return parent ? bot_dom_getCascadedStyle_(parent, styleName) : null;
 };
 
 /**
	 * Returns true if the specified value is not |undefined|. WARNING: Do not
	 * use this to test if an object has a property. Use the in operator
	 * instead. Additionally, this function assumes that the global undefined
	 * variable has not been redefined.
	 * 
	 * @param {*}
	 *            val Variable to test.
	 * @return {boolean} Whether variable is defined.
	 */
 var goog_isDef = function(val) {
   return val !== undefined;
 }; 
 /**
	 * Converts a string from selector-case to camelCase (e.g. from
	 * "multi-part-string" to "multiPartString"), useful for converting CSS
	 * selectors and HTML dataset keys to their equivalent JS properties.
	 * 
	 * @param {string}
	 *            str The string in selector-case form.
	 * @return {string} The string in camelCase form.
	 */
 var goog_string_toCamelCase = function(str) {
   return String(str).replace(/\-([a-z])/g, function(all, match) {
     return match.toUpperCase();
   });
 };
 
 /**
	 * If given a <map> or <area> element, finds the corresponding image and
	 * client rectangle of the element; otherwise returns null. The return value
	 * is an object with 'image' and 'rect' properties. When no image uses the
	 * given element, the returned rectangle is present but has zero size.
	 * 
	 * @param {!Element}
	 *            elem Element to test.
	 * @return {?{image: Element, rect: !goog_math.Rect}} Image and rectangle.
	 * @private
	 */
 var bot_dom_maybeFindImageMap_ = function(elem) {
   // If not a <map> or <area>, return null indicating so.
   var isMap = bot_dom_isElement(elem, "MAP");
   if (!isMap && !bot_dom_isElement(elem, "AREA")) {
     return null;
   }

   // Get the <map> associated with this element, or null if none.
   var map = isMap ? elem :
       (bot_dom_isElement(elem.parentNode, "MAP") ?
           elem.parentNode : null);

   var image = null, rect = null;
   if (map && map.name) {
     var mapDoc = goog_dom_getOwnerDocument(map);

     // The "//*" XPath syntax can confuse the closure compiler, so we
 // use
     // the "/descendant::*" syntax instead.
     // TODO: Try to find a reproducible case for the compiler bug.
     // TODO: Restrict to applet, img, input:image, and object nodes.
     var imageXpath = '/descendant::*[@usemap = "#' + map.name + '"]';

     // TODO: Break dependency of bot.locators on bot.dom,
     // so bot.locators.findElement can be called here instead.
    
     // GIOG image = bot.locators.xpath.single(imageXpath, mapDoc);
     image = getNodeByXPath(imageXpath, mapDoc);

     if (image) {
       rect = bot_dom_getClientRect(image);
       if (!isMap && elem.shape.toLowerCase() != 'default') {
         // Shift and crop the relative area rectangle to the map.
         var relRect = bot_dom_getAreaRelativeRect_(elem);
         var relX = Math.min(Math.max(relRect.left, 0), rect.width);
         var relY = Math.min(Math.max(relRect.top, 0), rect.height);
         var w = Math.min(relRect.width, rect.width - relX);
         var h = Math.min(relRect.height, rect.height - relY);
         rect = new goog_math_Rect(relX + rect.left, relY + rect.top, w, h);
       }
     }
   }

   return {image: image, rect: rect || new goog_math_Rect(0, 0, 0, 0)};
 };

 /**
	 * Returns the bounding box around an <area> element relative to its
	 * enclosing <map>. Does not apply to <area> elements with shape=='default'.
	 * 
	 * @param {!Element}
	 *            area Area element.
	 * @return {!goog_math.Rect} Bounding box of the area element.
	 * @private
	 */
 var bot_dom_getAreaRelativeRect_ = function(area) {
   var shape = area.shape.toLowerCase();
   var coords = area.coords.split(',');
   if (shape == 'rect' && coords.length == 4) {
     var x = coords[0], y = coords[1];
     return new goog_math_Rect(x, y, coords[2] - x, coords[3] - y);
   } else if (shape == 'circle' && coords.length == 3) {
     var centerX = coords[0], centerY = coords[1], radius = coords[2];
     return new goog_math_Rect(centerX - radius, centerY - radius,
                               2 * radius, 2 * radius);
   } else if (shape == 'poly' && coords.length > 2) {
     var minX = coords[0], minY = coords[1], maxX = minX, maxY = minY;
     for (var i = 2; i + 1 < coords.length; i += 2) {
       minX = Math.min(minX, coords[i]);
       maxX = Math.max(maxX, coords[i]);
       minY = Math.min(minY, coords[i + 1]);
       maxY = Math.max(maxY, coords[i + 1]);
     }
     return new goog_math_Rect(minX, minY, maxX - minX, maxY - minY);
   }
   return new goog_math_Rect(0, 0, 0, 0);
 };
 
 /**
	 * Returns the overflow state of the given element.
	 * 
	 * If an optional coordinate or rectangle region is provided, returns the
	 * overflow state of that region relative to the element. A coordinate is
	 * treated as a 1x1 rectangle whose top-left corner is the coordinate.
	 * 
	 * @param {!Element}
	 *            elem Element.
	 * @param {!(goog_math.Coordinate|goog_math.Rect)=}
	 *            opt_region Coordinate or rectangle relative to the top-left
	 *            corner of the element.
	 * @return {bot.dom.OverflowState} Overflow state of the element.
	 */
 var bot_dom_getOverflowState = function(elem, opt_region) {
   var region = bot_dom_getClientRegion(elem, opt_region);
   var ownerDoc = goog_dom_getOwnerDocument(elem);
   var htmlElem = ownerDoc.documentElement;
   var bodyElem = ownerDoc.body;
   var htmlOverflowStyle = bot_dom_getEffectiveStyle(htmlElem, 'overflow');
   var treatAsFixedPosition;

   // Return the closest ancestor that the given element may overflow.
   function getOverflowParent(e) {
     var position = bot_dom_getEffectiveStyle(e, 'position');
     if (position == 'fixed') {
       treatAsFixedPosition = true;
       // Fixed-position element may only overflow the viewport.
       return e == htmlElem ? null : htmlElem;
     } else {
       var parent = bot_dom_getParentElement(e);
       while (parent && !canBeOverflowed(parent)) {
         parent = bot_dom_getParentElement(parent);
       }
       return parent;
     }

     function canBeOverflowed(container) {
       // The HTML element can always be overflowed.
       if (container == htmlElem) {
         return true;
       }
       // An element cannot overflow an element with an inline display
 // style.
       var containerDisplay = /** @type {string} */ (
           bot_dom_getEffectiveStyle(container, 'display'));
       if (goog_string_startsWith(containerDisplay, 'inline')) {
         return false;
       }
       // An absolute-positioned element cannot overflow a
 // static-positioned one.
       if (position == 'absolute' &&
           bot_dom_getEffectiveStyle(container, 'position') == 'static') {
         return false;
       }
       return true;
     }
   }

// Return the x and y overflow styles for the given element.
   function getOverflowStyles(e) {
     // When the <html> element has an overflow style of 'visible', it
 // assumes
     // the overflow style of the body, and the body is really
 // overflow:visible.
     var overflowElem = e;
     if (htmlOverflowStyle == 'visible') {
       // Note: bodyElem will be null/undefined in SVG documents.
       if (e == htmlElem && bodyElem) {
         overflowElem = bodyElem;
       } else if (e == bodyElem) {
         return {x: 'visible', y: 'visible'};
       }
     }
     var overflow = {
       x: bot_dom_getEffectiveStyle(overflowElem, 'overflow-x'),
       y: bot_dom_getEffectiveStyle(overflowElem, 'overflow-y')
     };
     // The <html> element cannot have a genuine 'visible' overflow
 // style,
     // because the viewport can't expand; 'visible' is really 'auto'.
     if (e == htmlElem) {
       overflow.x = overflow.x == 'visible' ? 'auto' : overflow.x;
       overflow.y = overflow.y == 'visible' ? 'auto' : overflow.y;
     }
     return overflow;
   }

   // Returns the scroll offset of the given element.
   function getScroll(e) {
     if (e == htmlElem) {
       return new goog_dom_DomHelper(ownerDoc).getDocumentScroll();
     } else {
       return new goog_math_Coordinate(e.scrollLeft, e.scrollTop);
     }
   }

   // Check if the element overflows any ancestor element.
   for (var container = getOverflowParent(elem);
        !!container;
        container = getOverflowParent(container)) {
     var containerOverflow = getOverflowStyles(container);

     // If the container has overflow:visible, the element cannot
 // overflow it.
     if (containerOverflow.x == 'visible' && containerOverflow.y == 'visible') {
       continue;
     }

     var containerRect = bot_dom_getClientRect(container);

     // Zero-sized containers without overflow:visible hide all
 // descendants.
     if (containerRect.width == 0 || containerRect.height == 0) {
       return bot_dom_OverflowState.HIDDEN;
     }

     // Check "underflow": if an element is to the left or above the
 // container
     var underflowsX = region.right < containerRect.left;
     var underflowsY = region.bottom < containerRect.top;
     if ((underflowsX && containerOverflow.x == 'hidden') ||
         (underflowsY && containerOverflow.y == 'hidden')) {
       return bot_dom_OverflowState.HIDDEN;
     } else if ((underflowsX && containerOverflow.x != 'visible') ||
                (underflowsY && containerOverflow.y != 'visible')) {
       // When the element is positioned to the left or above a
 // container, we
       // have to distinguish between the element being completely
 // outside the
       // container and merely scrolled out of view within the
 // container.
       var containerScroll = getScroll(container);
       var unscrollableX = region.right < containerRect.left - containerScroll.x;
       var unscrollableY = region.bottom < containerRect.top - containerScroll.y;
       if ((unscrollableX && containerOverflow.x != 'visible') ||
           (unscrollableY && containerOverflow.x != 'visible')) {
         return bot_dom_OverflowState.HIDDEN;
       }
       var containerState = bot_dom_getOverflowState(container);
       return containerState == bot_dom_OverflowState.HIDDEN ?
           bot_dom_OverflowState.HIDDEN : bot_dom_OverflowState.SCROLL;
     }

     // Check "overflow": if an element is to the right or below a
 // container
     var overflowsX = region.left >= containerRect.left + containerRect.width;
     var overflowsY = region.top >= containerRect.top + containerRect.height;
     if ((overflowsX && containerOverflow.x == 'hidden') ||
         (overflowsY && containerOverflow.y == 'hidden')) {
       return bot_dom_OverflowState.HIDDEN;
     } else if ((overflowsX && containerOverflow.x != 'visible') ||
                (overflowsY && containerOverflow.y != 'visible')) {
       // If the element has fixed position and falls outside the
 // scrollable area
       // of the document, then it is hidden.
       if (treatAsFixedPosition) {
         var docScroll = getScroll(container);
         if ((region.left >= htmlElem.scrollWidth - docScroll.x) ||
             (region.right >= htmlElem.scrollHeight - docScroll.y)) {
           return bot_dom_OverflowState.HIDDEN;
         }
       }
       // If the element can be scrolled into view of the parent, it
 // has a scroll
       // state; unless the parent itself is entirely hidden by
 // overflow, in
       // which it is also hidden by overflow.
       var containerState = bot_dom_getOverflowState(container);
       return containerState == bot_dom_OverflowState.HIDDEN ?
           bot_dom_OverflowState.HIDDEN : bot_dom_OverflowState.SCROLL;
     }
   }

   // Does not overflow any ancestor.
   return bot_dom_OverflowState.NONE;
 };
   /**
	 * Fast prefix-checker.
	 * 
	 * @param {string}
	 *            str The string to check.
	 * @param {string}
	 *            prefix A string to look for at the start of {@code str}.
	 * @return {boolean} True if {@code str} begins with {@code prefix}.
	 */
 var goog_string_startsWith = function(str, prefix) {
     return str.lastIndexOf(prefix, 0) == 0;
   };
   

   /**
	 * Gets the element's client rectangle as a box, optionally clipped to the
	 * given coordinate or rectangle relative to the client's position. A
	 * coordinate is treated as a 1x1 rectangle whose top-left corner is the
	 * coordinate.
	 * 
	 * @param {!Element}
	 *            elem The element.
	 * @param {!(goog_math.Coordinate|goog_math.Rect)=}
	 *            opt_region Coordinate or rectangle relative to the top-left
	 *            corner of the element.
	 * @return {!goog_math.Box} The client region box.
	 */
   var bot_dom_getClientRegion = function(elem, opt_region) {
     var region = bot_dom_getClientRect(elem).toBox();

     if (opt_region) {
       var rect = opt_region instanceof goog_math_Rect ? opt_region :
           new goog_math_Rect(opt_region.x, opt_region.y, 1, 1);
       region.left = goog_math_clamp(
           region.left + rect.left, region.left, region.right);
       region.top = goog_math_clamp(
           region.top + rect.top, region.top, region.bottom);
       region.right = goog_math_clamp(
           region.left + rect.width, region.left, region.right);
       region.bottom = goog_math_clamp(
           region.top + rect.height, region.top, region.bottom);
     }

     return region;
   };
   
   var bot_dom_DISABLED_ATTRIBUTE_SUPPORTED_ = [
                                            "BUTTON",
                                            "INPUT",
                                            "OPTGROUP",
                                            "OPTION",
                                            "SELECT",
                                            "TEXTAREA",
                                            "FIELDSET"
                                          ];


   /**
    * Looks up the given property (not to be confused with an attribute) on the
    * given element.
    *
    * @param {!Element} element The element to use.
    * @param {string} propertyName The name of the property.
    * @return {*} The value of the property.
    */
   var bot_dom_getProperty = function(element, propertyName) {
     // When an <option>'s value attribute is not set, its value property should be
     // its text content, but IE < 8 does not adhere to that behavior, so fix it.
     // http://www.w3.org/TR/1999/REC-html401-19991224/interact/forms.html#adef-value-OPTION
// GIOG 
	   //if (bot.userAgent.IE_DOC_PRE8 && propertyName == 'value' &&
//         bot.dom.isElement(element, goog.dom.TagName.OPTION) &&
//         goog.isNull(bot.dom.getAttribute(element, 'value'))) {
//       return goog.dom.getRawTextContent(element);
//     }
     return element[propertyName];
   };
   
   /**
    * Determines if an element is enabled. An element is considered enabled if it
    * does not support the "disabled" attribute, or if it is not disabled.
    * @param {!Element} el The element to test.
    * @return {boolean} Whether the element is enabled.
    */
   var bot_dom_isEnabled = function(el) {
     var tagName = el.tagName.toUpperCase();
     if (!goog_array_contains(bot_dom_DISABLED_ATTRIBUTE_SUPPORTED_, tagName)) {
       return true;
     }

     if (bot_dom_getProperty(el, 'disabled')) {
       return false;
     }

     // The element is not explicitly disabled, but if it is an OPTION or OPTGROUP,
     // we must test if it inherits its state from a parent.
     if (el.parentNode &&
         el.parentNode.nodeType == Node.ELEMENT_NODE &&
         "OPTGROUP" == tagName ||
         "OPTION" == tagName) {
       return bot_dom_isEnabled(/**@type{!Element}*/ (el.parentNode));
     }

     // Is there an ancestor of the current element that is a disabled fieldset
     // and whose child is also an ancestor-or-self of the current element but is
     // not the first legend child of the fieldset. If so then the element is
     // disabled.
     return !goog_dom_getAncestor(el, function(e) {
       var parent = e.parentNode;

       if (parent &&
           bot_dom_isElement(parent, "FIELDSET") &&
           bot_dom_getProperty(/** @type {!Element} */ (parent), 'disabled')) {
         if (!bot_dom_isElement(e, "LEGEND")) {
           return true;
         }

         var sibling = e;
         // Are there any previous legend siblings? If so then we are not the
         // first and the element is disabled
         while (sibling = goog_dom_getPreviousElementSibling(sibling)) {
           if (bot_dom_isElement(sibling, "LEGEND")) {
             return true;
           }
         }
       }
       return false;
     }, true);
   };

   
   /**
	 * Takes a number and clamps it to within the provided bounds.
	 * 
	 * @param {number}
	 *            value The input number.
	 * @param {number}
	 *            min The minimum value to return.
	 * @param {number}
	 *            max The maximum value to return.
	 * @return {number} The input number if it is within bounds, or the nearest
	 *         number within the bounds.
	 */
   var goog_math_clamp = function(value, min, max) {
     return Math.min(Math.max(value, min), max);
   };

   
   

 /**
	 * The kind of overflow area in which an element may be located. NONE if it
	 * does not overflow any ancestor element; HIDDEN if it overflows and cannot
	 * be scrolled into view; SCROLL if it overflows but can be scrolled into
	 * view.
	 * 
	 * @enum {string}
	 */
 var bot_dom_OverflowState = {
   NONE: 'none',
   HIDDEN: 'hidden',
   SCROLL: 'scroll'
 };

 
 /**
	 * Create an instance of a DOM helper with a new document object.
	 * 
	 * @param {Document=}
	 *            opt_document Document object to associate with this DOM
	 *            helper.
	 * @constructor
	 */
 var goog_dom_DomHelper = function(opt_document) {
   /**
	 * Reference to the document object to use
	 * 
	 * @type {!Document}
	 * @private
	 */
   this.document_ = opt_document || goog_global.document || document;
 };
 
 /**
	 * Gets the document scroll distance as a coordinate object.
	 * 
	 * @return {!goog_math.Coordinate} Object with properties 'x' and 'y'.
	 */
 goog_dom_DomHelper.prototype.getDocumentScroll = function() {
   return goog_dom_getDocumentScroll_(this.document_);
 };
 
 /**
  * Helper for {@code getDocumentScroll}.
  *
  * @param {!Document} doc The document to get the scroll for.
  * @return {!goog.math.Coordinate} Object with values 'x' and 'y'.
  * @private
  */
 var goog_dom_getDocumentScroll_ = function(doc) {
   var el = goog_dom_getDocumentScrollElement_(doc);
   var win = goog_dom_getWindow_(doc);
   return new goog_math_Coordinate(win.pageXOffset || el.scrollLeft,
       win.pageYOffset || el.scrollTop);
 };
 
 
 /**
  * Gets the document scroll element.
  * @return {Element} Scrolling element.
  */
 var goog_dom_getDocumentScrollElement = function() {
   return goog_dom_getDocumentScrollElement_(document);
 };


 /**
  * Helper for {@code getDocumentScrollElement}.
  * @param {!Document} doc The document to get the scroll element for.
  * @return {Element} Scrolling element.
  * @private
  */
 goog_dom_getDocumentScrollElement_ = function(doc) {
   // Safari (2 and 3) needs body.scrollLeft in both quirks mode and strict mode.
   //GIOG return !goog.userAgent.WEBKIT && goog_dom_isCss1CompatMode_(doc) ? doc.documentElement : doc.body;
	 return  doc.documentElement;
 };
 
 
 
 
 
 /**
	 * Finds the first descendant node that matches the filter function, using a
	 * depth first search. This function offers the most general purpose way of
	 * finding a matching element. You may also wish to consider
	 * {@code goog_dom_query} which can express many matching criteria using CSS
	 * selector expressions. These expressions often result in a more compact
	 * representation of the desired result.
	 * 
	 * @see goog_dom_query
	 * 
	 * @param {Node}
	 *            root The root of the tree to search.
	 * @param {function(Node) :
	 *            boolean} p The filter function.
	 * @return {Node|undefined} The found node or undefined if none is found.
	 */
 var goog_dom_findNode = function(root, p) {
   var rv = [];
   var found = goog_dom_findNodes_(root, p, rv, true);
   return found ? rv[0] : undefined;
 };
 
 /**
	 * Finds all the descendant nodes that match the filter function, using a a
	 * depth first search. This function offers the most general-purpose way of
	 * finding a set of matching elements. You may also wish to consider
	 * {@code goog_dom_query} which can express many matching criteria using CSS
	 * selector expressions. These expressions often result in a more compact
	 * representation of the desired result.
	 * 
	 * @param {Node}
	 *            root The root of the tree to search.
	 * @param {function(Node) :
	 *            boolean} p The filter function.
	 * @return {!Array.<!Node>} The found nodes or an empty array if none are
	 *         found.
	 */
 var goog_dom_findNodes = function(root, p) {
   var rv = [];
   goog_dom_findNodes_(root, p, rv, false);
   return rv;
 };
 
 /**
	 * Finds the first or all the descendant nodes that match the filter
	 * function, using a depth first search.
	 * 
	 * @param {Node}
	 *            root The root of the tree to search.
	 * @param {function(Node) :
	 *            boolean} p The filter function.
	 * @param {!Array.
	 *            <!Node>} rv The found nodes are added to this array.
	 * @param {boolean}
	 *            findOne If true we exit after the first found node.
	 * @return {boolean} Whether the search is complete or not. True in case
	 *         findOne is true and the node is found. False otherwise.
	 * @private
	 */
 var goog_dom_findNodes_ = function(root, p, rv, findOne) {
   if (root != null) {
     var child = root.firstChild;
     while (child) {
       if (p(child)) {
         rv.push(child);
         if (findOne) {
           return true;
         }
       }
       if (goog_dom_findNodes_(child, p, rv, findOne)) {
         return true;
       }
       child = child.nextSibling;
     }
   }
   return false;
 };
 /**
	 * Calls f for each element of an array. If any call returns true, some()
	 * returns true (without checking the remaining elements). If all calls
	 * return false, some() returns false.
	 * 
	 * See {@link http://tinyurl.com/developer-mozilla-org-array-some}
	 * 
	 * @param {Array.
	 *            <T>|goog_array.ArrayLike} arr Array or array like object over
	 *            which to iterate.
	 * @param {?function(this:S,
	 *            T, number, ?) : boolean} f The function to call for for every
	 *            element. This function takes 3 arguments (the element, the
	 *            index and the array) and should return a boolean.
	 * @param {S=}
	 *            opt_obj The object to be used as the value of 'this' within f.
	 * @return {boolean} true if any element passes the test.
	 * @template T,S
	 */
 var goog_array_some = /*
						 * goog_NATIVE_ARRAY_PROTOTYPES &&
						 * goog_array.ARRAY_PROTOTYPE_.some ? function(arr, f,
						 * opt_obj) { goog_asserts.assert(arr.length != null);
						 * 
						 * return goog_array.ARRAY_PROTOTYPE_.some.call(arr, f,
						 * opt_obj); } :
						 */
     function(arr, f, opt_obj) {
       var l = arr.length;  // must be fixed during loop... see docs
       var arr2 = goog_isString(arr) ? arr.split('') : arr;
       for (var i = 0; i < l; i++) {
         if (i in arr2 && f.call(opt_obj, arr2[i], i, arr)) {
           return true;
         }
       }
       return false;
     };
  


     /**
		 * Call f for each element of an array. If all calls return true,
		 * every() returns true. If any call returns false, every() returns
		 * false and does not continue to check the remaining elements.
		 * 
		 * See {@link http://tinyurl.com/developer-mozilla-org-array-every}
		 * 
		 * @param {Array.
		 *            <T>|goog_array.ArrayLike} arr Array or array like object
		 *            over which to iterate.
		 * @param {?function(this:S,
		 *            T, number, ?) : boolean} f The function to call for for
		 *            every element. This function takes 3 arguments (the
		 *            element, the index and the array) and should return a
		 *            boolean.
		 * @param {S=}
		 *            opt_obj The object to be used as the value of 'this'
		 *            within f.
		 * @return {boolean} false if any element fails the test.
		 * @template T,S
		 */
     var goog_array_every = /***************************************************
							 * goog_NATIVE_ARRAY_PROTOTYPES &&
							 * goog_array.ARRAY_PROTOTYPE_.every ? function(arr,
							 * f, opt_obj) { goog_asserts.assert(arr.length !=
							 * null);
							 * 
							 * return
							 * goog_array.ARRAY_PROTOTYPE_.every.call(arr, f,
							 * opt_obj); } :
							 **************************************************/
         function(arr, f, opt_obj) {
           var l = arr.length;  // must be fixed during loop... see docs
           var arr2 = goog_isString(arr) ? arr.split('') : arr;
           for (var i = 0; i < l; i++) {
             if (i in arr2 && !f.call(opt_obj, arr2[i], i, arr)) {
               return false;
             }
           }
           return true;
         };
         /**
			 * Returns the first parent that could affect the position of a
			 * given element.
			 * 
			 * @param {Element}
			 *            element The element to get the offset parent for.
			 * @return {Element} The first offset parent or null if one cannot
			 *         be found.
			 */
       var goog_style_getOffsetParent = function(element) {
          
        // element.offsetParent does the right thing in IE7 and
 // below. In other
           // browsers it only includes elements with position
 // absolute, relative or
           // fixed, not elements with overflow set to auto or scroll.
           if (/* GIOG */false && goog_userAgent.IE && !goog_userAgent.isDocumentMode(8)) {
             return element.offsetParent;
           }

           var doc = goog_dom_getOwnerDocument(element);
           var positionStyle = goog_style_getStyle_(element, 'position');
           var skipStatic = positionStyle == 'fixed' || positionStyle == 'absolute';
           for (var parent = element.parentNode; parent && parent != doc;
                parent = parent.parentNode) {
             positionStyle =
                 goog_style_getStyle_(/** @type {!Element} */ (parent), 'position');
             skipStatic = skipStatic && positionStyle == 'static' &&
                          parent != doc.documentElement && parent != doc.body;
             if (!skipStatic && (parent.scrollWidth > parent.clientWidth ||
                                 parent.scrollHeight > parent.clientHeight ||
                                 positionStyle == 'fixed' ||
                                 positionStyle == 'absolute' ||
                                 positionStyle == 'relative')) {
               return /** @type {!Element} */ (parent);
             }
           }
           return null;
         };
         
         /**
			 * Cross-browser pseudo get computed style. It returns the computed
			 * style where available. If not available it tries the cascaded
			 * style value (IE currentStyle) and in worst case the inline style
			 * value. It shouldn't be called directly, see
			 * http://wiki/Main/ComputedStyleVsCascadedStyle for discussion.
			 * 
			 * @param {Element}
			 *            element Element to get style of.
			 * @param {string}
			 *            style Property to get (must be camelCase, not
			 *            css-style.).
			 * @return {string} Style value.
			 * @private
			 */
         var goog_style_getStyle_ = function(element, style) {
           return goog_style_getComputedStyle(element, style) ||
                  goog_style_getCascadedStyle(element, style) ||
                  (element.style && element.style[style]);
         };
         
         /**
			 * Returns the position of the event or the element's border box
			 * relative to the client viewport.
			 * 
			 * @param {Element|Event|goog_events.Event}
			 *            el Element or a mouse / touch event.
			 * @return {!goog_math.Coordinate} The position.
			 */
         var goog_style_getClientPosition = function(el) {
           var pos = new goog_math_Coordinate;
           if (el.nodeType == Node.ELEMENT_NODE) {
             el = /** @type {!Element} */ (el);
             if (el.getBoundingClientRect) {
               // IE, Gecko 1.9+, and most modern WebKit
               var box = goog_style_getBoundingClientRect_(el);
               pos.x = box.left;
               pos.y = box.top;
             } else {
               var scrollCoord = goog_dom_getDomHelper(el).getDocumentScroll();
               var pageCoord = goog_style_getPageOffset(el);
               pos.x = pageCoord.x - scrollCoord.x;
               pos.y = pageCoord.y - scrollCoord.y;
             }
             // GIOG if (goog_userAgent.GECKO &&
 // !goog_userAgent.isVersion(12)) {
             if(true){// here GECKO > 12
               pos = goog_math_Coordinate_sum(pos, goog_style_getCssTranslation(el));
             }
           } else {
             var isAbstractedEvent = goog_isFunction(el.getBrowserEvent);
             var targetEvent = el;

             if (el.targetTouches) {
               targetEvent = el.targetTouches[0];
             } else if (isAbstractedEvent && el.getBrowserEvent().targetTouches) {
               targetEvent = el.getBrowserEvent().targetTouches[0];
             }

             pos.x = targetEvent.clientX;
             pos.y = targetEvent.clientY;
           }

           return pos;
         };
         
         /**
			 * Returns the x,y translation component of any CSS transforms
			 * applied to the element, in pixels.
			 * 
			 * @param {!Element}
			 *            element The element to get the translation of.
			 * @return {!goog_math.Coordinate} The CSS translation of the
			 *         element in px.
			 */
         var goog_style_getCssTranslation = function(element) {
           var property;
// GIOG if (goog_userAgent.IE) {
// property = '-ms-transform';
// } else if (goog_userAgent.WEBKIT) {
// property = '-webkit-transform';
// } else if (goog_userAgent.OPERA) {
// property = '-o-transform';
// } else if (goog_userAgent.GECKO) {
// property = '-moz-transform';
// }
           property = '-moz-transform';
           var transform;
           if (property) {
             transform = goog_style_getStyle_(element, property);
           }
           if (!transform) {
             transform = goog_style_getStyle_(element, 'transform');
           }
           if (!transform) {
             return new goog_math_Coordinate(0, 0);
           }
           var matches = transform.match(goog_style_MATRIX_TRANSLATION_REGEX_);
           if (!matches) {
             return new goog_math_Coordinate(0, 0);
           }
           return new goog_math_Coordinate(parseFloat(matches[1]),
                                           parseFloat(matches[2]));
         };
         
         
         /**
			 * Regular expression to extract x and y translation components from
			 * a CSS transform Matrix representation.
			 * 
			 * @type {!RegExp}
			 * @const
			 * @private
			 */
         var goog_style_MATRIX_TRANSLATION_REGEX_ =
             new RegExp('matrix\\([0-9\\.\\-]+, [0-9\\.\\-]+, ' +
                        '[0-9\\.\\-]+, [0-9\\.\\-]+, ' +
                        '([0-9\\.\\-]+)p?x?, ([0-9\\.\\-]+)p?x?\\)');
         
         /**
			 * Returns the sum of two coordinates as a new goog_math.Coordinate.
			 * 
			 * @param {!goog_math.Coordinate}
			 *            a A Coordinate.
			 * @param {!goog_math.Coordinate}
			 *            b A Coordinate.
			 * @return {!goog_math.Coordinate} A Coordinate representing the sum
			 *         of the two coordinates.
			 */
         var goog_math_Coordinate_sum = function(a, b) {
           return new goog_math_Coordinate(a.x + b.x, a.y + b.y);
         };
         
         /**
			 * Gets the client rectangle of the DOM element.
			 * 
			 * getBoundingClientRect is part of a new CSS object model draft
			 * (with a long-time presence in IE), replacing the error-prone
			 * parent offset computation and the now-deprecated Gecko
			 * getBoxObjectFor.
			 * 
			 * This utility patches common browser bugs in
			 * getBoundingClientRect. It will fail if getBoundingClientRect is
			 * unsupported.
			 * 
			 * If the element is not in the DOM, the result is undefined, and an
			 * error may be thrown depending on user agent.
			 * 
			 * @param {!Element}
			 *            el The element whose bounding rectangle is being
			 *            queried.
			 * @return {Object} A native bounding rectangle with numerical left,
			 *         top, right, and bottom. Reported by Firefox to be of
			 *         object type ClientRect.
			 * @private
			 */
         var goog_style_getBoundingClientRect_ = function(el) {
           var rect = el.getBoundingClientRect();
           // Patch the result in IE only, so that this function can be
 // inlined if
           // compiled for non-IE.
           
           // GIOG
           if (false && goog_userAgent.IE) {

             // In IE, most of the time, 2 extra pixels are added to the
 // top and left
             // due to the implicit 2-pixel inset border. In IE6/7 quirks
 // mode and
             // IE6 standards mode, this border can be overridden by
 // setting the
             // document element's border to zero -- thus, we cannot rely
 // on the
             // offset always being 2 pixels.

             // In quirks mode, the offset can be determined by querying
 // the body's
             // clientLeft/clientTop, but in standards mode, it is found
 // by querying
             // the document element's clientLeft/clientTop. Since we
 // already called
             // getBoundingClientRect we have already forced a reflow, so
 // it is not
             // too expensive just to query them all.

             // See:
 // http://msdn.microsoft.com/en-us/library/ms536433(VS.85).aspx
             var doc = el.ownerDocument;
             rect.left -= doc.documentElement.clientLeft + doc.body.clientLeft;
             rect.top -= doc.documentElement.clientTop + doc.body.clientTop;
           }
           return /** @type {Object} */ (rect);
         };
         
         /**
			 * Returns true if the specified value is a string
			 * 
			 * @param {*}
			 *            val Variable to test.
			 * @return {boolean} Whether variable is a string.
			 */
         var goog_isString = function(val) {
           return typeof val == 'string';
         };
         
         /**
			 * Returns true if the specified value is a function
			 * 
			 * @param {*}
			 *            val Variable to test.
			 * @return {boolean} Whether variable is a function.
			 */
         var goog_isFunction = function(val) {
           // return goog_typeOf(val) == 'function';
          return typeof val == 'function';
         };
         

         /**
			 * Gets the window object associated with the given document.
			 * 
			 * @param {Document=}
			 *            opt_doc Document object to get window for.
			 * @return {!Window} The window associated with the given document.
			 */
         var goog_dom_getWindow = function(opt_doc) {
           // TODO(arv): This should not take an argument.
           return opt_doc ? goog_dom_getWindow_(opt_doc) : window;
         };


         /**
			 * Helper for {@code getWindow}.
			 * 
			 * @param {!Document}
			 *            doc Document object to get window for.
			 * @return {!Window} The window associated with the given document.
			 * @private
			 */
         var goog_dom_getWindow_ = function(doc) {
           return doc.parentWindow || doc.defaultView;
         };
         
         var goog_dom_getViewportSize = function(opt_window) {
            // TODO(arv): This should not take an argument
            return goog_dom_getViewportSize_(opt_window || window);
          };


          /**
			 * Helper for {@code getViewportSize}.
			 * 
			 * @param {Window}
			 *            win The window to get the view port size for.
			 * @return {!goog_math.Size} Object with values 'width' and
			 *         'height'.
			 * @private
			 */
         var goog_dom_getViewportSize_ = function(win) {
            var doc = win.document;
            // GIOG var el = goog_dom_isCss1CompatMode_(doc) ?
 // doc.documentElement : doc.body;
            var el = doc.documentElement;
            return new goog_math_Size(el.clientWidth, el.clientHeight);
          };
          /**
			 * Gets the height and width of an element, even if its display is
			 * none. Specifically, this returns the height and width of the
			 * border box, irrespective of the box model in effect.
			 * 
			 * @param {Element}
			 *            element Element to get size of.
			 * @return {!goog_math.Size} Object with width/height properties.
			 */
          var goog_style_getSize = function(element) {
            if (goog_style_getStyle_(element, 'display') != 'none') {
              return goog_style_getSizeWithDisplay_(element);
            }

            var style = element.style;
            var originalDisplay = style.display;
            var originalVisibility = style.visibility;
            var originalPosition = style.position;

            style.visibility = 'hidden';
            style.position = 'absolute';
            style.display = 'inline';

            var size = goog_style_getSizeWithDisplay_(element);

            style.display = originalDisplay;
            style.position = originalPosition;
            style.visibility = originalVisibility;

            return size;
          };
          
          /**
			 * Gets the height and with of an element when the display is not
			 * none.
			 * 
			 * @param {Element}
			 *            element Element to get size of.
			 * @return {!goog_math_Size} Object with width/height properties.
			 * @private
			 */
          var goog_style_getSizeWithDisplay_ = function(element) {
            var offsetWidth = element.offsetWidth;
            var offsetHeight = element.offsetHeight;
            // GIOG
            // var webkitOffsetsZero =
            // goog_userAgent.WEBKIT && !offsetWidth &&
 // !offsetHeight;
            if ((!goog_isDef(offsetWidth) /* || webkitOffsetsZero */) &&
                element.getBoundingClientRect) {
              // Fall back to calling getBoundingClientRect when
 // offsetWidth or
              // offsetHeight are not defined, or when they are zero
 // in WebKit browsers.
              // This makes sure that we return for the correct size
 // for SVG elements, but
              // will still return 0 on Webkit prior to 534.8, see
              // http://trac.webkit.org/changeset/67252.
              var clientRect = goog_style_getBoundingClientRect_(element);
              return new goog_math_Size(clientRect.right - clientRect.left,
                  clientRect.bottom - clientRect.top);
            }
            return new goog_math_Size(offsetWidth, offsetHeight);
          };
          
          

          /**
			 * Returns the last element in an array without removing it.
			 * 
			 * @param {goog_array.ArrayLike}
			 *            array The array.
			 * @return {*} Last item in array.
			 */
          var goog_array_peek = function(array) {
            return array[array.length - 1];
          };
          

          /**
			 * Returns the window currently being used for command execution.
			 * 
			 * @return {!Window} The window for command execution.
			 */
          var bot_getWindow = function() {
          try {
            return window;
          } catch (ignored) {
            // We only reach this place in a firefox
 // extension.
            bot_window_ = goog_global;
          }
          };
          

          /**
			 * Calls a function for each element in an array and inserts the
			 * result into a new array.
			 * 
			 * See {@link http://tinyurl.com/developer-mozilla-org-array-map}
			 * 
			 * @param {Array.
			 *            <T>|goog_array.ArrayLike} arr Array or array like
			 *            object over which to iterate.
			 * @param {?function(this:S,
			 *            T, number, ?):?} f The function to call for every
			 *            element. This function takes 3 arguments (the element,
			 *            the index and the array) and should return something.
			 *            The result will be inserted into a new array.
			 * @param {S=}
			 *            opt_obj The object to be used as the value of 'this'
			 *            within f.
			 * @return {!Array} a new array with the results from f.
			 * @template T,S
			 */
          var goog_array_map = /*
								 * goog_NATIVE_ARRAY_PROTOTYPES &&
								 * goog_array.ARRAY_PROTOTYPE_.map ?
								 * function(arr, f, opt_obj) {
								 * goog_asserts.assert(arr.length != null);
								 * 
								 * return
								 * goog_array.ARRAY_PROTOTYPE_.map.call(arr, f,
								 * opt_obj); } :
								 */
              function(arr, f, opt_obj) {
                var l = arr.length;  // must be fixed during loop...
 // see docs
                var res = new Array(l);
                var arr2 = goog_isString(arr) ? arr.split('') : arr;
                for (var i = 0; i < l; i++) {
                  if (i in arr2) {
                    res[i] = f.call(opt_obj, arr2[i], i, arr);
                  }
                }
                return res;
              };
              
          
          /**
			 * Class for representing sizes consisting of a width and height.
			 * Undefined width and height support is deprecated and results in
			 * compiler warning.
			 * 
			 * @param {number}
			 *            width Width.
			 * @param {number}
			 *            height Height.
			 * @constructor
			 */
          var goog_math_Size = function(width, height) {
            /**
			 * Width
			 * 
			 * @type {number}
			 */
            this.width = width;

            /**
			 * Height
			 * 
			 * @type {number}
			 */
            this.height = height;
          };
          
          /**
			 * Class for representing rectangular regions.
			 * 
			 * @param {number}
			 *            x Left.
			 * @param {number}
			 *            y Top.
			 * @param {number}
			 *            w Width.
			 * @param {number}
			 *            h Height.
			 * @constructor
			 */
          var goog_math_Rect = function(x, y, w, h) {
            /**
			 * Left
			 * 
			 * @type {number}
			 */
            this.left = x;

            /**
			 * Top
			 * 
			 * @type {number}
			 */
            this.top = y;

            /**
			 * Width
			 * 
			 * @type {number}
			 */
            this.width = w;

            /**
			 * Height
			 * 
			 * @type {number}
			 */
            this.height = h;
          };
          
          /**
			 * Returns a new copy of the rectangle.
			 * 
			 * @return {!goog_math.Rect} A clone of this Rectangle.
			 */
          goog_math_Rect.prototype.clone = function() {
            return new goog_math_Rect(this.left, this.top, this.width, this.height);
          };
          
          /**
			 * Returns a new Box object with the same position and dimensions as
			 * this rectangle.
			 * 
			 * @return {!goog_math.Box} A new Box representation of this
			 *         Rectangle.
			 */
          goog_math_Rect.prototype.toBox = function() {
            var right = this.left + this.width;
            var bottom = this.top + this.height;
            return new goog_math_Box(this.top,
                                     right,
                                     bottom,
                                     this.left);
          };
          

          /**
			 * Class for representing coordinates and positions.
			 * 
			 * @param {number=}
			 *            opt_x Left, defaults to 0.
			 * @param {number=}
			 *            opt_y Top, defaults to 0.
			 * @constructor
			 */
          var goog_math_Coordinate = function(opt_x, opt_y) {
            /**
			 * X-value
			 * 
			 * @type {number}
			 */
            this.x = goog_isDef(opt_x) ? opt_x : 0;

            /**
			 * Y-value
			 * 
			 * @type {number}
			 */
            this.y = goog_isDef(opt_y) ? opt_y : 0;
          };
          
          /**
			 * Class for representing a box. A box is specified as a top, right,
			 * bottom, and left. A box is useful for representing margins and
			 * padding.
			 * 
			 * @param {number}
			 *            top Top.
			 * @param {number}
			 *            right Right.
			 * @param {number}
			 *            bottom Bottom.
			 * @param {number}
			 *            left Left.
			 * @constructor
			 */
          var goog_math_Box = function(top, right, bottom, left) {
            /**
			 * Top
			 * 
			 * @type {number}
			 */
            this.top = top;

            /**
			 * Right
			 * 
			 * @type {number}
			 */
            this.right = right;

            /**
			 * Bottom
			 * 
			 * @type {number}
			 */
            this.bottom = bottom;

            /**
			 * Left
			 * 
			 * @type {number}
			 */
            this.left = left;
          };
          
          /**
			 * Returns a property, with a standardized color if it contains a
			 * convertible color.
			 * 
			 * @param {string}
			 *            propertyName Name of the CSS property in
			 *            selector-case.
			 * @param {string}
			 *            propertyValue The value of the CSS property.
			 * @return {string} The value, in a standardized format if it is a
			 *         color property.
			 */
          var bot_color_standardizeColor = function(propertyName, propertyValue) {
            if (bot_color_isColorProperty(propertyName) &&
                bot_color_isConvertibleColor(propertyValue)) {
              return bot_color_standardizeToRgba_(propertyValue);
            }
            return propertyValue;
          };


          /**
			 * Returns a color in RGBA format - rgba(r, g, b, a).
			 * 
			 * @param {string}
			 *            propertyValue The value of the CSS property.
			 * @return {string} The value, in RGBA format.
			 * @private
			 */
          var bot_color_standardizeToRgba_ = function(propertyValue) {
            var rgba = bot_color_parseRgbaColor(propertyValue);
            if (!rgba.length) {
              rgba = bot_color_convertToRgba_(propertyValue);
              bot_color_addAlphaIfNecessary_(rgba);
            }
            if (rgba.length != 4) {
              return propertyValue;
            }
            return bot_color_toRgbaStyle_(rgba);
          };


          /**
			 * Coverts a color to RGBA.
			 * 
			 * @param {string}
			 *            propertyValue The value of the CSS property.
			 * @return {!Array.<number>} array containing [r, g, b, a] with r,
			 *         g, b as ints in [0, 255] and a as a float in [0, 1].
			 * @private
			 */
          var bot_color_convertToRgba_ = function(propertyValue) {
            var rgba = bot_color_parseRgbColor_(propertyValue);
            if (rgba.length) {
              return rgba;
            }
            var hex = goog_color_names[propertyValue.toLowerCase()];
            hex = (!hex) ? bot_color_prependHashIfNecessary_(propertyValue) : hex;
            if (bot_color_isValidHexColor_(hex)) {
              rgba = bot_color_hexToRgb(bot_color_normalizeHex(hex));
              if (rgba.length) {
                return rgba;
              }
            }
            return [];
          };


          /**
			 * Determines if the given string is a color that can be converted
			 * to RGBA. Browsers can return colors in the following formats:
			 * RGB, RGBA, Hex, NamedColor So only those are supported by this
			 * module and therefore considered convertible.
			 * 
			 * @param {string}
			 *            str Potential color string.
			 * @return {boolean} True if str is in a format that can be
			 *         converted to RGBA.
			 */
          var bot_color_isConvertibleColor = function(str) {
            return !!(bot_color_isValidHexColor_(
                bot_color_prependHashIfNecessary_(str)) ||
                bot_color_parseRgbColor_(str).length ||
                goog_color_names && goog_color_names[str.toLowerCase()] ||
                bot_color_parseRgbaColor(str).length
            );
          };


          /**
			 * Used to determine whether a css property contains a color and
			 * should therefore be standardized to rgba. These are extracted
			 * from the W3C CSS spec:
			 * 
			 * http://www.w3.org/TR/CSS/#properties
			 * 
			 * Used by bot_color_isColorProperty()
			 * 
			 * @const
			 * @private {!Array.<string>}
			 */
          var bot_color_COLOR_PROPERTIES_ = [
            'background-color',
            'border-top-color',
            'border-right-color',
            'border-bottom-color',
            'border-left-color',
            'color',
            'outline-color'
          ];


          /**
			 * Determines if the given property can contain a color.
			 * 
			 * @param {string}
			 *            str CSS property name.
			 * @return {boolean} True if str is a property that can contain a
			 *         color.
			 */
          var bot_color_isColorProperty = function(str) {
            return goog_array_contains(bot_color_COLOR_PROPERTIES_, str);
          };


          /**
			 * Regular expression for extracting the digits in a hex color
			 * triplet.
			 * 
			 * @private {!RegExp}
			 * @const
			 */
          var bot_color_HEX_TRIPLET_RE_ = /#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])/;


          /**
			 * Normalize an hex representation of a color
			 * 
			 * @param {string}
			 *            hexColor an hex color string.
			 * @return {string} hex color in the format '#rrggbb' with all
			 *         lowercase literals.
			 */
          var bot_color_normalizeHex = function(hexColor) {
            if (!bot_color_isValidHexColor_(hexColor)) {
              throw Error("'" + hexColor + "' is not a valid hex color");
            }
            if (hexColor.length == 4) { // of the form #RGB
              hexColor = hexColor.replace(bot_color_HEX_TRIPLET_RE_, '#$1$1$2$2$3$3');
            }
            return hexColor.toLowerCase();
          };


          /**
			 * Converts a hex representation of a color to RGB.
			 * 
			 * @param {string}
			 *            hexColor Color to convert.
			 * @return {!Array} array containing [r, g, b] as ints in [0, 255].
			 */
          var bot_color_hexToRgb = function(hexColor) {
            hexColor = bot_color_normalizeHex(hexColor);
            var r = parseInt(hexColor.substr(1, 2), 16);
            var g = parseInt(hexColor.substr(3, 2), 16);
            var b = parseInt(hexColor.substr(5, 2), 16);

            return [r, g, b];
          };


          /**
			 * Helper for isValidHexColor_.
			 * 
			 * @private {!RegExp}
			 * @const
			 */
          var bot_color_VALID_HEX_COLOR_RE_ = /^#(?:[0-9a-f]{3}){1,2}$/i;


          /**
			 * Checks if a string is a valid hex color. We expect strings of the
			 * format #RRGGBB (ex: #1b3d5f) or #RGB (ex: #3CA == #33CCAA).
			 * 
			 * @param {string}
			 *            str String to check.
			 * @return {boolean} Whether the string is a valid hex color.
			 * @private
			 */
          var bot_color_isValidHexColor_ = function(str) {
            return bot_color_VALID_HEX_COLOR_RE_.test(str);
          };


          /**
			 * Helper for isNormalizedHexColor_.
			 * 
			 * @private {!RegExp}
			 * @const
			 */
          var bot_color_NORMALIZED_HEX_COLOR_RE_ = /^#[0-9a-f]{6}$/;


          /**
			 * Checks if a string is a normalized hex color. We expect strings
			 * of the format #RRGGBB (ex: #1b3d5f) using only lowercase letters.
			 * 
			 * @param {string}
			 *            str String to check.
			 * @return {boolean} Whether the string is a normalized hex color.
			 * @private
			 */
          var bot_color_isNormalizedHexColor_ = function(str) {
            return bot_color_NORMALIZED_HEX_COLOR_RE_.test(str);
          };


          /**
			 * Regular expression for matching and capturing RGBA style strings.
			 * Helper for parseRgbaColor.
			 * 
			 * @private {!RegExp}
			 * @const
			 */
          var bot_color_RGBA_COLOR_RE_ =
              /^(?:rgba)?\((\d{1,3}),\s?(\d{1,3}),\s?(\d{1,3}),\s?(0|1|0\.\d*)\)$/i;


          /**
			 * Attempts to parse a string as an rgba color. We expect strings of
			 * the format '(r, g, b, a)', or 'rgba(r, g, b, a)', where r, g, b
			 * are ints in [0, 255] and a is a float in [0, 1].
			 * 
			 * @param {string}
			 *            str String to check.
			 * @return {!Array.<number>} the integers [r, g, b, a] for valid
			 *         colors or the empty array for invalid colors.
			 */
          var bot_color_parseRgbaColor = function(str) {
            // Each component is separate (rather than using a
 // repeater) so we can
            // capture the match. Also, we explicitly set each
 // component to be either 0,
            // or start with a non-zero, to prevent octal numbers
 // from slipping through.
            var regExpResultArray = str.match(bot_color_RGBA_COLOR_RE_);
            if (regExpResultArray) {
              var r = Number(regExpResultArray[1]);
              var g = Number(regExpResultArray[2]);
              var b = Number(regExpResultArray[3]);
              var a = Number(regExpResultArray[4]);
              if (r >= 0 && r <= 255 &&
                  g >= 0 && g <= 255 &&
                  b >= 0 && b <= 255 &&
                  a >= 0 && a <= 1) {
                return [r, g, b, a];
              }
            }
            return [];
          };


          /**
			 * Regular expression for matching and capturing RGB style strings.
			 * Helper for parseRgbColor_.
			 * 
			 * @private {!RegExp}
			 * @const
			 */
          var bot_color_RGB_COLOR_RE_ =
              /^(?:rgb)?\((0|[1-9]\d{0,2}),\s?(0|[1-9]\d{0,2}),\s?(0|[1-9]\d{0,2})\)$/i;


          /**
			 * Attempts to parse a string as an rgb color. We expect strings of
			 * the format '(r, g, b)', or 'rgb(r, g, b)', where each color
			 * component is an int in [0, 255].
			 * 
			 * @param {string}
			 *            str String to check.
			 * @return {!Array.<number>} the integers [r, g, b] for valid
			 *         colors or the empty array for invalid colors.
			 * @private
			 */
          var bot_color_parseRgbColor_ = function(str) {
            // Each component is separate (rather than using a
 // repeater) so we can
            // capture the match. Also, we explicitly set each
 // component to be either 0,
            // or start with a non-zero, to prevent octal numbers
 // from slipping through.
            var regExpResultArray = str.match(bot_color_RGB_COLOR_RE_);
            if (regExpResultArray) {
              var r = Number(regExpResultArray[1]);
              var g = Number(regExpResultArray[2]);
              var b = Number(regExpResultArray[3]);
              if (r >= 0 && r <= 255 &&
                  g >= 0 && g <= 255 &&
                  b >= 0 && b <= 255) {
                return [r, g, b];
              }
            }
            return [];
          };


          /**
			 * Takes a string a prepends a '#' sign if one doesn't exist. Small
			 * helper method for use by bot_color and friends.
			 * 
			 * @param {string}
			 *            str String to check.
			 * @return {string} The value passed in, prepended with a '#' if it
			 *         didn't already have one.
			 * @private
			 */
          var bot_color_prependHashIfNecessary_ = function(str) {
            return str.charAt(0) == '#' ? str : '#' + str;
          };


          /**
			 * Takes an array and appends a 1 to it if the array only contains 3
			 * elements.
			 * 
			 * @param {!Array.
			 *            <number>} arr The array to check.
			 * @return {!Array.<number>} The same array with a 1 appended if it
			 *         only contained 3 elements.
			 * @private
			 */
          var bot_color_addAlphaIfNecessary_ = function(arr) {
            if (arr.length == 3) {
              arr.push(1);
            }
            return arr;
          };


          /**
			 * Takes an array of [r, g, b, a] and converts it into a string
			 * appropriate for CSS styles.
			 * 
			 * @param {!Array.
			 *            <number>} rgba An array with four elements.
			 * @return {string} string of the form 'rgba(r, g, b, a)'.
			 * @private
			 */
          var bot_color_toRgbaStyle_ = function(rgba) {
            return 'rgba(' + rgba.join(', ') + ')';
          };


          /**
			 * A map that contains a lot of colors that are recognised by
			 * various browsers. This list is way larger than the minimal one
			 * dictated by W3C. The keys of this map are the lowercase
			 * "readable" names of the colors, while the values are the "hex"
			 * values.
			 */
          var goog_color_names = {
            'aliceblue': '#f0f8ff',
            'antiquewhite': '#faebd7',
            'aqua': '#00ffff',
            'aquamarine': '#7fffd4',
            'azure': '#f0ffff',
            'beige': '#f5f5dc',
            'bisque': '#ffe4c4',
            'black': '#000000',
            'blanchedalmond': '#ffebcd',
            'blue': '#0000ff',
            'blueviolet': '#8a2be2',
            'brown': '#a52a2a',
            'burlywood': '#deb887',
            'cadetblue': '#5f9ea0',
            'chartreuse': '#7fff00',
            'chocolate': '#d2691e',
            'coral': '#ff7f50',
            'cornflowerblue': '#6495ed',
            'cornsilk': '#fff8dc',
            'crimson': '#dc143c',
            'cyan': '#00ffff',
            'darkblue': '#00008b',
            'darkcyan': '#008b8b',
            'darkgoldenrod': '#b8860b',
            'darkgray': '#a9a9a9',
            'darkgreen': '#006400',
            'darkgrey': '#a9a9a9',
            'darkkhaki': '#bdb76b',
            'darkmagenta': '#8b008b',
            'darkolivegreen': '#556b2f',
            'darkorange': '#ff8c00',
            'darkorchid': '#9932cc',
            'darkred': '#8b0000',
            'darksalmon': '#e9967a',
            'darkseagreen': '#8fbc8f',
            'darkslateblue': '#483d8b',
            'darkslategray': '#2f4f4f',
            'darkslategrey': '#2f4f4f',
            'darkturquoise': '#00ced1',
            'darkviolet': '#9400d3',
            'deeppink': '#ff1493',
            'deepskyblue': '#00bfff',
            'dimgray': '#696969',
            'dimgrey': '#696969',
            'dodgerblue': '#1e90ff',
            'firebrick': '#b22222',
            'floralwhite': '#fffaf0',
            'forestgreen': '#228b22',
            'fuchsia': '#ff00ff',
            'gainsboro': '#dcdcdc',
            'ghostwhite': '#f8f8ff',
            'gold': '#ffd700',
            'goldenrod': '#daa520',
            'gray': '#808080',
            'green': '#008000',
            'greenyellow': '#adff2f',
            'grey': '#808080',
            'honeydew': '#f0fff0',
            'hotpink': '#ff69b4',
            'indianred': '#cd5c5c',
            'indigo': '#4b0082',
            'ivory': '#fffff0',
            'khaki': '#f0e68c',
            'lavender': '#e6e6fa',
            'lavenderblush': '#fff0f5',
            'lawngreen': '#7cfc00',
            'lemonchiffon': '#fffacd',
            'lightblue': '#add8e6',
            'lightcoral': '#f08080',
            'lightcyan': '#e0ffff',
            'lightgoldenrodyellow': '#fafad2',
            'lightgray': '#d3d3d3',
            'lightgreen': '#90ee90',
            'lightgrey': '#d3d3d3',
            'lightpink': '#ffb6c1',
            'lightsalmon': '#ffa07a',
            'lightseagreen': '#20b2aa',
            'lightskyblue': '#87cefa',
            'lightslategray': '#778899',
            'lightslategrey': '#778899',
            'lightsteelblue': '#b0c4de',
            'lightyellow': '#ffffe0',
            'lime': '#00ff00',
            'limegreen': '#32cd32',
            'linen': '#faf0e6',
            'magenta': '#ff00ff',
            'maroon': '#800000',
            'mediumaquamarine': '#66cdaa',
            'mediumblue': '#0000cd',
            'mediumorchid': '#ba55d3',
            'mediumpurple': '#9370db',
            'mediumseagreen': '#3cb371',
            'mediumslateblue': '#7b68ee',
            'mediumspringgreen': '#00fa9a',
            'mediumturquoise': '#48d1cc',
            'mediumvioletred': '#c71585',
            'midnightblue': '#191970',
            'mintcream': '#f5fffa',
            'mistyrose': '#ffe4e1',
            'moccasin': '#ffe4b5',
            'navajowhite': '#ffdead',
            'navy': '#000080',
            'oldlace': '#fdf5e6',
            'olive': '#808000',
            'olivedrab': '#6b8e23',
            'orange': '#ffa500',
            'orangered': '#ff4500',
            'orchid': '#da70d6',
            'palegoldenrod': '#eee8aa',
            'palegreen': '#98fb98',
            'paleturquoise': '#afeeee',
            'palevioletred': '#db7093',
            'papayawhip': '#ffefd5',
            'peachpuff': '#ffdab9',
            'peru': '#cd853f',
            'pink': '#ffc0cb',
            'plum': '#dda0dd',
            'powderblue': '#b0e0e6',
            'purple': '#800080',
            'red': '#ff0000',
            'rosybrown': '#bc8f8f',
            'royalblue': '#4169e1',
            'saddlebrown': '#8b4513',
            'salmon': '#fa8072',
            'sandybrown': '#f4a460',
            'seagreen': '#2e8b57',
            'seashell': '#fff5ee',
            'sienna': '#a0522d',
            'silver': '#c0c0c0',
            'skyblue': '#87ceeb',
            'slateblue': '#6a5acd',
            'slategray': '#708090',
            'slategrey': '#708090',
            'snow': '#fffafa',
            'springgreen': '#00ff7f',
            'steelblue': '#4682b4',
            'tan': '#d2b48c',
            'teal': '#008080',
            'thistle': '#d8bfd8',
            'tomato': '#ff6347',
            'turquoise': '#40e0d0',
            'violet': '#ee82ee',
            'wheat': '#f5deb3',
            'white': '#ffffff',
            'whitesmoke': '#f5f5f5',
            'yellow': '#ffff00',
            'yellowgreen': '#9acd32'
          };
          
		