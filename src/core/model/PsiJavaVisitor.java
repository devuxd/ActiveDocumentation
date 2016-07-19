// https://github.com/cmf/psiviewer/blob/master/src/idea/plugin/psiviewer/util/IntrospectionUtil.java

package core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.psi.*;
import java.util.HashMap;

import java.beans.PropertyDescriptor;
import java.util.HashSet;


public class PsiJavaVisitor implements TreeVisitor {

    HashMap<PsiElement, JsonObject> map = new HashMap<>();
    private final HashSet<Class> acceptableClasses = generateAcceptableClasses();
    private final HashSet<String> unacceptableVars = generateUnacceptableVars();

    @Override
    public void visit(PsiElement psiElement, JsonObject jsonNode){

        if(psiElement == null){
            return;
        }

        if(isIgnorePsiElement(psiElement)){
            return;
        }

        // this was just to build the initial class hierarchy
        /*if(true){
            PsiPreCompEngine.doStuff(psiElement);
        }*/

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

        // properties that all elements will have
        properties.addProperty("type", element.getClass().getSimpleName());

        // customized properties that elements will have
        PropertyDescriptor[] propertyDescriptors = IntrospectionUtil.getProperties(element.getClass());
        // System.out.println(element.getClass());
        for(PropertyDescriptor pd : propertyDescriptors){
            if(isClassAcceptable(pd.getPropertyType()) && isVariableAcceptable(pd.getName())) {

                Object val = IntrospectionUtil.getValue(element, pd);
                if(val instanceof String){
                    properties.addProperty(pd.getName(), (String) val);
                }else if(val instanceof Integer){
                    properties.addProperty(pd.getName(), (Integer) val);
                }else if(val instanceof Double){
                    properties.addProperty(pd.getName(), (Double) val);
                }else if(val instanceof Byte){
                    properties.addProperty(pd.getName(), (Byte) val);
                }else if(val instanceof Boolean){
                    properties.addProperty(pd.getName(), (Boolean) val);
                }else if(val instanceof Character){
                    properties.addProperty(pd.getName(), (Character) val);
                }else if(val instanceof Short){
                    properties.addProperty(pd.getName(), (Short) val);
                }else if(val instanceof Long){
                    properties.addProperty(pd.getName(), (Long) val);
                }else if(val instanceof Float){
                    properties.addProperty(pd.getName(), (Float) val);
                }else if(val == null){

                }else{
                    System.out.println("An unhandled primitive?! " + val.getClass());
                }
            }
        }

        return properties;

    }

    public boolean isVariableAcceptable(String s){
        return !unacceptableVars.contains(s);
    }

    public boolean isClassAcceptable(Class clazz){
        // return true;
        return acceptableClasses.contains(clazz);
    }

    private HashSet<Class> generateAcceptableClasses(){
        HashSet<Class> output = new HashSet<>();
        output.add(Boolean.class);
        output.add(Character.class);
        output.add(Byte.class);
        output.add(Short.class);
        output.add(Integer.class);
        output.add(Long.class);
        output.add(Float.class);
        output.add(Double.class);
        output.add(String.class);

        output.add(boolean.class);
        output.add(char.class);
        output.add(byte.class);
        output.add(short.class);
        output.add(int.class);
        output.add(long.class);
        output.add(float.class);
        output.add(double.class);

        return output;
    }

    public HashSet<String> generateUnacceptableVars(){
        HashSet<String> output = new HashSet<>();

        output.add("userDataString");

        return output;
    }

}