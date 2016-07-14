import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import core.model.PsiJavaVisitor;
import org.java_websocket.WebSocketImpl;
import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * Created by User on 7/8/2016.
 */
public class GrepServerToolWindowFactory implements ToolWindowFactory {

    private JScrollPane pane;
    private JPanel panel;
    private JButton button;
    private ChatServer s;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        Component component = toolWindow.getComponent();
        panel = new JPanel();
        panel.setBackground(Color.yellow);
        panel.add(new JLabel("Hello World."));
        button = new JButton("Say hi.");
        button.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println(generateProjectHierarchyAsJSON().toString());
                    }
                }
        );
        panel.add(button);
        pane = new JScrollPane(panel);
        component.getParent().add(pane);

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                try {
                    WebSocketImpl.DEBUG = false;
                    int port = 8887; // 843 flash policy port
                    s = new ChatServer(port, MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "INITIAL_PROJECT_HIERARCHY", generateProjectHierarchyAsJSON()}).toString());
                    s.start();
                    System.out.println("ChatServer started on port: " + s.getPort());
                } catch (Exception e) {
                    try {
                        s.stop();
                    } catch (Exception e2) {
                    }
                }
            }
        });

        FileChangeManager fcm = new FileChangeManager(s);
        fcm.initComponent();

        // System.out.println(generateASTAsJSON(PsiDocumentManager.getInstance(project).getPsiFile(FileEditorManager.getInstance(project).getSelectedTextEditor().getDocument())).toString());

    }

    public static JsonObject generateJavaASTAsJSON(PsiJavaFile psiJavaFile) {

        JsonObject root = new JsonObject();
        new PsiJavaVisitor().visit(psiJavaFile, root);
        return root;

    }

    public static JsonObject generateDefaultASTAsJSON(PsiFile psiFile) {

        JsonObject file = new JsonObject();

        HashMap<PsiElement, JsonObject> psiElemToJsonMap = new HashMap<>();
        psiElemToJsonMap.put(psiFile, file);

        ArrayList<PsiElement> q = new ArrayList<>();
        q.add(psiFile);

        while (!q.isEmpty()) {

            ArrayList<PsiElement> new_q = new ArrayList<>();

            for (PsiElement el : q) {
                System.out.println(el.getClass());
                boolean shouldAddChildrenOfEl = true;
                if (el instanceof PsiFile) {

                    file.addProperty("canonicalPath", psiFile.getVirtualFile().getCanonicalPath());
                    file.addProperty("type", psiFile.getVirtualFile().getFileType().getName());
                    file.addProperty("name", psiFile.getVirtualFile().getNameWithoutExtension());
                    file.add("children", new JsonArray());

                } else {

                    JsonObject obj = new JsonObject();
                    obj.addProperty("type", "defaultUnimportantContainer");
                    obj.addProperty("intellijType", el.getClass().getName());
                    obj.add("children", new JsonArray());
                    psiElemToJsonMap.get(el.getParent()).get("children").getAsJsonArray().add(obj);
                    psiElemToJsonMap.put(el, obj);

                }
                shouldAddChildrenOfEl = true;
                if (shouldAddChildrenOfEl) {
                    for (PsiElement elCh : el.getChildren()) {
                        new_q.add(elCh);
                    }
                }
            }

            q = new_q;

        }

        return file;
    }

    public static JsonObject generateASTAsJSON(PsiFile psiFile) {

        if (psiFile instanceof PsiJavaFile) {
            return generateJavaASTAsJSON((PsiJavaFile) psiFile);
        } else {
            return new JsonObject(); // this should be a generic version based on intellij stuff
        }

    }

    public static JsonObject generateProjectHierarchyAsJSON() {

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
        com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl okfjsdf = null;


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
                        jsonChildOfItem.add("ast", generateASTAsJSON(psiFile));
                    }
                    canonicalToJsonMap.get(item.getCanonicalPath()).get("children").getAsJsonArray().add(jsonChildOfItem);
                    canonicalToJsonMap.put(childOfItem.getCanonicalPath(), jsonChildOfItem);
                }
            }
            q = new_q;
        }

        System.out.println(jsonRootDirectory);
        return jsonRootDirectory;

    }
}