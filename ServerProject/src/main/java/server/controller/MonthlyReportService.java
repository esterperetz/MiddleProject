package server.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import entities.Order;
import entities.WaitingList;
import entities.CustomerType;

/**
 * Service that creates monthly HTML reports.
 * It calculates statistics, creates charts based on actual data, and saves the file.
 */
public class MonthlyReportService {

    /**
     * Generates the HTML report file for a specific month.
     * @param month       The month number (1-12).
     * @param year        The year (e.g., 2025).
     * @param orders      List of finished orders for that month.
     * @param waitingList List of waiting list entries for that month.
     * @return The created HTML file, or null if there was an error.
     */
    public File generateHtmlReport(int month, int year, List<Order> orders, List<WaitingList> waitingList) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // --- 1. Statistics Variables ---
        double totalRevenue = 0;
        int subscribers = 0;
        int regulars = 0;
        int lateOrders = 0;
        
        // Array for hourly activity (0-23)
        int[] hourlyActivity = new int[24];
        Arrays.fill(hourlyActivity, 0);

        // Arrays for daily trends (1-31)
        int[] dailyOrdersCount = new int[32]; 
        int[] dailyWaitingCount = new int[32];
        Arrays.fill(dailyOrdersCount, 0);
        Arrays.fill(dailyWaitingCount, 0);

        // Data for delay chart
        StringBuilder delayLabels = new StringBuilder(); 
        StringBuilder delayData = new StringBuilder();   
        
        Calendar cal = Calendar.getInstance();
        // --- 2. Process Orders Data ---
        for (Order o : orders) {
            if (o.getOrderStatus() != Order.OrderStatus.CANCELLED) {
                totalRevenue += o.getTotalPrice();
                
                // Check customer type
                if (o.getCustomer() != null && o.getCustomer().getType() == CustomerType.SUBSCRIBER) {
                    subscribers++;
                } else {
                    regulars++;
                }

                // Daily count
                if (o.getOrderDate() != null) {
                    cal.setTime(o.getOrderDate());
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    dailyOrdersCount[day]++;
                }

                // Time analysis (Peak hours & Delays)
                if (o.getArrivalTime() != null && o.getOrderDate() != null) {
                    // Hourly activity
                    cal.setTime(o.getArrivalTime());
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    if (hour >= 0 && hour < 24) {
                        hourlyActivity[hour]++;
                    }

                    // Calculate delay
                    long diff = o.getArrivalTime().getTime() - o.getOrderDate().getTime();
                    long diffMinutes = diff / (60 * 1000);
                    
                    if (diffMinutes > 15) lateOrders++;
                    
                    // Add to delay chart
                    String name = (o.getCustomer() != null) ? o.getCustomer().getName() : "Guest";
                    delayLabels.append("'").append(name).append(" (#").append(o.getOrderNumber()).append(")',");
                    delayData.append(diffMinutes).append(",");
                }
            }
        }

        // --- 3. Process Waiting List Data ---
        int totalWaiting = 0;
        if (waitingList != null) {
            totalWaiting = waitingList.size();
            for (WaitingList w : waitingList) {
                if (w.getEnterTime() != null) {
                    cal.setTime(w.getEnterTime());
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    dailyWaitingCount[day]++;
                }
            }
        }

        // === 4. Build HTML ===
        sb.append("<!DOCTYPE html>");
        sb.append("<html lang='en'><head><meta charset='UTF-8'>");
        sb.append("<title>Monthly Report</title>");
        sb.append("<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>");

        // === CSS ===
        sb.append("<style>");
        sb.append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f0f2f5; color: #333; margin: 0; padding: 20px; }");
        sb.append(".container { max-width: 1400px; margin: 0 auto; background: white; padding: 40px; border-radius: 12px; box-shadow: 0 5px 20px rgba(0,0,0,0.08); }");
        sb.append("h1 { text-align: center; color: #2c3e50; margin-bottom: 5px; font-size: 32px; }");
        sb.append(".subtitle { text-align: center; color: #7f8c8d; margin-bottom: 50px; font-size: 18px; }");
        sb.append("h2 { color: #34495e; border-bottom: 2px solid #ecf0f1; padding-bottom: 10px; margin-top: 50px; margin-bottom: 20px; }");
        sb.append("h3 { color: #7f8c8d; margin-top: 30px; font-size: 16px; text-transform: uppercase; }");
        
        // CSS עבור ההסברים (Chart Descriptions)
        sb.append(".chart-desc { color: #555; font-size: 13px; margin-bottom: 15px; line-height: 1.5; background-color: #fdfdfd; padding: 10px; border-left: 4px solid #3498db; border-radius: 4px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }");

        sb.append(".cards { display: flex; justify-content: space-between; margin-bottom: 40px; gap: 20px; }");
        sb.append(".card { flex: 1; background: #fff; padding: 25px; border-radius: 10px; text-align: center; box-shadow: 0 4px 10px rgba(0,0,0,0.05); border: 1px solid #eee; }");
        sb.append(".card h3 { margin: 0 0 10px 0; font-size: 14px; text-transform: uppercase; color: #95a5a6; letter-spacing: 1px; }");
        sb.append(".card .value { font-size: 32px; font-weight: bold; color: #2c3e50; }");
        sb.append(".card.green { border-top: 5px solid #2ecc71; }");
        sb.append(".card.blue { border-top: 5px solid #3498db; }");
        sb.append(".card.orange { border-top: 5px solid #e67e22; }");
        sb.append(".card.red { border-top: 5px solid #e74c3c; }");

        sb.append(".charts-row { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 30px; margin-bottom: 40px; }");
        sb.append(".chart-box-half { flex: 1; min-width: 450px; background: #fff; padding: 25px; border-radius: 10px; border: 1px solid #e1e4e8; box-shadow: 0 2px 8px rgba(0,0,0,0.03); }");
        sb.append(".chart-box-full { width: 100%; background: #fff; padding: 30px; border-radius: 10px; border: 1px solid #e1e4e8; box-shadow: 0 2px 8px rgba(0,0,0,0.03); margin-bottom: 40px; }");
        sb.append(".chart-title { font-size: 18px; font-weight: bold; margin-bottom: 10px; color: #2c3e50; }");

        sb.append("table { width: 100%; border-collapse: collapse; margin-top: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); font-size: 13px; }");
        sb.append("th, td { padding: 12px 10px; text-align: left; border-bottom: 1px solid #eee; }");
        sb.append("th { background-color: #34495e; color: white; font-weight: 600; white-space: nowrap; }");
        sb.append("tr:hover { background-color: #f8f9fa; }");
        sb.append(".late { color: #e74c3c; font-weight: bold; }");
        sb.append(".status-paid { color: #27ae60; font-weight: bold; }");
        sb.append(".status-cancelled { color: #c0392b; font-weight: bold; }");
        
        sb.append("</style>");
        sb.append("</head><body>");

        // === Body ===
        sb.append("<div class='container'>");
        sb.append("<h1>Monthly Operations Report</h1>");
        sb.append("<p class='subtitle'>Report Period: ").append(month).append("/").append(year).append("</p>");

        // --- Summary Cards ---
        sb.append("<div class='cards'>");
        sb.append("<div class='card green'><h3>Total Revenue</h3><div class='value'>₪").append(String.format("%.2f", totalRevenue)).append("</div></div>");
        sb.append("<div class='card blue'><h3>Total Orders</h3><div class='value'>").append(orders.size()).append("</div></div>");
        sb.append("<div class='card orange'><h3>Waiting List</h3><div class='value'>").append(totalWaiting).append("</div></div>");
        sb.append("<div class='card red'><h3>Late Arrivals</h3><div class='value'>").append(lateOrders).append("</div></div>");
        sb.append("</div>");

        // --- Charts Section 1 ---
        sb.append("<h2>1. General Overview</h2>");
        sb.append("<div class='charts-row'>");
        
        // גרף 1: פילוח לקוחות
        sb.append("<div class='chart-box-half'>");
        sb.append("<div class='chart-title'>Customer Segmentation</div>");
        sb.append("<div class='chart-desc'>Displays the ratio between Subscribers and Regular customers. Use this to analyze membership program effectiveness.</div>");
        sb.append("<div style='height: 300px'><canvas id='pieChart'></canvas></div>");
        sb.append("</div>");

        // גרף 2: שעות שיא
        sb.append("<div class='chart-box-half'>");
        sb.append("<div class='chart-title'>Peak Activity Hours</div>");
        sb.append("<div class='chart-desc'>Shows the busiest hours based on actual arrival times. Essential for staff scheduling and kitchen preparation.</div>");
        sb.append("<div style='height: 300px'><canvas id='barChart'></canvas></div>");
        sb.append("</div>");
        
        sb.append("</div>");

        // --- Charts Section 2 ---
        sb.append("<h2>2. Performance & Punctuality Analysis</h2>");
        
        // גרף 3: איחורים
        sb.append("<div class='chart-box-full'>");
        sb.append("<div class='chart-title'>Customer Arrival Delays</div>");
        sb.append("<div class='chart-desc'>Red bars indicate late arrivals (bottlenecks), while green bars indicate early arrivals. Helps in managing table turnover efficiency.</div>");
        sb.append("<div style='height: 400px'><canvas id='delayChart'></canvas></div>");
        sb.append("</div>");

        // --- Charts Section 3 ---
        sb.append("<h2>3. Daily Demand Trends</h2>");
        
        // גרף 4: מגמות
        sb.append("<div class='chart-box-full'>");
        sb.append("<div class='chart-title'>Orders vs. Waiting List Load</div>");
        sb.append("<div class='chart-desc'>Compares daily successful orders against waiting list volume. Identifies days where demand exceeded capacity.</div>");
        sb.append("<div style='height: 400px'><canvas id='trendChart'></canvas></div>");
        sb.append("</div>");

        // --- Detailed Logs ---
        sb.append("<h2>4. Detailed Data Logs</h2>");
        
        // --- Table 1: Order Log ---
        sb.append("<h3>Order Log</h3>");
        sb.append("<table><thead><tr><th>ID</th><th>Table</th><th>Guests</th><th>Customer</th><th>Ordered For</th><th>Actual Arrival</th><th>Left At</th><th>Booked On</th><th>Delay</th><th>Total</th><th>Status</th></tr></thead><tbody>");
        
        for (Order o : orders) {
            // Prepare data (check for nulls)
            String name = (o.getCustomer() != null) ? o.getCustomer().getName() : "Guest";
            String orderedFor = (o.getOrderDate() != null) ? fmt.format(o.getOrderDate()) : "-";
            String arrivalTime = (o.getArrivalTime() != null) ? fmt.format(o.getArrivalTime()) : "-";
            String leavingTime = (o.getLeavingTime() != null) ? fmt.format(o.getLeavingTime()) : "-";
            String bookedOn = (o.getDateOfPlacingOrder() != null) ? fmt.format(o.getDateOfPlacingOrder()) : "-";
            String tableNum = (o.getTableNumber() != null) ? String.valueOf(o.getTableNumber()) : "-";
            String guests = String.valueOf(o.getNumberOfGuests());

            // Calculate delay text
            String delayStr = "0 min";
            String rowClass = "";
            if (o.getArrivalTime() != null && o.getOrderDate() != null) {
                long diffMillis = o.getArrivalTime().getTime() - o.getOrderDate().getTime();
                long diffMinutes = diffMillis / (60 * 1000);
                if (diffMinutes > 0) {
                    delayStr = "+" + diffMinutes + " min";
                    if (diffMinutes > 15) rowClass = "class='late'";
                } else if (diffMinutes < 0) {
                    delayStr = diffMinutes + " min (Early)";
                }
            }
            
            String statusClass = "";
            if(o.getOrderStatus() == Order.OrderStatus.PAID) statusClass = "class='status-paid'";
            else if(o.getOrderStatus() == Order.OrderStatus.CANCELLED) statusClass = "class='status-cancelled'";

            sb.append("<tr>");
            sb.append("<td>").append(o.getOrderNumber()).append("</td>");
            sb.append("<td>#").append(tableNum).append("</td>"); 
            sb.append("<td>").append(guests).append("</td>");
            sb.append("<td>").append(name).append("</td>");
            sb.append("<td>").append(orderedFor).append("</td>");
            sb.append("<td>").append(arrivalTime).append("</td>");
            sb.append("<td>").append(leavingTime).append("</td>");
            sb.append("<td>").append(bookedOn).append("</td>");
            sb.append("<td ").append(rowClass).append(">").append(delayStr).append("</td>");
            sb.append("<td>₪").append(o.getTotalPrice()).append("</td>");
            sb.append("<td ").append(statusClass).append(">").append(o.getOrderStatus()).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");

        // --- Table 2: Waiting List Log ---
        sb.append("<h3>Waiting List Log</h3>");
        if (waitingList != null && !waitingList.isEmpty()) {
            sb.append("<table><thead><tr><th>ID</th><th>Entered At</th><th>Customer</th><th>Guests (Places Needed)</th><th>Code</th></tr></thead><tbody>");
            for (WaitingList w : waitingList) {
                String wName = (w.getCustomer() != null) ? w.getCustomer().getName() : "Anonymous";
                String enterTime = (w.getEnterTime() != null) ? fmt.format(w.getEnterTime()) : "-";

                sb.append("<tr>");
                sb.append("<td>").append(w.getWaitingId()).append("</td>");
                sb.append("<td>").append(enterTime).append("</td>");
                sb.append("<td>").append(wName).append("</td>");
                sb.append("<td>").append(w.getNumberOfGuests()).append("</td>");
                sb.append("<td>").append(w.getConfirmationCode()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
        } else {
            sb.append("<p>No waiting list entries.</p>");
        }

        sb.append("</div>"); // Close Container

        // === JavaScript: Charts ===
        sb.append("<script>");
        
        sb.append("new Chart(document.getElementById('pieChart'), { type: 'doughnut', data: { labels: ['Subscribers', 'Regular Customers'], datasets: [{ data: [").append(subscribers).append(", ").append(regulars).append("], backgroundColor: ['#2ecc71', '#95a5a6'] }] }, options: { maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } } });");
        
        sb.append("new Chart(document.getElementById('barChart'), { type: 'bar', data: { labels: [");
        for(int i=0; i<24; i++) sb.append("'").append(String.format("%02d:00", i)).append("',");
        sb.append("], datasets: [{ label: 'Arrivals', data: [");
        for(int i=0; i<24; i++) sb.append(hourlyActivity[i]).append(",");
        sb.append("], backgroundColor: '#3498db', borderRadius: 4 }] }, options: { maintainAspectRatio: false, scales: { y: { beginAtZero: true } } } });");
        
        sb.append("new Chart(document.getElementById('delayChart'), { type: 'bar', data: { labels: [").append(delayLabels).append("], datasets: [{ label: 'Minutes (Positive=Late, Negative=Early)', data: [").append(delayData).append("], backgroundColor: (ctx) => { const v = ctx.raw; return v > 0 ? '#e74c3c' : '#27ae60'; }, borderRadius: 4 }] }, options: { maintainAspectRatio: false, indexAxis: 'y', scales: { x: { title: { display: true, text: 'Minutes' } } } } });");
        
        sb.append("new Chart(document.getElementById('trendChart'), { type: 'line', data: { labels: ["); 
        for(int i=1; i<=31; i++) sb.append("'Day ").append(i).append("',");
        sb.append("], datasets: [{ label: 'Orders', data: [");
        for(int i=1; i<=31; i++) sb.append(dailyOrdersCount[i]).append(",");
        sb.append("], borderColor: '#3498db', tension: 0.3, fill: true, backgroundColor: 'rgba(52, 152, 219, 0.1)' }, { label: 'Waiting List Entries', data: [");
        for(int i=1; i<=31; i++) sb.append(dailyWaitingCount[i]).append(",");
        sb.append("], borderColor: '#e67e22', tension: 0.3, fill: false }] }, options: { maintainAspectRatio: false, scales: { y: { beginAtZero: true } } } });");

        sb.append("</script>");
        sb.append("</body></html>");

        String fileName = "Report_" + year + "_" + String.format("%02d", month) + ".html";
        File file = new File("server_files/reports/" + fileName);
        
        // Create directory if it doesn't exist
        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(sb.toString().getBytes("UTF-8"));
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}