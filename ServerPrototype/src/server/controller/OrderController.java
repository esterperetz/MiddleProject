package server.controller;

import DAO.OrderDAO;
import Entities.ActionType;
import Entities.Order;
import Entities.Request;
import ocsf.server.ConnectionToClient;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class OrderController {

    private final OrderDAO orderDAO;

    public OrderController(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    /**
     * נקודת הכניסה העיקרית – השרת קורא ל־handle כשמגיעה בקשה שקשורה ל־ORDER.
     */
    public void handle(Request request, ConnectionToClient client) {
        try {
            ActionType action = request.getAction();

            switch (action) {
                case GET_ALL:
                    handleGetAll(client);
                    break;

                case GET_BY_ID:
                    handleGetById(request, client);
                    break;

                case CREATE:
                    handleCreate(request, client);
                    break;

                case UPDATE:
                    handleUpdate(request, client);
                    break;

                case DELETE:
                    handleDelete(request, client);
                    break;

                default:
                    sendError(client, "Unsupported action for ORDER: " + action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                sendError(client, "Internal server error: " + e.getMessage());
            } catch (IOException ignore) {}
        }
    }

    /* ===== פעולות ===== */

    private void handleGetAll(ConnectionToClient client) throws SQLException, IOException {
        List<Order> allOrders = orderDAO.getAllOrders();

        if (allOrders == null || allOrders.isEmpty()) {
            sendError(client, "No orders found.");
            return;
        }

        client.sendToClient(allOrders);
    }

    private void handleGetById(Request request, ConnectionToClient client)
            throws SQLException, IOException {

        Integer id = request.getId();
        if (id == null) {
            sendError(client, "Order id is required.");
            return;
        }

        Order order = orderDAO.getOrder(id);
        if (order == null) {
            sendError(client, "Order " + id + " not found.");
            return;
        }

        client.sendToClient(order);
    }

    private void handleCreate(Request request, ConnectionToClient client)
            throws SQLException, IOException {

        if (!(request.getPayload() instanceof Order)) {
            sendError(client, "Invalid payload for CREATE ORDER.");
            return;
        }

        Order order = (Order) request.getPayload();
        validateOrder(order);

        orderDAO.addOrder(order);
        client.sendToClient("Order created successfully: " + order.getOrder_number());
    }

    private void handleUpdate(Request request, ConnectionToClient client)
            throws SQLException, IOException {

        if (!(request.getPayload() instanceof Order)) {
            sendError(client, "Invalid payload for UPDATE ORDER.");
            return;
        }

        Order order = (Order) request.getPayload();
        validateOrder(order);

        // אפשר לוודא שקיימת הזמנה כזו
        if (orderDAO.getOrder(order.getOrder_number()) == null) {
            sendError(client, "Order " + order.getOrder_number() + " does not exist.");
            return;
        }

        orderDAO.updateOrder(order);
        client.sendToClient("Order updated successfully: " + order.getOrder_number());
    }

    private void handleDelete(Request request, ConnectionToClient client)
            throws SQLException, IOException {

        Integer id = request.getId();
        if (id == null) {
            sendError(client, "Order id is required for delete.");
            return;
        }

        Order existing = orderDAO.getOrder(id);
        if (existing == null) {
            sendError(client, "Order " + id + " not found.");
            return;
        }

        orderDAO.deleteOrder(existing);
        client.sendToClient("Order deleted successfully: " + id);
    }

    /* ===== לוגיקת עזר ===== */

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (order.getOrder_number() <= 0) {
            throw new IllegalArgumentException("Order number is invalid");
        }

        if (order.getNumber_of_guests() <= 0) {
            throw new IllegalArgumentException("Number of guests must be positive");
        }

        if (order.getNumber_of_guests() > 100) {
            throw new IllegalArgumentException("Number of guests is too large");
        }

        if (order.getConfirmation_code() <= 0) {
            throw new IllegalArgumentException("Confirmation code is invalid");
        }

        if (order.getSubscriber_id() <= 0) {
            throw new IllegalArgumentException("Subscriber id must be positive");
        }

        Date orderDate = order.getOrder_date();
        Date placingDate = order.getDate_of_placing_order();
        Date now = new Date();

        if (orderDate == null) {
            throw new IllegalArgumentException("Order date cannot be null");
        }
        if (!orderDate.after(now)) {
            throw new IllegalArgumentException("Order date must be in the future");
        }

        if (placingDate == null) {
            throw new IllegalArgumentException("Placing date cannot be null");
        }
        if (placingDate.after(orderDate)) {
            throw new IllegalArgumentException("Placing date cannot be after order date");
        }
    }

    private void sendError(ConnectionToClient client, String msg) throws IOException {
        client.sendToClient("ERROR: " + msg);
    }
}

