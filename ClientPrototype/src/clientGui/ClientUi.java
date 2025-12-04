package clientGui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Entities.Request; // ✅ ודא שאתה מייבא את Request
import client.ChatClient;
import client.MessageListener;

public class ClientUi {

    private ChatClient chatClient;
    @SuppressWarnings({ "rawtypes" })
	private List<MessageListener> listeners;

    public ClientUi() {
    	try {
			chatClient = new ChatClient("localhost", 5555, this);
		} catch (IOException e) {
			e.printStackTrace();
		}	
    	this.listeners = new ArrayList<>();
    }

    // ✅ התיקון כאן: שינוי הטיפוס מ-RequestPath ל-Request
    public void sendRequest(Request message) { 
      if (message != null && chatClient != null) {
          System.out.println(message.toString());
          chatClient.send(message); 
      }
    }

    // המתודה הזו תוקנה כבר לקבלת Object, כנדרש לתקשורת שרת-לקוח
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void displayMessage(Object msg) { 
    	for(MessageListener listener: this.listeners) {
    		listener.onMessageReceive(msg);
    	}
    }

    @SuppressWarnings("rawtypes")
	public void addListener(MessageListener listener) {
    	this.listeners.add(listener);
    }
    
    public void disconnectClient() {
        if (chatClient != null) {
            try {
                chatClient.closeConnection();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}