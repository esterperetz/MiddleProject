package clientUi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.ChatClient;
import clientGui.MessageListener;

//public class ClientUi extends Application {
public class ClientUi {

    private ChatClient chatClient;
    private List<MessageListener> listeners;

    private String data;
    public ClientUi() throws IOException {
    	chatClient = new ChatClient("localhost", 5555, this);	
    	this.listeners = new ArrayList<>();
    }

    public void sendMessage(String message) {

      if (!message.isEmpty() && chatClient != null) {
          chatClient.send_a_Message_From_Client_to_server(message);
      }
    }

    // Method called by ChatClient to display messages from server
    public void displayMessage(String msg) {
    	for(MessageListener listener: this.listeners) {
    		listener.onMessageReceive(msg);
    	}
    }
    
    public String getMessage() {
    	System.out.println("get "+data);
    	return data;
    	
    }
    
    public void addListener(MessageListener listener) {
    	this.listeners.add(listener);
    }

}
