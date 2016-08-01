function runRules(){

	function createScript(src) {
	    var s = document.createElement("script");
	    s.src = src;
	    return s;
	}

	var oldScript = document.querySelector("script[src^='rules.js']");
	var newScript = createScript("rules.js?t=" + (new Date()).getTime());
	// newScript.onload(reloadRules);
	newScript.onload=function(){reloadRules();};
	document.head.replaceChild(newScript, oldScript);

}

function reloadRules(){

	var ruleBox = document.getElementById("ruleBox").firstElementChild.firstElementChild.firstElementChild;
	ruleBox.innerHTML = "";
	var a;
	var b;

	for(a = 0; a < ruleTable.length; a++){
		var ruleResultsOfA = ruleTable[a].ruleFunc();

		if(ruleResultsOfA.length == 0){
			continue;
		}

		var theDiv = document.createElement("div");
		theDiv.setAttribute("class", "card blue-grey darken-1");

		/////////// child 1 - add title and description ///////////////

		var ch1OfDiv = document.createElement("div");
		ch1OfDiv.setAttribute("class", "card-content white-text");
		theDiv.appendChild(ch1OfDiv);

		var theSpanHeader = document.createElement("span");
		theSpanHeader.setAttribute("class", "card-title");
		theSpanHeader.innerHTML = ruleTable[a].header;
		ch1OfDiv.appendChild(theSpanHeader);

		var theDescriptionP = document.createElement("p");
		theDescriptionP.innerHTML = ruleTable[a].description;
		ch1OfDiv.appendChild(theDescriptionP);

		/////////// child 2 - the links ///////////////

		var ch2OfDiv = document.createElement("div");
		ch2OfDiv.setAttribute("class", "card-action");
		theDiv.appendChild(ch2OfDiv);

		for(b = 0; b < ruleResultsOfA.length; b++){
			var anALink = document.createElement("a");
			anALink.onclick = function(){
				// alert(this.innerHTML + "\n" + this.getAttribute("data-class-of-line-num"));
				var messageToSend = {"source":"WEB", "destination":"IDEA", "command":"JUMP_TO_CLASS_WITH_LINE_NUM"};

				var dataOfMessage = {};
				dataOfMessage["fileName"] = this.getAttribute("data-class-of-line-num");
				dataOfMessage["lineNumber"] = parseInt(this.innerHTML);

				messageToSend["data"] = dataOfMessage;
				ws.send(JSON.stringify(messageToSend));
			}
			anALink.setAttribute("data-class-of-line-num", getContainingFileGivenASTNode(ruleResultsOfA[b]).properties.fileName);
			anALink.innerHTML = ruleResultsOfA[b].properties.textOffset; // need to find the class where the error is
			ch2OfDiv.appendChild(anALink);
		}

		ruleBox.appendChild(theDiv);

	}


	return [];

}