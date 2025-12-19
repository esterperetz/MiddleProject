package server.controller;

import java.util.List;
import DAO.WaitingListDAO;
import Entities.WaitingList;

/**
 * Background thread following the same style as OrderCleanupThread.
 * Monitors the waiting list and performs automatic promotion.
 */
public class WaitingListCheckThread extends Thread {
    private final WaitingListDAO waitingListDao = new WaitingListDAO();
    private final WaitingListController controller = new WaitingListController();
    private boolean running = true;

    @Override
    public void run() {
        while (running) {
            try {
                // Sleep for 1 minute
                Thread.sleep(60000);
                checkWaitingList();
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    private void checkWaitingList() {
        try {
            List<WaitingList> entries = waitingListDao.getAllWaitingList();
            
            for (WaitingList entry : entries) {
                // Try to promote based on table availability and size matching
                boolean promoted = controller.autoPromote(entry);
                
                if (promoted) {
                    System.out.println("Automation: Promoted " + entry.getFullName() + " from waiting list.");
                }
                // If not promoted (no table fits), the loop continues to check if the next person fits an available table.
            }
        } catch (Exception e) {
            System.err.println("WaitingList thread error: " + e.getMessage());
        }
    }

    public void stopThread() {
        this.running = false;
        this.interrupt();
    }
}