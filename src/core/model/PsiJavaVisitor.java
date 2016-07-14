package core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.containers.HashMap;

public class PsiJavaVisitor implements TreeVisitor {

    HashMap<PsiElement, JsonObject> map = new HashMap<>();

    /*@Override
    public void visit(PsiElement psiElement, JsonObject parent) {

        if(parent == null){
            return;
        }

        if(isIgnorePsiElement(psiElement)){
            return;
        }

        JsonObject node = new JsonObject();
        node.add("children", new JsonArray());
        parent.get("children").getAsJsonArray().add(node);

        // extract properties of psiElement and put them into jsonNode
        node.add("properties", extractData(psiElement));

        // now add children
        for(PsiElement child : psiElement.getChildren()){
            visit(child, node);
        }

    }*/

    @Override
    public void visit(PsiElement psiElement, JsonObject jsonNode){

        if(psiElement == null){
            return;
        }

        if(isIgnorePsiElement(psiElement)){
            return;
        }

        // take the new json object and give it an empty children array
        jsonNode.add("children", new JsonArray());
        jsonNode.add("properties", extractData(psiElement));
        map.put(psiElement, jsonNode);

        // get the parent and add node to the parent's children
        PsiElement parent = psiElement.getParent();
        if(parent != null && map.containsKey(parent)) {
            map.get(parent).get("children").getAsJsonArray().add(jsonNode);
        }

        for(PsiElement child : psiElement.getChildren()){
            visit(child, new JsonObject());
        }

    }

    public boolean isIgnorePsiElement(PsiElement element){
        if(element instanceof PsiWhiteSpace){
            return true;
        }
        return false;
    }

    public JsonObject extractData(PsiElement element){

        JsonObject properties = new JsonObject();

        if(element instanceof PsiElement){
            properties.addProperty("type", element.getClass().getSimpleName());
            properties.addProperty("text", element.getText());
            properties.addProperty("toString", element.toString());
            properties.addProperty("getStartOffsetInParent", element.getStartOffsetInParent());
        }else{
            properties.addProperty("type", element.getClass().getSimpleName());
            properties.addProperty("notes", "This is not a PSI element!");
        }

        return properties;
    }

}
