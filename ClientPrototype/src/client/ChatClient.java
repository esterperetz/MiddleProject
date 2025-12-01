package client;

import java.io.IOException;

import clientUi.ClientUi;
import javafx.application.Platform;
import ocsf.client.AbstractClient;

public class ChatClient extends AbstractClient {

    private ClientUi clientUI;

    // Constructor
    public ChatClient(String host, int port, ClientUi clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        openConnection();
    }


    @Override
    public void handleMessageFromServer(Object msg) {
        String text = msg.toString();
        this.clientUI.displayMessage(text);
    }

//  // Handle messages from server
//  @Override
//  public void handleMessageFromServer(Object msg) {
//  	System.out.println(msg.toString());
//  	
//  	if (msg.toString().equals("quit")) {
//  		quit();
//  	}
//  	
////      Platform.runLater(() -> clientUI.displayMessage(msg.toString()));
//  	 clientUI.displayMessage(msg.toString());
//      
//  }
// Handle messages from server
//    // Handle messages from UI
    public void send_a_Message_From_Client_to_server(String message) {
    	
        try {
            sendToServer(message);
        } catch (IOException e) {
            Platform.runLater(() -> clientUI.displayMessage(
                "Could not send message to server. Terminating client."));
            quit();
        }
    }

    // Quit client
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
    
    
}
