package core.model;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;


/**
 * Created by User on 7/21/2016.
 */
public class ProjectClassBuilderEngine {

    private JsonObject ct = new JsonObject();
    private ProjectClassHierarchyBuilder projectClassHierarchyBuilder;

    public ProjectClassBuilderEngine(JsonObject classTable){
        ct = classTable;
        projectClassHierarchyBuilder = new ProjectClassHierarchyBuilder(ct);
    }

    public JsonObject getClassTable() {
        return ct;
    }

    public static String getTypeOfImportantElements(PsiElement element){
        if (element instanceof PsiExpression) {
            PsiType pt = ((PsiExpression) element).getType();
            if (pt != null) {
                return pt.getCanonicalText();
            }
        }
        PsiType pt2 = PsiUtil.getTypeByPsiElement(element);
        if (pt2 != null) {
            return pt2.getCanonicalText();
        }
        if (element instanceof PsiClass) {
            return ((PsiClass) element).getQualifiedName();
        }
        return null;
    }

    public void doStuff(PsiElement element) {

        if (element instanceof PsiExpression) {
            PsiType pt = ((PsiExpression) element).getType();
            if (pt != null) {
                tryToRunClass(pt.getCanonicalText());
            }
        }
        PsiType pt2 = PsiUtil.getTypeByPsiElement(element);
        if (pt2 != null) {
            tryToRunClass(pt2.getCanonicalText());
        }
        if (element instanceof PsiClass) {
            tryToRunClass(((PsiClass) element));
        }

    }

    private void tryToRunClass(PsiClass clazz) {
        if (clazz == null) {
            return;
        }
        projectClassHierarchyBuilder.processClass(clazz);
    }

    private void tryToRunClass(String canText) {
        if (canText == null) {
            return;
        }
        Class clazz;
        try {
            clazz = Class.forName(canText);
            // System.out.println("\tCanText: " + canText);
            // System.out.println("\t" + Class.forName(canText));
        } catch (ClassNotFoundException e) {
            clazz = null;
        }

        if (clazz != null) {
            projectClassHierarchyBuilder.processClass(clazz);
        }
    }

    public PsiClass getClass(Project p, String q) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(p);
        return psiFacade.findClass(q, GlobalSearchScope.allScope(p));
    }

}
