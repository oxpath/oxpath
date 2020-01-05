/**
 * COMMONS.JS THIS FILE IS NOT USED DIRECTLY. INSTEAD commons_mini.js IS
 * ACTUALLY USED AS GENERATED VIA http://closure-compiler.appspot.com/home
 */

var TextOrCommentNodeRepresentative = function(thisNode, position) {
	// here parentNode can be null, e.g., in case of removeChild
	var parent = thisNode.parentNode;
	if (parent === null)
		parent = false; // use a placeholder
	return [ thisNode.data, parent, position,
			thisNode.ownerDocument.documentElement,
			_getXPathLocatorForConstructor(thisNode), thisNode.nodeType ];
};

var ElementNodeRepresentative = function(thisNode) {
	return [ thisNode, thisNode.ownerDocument.documentElement,
			thisNode.textContent, _getXPathLocatorForConstructor(thisNode),
			thisNode.nodeType ];
};

var AttributeNodeRepresentative = function(attr) {
	return [ attr.ownerElement, attr.localName, attr.nodeValue,
			attr.ownerElement.ownerDocument.documentElement, attr.nodeType ];
};

var DocumentNodeRepresentative = function(thisDocument, nodeType) {
	// in java we use documentelement as representative
	return [ thisDocument.documentElement, thisDocument.nodeType ];
};

var DocumentTypeRepresentative = function(thisDocumentType) {
	return [ thisDocumentType.ownerDocument.documentElement,
			thisDocumentType.nodeType ];
};

// for internal use, not exposed
function getNodeByXPath(path, rootNode) {
	try {
		var context = rootNode || document;
		var result = document.evaluate(path, context, null,
				XPathResult.FIRST_ORDERED_NODE_TYPE, null);
		return result ? result.singleNodeValue : null;
	} catch (err) {
		console.error("Error in getNodeByXPath(). Cause: " + err);
		return null;
	}
}
// for internal use, not exposed
function getNodesByXPath(path, rootNode) {
	try {
		var context = rootNode || document;
		var nodesSnapshot = document.evaluate(path, context, null,
				XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);

		var res = [];
		for (var i = 0; i < nodesSnapshot.snapshotLength; i++) {
			res.push(nodesSnapshot.snapshotItem(i));
		}
		return res;
	} catch (err) {
		console.error("Error in getNodesByXPath(). Cause: " + err);
		return [];
	}

}

/**
 * 
 * @param {Node}
 *            parent
 * @param {Node}
 *            child
 * @return {Number}
 */
function findPositionAmongChildren(parent, child) {
	if (parent === null)
		return -1;
	var children = parent.childNodes;
	for (var i = 0; i < children.length; i++) {
		if (children[i] === child)
			return i;
	}
	return -1;
}

function testXPath() {
	var all = document.evaluate('//node()', document, null,
			XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
	var nodesSnapshot = all;
	var out = [];
	for (var i = 0; i < nodesSnapshot.snapshotLength; i++) {
		var n = nodesSnapshot.snapshotItem(i);
		var xpathRes = document.evaluate('string(.)', n, null,
				XPathResult.STRING_TYPE, null).stringValue;
		if (n.textContent !== xpathRes)
			out.push('Error: there are different -- node:'
					+ _getXPathLocator(n));
	}
	return out;
}
/**
 * Wraps xpath document.evaluate function
 * 
 * @param expr
 * @param contextNodeRepresentative
 *            comes from Java and represents all supported node types. For
 *            Element, the serialization it's delegated to WebDriver. For other
 *            node types, they are represented as arrays, to be resolved by the
 *            function fromJava()
 * @param type
 * @param useIdAttribute
 *            uses id in getXPathLocator
 * @param useClassAttribute
 *            uses class in getXPathLocator
 * @returns {Array}
 */
function evalXPath(expr, contextNodeRepresentative, type, useIdAttribute,
		useClassAttribute) {

	if (typeof useIdAttribute === 'undefined')
		useIdAttribute = true;
	if (typeof useClassAttribute === 'undefined')
		useClassAttribute = true;

	// set the variable for the function getXPathLocator()
	WebUtils.diademProperties.locatorUseIdAttribute = useIdAttribute;
	WebUtils.diademProperties.locatorUseClassAttribute = useClassAttribute;

	var handleNode = function(thisNode, res) {
		res.push(toJava(thisNode));
	}

	var contextNode = fromJava(contextNodeRepresentative);

	// evaluate xpath
	var xpathRes = document.evaluate(expr, contextNode, null, type, null);

	// read result per different types
	if (xpathRes.resultType === XPathResult.STRING_TYPE) {
		// _clog('result of type STRING_TYPE');
		return [ xpathRes.stringValue ];
	}

	if (xpathRes.resultType === XPathResult.NUMBER_TYPE) {
		// _clog('result of type NUMBER_TYPE');
		return [ xpathRes.numberValue ];
	}

	if (xpathRes === XPathResult.BOOLEAN_TYPE) {
		// _clog('result of type BOOLEAN_TYPE');
		return [ xpathRes.booleanValue ];
	}

	// iterators
	if (xpathRes.resultType === XPathResult.UNORDERED_NODE_ITERATOR_TYPE
			|| xpathRes.resultType === XPathResult.ORDERED_NODE_ITERATOR_TYPE) {
		// _clog('result of type ITERATORS');
		var res = [];
		var iterator = xpathRes;
		try {
			var thisNode = iterator.iterateNext();

			while (thisNode) {
				handleNode(thisNode, res);
				thisNode = iterator.iterateNext();
			}
			return res;
		} catch (e) {
			throw ('Error: Document tree modified during iteration ' + e);
		}
	}
	// snapshots
	if (xpathRes.resultType === XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE
			|| xpathRes.resultType === XPathResult.ORDERED_NODE_SNAPSHOT_TYPE) {
		// _clog('result of type SNAPSHOTS');
		var res = [];
		var nodesSnapshot = xpathRes;
		for (var i = 0; i < nodesSnapshot.snapshotLength; i++) {
			handleNode(nodesSnapshot.snapshotItem(i), res);
		}
		return res;
	}
	// first node
	if (xpathRes.resultType === XPathResult.ANY_UNORDERED_NODE_TYPE
			|| xpathRes.resultType === XPathResult.FIRST_UNORDERED_NODE_TYPE) {
		var res = [];
		// _clog('result of type FIRST_NODE');
		handleNode(xpathRes.singleNodeValue, res);
		return res;
	}

	throw ('Error: Result Type not expected ' + xpathRes.resultType);
}

function evalXPathBulk(pathToAnchorNodes, contextNodeFromJava,
		contextualExpressions, useIdAttribute, useClassAttribute) {

	// TODO evaluate xpath(contextNode,pathToAnchorNodes) --> records
	// and then put them in the result
	// result.push[contextNodeFromJava,pathToAnchorNodes,records] or just assume
	// that the first are the records
	// for each record R
	// for each contextualExpressions A
	// result.push[R,A,evaluate_xpath(R,A)]
	// 
	// return result

	if (typeof useIdAttribute === 'undefined')
		useIdAttribute = true;
	if (typeof useClassAttribute === 'undefined')
		useClassAttribute = true;

	// set the variable for the function getXPathLocator()
	WebUtils.diademProperties.locatorUseIdAttribute = useIdAttribute;
	WebUtils.diademProperties.locatorUseClassAttribute = useClassAttribute;

	var result = {
		anchorNodes : [],
		contextualNodes : [],
		errors : []
	};

	try {
		var anchorNodesInJavaFormat = evalXPath(pathToAnchorNodes,
				contextNodeFromJava, XPathResult.ORDERED_NODE_ITERATOR_TYPE);

		result.anchorNodes = anchorNodesInJavaFormat;

		for (var i = 0; i < anchorNodesInJavaFormat.length; i++) {

			var recordNode = anchorNodesInJavaFormat[i];
			var attr4Record = [];
			result.contextualNodes.push(attr4Record);
			for (var j = 0; j < contextualExpressions.length; j++) {
				var attrExpression = contextualExpressions[j];
				attr4Record.push(evalXPath(attrExpression, recordNode,
						XPathResult.ORDERED_NODE_ITERATOR_TYPE));
			}
		}

	} catch (e) {
		result.errors.push(e.toString());
		return result;
	}

	return result;
}

/**
 * to log messages
 * 
 * @param message
 */
function log(message) {

	if (typeof window.private_log === 'undefined') {
		// initialize the global variable to accumulate the results
		window.private_log = [];
	}
	window.private_log.push(message);
}
/**
 * Returns and reset the log
 * 
 * @returns
 */
function takeLog() {
	if (typeof window.private_log === 'undefined')
		return [];
	var msgs = private_log;
	window.private_log = [];
	return msgs;
}

var WebUtils = {
	diademProperties : {
		locatorUseIdAttribute : true,
		locatorUseClassAttribute : true
	},
	diademNS : '_diadem',
	allowedFormTags : [ "FORM", "INPUT", "TEXTAREA", "LABEL", "FIELDSET",
			"LEGEND", "SELECT", "OPTGROUP", "OPTION", "BUTTON", "DATALIST",
			"KEYGEN", "OUTPUT" ],

	fieldTags : [ "INPUT", "TEXTAREA", "SELECT", "BUTTON", "KEYGEN" ],

	monitoredAttributes : [],

	monitoredElements : [], // ["div","span","a"],

	arrayContains : function(arr, elem) {
		for (var i = 0; i < arr.length; i++) {
			if (arr[i] === elem)
				return true;
		}
		return false;
	},
	isOptionTag : function(tagName) {
		if (!tagName)
			return false;
		return "OPTION" === tagName.toUpperCase();
	},
	isOptionNode : function(node) {
		if (!node)
			throw ('node is undefined in calling WebUtils.isOptionNode');
		var tagName = node.nodeName;
		if (!tagName)
			tagName = node.tagName;
		if (!tagName)
			return false;

		return "OPTION" === tagName.toUpperCase();
	},
	isFieldTag : function(tagName) {
		if (!tagName)
			return false
		return WebUtils.arrayContains(WebUtils.fieldTags, tagName);
	},
	isWhiteSpace : function(text) {
		return /^\s+$/.test(text);
	},
	containsNonWord : function(text) {
		return /\W/.test(text);
	},
	isInteger : function isInteger(text) {
		return /^\d+$/.test(text);
	},

	startWithInteger : function(text) {
		return /^\d/.test(text);
	},
	isMonitoredAttribute : function(attrName) {
		return WebUtils.arrayContains(WebUtils.monitoredAttributes, attrName);
	},

	isMonitoredElement : function(elName) {
		return WebUtils.arrayContains(WebUtils.monitoredElements, elName);
	},

	findFieldOfOption : function(node) {
		if (!node)
			return null;
		var parent = node.parentNode;
		if (!parent)
			return null;
		var parentName = parent.nodeName;
		if (parentName === "SELECT")
			return parent;
		if (parentName === "OPTGROUP") {
			var grandpa = parent.parentNode;
			if (!grandpa)
				return null;
			if (grandpa.nodeName === "SELECT")
				return grandpa;
		} else if (parentName === "DATALIST") {
			var datalistId = parent.getAttribute("id");
			if (datalistId)
				return getNodeByXPath("//*[@list='" + datalistId + "']");
		}
		return null;
	},

	findFieldsOfFieldset : function(element) {
		if (element.nodeName !== "FIELDSET")
			return null;

		var descendant = getNodesByXPath("./descendant::*", element);

		function isFieldNotBlackListed(element) {
			return WebUtils.isFieldTag(element.nodeName);
		}
		return descendant.filter(isFieldNotBlackListed);
	},
	findAncestorByTagName : function(element, tagName) {

		// ./ancestor::*[local-name()='FIELDSET']
		var ancestor = getNodeByXPath("./ancestor::*[local-name()='"
				+ tagName.toLowerCase() + "']", element);
		return ancestor;
	},

	Pair : function(left, right) {
		return [ left, right ];
	}
};

function toJava(node) {
	if (node === null)
		return null;

	if (node.nodeType === Node.ELEMENT_NODE)
		return new ElementNodeRepresentative(node);

	if (node.nodeType === Node.TEXT_NODE || node.nodeType === Node.COMMENT_NODE)
		return new TextOrCommentNodeRepresentative(node,
				findPositionAmongChildren(node.parentNode, node));

	if (node.nodeType == 2)
		return new AttributeNodeRepresentative(node);

	if (node.nodeType === 9) {
		return new DocumentNodeRepresentative(node);
	}
	if (node.nodeType === Node.DOCUMENT_TYPE_NODE) {
		return new DocumentTypeRepresentative(node);
	}

	throw ('Unsupported serialization for nodeType <' + node.nodeType + '>');

}

/**
 * Gets an array that represents dom nodes in Java and finds the corresponding
 * node in DOM
 * 
 * @param {!Array}
 *            args
 * @returns
 */
function fromJava(args) {
	var what = Object.prototype.toString;

	// here we expect an object, the last element it's the nodeType
	if (what.call(args) !== '[object Object]'
			&& what.call(args) !== '[object Array]')
		throw ('fromJava function: Expected DomNode or Array, but got <'
				+ what.call(args) + '>');

	var nodeType = args[args.length - 1];

	// textnode [parentNode, position, NodeType
	if (nodeType === Node.ELEMENT_NODE)
		return args[0];

	// document [documentElement, nodeType]
	if (nodeType === Node.DOCUMENT_NODE)
		return args[0].ownerDocument;

	// documentType [documentElement, nodeType]
	if (nodeType === Node.DOCUMENT_TYPE_NODE) {
		return args[0].ownerDocument.doctype;
	}

	// textnode [parentNode, position, NodeType]
	if (nodeType === Node.TEXT_NODE)
		return args[0].childNodes[args[1]];

	// commentnode [parentNode, position, NodeType]
	if (nodeType === Node.COMMENT_NODE)
		return args[0].childNodes[args[1]];

	// attribute [ node.ownerElement, node.localName, nodeType];
	if (nodeType === Node.ATTRIBUTE_NODE) {
		var attr = args[0].attributes.getNamedItem(args[1]);
		// var attrs = args[0].attributes;
		// for (var i = 0; i < attrs.length; i++) {
		// if (attrs[i].localName === args[1])
		// return attrs[i];
		// }
		if (!attr)
			throw ('cannot find attribute node <' + args[1] + '>');

		return attr;
	}

	throw ('Cannot resolve node: unsupported nodeType <' + nodeType + '>');
}

function _clog(o) {
	console.log(JSON.stringify(o, null, 4));
}
/**
 * 
 */
function throwUndefinedProperty(property, node) {
	throw 'Undefined property: ' + property
			+ ' on eleTextOrCommentNodeRepresentativement ' + node;
}

/**
 * Used in createXPathFromElement to escape local names when containind non word
 * chars
 */
function escapelocalname(elm) {
	if (WebUtils.containsNonWord(elm.localName)) {
		return "*[local-name()='" + elm.localName.toLowerCase() + "']";
	}
	return elm.localName.toLowerCase();
}

function _createXPathFromElement(elm, useIdAttribute, useClassAttribute) {

	var allNodes = document.getElementsByTagName('*');
	for (segs = []; elm && elm.nodeType === Node.ELEMENT_NODE; elm = elm.parentNode) {
		// for html we don't bother
		if (elm === document.documentElement) {
			segs.unshift('/' + elm.nodeName.toLowerCase() + '[1]');
			return segs.join('/');
		}
		// neither for body
		if ("body" === elm.nodeName.toLowerCase()) {
			segs.unshift(elm.nodeName.toLowerCase() + '[1]');
			continue;
		}

		// value must be not empty and not containing the ' char and spaces
		var validId = elm.hasAttribute('id')
				&& elm.getAttribute('id').length > 0
				&& !elm.getAttribute('id').contains("'")
				&& !elm.getAttribute('id').contains(" ");

		var validClass = elm.hasAttribute('class')
				&& elm.getAttribute('class').length > 0
				&& !elm.getAttribute('class').contains("'")
				// && elm.getAttribute('class').trim() ===
				// elm.getAttribute('class')
				&& (elm.getAttribute('class').trim().replace(/\s+/g, ' ')
						.split(' ').length - 1) <= 1; // it allows 2 words in
														// class

		if (useIdAttribute && validId) {

			var uniqueIdCount = 0;
			for (var n = 0; n < allNodes.length; n++) {
				if (allNodes[n].hasAttribute('id') && allNodes[n].id === elm.id)
					uniqueIdCount++;
				if (uniqueIdCount > 1)
					break;
			}
			;
			if (uniqueIdCount === 1) {
				segs.unshift("id('" + elm.getAttribute('id') + "')");
				return segs.join('/');
			} else {
				for (ii = 1, sibl = elm.previousSibling; sibl; sibl = sibl.previousSibling) {
					if (sibl.localName === elm.localName)
						ii++;
				}
				segs.unshift(escapelocalname(elm) + '[' + ii + ']');

			}
		} else if (useClassAttribute && validClass) {
			for (i = 1, sib = elm.previousSibling; sib; sib = sib.previousSibling) {
				if ((sib.localName === elm.localName)
						&& (sib.hasAttribute('class'))
						&& (sib.getAttribute('class') === elm
								.getAttribute('class')))
					i++;
			}
			;
			var valueClass = elm.getAttribute('class');
			var normalizedValueClass = valueClass.trim().replace(/\s+/g, ' ');
			// if it contains multiple spaces we normalize
			var predicate = normalizedValueClass == valueClass ? "[@class='"
					+ valueClass + "']" : "[normalize-space(@class)='"
					+ normalizedValueClass + "']";

			segs.unshift(escapelocalname(elm) + predicate + "[" + i + "]");
		} else {
			for (i = 1, sib = elm.previousSibling; sib; sib = sib.previousSibling) {
				if (sib.localName === elm.localName)
					i++;
			}
			;
			segs.unshift(escapelocalname(elm) + '[' + i + ']');
		}
		;
	}
	;
	console.log(segs);
	return segs.length ? '/' + segs.join('/') : null;
};

function _createXPathFromTextNode(node, useIdAttribute, useClassAttribute) {

	var locator = _createXPathFromElement(node.parentNode, useIdAttribute,
			useClassAttribute);
	// find the position among siblings
	for (i = 1, sib = node.previousSibling; sib; sib = sib.previousSibling) {
		if (sib.nodeName === node.nodeName)
			i++;
	}

	return locator + "/text()[" + i + "]";
};

function _createXPathFromAttributeNode(node, useIdAttribute, useClassAttribute) {

	var locator = _createXPathFromElement(node.ownerElement, useIdAttribute,
			useClassAttribute);

	return locator + "/@" + escapelocalname(node);
};

function getCssPropertyValue(elem, prop) {
	return window.getComputedStyle
	// Modern browsers.
	? window.getComputedStyle(elem, null).getPropertyValue(prop)
	// IE8 and older.
	: elem.currentStyle.getAttribute(prop);
}

function getDocumentDimension() {
	var body = document.body, html = document.documentElement;

	var height = Math.max(body.scrollHeight, body.offsetHeight,
			html.clientHeight, html.scrollHeight, html.offsetHeight);

	var width = Math.max(body.scrollWidth, body.offsetWidth, html.clientWidth,
			html.scrollWidth, html.offsetWidth);

	return [ width, height ];
}
/**
 * It return a unique xpath locator for this node. Only Document, Element,
 * Attribute and TextNode are types supported. For other node types it throws an
 * exception
 * 
 * NOTE: this code is a copy of the one in domFactExtraction. Any modifification
 * here should be reported there accordingly. It will be eventually unified
 */
function getXPathLocator(nodeFromJava, useIdAttribute, useClassAttribute) {
	var node = fromJava(nodeFromJava);

	if (typeof useIdAttribute === 'undefined')
		useIdAttribute = true;
	if (typeof useClassAttribute === 'undefined')
		useClassAttribute = true;

	// remember the variable for the function getXPathLocator() in
	// ElementConstructor
	WebUtils.diademProperties.locatorUseIdAttribute = useIdAttribute;
	WebUtils.diademProperties.locatorUseClassAttribute = useClassAttribute;

	return _getXPathLocator(node, useIdAttribute, useClassAttribute);
	// throw ( 'Unsupported getXPath for nodeType <' + node.nodeType + '>' );
}

function _getXPathLocatorForConstructor(node) {

	return _getXPathLocator(node,
			WebUtils.diademProperties.locatorUseIdAttribute,
			WebUtils.diademProperties.locatorUseClassAttribute)
}
function _getXPathLocator(node, useIdAttribute, useClassAttribute) {

	if (node.nodeType === Node.DOCUMENT_NODE)
		return "/";

	if (node.nodeType === Node.ELEMENT_NODE)
		return _createXPathFromElement(node, useIdAttribute, useClassAttribute);

	if (node.nodeType === Node.TEXT_NODE)
		return _createXPathFromTextNode(node, useIdAttribute, useClassAttribute);

	if (node.nodeType === Node.ATTRIBUTE_NODE)
		return _createXPathFromAttributeNode(node, useIdAttribute,
				useClassAttribute);

	// all other node types are unsopported
	return null;
	// throw ( 'Unsupported getXPath for nodeType <' + node.nodeType + '>' );
}
/**
 * 
 * @param url
 * @returns
 */
function makeURLAbsolute(url) {
	var a = document.createElement('a');
	a.href = url;
	return a.href;
}


function asXML(nodeFromJava){
		return new XMLSerializer().serializeToString(fromJava(nodeFromJava));
}
/**
 * 
 * @param startRange
 * @param endRange
 *            inclusive
 * @returns
 */
function selectText(startRangeFromJava, endRangeFromJava) {

	var startRange = startRangeFromJava;
	if (startRangeFromJava)
		startRange = fromJava(startRangeFromJava);

	var endRange = endRangeFromJava;
	if (endRangeFromJava)
		endRange = fromJava(endRangeFromJava);

	if (startRange && endRange)
		return __selectTextOnRange(startRange, endRange).trim();
	if (startRange) {
		if (startRange.nodeType === Node.TEXT_NODE)
			return __selectOnTextNode(startRange).trim();
		else
			return __selectOnSingleElement(startRange).trim();// is an element here
	}
	throw ('start range is undefined for function <selectText(startRange,endRange)>');

}

function __isGarbage(node) {
	// Node not-empy that contains no non-whitespace character
	// return !/\S/.test(node.textContent);
	return /^\s+$/.test(node.textContent);
}

/**
 * checks if the first node is a garbage node
 * 
 * @param startRange
 * @returns {Number}
 */
function __amendStartRange(startRange) {
	var skip = 0;
	if (startRange.childNodes) {
		if (startRange.childNodes.length > 0) {
			if (__isGarbage(startRange.childNodes[0]))
				skip = 1;
		}
	}
	return skip;
}
/**
 * Returns a new node, skipping the last node if garbage
 * 
 * @param endRange
 * @returns
 */
function __skipEndGarbage(endRange){

	if(!endRange.childNodes)return endRange;
	    
		 var childs=endRange.childNodes.length;
		
	    var skip = childs>0 && __isGarbage(endRange.childNodes[childs-1]);
		  
	  if(!skip) return endRange;
	    
	    if(childs >1) 
		   return endRange.childNodes[childs-2];
	    else return endRange.parentNode;
	}

function __selectTextOnRange(startRange, endRange) {

	var range = document.createRange();

	var skipStartGarbage = __amendStartRange(startRange);

	range.setStart(startRange, skipStartGarbage);

	var amendedEnd = __skipEndGarbage(endRange);
	// setafter --> inclusive
	range.setEndAfter(amendedEnd);

	var selection = window.getSelection();
	selection.removeAllRanges();
	selection.addRange(range);
	var selected = window.getSelection().toString();
	selection.removeAllRanges();
	range.detach();
	return selected;
}

function __selectOnTextNode(textNode) {
	var range = document.createRange();
	range.setStart(textNode, 0);
	range.setEnd(textNode, textNode.textContent.length);
	var selection = window.getSelection();
	selection.removeAllRanges();
	selection.addRange(range);
	var selected = window.getSelection().toString();
	selection.removeAllRanges();
	range.detach();
	return selected;
}

function __selectOnSingleElement(startRange) {
	var amendedEnd = __skipEndGarbage(startRange);
	return __selectTextOnRange(startRange, amendedEnd);
	// window.getSelection().selectAllChildren(startRange);
	// return window.getSelection().toString();
}

// var newSelection = $x('descendant::table[1]//tr');
// var start=newSelection[2];
// var end=newSelection[4];//.childNodes[childs-1];
// selectText(start);

