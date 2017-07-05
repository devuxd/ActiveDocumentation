
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import core.model.SRCMLHandler;

import core.model.SRCMLxml;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;


public class FileChangeManager implements ProjectComponent {

    private final MessageBusConnection connection;
    private ChatServer s;
    private List<VirtualFile> ignoredFiles = new ArrayList<>();
    private SRCMLxml srcml;
    private String rules;

    public FileChangeManager(ChatServer server, SRCMLxml xmlP, String rule) {
        connection = ApplicationManager.getApplication().getMessageBus().connect();
        s = server;

        rules = rule;
        srcml = xmlP;
    }

    public SRCMLxml getSrcml() {
        return srcml;
    }

    public String getRules() {
        return rules;
    }

    public void setSrcml(SRCMLxml srcml) {
        this.srcml = srcml;
        s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "XML", srcml.xml}).toString());
        s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "UPDATE_RULE_TABLE_AND_CONTAINER", this.getRules()}).toString());
        s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "VERIFY_RULES", ""}).toString());
    }

    public void setRules(String rules) {
        this.rules = rules;
        s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "UPDATE_RULE_TABLE_AND_CONTAINER", rules}).toString());
        s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "VERIFY_RULES", ""}).toString());
    }

    public void initComponent() {
        // What is the difference of opening the socket here vs. when toolWindow is created?
        s.start();

        System.out.println("ChatServer started on port: " + s.getPort());

        createIgnoredFileList();

        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener.Adapter() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {

                // TODO handle events
                // update rules in ChatServer

                for (VFileEvent event : events) {
                    if (shouldIgnoreFile(event.getFile()))
                        continue;

                    if (event instanceof VFileCreateEvent) { // Create files
                        handleVFileCreateEvent(event);

                    } else if (event instanceof VFileContentChangeEvent) { // Text Change
                        handleVFileChangeEvent(event);

                    } else if (event instanceof VFileDeleteEvent) { // Delete files
                        handleVFileDeleteEvent(event);

                    } else if (event instanceof VFilePropertyChangeEvent) { // Property Change
                        handleVFilePropertyChangeEvent(event);
                    }
                }
            }
        });
    }

    public void disposeComponent() {
        connection.disconnect();
        try {
            s.stop();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public String getComponentName() {
        System.out.println("(Component Name)");
        return "";
    }

    // ProjectComponent

    @Override
    public void projectOpened() {
        System.out.println("(project Opened)");
    }

    @Override
    public void projectClosed() {
        System.out.println("(project Closed)");
    }


    public void processReceivedMessages(JsonObject messageAsJson) {

        String command = messageAsJson.get("command").getAsString();
        String projectPath = ProjectManager.getInstance().getOpenProjects()[0].getBasePath();

        switch (command) {
            case "xmlResult":
                try {
                    PrintWriter writer = new PrintWriter(projectPath + "/tempResultXmlFile.xml", "UTF-8");
                    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
                    writer.println(messageAsJson.get("data").getAsJsonObject().get("xml").getAsString());
                    writer.close();
                } catch (IOException e) {
                    System.out.println("error in writing the result xml");
                    return;
                }

                //System.out.println(SRCMLHandler.findLineNumber(projectPath + "/tempResultXmlFile.xml"));

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String[] filePath = messageAsJson.get("data").getAsJsonObject().get("fileName").getAsString().split("/");
                        String fileToFocusOn = filePath[filePath.length - 1];
                        int indexToFocusOn = SRCMLHandler.findLineNumber(projectPath + "/tempResultXmlFile.xml");
                        Project currentProject = ProjectManager.getInstance().getOpenProjects()[0];
                        VirtualFile theVFile = FilenameIndex.getVirtualFilesByName(currentProject, fileToFocusOn, GlobalSearchScope.projectScope(currentProject)).iterator().next();
                        FileEditorManager.getInstance(currentProject).openFile(theVFile, true);
                        Editor theEditor = FileEditorManager.getInstance(currentProject).getSelectedTextEditor();
                        theEditor.getCaretModel().moveToOffset(indexToFocusOn);
                        theEditor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
                    }
                });
                break;

            case "NewRule":
                try {
                    PrintWriter writer = new PrintWriter(projectPath + "/ruleJson.txt", "UTF-8");
                    writer.println(messageAsJson.get("data").getAsString());
                    writer.close();
                } catch (IOException e) {
                    System.out.println("error in writing the rules");
                    return;
                }
                this.setRules(MessageProcessor.getIntitialRules().toString());

        }

    }


    private void createIgnoredFileList() {

        List<String> files = new ArrayList<>(Arrays.asList(".idea", "out", "source_xml.xml", "tempResultXmlFile.xml", "testProject.iml"));
        for (String f : files) {
            VirtualFile vfile = ProjectManager.getInstance().getOpenProjects()[0].getBaseDir().findFileByRelativePath(f);
            if (vfile != null) {
                ignoredFiles.add(vfile);
            }
        }
    }

    // checks if we should ignore a file
    private boolean shouldIgnoreFile(VirtualFile s) {
        if (ignoredFiles == null) {
            return false;
        }
        for (VirtualFile vfile : ignoredFiles) {
            if (vfile.getCanonicalPath().equals(s.getCanonicalPath())) {
                return true;
            } else if (isFileAChildOf(s, vfile)) {
                return true;
            }
        }
        return false;
    }

    // determines if one file/directory is stored somewhere down the line in another directory
    private static boolean isFileAChildOf(VirtualFile maybeChild, VirtualFile possibleParent) {
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

    // when the text of a file changes
    private void handleVFileChangeEvent(VFileEvent event) {
        VirtualFile file = event.getFile();

        // if we are dealing with ruleJson.txt
        if (file.getName().equals("ruleJson.txt")) {
            this.setRules(MessageProcessor.getIntitialRules().toString());
            return;
        }

        // do not handle if the file is not a part of the project
        if (!shouldConsiderEvent(file)) {
            return;
        }

        System.out.println("CHANGE");
        this.setSrcml(SRCMLHandler.updateXMLForProject(this.getSrcml(), file.getPath()));
    }

    // when a file is created
    private void handleVFileCreateEvent(VFileEvent event) {

        VirtualFile file = event.getFile();

        // if we are dealing with ruleJson.txt
        if (file.getName().equals("ruleJson.txt")) {
            this.setRules(MessageProcessor.getIntitialRules().toString());
            return;
        }

        // do not handle if the file is not a part of the project
        if (!shouldConsiderEvent(file)) {
            return;
        }

        System.out.println("CREATE");
        this.setSrcml(SRCMLHandler.addXMLForProject(this.getSrcml(), file.getPath()));

    }

    // when a file's properties change. for instance, if the file is renamed.
    private void handleVFilePropertyChangeEvent(VFileEvent event) {
        VirtualFile file = event.getFile();

        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        List<String> newPaths = getFilePaths(project);

        System.out.println("PROP_CHANGE");
        for (String path : srcml.getPaths()) {
            if (!newPaths.contains(path)) {

                SRCMLxml newsrcML = SRCMLHandler.removeXMLForProject(srcml, path);
                newsrcML = SRCMLHandler.addXMLForProject(newsrcML, file.getPath());
                this.setSrcml(newsrcML);
                break;
            }
        }

    }

    // when a file is deleted
    private void handleVFileDeleteEvent(VFileEvent event) {
        VirtualFile file = event.getFile();

        // if we are dealing with ruleJson.txt
        if (file.getName().equals("ruleJson.txt")) {
            this.setRules("");
            return;
        }

        System.out.println("DELETE");
        this.setSrcml(SRCMLHandler.removeXMLForProject(this.getSrcml(), file.getPath()));

    }


    /**
     * chech whether we should consider the file change
     *
     * @param file
     * @return boolean
     */
    private boolean shouldConsiderEvent(VirtualFile file) {

        if (file.isDirectory())
            return false;
        else if (!file.getCanonicalPath().endsWith(".java")) {
            return false;
        }
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

        return PsiManager.getInstance(project).isInProject(psiFile);

    }


    /**
     * generate list of java file paths
     *
     * @param project
     * @return list of file paths in the project
     */
    public static List<String> getFilePaths(Project project) {
        List<String> paths = new ArrayList<>();

        // start off with root
        VirtualFile rootDirectoryVirtualFile = project.getBaseDir();

        // set up queue
        List<VirtualFile> q = new ArrayList<>();
        q.add(rootDirectoryVirtualFile);

        // traverse the queue
        while (!q.isEmpty()) {
            VirtualFile item = q.get(0);
            q.remove(0);
            for (VirtualFile childOfItem : item.getChildren()) {
                if (childOfItem.isDirectory())
                    q.add(childOfItem);
                else if (childOfItem.getCanonicalPath().endsWith(".java")) {
                    paths.add(childOfItem.toString().substring(7)); // remove file:// from the beginning
                }
            }
        }

        return paths;
    }

}