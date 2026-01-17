package clientLogic;

import java.sql.Date;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Order;
import entities.Request;
import entities.ResourceType;

/**
 * Logic class for handling Order-related requests.
 * Constructs requests for creating, updating, deleting, and retrieving orders.
 */
public class OrderLogic {

	private final ClientUi client;

	public OrderLogic(ClientUi client) {
		this.client = client;
	}

	/**
	 * Resends confirmation code by contact info.
	 * 
	 * @param contact The contact information (email/phone)
	 */
	public void getConfirmationCodeByContact(String contact) {
		Request req = new Request(ResourceType.ORDER, ActionType.RESEND_CONFIRMATION, null, contact);
		client.sendRequest(req);
	}

	/**
	 * Requests all orders from the server.
	 */
	public void getAllOrders() {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_ALL, null, null);
		client.sendRequest(req);
	}

	/**
	 * Requests a specific order by its ID.
	 * 
	 * @param orderId The ID of the order
	 */
	public void getOrderById(int orderId) {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_BY_ID, orderId, null);
		client.sendRequest(req);
	}

	/**
	 * Requests all orders for a specific subscriber.
	 * 
	 * @param subscriberId The subscriber's ID
	 */
	public void getOrdersBySubscriberId(int subscriberId) {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_ALL_BY_SUBSCRIBER_ID, subscriberId, null);
		client.sendRequest(req);
	}

	/**
	 * Gets subscriber's orders (likely similar to getOrdersBySubscriberId but using
	 * code).
	 * 
	 * @param subscriberCode The subscriber code
	 */
	public void getSubscriberOrders(int subscriberCode) {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_USER_ORDERS, subscriberCode, null);
		client.sendRequest(req);
	}

	/**
	 * Requests available times for a given order context.
	 * 
	 * @param order The order with date/time details
	 */
	public void getAvailabilityOptions(Order order) {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_AVAILABLE_TIME, null, order);
		client.sendRequest(req);
	}

	/**
	 * Creates a new order.
	 * 
	 * @param data The order data
	 */
	public void createOrder(Object data) {
		Request req = new Request(ResourceType.ORDER, ActionType.CREATE, null, data);
		client.sendRequest(req);
	}

	/**
	 * Checks if a specific table/time is available for the order.
	 * 
	 * @param order The order details to check
	 */
	public void checkAvailability(Order order) {
		Request req = new Request(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY, null, order);
		client.sendRequest(req);
	}

	/**
	 * Updates an existing order.
	 * 
	 * @param order The updated order object
	 */
	public void updateOrder(Order order) {
		Request req = new Request(ResourceType.ORDER, ActionType.UPDATE, order.getOrderNumber(), order);
		client.sendRequest(req);
	}

	/**
	 * Updates the checkout time/status of an order.
	 * 
	 * @param order The order to check out
	 */
	public void updateOrderCheckOut(Order order) {
		Request req = new Request(ResourceType.ORDER, ActionType.UPDATE_CHECKOUT, order.getOrderNumber(), order);
		client.sendRequest(req);
	}

	/**
	 * Deletes an order (cancels it).
	 * 
	 * @param orderId The ID of the order to delete
	 */
	public void deleteOrder(int orderId) {
		Request req = new Request(ResourceType.ORDER, ActionType.DELETE, orderId, null);
		client.sendRequest(req);
	}

	/**
	 * Gets order history for a subscriber.
	 * 
	 * @param subscriberId The subscriber ID
	 */
	public void getSubscriberHistory(int subscriberId) {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_USER_ORDERS, null, subscriberId);
		client.sendRequest(req);
	}

	/**
	 * Requests to send an email for the order.
	 * 
	 * @param order The order to send email for
	 */
	public void sendEmail(Order order) {
		Request req = new Request(ResourceType.ORDER, ActionType.SEND_EMAIL, null, order);
		client.sendRequest(req);
	}

	/**
	 * Retrieves an order by its confirmation code.
	 * 
	 * @param code                  The confirmation code
	 * @param currentSubscriberCode The subscriber code asking for it
	 */
	public void getOrderByConfirmationCode(int code, int currentSubscriberCode) {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_BY_CODE, currentSubscriberCode, code);
		client.sendRequest(req);

	}
}