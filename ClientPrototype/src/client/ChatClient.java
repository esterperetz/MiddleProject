package client;

import java.io.IOException;
import java.text.ParseException;

import Entities.Order;
import Entities.RequestPath;
import clientGui.ClientUi;
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
    // Handle messages from UI
	  public void send(RequestPath rq) {
	        try {
	            sendToServer(rq.toString());
	        } catch (IOException e) {
	            Platform.runLater(() -> clientUI.displayMessage("Error sending request to server."));
	        }
	    }
//	public void send_a_Message_From_Client_to_server(RequestPath message) {
//
//		RequestPath o = new RequestPath();
//		o.setPath("Order");
//		o.setMethod("GET");
//		o.addItem(message);
//
//		try {
//
//			sendToServer(o.toString());
//		} catch (IOException e) {
//			Platform.runLater(() -> clientUI.displayMessage("Could not send message to server. Terminating client."));
//			quit();
//		}
//	}

	// Quit client
	public void quit() {
		try {
			send(new RequestPath("quit",null));
			closeConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		System.exit(0);
	}

}
