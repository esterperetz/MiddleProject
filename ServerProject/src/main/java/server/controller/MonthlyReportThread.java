package server.controller;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import DAO.OrderDAO;
import DAO.WaitingListDAO;
import entities.Order;
import entities.WaitingList;

/**
 * Background thread for generating monthly reports automatically.
 * <p>
 * Logic:
 * 1. Checks on startup if the previous month's report is missing.
 * 2. Calculates the exact time remaining until the 1st of the next month (00:00).
 * 3. Sleeps for that duration to save CPU resources.
 * 4. Wakes up, generates the report, and repeats the cycle.
 * </p>
 */
public class MonthlyReportThread extends Thread {

    /** Controls the main loop of the thread. */
    private boolean running = true;
    
    private final OrderDAO orderDAO = new OrderDAO();
    private final MonthlyReportService monthlyReportService = new MonthlyReportService();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    
    /** Directory where reports are saved. Used to check if a file already exists. */
    private final String REPORTS_DIR = "server_files/reports/";

    /**
     * The main execution loop.
     * Checks for missing reports immediately, then sleeps until the 1st of the next month.
     */
    @Override
    public void run() {
        System.out.println("Monthly Report Thread Started.");

        // Check and generate report on startup if missing
        generateReportForPreviousMonth();

        while (running) {
            try {
                LocalDateTime now = LocalDateTime.now();
                
                // Calculate the 1st day of the NEXT month at 00:00:00
                LocalDateTime nextMonthFirstDay = now.with(TemporalAdjusters.firstDayOfNextMonth())
                                                     .toLocalDate()
                                                     .atStartOfDay();

                // Calculate duration to sleep
                Duration duration = Duration.between(now, nextMonthFirstDay);
                long sleepMillis = duration.toMillis();
                
                // Print sleep time for debugging
                long days = duration.toDays();
                long hours = duration.toHours() % 24;
                long minutes = duration.toMinutes() % 60;

                System.out.println(String.format("Report Thread: Sleeping until %s (Duration: %d days, %d hours, %d minutes)", 
                        nextMonthFirstDay.toString(), days, hours, minutes));
                
                // Sleep until the next month
                Thread.sleep(sleepMillis); 

                // --- Woke up at 00:00 on the 1st of the month ---
                
                if (!running) break;

                System.out.println("It's the 1st of the month! Generating report...");
                
                // Small delay to ensure system date is synced
                Thread.sleep(2000); 
                
                generateReportForPreviousMonth();

            } catch (InterruptedException e) {
                running = false;
                System.out.println("Monthly Report Thread stopped.");
            } catch (Exception e) {
                e.printStackTrace();
                // Sleep for 1 minute on error to avoid CPU spike
                try { Thread.sleep(60000); } catch (InterruptedException ex) {}
            }
        }
    }

    /**
     * Generates the report for the previous month.
     * <p>
     * 1. Calculates the previous month and year.
     * 2. Checks if the report ZIP already exists (to avoid overwriting).
     * 3. Fetches data from the database.
     * 4. Calls the service to create HTML and ZIP files.
     * </p>
     */
    private void generateReportForPreviousMonth() {
        try {
            LocalDate now = LocalDate.now();
            LocalDate prevMonth = now.minusMonths(1); 
            
            int targetMonth = prevMonth.getMonthValue();
            int targetYear = prevMonth.getYear();

            String expectedZipName = "Report_" + targetYear + "_" + String.format("%02d", targetMonth) + ".zip";
            File expectedFile = new File(REPORTS_DIR + expectedZipName);

            // Do not overwrite existing reports
            if (expectedFile.exists()) {
                System.out.println("Report for " + targetMonth + "/" + targetYear + " already exists. Skipping.");
                return;
            }

            System.out.println("Generating Monthly Report for: " + targetMonth + "/" + targetYear);

            List<Order> orders = orderDAO.getFinishedOrdersByMonth(targetMonth, targetYear);
            List<WaitingList> waitingList = waitingListDAO.getWaitingListForReport(targetMonth, targetYear);
            
            if (orders.isEmpty()) {
                System.out.println("Note: No orders found for this month. Creating an empty report.");
            }

            monthlyReportService.generateHtmlReports(targetMonth, targetYear, orders, waitingList);

        } catch (Exception e) {
            System.err.println("Error generating monthly report: " + e.getMessage());
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