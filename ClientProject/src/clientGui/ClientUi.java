package clientGui;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import Entities.Alarm;
import Entities.Request; // ✅ ודא שאתה מייבא את Request
import client.ChatClient;
import client.MessageListener;
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
    public ClientUi(String ip) {
    	try {
			chatClient = new ChatClient(ip, 5555, this);
			
		} catch (IOException e) {
			e.printStackTrace();
			String header="Error";
			String content="Connection failed, try a different ip";
			Alarm.showAlertWithException(header, content,Alert.AlertType.ERROR, e);
			 //Alert alert = new Alert(Alert.AlertType.ERROR);
		     //alert.setTitle("Error");
		     //alert.setContentText("Connection failed, try a different ip");
		     //alert.showAndWait();
		}
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

   public ClientUi getInstance() {
        return instance;
    }
        
}