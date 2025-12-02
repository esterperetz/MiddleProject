package clientLogic;

import client.ChatClient;
import clientGui.ClientUi;
import Entities.Order;
import Entities.RequestPath;

/**
 * שירות צד לקוח עבור פעולות על הזמנות (Order).
 * כל המתודות כאן רק בונות RequestPath ושולחות לשרת דרך ChatClient.
 */
public class OrderLogic {

    private final ClientUi client;

    public OrderLogic(ClientUi client) {
        this.client = client;
    }

    /**
     * בקשה: GET /Order
     * מבקש מהשרת את כל ההזמנות.
     */
    public void getAllOrders() {
        RequestPath rq = new RequestPath("Order", "GET");
        rq.addItem("GET_ALL_ORDERS");
        client.sendRequest(rq);   // מחלקת ChatClient צריכה להכיל מתודה send(RequestPath)
    }

    /**
     * בקשה: GET /Order/{id}
     * מבקש מהשרת הזמנה ספציפית לפי מספר הזמנה.
     */
    public void getOrderById(int orderId) {
        RequestPath rq = new RequestPath("Order", "GET");
        rq.addItem(String.valueOf(orderId));  // items[0] = id
        client.sendRequest(rq);
    }

    /**
     * בקשה: POST /Order
     * יצירת הזמנה חדשה בצד שרת.
     * (איך בדיוק למפות את השדות ל-items תלוי איך תחליטי בפרוטוקול שלך)
     */
    public void createOrder(Order order) {
        RequestPath rq = new RequestPath("Order", "POST");
        rq.addItem(String.valueOf(order.getOrder_number()));
        rq.addItem(order.getOrder_date().toString());
        rq.addItem(String.valueOf(order.getNumber_of_guests()));
        // אפשר להוסיף עוד שדות לפי הצורך
        client.sendRequest(rq);
    }

    /**
     * בקשה: PUT /Order
     * עדכון הזמנה קיימת.
     */
    public void updateOrder(Order order) {
        RequestPath rq = new RequestPath("Order", "PUT");
        rq.addItem(String.valueOf(order.getOrder_number()));
        rq.addItem(order.getOrder_date().toString());
        rq.addItem(String.valueOf(order.getNumber_of_guests()));
        // שוב – אפשר להוסיף עוד שדות
        client.sendRequest(rq);
    }

    /**
     * בקשה: DELETE /Order/{id}
     * מחיקת הזמנה לפי מזהה.
     */
    public void deleteOrder(int orderId) {
        RequestPath rq = new RequestPath("Order", "DELETE");
        rq.addItem(String.valueOf(orderId));
        client.sendRequest(rq);
    }
}

