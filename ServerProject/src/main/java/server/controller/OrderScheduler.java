package server.controller;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import DAO.OrderDAO;

public class OrderScheduler {

    private static ScheduledExecutorService scheduler;
    private static final OrderDAO orderDao = new OrderDAO();

    public static void startOrderExpirationTask() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return; // Already running
        }

        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Run every 1 minute
        scheduler.scheduleAtFixedRate(() -> {
            try {
                int count = orderDao.cancelExpiredOrders();
                if (count > 0) {
                    System.out.println("Scheduler: Cancelled " + count + " expired orders.");
                    // Optional: Broadcast update to clients
                    Router.sendToAllClients("REFRESH_ORDERS");
                }
            } catch (SQLException e) {
                System.err.println("Scheduler Error: Failed to cancel expired orders.");
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);

        System.out.println("Order Expiration Scheduler started.");
    }

    public static void stopScheduler() {
        if (scheduler != null) {
            scheduler.shutdown();
            System.out.println("Order Expiration Scheduler stopped.");
        }
    }
}
