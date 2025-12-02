package server.controller;

import Entities.Order;
import DAO.OrderDAO;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
/**
 * This controller manages the logic for orders.
 * It uses the OrderDAO to communicate with the database.
 */
public class OrderController {

    private final OrderDAO orderDAO;

    public OrderController(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    /**
     * Gets all orders from the database
     * @return List of all orders
     * @throws SQLException if the DB query fails
     */
    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.getAllOrders();
    }

    /**
     * Gets a single order from the database by its ID.
     * @param orderNumber the ID of the order we want to find
     * @return the order, or null if it does not exist
     * @throws SQLException if the DB query fails
     */
    public Order getOrder(int orderNumber) throws SQLException {
        return orderDAO.getOrder(orderNumber);
    }

    /**
     * Updates an order in the database.Before updating, we check that the order is valid.
     * @param order the updated order object
     * @throws IllegalArgumentException if the order data is invalid
     * @throws SQLException if the DB update fails
     */
    public void updateOrder(Order order)
            throws IllegalArgumentException, SQLException {
        validateOrder(order);
        // אפשר להוסיף כאן בדיקה שההזמנה קיימת אם תרצה:
        // if (orderDAO.getOrder(order.getOrder_number()) == null) { ... }
        orderDAO.updateOrder(order);
    }

    /**
     * Adds a new order to the database.We also check that the order is valid before inserting.
     * @param order the new order to save
     * @throws IllegalArgumentException if the order data is invalid
     * @throws SQLException if the DB insert fails
     */
    public void addOrder(Order order) throws SQLException {
        validateOrder(order);
        // ליצירה אפשר לוודא שאין כבר order_number כזה:
        // if (orderDAO.getOrder(order.getOrder_number()) != null) { ... }
        orderDAO.addOrder(order);
    }
    /**
     * Delete exist order from the database.We also check that the order is valid before deleting.
     * @param order  to delete
     * @throws IllegalArgumentException if the order data is invalid
     * @throws SQLException if the DB insert fails
     */
    public void deleteOrder(Order order) throws SQLException {
        validateOrder(order);
        orderDAO.deleteOrder(order);
    }

    /**
     * Checks that the order is valid.
     * @param order the order to check
     * @throws IllegalArgumentException if something is wrong in the order data
     */
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

        Date orderDate   = order.getOrder_date();
        Date placingDate = order.getDate_of_placing_order();
        Date now         = new Date();

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
}
