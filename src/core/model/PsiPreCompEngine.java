package core.model;

import com.google.gson.JsonObject;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiTypeElementImpl;
import com.intellij.psi.impl.source.tree.java.PsiDeclarationStatementImpl;

/**
 * Created by User on 7/19/2016.
 */
public class PsiPreCompEngine {

    public static JsonObject ct = new JsonObject();

    public static void doStuff(PsiElement element){
        if(element.getContainingFile().getName().contains("TWF")){
            if(element instanceof PsiDeclarationStatementImpl){
                PsiDeclarationStatementImpl pdsi = (PsiDeclarationStatementImpl) element;
                System.out.println(pdsi.getElementType().toString());
                Object x = pdsi.getFirstChild().getChildren()[1];
                if(x instanceof PsiTypeElementImpl){
                    PsiTypeElementImpl y = (PsiTypeElementImpl) x;
                    System.out.println("\t"+y.getText());
                    System.out.println("\t"+y.getType().toString());
                    String canText = y.getType().getCanonicalText();

                    PsiClassHierarchyBuilder psiClassHierarchyBuilder = new PsiClassHierarchyBuilder(ct);
                    Class clazz;
                    try{
                        clazz = Class.forName(canText);
                        System.out.println("\tCanText: " + canText);
                        System.out.println("\t"+Class.forName(canText));
                    } catch (ClassNotFoundException e) {
                        clazz = null;
                    }

                    if(clazz != null){
                        psiClassHierarchyBuilder.processClass(clazz);
                    }

                }
            }
        }
    }
}
