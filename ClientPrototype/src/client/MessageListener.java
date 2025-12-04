package client;

public interface MessageListener <T> {
	void onMessageReceive(T msg);
}