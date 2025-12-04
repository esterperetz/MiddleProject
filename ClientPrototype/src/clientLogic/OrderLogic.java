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

    /**
     * בקשה: קבלת כל ההזמנות
     * Request: Resource=ORDER, Action=GET_ALL
     */
    public void getAllOrders() {
        Request req = new Request(ResourceType.ORDER, ActionType.GET_ALL, null, null);
        client.sendRequest(req);
    }

    /**
     * בקשה: קבלת הזמנה לפי ID
     */
    public void getOrderById(int orderId) {
        Request req = new Request(ResourceType.ORDER, ActionType.GET_BY_ID, orderId, null);
        client.sendRequest(req);
    }

    /**
     * בקשה: יצירת הזמנה חדשה
     * Payload = אובייקט ה-Order
     */
    public void createOrder(Order order) {
        Request req = new Request(ResourceType.ORDER, ActionType.CREATE, null, order);
        client.sendRequest(req);
    }

    /**
     * בקשה: עדכון הזמנה
     */
    public void updateOrder(Order order) {
        // נניח שמעבירים את ה-ID בנפרד, או בתוך האובייקט, תלוי איך השרת בנוי.
        // לפי השרת שבנינו: הוא מצפה ל-Payload שהוא Order.
        Request req = new Request(ResourceType.ORDER, ActionType.UPDATE, order.getOrder_number(), order);
        client.sendRequest(req);
    }

    /**
     * בקשה: מחיקת הזמנה
     */
    public void deleteOrder(int orderId) {
        Request req = new Request(ResourceType.ORDER, ActionType.DELETE, orderId, null);
        client.sendRequest(req);
    }
}