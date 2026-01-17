package client;

import java.io.IOException;
import clientGui.ClientUi;
import javafx.application.Platform;
import ocsf.client.AbstractClient;

/**
 * ChatClient connects the client to the server.
 * It sends objects to the server and receives objects back.
 * All messages from the server are sent to the client UI.
 */
public class ChatClient extends AbstractClient {

	private ClientUi clientUI;

	/**
	 * Creates a new client and opens a connection to the server.
	 * 
	 * @param host     Server IP or hostname
	 * @param port     Server port
	 * @param clientUI The UI that shows messages to the user
	 * @throws IOException If connection fails
	 */
	public ChatClient(String host, int port, ClientUi clientUI) throws IOException {
		super(host, port);
		this.clientUI = clientUI;
		openConnection();
	}

	/**
	 * Called when the server sends a message.
	 * Passes the message to the UI.
	 * 
	 * @param msg The message received from the server
	 */
	@Override
	public void handleMessageFromServer(Object msg) {
		System.out.println(msg.toString());
		this.clientUI.displayMessage(msg);
	}

	/**
	 * Sends any object to the server.
	 * 
	 * @param obj The object to send
	 */
	public void send(Object obj) {
		try {
			sendToServer(obj);
		} catch (IOException e) {
			Platform.runLater(() -> clientUI.displayMessage("Error sending request to server."));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}