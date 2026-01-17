package server.controller;


import java.util.Date;
import java.util.List;

import DAO.OrderDAO;
import DAO.WaitingListDAO;
import entities.ActionType;
import entities.Order;
import entities.ResourceType;
import entities.Response;
import entities.Customer;

/**
 * Background thread that runs periodically to maintain order integrity.
 * 
 * It performs two main tasks:
 * 1. Cancels orders that are more than 15 minutes late.
 * 2. Automatically checks out orders that have been seated for more than 2 hours.
 */
public class OrderCleanupThread extends Thread {
    
    private final OrderDAO orderDao = new OrderDAO();
    private final WaitingListDAO waitingListDao = new WaitingListDAO();
    private final DAO.TableDAO tableDao = new DAO.TableDAO();
    private final DAO.CustomerDAO customerDao = new DAO.CustomerDAO();
    private boolean running = true;

    /**
     * Main execution loop.
     * Runs every 60 seconds to perform cleanup tasks.
     */
    @Override
    public void run() {
        while (running) {
            try {
                // Sleep for 1 minute
                Thread.sleep(60000);

                // Perform the check
                checkAndCancelLateOrders();
                autoCheckOutSeatedOrders();

            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    /**
     * Identifies orders that have exceeded the 15-minute arrival window.
     * Handles cancellation logic and notifies customers via email.
     */
    private void checkAndCancelLateOrders() {
        try {
            // Fetch only APPROVED orders
            List<Order> activeOrders = orderDao.getOrdersByStatus(Order.OrderStatus.APPROVED);
            activeOrders.addAll(orderDao.getOrdersByStatus(Order.OrderStatus.PENDING));

            long now = new Date().getTime();
            boolean hasChanges = false;

            for (Order order : activeOrders) {
                long orderTime = order.getOrderDate().getTime();
                long diffInMinutes = (now - orderTime) / 60000;

                if (diffInMinutes > 15) {
                    System.out.println("System: Auto-cancelling late order #" + order.getOrderNumber());
                    order.setCustomer(customerDao.getCustomerByCustomerId(order.getCustomer().getCustomerId()));

                    if (order.getOrderStatus() == Order.OrderStatus.PENDING) {
                        boolean isTrue = waitingListDao.getWaitingOrderByConfirmationCode(order.getConfirmationCode());
                        if (isTrue) {
                            EmailService.sendCancelation(order.getCustomer(), order);
                            System.out.println(EmailService.getContent());
                        }

                    } else {
                        // Handle APPROVED orders
                        EmailService.sendCancelation(order.getCustomer(), order);
                        System.out.println(EmailService.getContent());
                        order.setOrderStatus(Order.OrderStatus.CANCELLED);
                        orderDao.updateOrder(order);
                        hasChanges = true;
                    }
                }
            }

            // Broadcast update to all clients only if changes occurred
            if (hasChanges) {
                Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.GET_ALL,
                        Response.ResponseStatus.SUCCESS, "Cleanup completed", orderDao.getAllOrders()));
            }
        } catch (Exception e) {
            System.err.println("Cleanup Thread Error: " + e.getMessage());
        }
    }

    /**
     * Automatically processes checkout for orders that have been seated 
     * for the duration of the dining limit (2 hours).
     */
    private void autoCheckOutSeatedOrders() {
        try {
            List<Order> seatedOrders = orderDao.getOrdersByStatus(Order.OrderStatus.SEATED);
            long now = new Date().getTime();
            boolean hasChanges = false;

            for (Order order : seatedOrders) {
                if (order.getArrivalTime() == null)
                    continue;

                long arrivalTime = order.getArrivalTime().getTime();
                long diffInMinutes = (now - arrivalTime) / 60000;

                // If two hours have passed
                if (diffInMinutes >= 120) {

                    // check for customer discount
                    Customer fullCustomer = customerDao.getCustomerByCustomerId(order.getCustomer().getCustomerId());
                    double finalPrice = order.getTotalPrice();

                    if (fullCustomer != null && fullCustomer.getType() == entities.CustomerType.SUBSCRIBER) {
                        finalPrice *= 0.9;

                        // DB update for that order to PAID
                        if (orderDao.updateOrderCheckOut(order.getOrderNumber(), finalPrice, Order.OrderStatus.PAID)) {

                            // DB update to table
                            if (order.getTableNumber() != null) {
                                tableDao.updateTableStatus(order.getTableNumber(), 0);
                            }

                            if (finalPrice == 0.0) {
                                order.setTotalPrice(199.99);
                            }
                            order.setTotalPrice(finalPrice);
                            order.setLeavingTime(new Date());

                            EmailService.sendReceipt(fullCustomer, order);
                            System.out.println(EmailService.getContent());
                            hasChanges = true;
                        }
                    }
                }
            }

            // update status
            if (hasChanges) {
                Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.GET_ALL,
                        Response.ResponseStatus.SUCCESS, "Auto-checkout processed", orderDao.getAllOrders()));
            }
        } catch (Exception e) {
            System.err.println("Error in autoCheckOut: " + e.getMessage());
        }
    }

    /**
     * Stops the thread safely.
     */
    public void stopThread() {
        this.running = false;
        this.interrupt();
    }
}