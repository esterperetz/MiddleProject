package clientLogic;

import java.sql.Date;

import clientGui.ClientUi;
import entities.ActionType;
import entities.Order;
import entities.Request;
import entities.ResourceType;

public class OrderLogic {

	private final ClientUi client;

	public OrderLogic(ClientUi client) {
		this.client = client;
	}

	public void getAllOrders() {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_ALL, null, null);
		client.sendRequest(req);
	}

	public void getOrderById(int orderId) {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_BY_ID, orderId, null);
		client.sendRequest(req);
	}

	public void getOrdersBySubscriberId(int subscriberId) {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_ALL_BY_SUBSCRIBER_ID, subscriberId, null);
		client.sendRequest(req);
	}
	
	public void getAvailabilityOptions(Date time) {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_AVAILABLE_TIME, null, time);
		client.sendRequest(req);
	}

   public void createOrder(Object data) {
        Request req = new Request(ResourceType.ORDER, ActionType.CREATE, null, data);
        client.sendRequest(req);
    }

   public void checkAvailability(Order order) {
	   Request req = new Request(ResourceType.ORDER, ActionType.CHECK_AVAILABILITY, null, order);
       client.sendRequest(req);
   }

	public void updateOrder(Order order) {
		Request req = new Request(ResourceType.ORDER, ActionType.UPDATE, order.getOrderNumber(), order);
		client.sendRequest(req);
	}
	

	public void deleteOrder(int orderId) {
		Request req = new Request(ResourceType.ORDER, ActionType.DELETE, orderId, null);
		client.sendRequest(req);
	}

	public void getSubscriberHistory(int subscriberId) {
		Request req = new Request(ResourceType.ORDER, ActionType.GET_USER_ORDERS, null, subscriberId);
		client.sendRequest(req);
	}

	public void sendEmail(Order order) {
		Request req = new Request(ResourceType.ORDER, ActionType.SEND_EMAIL, null, order);
		client.sendRequest(req);
	}
}