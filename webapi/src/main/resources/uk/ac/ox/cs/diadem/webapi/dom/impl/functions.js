//FUNCTIONS.JS requires file commons.js
//THIS FILE IS NOT USED DIRECTLY. INSTEAD functions_mini.js IS ACTUALLY USED.
//AS GENERATED VIA http://closure-compiler.appspot.com/home

/**
 * @param {!Document}
 *            doc
 * @param {!string}
 *            tagName
 * @return {Element}
 */
function createElementAndAppendToBody(doc, tagName) {
	var ddoc = fromJava(doc);
	return toJava(ddoc.querySelector('body').appendChild(
			ddoc.createElement(tagName)));
}

/**
 * @param {!Element}
 *            element
 * @param {!string}
 *            name
 * @param {!string}
 *            value
 */
function setAttribute(element, name, value) {
	fromJava(element).setAttribute(name, value);
}


/**
 * @param {!Element}
 *            element
 * @param {!string}
 *            name
 */
function removeAttribute(element, name) {
	fromJava(element).removeAttribute(name);
}

/**
 * 
 * @param {!Element}
 *            e
 */
function clickOnElement(e) {
	fromJava(e).click();
}

/**
 * @param {!Element}
 *            element
 * @return {string}
 */
function getInnerHTML(aelement) {

	return fromJava(aelement).innerHTML;
}

/**
 * @param {!Element}
 *            element
 * @return {string}
 */
function getOuterHTML(aelement) {

	return fromJava(aelement).outerHTML;
}

/**
 * @param {!Element}
 *            element
 * @return {Array.<Element>}
 */
function getFormElements(aform) {
	var form = fromJava(aform);
	if (form instanceof HTMLFormElement) {
		var elements = form.elements;
		var filtered = [];
		// filter out object as bug
		for (var i = 0; i < elements.length; i++) {

			if (elements[i].nodeName.toLowerCase() !== 'object')
				filtered.push(toJava(elements[i]));
		}
		return filtered;
	} else
		// should not be called on non forms
		return null;
}
/**
 * 
 * @return {Element}
 */
function getDocumentElement() {
	return document.documentElement;
}
/**
 * @return {string}
 */
function getDocumentElementTextContent() {
	return document.documentElement.textContent;
}

/**
 * @param {Document}
 *            doc
 * @return {Element}
 */
function getLastChildOfDocument(doc) {
	return doc.documentElement;
}

/**
 * @return {Element}
 */
function getFirstChildOfDocument(doc) {
	// here we return the documentElement unlike FIREFOX that return the
	// DOCUMENTTYPE node (of type 10)
	return doc.documentElement;
}

/**
 * @param {Document}
 *            doc
 * @return {Array.<Element>}
 */
function getChildrenOfDocument(doc) {
	// here we return the documentElement unlike FIREFOX that return the
	// DOCUMENTTYPE node (of type 10)
	return [ doc.documentElement ];
}

/**
 * @param {!Node}
 *            node
 * @return {number}
 */
function getNodeType(node) {
	return fromJava(node).nodeType;
}

/**
 * @param {!Node}
 *            node
 * @return {string}
 */
function getNodeValue(node) {
	return fromJava(node).nodeValue;
}

/**
 * @param {!Node}
 *            node
 * @return {Node}
 */
function getFirstChild(anode) {
	var node = fromJava(anode);
	var c = node.firstChild;
	return toJava(c);
}

/**
 * @param {!Node}
 *            node
 * @return {Node}
 */
function getLastChild(anode) {
	var node = fromJava(anode);
	var c = node.lastChild;
	return toJava(c);
}

/**
 * @param {!Node}
 *            node
 * @return {Node}
 */
function getNextSibling(node) {
	var c = fromJava(node).nextSibling;
	return toJava(c);
}

/**
 * 
 * @param {Node}
 *            parentNode
 * @param {number}
 *            position
 * @return {Node}
 */
function getNextSiblingOfTextNode(parentNode, position) {
	var c = parentNode.childNodes[position];
	return getNextSibling(c);
}

/**
 * @param {!Node}
 *            node
 * @return {Node}
 */
function getPreviousSibling(anode) {
	var c = fromJava(anode).previousSibling;
	return toJava(c);
}

/**
 * 
 * @param {Node}
 *            parentNode
 * @param {number}
 *            position
 * @return {Node}
 */
function getPreviousSiblingOfTextNode(parentNode, position) {
	var c = parentNode.childNodes[position];
	return getPreviousSibling(c);
}
/**
 * 
 * @param {!Element}
 *            element
 * @param {!Node}
 *            child
 * @return {!Node}
 */
function appendChild(element, child) {
	var de = fromJava(element);
	var dc = fromJava(child);
	return toJava(de.appendChild(dc));
}

/**
 * @param {!Element}
 *            element
 * @param {Node}
 *            parentNode
 * @param {number}
 *            position
 * @return {Node}
 */
function appendTextChild(element, parentNode, position) {
	var c = parentNode.childNodes[position];
	var addedText = element.appendChild(c);
	return [ addedText.data, addedText.parentNode,
			findPositionAmongChildren(addedText.parentNode, addedText) ];
}

/**
 * @param {!Node}
 *            node
 * @return {Node}
 */
function getParentNode(node) {

	var c = fromJava(node).parentNode;
	return toJava(c);
}

/**
 * @param {!Node}
 *            node
 * @return {string}
 */
function getLocalName(node) {
	return fromJava(node).localName;
}

/**
 * @param {!Node}
 *            node
 * @return {string}
 */
function getNodeName(node) {
	return fromJava(node).nodeName;
}

/**
 * @param {!Node}
 *            node
 * @return {string}
 */
function getTextContent(node) {
	return fromJava(node).textContent;
}

/**
 * @param {Node}
 *            node
 * @param {Node}
 *            other
 * @return {boolean}
 */
function isSameNode(node, other) {
	// isSameNode() is deprecated in DOM4, see
	// https://developer.mozilla.org/en-US/docs/DOM/Node.isSameNode
	return fromJava(node) === fromJava(other);
}

/**
 * @param {Node}
 *            node
 * @param {Node}
 *            other
 * @return {boolean}
 */
function isSameDocument(node, other) {
	// isSameNode() is deprecated in DOM4, see
	// https://developer.mozilla.org/en-US/docs/DOM/Node.isSameNode
	return node === other;
}

/**
 * @param {Node}
 *            parentNode
 * @param {integer}
 *            childPosition
 * @param {Node}
 *            otherParentNode
 * @param {integer}
 *            otherChildPosition
 * @return {boolean}
 */
function isSameTextNode(parentNode, childPosition, otherParentNode,
		otherChildPosition) {
	// isSameNode() is deprecated in DOM4, see
	// https://developer.mozilla.org/en-US/docs/DOM/Node.isSameNode
	return isSameNode(parentNode.childNodes[childPosition],
			otherParentNode.childNodes[otherChildPosition]);
}

/**
 * @param {!Node}
 *            node
 * @param {!Node}
 *            other
 * @return {boolean}
 */
function isEqualNode(node, other) {
	return fromJava(node).isEqualNode(fromJava(other));
}



/**
 * @param {!Document}
 *            node
 * @param {!Document}
 *            other
 * @return {boolean}
 */
function isEqualDocument(node, other) {
	return node.isEqualNode(other);
}

/**
 * @param {Node}
 *            parentNode
 * @param {integer}
 *            childPosition
 * @param {Node}
 *            otherParentNode
 * @param {integer}
 *            otherChildPosition
 * @return {boolean}
 */
function isEqualTextNode(parentNode, childPosition, otherParentNode,
		otherChildPosition) {
	// isSameNode() is deprecated in DOM4, see
	// https://developer.mozilla.org/en-US/docs/DOM/Node.isSameNode
	return isEqualNode(parentNode.childNodes[childPosition],
			otherParentNode.childNodes[otherChildPosition]);
}

/**
 * @param {Node}
 *            parent
 * @param {Node}
 *            child
 * @return {Node}
 */
function removeChild(parent, child) {
	var removed = fromJava(parent).removeChild(fromJava(child));
	return toJava(removed);
}

/**
 * @param {Node}
 *            parent
 * @param {Node}
 *            child
 * @return {Node}
 */
function removeTextChild(parent, childParent, childPosition) {
	parent.removeChild(childParent.childNodes[childPosition]);
	return true;
}
/**
 * 
 * @param {Node}
 *            parentNode
 * @param {number}
 *            childPosition
 * @param {string}
 *            value
 * @return {string}
 */
function setTextContentForTextNode(parentNode, childPosition, value) {
	var c = parentNode.childNodes[childPosition];
	c.textContent = value;
	return c.textContent;
}
/**
 * 
 * @param {Node}
 *            element
 * @param {string}
 *            value
 */
function setTextContent(element, value) {
	fromJava(element).textContent = value;
}

/**
 * 
 * @param {!Element}
 *            element
 * @return {Array.<float>}
 */
function getBoundingBox(aelement) {
	var element = fromJava(aelement);
	var rect = element.getBoundingClientRect();
	var left = rect.left + window.scrollX;
	var top = rect.top + window.scrollY;
	var bottom = rect.bottom + window.scrollY;
	return [ left, top, rect.right, bottom, rect.width, rect.height ];
}

function elementFromPosition(x, y) {
	return document.elementFromPoint(x, y);
}

function isOnTarget(javaNode){
	var boundingClientRect = getBoundingBox(javaNode);
	
	var width = boundingClientRect[4];
    var height = boundingClientRect[5];

    var left = boundingClientRect[0];
    var top = boundingClientRect[1];

    var offSetX = width / 2;
    var targetX = (left + offSetX);
    var offsetY = height * 1.5;
    var targetY = (top + offsetY);
    var currentTarget = elementFromPosition(targetX,targetY);
    var element = fromJava(javaNode);
    return currentTarget === element;
    
}
/**
 * 
 */
function getFormMethod(formElement) {
	var form = fromJava(formElement);
	if (form.nodeName.toLowerCase() === 'form')
		return form.method;

	throwUndefinedProperty('method', form);
}

function observeCSSProperties(aelement, properties, index) {
	var element = fromJava(aelement);
	_clog('start observe ' + index);
	_clog(element);
	var _style = new Object();
	for (var i = 0; i < properties.length; i++) {
		var p = properties[i];
		_style[p] = getCssPropertyValue(element, p)
		_clog("property:" + p + " with value: " + _style[p])

	}

	if (typeof window.css_mutated === 'undefined') {
		// initialize the global variable to accumulate the results
		window.css_mutated = {};
	}
	window.css_mutated[index] = _style;
	_clog('end observe ' + index);
	_clog(_style);
}

var CSSModRecord = function(property, newValue, oldValue) {

	return [ property, newValue, oldValue ];
};




function takeCSSRecords(aelement, index) {
	var element = fromJava(aelement);

	//_clog('start take ' + index);
	//_clog(element);
	if (typeof window.css_mutated === 'undefined')
		throw 'no css observers registered for ' + index;

	var _style = window.css_mutated[index];

	if (typeof _style === "undefined" || _style == null)
		throw 'no css observers found for ' + index;

	var records = [];

	for ( var p in _style) {
		var old = _style[p];
		var current = getCssPropertyValue(element, p);
		if (current !== old)
			records.push(new CSSModRecord(p, current, old))
	}
	// reset
	delete window.css_mutated[index];
	//_clog('end take ' + index);
	//_clog(records);
	return records;
}

/**
 * 
 */
// function getValue(element) {
// if ('value' in element)
// return element.value;
//
// throwUndefinedProperty('value', element);
// }
/**
 * 
 */
function setValue(aelement, value) {
	var element = fromJava(aelement);
	if ('value' in element)
		element.value = value;
	else
		throwUndefinedProperty('value', element);
}

function getChecked(aelement) {
	var element = fromJava(aelement);
	if ('checked' in element)
		return element.checked;

	throwUndefinedProperty('checked', element);
}

/**
 * 
 */
function setChecked(aelement, checked) {
	var element = fromJava(aelement);
	if ('checked' in element)
		element.checked = checked;
	else
		throwUndefinedProperty('checked', element);
}

function setSelected(aelement, selected) {
	var element = fromJava(aelement);
	if ('selected' in element)
		element.selected = selected;
	else
		throwUndefinedProperty('selected', element);
}

function setSelectedIndex(aelement, selectedIndex) {
	var element = fromJava(aelement);
	if ('selectedIndex' in element)
		element.selectedIndex = selectedIndex;
	else
		throwUndefinedProperty('selectedIndex', element);
}

function setSelectedOptionByText(sel, someText) {
	for (var i=0, n=sel.options.length;i<n;i++) {
	  if (sel.options[i].text===find){
	    sel.selectedIndex=i;
	    return;
	  }
	}
}


/**
 * 
 * @param {!Element}
 *            element
 * @param {string}
 *            property
 * @param {string}
 *            value
 */
function setCSSProperty(aelement, property, value) {
	var element = fromJava(aelement);
	//var exp = 'element.style.' + property + '="' + value + '"';
	//eval(exp);
	element.style.setProperty(property, value);
}

/**
 * 
 * @param element
 * @param properties
 * @param values
 */
function setCSSProperties(element, properties, values) {
	for (var i = 0; i < properties.length; i++) {
		setCSSProperty(element, properties[i], values[i]);
	}
}

/**
 * 
 * @param {!Element}
 *            element
 * @param {string}
 *            property
 * @param {string}
 *            value
 */
function setProperty(aelement, property, value) {
	var element = fromJava(aelement);
	//var exp = 'element.' + property + '="' + value + '"';
	//eval(exp);
	element[property]=value;
}

/**
 * 
 * @param element
 * @param property
 * @returns
 */
function getProperty(element, property) {
	//var exp = 'element.' + property;
	//return eval(exp);
	return fromJava(element)[property];
}

/**
 * 
 * @param {Node}
 *            node
 * @return {string}
 */
function prettyToString(anode) {
	var node = fromJava(anode);
	var attrs = node.attributes;
	var toString = node.localName;
	for (var i = 0; i < attrs.length; i++) {
		toString += ' - ' + attrs[i].nodeName + ':' + attrs[i].nodeValue;
		if (attrs[i].nodeName === 'id' || attrs[i].nodeName === 'name')
			return toString;// we stop in this case
	}
	return toString;
}

/**
 * 
 * @param node
 * @return {Array.<string|node>}
 */
function getChildNodes(anode) {
	var node = fromJava(anode);
	var children = node.childNodes;
	var output = [];
	for (var i = 0; i < children.length; i++) {
		output.push(toJava(children[i]));
	}
	return output;
}

function getNeighbourhood(anode, radius) {

	var element = fromJava(anode);
	element.scrollIntoView(true);
	var output = [];

	var rect = element.getBoundingClientRect();

	var north = [ rect.left + (rect.width / 2), rect.top - radius ];
	output.push(document.elementFromPoint(north[0], north[1]) || document.body);

	var ne = [ rect.right + radius, rect.top - radius ];
	output.push(document.elementFromPoint(ne[0], ne[1]) || document.body);

	var east = [ rect.right + radius, rect.top + (rect.height / 2) ];
	output.push(document.elementFromPoint(east[0], east[1]) || document.body);

	var se = [ rect.right + radius, rect.bottom + radius ];
	output.push(document.elementFromPoint(se[0], se[1]) || document.body);

	var south = [ rect.left + (rect.width / 2), rect.bottom + radius ];
	output.push(document.elementFromPoint(south[0], south[1]) || document.body);

	var sw = [ rect.left - radius, rect.bottom + radius ];
	output.push(document.elementFromPoint(sw[0], sw[1]) || document.body);

	var west = [ rect.left - radius, rect.top + (rect.height / 2) ];
	output.push(document.elementFromPoint(west[0], west[1]) || document.body);

	var nw = [ rect.left - radius, rect.top - radius ];

	output.push(document.elementFromPoint(nw[0], nw[1]) || document.body);

	return output;
}

/**
 * 
 * @return {Array}
 */
function getScrollXY() {
	// https://developer.mozilla.org/en-US/docs/DOM/window.scrollX
	var x = (window.pageXOffset !== undefined) ? window.pageXOffset
			: (document.documentElement || document.body.parentNode || document.body).scrollLeft;
	var y = (window.pageYOffset !== undefined) ? window.pageYOffset
			: (document.documentElement || document.body.parentNode || document.body).scrollTop;
	return [ x, y ];
}
/**
 * 
 * @param {!Node}
 *            node
 * @param {!Node}
 *            newChild
 * @param {Node|null}
 *            refChild
 * @return {Node}
 */
function insertBefore(node, newChild, refChild) {

	var added = fromJava(node).insertBefore(fromJava(newChild),
			fromJava(refChild));
	return toJava(added);
}

/**
 * 
 * @param {!Node}
 *            node
 * @param {!Node}
 *            newChild
 * @param {!number}
 *            refPosition
 * @return {Node}
 */
function insertBeforeText(node, newChild, refPosition) {
	var refChild = null;
	if (refPosition < node.childNodes.length)
		refChild = node.childNodes[refPosition];
	return insertBefore(node, newChild, refChild);
}

/**
 * 
 * @param {!Node}
 *            node
 * @param {!Node}
 *            parentNode
 * @param {!number}
 *            position
 * @param {Node}
 *            refChild
 * @return {Array}
 */
function insertTextBeforeElement(node, parentNode, position, refChild) {
	var newChild = parentNode.childNodes[position];
	var addedText = insertBefore(node, newChild, refChild);
	return [ addedText.data, addedText.parentNode,
			findPositionAmongChildren(addedText.parentNode, addedText) ];
}

/**
 * 
 * @param {!Node}
 *            node
 * @param {!Node}
 *            parentNode
 * @param {!number}
 *            position
 * @param {!Node}
 *            refChildParent
 * @param {!number}
 *            refChildPosition
 * @return {Array}
 */
function insertTextBeforeText(node, parentNode, position, refChildParent,
		refChildPosition) {

	var newChild = parentNode.childNodes[position];
	var refChild = null;
	if (refChildPosition < refChildParent.childNodes.length)
		refChild = refChildParent.childNodes[refChildPosition];

	var addedText = insertBefore(node, newChild, refChild);
	return [ addedText.data, addedText.parentNode,
			findPositionAmongChildren(addedText.parentNode, addedText) ];
}

/**
 * 
 * @param {!Node}
 *            startNodeParent
 * @param {!number}
 *            startNodePosition
 * @param {!number}
 *            startOffset
 * @param {!Node}
 *            endNodeParent
 * @param {!number}
 *            endNodePosition
 * @param {!number}
 *            endOffset
 * @return {Array.<float>}
 * @throw if this browser does not support document.createRange
 */
function createRangeTextToText(startNodeParent, startNodePosition, startOffset,
		endNodeParent, endNodePosition, endOffset) {

	var startNode = startNodeParent.childNodes[startNodePosition];
	var endNode = endNodeParent.childNodes[endNodePosition];
	if (document.createRange) {
		var range = document.createRange();
		range.setStart(startNode, startOffset);
		range.setEnd(endNode, endOffset);
		if (range.getBoundingClientRect) {
			var rect = range.getBoundingClientRect();
			range.detach(); // we just are interested in the bounding-box, so we
			// detach immediately
			if (rect) {
				var left = rect.left + window.scrollX;
				var top = rect.top + window.scrollY;
				var bottom = rect.bottom + window.scrollY;
				return [ left, top, rect.right, bottom, rect.width, rect.height ];

			}
		}
	}
	throw 'createRange is unsupported in this browser';
}

/**
 * 
 * @param {!Element}
 *            element
 * @return {Array.<string>|null}
 */
function getAttributes(aelement) {
	var element = fromJava(aelement);
	var attrs = element.attributes;
	var res = [];
	if (attrs === null)
		return null;
	for (var i = 0; i < attrs.length; i++) {
		res.push(toJava(attrs[i]));
	}
	return res;
}

/**
 * 
 * @param {!Node}
 *            node
 * @param {!Node}
 *            other
 * @return {number}
 */
function compareDocumentPosition(node, other) {
	var dn = fromJava(node);
	var doth = fromJava(other);
	return dn.compareDocumentPosition(doth);
}

/**
 * 
 * @param {!Document}
 *            node
 * @param {!Element}
 *            other
 * @return {number}
 */
function compareDocumentPositionDocToElement(node, other) {
	return compareDocumentPosition(node, other);
}

/**
 * 
 * @param {!Element}
 *            node
 * @param {!Document}
 *            other
 * @return {number}
 */
function compareDocumentPositionElementToDoc(node, other) {
	return compareDocumentPosition(node, other);
}

/**
 * 
 * @param {!Document}
 *            node
 * @param {!Document}
 *            other
 * @return {number}
 */
function compareDocumentPositionDocToDoc(node, other) {
	return compareDocumentPosition(node, other);
}

/**
 * 
 * @param {!Node}
 *            node
 * @param {!Node}
 *            otherParentNode
 * @param {!number}
 *            otherPosition
 * @return {number}
 */
function compareDocumentPositionElementToText(node, otherParentNode,
		otherPosition) {
	var other = otherParentNode.childNodes[otherPosition];
	return compareDocumentPosition(node, other);
}
/**
 * 
 * @param {!Node}
 *            nodeParent
 * @param {!number}
 *            nodePosition
 * @param {!Node}
 *            other
 * @return {number}
 */
function compareDocumentPositionTextToElement(nodeParent, nodePosition, other) {
	var node = nodeParent.childNodes[nodePosition];
	return compareDocumentPosition(node, other);
}
/**
 * 
 * @param {!Node}
 *            nodeParent
 * @param {!number}
 *            nodePosition
 * @param {!Node}
 *            otherParentNode
 * @param {!number}
 *            otherPosition
 * @return {number}
 */
function compareDocumentPositionTextToText(nodeParent, nodePosition,
		otherParentNode, otherPosition) {
	var node = nodeParent.childNodes[nodePosition];
	var other = otherParentNode.childNodes[otherPosition];
	return compareDocumentPosition(node, other);
}

/**
 * 
 * @param {number}
 *            key
 * @return {Array}
 */
function takeRecords(key) {
	if (typeof window.mutated === 'undefined')
		return [];
	if (typeof window.mutated[key] === 'undefined')
		return [];
	var records = window.mutated[key];
	window.mutated[key] = [];
	return records;
}

function disconnect(key) {
	if (typeof window.observerArray === 'undefined')
		return;
	if (typeof window.observerArray[arguments[0]] === 'undefined')
		return;
	window.observerArray[arguments[0]].disconnect();
}

function createInfo(rect, types, values, classes, parentOverlay) {

	// main container
	var infoBox = document.createElement('div');
	// infoBox.style.position = 'absolute';
	// prop.put("pointer-events", "none");
	// infoBox.style.pointerEvents = 'none';
	infoBox.style.width = '100%';
	// infoBox.classList.add('pos');
	infoBox.style.position = 'absolute';
	parentOverlay.appendChild(infoBox);

	var max = types.length;
	if (max < values.length)
		max = values.length;

	for (var i = 0; i < max; i++) {
		var row = infoBox.appendChild(document.createElement('div'));

		// apply css classes to that row
		for (var k = 0; k < classes[i].length; k++) {
			row.classList.add(classes[i][k]);
		}
		// row.classList.add('pos');
		row.style.position = 'absolute';
		var col1 = row.appendChild(document.createElement('span'));

		var col2 = row.appendChild(document.createElement('span'));

		if (types[i])
			col1.textContent = types[i];
		if (values[i])
			col2.textContent = ': ' + values[i];
	}

	var offset = [ 0, 0 ];
	computeOffset(infoBox, offset);

	infoBox.style.top = (rect.bottom + 2 + window.scrollY - offset[1]) + 'px';
	infoBox.style.left = (rect.left + window.scrollX - offset[0]) + 'px';

	return infoBox;
}


function createStyleSheet(rules) {
	var sheet = document.createElement('style');

	// sheet.innerHTML = ".info {position: absolute; z-index: 2; border: 1px
	// solid white; color: white; font-weight: bolder; background-color: black;}
	// .record {position: absolute; z-index: 1; border: 2px dashed red;}
	// .attribute {position: absolute; z-index: 1; border: 1px solid blue;}
	// .hidden { visibility: hidden; } .unhidden { visibility: visible; }";

	sheet.innerHTML = rules;
	return sheet;
}

/**
 * 
 * @param expression
 * @param list
 * @returns {Number}
 */
function findInList(expression, list) {
	for (var i = 0; i < list.length; i++) {
		if (list[i] === expression)
			return i;
	}
	throw 'the given list does not contain <"' + expression
			+ '">. Check your input for errors';
}

function computeOffset(overlay, offset) {

	var parent = overlay.offsetParent;

	if (parent) {
		offset[1] += parent.offsetTop;
		offset[0] += parent.offsetLeft;
		var blw = parseInt(getCssPropertyValue(overlay, 'border-left-width'),
				10);
		if (blw)
			offset[0] += blw;
		var btw = parseInt(getCssPropertyValue(overlay, 'border-top-width'), 10);
		if (btw)
			offset[1] += btw;
		computeOffset(parent, offset);
	}
}

function getDenormalizedOffset(node, offset) {
	var strVal = node.textContent;
	// Special treatment for the case of an all whitespace node (FIX ME)
	if (/^\s+$/.test(strVal))
		return offset;
	// End special treatment
	var pos = offset;
	var actual = 0;
	var lastWs = false;// To eliminate start whitespace
	var white = new RegExp(/^\s$/);
	var i = 0;
	for (i = 0; i < strVal.length; i++) {
		if (pos <= actual) {
			break;
		}
		var charAt = strVal.charAt(i);
		if (white.test(charAt)) {
			// Special treatment as apparently at the beginning of nodes some
			// whitespace is stripped but not all (FIX ME)
			// if (i == 0) continue;
			if (!lastWs) {
				actual++;
			}
			lastWs = true;
		} else {
			actual++;
			lastWs = false;
		}
		// _clog(char + ":" + actual);
	}
	return i;
}

function getRect(node, startOffset, endLocator, endOffset) {
	var range = document.createRange();
	var startOffsetDeNorm = startOffset;
	// getDenormalizedOffset(node, startOffset);
	range.setStart(node, startOffsetDeNorm);
	endNode = getNodeByXPath(endLocator);
	var endOffsetDeNorm = endOffset;
	// getDenormalizedOffset(endNode, endOffset);
	range.setEnd(endNode, endOffsetDeNorm);
	var rect = range.getBoundingClientRect();
	range.detach();
	return rect;
}

function mergeRectangles(a, b) {
	if (!b)
		return a;
	if (emptyRectangle(a))
		return b;
	if (emptyRectangle(b))
		return a;
	var vleft = Math.min(a.left, b.left);
	var vtop = Math.min(a.top, b.top);
	var vright = Math.max(a.right, b.right);
	var vbottom = Math.max(a.bottom, b.bottom);
	return {
		left : vleft,
		top : vtop,
		right : vright,
		bottom : vbottom
	};
}

function emptyRectangle(r) {
	if ((r.left >= r.right) || (r.top >= r.bottom))
		return true;
	return false;
}

function getOverlayForElement(element) {
//    var range = document.createRange();
//    range.setStart(element, 0);
//    range.setEndAfter(element);
//    var rectangle = range.getBoundingClientRect();
	var rectangle = element.getBoundingClientRect();
	
	var children = element.childNodes;
	for (var i = 0; i < children.length; i++) {
		// What about overlay
		if (children[i].nodeType === Node.ELEMENT_NODE
				&& children[i].tagName !== "OPTION") {
			var crect = getOverlayForElement(children[i]);
			rectangle = mergeRectangles(rectangle, crect);
		}
	}
	// Restrict bounding box again by using parent and overflow hidden
	var left = rectangle.left;
	var right = rectangle.right;
	var top = rectangle.top;
	var bottom = rectangle.bottom;
	var parent = element.parentNode;
	while (parent !== null) {
	  if (parent.getBoundingClientRect) {
		  parentRect = parent.getBoundingClientRect();
	      var computedStyle = document.defaultView.getComputedStyle(parent, null);
		  if (computedStyle.getPropertyValue('overflow')==='hidden' || computedStyle.getPropertyValue('overflow-x')==='hidden') {
			  if (parentRect.right > parentRect.left && parentRect.left > 0 ) {
				  left = Math.round(Math.max(left, parentRect.left));
				  right = Math.round(Math.min(right, parentRect.right));
			  }
		  }
		  if (computedStyle.getPropertyValue('overflow')==='hidden' || computedStyle.getPropertyValue('overflow-y')==='hidden') {
			  if (parentRect.bottom > parentRect.top && parentRect.top > 0 ) {
				  top = Math.round(Math.max(top, parentRect.top));
				  bottom = Math.round(Math.min(bottom, parentRect.bottom));
			  }
		  }
	  }
	  parent = parent.parentNode;
	}
	// What about floats and z-Index?
	
	return {
		left : left,
		top : top,
		right : right,
		bottom : bottom
	};
}

/**
 * 
 * @param element
 * @param classes
 * @returns {element}
 */
function overlayForElement(element, classes, types, values, infoClasses,
		parentOverlay) {
	var rect = getOverlayForElement(element);
	rect.width = rect.right - rect.left;
	rect.height = rect.bottom - rect.top;
	var over = document.createElement('span');
	over.style.position = 'absolute';
	// over.style.pointerEvents = 'none';
	parentOverlay.appendChild(over);

	// apply css classes
	for (var i = 0; i < classes.length; i++) {
		over.classList.add(classes[i]);
	}
	// over.classList.add('pos');
	over.style.position = 'absolute';

	var offset = [ 0, 0 ];
	computeOffset(over, offset);
	// _clog("offset for "+element);
	// console.dir(offset);

	// absolute positioning including scroll
	over.style.top = (rect.top + window.scrollY - offset[1]) + 'px';
	over.style.left = (rect.left + window.scrollX - offset[0]) + 'px';
	over.style.width = rect.width + 'px';
	over.style.height = rect.height + 'px';

	// attach Info If Any
	if (types.length > 0 || values.length > 0) {
		createInfo(rect, types, values, infoClasses, parentOverlay);
	}
	return over;
}

function overlayForText(text, range, classes, types, values, infoClasses,
		parentOverlay) {

	var over = document.createElement('span');
	// over.style.position = 'absolute';
	// over.style.pointerEvents = 'none';
	parentOverlay.appendChild(over);

	// apply css classes
	for (var i = 0; i < classes.length; i++) {
		over.classList.add(classes[i]);
	}
	// over.classList.add('pos');
	over.style.position = 'absolute';

	var offset = [ 0, 0 ];
	computeOffset(over, offset);

	var rect = getRect(text, range[0], range[1], range[2]);

	// absolute positioning including scroll
	over.style.top = (rect.top + window.scrollY - offset[1]) + 'px';
	over.style.left = (rect.left + window.scrollX - offset[0]) + 'px';
	over.style.width = rect.width + 'px';
	over.style.height = rect.height + 'px';

	// attach Info If Any
	if (types.length > 0 || values.length > 0) {
		createInfo(rect, types, values, infoClasses, parentOverlay);
	}
	return over;
}

function getImageSources() {
	var res = [];
	var imgs = getNodesByXPath('//img[@src]');
	for (var i = 0; i < imgs.length; i++) {
		res.push(imgs[i].getAttribute('src'))
	}
	return res;

}

function getLinkHRefs() {
	var res = [];
	var hrefs = getNodesByXPath('//a[@href]');
	for (var i = 0; i < hrefs.length; i++) {
		res.push(hrefs[i].getAttribute('href'))
	}
	return res;

}

function getIDAttributes() {
	var res = [];
	var ids = getNodesByXPath('//@id');
	for (var i = 0; i < ids.length; i++) {
		res.push(ids[i].nodeValue)
	}
	return res;

}

function getClassAttributes() {
	var res = [];
	var ids = getNodesByXPath('//@class');
	for (var i = 0; i < ids.length; i++) {
		res.push(ids[i].nodeValue)
	}
	return res;

}

/**
 * 
 * @param key
 * @param allNodes
 * @param childrenMap
 * @param classesMap
 * @param rangesMap
 */
function createOverlay(key, allNodes, childrenMap, classesMap, rangesMap,
		typesMap, valuesMap, infoClasses, parentOverlay) {

	var currentNode = getNodeByXPath(allNodes[key]);

	if (currentNode === null) {
		// throw 'cannot retrieve the node <"' + allNodes[key] + '">';
		log('cannot retrieve the node <"' + allNodes[key]
				+ '">, skip visualization');
		// nullify childred
		var children = childrenMap[key];
		for (var i = 0; i < children.length; i++) {
			var childKey = findInList(children[i], allNodes);
			// remove the node to avoid following matches
			allNodes[childKey] = null;
		}
		return;
	}

	var currentNodeType = currentNode.nodeType;

	if (currentNodeType !== 1 && currentNodeType !== 3)
		throw 'the current node is of type <"' + currentNode.nodeType
				+ '"> which is unsupported for visualization';

	var hasRange = rangesMap[key].length > 0;

	var overlay = null;
	if (currentNodeType === Node.ELEMENT_NODE) {
		if (hasRange)
			throw 'unsupported ranges on element node, it must be a text node : <"'
					+ allNodes[key] + '">';

		overlay = overlayForElement(currentNode, classesMap[key],
				typesMap[key], valuesMap[key], infoClasses[key], parentOverlay);
	} else {
		var range = rangesMap[key];
		if (!hasRange)
			range = [ 0, allNodes[key], currentNode.textContent.length ];

		overlay = overlayForText(currentNode, range, classesMap[key],
				typesMap[key], valuesMap[key], infoClasses[key], parentOverlay);
	}

	var children = childrenMap[key];
	for (var i = 0; i < children.length; i++) {
		var childKey = findInList(children[i], allNodes);
		createOverlay(childKey, allNodes, childrenMap, classesMap, rangesMap,
				typesMap, valuesMap, infoClasses, overlay)
		// remove the node to avoid following matches
		allNodes[childKey] = null;
	}

}


function createOverlayBox(box,parentOverlay) {
	 
	 
	//[[1,1,100,100],"<li>this is a list</li>",["attribute","record"]]
		
	  console.log("createOverlayBox");
		if (box === null)
			throw 'null fixed box, cannot create overlay';
		var over = document.createElement('div');
		over.innerHTML=box[1];
		over.style.position = 'fixed';
		over.style.pointerEvents = 'none';
		parentOverlay.appendChild(over);
	  var classes=box[2];
		// apply css classes
		for ( var i = 0; i < classes.length; i++) {
			over.classList.add(classes[i]);
		}

//		var offset = [ 0, 0 ];
//		computeOffset(over, offset);

		 //absolute positioning including scroll
	  var coord = box[0];
		over.style.top = coord[0]+ 'px';//(rect.top + window.scrollY - offset[1]) + 'px';
		over.style.left = coord[1]+ 'px';//(rect.left + window.scrollX - offset[0]) + 'px';
	  over.style.width = coord[2]+ 'px';//rect.width + 'px';
		over.style.height = coord[3]+ 'px';//rect.height + 'px';
	}


/**
 * @param {Number}
 *            overlayId to reference it back later
 * @param {Array.
 *            <string>} roots all roots' xpath locators
 * @param {Array.
 *            <string>} allNodes all nodes' xpath locators
 * @param {Array.
 *            <Array.<string>>} childrenMap implements a map by position [node
 *            --> [child1, child2,..]]
 * @param {Array.
 *            <Array.<string>>} classesMap implements a map by position [node
 *            --> [class1,class2,...]]
 * @param {Array.
 *            <Array.<Number>>} rangesMap implements a map by position [node
 *            --> [start,end]]
 */
function attachOverlay(overlayId, roots, allNodes, childrenMap, classesMap,
		rangesMap, cssRules, typesMap, valuesMap, infoClasses,fixedBoxes) {

	var sheet = createStyleSheet(cssRules);
	sheet.setAttribute('id', 'sheet-' + overlayId);
	document.body.appendChild(sheet);

	var mainOverlay = document.createElement('div');
	mainOverlay.classList.add('mainOverlay');
	// mainOverlay.style.position = 'absolute';
	document.body.appendChild(mainOverlay);
	mainOverlay.setAttribute('id', overlayId);

	 for ( var i = 0; i < fixedBoxes.length; i++) {
		 createOverlayBox(fixedBoxes[i],mainOverlay);
	 }
	 
	for (var i = 0; i < roots.length; i++) {
		var key = findInList(roots[i], allNodes);
		createOverlay(key, allNodes, childrenMap, classesMap, rangesMap,
				typesMap, valuesMap, infoClasses, mainOverlay);
		// we remove the node mapping to ensure no following matches
		// it is a multimap
		allNodes[key] = null;;
	}

	return takeLog();
}
/**
 * 
 * @param overlayId
 * @returns
 */
function getCSSStyleElement(overlayId) {
	return document.getElementById('sheet-' + overlayId);
}

function getStyleSheet(overlayId) {
	return getCSSStyleElement(overlayId).sheet;
}

function ruleListLength(overlayId) {
	var sheet = getStyleSheet(overlayId);
	if (sheet) {
		return sheet.cssRules.length;
	} else
		throw 'Cannot find css stylesheet for overlay ' + overlayId;
}

function getCSSRule(overlayId, rulePosition) {
	var sheet = getStyleSheet(overlayId);
	if (sheet) {
		return sheet.cssRules[rulePosition];
	} else
		throw 'Cannot find css stylesheet for overlay ' + overlayId;
}

/**
 * 
 * @param overlayId
 * @param rulePosition
 * @returns
 */
function getSelectorText(overlayId, rulePosition) {
	var rule = getCSSRule(overlayId, rulePosition);
	return rule.selectorText;
}

function findRulesBySelectorText(overlayId, selectorText) {
	var result = [];
	var len = ruleListLength(overlayId);
	for (var i = 0; i < len; i++) {
		var rule = getCSSRule(overlayId, i);
		if (rule.selectorText === selectorText)
			result.push(i);
	}
	return result;
}

function getRulePropertyValue(overlayId, rulePosition, propertyName) {
	var rule = getCSSRule(overlayId, rulePosition);
	return rule.style.getPropertyValue(propertyName);
}

function setRulePropertyValue(overlayId, rulePosition, propertyName, value) {
	var rule = getCSSRule(overlayId, rulePosition);
	rule.style.setProperty(propertyName, value);
}

function removeRuleProperty(overlayId, rulePosition, propertyName) {
	var rule = getCSSRule(overlayId, rulePosition);
	rule.style.removeProperty(propertyName);
}

function appendRule(overlayId, ruleText) {
	var sheet = getStyleSheet(overlayId);
	if (sheet) {
		var pos = sheet.cssRules.length
		sheet.insertRule(ruleText, pos)
	} else
		throw 'Cannot find css stylesheet for overlay ' + overlayId;
}

function deleteRule(overlayId, rulePosition) {
	var sheet = getStyleSheet(overlayId);
	if (sheet) {
		sheet.deleteRule(rulePosition)
	} else
		throw 'Cannot find css stylesheet for overlay ' + overlayId;
}

/**
 * 
 * @param overlayId
 * @returns
 */
function isCSSStyleSheetDisabled(overlayId) {
	var styleElement = getCSSStyleElement(overlayId);
	if (styleElement)
		return styleElement.disabled;
	throw 'Cannot find css stylesheet for overlay ' + overlayId;
}
/**
 * 
 * @param overlayId
 */
function disableCSSStyleSheet(overlayId) {
	var styleElement = getCSSStyleElement(overlayId);
	if (styleElement)
		styleElement.disabled = true;
	else
		throw 'Cannot find css stylesheet for overlay ' + overlayId;
}
/**
 * 
 * @param sheetId
 */
function enableCSSStyleSheet(overlayId) {
	var styleElement = getCSSStyleElement(overlayId);
	if (styleElement)
		styleElement.disabled = false;
	else
		throw 'Cannot find css stylesheet for overlay ' + overlayId;
}

/**
 * 
 * @param overlayId
 */
function detachOverlay(overlayId) {
	var sheet = document.getElementById('sheet-' + overlayId);
	if (sheet)
		sheet.parentNode.removeChild(sheet);

	var mainOveraly = document.getElementById(overlayId);
	if (mainOveraly)
		mainOveraly.parentNode.removeChild(mainOveraly);

}
/**
 * 
 * @param path
 * @param className
 */
function toggleCSSClass(path, className) {

	var el = getNodeByXPath(path);
	if (el.nodeType === Node.ELEMENT_NODE)
		el.classList.toggle(className);
	else
		throw 'Cannot toggle class  to not element nodes ' + path;

}
/**
 * 
 * @param path
 * @param className
 */
function addCSSClass(path, className) {

	var el = getNodeByXPath(path);
	if (el.nodeType === Node.ELEMENT_NODE)
		el.classList.add(className);
	else
		throw 'Cannot add class  to not element nodes ' + path;

}
/**
 * 
 * @param path
 * @param className
 */
function removeCSSClass(path, className) {

	var el = getNodeByXPath(path);
	if (el.nodeType === Node.ELEMENT_NODE)
		return el.classList.remove(className);
	else
		throw 'Cannot remove class  from not element nodes ' + path;

}
/**
 * 
 * @param path
 * @param className
 */
function containsCSSClass(path, className) {

	var el = getNodeByXPath(path);
	if (el.nodeType === Node.ELEMENT_NODE)
		el.classList.contains(className);
	else
		throw 'Cannot check contain class for not element nodes ' + path;

}

function isAncestorOf(element, expressions){
	var root = fromJava(element);
	for (var i = 0; i < expressions.length; i++) {
		var candidate = getNodeByXPath(expressions[i]);
		var found = false;
		while (candidate) {
		    if(candidate === root){
		    	found = true;
		    	break;
		    }
		    candidate = candidate.parentNode;
		}
		if(!found)
			return false;
	}
	return true;
	
}