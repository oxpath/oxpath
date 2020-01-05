function createElementAndAppendToBody(a,b){var c=fromJava(a);return toJava(c.querySelector("body").appendChild(c.createElement(b)))}function setAttribute(a,b,c){fromJava(a).setAttribute(b,c)}function removeAttribute(a,b){fromJava(a).removeAttribute(b)}function clickOnElement(a){fromJava(a).click()}function getInnerHTML(a){return fromJava(a).innerHTML}function getOuterHTML(a){return fromJava(a).outerHTML}
function getFormElements(a){a=fromJava(a);if(a instanceof HTMLFormElement){a=a.elements;for(var b=[],c=0;c<a.length;c++)"object"!==a[c].nodeName.toLowerCase()&&b.push(toJava(a[c]));return b}return null}function getDocumentElement(){return document.documentElement}function getDocumentElementTextContent(){return document.documentElement.textContent}function getLastChildOfDocument(a){return a.documentElement}function getFirstChildOfDocument(a){return a.documentElement}
function getChildrenOfDocument(a){return[a.documentElement]}function getNodeType(a){return fromJava(a).nodeType}function getNodeValue(a){return fromJava(a).nodeValue}function getFirstChild(a){a=fromJava(a).firstChild;return toJava(a)}function getLastChild(a){a=fromJava(a).lastChild;return toJava(a)}function getNextSibling(a){a=fromJava(a).nextSibling;return toJava(a)}function getNextSiblingOfTextNode(a,b){return getNextSibling(a.childNodes[b])}
function getPreviousSibling(a){a=fromJava(a).previousSibling;return toJava(a)}function getPreviousSiblingOfTextNode(a,b){return getPreviousSibling(a.childNodes[b])}function appendChild(a,b){var c=fromJava(a),d=fromJava(b);return toJava(c.appendChild(d))}function appendTextChild(a,b,c){a=a.appendChild(b.childNodes[c]);return[a.data,a.parentNode,findPositionAmongChildren(a.parentNode,a)]}function getParentNode(a){a=fromJava(a).parentNode;return toJava(a)}
function getLocalName(a){return fromJava(a).localName}function getNodeName(a){return fromJava(a).nodeName}function getTextContent(a){return fromJava(a).textContent}function isSameNode(a,b){return fromJava(a)===fromJava(b)}function isSameDocument(a,b){return a===b}function isSameTextNode(a,b,c,d){return isSameNode(a.childNodes[b],c.childNodes[d])}function isEqualNode(a,b){return fromJava(a).isEqualNode(fromJava(b))}function isEqualDocument(a,b){return a.isEqualNode(b)}
function isEqualTextNode(a,b,c,d){return isEqualNode(a.childNodes[b],c.childNodes[d])}function removeChild(a,b){var c=fromJava(a).removeChild(fromJava(b));return toJava(c)}function removeTextChild(a,b,c){a.removeChild(b.childNodes[c]);return!0}function setTextContentForTextNode(a,b,c){a=a.childNodes[b];a.textContent=c;return a.textContent}function setTextContent(a,b){fromJava(a).textContent=b}
function getBoundingBox(a){a=fromJava(a).getBoundingClientRect();return[a.left+window.scrollX,a.top+window.scrollY,a.right,a.bottom+window.scrollY,a.width,a.height]}function elementFromPosition(a,b){return document.elementFromPoint(a,b)}function isOnTarget(a){var b=getBoundingBox(a),b=elementFromPosition(b[0]+b[4]/2,b[1]+1.5*b[5]);a=fromJava(a);return b===a}function getFormMethod(a){a=fromJava(a);if("form"===a.nodeName.toLowerCase())return a.method;throwUndefinedProperty("method",a)}
function observeCSSProperties(a,b,c){a=fromJava(a);_clog("start observe "+c);_clog(a);for(var d={},e=0;e<b.length;e++){var g=b[e];d[g]=getCssPropertyValue(a,g);_clog("property:"+g+" with value: "+d[g])}"undefined"===typeof window.css_mutated&&(window.css_mutated={});window.css_mutated[c]=d;_clog("end observe "+c);_clog(d)}var CSSModRecord=function(a,b,c){return[a,b,c]};
function takeCSSRecords(a,b){var c=fromJava(a);if("undefined"===typeof window.css_mutated)throw"no css observers registered for "+b;var d=window.css_mutated[b];if("undefined"===typeof d||null==d)throw"no css observers found for "+b;var e=[],g;for(g in d){var f=d[g],h=getCssPropertyValue(c,g);h!==f&&e.push(new CSSModRecord(g,h,f))}delete window.css_mutated[b];return e}function setValue(a,b){var c=fromJava(a);"value"in c?c.value=b:throwUndefinedProperty("value",c)}
function getChecked(a){a=fromJava(a);if("checked"in a)return a.checked;throwUndefinedProperty("checked",a)}function setChecked(a,b){var c=fromJava(a);"checked"in c?c.checked=b:throwUndefinedProperty("checked",c)}function setSelected(a,b){var c=fromJava(a);"selected"in c?c.selected=b:throwUndefinedProperty("selected",c)}function setSelectedIndex(a,b){var c=fromJava(a);"selectedIndex"in c?c.selectedIndex=b:throwUndefinedProperty("selectedIndex",c)}
function setSelectedOptionByText(a,b){for(var c=0,d=a.options.length;c<d;c++)if(a.options[c].text===find){a.selectedIndex=c;break}}function setCSSProperty(a,b,c){fromJava(a).style.setProperty(b,c)}function setCSSProperties(a,b,c){for(var d=0;d<b.length;d++)setCSSProperty(a,b[d],c[d])}function setProperty(a,b,c){fromJava(a)[b]=c}function getProperty(a,b){return fromJava(a)[b]}
function prettyToString(a){var b=fromJava(a);a=b.attributes;for(var b=b.localName,c=0;c<a.length&&(b+=" - "+a[c].nodeName+":"+a[c].nodeValue,"id"!==a[c].nodeName&&"name"!==a[c].nodeName);c++);return b}function getChildNodes(a){a=fromJava(a).childNodes;for(var b=[],c=0;c<a.length;c++)b.push(toJava(a[c]));return b}
function getNeighbourhood(a,b){var c=fromJava(a);c.scrollIntoView(!0);var d=[],c=c.getBoundingClientRect(),e=[c.left+c.width/2,c.top-b];d.push(document.elementFromPoint(e[0],e[1])||document.body);e=[c.right+b,c.top-b];d.push(document.elementFromPoint(e[0],e[1])||document.body);e=[c.right+b,c.top+c.height/2];d.push(document.elementFromPoint(e[0],e[1])||document.body);e=[c.right+b,c.bottom+b];d.push(document.elementFromPoint(e[0],e[1])||document.body);e=[c.left+c.width/2,c.bottom+b];d.push(document.elementFromPoint(e[0],
e[1])||document.body);e=[c.left-b,c.bottom+b];d.push(document.elementFromPoint(e[0],e[1])||document.body);e=[c.left-b,c.top+c.height/2];d.push(document.elementFromPoint(e[0],e[1])||document.body);c=[c.left-b,c.top-b];d.push(document.elementFromPoint(c[0],c[1])||document.body);return d}
function getScrollXY(){return[void 0!==window.pageXOffset?window.pageXOffset:(document.documentElement||document.body.parentNode||document.body).scrollLeft,void 0!==window.pageYOffset?window.pageYOffset:(document.documentElement||document.body.parentNode||document.body).scrollTop]}function insertBefore(a,b,c){a=fromJava(a).insertBefore(fromJava(b),fromJava(c));return toJava(a)}function insertBeforeText(a,b,c){var d=null;c<a.childNodes.length&&(d=a.childNodes[c]);return insertBefore(a,b,d)}
function insertTextBeforeElement(a,b,c,d){a=insertBefore(a,b.childNodes[c],d);return[a.data,a.parentNode,findPositionAmongChildren(a.parentNode,a)]}function insertTextBeforeText(a,b,c,d,e){b=b.childNodes[c];c=null;e<d.childNodes.length&&(c=d.childNodes[e]);a=insertBefore(a,b,c);return[a.data,a.parentNode,findPositionAmongChildren(a.parentNode,a)]}
function createRangeTextToText(a,b,c,d,e,g){a=a.childNodes[b];e=d.childNodes[e];if(document.createRange&&(d=document.createRange(),d.setStart(a,c),d.setEnd(e,g),d.getBoundingClientRect&&(c=d.getBoundingClientRect(),d.detach(),c)))return[c.left+window.scrollX,c.top+window.scrollY,c.right,c.bottom+window.scrollY,c.width,c.height];throw"createRange is unsupported in this browser";}
function getAttributes(a){a=fromJava(a).attributes;var b=[];if(null===a)return null;for(var c=0;c<a.length;c++)b.push(toJava(a[c]));return b}function compareDocumentPosition(a,b){var c=fromJava(a),d=fromJava(b);return c.compareDocumentPosition(d)}function compareDocumentPositionDocToElement(a,b){return compareDocumentPosition(a,b)}function compareDocumentPositionElementToDoc(a,b){return compareDocumentPosition(a,b)}
function compareDocumentPositionDocToDoc(a,b){return compareDocumentPosition(a,b)}function compareDocumentPositionElementToText(a,b,c){return compareDocumentPosition(a,b.childNodes[c])}function compareDocumentPositionTextToElement(a,b,c){return compareDocumentPosition(a.childNodes[b],c)}function compareDocumentPositionTextToText(a,b,c,d){return compareDocumentPosition(a.childNodes[b],c.childNodes[d])}
function takeRecords(a){if("undefined"===typeof window.mutated||"undefined"===typeof window.mutated[a])return[];var b=window.mutated[a];window.mutated[a]=[];return b}function disconnect(a){"undefined"!==typeof window.observerArray&&"undefined"!==typeof window.observerArray[a]&&window.observerArray[a].disconnect()}
function createInfo(a,b,c,d,e){var g=document.createElement("div");g.style.width="100%";g.style.position="absolute";e.appendChild(g);e=b.length;e<c.length&&(e=c.length);for(var f=0;f<e;f++){for(var h=g.appendChild(document.createElement("div")),k=0;k<d[f].length;k++)h.classList.add(d[f][k]);h.style.position="absolute";k=h.appendChild(document.createElement("span"));h=h.appendChild(document.createElement("span"));b[f]&&(k.textContent=b[f]);c[f]&&(h.textContent=": "+c[f])}b=[0,0];computeOffset(g,b);
g.style.top=a.bottom+2+window.scrollY-b[1]+"px";g.style.left=a.left+window.scrollX-b[0]+"px";return g}function createStyleSheet(a){var b=document.createElement("style");b.innerHTML=a;return b}function findInList(a,b){for(var c=0;c<b.length;c++)if(b[c]===a)return c;throw'the given list does not contain <"'+a+'">. Check your input for errors';}
function computeOffset(a,b){var c=a.offsetParent;if(c){b[1]+=c.offsetTop;b[0]+=c.offsetLeft;var d=parseInt(getCssPropertyValue(a,"border-left-width"),10);d&&(b[0]+=d);(d=parseInt(getCssPropertyValue(a,"border-top-width"),10))&&(b[1]+=d);computeOffset(c,b)}}function getDenormalizedOffset(a,b){var c=a.textContent;if(/^\s+$/.test(c))return b;for(var d=0,e=!1,g=new RegExp(/^\s$/),f=0,f=0;f<c.length&&!(b<=d);f++){var h=c.charAt(f);g.test(h)?(e||d++,e=!0):(d++,e=!1)}return f}
function getRect(a,b,c,d){var e=document.createRange();e.setStart(a,b);endNode=getNodeByXPath(c);e.setEnd(endNode,d);a=e.getBoundingClientRect();e.detach();return a}function mergeRectangles(a,b){if(!b)return a;if(emptyRectangle(a))return b;if(emptyRectangle(b))return a;var c=Math.min(a.left,b.left),d=Math.min(a.top,b.top),e=Math.max(a.right,b.right),g=Math.max(a.bottom,b.bottom);return{left:c,top:d,right:e,bottom:g}}function emptyRectangle(a){return a.left>=a.right||a.top>=a.bottom?!0:!1}
function getOverlayForElement(a){for(var b=a.getBoundingClientRect(),c=a.childNodes,d=0;d<c.length;d++)if(c[d].nodeType===Node.ELEMENT_NODE&&"OPTION"!==c[d].tagName)var e=getOverlayForElement(c[d]),b=mergeRectangles(b,e);c=b.left;d=b.right;e=b.top;b=b.bottom;for(a=a.parentNode;null!==a;){if(a.getBoundingClientRect){parentRect=a.getBoundingClientRect();var g=document.defaultView.getComputedStyle(a,null);("hidden"===g.getPropertyValue("overflow")||"hidden"===g.getPropertyValue("overflow-x"))&&parentRect.right>
parentRect.left&&0<parentRect.left&&(c=Math.round(Math.max(c,parentRect.left)),d=Math.round(Math.min(d,parentRect.right)));("hidden"===g.getPropertyValue("overflow")||"hidden"===g.getPropertyValue("overflow-y"))&&parentRect.bottom>parentRect.top&&0<parentRect.top&&(e=Math.round(Math.max(e,parentRect.top)),b=Math.round(Math.min(b,parentRect.bottom)))}a=a.parentNode}return{left:c,top:e,right:d,bottom:b}}
function overlayForElement(a,b,c,d,e,g){a=getOverlayForElement(a);a.width=a.right-a.left;a.height=a.bottom-a.top;var f=document.createElement("span");f.style.position="absolute";g.appendChild(f);for(var h=0;h<b.length;h++)f.classList.add(b[h]);f.style.position="absolute";b=[0,0];computeOffset(f,b);f.style.top=a.top+window.scrollY-b[1]+"px";f.style.left=a.left+window.scrollX-b[0]+"px";f.style.width=a.width+"px";f.style.height=a.height+"px";(0<c.length||0<d.length)&&createInfo(a,c,d,e,g);return f}
function overlayForText(a,b,c,d,e,g,f){var h=document.createElement("span");f.appendChild(h);for(var k=0;k<c.length;k++)h.classList.add(c[k]);h.style.position="absolute";c=[0,0];computeOffset(h,c);a=getRect(a,b[0],b[1],b[2]);h.style.top=a.top+window.scrollY-c[1]+"px";h.style.left=a.left+window.scrollX-c[0]+"px";h.style.width=a.width+"px";h.style.height=a.height+"px";(0<d.length||0<e.length)&&createInfo(a,d,e,g,f);return h}
function getImageSources(){for(var a=[],b=getNodesByXPath("//img[@src]"),c=0;c<b.length;c++)a.push(b[c].getAttribute("src"));return a}function getLinkHRefs(){for(var a=[],b=getNodesByXPath("//a[@href]"),c=0;c<b.length;c++)a.push(b[c].getAttribute("href"));return a}function getIDAttributes(){for(var a=[],b=getNodesByXPath("//@id"),c=0;c<b.length;c++)a.push(b[c].nodeValue);return a}
function getClassAttributes(){for(var a=[],b=getNodesByXPath("//@class"),c=0;c<b.length;c++)a.push(b[c].nodeValue);return a}
function createOverlay(a,b,c,d,e,g,f,h,k){var l=getNodeByXPath(b[a]);if(null===l)for(log('cannot retrieve the node <"'+b[a]+'">, skip visualization'),a=c[a],k=0;k<a.length;k++)l=findInList(a[k],b),b[l]=null;else{var m=l.nodeType;if(1!==m&&3!==m)throw'the current node is of type <"'+l.nodeType+'"> which is unsupported for visualization';var p=0<e[a].length,n=null;if(m===Node.ELEMENT_NODE){if(p)throw'unsupported ranges on element node, it must be a text node : <"'+b[a]+'">';n=overlayForElement(l,d[a],
g[a],f[a],h[a],k)}else n=e[a],p||(n=[0,b[a],l.textContent.length]),n=overlayForText(l,n,d[a],g[a],f[a],h[a],k);a=c[a];for(k=0;k<a.length;k++)l=findInList(a[k],b),createOverlay(l,b,c,d,e,g,f,h,n),b[l]=null}}
function createOverlayBox(a,b){console.log("createOverlayBox");if(null===a)throw"null fixed box, cannot create overlay";var c=document.createElement("div");c.innerHTML=a[1];c.style.position="fixed";c.style.pointerEvents="none";b.appendChild(c);for(var d=a[2],e=0;e<d.length;e++)c.classList.add(d[e]);d=a[0];c.style.top=d[0]+"px";c.style.left=d[1]+"px";c.style.width=d[2]+"px";c.style.height=d[3]+"px"}
function attachOverlay(a,b,c,d,e,g,f,h,k,l,m){f=createStyleSheet(f);f.setAttribute("id","sheet-"+a);document.body.appendChild(f);f=document.createElement("div");f.classList.add("mainOverlay");document.body.appendChild(f);f.setAttribute("id",a);for(a=0;a<m.length;a++)createOverlayBox(m[a],f);for(a=0;a<b.length;a++)m=findInList(b[a],c),createOverlay(m,c,d,e,g,h,k,l,f),c[m]=null;return takeLog()}function getCSSStyleElement(a){return document.getElementById("sheet-"+a)}
function getStyleSheet(a){return getCSSStyleElement(a).sheet}function ruleListLength(a){var b=getStyleSheet(a);if(b)return b.cssRules.length;throw"Cannot find css stylesheet for overlay "+a;}function getCSSRule(a,b){var c=getStyleSheet(a);if(c)return c.cssRules[b];throw"Cannot find css stylesheet for overlay "+a;}function getSelectorText(a,b){return getCSSRule(a,b).selectorText}
function findRulesBySelectorText(a,b){for(var c=[],d=ruleListLength(a),e=0;e<d;e++)getCSSRule(a,e).selectorText===b&&c.push(e);return c}function getRulePropertyValue(a,b,c){return getCSSRule(a,b).style.getPropertyValue(c)}function setRulePropertyValue(a,b,c,d){getCSSRule(a,b).style.setProperty(c,d)}function removeRuleProperty(a,b,c){getCSSRule(a,b).style.removeProperty(c)}
function appendRule(a,b){var c=getStyleSheet(a);if(c)c.insertRule(b,c.cssRules.length);else throw"Cannot find css stylesheet for overlay "+a;}function deleteRule(a,b){var c=getStyleSheet(a);if(c)c.deleteRule(b);else throw"Cannot find css stylesheet for overlay "+a;}function isCSSStyleSheetDisabled(a){var b=getCSSStyleElement(a);if(b)return b.disabled;throw"Cannot find css stylesheet for overlay "+a;}
function disableCSSStyleSheet(a){var b=getCSSStyleElement(a);if(b)b.disabled=!0;else throw"Cannot find css stylesheet for overlay "+a;}function enableCSSStyleSheet(a){var b=getCSSStyleElement(a);if(b)b.disabled=!1;else throw"Cannot find css stylesheet for overlay "+a;}function detachOverlay(a){var b=document.getElementById("sheet-"+a);b&&b.parentNode.removeChild(b);(a=document.getElementById(a))&&a.parentNode.removeChild(a)}
function toggleCSSClass(a,b){var c=getNodeByXPath(a);if(c.nodeType===Node.ELEMENT_NODE)c.classList.toggle(b);else throw"Cannot toggle class  to not element nodes "+a;}function addCSSClass(a,b){var c=getNodeByXPath(a);if(c.nodeType===Node.ELEMENT_NODE)c.classList.add(b);else throw"Cannot add class  to not element nodes "+a;}function removeCSSClass(a,b){var c=getNodeByXPath(a);if(c.nodeType===Node.ELEMENT_NODE)return c.classList.remove(b);throw"Cannot remove class  from not element nodes "+a;}
function containsCSSClass(a,b){var c=getNodeByXPath(a);if(c.nodeType===Node.ELEMENT_NODE)c.classList.contains(b);else throw"Cannot check contain class for not element nodes "+a;}function isAncestorOf(a,b){for(var c=fromJava(a),d=0;d<b.length;d++){for(var e=getNodeByXPath(b[d]),g=!1;e;){if(e===c){g=!0;break}e=e.parentNode}if(!g)return!1}return!0};