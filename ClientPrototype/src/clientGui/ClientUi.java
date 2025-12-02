package clientGui;

import java.io.IOException;
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

    public ClientUi() throws IOException {
    	chatClient = new ChatClient("localhost", 5555, this);	
    	this.listeners = new ArrayList<>();
    }

    public void sendRequest(RequestPath message) {

      if (message != null && chatClient != null) {
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
