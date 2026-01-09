package server.controller;

import java.time.LocalDate;
import java.util.List;
import DAO.OrderDAO;
import DAO.WaitingListDAO;
import entities.Order;
import entities.WaitingList;

public class MonthlyReportThread extends Thread {

    private boolean running = true;
    private final OrderDAO orderDAO = new OrderDAO();
    private final MonthlyReportService monthlyReportService = new MonthlyReportService();
    private final WaitingListDAO waitingListDAO = new WaitingListDAO();
    
    // מעקב כדי לא לייצר את אותו דוח פעמיים באותו יום
    private LocalDate lastRunDate = null; 

    @Override
    public void run() {
        System.out.println("Monthly Report Thread Started.");

        // 1. הרצה מיידית (לצרכי פיתוח - מייצר דוח עבור החודש הקודם)
        // אפשר להעיר את השורה הזו בפרודקשן אם לא רוצים שזה יקרה בכל ריסטארט
        generateReportForPreviousMonth();

        while (running) {
            try {
                LocalDate today = LocalDate.now();

                // התנאי: היום הוא ה-1 לחודש, ועדיין לא רצנו היום
                if (today.getDayOfMonth() == 1 && !today.equals(lastRunDate)) {
                    System.out.println("Auto-Report Trigger: It's the 1st of the month.");
                    generateReportForPreviousMonth();
                    lastRunDate = today;
                }

                // בדיקה פעם ב-12 שעות כדי לא להעמיס על המעבד
                Thread.sleep(1000 * 60 * 60 * 12); 

            } catch (InterruptedException e) {
                running = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void generateReportForPreviousMonth() {
        try {
            LocalDate now = LocalDate.now();
            LocalDate prevMonth = now.minusMonths(1); // הולכים חודש אחורה
            
            int targetMonth = prevMonth.getMonthValue();
            int targetYear = prevMonth.getYear();

            System.out.println("Generating report data for: " + targetMonth + "/" + targetYear);

            List<Order> orders = orderDAO.getFinishedOrdersByMonth(targetMonth, targetYear);
            List<WaitingList> waitingList = waitingListDAO.getWaitingListForReport(targetMonth,targetYear);
            if (orders.isEmpty()) {
                System.out.println("No finished orders found for " + targetMonth + "/" + targetYear + ". Report skipped.");
            } else {
            	monthlyReportService.generateHtmlReport(targetMonth, targetYear, orders,waitingList);
            }

        } catch (Exception e) {
            System.err.println("Failed to generate monthly report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopThread() {
        running = false;
        this.interrupt();
    }
}