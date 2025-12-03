package clientGui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import Entities.RequestPath;
import client.ChatClient;
import client.MessageListener;

//public class ClientUi extends Application {
public class ClientUi {

    private ChatClient chatClient;
    @SuppressWarnings({ "rawtypes" })
	private List<MessageListener> listeners;

    public ClientUi() {
    	try {
			chatClient = new ChatClient("localhost", 5555, this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
    	this.listeners = new ArrayList<>();
    }

    public void sendRequest(RequestPath message) {

      if (message != null && chatClient != null) {

    	
    	  System.out.println(message.toString());
          chatClient.send(message);
      }
    }

    // Method called by ChatClient to display messages from server
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void displayMessage(String msg) {
    	for(MessageListener listener: this.listeners) {
    		listener.onMessageReceive(msg);
    	}
    }

    @SuppressWarnings("rawtypes")
	public void addListener(MessageListener listener) {
    	this.listeners.add(listener);
    }
    
    public void DisconnectClient() {
    	
    	chatClient.quit();
    }

}
