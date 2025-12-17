package server.controller;

import java.util.Date;
import java.util.List;
import DAO.OrderDAO;
import Entities.Order;
import Entities.Request;
import Entities.ResourceType;
import Entities.ActionType;

/**
 * Background thread that runs every minute to check for late orders.
 * According to requirements, if a customer is more than 15 minutes late, 
 * the reservation is automatically cancelled.
 */
public class OrderCleanupThread extends Thread {
    private final OrderDAO orderDao = new OrderDAO();
    private boolean running = true;

    @Override
    public void run() {
        while (running) {
            try {
                // Sleep for 1 minute 
                Thread.sleep(60000);

                // Perform the check
                checkAndCancelLateOrders();

            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void checkAndCancelLateOrders() {
        try {
            List<Order> orders = orderDao.getAllOrders();
            long now = new Date().getTime();

            for (Order order : orders) {
                // Check only APPROVED orders (future ones or promoted from waiting list)
                if (order.getStatus() == Order.OrderStatus.APPROVED) {
                    long orderTime = order.getOrder_date().getTime();
                    long diffInMinutes = (now - orderTime) / 60000;

                    // If more than 15 minutes have passed since the scheduled time
                    if (diffInMinutes > 15) {
                        System.out.println("Auto-cancelling late order: " + order.getOrder_number());
                        
                        // Update DB status to CANCELLED
                        order.setStatus(Order.OrderStatus.CANCELLED);
                        orderDao.updateOrder(order);

                        Router.sendToAllClients(new Request(ResourceType.ORDER, ActionType.GET_ALL, null, orderDao.getAllOrders()));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Cleanup thread error: " + e.getMessage());
        }
    }

    public void stopThread() {
        this.running = false;
        this.interrupt(); 
    }
}