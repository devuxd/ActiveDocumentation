var projectHierarchy = null;

document.observe("dom:loaded", function() {
    function log(text) {
        $("log").innerHTML = (new Date).getTime() + ": " + (!Object.isUndefined(text) && text !== null ? text.escapeHTML() : "null") + $("log").innerHTML;
    }

    if (!window.WebSocket) {
        alert("FATAL: WebSocket not natively supported. This demo will not work!");
    }

    var ws;

    $("uriForm").observe("submit", function(e) {
        e.stop();
        ws = new WebSocket($F("uri"));
        ws.onopen = function() {
            log("[WebSocket#onopen]\n");
        }
        ws.onmessage = function(e) {
            // log("[WebSocket#onmessage] Message: '" + e.data + "'\n"); // CONTROLS WHETHER MESSAGES ARE DISPLAYED OR NOT
            var message = JSON.parse(e.data);
            if(message.command === "INITIAL_PROJECT_HIERARCHY"){
                projectHierarchy = message.data;
                addParentPropertyToNodes(projectHierarchy);
                console.log("initial project hierarchy added");
            }else if(message.command === "UPDATE_CODE_IN_FILE"){
                var messageInfo = message.data;
                var targetFileName = messageInfo.canonicalPath;
                var curr = projectHierarchy;
                
                while(!(curr.canonicalPath === targetFileName)){
                    
                    var i;
                    for(i = 0; i < curr.children.length; i++){
                        if(targetFileName.startsWith(curr.children[i].canonicalPath)){
                            curr = curr.children[i];
                            break;
                        }
                    }
                }

                curr.code = messageInfo.code;
                curr.ast = messageInfo.ast;
                console.log("Code updated in " + curr.canonicalPath);
                // ?

            }else if(message.command === "DELETE_FILE"){

                var messageInfo = message.data;
                var targetFileName = messageInfo.canonicalPath;
                var curr = projectHierarchy;
                
                while(curr != null && !(curr.canonicalPath === targetFileName)){
                    var i;
                    for(i = 0; i < curr.children.length; i++){
                        if(targetFileName === curr.children[i].canonicalPath){
                            curr.children.splice(i, 1);
                            curr = null;
                            break;
                        }else if(targetFileName.startsWith(curr.children[i].canonicalPath)){
                            curr = curr.children[i];
                            break;
                        }
                    }
                }

                console.log("Deleted item at " + targetFileName);

            }else if(message.command === "RENAME_FILE"){

                var messageInfo = message.data;
                var targetFileName = messageInfo.oldCanonicalPath;
                var curr = projectHierarchy;
                
                while(!(curr.canonicalPath === targetFileName)){
                    var i;
                    for(i = 0; i < curr.children.length; i++){
                        if(targetFileName.startsWith(curr.children[i].canonicalPath)){
                            curr = curr.children[i];
                            break;
                        }
                    }
                }

                curr.name = messageInfo.name;
                curr.canonicalPath = messageInfo.newCanonicalPath;
                console.log("Renamed " + targetFileName + " to " + curr.canonicalPath);

            }else if(message.command === "CREATE_FILE"){

                var messageInfo = message.data;
                var targetFileName = messageInfo.parent;
                var curr = projectHierarchy;
                
                while(!(curr.canonicalPath === targetFileName)){
                    console.log(curr.canonicalPath);
                    var i;
                    for(i = 0; i < curr.children.length; i++){
                        if(targetFileName.startsWith(curr.children[i].canonicalPath)){
                            curr = curr.children[i];
                            break;
                        }
                    }
                }

                curr.children.push(messageInfo);
                console.log("created file at " + messageInfo.canonicalPath);

            }else{

                console.log("Oops.");
                console.log(e.command);

                // don't know what to do with this type of message
            }
        }
        ws.onclose = function() {
            log("[WebSocket#onclose]\n");
            $("uri", "connect").invoke("enable");
            $("disconnect").disable();
            ws = null;
        }
        $("uri", "connect").invoke("disable");
        $("disconnect").enable();
    });

    $("sendForm").observe("submit", function(e) {
        e.stop();
        if (ws) {
            var textField = $("textField");
            ws.send(textField.value);
            log("[WebSocket#send]      Send:    '" + textField.value + "'\n");
            textField.value = "";
            textField.focus();
        }
    });

    $("disconnect").observe("click", function(e) {
        e.stop();
        if (ws) {
            ws.close();
            ws = null;
        }
    });
});