package server.controller;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import DAO.OrderDAO;
import DAO.WaitingListDAO;
import entities.ActionType;
import entities.Order;
import entities.ResourceType;
import entities.Response;

/**
 * Background thread that runs every minute to check for late orders.
 * According to requirements, if a customer is more than 15 minutes late, 
 * the reservation is automatically cancelled.
 */
public class OrderCleanupThread extends Thread {
    private final OrderDAO orderDao = new OrderDAO();
    private final WaitingListDAO waitingListDao = new WaitingListDAO(); 
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
            // Fetch only APPROVED orders
            List<Order> activeOrders = orderDao.getOrdersByStatus(Order.OrderStatus.APPROVED);
            activeOrders.addAll(orderDao.getOrdersByStatus(Order.OrderStatus.PENDING));
            
            long now = new Date().getTime();
            boolean hasChanges = false;

            for (Order order : activeOrders) {
                long orderTime = order.getOrderDate().getTime();
                long diffInMinutes = (now - orderTime) / 60000; 
                


                // 15-minute late rule enforcement [cite: 32]
                if (diffInMinutes > 15) {
                    System.out.println("System: Auto-cancelling late order #" + order.getOrderNumber());
                    EmailService.sendCancelation(order.getCustomer(), order);
                    System.out.println(EmailService.getContent());
                    if(order.getOrderStatus() == Order.OrderStatus.PENDING) {
                    	waitingListDao.getWaitingOrderByConfirmationCode(order.getConfirmationCode());
                    }
                    order.setOrderStatus(Order.OrderStatus.CANCELLED);
                    orderDao.updateOrder(order);
                    hasChanges = true;
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
    
    private void EmailService() throws SQLException {
    	try {
    		List<Order> seatedOrders = orderDao.getOrdersByStatus(Order.OrderStatus.SEATED);
    		long now = new Date().getTime();
    		for (Order order : seatedOrders) { 
    			
    	}

        	
        }
    	finally {
    		
    	}

    }
    

    public void stopThread() {
        this.running = false;
        this.interrupt(); 
    }
}