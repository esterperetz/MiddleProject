package clientLogic;

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


    public void createOrder(Order order) {
        Request req = new Request(ResourceType.ORDER, ActionType.CREATE, null, order);
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
}