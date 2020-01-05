function createInfo(rect, types, values, classes, parentOverlay) {

	// main container
	var infoBox = document.createElement('div');
	//infoBox.style.position = 'absolute';
	 //prop.put("pointer-events", "none");
	infoBox.style.pointerEvents = 'none';
	infoBox.style.width = '100%';
	//infoBox.classList.add('pos');
	infoBox.style.position = 'absolute';
	parentOverlay.appendChild(infoBox);

	var max = types.length;
	if (max < values.length)
		max = values.length;

	for ( var i = 0; i < max; i++) {
		var row = infoBox.appendChild(document.createElement('div'));

		// apply css classes to that row
		for ( var k = 0; k < classes[i].length; k++) {
			row.classList.add(classes[i][k]);
		}
		//row.classList.add('pos');
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

function getRect(node, startOffset, endLocator, endOffset) {

	var range = document.createRange();
	range.setStart(node, startOffset);
	endNode= getNodeByXPath(endLocator);
	range.setEnd(endNode, endOffset);
	var rect = range.getBoundingClientRect();
	range.detach();
	return rect;
}

function overlayForElement(element, classes, types, values, infoClasses,
		parentOverlay) {
	var rect = element.getBoundingClientRect();
	var over = document.createElement('span');
	over.style.position = 'absolute';
	over.style.pointerEvents = 'none';
	parentOverlay.appendChild(over);

	// apply css classes
	for ( var i = 0; i < classes.length; i++) {
		over.classList.add(classes[i]);
	}
//	over.classList.add('pos');
	over.style.position = 'absolute';

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
	//over.style.position = 'absolute';
	over.style.pointerEvents = 'none';
	parentOverlay.appendChild(over);

	// apply css classes
	for ( var i = 0; i < classes.length; i++) {
		over.classList.add(classes[i]);
	}
//	over.classList.add('pos');
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

function getNodeByXPath(path){
	return document.evaluate(path, document, null,
			XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
}

function createOverlay(key, allNodes, childrenMap, classesMap, rangesMap,
		typesMap, valuesMap, infoClasses, parentOverlay) {
		 console.log("creating overlay for node with key: "+key);

	var currentNode = getNodeByXPath(allNodes[key]);

	if (currentNode === null)
		throw 'cannot retrieve the node <"' + allNodes[key] + '">';

    var currentNodeType = currentNode.nodeType;

	if (currentNodeType !== 1 && currentNodeType !== 3)
		throw 'the current node is of type <"' + currentNode.nodeType
				+ '"> which is unsupported for visualization';

    var hasRange = rangesMap[key].length > 0;

	var overlay = null;

	if (currentNodeType === 1) {
	if(hasRange)
	   throw 'unsupported ranges on element node, it must be a text node : <"' + allNodes[key] + '">';

		overlay = overlayForElement(currentNode, classesMap[key],
				typesMap[key], valuesMap[key], infoClasses[key], parentOverlay);
	} else {
	    var range = rangesMap[key];
	    if(!hasRange)
	       range = [0, allNodes[key], currentNode.textContent.lenght];

		overlay = overlayForText(currentNode, range, classesMap[key],
				typesMap[key], valuesMap[key], infoClasses[key], parentOverlay);
	}

	var children = childrenMap[key];
	for ( var i = 0; i < children.length; i++) {
		var childKey = findInList(children[i], allNodes);
		console.log("from node key "+key+", call child key: "+childKey);
		childOver = createOverlay(childKey, allNodes, childrenMap, classesMap,
				rangesMap, typesMap, valuesMap, infoClasses, overlay)
        //remove the node to avoid following matches
		allNodes[childKey]=null;

	}

	return overlay;

}

function attachOverlay(overlayId, roots, allNodes, childrenMap, classesMap,
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
		console.log("creating for root key: "+key);
		var overlay = createOverlay(key, allNodes, childrenMap, classesMap,
				rangesMap, typesMap, valuesMap, infoClasses, mainOverlay);
//remove the node to avoid following matches
		allNodes[key]=null;
	}

}

var id = "1";
var roots= ['/html/body/div'];
//var roots= ['/html/body/p/child::text()', '/html/body/p/child::text()'];
var allnodes= ['/html/body/div','/html/body/div/p[1]/child::text()', '/html/body/div/p[2]/child::text()'];
var childrenMap = [["/html/body/div/p[1]/child::text()", "/html/body/div/p[2]/child::text()"], [], []];
var classesMap = [["record"], ["attribute"], ["attribute"]];
var rangesMap = [[], [2,"/html/body/div/p[2]/child::text()",2], [5,"/html/body/div/p[2]/child::text()",8]];
var cssRules = ".attribute { z-index: 1; border: 1px solid blue;} .record { z-index: 1; border: 2px solid red;}"
var typesMap = [[], [], []];
var valuesMap = [[], [], []];
var infoClasses = [[], [], []];

attachOverlay(id,roots, allnodes, childrenMap, classesMap, rangesMap,cssRules, typesMap, valuesMap, infoClasses);