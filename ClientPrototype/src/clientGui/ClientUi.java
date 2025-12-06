package clientGui;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import Entities.Request; // ✅ ודא שאתה מייבא את Request
import client.ChatClient;
import client.MessageListener;
import javafx.scene.control.Alert;

/**
 * ClientUi manages the client-side user interface.
 * It connects to the server using ChatClient and sends/receives messages.
 * All messages from the server are passed to registered listeners.
 */
public class ClientUi {

    private ChatClient chatClient;
    @SuppressWarnings({ "rawtypes" })
	private List<MessageListener> listeners;//any information from screens that show information from server, be here

    /**
     * Creates the UI and connects to the server.
     *
     * @param ip The server IP address.
     */
    public ClientUi(String ip) {
    	try {
			chatClient = new ChatClient(ip, 5555, this);
		} catch (IOException e) {
			e.printStackTrace();
			 Alert alert = new Alert(Alert.AlertType.ERROR);
		     alert.setTitle("Error");
		     alert.setContentText("Connection failed, try a different ip");
		     alert.showAndWait();
		}	
    	this.listeners = new ArrayList<>();
    }

    /**
     * Sends a Request object to the server.
     *
     * @param message The request to send.
     */
    public void sendRequest(Request message) { 
      if (message != null && chatClient != null) {
          System.out.println(message.toString());
          chatClient.send(message); 
      }
    }

    /**
     * * Called when the server sends a message.
     * Sends the message to all listeners.
     * @param msg The message from the server.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void displayMessage(Object msg) { 
    	
    	for(MessageListener listener: this.listeners) {
    		listener.onMessageReceive(msg);
    	}
    }

    /**
     * Adds a listener that will receive server messages.
     * @param listener The listener to add.
     */
    @SuppressWarnings("rawtypes")
	public void addListener(MessageListener listener) {
    	this.listeners.add(listener);
    }
    
    /**
     *Sends a "quit" message to the server.
     * Used when the client disconnects.
     */
    public void disconnectClient() {
        if (chatClient != null) {
            try {
            	chatClient.send("quit");
                //chatClient.closeConnection();
                
            } catch (Exception e) {
                // Ignore
            	e.printStackTrace();
            }
        }
    }
        
}