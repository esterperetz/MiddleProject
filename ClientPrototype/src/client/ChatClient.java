package client;

import java.io.IOException;
import Entities.Request;
import clientGui.ClientUi;
import ocsf.client.AbstractClient;

public class ChatClient extends AbstractClient {

    private ClientUi clientUI;

    public ChatClient(String host, int port, ClientUi clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        openConnection();
    }

    @Override
    public void handleMessageFromServer(Object msg) {
        System.out.println("ChatClient received object: " + msg.getClass().getSimpleName());
        clientUI.notifyListeners(msg);
    }

    //sends Request message
    public void send(Request req) {
        try {
            sendToServer(req);
        }
        catch (IOException e) {
            e.printStackTrace();
            clientUI.notifyListeners("Error: Could not send request to server.");
        }
    }
    
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}