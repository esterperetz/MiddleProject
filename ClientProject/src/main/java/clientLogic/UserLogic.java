package clientLogic;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Customer;
import entities.Request;
import entities.ResourceType;

/**
 * Logic class for handling User-related requests (Subscribers/Customers).
 */
public class UserLogic {

	private final ClientUi client;

	public UserLogic(ClientUi client) {
		this.client = client;
	}

	/**
	 * Requests all subscribers from the server.
	 */
	public void getAllSubscribers() {
		Request req = new Request(ResourceType.CUSTOMER, ActionType.GET_ALL, null, null);
		client.sendRequest(req);
	}

	/**
	 * Requests a subscriber by their ID.
	 * 
	 * @param id The subscriber ID
	 */
	public void getSubscriberById(int id) {
		Request req = new Request(ResourceType.CUSTOMER, ActionType.GET_BY_ID, id, null);
		client.sendRequest(req);
	}

	/**
	 * Creates a new customer/subscriber.
	 * 
	 * @param customer The customer data
	 */
	public void createCustomer(Customer customer) {

		Request req = new Request(ResourceType.CUSTOMER, ActionType.REGISTER_CUSTOMER, null, customer);
		client.sendRequest(req);
	}

	/**
	 * Checks validity of a QR code.
	 * 
	 * @param code The scanned code
	 */
	public void CheckQRcode(String code) {

		Request req = new Request(ResourceType.CUSTOMER, ActionType.CHECK_QR_CODE, null, code);
		client.sendRequest(req);
	}

	/**
	 * Updates existing subscriber details.
	 * 
	 * @param customer The updated customer object
	 */
	public void updateSubscriber(Customer customer) {
		Request req = new Request(ResourceType.CUSTOMER, ActionType.UPDATE, customer.getSubscriberCode(),
				customer);
		client.sendRequest(req);
	}
}
