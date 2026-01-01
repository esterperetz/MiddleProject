package clientLogic;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Customer;
import entities.Request;
import entities.ResourceType;

public class UserLogic {

	private final ClientUi client;

	public UserLogic(ClientUi client) {
	        this.client = client;
	    }

	public void getAllSubscribers() {
		Request req = new Request(ResourceType.CUSTOMER, ActionType.GET_ALL, null, null);
		client.sendRequest(req);
	}

	public void getSubscriberById(int id) {
		Request req = new Request(ResourceType.CUSTOMER, ActionType.GET_BY_ID, id, null);
		client.sendRequest(req);
	}
	

	public void createCustomer(Customer customer) {
		System.out.println("in create client");
		Request req = new Request(ResourceType.CUSTOMER, ActionType.REGISTER_SUBSCRIBER, null, customer);
		client.sendRequest(req);
	}

	public void updateSubscriber(Customer customer) {
		Request req = new Request(ResourceType.CUSTOMER, ActionType.UPDATE, customer.getSubscriberCode(),
				customer);
		client.sendRequest(req);
	}
}
