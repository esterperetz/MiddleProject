package client;

/**
 * Interface for receiving messages from the server.
 * Classes implementing this interface can register to receive asynchronous
 * server messages.
 * 
 * @param <T> The type of message expected
 */
public interface MessageListener<T> {
	/**
	 * Called when a message is received from the server.
	 * 
	 * @param msg The message object
	 */
	void onMessageReceive(T msg);
}