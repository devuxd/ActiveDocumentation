// todo: message visualizer on html page (not on console)
// 		 save rules to firebase
//       more rules
//		 find patterns
// 		 click to navigate to code

// check everything related to a certain rule, so try and query all the things related to a design rule

//  ENTITY CLASSES MUST REGISTER THEMSELVES

var RuleContainer =  class RC{

	static test1(){
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

	static test2(){
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

	static test3(){
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

	static test4(){
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

	static test5(){
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

}


ruleTable = 
[
	{
		"header" : "All @Entity classes must register themselves.",
		"description" : "All @Entity classes must register themselves so that they can be used with Objectify. You need the statement ObjectifyService.register(TheEntityClassInQuestion);",
		"ruleFunc" : "test1"
	},
	{
		"header" : "Saves and deletes should be followed by now().",
		"description" : "Saves and deletes should be followed by now() preemptively so that Google App Engine always has the most up to date version of an object.",
		"ruleFunc" : "test2"
	},
	{
		"header" : "All @Entity classes must have a field called \'id\' which is labeled with @Id.",
		"description" : "In order for Objectify to properly function, @Entity classes require ids for unique identification.",
		"ruleFunc" : "test3"
	},
	{
		"header" : "All @Entity classes must have two constructors at the minimum, with one of them being a no-arg constructor.",
		"description" : "@Entity classes need a no-arg constructor for serialization and require another constructor with args for the initialization of the object.",
		"ruleFunc" : "test4"
	},
	{
		"header" : "When contains is called on a Collection of @Entity classes, the @Entity class in question requires an equals() function to be defined.",
		"description" : "An equals function is needed because the default equals() function will not work as usually intended.",
		"ruleFunc" : "test5"
	},
	{
		"header" : "A",
		"description" : "Alphabet.",
		"ruleFunc" : "test5"
	}
];