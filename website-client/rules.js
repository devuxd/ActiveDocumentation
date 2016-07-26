// ALL ENTITY CLASSES MUST REGISTER THEMSELVES

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

		console.log(classArr);
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

		console.log(classArr);
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
			targ.push("You are missing ObjectifyService.register(" + classArr[i].properties.name + ".class);");
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

		console.log(classArr);
		return classArr;
	}

	findAllPsiExpressionStatements();
	
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

		console.log(classArr);
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