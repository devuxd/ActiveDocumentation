package core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;

public class PsiClassHierarchyBuilder implements TreeVisitor {

    private JsonObject classTable;

    public PsiClassHierarchyBuilder(JsonObject cTable){
        classTable = cTable;
    }

    @Override
    public void visit(PsiElement psiElement, JsonObject jsonNode) {

        if(psiElement == null){
            return;
        }

        if(isIgnorePsiElement(psiElement)){
            return;
        }

        processClass(psiElement.getClass());

        for(PsiElement child : psiElement.getChildren()){
            visit(child, new JsonObject());
        }
    }

    public void processClass(Class clazz){

        if(classTable.has(clazz.getName())){
            return;
        }

        JsonObject val = new JsonObject();
        val.addProperty("name", clazz.getName());
        val.addProperty("simpleName", clazz.getSimpleName());
        classTable.add(clazz.getName(), val);

        if(clazz.isInterface()){
            val.addProperty("type", "interface");
            JsonArray imp = new JsonArray();
            for(Class i : clazz.getInterfaces()){
                imp.add(i.getName());
                processClass(i);
            }
            val.add("extends", imp);
        }else{
            val.addProperty("type", "class");
            if(clazz.getSuperclass() != null) {
                val.addProperty("extends", clazz.getSuperclass().getName());
                processClass(clazz.getSuperclass());
            }
            JsonArray imp = new JsonArray();
            for(Class i : clazz.getInterfaces()){
                imp.add(i.getName());
                processClass(i);
            }
            val.add("implements", imp);
        }

    }

    public boolean isIgnorePsiElement(PsiElement element){
        if(element instanceof PsiWhiteSpace){
            return true;
        }
        return false;
    }
}
