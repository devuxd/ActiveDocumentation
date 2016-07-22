package core.model;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiTypeElementImpl;
import com.intellij.psi.impl.source.tree.java.PsiDeclarationStatementImpl;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 7/21/2016.
 */
public class ProjectClassBuilderEngine {

    public static JsonObject ct = new JsonObject();
    public static ProjectClassHierarchyBuilder projectClassHierarchyBuilder = new ProjectClassHierarchyBuilder(ct);

    public static void doStuff(PsiElement element) {

        if (element instanceof PsiExpression) {

            if (((PsiExpression) element).getType() != null) {
                System.out.println("1.5: " + ((PsiExpression) element).getType().getCanonicalText());
                System.out.println(element.getText());
                System.out.println(element.getParent().getText());
                tryToRunClass(((PsiExpression) element).getType().getCanonicalText());
                System.out.println("-----------------------");
            }
        }
        if (PsiUtil.getTypeByPsiElement(element) != null) {
            System.out.println("2: " + PsiUtil.getTypeByPsiElement(element).getCanonicalText());
            System.out.println(element.getText());
            System.out.println(element.getParent().getText());
            tryToRunClass(PsiUtil.getTypeByPsiElement(element).getCanonicalText());

            System.out.println("-----------------------");
        }
        if (element instanceof PsiClass) {
            System.out.println("4: " + ((PsiClass) element).getQualifiedName());
            tryToRunClass(((PsiClass) element));
            System.out.println("-----------------------");
        }

    }

    private static void tryToRunClass(PsiClass clazz){
        if(clazz == null){
            return;
        }
        projectClassHierarchyBuilder.processClass(clazz);
    }

    private static void tryToRunClass(String canText) {
        if (canText == null) {
            return;
        }
        Class clazz;
        try {
            clazz = Class.forName(canText);
            System.out.println("\tCanText: " + canText);
            System.out.println("\t" + Class.forName(canText));
        } catch (ClassNotFoundException e) {
            // e.printStackTrace();
            clazz = null;
        }

        if (clazz != null) {
            projectClassHierarchyBuilder.processClass(clazz);
        }
    }

    public static PsiClass getClass(Project p, String q){
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(p);
        return psiFacade.findClass(q, GlobalSearchScope.allScope(p));
    }

}
