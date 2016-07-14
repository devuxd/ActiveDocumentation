package core.model;

import com.google.gson.JsonObject;
import com.intellij.psi.PsiElement;

public interface TreeVisitor {
    void visit(PsiElement psiElement, JsonObject node);
}
