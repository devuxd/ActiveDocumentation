import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.vcs.log.Hash;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

/**
 * Created by User on 7/6/2016.
 */
public class ServerToolWindowFactory implements ToolWindowFactory {

    private JScrollPane pane;
    private JPanel panel;
    private JButton button;


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        Component component = toolWindow.getComponent();
        panel = new JPanel();
        panel.setBackground(Color.yellow);
        panel.add(new JLabel("Hello World."));
        button = new JButton();
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("I pressed a button.");
            }
        });
        panel.add(button);
        pane = new JScrollPane(panel);
        component.getParent().add(pane);

        System.out.println("Project Hierarchy As JSON:");
        System.out.println(generateProjectHierarchyAsJSON());

        HashMap<String, PsiFileSystemItem> map = getCanonicalToPsiMap();
        for(String k : map.keySet()){
            getASTAsJson(map.get(k));
        }
        // getASTAsJson();

    }

    public static String getASTAsJson(PsiFileSystemItem psiFileSystemItem) {

        if (psiFileSystemItem.isDirectory()) {
            System.out.println(psiFileSystemItem.getName() + " is a directory.");
            return "directory";
        }

        // set up the queue
        PsiFile psiFile = (PsiFile) psiFileSystemItem;
        List<PsiElement> q = new ArrayList<PsiElement>();
        q.addAll(Arrays.asList(psiFile.getChildren()));
        String fileType = psiFile.getFileType().getName();

        // here we will build the AST based on the filetype
        // and whatever information we feel is appropriate to
        // carry over to our JS representation

        if(psiFile instanceof PsiJavaFile){
            System.out.println("The file is an instance of JAVA.");
            System.out.println(getJavaAST((PsiJavaFile) psiFile));
        }else{
            getDefaultAst(psiFile);
            System.out.println("The file is not a JAVA one. " + psiFile.getName() + " is a " + fileType + " file.");
        }


        return null;
    }

    public static void getDefaultAst(PsiFile psiFile){

    }

    public static boolean isValid(PsiElement psiElement){

        if(psiElement instanceof PsiClass){
            return true;
        }

        if(psiElement instanceof PsiMethod){
            return true;
        }

        if(psiElement instanceof PsiField){
            return true;
        }

        if(psiElement instanceof PsiExpressionStatement){
            return true;
        }

        if(psiElement instanceof PsiDeclarationStatement){
            return true;
        }

        if(psiElement instanceof PsiImportList){
            return true;
        }

        return false;
    }

    public static String getJavaAST(PsiJavaFile psiJavaFile){

        // set up the root
        JsonObject jsonRoot = new JsonObject();
        VirtualFile virtualFile = psiJavaFile.getVirtualFile();
        jsonRoot.addProperty("name", virtualFile.getNameWithoutExtension());
        jsonRoot.addProperty("fileType", virtualFile.getFileType().getName());
        jsonRoot.addProperty("type", "file");

        /*
        * if(childOfItem instanceof PsiImportList){

                        // ASSUMPTION: since we have import statements here, we must be dealing with the root
                        PsiImportList childOfItemAsImportList = (PsiImportList) childOfItem;
                        JsonArray importListAsJson = new JsonArray();
                        for(PsiImportStatement psiImportStatement : childOfItemAsImportList.getImportStatements()){
                            importListAsJson.add(psiImportStatement.getImportReference().getReferenceName());
                        }
                        jsonRoot.add("importList", importListAsJson);

                    }else if(childOfItem instanceof PsiClass){



                    }else{

                    }
        * */

        // set up the queue
        // we will account for the following types (from https://github.com/joewalnes/idea-community/tree/master/java/java-impl/src/com/intellij/psi/impl/source/tree/java)
            // PsiClass
                // PsiMethod
                    // PsiModifierList
                        // PsiAnnotation
                    // PsiTypeElement
                    // PsiIdentifier
                    // PsiParameterList
                        // PsiParameter
                // PsiField
                // PsiCodeBlock ~
                    // PsiExpressionStatement
                    // PsiDeclarationStatement
                    // PsiForeachStatement
                        // PsiBlockStatement
                            // PsiCodeBlock ~
            // PsiImportList

        Map<PsiElement, JsonObject> psiElementToJsonObjectHashMap = new HashMap<PsiElement, JsonObject>();
        psiElementToJsonObjectHashMap.put(psiJavaFile, jsonRoot);

        List<PsiElement> q = new ArrayList<PsiElement>();
        q.addAll(Arrays.asList(psiJavaFile.getChildren()));

        JsonArray childrenOfRootAsJson = new JsonArray();
        for(PsiElement item : q){
            System.out.println("isValid? " + isValid(item) + " | " + item.getClass().getName() + " | " + item.getText());
            JsonObject itemAsJson = new JsonObject();
            itemAsJson.addProperty("type", item.getClass().getName());
            itemAsJson.addProperty("text", item.getText());
            childrenOfRootAsJson.add(itemAsJson);
            psiElementToJsonObjectHashMap.put(item, itemAsJson);
        }
        jsonRoot.add("children", childrenOfRootAsJson);
        // isValidType(PsiElement)
        // extractProperties() which uses isValidType, returns a hashmpp, prop name => value
        //
        while(!q.isEmpty()){
            List<PsiElement> new_q = new ArrayList<PsiElement>();
            for(PsiElement item : q){

                System.out.println("isValid? " + isValid(item) + " | " + item.getClass().getName() + " | " + item.getText());

                System.out.println("--");
                System.out.println(item.getText());
                System.out.println(item.getChildren());
                System.out.println("--");

                JsonArray childrenOfItemAsJson = new JsonArray();
                for(PsiElement childOfItem: item.getChildren()){
                    JsonObject childOfItemAsJson = new JsonObject();
                    childOfItemAsJson.addProperty("type", childOfItem.getClass().getName());
                    childOfItemAsJson.addProperty("text", childOfItem.getText());
                    childrenOfItemAsJson.add(childOfItemAsJson);
                    psiElementToJsonObjectHashMap.put(childOfItem, childOfItemAsJson);
                    new_q.add(childOfItem);
                }
                psiElementToJsonObjectHashMap.get(item).add("children", childrenOfItemAsJson);
            }
            q = new_q;
        }

        return jsonRoot.toString();

    }

    public static void testRandomStuff(Project project) {
        System.out.println("Base Path");
        System.out.println(project.getBasePath());
        System.out.println("Batch Load");
        applicationBatchLoad();

        System.out.println(getProjectPsiFilesOfType(project, "java"));
    }

    public static Vector<PsiFile> getProjectPsiFilesOfType(Project project, String ext) {

        Vector<PsiFile> files = new Vector<PsiFile>();
        Vector<PsiFile> isInProjectVector = new Vector<PsiFile>();


        for (VirtualFile virtualFile : FilenameIndex.getAllFilesByExt(project, ext)) {
            ApplicationManager.getApplication().runReadAction(
                    new Runnable() {
                        @Override
                        public void run() {
                            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

                            files.add(psiFile);
                            if (PsiManager.getInstance(project).isInProject(psiFile)) {
                                isInProjectVector.add(psiFile);
                            }
                        }
                    }
            );
        }
        System.out.println("Is In Project Vector");
        System.out.println(isInProjectVector.toString());

        System.out.println("Get all Java files in the project a little more quickly?");
        System.out.println(FilenameIndex.getAllFilesByExt(project, "java", GlobalSearchScope.projectScope(project))); // limit the scope the hierarchy

        System.out.println("Get Project Psi Files Of Type Java");
        return files;

    }

    public static HashMap<String, PsiFileSystemItem> getCanonicalToPsiMap() {

        // while we do the traversal, we should keep track of what PsiFiles we have for AST
        // we can map the canonical name to an PsiFile. the PsiFiles can then be processed seperately
        // and called easily by the javascript client
        HashMap<String, PsiFileSystemItem> canonicalToPsiMap = new HashMap<String, PsiFileSystemItem>();

        // start off with root
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        VirtualFile rootDirectoryVirtualFile = project.getBaseDir();
        PsiDirectory psiRootDirectory = PsiManager.getInstance(project).findDirectory(rootDirectoryVirtualFile);
        canonicalToPsiMap.put(rootDirectoryVirtualFile.getCanonicalPath(), psiRootDirectory);

        // set up queue
        List<VirtualFile> q = new ArrayList<VirtualFile>();
        q.add(rootDirectoryVirtualFile);

        // traverse the queue
        while (!q.isEmpty()) {
            List<VirtualFile> new_q = new ArrayList<VirtualFile>();
            for (VirtualFile item : q) {
                for (VirtualFile childOfItem : item.getChildren()) {
                    new_q.add(childOfItem);
                    if (childOfItem.isDirectory()) {
                        canonicalToPsiMap.put(childOfItem.getCanonicalPath(), PsiManager.getInstance(project).findDirectory(childOfItem));
                    } else {
                        canonicalToPsiMap.put(childOfItem.getCanonicalPath(), PsiManager.getInstance(project).findFile(childOfItem));
                    }
                }
            }
            q = new_q;
        }

        return canonicalToPsiMap;

    }

    public static String generateProjectHierarchyAsJSON() {

        // start off with root
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        VirtualFile rootDirectoryVirtualFile = project.getBaseDir();
        PsiDirectory psiRootDirectory = PsiManager.getInstance(project).findDirectory(rootDirectoryVirtualFile);

        // json version of root
        JsonObject jsonRootDirectory = new JsonObject();
        jsonRootDirectory.addProperty("canonicalPath", rootDirectoryVirtualFile.getCanonicalPath());
        jsonRootDirectory.addProperty("parent", "");
        jsonRootDirectory.addProperty("name", rootDirectoryVirtualFile.getNameWithoutExtension());
        jsonRootDirectory.addProperty("isDirectory", true);
        jsonRootDirectory.add("children", new JsonArray());

        // set up a hashmap for the traversal
        HashMap<String, JsonObject> canonicalToJsonMap = new HashMap<String, JsonObject>();
        canonicalToJsonMap.put(rootDirectoryVirtualFile.getCanonicalPath(), jsonRootDirectory);

        // set up queue
        java.util.List<VirtualFile> q = new ArrayList<VirtualFile>();
        q.add(rootDirectoryVirtualFile);

        // traverse the queue
        while (!q.isEmpty()) {
            java.util.List<VirtualFile> new_q = new ArrayList<VirtualFile>();
            for (VirtualFile item : q) {
                for (VirtualFile childOfItem : item.getChildren()) {
                    new_q.add(childOfItem);
                    JsonObject jsonChildOfItem = new JsonObject();
                    if (childOfItem.isDirectory()) {
                        jsonChildOfItem.addProperty("canonicalPath", childOfItem.getCanonicalPath());
                        jsonChildOfItem.addProperty("parent", item.getCanonicalPath());
                        jsonChildOfItem.addProperty("name", childOfItem.getNameWithoutExtension());
                        jsonChildOfItem.addProperty("isDirectory", true);
                        jsonChildOfItem.add("children", new JsonArray());
                    } else {
                        jsonChildOfItem.addProperty("canonicalPath", childOfItem.getCanonicalPath());
                        jsonChildOfItem.addProperty("parent", item.getCanonicalPath());
                        jsonChildOfItem.addProperty("name", childOfItem.getNameWithoutExtension());
                        jsonChildOfItem.addProperty("isDirectory", false);
                        jsonChildOfItem.addProperty("fileType", childOfItem.getFileType().getName());
                        PsiFile psiFile = PsiManager.getInstance(project).findFile(childOfItem);
                        jsonChildOfItem.addProperty("code", psiFile.getText());
                    }
                    canonicalToJsonMap.get(item.getCanonicalPath()).get("children").getAsJsonArray().add(jsonChildOfItem);
                    canonicalToJsonMap.put(childOfItem.getCanonicalPath(), jsonChildOfItem);
                }
            }
            q = new_q;
        }

        return jsonRootDirectory.toString();

    }


    public static void applicationBatchLoad() {

        File rootFolderFile = new File(ProjectManager.getInstance().getOpenProjects()[0].getBasePath());
        VirtualFile rootDirectory = ProjectManager.getInstance().getOpenProjects()[0].getBaseDir();

        File[] appFolders = null;
        if (rootFolderFile.isDirectory()) {
            appFolders = rootFolderFile.listFiles(
                    new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            if (pathname.isDirectory()) {
                                return true;
                            } else {
                                return false;
                            }
                        }
                    }
            );
        }
        System.out.println(Arrays.toString(appFolders));
    }
}
