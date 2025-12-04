package clientGui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Entities.Request;
import client.ChatClient;
import client.MessageListener;

public class ClientUi {

    private ChatClient chatClient;
    private List<MessageListener> listeners;

    public ClientUi() {
        this.listeners = new ArrayList<>();
        try {
            chatClient = new ChatClient("localhost", 5555, this);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("CRITICAL: Could not connect to server!");
        }
    }

    public void sendRequest(Request req) {
        if (chatClient != null) {
            System.out.println("Sending request: " + req.toString());
            chatClient.send(req);
        } else {
            System.err.println("ChatClient is null - cannot send request.");
        }
    }

    // הלקוח (ChatClient) קורא לזה כשיש הודעה חדשה
    public void notifyListeners(Object msg) {
        for (MessageListener listener : listeners) {
            listener.onMessageReceive(msg);
        }
    }

    public void addListener(MessageListener listener) {
        this.listeners.add(listener);
    }
    
    public void disconnectClient() {
        if (chatClient != null) {
            chatClient.quit();
        }
    }
}