package server.controller;

import java.sql.SQLException;
import java.util.List;

import DAO.OrderDAO;
import entities.Order;

/**
 * Background thread for sending reminders to customers 2 hours before their
 * reservation.
 */
public class ReminderThread extends Thread {

    private final OrderDAO orderDAO = new OrderDAO();
    private boolean running = true;

    // Check every minute
    private static final int SLEEP_TIME = 60000;

    // Remind 2 hours ahead (120 minutes)
    private static final int MINUTES_AHEAD = 120;

    @Override
    public void run() {
        System.out.println("ReminderThread started. Checking for reservations in " + MINUTES_AHEAD + " minutes.");
        while (running) {
            try {
                processReminders();
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                running = false;
            } catch (Exception e) {
                System.err.println("Error in ReminderThread: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void processReminders() {
        try {
            // Get orders scheduled for ~2 hours from now that haven't been reminded
            List<Order> orders = orderDAO.getOrdersForReminder(MINUTES_AHEAD);

            if (!orders.isEmpty()) {
                System.out.println("[ReminderThread] Found " + orders.size() + " orders to remind.");
              
            }

            for (Order order : orders) {
                // Send actual email via EmailService
            	 
                EmailService.sendReminder(order);
                System.out.println("[ReminderThread] Sent reminder to order: " + order.getOrderNumber());

                // Mark as reminded in DB so we don't send again
                orderDAO.markAsReminded(order.getOrderNumber());
            }
        } catch (SQLException e) {
            System.err.println("ReminderThread DB Error: " + e.getMessage());
        }
    }

    // Removed local sendReminder simulation method as it is no longer used

    public void stopThread() {
        this.running = false;
        this.interrupt();
    }
}
