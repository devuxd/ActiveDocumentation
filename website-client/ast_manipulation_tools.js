// !! Everyone has properties, but not everyone has children

// walk across the tree and execute commands on each node (DFS)
function astTreeWalk(node, func){

	// function used to process the node's properties
	func(node);

	if(!node.hasOwnProperty("children")){
		return;
	}

	// continue traversing the tree
	var i;
	for(i = 0; i < node["children"].length; i++){
		astTreeWalk(node["children"][i], func);
	}

}

// return the first node that matches a criteria indicated by func (DFS)
// if match cant be found, then function returns null
function astTreeFindFirstDFS(node, func){

	if(func(node)){
		return node;
	}

	// else continue traversing the tree
	if(!node.hasOwnProperty("children")){
		return null;
	}
	var i;
	for(i = 0; i < node["children"].length; i++){
		var val = astTreeFindFirstDFS(node["children"][i], func);
		if(val != null){
			return val;
		}
	}

	return null;
}

// return a list of all nodes that match a criteria indicated by func (DFS)
function astTreeFindAll(root, func){

	var list = [];
	astTreeFindAllAux(root, func, list);
	return list;

}

function astTreeFindAllAux(node, func, list){

	// console.log(node);

	// if criteria matches, add node to list
	if(func(node)){
		list.push(node);
	}

	if(!node.hasOwnProperty("children")){
		return;
	}
	// continue traversing the tree
	var i;
	for(i = 0; i < node["children"].length; i++){
		astTreeFindAllAux(node["children"][i], func, list);
	}

}

// add properties to everything including project hierarchy and ASTs
function addParentPropertyToNodes(root){
	var setParent = function(o){
    	var i;
    	if(!o.hasOwnProperty("children")){
			return;
		}
		for(i = 0; i < o["children"].length; i++){
			o["children"][i].properties.parent = o;
			setParent(o["children"][i]);
		}
	}
	setParent(root);
}

function getAllFilesOfType(fileType){

	var foo = function(o){
		if(!o.hasOwnProperty("properties")){
			return false;
		}
		if(!o["properties"].hasOwnProperty("fileType")){
			return false;
		}
		if(!o["properties"].hasOwnProperty("name")){
			return false;
		}
		if(fileType.toUpperCase() === o["properties"]["fileType"].toUpperCase()){
			return true;
		}
		return false;
	}
	return projectHierarchyFindAll(foo, false);

}

// walk down the project hierarchy starting at the given node
// arguments include node, the function to perform on the node
// and whether or not to have the explorer go into the ASTs
function projectHierarchyTreeWalk(node, func, examineASTs){

	// overloaded so that user starts at root if node is not provided
	if(arguments.length == 2){
		projectHierarchyTreeWalk(projectHierarchy, node, func); // args translated
		return;
	}

	// function used to process the node's properties
	func(node);

	if(!node.hasOwnProperty("children")){
		return;
	}

	// we want to still examine the file itself without going into it
	if(node.properties.hasOwnProperty("ast") && !examineASTs){
		return;
	}

	// continue traversing the tree
	var i;
	for(i = 0; i < node["children"].length; i++){
		projectHierarchyTreeWalk(node["children"][i], func, examineASTs);
	}

}

function projectHierarchyFindFirstDFS(node, func, examineASTs){

	if(arguments.length == 2){
		return projectHierarchyFindFirstDFS(projectHierarchy, node, func);
	}

	if(func(node)){
		return node;
	}

	// else continue traversing the tree
	if(!node.hasOwnProperty("children")){
		return null;
	}

	// we want to still examine the file itself without going into it
	if(node.properties.hasOwnProperty("ast") && !examineASTs){
		return null;
	}

	var i;
	for(i = 0; i < node["children"].length; i++){
		var val = projectHierarchyFindFirstDFS(node["children"][i], func, examineASTs);
		if(val != null){
			return val;
		}
	}

	return null;
}

function projectHierarchyFindAll(root, func, examineASTs){
	var list = [];
	if(arguments.length == 2){
		projectHierarchyFindAllAux(projectHierarchy, root, func, list);
	}else{
		projectHierarchyFindAllAux(root, func, examineASTs, list);
	}
	return list;
}

function projectHierarchyFindAllAux(node, func, examineASTs, list){

	// if criteria matches, add node to list
	if(func(node)){
		list.push(node);
	}

	if(!node.hasOwnProperty("children")){
		return;
	}

	// we want to still examine the file itself without going into it
	if(node.properties.hasOwnProperty("ast") && !examineASTs){
		return;
	}

	// continue traversing the tree
	var i;
	for(i = 0; i < node["children"].length; i++){
		projectHierarchyFindAllAux(node["children"][i], func, examineASTs, list);
	}

}

function getFile(fileName, fileType){
	var foo = function(o){
		if(!o.hasOwnProperty("properties")){
			return false;
		}
		if(!o["properties"].hasOwnProperty("fileType")){
			return false;
		}
		if(!o["properties"].hasOwnProperty("name")){
			return false;
		}
		if(fileName.toUpperCase() === o["properties"]["name"].toUpperCase() && fileType.toUpperCase() === o["properties"]["fileType"].toUpperCase()){
			return true;
		}
		return false;
	}
	return projectHierarchyFindFirstDFS(foo, false);
}

function maniptest(){
	
	console.log("------------------");

	astTreeWalk(getFile("Farm", "JAVA").properties.ast, function(n){
		if(!n.properties.hasOwnProperty("type")){
			return;
		}
		if(psiInstanceOf(n.properties.type, "PsiMethod")){
			console.log(n.properties.name);
		}
	});
	
	console.log("------------------");

	console.log(astTreeFindFirstDFS(getFile("Farm", "JAVA").properties.ast, function(n){
		if(!n.properties.hasOwnProperty("type")){
			return false;
		}
		if(psiInstanceOf(n.properties.type, "PsiMethod")){
			// console.log(n.properties.name);
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