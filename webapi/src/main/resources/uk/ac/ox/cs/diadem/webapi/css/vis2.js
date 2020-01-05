function createInfo(rect, types, values, classes, parentOverlay) {

	// main container
	var infoBox = document.createElement('div');
	infoBox.style.position = 'absolute';
	infoBox.style.width = '100%';
	parentOverlay.appendChild(infoBox);

	var max = types.length;
	if (max < values.length)
		max = values.length;

	for ( var i = 0; i < max; i++) {
		var row = infoBox.appendChild(document.createElement('div'));

		// apply css classes to that row
		for ( var k = 0; k < classes.length; k++) {
			row.classList.add(classes[k]);
		}
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

function getCssPropertyValue(elem, prop) {
	return window.getComputedStyle
	// Modern browsers.
	? window.getComputedStyle(elem, null).getPropertyValue(prop)
	// IE8 and older.
	: elem.currentStyle.getAttribute(prop);
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
	for ( var i = 0; i < list.length; i++) {
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

function getRect(node, startOffset, endOffset) {

	var range = document.createRange();
	range.setStart(node, startOffset);
	range.setEnd(node, endOffset);
	var rect = range.getBoundingClientRect();
	range.detach();
	return rect;
}

/**
 * 
 * @param element
 * @param classes
 * @returns {element}
 */
function overlayForElement(element, classes, types, values, infoClasses,
		parentOverlay) {
	var rect = element.getBoundingClientRect();
	var over = document.createElement('span');
	parentOverlay.appendChild(over);

	// apply css classes
	for ( var i = 0; i < classes.length; i++) {
		over.classList.add(classes[i]);
		over.setAttribute('id', classes[i]);
	}

	var offset = [ 0, 0 ];
	computeOffset(over, offset);

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
	parentOverlay.appendChild(over);

	// apply css classes
	for ( var i = 0; i < classes.length; i++) {
		over.classList.add(classes[i]);
	}

	var offset = [ 0, 0 ];
	computeOffset(over, offset);

	var rect = getRect(text, range[0], range[1]);

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

	var currentNode = document.evaluate(allNodes[key], document, null,
			XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;

	if (currentNode === null)
		throw 'cannot retrieve the node <"' + allNodes[key] + '">';

	var overlay = null;
	if (currentNode.nodeType === 1) {
		overlay = overlayForElement(currentNode, classesMap[key],
				typesMap[key], valuesMap[key], infoClasses[key], parentOverlay);
	} else if (currentNode.nodeType === 3) {
		overlay = overlayForText(currentNode, rangesMap[key], classesMap[key],
				typesMap[key], valuesMap[key], infoClasses[key], parentOverlay);
	} else
		throw 'the current node is of type <"' + currentNode.nodeType
				+ '"> which is unsupported for visualization';

	var children = childrenMap[key];
	for ( var i = 0; i < children.length; i++) {
		var childKey = findInList(children[i], allNodes);
		childOver = createOverlay(childKey, allNodes, childrenMap, classesMap,
				rangesMap, typesMap, valuesMap, infoClasses, overlay)

		// overlay.appendChild(childOver)
		// document.body.appendChild(childOver);
	}

	return overlay;

}

/**
 * 
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
function doDisplay(overlayId, roots, allNodes, childrenMap, classesMap,
		rangesMap, cssRules, typesMap, valuesMap, infoClasses) {

	var sheet = createStyleSheet(cssRules);
	sheet.setAttribute('id', 'sheet-' + overlayId);
	document.body.appendChild(sheet);

	var mainOverlay = document.createElement('div');
	mainOverlay.style.position = 'absolute';
	document.body.appendChild(mainOverlay);
	mainOverlay.setAttribute('id', overlayId);

	for ( var i = 0; i < roots.length; i++) {
		var key = findInList(roots[i], allNodes);
		var overlay = createOverlay(key, allNodes, childrenMap, classesMap,
				rangesMap, typesMap, valuesMap, infoClasses, mainOverlay);
	}

}

function disableSheet(sheetId) {
	var sheet = document.getElementById('sheet-' + sheetId);
	if (sheet)
		sheet.disabled = true;
}
function enableSheet(sheetId) {
	var sheet = document.getElementById('sheet-' + sheetId);
	if (sheet)
		sheet.disabled = false;
}

function removeOverlay(overlayId) {
	var sheet = document.getElementById('sheet-' + overlayId);
	if (sheet)
		sheet.parentNode.removeChild(sheet);

	var mainOveraly = document.getElementById(overlayId);
	if (mainOveraly)
		mainOveraly.parentNode.removeChild(mainOveraly);

}

var overlayId = 'xx';
var roots = [ "html/body[1]/div[@class='subpage-content clearfix'][1]" ];
var allNodes = [
		"html/body[1]/div[@class='subpage-content clearfix'][1]",
		"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]",
		"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_info'][1]/ul[@class='prop_keypoints'][1]/li[2]/strong[1]/text()[1]",
		"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_info'][1]/ul[@class='prop_keypoints'][1]/li[1]/strong[1]/text()[1]",
		"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_info'][1]/span[@class='prop_price'][1]/text()[1]",
		"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_img'][1]/div[@class='prop_statuses'][1]/span[@class='is_reduced'][1]/text()[1]",
		"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_info'][1]/ul[@class='prop_keypoints'][1]/li[4]/strong[1]/text()[1]" ];
var childrenMap = [
		[ "html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]" ],
		[
				"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_info'][1]/ul[@class='prop_keypoints'][1]/li[2]/strong[1]/text()[1]",
				"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_info'][1]/ul[@class='prop_keypoints'][1]/li[1]/strong[1]/text()[1]",
				"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_info'][1]/span[@class='prop_price'][1]/text()[1]",
				"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_img'][1]/div[@class='prop_statuses'][1]/span[@class='is_reduced'][1]/text()[1]",
				"html/body[1]/div[@class='subpage-content clearfix'][1]/div[@class='proplist_wrap proplist_wrap_ox12 '][1]/div[@class='prop_info'][1]/ul[@class='prop_keypoints'][1]/li[4]/strong[1]/text()[1]" ],
		[], [], [], [], [], ];
var classesMap = [ [ "dataArea" ], [ "record" ], [ "attribute" ],
		[ "attribute" ], [ "attribute" ], [ "attribute" ], [ "attribute" ] ];
var rangesMap = [ [], [], [ 0, 2 ], [ 0, 2 ], [ 0, 10 ], [ 0, 9 ], [ 0, 5 ] ];
var cssRules = '.info2 {position: absolute; z-index: 2; border: 1px solid white; color: black; font-weight: bolder; background-color: red;} .info {position: absolute; z-index: 2; border: 1px solid white; color: white; font-weight: bolder; background-color: black;} .dataArea {position: absolute; z-index: 1; border: 2px dashed green;} .record {position: absolute; z-index: 1; border: 2px dashed red;} .attribute {position: absolute; z-index: 1; border: 1px solid blue;} .hidden { visibility: hidden; } .unhidden { visibility: visible; }';
var typesMap = [ [ "dataArea" ], [ "record" ], [ "bathroom_number" ],
		[ "bedroom_number" ], [ "location" ], [ "price" ],
		[ "property_status" ], [ "property_type" ] ];
var valuesMap = [ [ "dataArea" ], [ "record" ], [ "13" ], [ "15" ],
		[ "£1,499,950" ], [ "New price" ], [ "House" ] ];
var infoClasses = [ [ "info" ], [ "info2" ], [ "info" ], [ "info2" ],
		[ "info" ], [ "info2" ], [ "info" ] ];

doDisplay(overlayId,roots, allNodes, childrenMap, classesMap, rangesMap, cssRules,typesMap,valuesMap, infoClasses);
// enableSheet('xx');
//removeOverlay('xx');
