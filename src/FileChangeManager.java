import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.messages.MessageBusConnection;
import core.model.PsiClassHierarchyBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FileChangeManager implements ApplicationComponent, BulkFileListener {

    private final MessageBusConnection connection;
    ChatServer s;

    public FileChangeManager(ChatServer server) {
        connection = ApplicationManager.getApplication().getMessageBus().connect();
        s = server;
    }

    public void initComponent() {
        connection.subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    public void disposeComponent() {
        connection.disconnect();
    }

    public void before(List<? extends VFileEvent> events) {
        // System.out.println(events);
        // ...
    }

    // precondition: event instanceof VFileContentChangeEvent => true
    public void handleVFileChangeEvent(VFileEvent event){
        VirtualFile file = event.getFile();

        if(GrepServerToolWindowFactory.shouldIgnoreFile(file)){
            return;
        }

        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

        // do not handle if the file is not a part of the project
        if(!PsiManager.getInstance(project).isInProject(psiFile)){
            // System.out.println("Ignored: " + event);
            return;
        }

        JsonObject data = new JsonObject();
        data.addProperty("canonicalPath", file.getCanonicalPath());
        data.addProperty("code", psiFile.getText());
        data.add("ast", GrepServerToolWindowFactory.generateASTAsJSON(psiFile));

        s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "UPDATE_CODE_IN_FILE", data}).toString());
        System.out.println("Handled: " + event);

        // Class Table now precomputed (for PSI classes)
        /*if(psiFile instanceof PsiJavaFile){
            JsonObject newCt = new JsonObject();
            PsiClassHierarchyBuilder classBuilder = new PsiClassHierarchyBuilder(newCt);
            classBuilder.visit(psiFile, new JsonObject());
            s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "UPDATE_PSI_CLASS_TABLE", newCt}).toString());
        }*/

    }

    // precondition: event instanceof VFileContentChangeEvent => true
    public void handleVFileDeleteEvent(VFileEvent event){

        VirtualFile file =  event.getFile();

        if(GrepServerToolWindowFactory.shouldIgnoreFile(file)){
            return;
        }

        JsonObject data = new JsonObject();
        data.addProperty("canonicalPath", file.getCanonicalPath());

        s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "DELETE_FILE", data}).toString());
        System.out.println("Handled: " + event);

    }

    public void handleVFileCreateEvent(VFileEvent event){

        VirtualFile file =  event.getFile();

        if(GrepServerToolWindowFactory.shouldIgnoreFile(file)){
            return;
        }

        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

        JsonObject data = new JsonObject();

        data.addProperty("canonicalPath", file.getCanonicalPath());
        data.addProperty("name", file.getNameWithoutExtension());
        try {
            data.addProperty("parent", file.getParent().getCanonicalPath());
        }catch(Exception e){
            data.addProperty("parent", "");
        }

        if(file.isDirectory()){
            data.addProperty("isDirectory", true);
            data.add("children", new JsonArray());
        }else{
            data.addProperty("isDirectory", false);
            data.addProperty("code", psiFile.getText());
            data.add("ast", GrepServerToolWindowFactory.generateASTAsJSON(psiFile));

        }

        // add more params

        s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "CREATE_FILE", data}).toString());

    }

    public void handleVFilePropertyChangeEvent(VFileEvent event){

        // potential bug area: the file path of the old file may not be canonical

        VFilePropertyChangeEvent pcevent = (VFilePropertyChangeEvent) event;
        VirtualFile file = pcevent.getFile();

        if(GrepServerToolWindowFactory.shouldIgnoreFile(file)){
            return;
        }

        if(pcevent.getPropertyName().equals("name")){ // rename of a file
            System.out.println("RENAME");

            JsonObject data = new JsonObject();
            data.addProperty("newCanonicalPath", file.getCanonicalPath());
            data.addProperty("oldCanonicalPath", pcevent.getOldPath());
            data.addProperty("name", file.getNameWithoutExtension());

            s.sendToAll(MessageProcessor.encodeData(new Object[]{"IDEA", "WEB", "RENAME_FILE", data}).toString());
        }else{
            System.out.println("UNHANDLED CASE IN PROPERTY_CHANGE!!");
            System.out.println(pcevent);
        }

    }

    public void after(List<? extends VFileEvent> events) {

        if(s == null){
            return;
        }

        for(VFileEvent event : events){
            if(event instanceof VFileCreateEvent){
                System.out.println("CREATE");
                handleVFileCreateEvent(event);
            }else if(event instanceof VFileContentChangeEvent){
                System.out.println("CHANGE");
                handleVFileChangeEvent(event);
            }else if(event instanceof VFileDeleteEvent){
                System.out.println("DEL");
                handleVFileDeleteEvent(event);
            }else if(event instanceof VFilePropertyChangeEvent){
                System.out.println("PROP_CHANGE");
                handleVFilePropertyChangeEvent(event);
            }
        }
    }

    @NotNull
    @Override
    public String getComponentName() {
        return null;
    }
}