package clientGui;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import client.ChatClient;
import client.MessageListener;
import entities.Alarm;
import entities.Request;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * ClientUi manages the client-side user interface.
 * It connects to the server using ChatClient and sends/receives messages.
 * All messages from the server are passed to registered listeners.
 */
public class ClientUi {

    private ChatClient chatClient;
    @SuppressWarnings({ "rawtypes" })
	private List<MessageListener> listeners;//any information from screens that show information from server, be here
    private ClientUi instance;
    private String ip;
    /**
     * Creates the UI and connects to the server.
     *
     * @param ip The server IP address.
     */
    public ClientUi(String ip) throws IOException {  //ADDED IOEXCE
    	
    	chatClient = new ChatClient(ip, 5555, this);
		
    	this.ip = ip;
    	this.listeners = new ArrayList<>();
//    	clientGui.reservation.OrderUi_controller controller = new clientGui.reservation.OrderUi_controller();
//    	controller.initData(this, ip);
//    	instance = this;
    }
    
    public void setIp(String ip) {
    	this.ip = ip;
    }
    
    public String getIp() 
    {
    	return ip;
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
    	// check if object messege "quit"
    	if(msg instanceof String && msg.toString().equals("quit"))
    	{
    		Platform.runLater(()->{Alarm.showAlert("Server Down!", "Your app will close when you close this alert!", AlertType.ERROR);System.exit(0);});
    	}
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
   
    @SuppressWarnings("rawtypes")
	public void removeListener(MessageListener listener) {
    	this.listeners.remove(listener);
    	
    }
    
    public void removeAllListeners() {
        listeners.clear(); // מרוקן את כל רשימת המאזינים
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
            finally {
                System.exit(0);
            }
        }
    }

   public ClientUi getInstance() {
        return instance;
    }


        
}