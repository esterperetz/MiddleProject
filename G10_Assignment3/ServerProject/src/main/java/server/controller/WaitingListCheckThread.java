package server.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import DAO.OrderDAO;
import DAO.TableDAO;
import DAO.WaitingListDAO;
import entities.WaitingList;

/**
 * Background thread that periodically checks the Waiting List to see if any
 * pending requests can be promoted to active orders based on table availability.
 */
public class WaitingListCheckThread extends Thread {

    // Constants for timing logic
    private static final long CHECK_INTERVAL_MS = 60000; // 1 Minute
    private static final long ORDER_DURATION_MS = 2 * 60 * 60 * 1000; // 2 Hours

    private final WaitingListDAO waitingListDao = new WaitingListDAO();
    private final WaitingListController waitingListController = new WaitingListController();
    private final TableDAO tableDao = new TableDAO();
    private final OrderDAO orderDao = new OrderDAO();

    private boolean running = true;

    /**
     * Main execution loop of the thread.
     * Sleeps for a defined interval and then triggers the waiting list processing.
     */
    @Override
    public void run() {
        System.out.println("WaitingList Thread Started...");
        while (running) {
            try {
                Thread.sleep(CHECK_INTERVAL_MS);
                processWaitingList();
            } catch (InterruptedException e) {
                running = false;
                System.out.println("WaitingList Thread Interrupted. Stopping.");
            }
        }
    }

    /**
     * Core logic for processing the waiting list.
     * Iterates through all waiting customers and checks if a table is available.
     * It accounts for:
     * 1. Physical table capacity.
     * 2. Existing orders in the DB.
     * 3. Orders promoted *during this specific cycle* (to prevent double-booking).
     */
    private void processWaitingList() {
        try {
            List<WaitingList> entries = waitingListDao.getAllWaitingList();
            
            // Tracks entries promoted in the current loop iteration to manage local conflicts
            List<WaitingList> promotedThisCycle = new ArrayList<>();

            for (WaitingList entry : entries) {
                int guests = entry.getNumberOfGuests();

                Date requestedDate = entry.getReservationDate();
                if (requestedDate == null) {
                    requestedDate = new Date();
                }

                // Calculate availability
                int totalSuitableTables = tableDao.countTotalPhysicalTables(guests);
                int dbConflicts = orderDao.countConflictingOrders(requestedDate, guests);
                int localConflicts = 0;

                // Check against items we just promoted in this exact second/cycle
                for (WaitingList promoted : promotedThisCycle) {
                    Date promotedDate = promoted.getReservationDate();
                    if (promotedDate == null) {
                        promotedDate = new Date();
                    }

                    if (promoted.getNumberOfGuests() >= guests && isOverlapping(promotedDate, requestedDate)) {
                        localConflicts++;
                    }
                }

                // If we have more tables than (Existing Orders + Just Promoted Orders)
                if ((totalSuitableTables > (dbConflicts + localConflicts))) {
                    List<Integer> tableCapacities = tableDao.getAllTableCapacities2();
                    List<Integer> activeOrderSizes = orderDao.getActiveOrderSizes2(requestedDate);

                    // Perform detailed algorithm check for table fitting
                    if (isValidRemoveWaitingList(tableCapacities, activeOrderSizes) && entry.getInWaitingList() == 1) {
                        boolean success = promoteEntry(entry);

                        if (success) {
                            System.out.println("Promoted waiting list entry " + entry.getWaitingId());
                            promotedThisCycle.add(entry);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("WaitingList Thread Error: " + e.getMessage());
        }
    }

    /**
     * Checks if two dates overlap based on the standard order duration (2 hours).
     *
     * @param date1 First date.
     * @param date2 Second date.
     * @return true if the time windows overlap.
     */
    private boolean isOverlapping(Date date1, Date date2) {
        long time1 = date1.getTime();
        long time2 = date2.getTime();

        return (time1 < time2 + ORDER_DURATION_MS) && (time1 + ORDER_DURATION_MS > time2);
    }

    /**
     * valid that if we add the new order (implicitly handled by the caller logic)
     * we still have room. 
     * The logic sorts both lists in reverse order (Largest first) and attempts to match.
     *
     * @param tableList List of all physical table capacities.
     * @param orders    List of current active order sizes (guest counts).
     * @return true if the allocation is valid.
     */
    private boolean isValidRemoveWaitingList(List<Integer> tableList, List<Integer> orders) {
        if (tableList.size() < orders.size()) {
            System.out.println("The size of lists are different!!");
            return false;
        }

        // Sort descending to match largest groups to largest tables first
        tableList.sort(Comparator.reverseOrder());
        orders.sort(Comparator.reverseOrder());

        int tableIndex = 0;

        for (int i = 0; i < orders.size(); i++) {
            // Check if current table can hold current order
            if (!(tableList.get(tableIndex) >= orders.get(i))) {
                return false;
            }
            tableIndex++;
            
            // Prevent index out of bounds if we run out of tables
            if (tableIndex >= tableList.size()) {
                break;
            }
        }
        return true;
    }

    /**
     * Delegates the actual promotion logic to the controller.
     *
     * @param entry The waiting list entry to promote.
     * @return true if successful.
     */
    private boolean promoteEntry(WaitingList entry) {
        try {
            System.out.println("Attempting to promote waiting list entry: " + entry.getWaitingId());
            boolean success = waitingListController.handlePromoteToOrder(entry.getWaitingId(), null);
            if (success) {
                System.out.println("Success! Entry " + entry.getWaitingId() + " promoted to Order.");
                return true;
            }
        } catch (Exception e) {
            System.err.println("Promotion failed for ID " + entry.getWaitingId() + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Stops the thread safely.
     */
    public void stopThread() {
        this.running = false;
        this.interrupt();
    }
}