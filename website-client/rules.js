// todo: message visualizer on html page (not on console)
// 		 save rules to firebase
//       more rules
//		 find patterns
// 		 click to navigate to code

// ALL ENTITY CLASSES MUST REGISTER THEMSELVES

function generateRuleTable(){

	var ruleTable = [];
	var i;
	for(i = 0; i < 5; i++){
		ruleTable.push({});
	}

	ruleTable[0].header = "All @Entity classes must register themselves.";
	ruleTable[0].description = "All @Entity classes must register themselves so that they can be used with Objectify. You need the statement ObjectifyService.register(TheEntityClassInQuestion);";
	ruleTable[0].relatedLinks = ["https://github.com/objectify/objectify/wiki/Entities", "https://www.google.com/", "https://github.com/"];
	// ruleTable[0].relatedRules = [ruleId1, ruleId2, ruleId3];

	ruleTable[1].header = "Saves and deletes should be followed by now().";
	ruleTable[1].description = "Saves and deletes should be followed by now() preemptively so that Google App Engine always has the most up to date version of an object.";

	ruleTable[2].header = "All @Entity classes must have a field called \'id\' which is labeled with @Id.";
	ruleTable[2].description = "In order for Objectify to properly function, @Entity classes require ids for unique identification.";

	ruleTable[3].header = "All @Entity classes must have two constructors at the minimum, with one of them being a no-arg constructor.";
	ruleTable[3].description = "@Entity classes need a no-arg constructor for serialization and require another constructor with args for the initialization of the object.";

	ruleTable[4].header = "When contains is called on a Collection of @Entity classes, the @Entity class in question requires an equals() function to be defined.";
	ruleTable[4].description = "An equals function is needed because the default equals() function will not work as usually intended.";

	return ruleTable;

}

ruleTable = generateRuleTable();

function runRules(){
	// console.clear();
	var ruleBox = document.getElementById("ruleBox").firstElementChild.firstElementChild.firstElementChild;
	ruleBox.innerHTML = "";
	var arr = [test1(), test2(), test3(), test4(), test5()];
	var a;
	var b;

	for(a = 0; a < arr.length; a++){

		if(arr[a].length == 0){
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

		for(b = 0; b < arr[a].length; b++){
			var anALink = document.createElement("a");
			anALink.innerHTML = arr[a][b].properties.textOffset; // need to find the class where the error is
			ch2OfDiv.appendChild(anALink);
			// res.push(arr[a][b]);
		}

		ruleBox.appendChild(theDiv);

	}

	var res = [];
	for(a = 0; a < arr.length; a++){
		for(b = 0; b < arr[a].length; b++){
			res.push(arr[a][b]);
		}
	}
	return res;
}

function test1(){

	console.log("ENTITY CLASSES THAT HAVE NOT REGISTERED THEMSELVES");

	function findAllPsiExpressionStatements(){
		var listOfFiles = getAllFilesOfType("JAVA");
		var i = 0;
		var classArr = [];
		for(i = 0; i < listOfFiles.length; i++){
			var arr = astTreeFindAll(listOfFiles[i].properties.ast, function(n){
				return psiInstanceOf(n.properties.psiType, "PsiExpressionStatement") && n.properties.text.startsWith("ObjectifyService.register(");
			});
			var j = 0;
			for(j = 0; j < arr.length; j++){
				classArr.push(arr[j]);
			}
		}

		// console.log(classArr);
		return classArr;
	}

	function findAllEntityClassesInAst(){
		var listOfFiles = getAllFilesOfType("JAVA");
		var i = 0;
		var classArr = [];
		for(i = 0; i < listOfFiles.length; i++){
			var arr = astTreeFindAll(listOfFiles[i].properties.ast, function(n){
				return psiInstanceOf(n.properties.psiType, "PsiClass") && n.properties.hasOwnProperty("annotations") && n.properties.annotations.indexOf("@Entity") > -1;
			});
			var j = 0;
			for(j = 0; j < arr.length; j++){
				classArr.push(arr[j]);
			}
		}

		// console.log(classArr);
		return classArr;
	}

	var targ = [];
	var classArr = findAllEntityClassesInAst();
	var expArr = findAllPsiExpressionStatements();
	var i;
	for(i = 0; i < classArr.length; i++){
		var j;
		var hasThing = false;
		for(j = 0; j < expArr.length; j++){
			if(expArr[j].properties.text.startsWith("ObjectifyService.register(" + classArr[i].properties.name)){
				hasThing = true;
				break;
			}
		}
		if(!hasThing){
			targ.push(classArr[i]);
			// targ.push("You are missing ObjectifyService.register(" + classArr[i].properties.name + ".class);");
		}
	}
	return targ;
}

/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////

// for testing methods above
function maniptest(){

	console.log("maniptest");
	
	console.log("------------------");

	astTreeWalk(getFile("Farm", "JAVA").properties.ast, function(n){
		if(!n.properties.hasOwnProperty("type")){
			return;
		}
		if(psiInstanceOf(n.properties.type, "PsiMethod")){
			// console.log(n.properties.name);
		}
	});
	
	console.log("------------------");

	console.log(astTreeFindFirstDFS(getFile("Farm", "JAVA").properties.ast, function(n){
		if(!n.properties.hasOwnProperty("type")){
			return false;
		}
		if(psiInstanceOf(n.properties.type, "PsiMethod")){
			return true;
		}
		return false;
	}));

	console.log("------------------");

	console.log(astTreeFindAll(getFile("Goose", "JAVA").properties.ast, function(n){
			if(!n.properties.hasOwnProperty("type")){
				return false;
			}
			if(psiInstanceOf(n.properties.type, "PsiMethod")){
				return true;
			}
			return false;
		}));

	console.log("------------------");

	projectHierarchyTreeWalk(function(n){
		console.log(n);
	}, false);

	console.log("------------------");

	console.log(projectHierarchyFindFirstDFS(function(o){
		if(!o["properties"].hasOwnProperty("fileType")){
			return false;
		}
		if(o["properties"]["fileType"].toUpperCase() === "JAVA"){
			return true;
		}
		return false;
	}, false));

	console.log("------------------");

	console.log(projectHierarchyFindAll(function(o){
		if(!o["properties"].hasOwnProperty("fileType")){
			return false;
		}
		if(o["properties"]["fileType"].toUpperCase() === "JAVA"){
			return true;
		}
		return false;
	}, false));

	console.log("------------------");

	console.log(getFile("Goose", "JAVA"));

	console.log("------------------");

	console.log(getAllFilesOfType("JAVA"));

}

/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////

// UPDATES AND DELETES SHOULD BE IMMEDIATELY STORED

function test2(){

	console.log("Find .entity() without .now().");

	// ^\s*ofy\s*\(\s*\)\s*\.\s*(save|delete)\s*\(\s*\)\s*\.\s*entity\s*\(\s*[^]*\s*\)\s*$ /gmi
	// ofy().save().entity(...).now()
	function findAllPsiExpressionStatements(){
		var patt = new RegExp("^\\s*ofy\\s*\\(\\s*\\)\\s*\\.\\s*(save|delete)\\s*\\(\\s*\\)\\s*\\.\\s*entity\\s*\\(\\s*[^]*\\s*\\)\\s*$", "gmi");
		var listOfFiles = getAllFilesOfType("JAVA");
		var i = 0;
		var classArr = [];
		for(i = 0; i < listOfFiles.length; i++){
			var arr = astTreeFindAll(listOfFiles[i].properties.ast, function(n){
				return psiInstanceOf(n.properties.psiType, "PsiExpression") 
				&& !n.properties.text.endsWith(".now()") 
				&& !n.properties.text.endsWith(".now") 
				&& patt.test(n.properties.text) 
				&& n.properties.hasOwnProperty("parent") 
				&& !n.properties.parent.properties.text.endsWith(".now") 
				&& n.properties.type.startsWith("com.googlecode.objectify.Result<");
			});
			var j = 0;
			for(j = 0; j < arr.length; j++){
				classArr.push(arr[j]);
			}
		}

		// console.log(classArr);
		return classArr;
	}

	return findAllPsiExpressionStatements();
	
}

/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////

function test3(){

	console.log("Entity classes that dont have @Id fields.");

	function findAllEntityClassesInAst(){
		var listOfFiles = getAllFilesOfType("JAVA");
		var i = 0;
		var classArr = [];
		for(i = 0; i < listOfFiles.length; i++){
			var arr = astTreeFindAll(listOfFiles[i].properties.ast, function(n){
				return psiInstanceOf(n.properties.psiType, "PsiClass") && n.properties.hasOwnProperty("annotations") && n.properties.annotations.indexOf("@Entity") > -1;
			});
			var j = 0;
			for(j = 0; j < arr.length; j++){
				classArr.push(arr[j]);
			}
		}

		// console.log(classArr);
		return classArr;
	}

	var targ = [];
	var classArr = findAllEntityClassesInAst();
	var i;
	for(i = 0; i < classArr.length; i++){
		var j;
		var found = false;
		for(j = 0; j < classArr[i].children.length; j++){
			var child = classArr[i].children[j];
			if(child.hasOwnProperty("properties") 
			&& child.properties.hasOwnProperty("psiType") 
			&& child.properties.psiType === "PsiFieldImpl" 
			&& child.properties.hasOwnProperty("annotations") 
			&& child.properties.annotations.indexOf("@Id") > -1){
				found = true;
				break;
			}
		}
		if(!found){
			targ.push(classArr[i]);
		}
	}
	return targ;
}

/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////

// need no arg and def constructor

function test4(){

	console.log("need no arg and def constructor");

	function findAllEntityClassesInAst(){
		var listOfFiles = getAllFilesOfType("JAVA");
		var i = 0;
		var classArr = [];
		for(i = 0; i < listOfFiles.length; i++){
			var arr = astTreeFindAll(listOfFiles[i].properties.ast, function(n){
				return psiInstanceOf(n.properties.psiType, "PsiClass") && n.properties.hasOwnProperty("annotations") && n.properties.annotations.indexOf("@Entity") > -1;
			});
			var j = 0;
			for(j = 0; j < arr.length; j++){
				classArr.push(arr[j]);
			}
		}

		// console.log(classArr);
		return classArr;
	}

	var targ = [];
	var classArr = findAllEntityClassesInAst();
	var i;
	for(i = 0; i < classArr.length; i++){
		var j;
		var foundOther = false;
		var foundArglessConst = false;
		for(j = 0; j < classArr[i].children.length; j++){
			var child = classArr[i].children[j];
			if(child.hasOwnProperty("properties") 
			&& child.properties.hasOwnProperty("psiType") 
			&& child.properties.psiType === "PsiMethodImpl"
			&& classArr[i].properties.name === child.properties.name){
				if(child.children[3].children.length == 2){
					foundArglessConst = true;
				}else if(child.children[3].children.length > 2){
					foundOther = true;
				}
			}
		}
		if(!(foundOther && foundArglessConst)){
			targ.push(classArr[i]);
		}
	}
	return targ;
}

/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////

function test5(){

	console.log("collection contains requires equals");
	var listOfFiles = getAllFilesOfType("JAVA");

	function entityClassHasEquals(ob){
		// why this? look through all psimethodimpls
		// return ob.children[3].properties.text === "equals";
		var x = 0;
		for(x = 0; x < ob.children.length; x++){
			if(psiInstanceOf(ob.children[x].properties.psiType, "PsiMethodImpl") && ob.children[x].properties.name === "equals"){
				return true;
			}
		}
		return false;
	}

	function findAllEntityClassesInAst(){
		var listOfFiles = getAllFilesOfType("JAVA");
		var i = 0;
		var classArr = [];
		for(i = 0; i < listOfFiles.length; i++){
			var arr = astTreeFindAll(listOfFiles[i].properties.ast, function(n){
				return psiInstanceOf(n.properties.psiType, "PsiClass") && n.properties.hasOwnProperty("annotations") && n.properties.annotations.indexOf("@Entity") > -1 && !entityClassHasEquals(n);
			});
			var j = 0;
			for(j = 0; j < arr.length; j++){
				classArr.push(arr[j]);
			}
		}

		// console.log(classArr);
		return classArr;
	}

	var entityClasses = findAllEntityClassesInAst(); // ones without equals

	function getBaseOfGeneric(st){
		var ind = st.indexOf("<");
		if(ind < 0){
			return st;
		}else{
			return st.substring(0, ind);
		}
	}

	function isEntity(st){
		// properties.qualifiedName
		var k;
		for(k = 0; k < entityClasses.length; k++){
			if(instanceOf(st, entityClasses[k].properties.qualifiedName)){
				return true;
			}
		}
		return false;
	}

	function findAllVars(){
		var i = 0;
		var classArr = [];
		for(i = 0; i < listOfFiles.length; i++){
			var arr = astTreeFindAll(listOfFiles[i].properties.ast, function(n){
				return psiInstanceOf(n.properties.psiType, "PsiMethodCallExpressionImpl") && n.children[0].properties.hasOwnProperty("referenceName") && n.children[0].properties.referenceName === "contains" && isEntity(n.children[1].children[1].properties.type);
			});
			var j = 0;
			for(j = 0; j < arr.length; j++){
				classArr.push(arr[j]);
			}
		}

		return classArr;
	}

	return findAllVars();

}