import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.file.impl.FileManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class ChatServer extends WebSocketServer {

    private List<String> backedUpMessages = new LinkedList<String>();

	public ChatServer(int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
	}

    public ChatServer(int port, String initialMessage) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
        backedUpMessages.add(initialMessage);
    }

	public ChatServer(InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake ) {
		// this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
        sendBackedUpMessages();
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote ) {
		this.sendToAll( conn + " has left the room!" );
		System.out.println( conn + " has left the room!" );
	}

	@Override
	public void onMessage(WebSocket conn, String message ) {
		this.sendToAll( message );
		System.out.println( conn + ": " + message );

        JsonParser parser = new JsonParser();
        JsonObject messageAsJson = parser.parse(message).getAsJsonObject();
        JsonObject theDataFromTheMessage = messageAsJson.get("data").getAsJsonObject();

        if(messageAsJson.get("command").getAsString().equals("JUMP_TO_CLASS_WITH_LINE_NUM")){
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String fileToFocusOn = theDataFromTheMessage.get("fileName").getAsString();
                    int indexToFocusOn = theDataFromTheMessage.get("lineNumber").getAsInt();
                    Project currentProject = ProjectManager.getInstance().getOpenProjects()[0];
                    VirtualFile theVFile = FilenameIndex.getVirtualFilesByName(currentProject, fileToFocusOn, GlobalSearchScope.projectScope(currentProject)).iterator().next();
                    FileEditorManager.getInstance(currentProject).openFile(theVFile, true);
                    Editor theEditor = FileEditorManager.getInstance(currentProject).getSelectedTextEditor();
                    theEditor.getCaretModel().moveToOffset(indexToFocusOn);
                    theEditor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
                }
            });
        }


	}

	@Override
	public void onFragment(WebSocket conn, Framedata fragment ) {
		System.out.println( "received fragment: " + fragment );
	}

	public static void startServer() throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = true;
		int port = 8887; // 843 flash policy port
		ChatServer s = new ChatServer( port );
		s.start();
		System.out.println( "ChatServer started on port: " + s.getPort() );

		/*
		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			s.sendToAll( in );
			if( in.equals( "exit" ) ) {
				s.stop();
				break;
			} else if( in.equals( "restart" ) ) {
				s.stop();
				s.start();
				break;
			}
		}*/
	}
	@Override
	public void onError(WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	/**
	 * Sends <var>text</var> to all currently connected WebSocket clients.
	 * 
	 * @param text
	 *            The String to send across the network.
	 * @throws InterruptedException
	 *             When socket related I/O errors occur.
	 */
	public void sendToAll( String text ) {
		Collection<WebSocket> con = connections();
        backedUpMessages.add(text);

        if(con.size() == 0){
            System.out.println("Putting message on hold since there's no connection.");
        }else{
            while(con.size() != 0 && !backedUpMessages.isEmpty()){
                String itemToSend = backedUpMessages.get(0);
                synchronized ( con ) {
                    for( WebSocket c : con ) {
                        c.send( itemToSend );
                        System.out.println("Server sent: " + itemToSend);
                    }
                }
                backedUpMessages.remove(0);
            }

        }

	}

	public void sendBackedUpMessages(){

		Collection<WebSocket> con = connections();

		if(con.size() == 0){
			if(backedUpMessages.size() > 0){
				System.out.println("Can't clear out backlog since there's no connection right now.");
			}
		}else{
			while(con.size() != 0 && !backedUpMessages.isEmpty()){
				String itemToSend = backedUpMessages.get(0);
				synchronized ( con ) {
					for( WebSocket c : con ) {
						c.send( itemToSend );
						System.out.println("Server sent: " + itemToSend);
					}
				}
				backedUpMessages.remove(0);
			}

		}

	}

}
