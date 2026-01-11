package server.controller;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import entities.Order;
import entities.WaitingList;
import entities.CustomerType;

/**
 * Service responsible for generating statistical HTML reports.
 * It processes order and waiting list data, creates two HTML reports
 * (Customer/Financial and Time/Performance), and compresses them into a ZIP file.
 */
public class MonthlyReportService {

    /**
     * Main method to generate monthly reports.
     * Calculates statistics, creates the HTML files, and zips the result.
     *
     * @param month       The month of the report.
     * @param year        The year of the report.
     * @param orders      List of orders for the specified month.
     * @param waitingList List of waiting list entries for the specified month.
     */
    public void generateHtmlReports(int month, int year, List<Order> orders, List<WaitingList> waitingList) {
        
        // 1. Setup paths and directories
        String reportsRoot = "server_files/reports/";
        File rootDir = new File(reportsRoot);
        if (!rootDir.exists()) rootDir.mkdirs();

        String folderName = year + "_" + String.format("%02d", month);
        File reportDir = new File(rootDir, folderName);
        if (!reportDir.exists()) reportDir.mkdirs();

        // --- Data Calculation ---
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Calendar cal = Calendar.getInstance();

        // Data for Customer_Report
        double totalRevenue = 0;
        int subscribers = 0, regulars = 0;
        int[] weeklyDistribution = new int[8];
        int[] dailyOrdersCount = new int[32]; 
        int[] dailyWaitingCount = new int[32];

        // Data for Time_Report
        int[] hourlyActivity = new int[24]; 
        int[] punctualityStats = new int[4]; 
        int[] durationStats = new int[3]; 

        // Process Orders
        for (Order o : orders) {
            if (o.getOrderStatus() != Order.OrderStatus.CANCELLED) {
                totalRevenue += o.getTotalPrice();

                if (o.getCustomer() != null && o.getCustomer().getType() == CustomerType.SUBSCRIBER) subscribers++;
                else regulars++;

                if (o.getOrderDate() != null) {
                    cal.setTime(o.getOrderDate());
                    dailyOrdersCount[cal.get(Calendar.DAY_OF_MONTH)]++;
                    weeklyDistribution[cal.get(Calendar.DAY_OF_WEEK)]++;
                }

                if (o.getArrivalTime() != null && o.getOrderDate() != null) {
                    cal.setTime(o.getArrivalTime());
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    if (hour >= 0 && hour < 24) hourlyActivity[hour]++;

                    long diff = o.getArrivalTime().getTime() - o.getOrderDate().getTime();
                    long diffMinutes = diff / (60 * 1000);
                    
                    if (diffMinutes < 0) punctualityStats[0]++;       
                    else if (diffMinutes <= 15) punctualityStats[1]++; 
                    else if (diffMinutes <= 30) punctualityStats[2]++; 
                    else punctualityStats[3]++;                        

                    if (o.getLeavingTime() != null) {
                        long stayDiff = o.getLeavingTime().getTime() - o.getArrivalTime().getTime();
                        long stayMinutes = stayDiff / (60 * 1000);
                        if (stayMinutes <= 60) durationStats[0]++;      
                        else if (stayMinutes <= 120) durationStats[1]++; 
                        else durationStats[2]++;                         
                    }
                }
            }
        }

        // Process Waiting List
        if (waitingList != null) {
            for (WaitingList w : waitingList) {
                if (w.getEnterTime() != null) {
                    cal.setTime(w.getEnterTime());
                    dailyWaitingCount[cal.get(Calendar.DAY_OF_MONTH)]++;
                }
            }
        }

        String absPath = reportDir.getAbsolutePath();
        System.out.println("Generating reports in: " + absPath);
        
        // Generate Customer Report
        createCustomerReport(absPath, month, year, totalRevenue, subscribers, regulars, dailyOrdersCount, dailyWaitingCount, weeklyDistribution, orders);

        // Generate Time Report
        createTimeReport(absPath, month, year, hourlyActivity, punctualityStats, durationStats, waitingList, orders);

        // Create ZIP file
        File zipFile = new File(rootDir, "Report_" + folderName + ".zip");
        try {
            zipDirectory(absPath, zipFile.getAbsolutePath());
            System.out.println("ZIP created: " + zipFile.getAbsolutePath());
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Generates the "Financial & Customer Report" HTML file.
     * Includes revenue data, customer segmentation, and demand trends.
     */
    private void createCustomerReport(String path, int m, int y, double revenue, int subs, int regs, int[] dailyOrds, int[] dailyWait, int[] weeklyDist, List<Order> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHtmlHeader("Financial & Customer Report")); 
        sb.append("<div class='container'><h1>Financial & Customer Report</h1><p class='subtitle'>Period: ").append(m).append("/").append(y).append("</p>");
        
        // Info Cards
        sb.append("<div class='cards'><div class='card green'><h3>Total Revenue</h3><div class='value'>₪").append(String.format("%.2f", revenue)).append("</div></div>");
        sb.append("<div class='card blue'><h3>Total Orders</h3><div class='value'>").append(orders.size()).append("</div></div></div>");

        // Charts
        sb.append("<h2>1. Customer Segmentation</h2><div class='chart-desc'>Subscribers vs Regular customers.</div><div class='chart-box-half'><canvas id='pieChart'></canvas></div>");
        sb.append("<h2>2. Monthly Demand Trends</h2><div class='chart-desc'>Orders vs Waiting List load throughout the month.</div><div class='chart-box-full'><canvas id='trendChart'></canvas></div>");
        sb.append("<h2>3. Weekly Distribution</h2><div class='chart-desc'>Which days are the busiest?</div><div class='chart-box-full'><canvas id='weeklyChart'></canvas></div>");

        // Table
        sb.append("<h2>4. Financial Log</h2>");
        appendOrderTable(sb, orders);
        
        sb.append("</div>");
        sb.append(getChartJsScript());
        sb.append("<script>");
        
        // Chart configurations
        sb.append("new Chart(document.getElementById('pieChart'), { type: 'doughnut', data: { labels: ['Subscribers', 'Regular'], datasets: [{ data: [").append(subs).append(", ").append(regs).append("], backgroundColor: ['#2ecc71', '#95a5a6'] }] }, options: { maintainAspectRatio: false } });");
        sb.append("new Chart(document.getElementById('trendChart'), { type: 'line', data: { labels: ["); 
        for(int i=1; i<=31; i++) sb.append(i).append(",");
        sb.append("], datasets: [{ label: 'Orders', data: [");
        for(int i=1; i<=31; i++) sb.append(dailyOrds[i]).append(",");
        sb.append("], borderColor: '#3498db', fill: false }, { label: 'Waiting List', data: [");
        for(int i=1; i<=31; i++) sb.append(dailyWait[i]).append(",");
        sb.append("], borderColor: '#e67e22', fill: false }] }, options: { maintainAspectRatio: false, scales: { y: { beginAtZero: true, ticks: { precision: 0 } }, x: { title: {display:true, text:'Day of Month'} } } } });");
        sb.append("new Chart(document.getElementById('weeklyChart'), { type: 'bar', data: { labels: ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'], datasets: [{ label: 'Total Orders', data: [");
        for(int i=1; i<=7; i++) sb.append(weeklyDist[i]).append(",");
        sb.append("], backgroundColor: '#9b59b6' }] }, options: { maintainAspectRatio: false, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } } });");
        
        sb.append("</script></body></html>");
        
        saveFile(path + File.separator + "Customer_Report.html", sb.toString());
    }

    /**
     * Generates the "Times & Performance Report" HTML file.
     * Includes arrival times, punctuality statistics, and visit duration.
     */
    private void createTimeReport(String path, int m, int y, int[] hourly, int[] punctuality, int[] duration, List<WaitingList> wList, List<Order> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHtmlHeader("Times & Performance Report")); 
        sb.append("<div class='container'><h1>Performance & Times Report</h1><p class='subtitle'>Period: ").append(m).append("/").append(y).append("</p>");

        int totalLate = punctuality[2] + punctuality[3];
        sb.append("<div class='cards'><div class='card orange'><h3>Waiting List Count</h3><div class='value'>").append(wList == null ? 0 : wList.size()).append("</div></div>");
        sb.append("<div class='card red'><h3>Total Late Arrivals</h3><div class='value'>").append(totalLate).append("</div></div></div>");

        // Charts
        sb.append("<h2>1. Peak Arrival Hours</h2><div class='chart-desc'>When do customers usually arrive?</div><div class='chart-box-full'><canvas id='hourlyChart'></canvas></div>");
        sb.append("<h2>2. Punctuality Analysis</h2><div class='chart-desc'>Breakdown of delays (On Time vs Late).</div><div class='chart-box-half'><canvas id='punctualityChart'></canvas></div>");
        sb.append("<h2>3. Visit Duration</h2><div class='chart-desc'>How long do customers stay?</div><div class='chart-box-half'><canvas id='durationChart'></canvas></div>");

        // Tables
        sb.append("<h2>4. Detailed Time & Delay Log</h2>");
        appendTimeLogTable(sb, orders);
        
        sb.append("<h2>5. Waiting List Log</h2>");
        appendWaitingListTable(sb, wList);

        sb.append("</div>");
        sb.append(getChartJsScript());
        sb.append("<script>");
        
        // Chart configurations
        sb.append("new Chart(document.getElementById('hourlyChart'), { type: 'bar', data: { labels: [");
        for(int i=0; i<24; i++) sb.append("'").append(String.format("%02d:00", i)).append("',");
        sb.append("], datasets: [{ label: 'Arrivals', data: [");
        for(int i=0; i<24; i++) sb.append(hourly[i]).append(",");
        sb.append("], backgroundColor: '#3498db' }] }, options: { maintainAspectRatio: false, scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } } });");
        sb.append("new Chart(document.getElementById('punctualityChart'), { type: 'doughnut', data: { labels: ['Early', 'On Time (0-15m)', 'Late (15-30m)', 'Very Late (30m+)'], datasets: [{ data: [");
        sb.append(punctuality[0]).append(",").append(punctuality[1]).append(",").append(punctuality[2]).append(",").append(punctuality[3]);
        sb.append("], backgroundColor: ['#2ecc71', '#3498db', '#f1c40f', '#e74c3c'] }] }, options: { maintainAspectRatio: false } });");
        sb.append("new Chart(document.getElementById('durationChart'), { type: 'pie', data: { labels: ['Short (<1h)', 'Medium (1-2h)', 'Long (>2h)'], datasets: [{ data: [");
        sb.append(duration[0]).append(",").append(duration[1]).append(",").append(duration[2]);
        sb.append("], backgroundColor: ['#1abc9c', '#9b59b6', '#34495e'] }] }, options: { maintainAspectRatio: false } });");
        
        sb.append("</script></body></html>");
        
        saveFile(path + File.separator + "Time_Report.html", sb.toString());
    }

    // --- Helpers ---

    /**
     * Helper method to create a detailed HTML table showing booking times, actual arrival, and delay calculations.
     */
    private void appendTimeLogTable(StringBuilder sb, List<Order> orders) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sb.append("<table><thead><tr><th>Order ID</th><th>Customer</th><th>Booking Time</th><th>Actual Arrival</th><th>Left At</th><th>Delay</th><th>Status</th></tr></thead><tbody>");
        
        for (Order o : orders) {
            String name = (o.getCustomer() != null) ? o.getCustomer().getName() : "Guest";
            String bookingTime = (o.getOrderDate() != null) ? fmt.format(o.getOrderDate()) : "-";
            String arrivalTime = (o.getArrivalTime() != null) ? fmt.format(o.getArrivalTime()) : "-";
            String leavingTime = (o.getLeavingTime() != null) ? fmt.format(o.getLeavingTime()) : "-";
            
            String delayStr = "-";
            String rowStyle = "";
            
            if (o.getArrivalTime() != null && o.getOrderDate() != null) {
                long diffMillis = o.getArrivalTime().getTime() - o.getOrderDate().getTime();
                long diffMinutes = diffMillis / (60 * 1000);
                
                if (diffMinutes > 0) {
                    delayStr = "+" + diffMinutes + " min";
                    if (diffMinutes > 15) rowStyle = "style='color: #e74c3c; font-weight: bold;'";
                } else if (diffMinutes < 0) {
                    delayStr = diffMinutes + " min (Early)";
                    rowStyle = "style='color: #27ae60;'";
                } else {
                    delayStr = "On Time";
                }
            }

            sb.append("<tr ").append(rowStyle).append(">");
            sb.append("<td>").append(o.getOrderNumber()).append("</td>");
            sb.append("<td>").append(name).append("</td>");
            sb.append("<td>").append(bookingTime).append("</td>");
            sb.append("<td>").append(arrivalTime).append("</td>");
            sb.append("<td>").append(leavingTime).append("</td>");
            sb.append("<td>").append(delayStr).append("</td>");
            sb.append("<td>").append(o.getOrderStatus()).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
    }

    /**
     * Helper method to create a simple financial log table for the report.
     */
    private void appendOrderTable(StringBuilder sb, List<Order> orders) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sb.append("<table><thead><tr><th>ID</th><th>Customer</th><th>Date</th><th>Total</th><th>Status</th></tr></thead><tbody>");
        for (Order o : orders) {
            String name = (o.getCustomer() != null) ? o.getCustomer().getName() : "Guest";
            String date = (o.getOrderDate() != null) ? fmt.format(o.getOrderDate()) : "-";
            sb.append("<tr><td>").append(o.getOrderNumber()).append("</td><td>").append(name).append("</td><td>").append(date).append("</td><td>₪").append(o.getTotalPrice()).append("</td><td>").append(o.getOrderStatus()).append("</td></tr>");
        }
        sb.append("</tbody></table>");
    }

    /**
     * Helper method to create a table displaying waiting list entries.
     */
    private void appendWaitingListTable(StringBuilder sb, List<WaitingList> list) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (list == null || list.isEmpty()) { sb.append("<p>No waiting list entries.</p>"); return; }
        sb.append("<table><thead><tr><th>Time</th><th>Name</th><th>Guests</th></tr></thead><tbody>");
        for (WaitingList w : list) {
            String name = (w.getCustomer() != null) ? w.getCustomer().getName() : "Anonymous";
            String time = (w.getEnterTime() != null) ? fmt.format(w.getEnterTime()) : "-";
            sb.append("<tr><td>").append(time).append("</td><td>").append(name).append("</td><td>").append(w.getNumberOfGuests()).append("</td></tr>");
        }
        sb.append("</tbody></table>");
    }

    /**
     * Returns the standard HTML header with CSS styles for the reports.
     */
    private String getHtmlHeader(String title) {
        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><title>" + title + "</title>" +
               "<style>body { font-family: 'Segoe UI', sans-serif; background: #f0f2f5; padding: 20px; color: #333; }" +
               ".container { max-width: 1000px; margin: 0 auto; background: white; padding: 40px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }" +
               "h1 { text-align: center; color: #2c3e50; } .subtitle { text-align: center; color: #7f8c8d; margin-bottom: 40px; }" +
               ".cards { display: flex; gap: 20px; margin-bottom: 30px; } .card { flex: 1; padding: 20px; text-align: center; background: #f9f9f9; border-radius: 8px; border-top: 5px solid #333; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }" +
               ".card.green { border-color: #2ecc71; } .card.blue { border-color: #3498db; } .card.orange { border-color: #e67e22; } .card.red { border-color: #e74c3c; }" +
               ".card .value { font-size: 28px; font-weight: bold; margin-top: 10px; }" +
               ".chart-desc { background: #eef2f3; padding: 10px; border-left: 4px solid #3498db; margin-bottom: 15px; font-size: 14px; color: #555; }" +
               ".chart-box-full { height: 350px; margin-bottom: 40px; border: 1px solid #eee; padding: 10px; }" +
               ".chart-box-half { height: 300px; width: 60%; margin: 0 auto 40px auto; border: 1px solid #eee; padding: 10px; }" +
               "table { width: 100%; border-collapse: collapse; font-size: 13px; } th, td { padding: 10px; border-bottom: 1px solid #eee; text-align: left; } th { background: #34495e; color: white; }" +
               "</style></head><body>";
    }

    /**
     * Returns the script tag for Chart.js CDN.
     */
    private String getChartJsScript() { return "<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>"; }

    /**
     * Saves a string content to a file.
     */
    private void saveFile(String path, String content) {
        try (FileOutputStream fos = new FileOutputStream(new File(path))) {
            fos.write(content.getBytes("UTF-8"));
            System.out.println("HTML File saved: " + path);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Compresses a directory into a ZIP file.
     */
    private void zipDirectory(String sourceDirPath, String zipFilePath) throws IOException {
        File sourceDir = new File(sourceDirPath);
        File[] files = sourceDir.listFiles();
        if (files == null) return;
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            for (File file : files) {
                if (file.isFile()) {
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) >= 0) zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }
}