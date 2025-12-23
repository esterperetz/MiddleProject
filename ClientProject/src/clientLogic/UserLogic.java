package clientLogic;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Request;
import entities.ResourceType;
import entities.Subscriber;

public class UserLogic {

	private final ClientUi client;

	public UserLogic(ClientUi client) {
	        this.client = client;
	    }

	public void getAllSubscribers() {
		Request req = new Request(ResourceType.SUBSCRIBER, ActionType.GET_ALL, null, null);
		client.sendRequest(req);
	}

	public void getSubscriberById(int id) {
		Request req = new Request(ResourceType.SUBSCRIBER, ActionType.GET_BY_ID, id, null);
		client.sendRequest(req);
	}
	

	public void registerSubscriber(Subscriber subscriber) {
		System.out.println("three");
		Request req = new Request(ResourceType.SUBSCRIBER, ActionType.REGISTER_SUBSCRIBER, null, subscriber);
		client.sendRequest(req);
	}

	public void updateSubscriber(Subscriber subscriber) {
		Request req = new Request(ResourceType.SUBSCRIBER, ActionType.UPDATE, subscriber.getSubscriberId(),
				subscriber);
		client.sendRequest(req);
	}
}
