package server.controller;

import java.time.LocalDate;
import java.util.List;
import DAO.OrderDAO;
import DAO.WaitingListDAO;
import entities.Order;
import entities.WaitingList;

/**
 * A background thread that creates monthly reports automatically.
 * It checks the date every day to see if a new report is needed.
 */
public class MonthlyReportThread extends Thread {

    /** Keeps the thread running. */
    private boolean running = true;

    private final OrderDAO orderDAO = new OrderDAO();
    private final MonthlyReportService monthlyReportService = new MonthlyReportService();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    
    /** Remembers the last time we ran a report so we don't do it twice. */
    private LocalDate lastRunDate = null; 

    /**
     * The main loop of the thread.
     * 1. Creates a report immediately when the server starts.
     * 2. Checks every 12 hours if today is the 1st of the month.
     */
    @Override
    public void run() {
        System.out.println("Monthly Report Thread Started.");

        // Run once on startup
        generateReportForPreviousMonth();

        while (running) {
            try {
                LocalDate today = LocalDate.now();

                // Check: Is it the 1st of the month? Did we run it today?
                if (today.getDayOfMonth() == 1 && !today.equals(lastRunDate)) {
                    System.out.println("Auto-Report Trigger: It's the 1st of the month.");
                    generateReportForPreviousMonth();
                    lastRunDate = today;
                }

                // Sleep for 12 hours to save CPU
                Thread.sleep(1000 * 60 * 60 * 12); 

            } catch (InterruptedException e) {
                running = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets data for the previous month and saves the HTML report.
     */
    private void generateReportForPreviousMonth() {
        try {
            LocalDate now = LocalDate.now();
            LocalDate prevMonth = now.minusMonths(1); 
            
            int targetMonth = prevMonth.getMonthValue();
            int targetYear = prevMonth.getYear();

            System.out.println("Generating report data for: " + targetMonth + "/" + targetYear);

            List<Order> orders = orderDAO.getFinishedOrdersByMonth(targetMonth, targetYear);
            List<WaitingList> waitingList = waitingListDAO.getWaitingListForReport(targetMonth, targetYear);
            
            if (orders.isEmpty()) {
                System.out.println("No finished orders found. Report skipped.");
            } else {
                monthlyReportService.generateHtmlReport(targetMonth, targetYear, orders, waitingList);
            }

        } catch (Exception e) {
            System.err.println("Failed to generate monthly report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stops the thread safely.
     */
    public void stopThread() {
        running = false;
        this.interrupt();
    }
}