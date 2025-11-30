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

    public void updateOrder(int orderNumber, Date newDate, int newGuests)
            throws IllegalArgumentException, SQLException {

        // VALIDATIONS//
        if (newDate == null) {
            throw new IllegalArgumentException("Order date cannot be null");
        }

        Date now = new Date();
        if (!newDate.after(now)) {
            throw new IllegalArgumentException("Order date must be in the future");
        }

        if (newGuests <= 0) {
            throw new IllegalArgumentException("Number of guests must be positive");
        }

        // making sure order exists
        Order existing = orderDAO.getOrder(orderNumber);
        if (existing == null) {
            throw new IllegalArgumentException("Order " + orderNumber + " does not exist");
        }

        //Updating order
        orderDAO.updateOrder(orderNumber, newDate, newGuests);
    }
}
