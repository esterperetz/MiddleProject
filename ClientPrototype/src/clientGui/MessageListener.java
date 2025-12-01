package clientGui;

public interface MessageListener <T> {
	void onMessageReceive(T msg);
}
