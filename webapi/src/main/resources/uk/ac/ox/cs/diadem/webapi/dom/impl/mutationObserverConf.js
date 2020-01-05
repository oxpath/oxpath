
//requires file commons.js

var AttributeModRecord = function(type, targetNode, attrName,oldValue) {
	var newValue=null;
	if(targetNode!=null)
		newValue=targetNode.getAttribute(attrName);
        return [type, targetNode, attrName,oldValue,newValue];
    };

var DataModRecord = function(type, targetNode, oldValue) {
	var tnode=targetNode;
	if(targetNode!=null && targetNode.nodeType===3)
		tnode=new TextOrCommentNodeRepresentative(targetNode,findPositionAmongChildren(targetNode.parentNode, targetNode));

        return [type, tnode, oldValue,targetNode.nodeValue];
    };

var ChildListModRecord = function(type, targetNode, addedNodes, removedNodes) {

	var tnode=targetNode;
	if(targetNode!=null && targetNode.nodeType===3)
		tnode=new TextOrCommentNodeRepresentative(targetNode, findPositionAmongChildren(targetNode.parentNode, targetNode))
        return [type, tnode, addedNodes, removedNodes];

    };



function handleEvent(m){
if(m.type==='attributes'){
	storeOutput(new AttributeModRecord(m.type, m.target, m.attributeName,m.oldValue));
}

if(m.type==='childList'){
	var filteredAddedNodes=[];
    var filteredRemovedNodes=[];
    for (var i=0;i< m.addedNodes.length;i++) {
        var node=m.addedNodes[i];
        if(node instanceof HTMLElement)
            filteredAddedNodes.push(node);
        if(node instanceof Text)
        	 filteredAddedNodes.push(new TextOrCommentNodeRepresentative(node, findPositionAmongChildren(node.parentNode, node)));
    }

    for (var i=0;i< m.removedNodes.length;i++) {
        var node=m.removedNodes[i];
        if(node instanceof HTMLElement)
            filteredRemovedNodes.push(node);
        if(node instanceof Text)
        	 filteredAddedNodes.push(new TextOrCommentNodeRepresentative(node, findPositionAmongChildren(node.parentNode, node)));
    }
    storeOutput(new ChildListModRecord(m.type, m.target, filteredAddedNodes,filteredRemovedNodes));
}

	if(m.type==='characterData'){
		storeOutput(new DataModRecord(m.type, m.target, m.oldValue));
	}

}


function storeOutput(record){

if (typeof window.mutated === 'undefined'){
    //initialize the global variable to accumulate the results
	window.mutated=[];
}
//var index=arguments[6];
var index=indexKey;
if(typeof window.mutated[index] === 'undefined'){

 window.mutated[index]=[];
}

window.mutated[index].push(record);
}


function createObserver(index){

if (typeof window.observerArray === 'undefined'){
    //initialize the global variable to accumulate the results
	window.observerArray=[];
}

if (typeof window.observerArray[index] === 'undefined'){
    //initialize the global variable to accumulate the results
	window.observerArray[index]=[];
}

var observer = new MutationObserver(function(mutations) {

    mutations.forEach(function(mutation) {
            handleEvent(mutation);
    });
});


window.observerArray[index]=observer;
return observer;
}



// configuration of the observer:
//attributeFilter=['style','selected'];
//var config = {childList: true, attributes:true, subtree:true, characterData: true, attributeFilter:attributeFilter};

var config = null;
if(arguments[5] === null)
	config={childList: arguments[1], attributes:arguments[2], attributeOldValue:arguments[2], subtree:arguments[3], characterData: arguments[4],characterDataOldValue: arguments[4]};
else
	config={childList: arguments[1], attributes:arguments[2], attributeOldValue:arguments[2], subtree:arguments[3], characterData: arguments[4],characterDataOldValue: arguments[4], attributeFilter:arguments[5]};

//var target = document.getElementsByTagName('body')[0];
var target = arguments[0];
//key ofr this element target
var indexKey=arguments[6];


var observer=createObserver(indexKey);
// pass in the target node, as well as the observer options
// create an observer instance
observer.observe(target, config);
// to validate
return true;
