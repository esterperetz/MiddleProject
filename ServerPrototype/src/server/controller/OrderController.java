package server.controller;

import java.sql.SQLException;
import java.util.List;
import DAO.OrderDAO;
import Entities.*;
import ocsf.server.ConnectionToClient;
import java.io.IOException;

public class OrderController {

    private final OrderDAO dao = new OrderDAO();

    public void handle(Request req, ConnectionToClient client) throws IOException {
        
        if (req.getResource() != ResourceType.ORDER) {
            System.err.println("OrderController received request for wrong resource: " + req.getResource());
            client.sendToClient("Error: Incorrect resource type requested. Expected ORDER.");
            return; 
        }
        
        try {
            switch (req.getAction()) {
                case GET_ALL:
                    List<Order> orders = dao.getAllOrders();
                    client.sendToClient(orders);
                    break;

                case GET_BY_ID:
                    if (req.getId() == null) {
                         client.sendToClient("Error: GET_BY_ID requires an ID.");
                         break;
                    }
                    Order order = dao.getOrder(req.getId());
                    client.sendToClient(order);
                    break;

                case CREATE:
                    if (req.getPayload() == null || !(req.getPayload() instanceof Order)) {
                         client.sendToClient("Error: CREATE action requires an Order payload.");
                         break;
                    }
                    Order o = (Order) req.getPayload();
                    boolean created = dao.createOrder(o);
                    client.sendToClient(created); 
                    break;
                    
                case UPDATE: // הוספת טיפול ב-UPDATE כפי שהגדרנו בצד הלקוח
                    if (req.getPayload() == null || !(req.getPayload() instanceof Order)) {
                         client.sendToClient("Error: UPDATE action requires an Order payload.");
                         break;
                    }
                    Order updatedOrder = (Order) req.getPayload();
                    // הערה: ה-DAO צריך לממש את updateOrder(Order)
                    boolean updated = dao.updateOrder(updatedOrder); 
                    client.sendToClient(updated); // שליחת Boolean
                    break;

                case DELETE: // הוספת טיפול ב-DELETE
                    if (req.getId() == null) {
                         client.sendToClient("Error: DELETE requires an ID.");
                         break;
                    }
                    boolean deleted = dao.deleteOrder(req.getId()); 
                    client.sendToClient(deleted); 
                    break;

                default:
                    client.sendToClient("Unsupported action: " + req.getAction());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            client.sendToClient("Database error: " + e.getMessage());
        }
    }
}