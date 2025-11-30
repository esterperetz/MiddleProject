package server.controller;

import Entities.Order;
import DAO.OrderDAO;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class OrderController {

    private final OrderDAO orderDAO;

    public OrderController(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.getAllOrders();
    }

    public Order getOrder(int orderNumber) throws SQLException {
        return orderDAO.getOrder(orderNumber);
    }

    public void updateOrder(Order order)
            throws IllegalArgumentException, SQLException {
        validateOrder(order);
        // אפשר להוסיף כאן בדיקה שההזמנה קיימת אם תרצה:
        // if (orderDAO.getOrder(order.getOrder_number()) == null) { ... }
        orderDAO.updateOrder(order);
    }

    public void addOrder(Order order) throws SQLException {
        validateOrder(order);
        // ליצירה אפשר לוודא שאין כבר order_number כזה:
        // if (orderDAO.getOrder(order.getOrder_number()) != null) { ... }
        orderDAO.addOrder(order);
    }

    // validate order
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
