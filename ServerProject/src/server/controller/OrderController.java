package server.controller;

import java.sql.SQLException;
import java.util.List;
import DAO.OrderDAO;
import Entities.*;
import ocsf.server.ConnectionToClient;
import java.io.IOException;

public class OrderController {

    private final OrderDAO dao = new OrderDAO();

    public void handle(Request req, ConnectionToClient client, List<ConnectionToClient> clients) throws IOException {
        
        if (req.getResource() != ResourceType.ORDER) {
            client.sendToClient("Error: Incorrect resource type requested. Expected ORDER.");
            return; 
        }
        
        try {
            switch (req.getAction()) {
                case GET_ALL:
                    List<Order> orders = dao.getAllOrders();
                    client.sendToClient(new Request(req.getResource(), ActionType.GET_ALL, null, orders));
                    break;

                case GET_BY_ID:
                    if (req.getId() == null) {
                         client.sendToClient("Error: GET_BY_ID requires an ID.");
                         break;
                    }
                    Order order = dao.getOrder(req.getId());
                    client.sendToClient(new Request(req.getResource(), ActionType.GET_BY_ID, req.getId(), order));
                    break;

                case CREATE:
                    if (req.getPayload() == null || !(req.getPayload() instanceof Order)) {
                         client.sendToClient("Error: CREATE action requires an Order payload.");
                         break;
                    }
                    Order o = (Order) req.getPayload();
                    
                    // Validate Mandatory Identification
                    if (o.getIdentification_details() == null || o.getIdentification_details().isEmpty()) {
                        client.sendToClient("Error: Identification details are mandatory.");
                        break;
                    }

                    boolean created = dao.createOrder(o);
                    if (created) {
                        client.sendToClient("Success: Order created.");
                        sendOrdersToAllClients();
                    } else {
                        client.sendToClient("Error: Failed to create order.");
                    }
                    break;
                    
                case UPDATE: 
                    if (req.getPayload() == null || !(req.getPayload() instanceof Order)) {
                         client.sendToClient("Error: UPDATE action requires an Order payload.");
                         break;
                    }
                    Order updatedOrder = (Order) req.getPayload();
                    boolean updated = dao.updateOrder(updatedOrder); 
                    if (updated) {
                        client.sendToClient("Success: Order updated.");
                        sendOrdersToAllClients();
                    } else {
                        client.sendToClient("Error: Failed to update order.");
                    }
                    break;

                case DELETE: 
                    if (req.getId() == null) {
                         client.sendToClient("Error: DELETE requires an ID.");
                         break;
                    }
                    boolean deleted = dao.deleteOrder(req.getId()); 
                    if (deleted) {
                        client.sendToClient("Success: Order deleted.");
                        sendOrdersToAllClients();
                    } else {
                        client.sendToClient("Error: Failed to delete order.");
                    }
                    break;

                default:
                    client.sendToClient("Unsupported action: " + req.getAction());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            client.sendToClient("Database error: " + e.getMessage());
        }
    }

    private void sendOrdersToAllClients() {
        try {
            List<Order> orders = dao.getAllOrders();
            Request updateMsg = new Request(ResourceType.ORDER, ActionType.GET_ALL, null, orders);
            Router.sendToAllClients(updateMsg);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}