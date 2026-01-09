package server.controller;

import java.util.Date;
import java.util.List;
import DAO.OrderDAO;
import entities.ActionType;
import entities.Order;
import entities.Response;
import entities.ResourceType;

public class OrderCleanupThread extends Thread {
    private final OrderDAO orderDao = new OrderDAO();
    private boolean running = true;

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(60000); // Check every minute
                checkLateOrders();
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void checkLateOrders() {
        try {
            List<Order> activeOrders = orderDao.getOrdersByStatus(Order.OrderStatus.APPROVED);
            long now = System.currentTimeMillis();
            boolean changed = false;

            for (Order o : activeOrders) {
                // If more than 15 minutes past reservation time
                if (now - o.getOrderDate().getTime() > 15 * 60000) {
                    o.setOrderStatus(Order.OrderStatus.CANCELLED);
                    orderDao.updateOrder(o);
                    changed = true;
                }
            }

            if (changed) {
                Router.sendToAllClients(new Response(ResourceType.ORDER, ActionType.GET_ALL,
                        Response.ResponseStatus.SUCCESS, "Orders updated by cleanup",
                        orderDao.getAllOrdersWithCustomers()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopThread() {
        this.running = false;
        this.interrupt();
    }
}