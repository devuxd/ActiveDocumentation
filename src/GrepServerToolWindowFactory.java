import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.*;
import core.model.ProjectClassBuilderEngine;
import core.model.PsiClassHierarchyBuilder;
import core.model.PsiJavaVisitor;
import core.model.PsiPreCompEngine;
import org.java_websocket.WebSocketImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

public class GrepServerToolWindowFactory implements ToolWindowFactory {

    private JScrollPane pane;
    private JPanel panel;
    private JButton button;
    private ChatServer s;
    private static List<VirtualFile> ignoredFilesList = null;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        Component component = toolWindow.getComponent();

        ignoredFilesList = getIgnoredFilesList(project.getBaseDir());

        panel = new JPanel();
        panel.setBackground(Color.yellow);
        panel.add(new JLabel("Hello World."));
        button = new JButton("Say hi.");
        button.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // System.out.println(generateProjectHierarchyAsJSON().toString());
                    }
                }
        );
        panel.add(button);
        pane = new JScrollPane(panel);
        component.getParent().add(pane);

        System.out.println(project.getBaseDir());

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                try {
                    WebSocketImpl.DEBUG = false;
                    int port = 8887; // 843 flash policy port
                    s = new ChatServer(port, MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "INITIAL_PROJECT_HIERARCHY", generateProjectHierarchyAsJSON()}).toString());
                    s.start();
                    System.out.println();
                    System.out.println(ProjectClassBuilderEngine.ct);
                    // s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "INITIAL_PSI_CLASS_TABLE", ct}).toString());
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

    }

    // SHOULD NOT USE UNLESS YOU ARE BUILDING THE PSI CLASS HIERARCHY IF INTELLIJ ISSUED SOME NEW FEATURES / UPDATES


    public static JsonObject generateJavaASTAsJSON(PsiJavaFile psiJavaFile) {

        JsonObject root = new JsonObject();
        PsiJavaVisitor pjv = new PsiJavaVisitor();
        pjv.visit(psiJavaFile, root);
        System.out.println(PsiPreCompEngine.ct);
        return root;

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

        // json version of root
        JsonObject jsonRootDirectory = new JsonObject();
        JsonObject properties = new JsonObject();
        properties.addProperty("canonicalPath", rootDirectoryVirtualFile.getCanonicalPath());
        properties.addProperty("parent", "");
        properties.addProperty("name", rootDirectoryVirtualFile.getNameWithoutExtension());
        properties.addProperty("isDirectory", true);
        jsonRootDirectory.add("children", new JsonArray());
        jsonRootDirectory.add("properties", properties);

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
                // System.out.println(item.getName());
                if(shouldIgnoreFile(item)){
                    continue;
                }
                System.out.println("Included: " + item.getCanonicalPath());
                for (VirtualFile childOfItem : item.getChildren()) {
                    if(shouldIgnoreFile(childOfItem)){
                        continue;
                    }
                    new_q.add(childOfItem);
                    JsonObject jsonChildOfItem = new JsonObject();
                    JsonObject propertiesOfChild = new JsonObject();
                    if (childOfItem.isDirectory()) {
                        propertiesOfChild.addProperty("canonicalPath", childOfItem.getCanonicalPath());
                        propertiesOfChild.addProperty("parent", item.getCanonicalPath());
                        propertiesOfChild.addProperty("name", childOfItem.getNameWithoutExtension());
                        propertiesOfChild.addProperty("isDirectory", true);
                        jsonChildOfItem.add("children", new JsonArray());
                        jsonChildOfItem.add("properties", propertiesOfChild);
                    } else {
                        propertiesOfChild.addProperty("canonicalPath", childOfItem.getCanonicalPath());
                        propertiesOfChild.addProperty("parent", item.getCanonicalPath());
                        propertiesOfChild.addProperty("name", childOfItem.getNameWithoutExtension());
                        propertiesOfChild.addProperty("isDirectory", false);
                        propertiesOfChild.addProperty("fileType", childOfItem.getFileType().getName());
                        jsonChildOfItem.add("properties", propertiesOfChild);
                        PsiFile psiFile = PsiManager.getInstance(project).findFile(childOfItem);
                        propertiesOfChild.addProperty("code", psiFile.getText());
                        propertiesOfChild.add("ast", generateASTAsJSON(psiFile));
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

    public static boolean shouldIgnoreFile(VirtualFile s){
        if(ignoredFilesList == null){
            return false;
        }
        for(VirtualFile vfile : ignoredFilesList){
            if(vfile.getCanonicalPath().equals(s.getCanonicalPath())){
                return true;
            }else if(isFileAChildOf(s, vfile)){
                return true;
            }
        }

        return false;
    }

     public static boolean isFileAChildOf(VirtualFile maybeChild, VirtualFile possibleParent)
    {
        final VirtualFile parent = possibleParent.getCanonicalFile();
        if (!parent.exists() || !parent.isDirectory()) {
            // this cannot possibly be the parent
            return false;
        }

        VirtualFile child = maybeChild.getCanonicalFile();
        while (child != null) {
            if (child.equals(parent)) {
                return true;
            }
            child = child.getParent();
        }
        // No match found, and we've hit the root directory
        return false;
    }


    public static List<VirtualFile> getIgnoredFilesList(VirtualFile bDir){

        List<String> list = new ArrayList<>();
        list.add(".idea");
        list.add("out");

        List<VirtualFile> set = new ArrayList<>();

        for(String item : list){
            VirtualFile vfile = bDir.findFileByRelativePath(item);
            if(vfile != null) {
                set.add(vfile);
            }
        }

        return set;
    }

}