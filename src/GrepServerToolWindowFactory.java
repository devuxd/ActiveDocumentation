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
    private static JsonObject initialClassTable = new JsonObject();


    // This function creates the GUI for the plugin.
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        Component component = toolWindow.getComponent();

        ignoredFilesList = getIgnoredFilesList(project.getBaseDir());

        // This are just some random GUI elements here. Eventually, there should be an HTML viewer here where the Active Documentation web client is added in
        panel = new JPanel();
        panel.setBackground(Color.yellow);
        panel.add(new JLabel("Hello World."));
        button = new JButton("Say hi.");
        button.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    }
                }
        );
        panel.add(button);
        pane = new JScrollPane(panel);
        component.getParent().add(pane);

        System.out.println(project.getBaseDir());

        // Use ApplicationManager.getApplication().runReadAction() to run things on a new thread with IntelliJ. Don't use the Java Thread library.
        // Here, the ChatServer is initialized and continues to run once the plugin has been opened.
        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                try {
                    WebSocketImpl.DEBUG = false;
                    int port = 8887; // 843 flash policy port
                    s = new ChatServer(port, MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "INITIAL_PROJECT_HIERARCHY", generateProjectHierarchyAsJSON()}).toString()); // generateProjectHierarchyAsJSON() populates initialClassTable as well
                    s.start();
                    System.out.println(initialClassTable);
                    s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "INITIAL_PROJECT_CLASS_TABLE", initialClassTable}).toString());
                    s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "UPDATE_RULE_TABLE_AND_CONTAINER", sendRulesInitially()}).toString());
                    System.out.println("ChatServer started on port: " + s.getPort());
                } catch (Exception e) {
                    System.out.println("Error occured while running the Chat Server: ");
                    e.printStackTrace();
                    try {
                        s.stop();
                    } catch (Exception e2) {
                        System.out.println("Error occured when stopping the chat server:");
                        e2.printStackTrace();
                    }
                }
            }
        });

        // This will allow file changes to be sent to the web client
        FileChangeManager fcm = new FileChangeManager(s);
        fcm.initComponent();

    }

    // Processes PsiJavaFiles
    public static JsonObject generateJavaASTAsJSON(PsiJavaFile psiJavaFile) {

        JsonObject root = new JsonObject();
        PsiJavaVisitor pjv = new PsiJavaVisitor(initialClassTable);
        pjv.visit(psiJavaFile, root);

        /*
        * This is really important to the functioning of the Web Client.
        * Since the client needs to know the relationship between all the
        * Psi- classes in order to run queries like "find all PsiExpressions"
        * (PsiExpression is a superclass of PsiArrayAccessExpression,
        * PsiArrayInitializerExpression, PsiAssignmentExpression,
        * PsiCallExpression, etc.). There are a lot of classes here to handle.
        * There is a seperate project to use in order to generate a table of
        * these relationships. That table is then put into precomputedclasstable.js
        * */

        if (PsiPreCompEngine.recomputePsiClassTable) { // this is a boolean that should be set to true only if trying to recompute the precomputed class table for psi elements and if working with a special project which is provided as a .zip file on the Github page.
            System.out.println("FINDME");
            System.out.println(PsiPreCompEngine.ct); // the class table
        }
        return root;

    }

    // retrives a JSON AST given a PsiFile
    public static JsonObject generateASTAsJSON(PsiFile psiFile) {

        if (psiFile instanceof PsiJavaFile) {
            return generateJavaASTAsJSON((PsiJavaFile) psiFile);
        } else {
            return new JsonObject(); // this should be a generic AST that is provided when we don't handle the specific file type
        }

    }

    // traverses the project hierarchy (the directories of the project that the user is working on)
        // when it hits a file that is not a directory, the getASTAsJSON function is called on that file
        // returns a JSON object that has the entire project hierarchy with individual files that have a property called 'ast'
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
                if (shouldIgnoreFile(item)) {
                    continue;
                }
                System.out.println("Included: " + item.getCanonicalPath());
                for (VirtualFile childOfItem : item.getChildren()) {
                    if (shouldIgnoreFile(childOfItem)) {
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
                        propertiesOfChild.addProperty("text", psiFile.getText());
                        propertiesOfChild.add("ast", generateASTAsJSON(psiFile));
                        propertiesOfChild.addProperty("fileName", psiFile.getName());
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

    // returns a JSONObject with the initial rules from ruleJson.txt (the file where users modify rules)
    public static JsonObject sendRulesInitially() {
        System.out.println("Send Rules initially");
        JsonObject data = new JsonObject();

        // start off with root
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        VirtualFile rootDirectoryVirtualFile = project.getBaseDir();

        // set up queue
        java.util.List<VirtualFile> q = new ArrayList<VirtualFile>();
        q.add(rootDirectoryVirtualFile);

        // traverse the queue
        while (!q.isEmpty()) {
            java.util.List<VirtualFile> new_q = new ArrayList<VirtualFile>();
            for (VirtualFile item : q) {
                System.out.println("Included: " + item.getCanonicalPath());
                for (VirtualFile childOfItem : item.getChildren()) {
                    new_q.add(childOfItem);
                    if(childOfItem.getCanonicalPath().endsWith("ruleJson.txt")) {
                        PsiFile psiFile = PsiManager.getInstance(project).findFile(childOfItem);
                        data.addProperty("text", psiFile.getText());
                        return data;
                    }
                }
            }
            q = new_q;
        }
        if(!data.has("text")){
            data.addProperty("text", "");
        }
        return data;
    }

    // checks if we should ignore a file
    public static boolean shouldIgnoreFile(VirtualFile s) {
        if (ignoredFilesList == null) {
            return false;
        }
        for (VirtualFile vfile : ignoredFilesList) {
            if (vfile.getCanonicalPath().equals(s.getCanonicalPath())) {
                return true;
            } else if (isFileAChildOf(s, vfile)) {
                return true;
            }
        }

        return false;
    }

    // determines if one file/directory is stored somewhere down the line in another directory
    public static boolean isFileAChildOf(VirtualFile maybeChild, VirtualFile possibleParent) {
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

    // generates the list of files/folders to ignore
    public static List<VirtualFile> getIgnoredFilesList(VirtualFile bDir) {

        List<String> list = new ArrayList<>();
        list.add(".idea");
        list.add("out");
        list.add("website-client");

        List<VirtualFile> set = new ArrayList<>();

        for (String item : list) {
            VirtualFile vfile = bDir.findFileByRelativePath(item);
            if (vfile != null) {
                set.add(vfile);
            }
        }

        return set;
    }

}