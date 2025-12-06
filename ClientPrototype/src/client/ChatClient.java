package client;

import java.io.IOException;
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
		// ✅ מעביר את האובייקט כפי שהוא ל-ClientUi
		this.clientUI.displayMessage(msg);
	}

    // Handle messages from UI
	// ❌ הוחלף מ-send(RequestPath rq) ל-send(Object obj)
	public void send(Object obj) { 
	    try {
	        // ✅ שולח את האובייקט ישירות לשרת (sendToServer מקבל Object)
	        sendToServer(obj);
	    } catch (IOException e) {
	        Platform.runLater(() -> clientUI.displayMessage("Error sending request to server."));
	    }
	}
}