package clientLogic;

import clientGui.ClientUi;
import Entities.ActionType;
import Entities.Order;
import Entities.Request;
import Entities.ResourceType;

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


    public void createOrder(Order order) {
        Request req = new Request(ResourceType.ORDER, ActionType.CREATE, null, order);
        client.sendRequest(req);
    }


    public void updateOrder(Order order) {
        Request req = new Request(ResourceType.ORDER, ActionType.UPDATE, order.getOrder_number(), order);
        client.sendRequest(req);
    }


    public void deleteOrder(int orderId) {
        Request req = new Request(ResourceType.ORDER, ActionType.DELETE, orderId, null);
        client.sendRequest(req);
    }
}